package com.shreejicls.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shreejicls.app.data.local.dao.*
import com.shreejicls.app.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        UserEntity::class,
        FeeEntity::class,
        NoteEntity::class,
        DoubtEntity::class,
        TestEntity::class,
        ResultEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun feeDao(): FeeDao
    abstract fun noteDao(): NoteDao
    abstract fun doubtDao(): DoubtDao
    abstract fun testDao(): TestDao
    abstract fun resultDao(): ResultDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shreeji_classes_db"
                )
                    .addCallback(SeedDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SeedDatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedDatabase(database)
                }
            }
        }

        private suspend fun seedDatabase(db: AppDatabase) {
            val cal = Calendar.getInstance()
            val now = cal.timeInMillis

            // Teacher
            db.userDao().insertUser(
                UserEntity("teacher01", "Guruji", "TEACHER", "shreeji123", "9876543210")
            )

            // Students
            val students = listOf(
                UserEntity("STU001", "Aarav Sharma", "STUDENT", "stu001", "9876500001", "9876500011", "10th"),
                UserEntity("STU002", "Priya Patel", "STUDENT", "stu002", "9876500002", "9876500012", "10th"),
                UserEntity("STU003", "Rahul Gupta", "STUDENT", "stu003", "9876500003", "9876500013", "9th"),
                UserEntity("STU004", "Ananya Singh", "STUDENT", "stu004", "9876500004", "9876500014", "9th"),
                UserEntity("STU005", "Vikram Joshi", "STUDENT", "stu005", "9876500005", "9876500015", "10th")
            )
            students.forEach { db.userDao().insertUser(it) }

            // Fees
            val currentMonth = cal.get(Calendar.MONTH) + 1
            val currentYear = cal.get(Calendar.YEAR)
            students.forEach { student ->
                // Current month - unpaid for some
                val status = if (student.userId in listOf("STU001", "STU004")) "PAID" else "UNPAID"
                val paidDate = if (status == "PAID") now - 86400000L * 5 else null
                val paidAmt = if (status == "PAID") 1500.0 else 0.0
                db.feeDao().insertFee(
                    FeeEntity(
                        studentId = student.userId, month = currentMonth, year = currentYear,
                        amount = 1500.0, status = status, paidAmount = paidAmt,
                        paidDate = paidDate, dueDate = now + 86400000L * 5
                    )
                )
                // Previous month - all paid
                val prevMonth = if (currentMonth == 1) 12 else currentMonth - 1
                val prevYear = if (currentMonth == 1) currentYear - 1 else currentYear
                db.feeDao().insertFee(
                    FeeEntity(
                        studentId = student.userId, month = prevMonth, year = prevYear,
                        amount = 1500.0, status = "PAID", paidAmount = 1500.0,
                        paidDate = now - 86400000L * 35, dueDate = now - 86400000L * 30
                    )
                )
            }

            // Notes
            val notes = listOf(
                NoteEntity(title = "Quadratic Equations", subject = "Mathematics", topic = "Algebra", type = "LINK", content = "https://example.com/quadratic", description = "Complete chapter on quadratic equations with solved examples", uploadedBy = "teacher01"),
                NoteEntity(title = "Newton's Laws of Motion", subject = "Physics", topic = "Mechanics", type = "LINK", content = "https://example.com/newtons-laws", description = "All three laws with numerical problems", uploadedBy = "teacher01"),
                NoteEntity(title = "Chemical Bonding", subject = "Chemistry", topic = "Bonds", type = "LINK", content = "https://example.com/chemical-bonding", description = "Ionic, covalent and metallic bonding explained", uploadedBy = "teacher01"),
                NoteEntity(title = "English Grammar - Tenses", subject = "English", topic = "Grammar", type = "LINK", content = "https://example.com/tenses", description = "All 12 tenses with practice exercises", uploadedBy = "teacher01"),
                NoteEntity(title = "Trigonometry Formulas", subject = "Mathematics", topic = "Trigonometry", type = "LINK", content = "https://example.com/trigo", description = "All important formulas and identities", uploadedBy = "teacher01")
            )
            notes.forEach { db.noteDao().insertNote(it) }

            // Tests
            val tests = listOf(
                TestEntity(subject = "Mathematics", title = "Unit Test - Algebra", date = now + 86400000L * 3, time = "10:00 AM", syllabus = "Quadratic Equations, Polynomials", totalMarks = 50, createdBy = "teacher01"),
                TestEntity(subject = "Physics", title = "Chapter Test - Mechanics", date = now + 86400000L * 7, time = "11:00 AM", syllabus = "Newton's Laws, Work Energy Power", totalMarks = 50, createdBy = "teacher01"),
                TestEntity(subject = "Chemistry", title = "Monthly Test", date = now - 86400000L * 5, time = "10:00 AM", syllabus = "Chemical Bonding, Periodic Table", totalMarks = 100, createdBy = "teacher01")
            )
            tests.forEach { db.testDao().insertTest(it) }

            // Results for past test (testId = 3)
            val results = listOf(
                ResultEntity(testId = 3, studentId = "STU001", marksObtained = 85, totalMarks = 100, remarks = "Excellent work!"),
                ResultEntity(testId = 3, studentId = "STU002", marksObtained = 72, totalMarks = 100, remarks = "Good, improve in organic chemistry"),
                ResultEntity(testId = 3, studentId = "STU003", marksObtained = 91, totalMarks = 100, remarks = "Outstanding performance!"),
                ResultEntity(testId = 3, studentId = "STU004", marksObtained = 65, totalMarks = 100, remarks = "Need more practice in bonding"),
                ResultEntity(testId = 3, studentId = "STU005", marksObtained = 78, totalMarks = 100, remarks = "Good effort, keep it up")
            )
            results.forEach { db.resultDao().insertResult(it) }

            // Doubts
            val doubts = listOf(
                DoubtEntity(studentId = "STU001", studentName = "Aarav Sharma", subject = "Mathematics", question = "How to find discriminant in quadratic equations?", reply = "The discriminant D = b² - 4ac. If D > 0, two real roots. If D = 0, equal roots. If D < 0, no real roots.", repliedBy = "teacher01", replyDate = now - 3600000, status = "RESOLVED"),
                DoubtEntity(studentId = "STU003", studentName = "Rahul Gupta", subject = "Physics", question = "What is the difference between mass and weight?", status = "OPEN"),
                DoubtEntity(studentId = "STU002", studentName = "Priya Patel", subject = "Chemistry", question = "Why is diamond hard but graphite is soft when both are carbon?", status = "OPEN")
            )
            doubts.forEach { db.doubtDao().insertDoubt(it) }

            // Notifications
            val notifs = listOf(
                NotificationEntity(title = "Fee Reminder", body = "Your fee for this month is pending. Please pay soon.", type = "FEE_REMINDER", targetUserId = "STU002"),
                NotificationEntity(title = "New Notes Uploaded", body = "Trigonometry Formulas have been uploaded in Mathematics.", type = "NEW_NOTE", targetUserId = "ALL"),
                NotificationEntity(title = "Test Scheduled", body = "Unit Test - Algebra on ${cal.get(Calendar.DAY_OF_MONTH) + 3}/${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}", type = "TEST_SCHEDULED", targetUserId = "ALL"),
                NotificationEntity(title = "Marks Uploaded", body = "Chemistry Monthly Test marks have been uploaded.", type = "MARKS_UPLOADED", targetUserId = "ALL")
            )
            notifs.forEach { db.notificationDao().insertNotification(it) }
        }
    }
}
