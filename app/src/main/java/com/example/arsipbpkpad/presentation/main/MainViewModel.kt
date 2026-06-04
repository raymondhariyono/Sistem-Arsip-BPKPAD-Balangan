package com.example.arsipbpkpad.presentation.main

import androidx.lifecycle.ViewModel
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ArchiveRepository
) : ViewModel() {
    // UI state and logic
}
