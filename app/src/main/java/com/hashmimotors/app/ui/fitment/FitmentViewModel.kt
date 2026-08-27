package com.hashmimotors.app.ui.fitment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.repository.VehicleRepository
import com.hashmimotors.app.domain.model.Vehicle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FitmentUiState(
    val allMakes: List<String> = emptyList(),
    val selectedMake: String? = null,
    val models: List<Vehicle> = emptyList(),
    val selectedVehicleId: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FitmentViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _selectedMake = MutableStateFlow<String?>(null)
    private val _selectedVehicleId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<FitmentUiState> = combine(
        combine(
            vehicleRepository.getAllVehicles(),
            _selectedMake
        ) { all, selectedMake ->
            all to selectedMake
        },
        _selectedMake.flatMapLatest { make ->
            if (make == null) kotlinx.coroutines.flow.flowOf(emptyList())
            else vehicleRepository.getByMake(make)
        },
        _selectedVehicleId
    ) { (all, selectedMake), models, selectedVehicleId ->
        FitmentUiState(
            allMakes = all.map { it.make }.distinct().sorted(),
            selectedMake = selectedMake,
            models = models,
            selectedVehicleId = selectedVehicleId
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FitmentUiState())

    init {
        viewModelScope.launch { vehicleRepository.ensureSeeded() }
    }

    fun selectMake(make: String) {
        _selectedMake.value = make
        _selectedVehicleId.value = null
    }

    fun selectVehicle(id: String) {
        _selectedVehicleId.value = id
    }

    fun reset() {
        _selectedMake.value = null
        _selectedVehicleId.value = null
    }
}
