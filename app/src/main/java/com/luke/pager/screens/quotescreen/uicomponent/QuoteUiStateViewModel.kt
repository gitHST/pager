package com.luke.pager.screens.quotescreen.uicomponent

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class QuoteUiStateViewModel : ViewModel() {

    private val _showQuoteModal = MutableStateFlow(false)
    val showQuoteModal = _showQuoteModal.asStateFlow()

    private val _showScanModal = MutableStateFlow(false)
    val showScanModal = _showScanModal.asStateFlow()

    private val _selectedBookId = MutableStateFlow<Long?>(null)
    val selectedBookId = _selectedBookId.asStateFlow()

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex = _selectedTabIndex.asStateFlow()

    val overlayAlpha = MutableStateFlow(0f)

    private val _isFabExpanded = MutableStateFlow(false)
    val isFabExpanded = _isFabExpanded.asStateFlow()

    private val _fullyCollapsed = MutableStateFlow(true)
    val fullyCollapsed = _fullyCollapsed.asStateFlow()

    private val _showFabActions = MutableStateFlow(false)
    val showFabActions = _showFabActions.asStateFlow()

    fun setFabExpanded(expanded: Boolean) {
        _isFabExpanded.value = expanded
    }

    fun setFullyCollapsed(collapsed: Boolean) {
        _fullyCollapsed.value = collapsed
    }

    fun setShowFabActions(show: Boolean) {
        _showFabActions.value = show
    }

    fun setShowQuoteModal(show: Boolean) {
        _showQuoteModal.value = show
        updateOverlayAlpha()
    }

    fun setShowScanModal(show: Boolean) {
        _showScanModal.value = show
        updateOverlayAlpha()
    }

    fun setSelectedBookId(id: Long?) {
        _selectedBookId.value = id
    }

    fun setSelectedTabIndex(index: Int) {
        _selectedTabIndex.value = index
    }

    private fun updateOverlayAlpha() {
        overlayAlpha.value = if (_showQuoteModal.value || _showScanModal.value) 0.5f else 0f
    }

    fun reset() {
        _showQuoteModal.value = false
        _showScanModal.value = false
        _selectedTabIndex.value = 0
    }
}