package io.shellify.app.presentation.settings.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.shellify.app.domain.model.PwaNotification
import io.shellify.app.domain.usecase.GetNotificationsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationHistoryUiState(
    val notifications: List<PwaNotification> = emptyList(),
    val isLoading: Boolean = true,
)

class NotificationHistoryViewModel(
    private val appId: Long,
    private val getNotifications: GetNotificationsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationHistoryUiState())
    val uiState: StateFlow<NotificationHistoryUiState> = _state

    init {
        viewModelScope.launch {
            getNotifications(appId).collect { list ->
                _state.update { it.copy(notifications = list, isLoading = false) }
            }
        }
    }

    class Factory(
        private val appId: Long,
        private val getNotifications: GetNotificationsUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NotificationHistoryViewModel(appId, getNotifications) as T
    }
}
