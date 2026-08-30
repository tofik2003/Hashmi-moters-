package com.hashmimotors.app.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.repository.CategoryRepository
import com.hashmimotors.app.data.repository.PartRepository
import com.hashmimotors.app.domain.model.Category
import com.hashmimotors.app.domain.model.Part
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CatalogUiState(
    val parts: List<Part> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val showLowStockOnly: Boolean = false,
    val isLoading: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val partRepo: PartRepository,
    private val categoryRepo: CategoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    private val _showLowStockOnly = MutableStateFlow(false)

    val uiState: StateFlow<CatalogUiState> = combine(
        _searchQuery,
        _selectedCategoryId,
        _showLowStockOnly
    ) { q, catId, lowOnly ->
        Triple(q, catId, lowOnly)
    }.flatMapLatest { (q, catId, lowOnly) ->
        val partsFlow = when {
            lowOnly -> partRepo.getLowStock()
            else -> partRepo.getAllParts()
        }
        combine(partsFlow, categoryRepo.getAllCategories()) { allParts, categories ->
            val byCategory = if (catId != null) allParts.filter { it.categoryId == catId } else allParts
            val filtered = if (q.isNotBlank()) FuzzySearch.rank(byCategory, q) else byCategory
            CatalogUiState(
                parts = filtered,
                categories = categories,
                selectedCategoryId = catId,
                searchQuery = q,
                showLowStockOnly = lowOnly
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CatalogUiState()
    )

    fun onSearchChange(query: String) {
        _searchQuery.value = query
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

    fun deletePart(part: Part) {
        viewModelScope.launch {
            partRepo.deletePart(part)
        }
    }
}
