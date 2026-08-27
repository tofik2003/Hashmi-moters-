package com.hashmimotors.app.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.repository.PartRepository
import com.hashmimotors.app.domain.model.Part
import com.hashmimotors.app.ui.sound.SoundEffect
import com.hashmimotors.app.ui.sound.SoundManager
import com.hashmimotors.app.util.SmartSearch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ScanAction { RECEIVE, CHECKOUT, LOOKUP, IDENTIFY }

data class ScanEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val partName: String,
    val delta: Int,
    val stockAfter: Int?,
    val at: Long = System.currentTimeMillis()
)

data class ScanStationUiState(
    val action: ScanAction = ScanAction.RECEIVE,
    val qty: Int = 1,
    val lastPart: Part? = null,
    val message: String = "Point at a barcode or part label",
    val unknownCode: String? = null,
    val identifyHits: List<Part> = emptyList(),
    val ocrSnippet: String = "",
    val session: List<ScanEvent> = emptyList(),
    val catalogSize: Int = 0
)

@HiltViewModel
class ScanStationViewModel @Inject constructor(
    private val partRepo: PartRepository,
    private val soundManager: SoundManager
) : ViewModel() {

    private val _action = MutableStateFlow(ScanAction.RECEIVE)
    private val _qty = MutableStateFlow(1)
    private val _lastPart = MutableStateFlow<Part?>(null)
    private val _message = MutableStateFlow("Point at a barcode or part label")
    private val _unknown = MutableStateFlow<String?>(null)
    private val _hits = MutableStateFlow<List<Part>>(emptyList())
    private val _ocr = MutableStateFlow("")
    private val _session = MutableStateFlow<List<ScanEvent>>(emptyList())
    private val catalog = partRepo.getAllParts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<ScanStationUiState> = combine(
        combine(_action, _qty, _lastPart, _message, _unknown) { action, qty, last, msg, unknown ->
            Quint(action, qty, last, msg, unknown)
        },
        combine(_hits, _ocr, _session, catalog) { hits, ocr, session, parts ->
            Quad(hits, ocr, session, parts.size)
        }
    ) { a, b ->
        ScanStationUiState(
            action = a.action,
            qty = a.qty,
            lastPart = a.last,
            message = a.msg,
            unknownCode = a.unknown,
            identifyHits = b.hits,
            ocrSnippet = b.ocr,
            session = b.session,
            catalogSize = b.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScanStationUiState())

    fun setAction(action: ScanAction) {
        _action.value = action
        _unknown.value = null
        _hits.value = emptyList()
        _message.value = when (action) {
            ScanAction.RECEIVE -> "Scan to add stock"
            ScanAction.CHECKOUT -> "Scan to take stock out"
            ScanAction.LOOKUP -> "Scan to look up a part"
            ScanAction.IDENTIFY -> "Point at a label or part — text + barcode"
        }
    }

    fun setQty(qty: Int) {
        _qty.value = qty.coerceIn(1, 99)
    }

    fun onBarcode(code: String) {
        val trimmed = code.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            if (_action.value == ScanAction.IDENTIFY) {
                identify(trimmed)
                return@launch
            }
            val part = match(trimmed)
            if (part == null) {
                _unknown.value = trimmed
                _lastPart.value = null
                _message.value = "Unknown code $trimmed"
                soundManager.play(SoundEffect.ERROR)
                return@launch
            }
            _unknown.value = null
            applyKnown(part)
        }
    }

    fun onOcr(text: String) {
        if (_action.value != ScanAction.IDENTIFY) return
        val snippet = text.trim()
        if (snippet.length < 3) return
        viewModelScope.launch {
            _ocr.value = snippet.take(240)
            val q = SmartSearch.queryFromOcr(snippet)
            val hits = SmartSearch.rank(catalog.value, q).take(8)
            _hits.value = hits
            _message.value = if (hits.isEmpty()) "No catalog match for this label" else "${hits.size} possible match${if (hits.size == 1) "" else "es"}"
        }
    }

    fun applyHit(part: Part) {
        viewModelScope.launch {
            val fresh = partRepo.getPartByIdOnce(part.id) ?: part
            when (_action.value) {
                ScanAction.IDENTIFY, ScanAction.LOOKUP -> {
                    _lastPart.value = fresh
                    _message.value = "${fresh.name} · ${fresh.stockQty} in stock"
                    soundManager.play(SoundEffect.SUCCESS)
                }
                else -> applyKnown(fresh)
            }
        }
    }

    fun dismissUnknown() {
        _unknown.value = null
    }

    private suspend fun identify(code: String) {
        val part = match(code)
        if (part != null) {
            _lastPart.value = part
            _hits.value = listOf(part)
            _message.value = "${part.name} · ${part.stockQty} in stock"
            soundManager.play(SoundEffect.SUCCESS)
        } else {
            val hits = SmartSearch.rank(catalog.value, code).take(8)
            _hits.value = hits
            _message.value = if (hits.isEmpty()) "Unknown $code" else "Close matches for $code"
            if (hits.isEmpty()) {
                _unknown.value = code
                soundManager.play(SoundEffect.ERROR)
            } else soundManager.play(SoundEffect.NOTIFICATION)
        }
    }

    private suspend fun applyKnown(part: Part) {
        val qty = _qty.value
        when (_action.value) {
            ScanAction.RECEIVE -> {
                partRepo.addStock(part.id, qty, "default-user", reason = "Fast scan +$qty")
                val after = (part.stockQty + qty)
                _lastPart.value = part.copy(stockQty = after)
                _message.value = "+$qty ${part.name} → $after"
                pushEvent(part.name, qty, after)
                soundManager.play(SoundEffect.SUCCESS)
            }
            ScanAction.CHECKOUT -> {
                partRepo.adjustStock(part.id, -qty, "Fast scan −$qty", "default-user")
                val after = (part.stockQty - qty)
                _lastPart.value = part.copy(stockQty = after)
                _message.value = "−$qty ${part.name} → $after"
                pushEvent(part.name, -qty, after)
                soundManager.play(SoundEffect.WHOOSH)
            }
            ScanAction.LOOKUP, ScanAction.IDENTIFY -> {
                _lastPart.value = part
                _message.value = "${part.name} · ${part.stockQty} in stock · ₹${"%,.0f".format(part.sellingPrice)}"
                soundManager.play(SoundEffect.TAP)
            }
        }
    }

    private suspend fun match(code: String): Part? {
        partRepo.getPartByBarcode(code)?.let { return it }
        val all = catalog.value
        return all.firstOrNull { it.sku.equals(code, ignoreCase = true) }
            ?: all.firstOrNull { it.oemNumbers.any { oem -> oem.equals(code, ignoreCase = true) } }
            ?: SmartSearch.rank(all, code).firstOrNull()?.takeIf {
                it.barcode.equals(code, true) || it.sku.equals(code, true) ||
                    it.oemNumbers.any { oem -> oem.equals(code, true) }
            }
    }

    private fun pushEvent(name: String, delta: Int, after: Int) {
        _session.update { (listOf(ScanEvent(partName = name, delta = delta, stockAfter = after)) + it).take(12) }
    }

    private data class Quint(
        val action: ScanAction,
        val qty: Int,
        val last: Part?,
        val msg: String,
        val unknown: String?
    )

    private data class Quad(
        val hits: List<Part>,
        val ocr: String,
        val session: List<ScanEvent>,
        val size: Int
    )
}
