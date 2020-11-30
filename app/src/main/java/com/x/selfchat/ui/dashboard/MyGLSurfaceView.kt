package com.x.selfchat.ui.dashboard

import android.app.ActivityManager
import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat.getSystemService


class MyGLSurfaceView(context: Context?) : GLSurfaceView(context) {

    private var render: BaseRender

    constructor(context: Context?, attrs: AttributeSet?) : this(context) {

    }

    init {
        val am = context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = am?.deviceConfigurationInfo
        val glVersion = info.reqGlEsVersion
        Log.e("GL", glVersion.toString(16))
        this.setEGLConfigChooser(5, 6, 5, 0, 16, 8)
//        this.setEGLConfigChooser(8,8,8,8,16,0)
        this.isFocusableInTouchMode = true
        holder.setFormat(PixelFormat.TRANSPARENT)
//        setZOrderOnTop(true)
        if(glVersion>0x30000) {//使用opengl3.1
//        if (false) {
            this.setEGLContextClientVersion(3)
            render = context?.let { Render31(it) }
        } else//使用opengl2.0
        {
            this.setEGLContextClientVersion(2)
            render = context?.let { Render20(it) }
        }
//        this.setRenderer(render)

    }

    fun runOnGLThread(runable: Runnable) {
        this.queueEvent(runable)
    }

    var idx = 0
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            when (event.action and event.actionMasked) {
                MotionEvent.ACTION_UP -> {
                    Log.e("test", "onTouchEvent: ")
                    runOnGLThread {
                        var set = PlaySrcSet(
                            arrayOf(
                                PlaySrcAttr("pwait.zip", 40, PLAY_TYPE.LOOP),
//                                PlaySrcAttr("click.zip", 25, PLAY_TYPE.ONCE),
//                                PlaySrcAttr("open.zip", 31, PLAY_TYPE.ONCE),
//                                PlaySrcAttr("test.zip", 56, PLAY_TYPE.ONCE),
//                                PlaySrcAttr("wait.zip", 40, PLAY_TYPE.ONCE),
                            )
                        )
                        render.play(set)
//                        when(idx)
//                        {
//                            0->{
//
//                                render.playInRange(0,39)
//                                render.loadTexture("wait.zip",40)
//                            }
//                            1->{
//                                render.playInRange(0,24)
//                                render.loadTexture("click.zip",25)
//                            }
//                            2->{
//                                render.playInRange(0,30)
//                                render.loadTexture("open.zip",31)
//
//                            }
//                            3->{
//                                render.playInRange(0,55)
//                                render.loadTexture("test.zip",56)
//                            }
//                        }
//                        idx++
//                        idx=if(idx>3) 0 else idx
                    }

                }
            }
        }

        return true
    }

}