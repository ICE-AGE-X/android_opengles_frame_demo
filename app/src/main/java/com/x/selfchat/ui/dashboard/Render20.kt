package com.x.selfchat.ui.dashboard

import android.content.Context
import android.opengl.ETC1Util
import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Render20(context: Context) : BaseRender(context) {

    private val vsh: String =
            "attribute vec3 aPos;\n" +
                    "attribute vec2 texCoord;\n" +
                    "\n" +
                    "uniform mat4 model;\n" +
                    "uniform mat4 view;\n" +
                    "uniform mat4 projection;\n" +
                    "\n" +
                    "varying vec2 outTexCoord;\n" +
                    "varying vec4 outPos;\n" +
                    "void main()\n" +
                    "{\n" +
                    "    outTexCoord=texCoord;\n" +
                    "    gl_Position=projection *view *model* vec4(aPos.x,aPos.y*-1.0f,aPos.z,1.0f);\n" +
                    "}"
    private val fsh: String =
            "varying vec2 outTexCoord;\n" +
                    "uniform sampler2D texture1;\n" +
                    "varying vec4 outPos;\n" +
                    "void main()\n" +
                    "{\n" +
                    "    vec2 offset=vec2(1.0f,0.5f);\n" +
                    "    vec2 uv=outTexCoord*offset;\n" +
                    "    vec4 color =texture2D(texture1,vec2(uv.x,uv.y));\n" +
                    "\n" +
                    "    vec2 aUv=uv+vec2(0.0,0.5);\n" +
                    "    color.a=texture2D(texture1,vec2(aUv.x,aUv.y)).r;\n" +
                    "    gl_FragColor=color;\n" +
                    "}"

    private var aPosLoc: Int = 0
    private var texLoc: Int = 0
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)
//        setFps(25)
        shaderProgram = createShader(vsh, fsh)
        aPosLoc = GLES20.glGetAttribLocation(shaderProgram, "aPos")
        texLoc = GLES20.glGetAttribLocation(shaderProgram, "texCoord")
        var vertices = floatArrayOf(
                // positions                    // texture coords
                1f, 1f, 0.0f, 1.0f, 1.0f, // top right
                1f, -1f, 0.0f, 1.0f, 0.0f, // bottom right
                -1f, -1f, 0.0f, 0.0f, 0.0f, // bottom left
                -1f, 1f, 0.0f, 0.0f, 1.0f  // top left
        )

        var indices = intArrayOf(0, 1, 3, 1, 2, 3)
        var ebo = IntBuffer.allocate(1)

        var vbbf = ByteBuffer.allocateDirect(vertices.size * Float.SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertices)
        vbbf.position(0)

        var vbo = IntArray(1)
        GLES20.glGenBuffers(1, vbo, 0)
        GLES20.glGenBuffers(1, ebo)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
        GLES20.glBufferData(
                GLES20.GL_ARRAY_BUFFER,
                vertices.size * Float.SIZE_BYTES,
                vbbf,
                GLES20.GL_STATIC_DRAW
        )

        var ebbf = ByteBuffer.allocateDirect(indices.size * Int.SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asIntBuffer().put(indices)
        ebbf.position(0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ebo[0])
        GLES20.glBufferData(
                GLES20.GL_ELEMENT_ARRAY_BUFFER,
                indices.size * Int.SIZE_BYTES,
                ebbf,
                GLES20.GL_STATIC_DRAW
        )

        GLES20.glVertexAttribPointer(aPosLoc, 3, GLES20.GL_FLOAT, false, 5 * Float.SIZE_BYTES, 0)
        GLES20.glEnableVertexAttribArray(aPosLoc)

        GLES20.glVertexAttribPointer(
                texLoc,
                2,
                GLES20.GL_FLOAT,
                false,
                5 * Float.SIZE_BYTES,
                3 * Float.SIZE_BYTES
        )
        GLES20.glEnableVertexAttribArray(texLoc)

        modelLoc = GLES20.glGetUniformLocation(shaderProgram, "model")
        viewLoc = GLES20.glGetUniformLocation(shaderProgram, "view")
        projLoc = GLES20.glGetUniformLocation(shaderProgram, "projection")

    }

    override fun render() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texs[frameIdx])
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_INT, 0)

//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texs[66])
//        GLES20.glDrawElements(GLES20.GL_TRIANGLES,6,GLES20.GL_UNSIGNED_INT,0)
    }


    override fun commitTexture(data: ByteArray?) {

        var tx= ETC1Util.createTexture(data?.inputStream())
        setNormalMat(mModelMatrix)
        Matrix.scaleM(mModelMatrix,0,1f,tx.height/ 2 /tx.width.toFloat(),1f)

        ETC1Util.loadTexture(
                GLES20.GL_TEXTURE_2D,
                0,
                0,
                GLES20.GL_RGB,
                GLES20.GL_UNSIGNED_SHORT_5_6_5,
                tx
        )

    }

}