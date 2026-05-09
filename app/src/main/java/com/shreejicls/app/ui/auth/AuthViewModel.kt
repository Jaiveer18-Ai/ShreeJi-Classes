package com.shreejicls.app.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shreejicls.app.ShreeJiApp
import com.shreejicls.app.util.SessionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = false,
    val userId: String? = null,
    val userRole: String? = null,
    val userName: String? = null,
    val loginError: String? = null,
    val passwordChanged: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as ShreeJiApp).database
    private val session = SessionManager(application)

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            session.userId.combine(session.userRole) { id, role -> Pair(id, role) }
                .combine(session.userName) { (id, role), name -> Triple(id, role, name) }
                .collect { (id, role, name) ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isLoggedIn = id != null,
                        userId = id,
                        userRole = role,
                        userName = name
                    )
                }
        }
    }

    fun login(userId: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, loginError = null)
            val user = db.userDao().login(userId.trim(), password.trim())
            if (user != null) {
                session.saveSession(user.userId, user.role, user.name)
                _state.value = _state.value.copy(
                    isLoading = false, isLoggedIn = true,
                    userId = user.userId, userRole = user.role, userName = user.name, loginError = null
                )
            } else {
                _state.value = _state.value.copy(isLoading = false, loginError = "Invalid User ID or Password")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            session.clearSession()
            _state.value = AuthState(isLoading = false)
        }
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            val userId = _state.value.userId ?: return@launch
            val user = db.userDao().login(userId, oldPassword)
            if (user != null) {
                db.userDao().changePassword(userId, newPassword)
                _state.value = _state.value.copy(passwordChanged = true, loginError = null)
            } else {
                _state.value = _state.value.copy(loginError = "Current password is incorrect")
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(loginError = null, passwordChanged = false)
    }
}
