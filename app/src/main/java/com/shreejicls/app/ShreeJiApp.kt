package com.shreejicls.app

import android.app.Application
import com.shreejicls.app.data.local.AppDatabase

class ShreeJiApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}
