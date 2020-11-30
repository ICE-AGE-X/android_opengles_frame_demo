package com.x.selfchat.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.x.selfchat.MsgItem
import com.x.selfchat.R
import com.x.selfchat.databinding.MsgViewBinding

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var msgBinding:MsgViewBinding
    var items=ArrayList<MsgItem>()
    lateinit var adapter:MsgRVAdapter
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        msgBinding= DataBindingUtil.bind(root)!!

        adapter=MsgRVAdapter( items)
        msgBinding.adapter=adapter
        homeViewModel.amsg.observe(viewLifecycleOwner,{
            adapter.addData(it)
            msgBinding.chatRv.layoutManager?.scrollToPosition(adapter.data.size-1)
        })

        msgBinding.onBtnClick= View.OnClickListener {
            homeViewModel.addItem(msgBinding.input)
            msgBinding.input=""
        }

        return root
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("home ondes", "onDestroy: " )
    }
}