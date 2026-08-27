package com.hashmimotors.app.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.repository.CategoryRepository
import com.hashmimotors.app.data.repository.PartRepository
import com.hashmimotors.app.data.seed.DemoCatalogSeeder
import com.hashmimotors.app.domain.model.Category
import com.hashmimotors.app.domain.model.Part
import com.hashmimotors.app.util.SmartSearch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CatalogUiState(
    val parts: List<Part> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val showLowStockOnly: Boolean = false,
    val isLoading: Boolean = false,
    val suggestions: List<String> = emptyList(),
    val recent: List<String> = emptyList(),
    val matchHint: String? = null
)

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val partRepo: PartRepository,
    private val categoryRepo: CategoryRepository,
    private val demoSeeder: DemoCatalogSeeder
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    private val _showLowStockOnly = MutableStateFlow(false)
    private val _recent = MutableStateFlow<List<String>>(emptyList())

    val uiState: StateFlow<CatalogUiState> = combine(
        combine(
            partRepo.getAllParts(),
            partRepo.getLowStock(),
            categoryRepo.getAllCategories()
        ) { all, low, cats -> Triple(all, low, cats) },
        combine(_searchQuery, _selectedCategoryId, _showLowStockOnly, _recent) { q, cat, lowOnly, recent ->
            Filter(q, cat, lowOnly, recent)
        }
    ) { pack, filter ->
        val source = if (filter.lowOnly) pack.second else pack.first
        val ranked = SmartSearch.rank(source, filter.q)
        val filtered = if (filter.cat != null) ranked.filter { it.categoryId == filter.cat } else ranked
        CatalogUiState(
            parts = filtered,
            categories = pack.third,
            selectedCategoryId = filter.cat,
            searchQuery = filter.q,
            showLowStockOnly = filter.lowOnly,
            suggestions = SmartSearch.suggestions(pack.first, filter.q),
            recent = filter.recent,
            matchHint = SmartSearch.hint(filter.q, filtered.size)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CatalogUiState()
    )

    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }

    fun rememberQuery() {
        val q = _searchQuery.value.trim()
        if (q.length < 2) return
        _recent.value = (listOf(q) + _recent.value.filter { !it.equals(q, ignoreCase = true) }).take(8)
    }

    fun onCategorySelect(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    fun toggleLowStockOnly() {
        _showLowStockOnly.value = !_showLowStockOnly.value
    }

    fun savePart(part: Part) {
        viewModelScope.launch {
            partRepo.savePart(part)
        }
    }

    suspend fun savePartNow(part: Part) {
        partRepo.savePart(part)
    }

    fun deletePart(part: Part) {
        viewModelScope.launch {
            partRepo.deletePart(part)
        }
    }

    suspend fun findByBarcode(barcode: String): Part? = partRepo.getPartByBarcode(barcode)

    fun importParts(parts: List<Part>) {
        viewModelScope.launch { partRepo.saveAllParts(parts) }
    }

    suspend fun seedDemoCatalog(): Int = demoSeeder.seedIfEmpty()

    private data class Filter(
        val q: String,
        val cat: String?,
        val lowOnly: Boolean,
        val recent: List<String>
    )
}
