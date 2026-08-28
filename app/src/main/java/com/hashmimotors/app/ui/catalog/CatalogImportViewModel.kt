package com.hashmimotors.app.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.repository.CategoryRepository
import com.hashmimotors.app.data.repository.PartRepository
import com.hashmimotors.app.domain.model.Part
import com.hashmimotors.app.util.CsvParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A single parsed row that is ready to become a [Part].
 */
data class ImportRow(
    val name: String,
    val sku: String = "",
    val oemNumbers: List<String> = emptyList(),
    val brand: String? = null,
    val category: String? = null,
    val mrp: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val stockQty: Int = 0,
    val reorderLevel: Int = 5,
    val hsnCode: String? = null,
    val barcode: String? = null,
    val notes: String? = null
)

data class CatalogImportUiState(
    val isLoading: Boolean = false,
    val parsedRows: List<ImportRow> = emptyList(),
    val errors: List<String> = emptyList(),
    val importedCount: Int = 0,
    val message: String? = null
)

@HiltViewModel
class CatalogImportViewModel @Inject constructor(
    private val partRepo: PartRepository,
    private val categoryRepo: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogImportUiState())
    val uiState: StateFlow<CatalogImportUiState> = _uiState.asStateFlow()

    /**
     * Parses raw CSV text into preview rows. Does not persist anything yet.
     */
    fun parseCsv(text: String) {
        val rawRows = CsvParser.parse(text)
        if (rawRows.isEmpty()) {
            _uiState.value = CatalogImportUiState(
                errors = listOf("No data found. Please paste valid CSV content."),
                message = "Nothing to import"
            )
            return
        }

        val header = rawRows.first().map { it.trim().lowercase() }
        val columnMap = mapHeader(header)
        val errors = mutableListOf<String>()

        if (!columnMap.containsKey("name")) {
            _uiState.value = CatalogImportUiState(
                errors = listOf(
                    "A 'name' column is required. Expected one of: " +
                        "name, part_name, part."
                ),
                message = "Missing required 'name' column"
            )
            return
        }

        val dataRows = if (isHeaderRow(header)) rawRows.drop(1) else rawRows
        val parsed = mutableListOf<ImportRow>()

        dataRows.forEachIndexed { index, fields ->
            val get = { key: String -> columnMap[key]?.let { fields.getOrNull(it)?.trim() } }
            val name = get("name").orEmpty()
            if (name.isBlank()) {
                if (fields.any { it.isNotBlank() }) {
                    errors.add("Row ${index + 1}: skipped — empty part name")
                }
                return@forEachIndexed
            }
            parsed.add(
                ImportRow(
                    name = name,
                    sku = get("sku").orEmpty(),
                    oemNumbers = get("oem")
                        .orEmpty()
                        .split(';', '|', ',')
                        .map { it.trim() }
                        .filter { it.isNotBlank() },
                    brand = get("brand")?.ifBlank { null },
                    category = get("category")?.ifBlank { null },
                    mrp = get("mrp")?.toDoubleOrNull() ?: 0.0,
                    sellingPrice = get("sellingPrice")?.toDoubleOrNull() ?: 0.0,
                    stockQty = get("stock")?.toIntOrNull() ?: 0,
                    reorderLevel = get("reorderLevel")?.toIntOrNull() ?: 5,
                    hsnCode = get("hsn")?.ifBlank { null },
                    barcode = get("barcode")?.ifBlank { null },
                    notes = get("notes")?.ifBlank { null }
                )
            )
        }

        _uiState.value = CatalogImportUiState(
            parsedRows = parsed,
            errors = errors,
            message = if (parsed.isEmpty() && errors.isEmpty()) "No valid rows found" else null
        )
    }

    /**
     * Persists the previewed rows as real parts. Categories referenced by name are
     * resolved or created on the fly.
     */
    fun importRows(rows: List<ImportRow>) {
        if (rows.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val categoryCache = mutableMapOf<String, String?>()
            val parts = rows.map { row ->
                val categoryId = row.category?.let { cat ->
                    categoryCache.getOrPut(cat.lowercase()) {
                        categoryRepo.getOrCreateCategory(cat)?.id
                    }
                }
                Part(
                    id = java.util.UUID.randomUUID().toString(),
                    sku = row.sku,
                    name = row.name,
                    oemNumbers = row.oemNumbers,
                    brand = row.brand,
                    categoryId = categoryId,
                    mrp = row.mrp,
                    sellingPrice = row.sellingPrice,
                    hsnCode = row.hsnCode,
                    stockQty = row.stockQty,
                    reorderLevel = row.reorderLevel,
                    barcode = row.barcode,
                    notes = row.notes
                )
            }
            partRepo.saveAllParts(parts)
            _uiState.value = CatalogImportUiState(
                isLoading = false,
                importedCount = parts.size,
                message = "Imported ${parts.size} part${if (parts.size == 1) "" else "s"}"
            )
        }
    }

    fun reset() {
        _uiState.value = CatalogImportUiState()
    }

    private fun mapHeader(header: List<String>): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        header.forEachIndexed { index, col ->
            HEADER_ALIASES[col]?.let { canonical ->
                // Prefer the first matching column for a given canonical field.
                if (!result.containsKey(canonical)) result[canonical] = index
            }
        }
        return result
    }

    private fun isHeaderRow(header: List<String>): Boolean {
        return header.any { HEADER_ALIASES.containsKey(it) }
    }

    companion object {
        val HEADER_ALIASES = mapOf(
            "name" to "name", "partname" to "name", "part_name" to "name",
            "part" to "name", "description" to "name",
            "sku" to "sku", "code" to "sku", "partcode" to "sku", "part_code" to "sku",
            "oem" to "oem", "oemno" to "oem", "oemnumber" to "oem",
            "oemnumbers" to "oem", "oem_numbers" to "oem",
            "brand" to "brand", "make" to "brand",
            "category" to "category", "cat" to "category",
            "mrp" to "mrp", "listprice" to "mrp", "list_price" to "mrp",
            "sellingprice" to "sellingPrice", "selling_price" to "sellingPrice",
            "ourprice" to "sellingPrice", "our_price" to "sellingPrice",
            "price" to "sellingPrice",
            "stock" to "stock", "qty" to "stock", "stockqty" to "stock",
            "quantity" to "stock",
            "reorder" to "reorderLevel", "reorderlevel" to "reorderLevel",
            "reorder_level" to "reorderLevel", "minstock" to "reorderLevel",
            "min_stock" to "reorderLevel",
            "hsn" to "hsn", "hsncode" to "hsn", "hsn_code" to "hsn",
            "barcode" to "barcode", "ean" to "barcode", "upc" to "barcode",
            "notes" to "notes", "note" to "notes"
        )
    }
}
