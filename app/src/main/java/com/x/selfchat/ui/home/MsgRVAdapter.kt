package com.x.selfchat.ui.home

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.x.selfchat.MsgItem
import com.x.selfchat.R
import com.x.selfchat.databinding.MsgLeftItemBinding
import com.x.selfchat.databinding.MsgRightItemBinding


class MsgRVAdapter(data: List<MsgItem>) : BaseMultiItemQuickAdapter<MsgItem, MsgViewHolder>(data) {

    init {
        addItemType(MsgItem.OTHER, R.layout.chat_msg_l_item)
        addItemType(MsgItem.SELF,R.layout.chat_msg_r_item)
    }

    override fun convert(helper: MsgViewHolder, item: MsgItem?) {
        if(helper.itemViewType==MsgItem.OTHER)
        {
            (helper.msgBinding as MsgLeftItemBinding ).msgItem=item
        }else
        {
            (helper.msgBinding as MsgRightItemBinding ).msgItem=item
        }
    }
}

class MsgViewHolder(view: View?):BaseViewHolder(view){
    var msgBinding:ViewDataBinding=view?.let { DataBindingUtil.bind(it) }!!
}

