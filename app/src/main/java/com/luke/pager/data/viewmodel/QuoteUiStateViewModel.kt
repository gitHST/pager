package com.luke.pager.data.viewmodel

import androidx.lifecycle.ViewModel
import com.luke.pager.screens.quotescreen.scan.imageprocessing.staticdataclasses.ScanPage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class QuoteUiStateViewModel : ViewModel() {
    private val _showQuoteModal = MutableStateFlow(false)
    val showQuoteModal = _showQuoteModal.asStateFlow()

    private val _selectedBookId = MutableStateFlow<String?>(null)
    val selectedBookId: StateFlow<String?> = _selectedBookId.asStateFlow()

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex = _selectedTabIndex.asStateFlow()

    val overlayAlpha = MutableStateFlow(0f)

    private val _isFabExpanded = MutableStateFlow(false)
    val isFabExpanded = _isFabExpanded.asStateFlow()

    private val _fullyCollapsed = MutableStateFlow(true)
    val fullyCollapsed = _fullyCollapsed.asStateFlow()

    private val _showFabActions = MutableStateFlow(false)
    val showFabActions = _showFabActions.asStateFlow()

    private val _isSortAscending = MutableStateFlow(false)
    val isSortAscending: StateFlow<Boolean> = _isSortAscending

    private val _capturedImageUri = MutableStateFlow<String?>(null)
    val capturedImageUri: StateFlow<String?> = _capturedImageUri

    private val _scannedPages = MutableStateFlow<List<ScanPage>>(emptyList())
    val scannedPages: StateFlow<List<ScanPage>> = _scannedPages

    private val _prefilledQuoteText = MutableStateFlow("")
    val prefilledQuoteText = _prefilledQuoteText.asStateFlow()

    fun setPrefilledQuoteText(text: String) {
        _prefilledQuoteText.value = text
    }

    fun clearScannedPages() {
        _scannedPages.value = emptyList()
    }

    fun setScannedPages(pages: List<ScanPage>) {
        _scannedPages.value = pages
    }

    fun setCapturedImageUri(uri: String?) {
        _capturedImageUri.value = uri
    }

    fun toggleSortOrder() {
        _isSortAscending.value = !_isSortAscending.value
    }

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

    fun setSelectedBookId(id: String?) {
        _selectedBookId.value = id
    }

    fun setSelectedTabIndex(index: Int) {
        _selectedTabIndex.value = index
    }

    private fun updateOverlayAlpha() {
        overlayAlpha.value = if (_showQuoteModal.value) 0.5f else 0f
    }

    fun reset() {
        _showQuoteModal.value = false
        _selectedTabIndex.value = 0
    }
}
