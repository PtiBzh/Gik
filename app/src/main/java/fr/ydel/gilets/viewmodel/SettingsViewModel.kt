package fr.ydel.gilets.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import fr.ydel.gilets.data.CampaignRepository
import fr.ydel.gilets.data.MeasurementMode
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val campaignRepository = CampaignRepository(application, (application as fr.ydel.gilets.TestGiletsApp).database.giletDao())
    private val prefs = application.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)

    private val _measurementMode = MutableLiveData<MeasurementMode>(
        MeasurementMode.valueOf(prefs.getString("measurement_mode", MeasurementMode.BANC_POULIE.name) ?: MeasurementMode.BANC_POULIE.name)
    )
    val measurementMode: LiveData<MeasurementMode> = _measurementMode

    private val _lestValue = MutableLiveData<Double>(
        prefs.getFloat("lest_value", 11.0f).toDouble()
    )
    val lestValue: LiveData<Double> = _lestValue

    private val _importResult = MutableLiveData<Result<Int>?>(null)
    val importResult: LiveData<Result<Int>?> = _importResult

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun setMeasurementMode(mode: MeasurementMode) {
        _measurementMode.value = mode
        prefs.edit().putString("measurement_mode", mode.name).apply()
    }

    fun setLestValue(value: Double) {
        _lestValue.value = value
        prefs.edit().putFloat("lest_value", value.toFloat()).apply()
    }

    fun importCampaign(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = campaignRepository.importCampaignFromUri(uri)
            _importResult.value = result
            _isLoading.value = false
        }
    }

    fun resetImportResult() {
        _importResult.value = null
    }
}
