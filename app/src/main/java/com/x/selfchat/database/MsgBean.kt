package com.x.selfchat.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "msg")
class MsgBean() {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id:Long=0
    @ColumnInfo(name = "content")
    var content:String=""
    @ColumnInfo(name = "type")
    var type:String=""
    @ColumnInfo(name = "head")
    var head:String=""
    @ColumnInfo(name = "send_time")
    var send_time:String=""
}