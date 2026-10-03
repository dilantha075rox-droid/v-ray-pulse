package com.example.service

import com.example.model.VlessConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class VpnState {
    object Disconnected : VpnState()
    object Connecting : VpnState()
    object Connected : VpnState()
    object Disconnecting : VpnState()
    data class Error(val message: String) : VpnState()

    val isConnected: Boolean get() = this is Connected
    val isConnecting: Boolean get() = this is Connecting
    val isDisconnected: Boolean get() = this is Disconnected
    val isDisconnecting: Boolean get() = this is Disconnecting
}

object VpnStateManager {
    private val _state = MutableStateFlow<VpnState>(VpnState.Disconnected)
    val state: StateFlow<VpnState> = _state.asStateFlow()

    private val _activeConfig = MutableStateFlow<VlessConfig?>(null)
    val activeConfig: StateFlow<VlessConfig?> = _activeConfig.asStateFlow()

    fun updateState(newState: VpnState) {
        _state.value = newState
    }

    fun setActiveConfig(config: VlessConfig?) {
        _activeConfig.value = config
    }

    fun setError(message: String) {
        _state.value = VpnState.Error(message)
    }

    fun clearError() {
        if (_state.value is VpnState.Error) {
            _state.value = VpnState.Disconnected
        }
    }
}
