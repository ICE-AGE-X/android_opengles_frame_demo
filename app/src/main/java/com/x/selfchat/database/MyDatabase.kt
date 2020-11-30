package com.x.selfchat.database

import androidx.room.Database
import androidx.room.RoomDatabase
@Database(entities = arrayOf(MsgBean::class),version = 1 )
abstract class MyDatabase:RoomDatabase(){
    abstract fun dataDao():DataDao
}