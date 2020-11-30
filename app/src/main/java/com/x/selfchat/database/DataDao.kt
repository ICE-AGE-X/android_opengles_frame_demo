package com.x.selfchat.database

import androidx.room.*

@Dao
interface DataDao {
    @Update
    fun update(msgBean: MsgBean)

    @Insert
    fun add(msgBean: MsgBean)

    @Query("select * from msg")
    fun loadAll():List<MsgBean>

    @Query("delete from msg where id=:id")
    fun removeById(id: Int)

    @Query(value = "delete from msg")
    fun clean()
}