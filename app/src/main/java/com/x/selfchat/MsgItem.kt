package com.x.selfchat

import com.chad.library.adapter.base.entity.MultiItemEntity

class MsgItem:MultiItemEntity{
    var image:String=""
    var msg:String=""
    var type:Int=MsgItem.OTHER
    override fun getItemType(): Int {
        return type
    }

    companion object{
        val OTHER:Int=0
        val SELF:Int=1
    }
}