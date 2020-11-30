package com.x.selfchat.ui.dashboard

import android.content.Context
import android.opengl.*
import android.util.Log
import java.io.File
import java.io.FileInputStream
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

enum class PLAY_STATE {
    NONE,
    PLAY,
    PAUSE
}

abstract class BaseRender(context: Context) : GLTextureView.Renderer {
    private var TAG = "BaseRender"
    protected var context: Context
    protected var texs: IntArray = IntArray(0)
    protected var shaderProgram: Int = 0
    protected var frameIdx = 0
    protected var modelLoc = 0
    protected var viewLoc = 0
    protected var projLoc = 0
    protected var mProjectionMatrix: FloatArray = FloatArray(16)
    protected var width = 0f
    protected var height = 0f
    protected var playStartIdx = 0
    protected var playEndIdx = 0
    private var NANOSECONDSPERSECOND = 1000000000L
    private var INTERVAL_60_FPS = (1.0f / 60 * NANOSECONDSPERSECOND).toLong()
    private var sAnimationInterval = INTERVAL_60_FPS
    private var mLastTickInNanoSeconds: Long = 0L
    private val NANOSECONDSPERMICROSECOND: Long = 1000000

    var playerState: PLAY_STATE = PLAY_STATE.NONE

    var playSet: PlaySrcSet? = null

    init {
        this.context = context
    }

    fun setFps(fps: Int) {
        sAnimationInterval = (1.0f / fps * NANOSECONDSPERSECOND).toLong()
    }


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        if (gl != null) {
            gl.glClearColor(1f, 1f, 1f, 0f);
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            Log.e(TAG, "enable gl_blend")
        }
    }

    protected var mModelMatrix = FloatArray(16)
    protected var mViewMatrix = FloatArray(16)
    var aspectRatio: Float = 0f
    var isHor: Boolean = false//是横屏展示吗
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

        if (gl != null) {
            val aspectRatio =
                    if (width > height) width.toFloat() / height.toFloat() else height.toFloat() / width.toFloat()
            this.width = width.toFloat()
            this.height = height.toFloat()
            this.aspectRatio = aspectRatio
//            Matrix.translateM()
            gl.glViewport(0, 0, width, height)
            isHor = width > height
            if (isHor) {
                //横屏。需要设置的就是左右。
                Matrix.orthoM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1.0f, 1f);
            } else {
                //竖屏。需要设置的就是上下
                Matrix.orthoM(mProjectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1.0f, 1f);
            }
            setNormalMat(mViewMatrix)
            setNormalMat(mModelMatrix)

//            Matrix.translateM(mProjectionMatrix,0, 0.5f, aspectRatio,0f)
//            Matrix.translateM(mProjectionMatrix,0, 0.5f, 0f,0f)
//            val h=878/height.toFloat()

