package com.hashmimotors.app.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.remote.BarcodeLookupApi
import com.hashmimotors.app.data.remote.OnlineProduct
import com.hashmimotors.app.data.repository.PartRepository
import com.hashmimotors.app.domain.model.Part
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanUiState(
    val raw: String? = null,
    val matchedPart: Part? = null,
    val isLookingUpOnline: Boolean = false,
    val onlineProduct: OnlineProduct? = null,
    val error: String? = null
)

@HiltViewModel
class BarcodeScannerViewModel @Inject constructor(
    private val partRepo: PartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private var lastRaw: String = ""

    /**
     * Called from the camera analyzer whenever a barcode is decoded.
     * Debounces duplicates and looks up the code in the local catalog.
     */
    fun onBarcode(raw: String) {
        val code = raw.trim()
        if (code.isBlank() || code == lastRaw) return
        lastRaw = code
        viewModelScope.launch {
            val part = partRepo.getPartByBarcode(code)
            _uiState.value = ScanUiState(raw = code, matchedPart = part)
        }
    }

    /** Attempts to identify an unknown barcode using free online catalogs. */
    fun lookupOnline() {
        val code = _uiState.value.raw ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLookingUpOnline = true,
                error = null,
                onlineProduct = null
            )
            val product = BarcodeLookupApi.lookup(code)
            _uiState.value = _uiState.value.copy(
                isLookingUpOnline = false,
                onlineProduct = product,
                error = if (product == null)
                    "Couldn't identify this barcode online. You can still add it manually."
                else null
            )
        }
    }

    /** Clears the current result so the user can scan another item. */
    fun clearResult() {
        lastRaw = ""
        _uiState.value = ScanUiState()
    }
}
