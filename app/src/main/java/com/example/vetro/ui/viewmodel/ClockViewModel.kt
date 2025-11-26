// ClockViewModel.kt
package com.example.vetro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ClockViewModel : ViewModel() {

    private val _timeString = MutableStateFlow("00:00")
    val timeString: StateFlow<String> = _timeString.asStateFlow()

    private val _dateString = MutableStateFlow("")
    val dateString: StateFlow<String> = _dateString.asStateFlow()

    init {
        startClock()
    }

    private fun startClock() {
        viewModelScope.launch {
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val dateFormatter = DateTimeFormatter.ofPattern("M月d日 (E)") // 例: 11月24日 (月)

            while (true) {
                val now = LocalTime.now()
                val today = java.time.LocalDate.now()

                _timeString.value = now.format(timeFormatter)
                _dateString.value = today.format(dateFormatter)

                // 次の00秒または更新タイミングまで待つのが理想ですが、
                // 簡易的に1秒ごとに更新チェックを行います
                delay(1000L)
            }
        }
    }
}