//            Matrix.rotateM(mModelMatrix,0,-55f,1f,0f,0f)
//            Matrix.translateM(mViewMatrix,0,0f,0f,-3f)
//            Matrix.translateM(mModelMatrix,0,0f,0f,0f)
//            Matrix.scaleM(mModelMatrix,0,1f,878f/750,1f)
//            Matrix.multiplyMM(mViewMatrix,0,mModelMatrix,0,mProjectionMatrix,0)
            Log.e(TAG, "surface changed")
        }
    }

    //重置为 单位矩阵
    fun setNormalMat(mat: FloatArray) {
        mat.fill(0f)
        mat[0] = 1f
        mat[5] = 1f
        mat[10] = 1f
        mat[15] = 1f
    }

    override fun onDrawFrame(gl: GL10?) {
        if (gl != null) {
            GLES20.glClearColor(0f, 0f, 0f, 0f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            if (playerState == PLAY_STATE.PAUSE || playerState == PLAY_STATE.NONE || texs.isEmpty()) {
                Thread.sleep(500)
                return
            }
            if (frameIdx == playStartIdx) currentPlaySrcAttr?.startListener?.let {
                it.onPlayStart()
            }
            currentPlaySrcAttr?.progressListener?.onPlayProgress(frameIdx)

            GLES20.glUniformMatrix4fv(modelLoc, 1, false, mModelMatrix, 0)
            GLES20.glUniformMatrix4fv(viewLoc, 1, false, mViewMatrix, 0)
            GLES20.glUniformMatrix4fv(projLoc, 1, false, mProjectionMatrix, 0)
            GLES20.glUseProgram(shaderProgram)

            if (sAnimationInterval <= INTERVAL_60_FPS) {
                render()
                loadTextureByFrame()
            } else {
                val now = System.nanoTime()
                var interval = now - mLastTickInNanoSeconds
                if (interval < sAnimationInterval) {
                    loadTextureByFrame()
                    interval = System.nanoTime() - mLastTickInNanoSeconds
                    if (interval >= sAnimationInterval) {
                        Log.e(TAG, "load time out:$interval cf:${frameIdx} cl:${currentLoadIdx}")
                        if (frameIdx == currentLoadIdx) frameIdx--
                    }

                    if (interval < sAnimationInterval)
                        Thread.sleep((sAnimationInterval - interval) / NANOSECONDSPERMICROSECOND)
                }
                this.mLastTickInNanoSeconds = System.nanoTime()
                render()
            }

            frameIdx++
            if(playEndIdx-frameIdx==1) //提前调回调
            {
                when (currentPlaySrcAttr?.type) {
                    PLAY_TYPE.ONCE -> {
                        currentPlaySrcAttr?.endListener?.let {
                            context.run{ it.onPlayEnd() }
                        }
                    }
                }
            }
            if (frameIdx >= texs.size || frameIdx == playEndIdx) {
                when (currentPlaySrcAttr?.type) {
                    PLAY_TYPE.LOOP -> {
                        frameIdx = playStartIdx
                    }
                    PLAY_TYPE.ONCE -> {
                        var next = playSet?.getNext()
                        if (next != null) {
                            currentPlaySrcAttr = next
                            files=currentPlaySrcAttr?.files
                            play()
                        } else {
                            playerState = PLAY_STATE.NONE
                            deleteTexture()
                        }
                    }
                }
            }
        }
    }

    abstract fun render()

    //清空纹理
    fun deleteTexture() {
        if (this.texs.isNotEmpty())
            GLES20.glDeleteTextures(this.texs.size, this.texs, 0)

        this.texs = IntArray(0)
    }

    fun bindTexture(tId: Int) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tId)
        // set the texture wrapping parameters
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        // set texture wrapping to GL_REPEAT (default wrapping method)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        // set texture filtering parameters
        GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR
        )
    }

    fun createShader(vsh: String, fsh: String): Int {
        var v = GLES31.glCreateShader(GLES31.GL_VERTEX_SHADER)
        GLES31.glShaderSource(v, vsh)
        GLES31.glCompileShader(v)
        Log.e(TAG, "vsh err" + GLES31.glGetShaderInfoLog(v))
        var f = GLES31.glCreateShader(GLES31.GL_FRAGMENT_SHADER)
        GLES31.glShaderSource(f, fsh)
        GLES31.glCompileShader(f)
        Log.e(TAG, "fsh err" + GLES31.glGetShaderInfoLog(f))

        var program = GLES31.glCreateProgram()
        GLES31.glAttachShader(program, v)
        GLES31.glAttachShader(program, f)
        GLES31.glLinkProgram(program)
        Log.e(TAG, "sp err: " + GLES31.glGetProgramInfoLog(program))

        GLES31.glDeleteShader(v)
        GLES31.glDeleteShader(f)
        return program
    }

    fun playInRange(sIdx: Int, endIdx: Int) {
        this.playStartIdx = sIdx
        this.playEndIdx = endIdx
        this.frameIdx = sIdx
    }

    var currentPlaySrcAttr: PlaySrcAttr? = null

    //链式播放
    fun play(playSrcSet: PlaySrcSet) {
        playSet = playSrcSet
        var next = playSet?.getNext()
        if (next != null) {
            currentPlaySrcAttr = next
            files=currentPlaySrcAttr?.files
            files?.size?.minus(1)?.let { playInRange(0, it) }
            play()
        }
    }

    fun stop()
    {
        this.playerState= PLAY_STATE.NONE
    }


    //一个文件单播
    fun play(playSrcAttr: PlaySrcAttr) {
        play(PlaySrcSet(playSrcAttr))
    }

    //提供播放 单帧动画
    private fun play() {
//        this.loadTexture(filePath)
        this.loadTextureByFile()
        this.playerState = PLAY_STATE.PLAY
    }

    //缓存 的 分帧加载 临时对象  之所以缓存起来 是因为重新构造对象 开销过大
    protected var files: Array<File?>? = null
    protected var currentLoadIdx = 0


//    fun loadTexture(filePath: String) {
//
//        FileObservable.getFiles(filePath).subscribe(object : MSubscriber<Array<File?>?>() {
//            override fun onSuccess(files: Array<File?>?) {
//                files?.let {
//                    this@BaseRender.files = files
//                    this@BaseRender.currentPlaySrcAttr?.frameCount= files.size
//                    this@BaseRender.playInRange(0, files.size - 1)
//                    loadTextureByFile()
//                }
//            }
//
//            override fun onError(e: Throwable) {
//
//            }
//        })
//    }

    fun loadTextureByFile() {
        files?.let {
            currentLoadIdx = 0
            deleteTexture()
            texs = IntArray(it.size)
            GLES20.glGenTextures(it.size, texs, 0)
//            var ifs = FileInputStream(it[currentLoadIdx])
//            bindTexture(texs[currentLoadIdx])
//            commitTexture(ifs.readBytes())
//            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
//            currentLoadIdx++
            while (currentLoadIdx < 3) {
                loadTextureByFrame()
            }
        }

    }

    fun loadTextureByFrame() {
        if (currentLoadIdx == -1 || currentPlaySrcAttr == null) return
        var t = System.currentTimeMillis()

        files?.let {
            var ifs = FileInputStream(it[currentLoadIdx])
            bindTexture(texs[currentLoadIdx])
            commitTexture(ifs.readBytes())
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
            ifs.close()
            currentLoadIdx++
        }
        //标记读取完成
        if (currentLoadIdx >= currentPlaySrcAttr?.frameCount!!) {
            currentLoadIdx = -1
        }
        calTime("frame", t)
    }

    fun calTime(flag: String, t: Long): Long {
        Log.e(TAG, "load ${flag} ${System.currentTimeMillis() - t}")
        return System.currentTimeMillis()
    }


    //不同的 render  分开提交 纹理操作
    abstract fun commitTexture(array: ByteArray?)

    override fun onSurfaceDestroyed(gl: GL10?) {
        this.deleteTexture()
    }

    fun loadShader(name: String): String {
        var mng = context.assets
        var infs = mng.open(name)
        var s = String(infs.readBytes())
        infs.close()
        return s
    }
}