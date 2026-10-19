package fr.ydel.gilets.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import fr.ydel.gilets.data.SessionStats
import fr.ydel.gilets.data.TestRepository
import fr.ydel.gilets.data.TestResult
import kotlinx.coroutines.launch

class SessionViewModel(application: Application) : AndroidViewModel(application) {
    private val testRepository = (application as fr.ydel.gilets.TestGiletsApp).testRepository

    private val _tests = MutableLiveData<List<TestResult>>(emptyList())
    val tests: LiveData<List<TestResult>> = _tests

    private val _stats = MutableLiveData<SessionStats>(SessionStats(0, 0, 0, 0))
    val stats: LiveData<SessionStats> = _stats

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadTests()
    }

    fun loadTests() {
        viewModelScope.launch {
            _isLoading.value = true
            val tests = testRepository.getAllTests()
            _tests.value = tests
            val stats = testRepository.getStats()
            _stats.value = stats
            _isLoading.value = false
        }
    }

    fun deleteTest(testId: Long) {
        viewModelScope.launch {
            testRepository.deleteTest(testId)
            loadTests()
        }
    }

    fun deleteAllTests() {
        viewModelScope.launch {
            testRepository.deleteAllTests()
            _tests.value = emptyList()
            _stats.value = SessionStats(0, 0, 0, 0)
        }
    }

    fun getTestsCount(): Int {
        return _tests.value?.size ?: 0
    }
}
