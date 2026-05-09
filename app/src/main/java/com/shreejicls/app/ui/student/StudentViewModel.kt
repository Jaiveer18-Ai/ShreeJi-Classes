package com.shreejicls.app.ui.student

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shreejicls.app.ShreeJiApp
import com.shreejicls.app.data.local.entity.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StudentViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as ShreeJiApp).database

    private val _studentId = MutableStateFlow("")
    val studentId: StateFlow<String> = _studentId

    fun setStudentId(id: String) { _studentId.value = id }

    val myFees = _studentId.flatMapLatest { id ->
        if (id.isNotBlank()) db.feeDao().getFeesByStudent(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val myDoubts = _studentId.flatMapLatest { id ->
        if (id.isNotBlank()) db.doubtDao().getDoubtsByStudent(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val myResults = _studentId.flatMapLatest { id ->
        if (id.isNotBlank()) db.resultDao().getResultsByStudent(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val myNotifications = _studentId.flatMapLatest { id ->
        if (id.isNotBlank()) db.notificationDao().getNotificationsForUser(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val unreadNotifCount = _studentId.flatMapLatest { id ->
        if (id.isNotBlank()) db.notificationDao().getUnreadCount(id) else flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val allNotes = db.noteDao().getAllNotes().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val upcomingTests = db.testDao().getUpcomingTests(System.currentTimeMillis()).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allTests = db.testDao().getAllTests().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun askDoubt(subject: String, question: String, studentName: String) {
        viewModelScope.launch {
            val id = _studentId.value
            if (id.isNotBlank()) {
                db.doubtDao().insertDoubt(
                    DoubtEntity(studentId = id, studentName = studentName, subject = subject, question = question)
                )
            }
        }
    }

    fun markNotificationsRead() {
        viewModelScope.launch {
            val id = _studentId.value
            if (id.isNotBlank()) db.notificationDao().markAllAsRead(id)
        }
    }
}
