package com.x.selfchat.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.x.selfchat.MsgItem
import java.util.*
import kotlin.collections.ArrayList

class HomeViewModel : ViewModel() {
    var msgs=MutableLiveData<List<MsgItem>>()
    var amsg=MutableLiveData<MsgItem>()
    init {
        msgs.postValue(ArrayList())
    }

    fun addItem(msg:String?)
    {
        var m=MsgItem()
        m.type=Random().nextInt(2)
        if (msg != null) {
            m.msg=msg
        }
        amsg.postValue(m)
    }
}