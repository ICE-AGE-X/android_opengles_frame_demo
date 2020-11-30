package com.x.selfchat.ui.dashboard

import android.content.Context
import android.util.AttributeSet
import java.io.File

interface IFrameEndListener {
    fun onPlayEnd()
}

interface IFrameStartListener {
    fun onPlayStart()
}

interface IFrameProgressListener {
    fun onPlayProgress(idx:Int)
}

open class MyGLTextureView : GLTextureView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)
//    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):this(context,attrs!!)

    var render: BaseRender

    init {
        setEGLContextClientVersion(2)
//        setEGLConfigChooser(5, 6, 5, 0, 16, 8)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        render = Render31(context)
        setRenderer(render)
//        this.setOnClickListener(this)
    }

    fun runOnGLThread(runnable: Runnable) {
        this.queueEvent(runnable)
    }

    fun play(fileDir: String, isLoop: Boolean, endListener: IFrameEndListener?=null, progressListener: IFrameProgressListener?=null) {
        var files = File(fileDir).listFiles().toMutableList()
        files.sortWith { o1: File, o2: File ->
            if (o1.path.length == o2.path.length)//长度相等 根据 完整路径排序
                o1.path.compareTo(o2.path)
            else//优先根据文件名长度排
                o1.path.length - o2.path.length
        }

        runOnGLThread{
            var src= PlaySrcAttr(fileDir,files.size,if (isLoop) PLAY_TYPE.LOOP else PLAY_TYPE.ONCE)
            src.endListener=endListener
            src.progressListener=progressListener
            src.files=files.toTypedArray()
            render.play(src)
        }
    }

    fun setFps(fps: Int) {
        runOnGLThread {
            render.setFps(fps)
        }
    }

    fun stop() {
        runOnGLThread {
            render.stop()
        }
    }

}