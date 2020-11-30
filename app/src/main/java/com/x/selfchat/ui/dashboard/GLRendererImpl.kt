package com.x.selfchat.ui.dashboard

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer


class GLRendererImpl(context: Context) : GLRenderer {
    private var mProgramObject = 0
    private var mWidth = 0
    private var mHeight = 0
    private var mVertices: FloatBuffer
    private var mTexCoords: ShortBuffer
    private var mContext: Context
    private var mTexID = 0
    private val TAG = "GLRendererImpl"
    private val mVerticesData =
        floatArrayOf(-0.5f, -0.5f, 0f, 0.5f, -0.5f, 0f, -0.5f, 0.5f, 0f, 0.5f, 0.5f, 0f)
    private val mTexCoordsData = shortArrayOf(0, 1, 1, 1, 0, 0, 1, 0)
   init {
        mVertices = ByteBuffer.allocateDirect(mVerticesData.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mVertices.put(mVerticesData).position(0)
        mTexCoords = ByteBuffer.allocateDirect(mTexCoordsData.size * 2)
            .order(ByteOrder.nativeOrder()).asShortBuffer()
        mTexCoords.put(mTexCoordsData).position(0)
        mContext = context
    }

    fun setViewport(width: Int, height: Int) {
        mWidth = width
        mHeight = height
    }

    fun initGL() {
        comipleAndLinkProgram()
        loadTexture()
        GLES20.glClearColor(0f, 0f, 0f, 0f)
    }

    fun resize(width: Int, height: Int) {
        mWidth = width
        mHeight = height
    }

    override fun drawFrame() {
        // TODO Auto-generated method stub
        GLES20.glViewport(0, 0, mWidth, mHeight)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(mProgramObject)
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, mVertices)
        GLES20.glEnableVertexAttribArray(0)
        GLES20.glVertexAttribPointer(1, 2, GLES20.GL_SHORT, false, 0, mTexCoords)
        GLES20.glEnableVertexAttribArray(1)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexID)
        val loc = GLES20.glGetUniformLocation(mProgramObject, "u_Texture")
        GLES20.glUniform1f(loc, 0f)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        Log.i("GLRendererImpl", "drawing...$mWidth")
    }

    private fun loadTexture() {
        val b = BitmapFactory.decodeResource(mContext.getResources(), com.x.selfchat.R.mipmap.ic_launcher)
        if (b != null) {
            val texID = IntArray(1)
            GLES20.glGenTextures(1, texID, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texID[0])
            mTexID = texID[0]
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_REPEAT
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_REPEAT
            )
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, b, 0)
            b.recycle()
        }
    }

    private fun loadShader(shaderType: Int, shaderSource: String): Int {
        val shader: Int
        val compiled = IntArray(1)
        // Create the shader object
        shader = GLES20.glCreateShader(shaderType)
        if (shader == 0) return 0
        // Load the shader source
        GLES20.glShaderSource(shader, shaderSource)
        // Compile the shader
        GLES20.glCompileShader(shader)
        // Check the compile status
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.e(TAG, GLES20.glGetShaderInfoLog(shader))
            GLES20.glDeleteShader(shader)
            return 0
        }
        return shader
    }

    private fun comipleAndLinkProgram() {
        val vShaderStr = """attribute vec4 a_position;    
attribute vec2 a_texCoords; 
varying vec2 v_texCoords; 
void main()                  
{                            
   gl_Position = a_position;  
    v_texCoords = a_texCoords; 
}                            
"""
        val fShaderStr = """precision mediump float;                     
uniform sampler2D u_Texture; 
varying vec2 v_texCoords; 
void main()                                  
{                                            
  gl_FragColor = texture2D(u_Texture, v_texCoords) ;
}                                            
"""
        val vertexShader: Int
        val fragmentShader: Int
        val programObject: Int
        val linked = IntArray(1)
        // Load the vertex/fragment shaders
        vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vShaderStr)
        fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fShaderStr)
        // Create the program object
        programObject = GLES20.glCreateProgram()
        if (programObject == 0) return
        GLES20.glAttachShader(programObject, vertexShader)
        GLES20.glAttachShader(programObject, fragmentShader)
        // Bind vPosition to attribute 0
        GLES20.glBindAttribLocation(programObject, 0, "a_position")
        GLES20.glBindAttribLocation(programObject, 1, "a_texCoords")
        // Link the program
        GLES20.glLinkProgram(programObject)
        // Check the link status
        GLES20.glGetProgramiv(programObject, GLES20.GL_LINK_STATUS, linked, 0)
        if (linked[0] == 0) {
            Log.e(TAG, "Error linking program:")
            Log.e(TAG, GLES20.glGetProgramInfoLog(programObject))
            GLES20.glDeleteProgram(programObject)
            return
        }
        mProgramObject = programObject
    }
}