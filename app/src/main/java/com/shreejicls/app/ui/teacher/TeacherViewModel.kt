package com.shreejicls.app.ui.teacher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shreejicls.app.ShreeJiApp
import com.shreejicls.app.data.local.entity.*
import com.shreejicls.app.util.IdGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class TeacherViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as ShreeJiApp).database

    val students = db.userDao().getAllStudents().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val studentCount = db.userDao().getStudentCount().stateIn(viewModelScope, SharingStarted.Lazily, 0)
    val pendingFeesCount = db.feeDao().getPendingFeesCount().stateIn(viewModelScope, SharingStarted.Lazily, 0)
    val pendingFees = db.feeDao().getPendingFees().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allFees = db.feeDao().getAllFees().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allNotes = db.noteDao().getAllNotes().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allDoubts = db.doubtDao().getAllDoubts().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val openDoubtsCount = db.doubtDao().getOpenDoubtsCount().stateIn(viewModelScope, SharingStarted.Lazily, 0)
    val allTests = db.testDao().getAllTests().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val upcomingTests = db.testDao().getUpcomingTests(System.currentTimeMillis()).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val upcomingTestsCount = db.testDao().getUpcomingTestsCount(System.currentTimeMillis()).stateIn(viewModelScope, SharingStarted.Lazily, 0)
    val allResults = db.resultDao().getAllResults().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    val filteredStudents = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) db.userDao().getAllStudents()
        else db.userDao().searchStudents(query)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun searchStudents(query: String) { _searchQuery.value = query }

    fun addStudent(name: String, studentClass: String, phone: String, parentPhone: String, onResult: (String, String) -> Unit) {
        viewModelScope.launch {
            val id = IdGenerator.generateStudentId()
            val pwd = IdGenerator.generatePassword()
            db.userDao().insertUser(
                UserEntity(userId = id, name = name, role = "STUDENT", password = pwd, phone = phone, parentPhone = parentPhone, studentClass = studentClass)
            )
            onResult(id, pwd)
        }
    }

    fun updateStudent(student: UserEntity) { viewModelScope.launch { db.userDao().updateUser(student) } }
    fun deleteStudent(student: UserEntity) { viewModelScope.launch { db.userDao().deleteUser(student) } }

    fun addFee(studentId: String, month: Int, year: Int, amount: Double) {
        viewModelScope.launch {
            val cal = Calendar.getInstance().apply { set(year, month - 1, 15) }
            db.feeDao().insertFee(
                FeeEntity(studentId = studentId, month = month, year = year, amount = amount, dueDate = cal.timeInMillis)
            )
        }
    }

    fun markFeePaid(feeId: Long, amount: Double) {
        viewModelScope.launch {
            db.feeDao().updateFeeStatus(feeId, "PAID", amount, System.currentTimeMillis())
            db.notificationDao().insertNotification(
                NotificationEntity(title = "Fee Payment Recorded", body = "₹${amount.toInt()} fee has been marked as paid.", type = "FEE_REMINDER", targetUserId = "ALL")
            )
        }
    }

    fun markFeeUnpaid(feeId: Long) {
        viewModelScope.launch { db.feeDao().updateFeeStatus(feeId, "UNPAID", 0.0, null) }
    }

    fun addNote(title: String, subject: String, topic: String, type: String, content: String, description: String) {
        viewModelScope.launch {
            db.noteDao().insertNote(
                NoteEntity(title = title, subject = subject, topic = topic, type = type, content = content, description = description, uploadedBy = "teacher01")
            )
            db.notificationDao().insertNotification(
                NotificationEntity(title = "New Notes Uploaded", body = "$title has been uploaded in $subject.", type = "NEW_NOTE", targetUserId = "ALL")
            )
        }
    }

    fun deleteNote(note: NoteEntity) { viewModelScope.launch { db.noteDao().deleteNote(note) } }

    fun replyToDoubt(doubtId: Long, reply: String) {
        viewModelScope.launch {
            db.doubtDao().replyToDoubt(doubtId, reply, "teacher01", System.currentTimeMillis())
        }
    }

    fun addTest(subject: String, title: String, date: Long, time: String, syllabus: String, totalMarks: Int) {
        viewModelScope.launch {
            db.testDao().insertTest(
                TestEntity(subject = subject, title = title, date = date, time = time, syllabus = syllabus, totalMarks = totalMarks, createdBy = "teacher01")
            )
            db.notificationDao().insertNotification(
                NotificationEntity(title = "Test Scheduled", body = "$title ($subject) has been scheduled.", type = "TEST_SCHEDULED", targetUserId = "ALL")
            )
        }
    }

    fun deleteTest(test: TestEntity) { viewModelScope.launch { db.testDao().deleteTest(test) } }

    fun addResult(testId: Long, studentId: String, marks: Int, totalMarks: Int, remarks: String) {
        viewModelScope.launch {
            db.resultDao().insertResult(
                ResultEntity(testId = testId, studentId = studentId, marksObtained = marks, totalMarks = totalMarks, remarks = remarks)
            )
            db.notificationDao().insertNotification(
                NotificationEntity(title = "Marks Uploaded", body = "Your marks have been uploaded.", type = "MARKS_UPLOADED", targetUserId = studentId)
            )
        }
    }
}
