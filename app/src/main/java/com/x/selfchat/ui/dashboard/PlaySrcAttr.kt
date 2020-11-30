package com.x.selfchat.ui.dashboard

import java.io.File

enum class PLAY_TYPE{
    LOOP,//循环
    ONCE,//一次
}

class PlaySrcAttr(paths:String,count: Int,type: PLAY_TYPE){
    var path:String
    var frameCount:Int
    var type: PLAY_TYPE
    var width:Int
    var height:Int
    var scale:Int
    var startListener: IFrameStartListener? =null
    var endListener: IFrameEndListener? =null
    var progressListener: IFrameProgressListener?=null
    var files:Array<File?>?=null
    init {
        this.path=paths
        this.frameCount=count
        this.type=type
        width=0
        height=0
        scale=1
    }
}

class PlaySrcSet(playSrcAttr: Array<PlaySrcAttr>){
    var ary:Array<PlaySrcAttr>
    private var _currentIdx:Int=0
    get() {return field }
    set(value) {field=value}
    init {
        ary=playSrcAttr
    }

    constructor(playSrcAttr: PlaySrcAttr):this(Array(1){playSrcAttr})

    constructor(paths:Array<String>,counts:IntArray,types:Array<PLAY_TYPE>):this(Array(paths.size){
        PlaySrcAttr(paths[it],counts[it],types[it])
    })

    fun getNext(): PlaySrcAttr?{
        if(_currentIdx>=ary.size)
        {
            return null
        }
        return ary[_currentIdx++]
    }
}