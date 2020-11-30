package com.x.selfchat.ui.dashboard

import android.graphics.SurfaceTexture
import android.opengl.GLUtils
import java.util.concurrent.atomic.AtomicBoolean
import javax.microedition.khronos.egl.*
import javax.microedition.khronos.opengles.GL

interface GLRenderer {
    fun drawFrame()
}

class MyGLThread(surfaceTexture: SurfaceTexture, render: GLRenderer, shouldRender: AtomicBoolean) : Thread() {

    private var mShouldRender: AtomicBoolean? = null
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mRenderer: GLRenderer? = null
    private lateinit var mEgl: EGL10
    private var mEglDisplay: EGLDisplay = EGL10.EGL_NO_DISPLAY
    private var mEglContext: EGLContext = EGL10.EGL_NO_CONTEXT
    private var mEglSurface: EGLSurface = EGL10.EGL_NO_SURFACE
    private var mGL: GL? = null
    private val EGL_CONTEXT_CLIENT_VERSION = 0x3098
    private val EGL_OPENGL_ES2_BIT = 4
    init {
        mRenderer=render
        mSurfaceTexture=surfaceTexture
        mShouldRender=shouldRender
    }

    private fun initGL() {
        mEgl = EGLContext.getEGL() as EGL10
        mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
        if (mEglDisplay === EGL10.EGL_NO_DISPLAY) {
            throw RuntimeException(
                "eglGetdisplay failed : " +
                        GLUtils.getEGLErrorString(mEgl.eglGetError())
            )
        }
        val version = IntArray(2)
        if (!mEgl.eglInitialize(mEglDisplay, version)) {
            throw RuntimeException(
                "eglInitialize failed : " +
                        GLUtils.getEGLErrorString(mEgl.eglGetError())
            )
        }
        val configAttribs = intArrayOf(
            EGL10.EGL_BUFFER_SIZE, 32,
            EGL10.EGL_ALPHA_SIZE, 8,
            EGL10.EGL_BLUE_SIZE, 8,
            EGL10.EGL_GREEN_SIZE, 8,
            EGL10.EGL_RED_SIZE, 8,
            EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT,
            EGL10.EGL_NONE
        )
        val numConfigs = IntArray(1)
        val configs: Array<EGLConfig?> = arrayOfNulls<EGLConfig>(1)
        if (!mEgl.eglChooseConfig(mEglDisplay, configAttribs, configs, 1, numConfigs)) {
            throw RuntimeException(
                "eglChooseConfig failed : " +
                        GLUtils.getEGLErrorString(mEgl.eglGetError())
            )
        }
        val contextAttribs = intArrayOf(
            EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL10.EGL_NONE
        )
        mEglContext = mEgl.eglCreateContext(
            mEglDisplay,
            configs[0], EGL10.EGL_NO_CONTEXT, contextAttribs
        )
        mEglSurface = mEgl.eglCreateWindowSurface(mEglDisplay, configs[0], mSurfaceTexture, null)
        if (mEglSurface === EGL10.EGL_NO_SURFACE || mEglContext === EGL10.EGL_NO_CONTEXT) {
            val error = mEgl.eglGetError()
            if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                throw RuntimeException("eglCreateWindowSurface returned  EGL_BAD_NATIVE_WINDOW. ")
            }
            throw RuntimeException(
                "eglCreateWindowSurface failed : " +
                        GLUtils.getEGLErrorString(mEgl.eglGetError())
            )
        }
        if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
            throw RuntimeException(
                "eglMakeCurrent failed : " +
                        GLUtils.getEGLErrorString(mEgl.eglGetError())
            )
        }
        mGL = mEglContext.gl
    }

    private fun destoryGL() {
        mEgl.eglDestroyContext(mEglDisplay, mEglContext)
        mEgl.eglDestroySurface(mEglDisplay, mEglSurface)
        mEglContext = EGL10.EGL_NO_CONTEXT
        mEglSurface = EGL10.EGL_NO_SURFACE
    }

    override fun run() {
        initGL()
        if (mRenderer != null) {
            (mRenderer as GLRendererImpl).initGL()
        }
        while (mShouldRender != null && mShouldRender!!.get() != false) {
            mRenderer?.drawFrame()
            mEgl.eglSwapBuffers(mEglDisplay, mEglSurface)
            try {
                sleep(15)
            } catch (e: InterruptedException) {
            }
        }
        destoryGL()
    }

}