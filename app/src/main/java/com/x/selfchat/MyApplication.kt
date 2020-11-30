package com.x.selfchat

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.x.selfchat.database.MyDatabase



class MyApplication:Application() {
    val appDatabase:MyDatabase by lazy ( LazyThreadSafetyMode.SYNCHRONIZED ){
        Room.databaseBuilder(this,MyDatabase::class.java,packageName).build()
    }
    override fun onCreate() {
        super.onCreate()
        Log.d("test","database init : " + appDatabase.openHelper.databaseName)
    }
}