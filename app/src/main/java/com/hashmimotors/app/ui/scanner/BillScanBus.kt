package com.hashmimotors.app.ui.scanner

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * In-memory hand-off from the bill scanner to the billing screen.
 * The scanner publishes parsed lines here; the billing screen consumes them
 * into its cart on the next composition.
 */
object BillScanBus {
    private val _items = MutableStateFlow<List<ScannedLine>>(emptyList())
    val items: StateFlow<List<ScannedLine>> = _items

    fun publish(lines: List<ScannedLine>) {
        _items.value = lines
    }

    fun consume() {
        _items.value = emptyList()
    }
}
