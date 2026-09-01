package com.hashmimotors.app.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.repository.CategoryRepository
import com.hashmimotors.app.data.repository.PartRepository
import com.hashmimotors.app.data.seed.ReferenceDataRepository
import com.hashmimotors.app.domain.model.Part
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Saves scanned bill lines as catalog parts, auto-guessing category, HSN and
 * GST from the bundled reference data.
 */
@HiltViewModel
class BillScannerViewModel @Inject constructor(
    private val partRepo: PartRepository,
    private val categoryRepo: CategoryRepository,
    private val referenceDataRepository: ReferenceDataRepository
) : ViewModel() {

    fun saveAsParts(lines: List<ScannedLine>, onDone: () -> Unit) {
        viewModelScope.launch {
            val categories = categoryRepo.getAllCategories().first()
            lines.filter { it.name.isNotBlank() }.forEach { line ->
                val match = referenceDataRepository.searchParts(line.name, limit = 1).firstOrNull()
                val categoryId = categories
                    .firstOrNull { it.name.equals(match?.category, ignoreCase = true) }
                    ?.id
                val price = if (line.rate > 0.0) line.rate else line.amount

                partRepo.savePart(
                    Part(
                        name = line.name.trim(),
                        categoryId = categoryId,
                        mrp = price,
                        sellingPrice = price,
                        gstPercent = match?.gstPercent ?: 0.0,
                        hsnCode = match?.hsnCode,
                        stockQty = line.qty,
                        notes = "Scanned from bill"
                    )
                )
            }
            onDone()
        }
    }
}
