package com.hashmimotors.app.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.local.SearchHistoryStore
import com.hashmimotors.app.data.repository.CategoryRepository
import com.hashmimotors.app.data.repository.PartRepository
import com.hashmimotors.app.data.seed.ReferenceDataRepository
import com.hashmimotors.app.data.seed.SeedCategory
import com.hashmimotors.app.data.seed.SeedPartReference
import com.hashmimotors.app.domain.model.Category
import com.hashmimotors.app.domain.model.Part
import com.hashmimotors.app.util.Barcodes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortMode(val label: String) {
    RELEVANCE("Relevance"),
    NAME("Name A–Z"),
    PRICE_ASC("Price ↑"),
    PRICE_DESC("Price ↓"),
    STOCK_DESC("Stock ↑")
}

enum class StockFilter(val label: String) {
    ALL("All"),
    IN_STOCK("In stock"),
    LOW("Low stock"),
    OUT("Out of stock")
}

data class CatalogUiState(
    val parts: List<Part> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val sortMode: SortMode = SortMode.RELEVANCE,
    val stockFilter: StockFilter = StockFilter.ALL,
    val brandFilter: String? = null,
    val availableBrands: List<String> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    // Smart features
    val recentlyScannedParts: List<Part> = emptyList(),
    val quickInOutItems: List<Part> = emptyList(),
    val favoriteParts: List<Part> = emptyList(),
    val isSmartSearchEnabled: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val partRepo: PartRepository,
    private val categoryRepo: CategoryRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val searchHistoryStore: SearchHistoryStore
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    private val _sortMode = MutableStateFlow(SortMode.RELEVANCE)
    private val _brandFilter = MutableStateFlow<String?>(null)
    private val _stockFilter = MutableStateFlow(StockFilter.ALL)
    private val _recentSearches = MutableStateFlow(searchHistoryStore.get())
    private val _useSmartSearch = MutableStateFlow(true)

    /** Immediate query, safe to bind to a TextField without input lag. */
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val debouncedQuery = _searchQuery
        .debounce(250)
        .distinctUntilChanged()

    val uiState: StateFlow<CatalogUiState> = combine(
        debouncedQuery,
        _selectedCategoryId,
        _sortMode,
        _brandFilter,
        _stockFilter,
        _useSmartSearch
    ) { q, catId, sort, brand, stock, useSmart ->
        Params(q, catId, sort, brand, stock, useSmart)
    }.flatMapLatest { params ->
        val partsFlow = if (params.query.isNotBlank()) {
            if (params.useSmart) {
                partRepo.smartSearchParts(params.query)
            } else {
                partRepo.searchParts(params.query)
            }
        } else {
            partRepo.getAllParts()
        }
        
        // Combine with smart feature data streams
        combine(
            partsFlow, 
            categoryRepo.getAllCategories(), 
            partRepo.getAllParts(),
            partRepo.getRecentlyScannedParts(15),
            partRepo.getQuickInOutItems(15),
            partRepo.getFavoriteParts()
        ) { parts, categories, allParts, recentScanned, quickInOut, favorites ->
            val brands = allParts.mapNotNull { it.brand }
                .distinct()
                .sortedBy { it.lowercase() }

            var filtered = parts
            if (params.categoryId != null) {
                filtered = filtered.filter { it.categoryId == params.categoryId }
            }
            filtered = when (params.stock) {
                StockFilter.ALL -> filtered
                StockFilter.IN_STOCK -> filtered.filter { it.stockQty > 0 }
                StockFilter.LOW -> filtered.filter { it.stockQty in 1..it.reorderLevel }
                StockFilter.OUT -> filtered.filter { it.stockQty == 0 }
            }
            if (params.brand != null) {
                filtered = filtered.filter { it.brand == params.brand }
            }
            filtered = sortParts(filtered, params.sort)

            CatalogUiState(
                parts = filtered,
                categories = categories,
                selectedCategoryId = params.categoryId,
                sortMode = params.sort,
                stockFilter = params.stock,
                brandFilter = params.brand,
                availableBrands = brands,
                recentlyScannedParts = recentScanned,
                quickInOutItems = quickInOut,
                favoriteParts = favorites,
                isSmartSearchEnabled = params.useSmart
            )
        }
    }.combine(_recentSearches) { state, recent ->
        state.copy(recentSearches = recent)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CatalogUiState()
    )

    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }

    fun onSearchSubmit(query: String) {
        if (query.isBlank()) return
        searchHistoryStore.add(query)
        _recentSearches.value = searchHistoryStore.get()
    }

    fun selectRecentSearch(query: String) {
        searchHistoryStore.add(query)
        _recentSearches.value = searchHistoryStore.get()
        _searchQuery.value = query
    }

    fun clearRecentSearches() {
        searchHistoryStore.clear()
        _recentSearches.value = emptyList()
    }

    fun onCategorySelect(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    fun onSortChange(mode: SortMode) {
        _sortMode.value = mode
    }

    fun onBrandFilter(brand: String?) {
        _brandFilter.value = brand
    }

    fun onStockFilter(stock: StockFilter) {
        _stockFilter.value = stock
    }

    fun toggleSmartSearch() {
        _useSmartSearch.value = !_useSmartSearch.value
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

    /** Record a scan for analytics (called when part is scanned/used) */
    fun recordPartScan(partId: String) {
        viewModelScope.launch {
            partRepo.recordScan(partId)
        }
    }

    /** Record a sale for analytics */
    fun recordPartSale(partId: String, qty: Int) {
        viewModelScope.launch {
            partRepo.recordSale(partId, qty)
        }
    }

    /** Toggle favorite status */
    fun toggleFavorite(partId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            partRepo.setFavorite(partId, !currentStatus)
        }
    }

    // ---- Duplicate detection & auto-generation helpers (used by Add Part) ----

    suspend fun findByBarcode(barcode: String): Part? = partRepo.findByBarcodeOnce(barcode)

    suspend fun findBySku(sku: String): Part? = partRepo.findBySkuOnce(sku)

    suspend fun findByName(name: String): Part? = partRepo.findByNameOnce(name)

    suspend fun nextSku(): String = partRepo.nextSku()

    fun generateBarcode(): String = Barcodes.generateEan13()

    // ---- Reference catalog (quick-add / suggestions) ----

    /**
     * Search the bundled common-parts reference catalog. Used for the
     * "quick add" suggestions in Add Part and search type-ahead.
     */
    fun searchReferenceParts(query: String, limit: Int = 12): List<SeedPartReference> =
        referenceDataRepository.searchParts(query, limit)

    /**
     * Default HSN code / GST rate for a category (used to auto-fill Add Part).
     */
    fun categoryDefaults(categoryName: String): SeedCategory? =
        referenceDataRepository.defaultsForCategory(categoryName)

    private fun sortParts(list: List<Part>, mode: SortMode): List<Part> = when (mode) {
        SortMode.RELEVANCE -> list
        SortMode.NAME -> list.sortedBy { it.name.lowercase() }
        SortMode.PRICE_ASC -> list.sortedBy { it.sellingPrice }
        SortMode.PRICE_DESC -> list.sortedByDescending { it.sellingPrice }
        SortMode.STOCK_DESC -> list.sortedByDescending { it.stockQty }
    }

    private data class Params(
        val query: String,
        val categoryId: String?,
        val sort: SortMode,
        val brand: String?,
        val stock: StockFilter,
        val useSmart: Boolean = true
    )
}
