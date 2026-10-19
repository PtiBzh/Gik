package fr.ydel.gilets.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import fr.ydel.gilets.data.CampaignRepository
import fr.ydel.gilets.data.Gilet
import fr.ydel.gilets.data.MeasurementMode
import fr.ydel.gilets.data.TestRepository
import fr.ydel.gilets.data.TestResult
import fr.ydel.gilets.util.IdNormalizer
import fr.ydel.gilets.util.TestCalculator
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val campaignRepository = CampaignRepository(application, (application as fr.ydel.gilets.TestGiletsApp).database.giletDao())
    private val testRepository = (application as fr.ydel.gilets.TestGiletsApp).testRepository

    private val _currentGilet = MutableLiveData<Gilet?>(null)
    val currentGilet: LiveData<Gilet?> = _currentGilet

    private val _giletIdInput = MutableLiveData<String>("")
    val giletIdInput: LiveData<String> = _giletIdInput

    private val _suggestions = MutableLiveData<List<Gilet>>(emptyList())
    val suggestions: LiveData<List<Gilet>> = _suggestions

    private val _lectureInput = MutableLiveData<String>("")
    val lectureInput: LiveData<String> = _lectureInput

    private val _testResult = MutableLiveData<TestResult?>(null)
    val testResult: LiveData<TestResult?> = _testResult

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _alreadyTested = MutableLiveData<Boolean>(false)
    val alreadyTested: LiveData<Boolean> = _alreadyTested

    private val _measurementMode = MutableLiveData<MeasurementMode>(MeasurementMode.BANC_POULIE)
    val measurementMode: LiveData<MeasurementMode> = _measurementMode

    private val _lestValue = MutableLiveData<Double>(11.0)
    val lestValue: LiveData<Double> = _lestValue

    private var currentManualSeuil: Double? = null

    init {
        loadCampaign()
    }

    fun loadCampaign() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                campaignRepository.loadDefaultCampaign()
            } catch (e: Exception) {
                _errorMessage.value = "Erreur de chargement de la campagne: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun onGiletIdChanged(input: String) {
        _giletIdInput.value = input
        
        if (input.length >= 2) {
            viewModelScope.launch {
                val suggestions = campaignRepository.searchGilets(input)
                _suggestions.value = suggestions
            }
        } else {
            _suggestions.value = emptyList()
        }
    }

    fun onSuggestionSelected(gilet: Gilet) {
        _currentGilet.value = gilet
        _giletIdInput.value = gilet.idClub
        _suggestions.value = emptyList()
        
        checkAlreadyTested(gilet.idCanonique)
    }

    fun onBarcodeScanned(barcode: String) {
        val normalizedId = IdNormalizer.extractIdFromBarcode(barcode)
        _giletIdInput.value = normalizedId
        
        viewModelScope.launch {
            val gilet = campaignRepository.findGiletById(normalizedId)
            if (gilet != null) {
                _currentGilet.value = gilet
                _giletIdInput.value = gilet.idClub
                checkAlreadyTested(gilet.idCanonique)
            } else {
                _currentGilet.value = null
                _errorMessage.value = "Gilet introuvable. Vous pouvez créer un test hors liste."
            }
        }
    }

    private fun checkAlreadyTested(giletId: String) {
        viewModelScope.launch {
            val latestTest = testRepository.getLatestTestByGilet(giletId)
            _alreadyTested.value = latestTest != null
        }
    }

    fun onLectureChanged(input: String) {
        _lectureInput.value = input
    }

    fun onManualSeuilChanged(seuil: Double) {
        currentManualSeuil = seuil
    }

    fun setMeasurementMode(mode: MeasurementMode) {
        _measurementMode.value = mode
    }

    fun setLestValue(value: Double) {
        _lestValue.value = value
    }

    fun validateTest() {
        val lecture = _lectureInput.value?.toDoubleOrNull()
        val gilet = _currentGilet.value
        val mode = _measurementMode.value ?: MeasurementMode.BANC_POULIE
        val lest = _lestValue.value

        if (lecture == null || lecture < 0 || lecture > 25) {
            _errorMessage.value = "Veuillez entrer une lecture valide entre 0 et 25 kg"
            return
        }

        val testResult = if (gilet != null) {
            TestCalculator.calculateTestResult(gilet, lecture, mode, lest, null)
        } else {
            // Hors liste - need manual seuil
            if (currentManualSeuil == null) {
                _errorMessage.value = "Gilet hors liste. Veuillez indiquer le seuil nominal."
                return
            }
            TestCalculator.calculateTestResult(null, lecture, mode, lest, currentManualSeuil)
        }

        _testResult.value = testResult
    }

    fun createHorsListeTest(constat: String) {
        val mode = _measurementMode.value ?: MeasurementMode.BANC_POULIE
        val testResult = TestCalculator.createManualTestResult(
            giletId = _giletIdInput.value ?: "HORS_LISTE",
            constat = constat,
            modeMesure = mode,
            lestKg = _lestValue.value
        )
        _testResult.value = testResult
    }

    fun saveTest() {
        val test = _testResult.value ?: return
        
        viewModelScope.launch {
            try {
                testRepository.insertTest(test)
                _testResult.value = null
                _lectureInput.value = ""
                _currentGilet.value = null
                _giletIdInput.value = ""
                currentManualSeuil = null
            } catch (e: Exception) {
                _errorMessage.value = "Erreur lors de l'enregistrement: ${e.message}"
            }
        }
    }

    fun cancelLastTest() {
        viewModelScope.launch {
            val tests = testRepository.getAllTests()
            if (tests.isNotEmpty()) {
                testRepository.deleteTest(tests.first().id)
            }
        }
    }

    fun resetError() {
        _errorMessage.value = null
    }

    fun resetTestResult() {
        _testResult.value = null
    }
}
