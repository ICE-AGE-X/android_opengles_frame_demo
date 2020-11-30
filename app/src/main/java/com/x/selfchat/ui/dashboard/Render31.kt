package com.x.selfchat.ui.dashboard

import android.content.Context
import android.opengl.*
import android.os.Build
import androidx.annotation.RequiresApi
import com.x.selfchat.ui.dashboard.BaseRender
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Render31(context: Context) : BaseRender(context) {
    private var vao: Int = 0
    private var textureLoc: Int = 0
    private var vsh: String = "#version 310 es\n" +
            "precision lowp float;\n" +
            "layout(location=0) in vec3 aPos;\n" +
            "layout(location=1) in vec2 aTexCoord;\n" +
            "out vec2 TexCoord;\n" +
            "uniform mat4 model;\n" +
            "uniform mat4 view;\n" +
            "uniform mat4 projection;\n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position=projection *view* model *vec4(aPos.x,aPos.y*-1.0f,aPos.z,1.0f);\n" +
            "    TexCoord=aTexCoord;\n" +
            "}"
    private var fsh: String = "#version 310 es\n" +
            "precision lowp float;\n" +
            "out vec4 FragColor;\n" +
            "in vec2 TexCoord;\n" +
            "uniform sampler2D texture1;\n" +
            "void main()\n" +
            "{\n" +
            "    FragColor=texture(texture1,vec2(TexCoord.x,TexCoord.y));\n" +
            "}\n"

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)
        shaderProgram = createShader(vsh, fsh)
        var vertices = floatArrayOf(
                // positions    // texture coords
                1f, 1f, 0.0f,  1.0f, 1.0f, // top right
                1f, -1f, 0.0f,  1.0f, 0.0f, // bottom right
                -1f, -1f, 0.0f,  0.0f, 0.0f, // bottom left
                -1f, 1f, 0.0f,  0.0f, 1.0f  // top left
        )

        var indices = intArrayOf(0, 1, 3, 1, 2, 3)

        var vbo = IntBuffer.allocate(1)
        var vao = IntBuffer.allocate(1)
        var ebo = IntBuffer.allocate(1)
        GLES31.glGenVertexArrays(1, vao)
        GLES31.glGenBuffers(1, vbo)
        GLES31.glGenBuffers(1, ebo)
        GLES31.glBindVertexArray(vao[0])

        var vbbf = ByteBuffer.allocateDirect(vertices.size * Float.SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertices)
        vbbf.position(0)
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, vbo[0]);
        GLES31.glBufferData(
                GLES31.GL_ARRAY_BUFFER,
                vertices.size * Float.SIZE_BYTES,
                vbbf,
                GLES31.GL_STATIC_DRAW
        )

        var ebbf = ByteBuffer.allocateDirect(indices.size * Int.SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asIntBuffer().put(indices)
        ebbf.position(0)
        GLES31.glBindBuffer(GLES31.GL_ELEMENT_ARRAY_BUFFER, ebo[0])
        GLES31.glBufferData(
                GLES31.GL_ELEMENT_ARRAY_BUFFER,
                indices.size * Int.SIZE_BYTES,
                ebbf,
                GLES31.GL_STATIC_DRAW
        )

        GLES31.glVertexAttribPointer(0, 3, GLES31.GL_FLOAT, false, 5 * Float.SIZE_BYTES, 0)
        GLES31.glEnableVertexAttribArray(0)

        GLES31.glVertexAttribPointer(
                1,
                2,
                GLES31.GL_FLOAT,
                false,
                5 * Float.SIZE_BYTES,
                3 * Float.SIZE_BYTES
        )
        GLES31.glEnableVertexAttribArray(1)

        this.vao = vao[0]
        GLES31.glUseProgram(shaderProgram)
        var loc = GLES31.glGetUniformLocation(shaderProgram, "texture1")
        GLES31.glUniform1i(loc, 0)
        textureLoc = loc

        projLoc = GLES20.glGetUniformLocation(shaderProgram, "projection")
        modelLoc = GLES20.glGetUniformLocation(shaderProgram, "model")
        viewLoc = GLES20.glGetUniformLocation(shaderProgram, "view")
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    override fun render() {

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texs[frameIdx])
//        GLES20.glUniformMatrix4fv(projLoc, 1, false, mProjectionMatrix, 0)
        GLES30.glBindVertexArray(vao)
        GLES20.glDrawElements(GLES31.GL_TRIANGLES, 6, GLES31.GL_UNSIGNED_INT, 0)

    }

    override fun commitTexture(array: ByteArray?) {
        val imgData = array?.asUByteArray()!!
        val blockdim_x = imgData[4]
        val blockdim_y = imgData[5]
        val blockdim_z = imgData[6]
        val xSize0 = imgData[7]
        val xSize1 = imgData[8]
        val xSize2 = imgData[9]

        var ySize0 = imgData[10]
        var ySize1 = imgData[11]
        var ySize2 = imgData[12]

        var zSize0 = imgData[13]
        var zSize1 = imgData[14]
        var zSize2 = imgData[15]

        var xSize = xSize0 + (xSize1.toUInt().shl(8)) + (xSize2.toUInt().shl(16))
        var ySize = ySize0 + (ySize1.toUInt().shl(8)) + (ySize2.toUInt().shl(16))
        var zSize = zSize0 + (zSize1.toUInt().shl(8)) + (zSize2.toUInt().shl(16))

        var xblocks = (xSize + blockdim_x - 1u) / blockdim_x
        var yblocks = (ySize + blockdim_y - 1u) / blockdim_y
        var zblocks = (zSize + blockdim_z - 1u) / blockdim_z
        var len = (xblocks * yblocks * zblocks).shl(4)
        var id = imgData.sliceArray(IntRange(16, imgData.size - 1))

        setNormalMat(mModelMatrix)
        Matrix.scaleM(mModelMatrix,0,1f,ySize.toFloat()/xSize.toFloat(),1f)
        var tBuffer =
                ByteBuffer.allocateDirect(id.size * Byte.SIZE_BYTES).order(ByteOrder.nativeOrder())
                        .put(id.toByteArray())
        tBuffer.position(0)
        GLES31.glCompressedTexImage2D(
                GLES31.GL_TEXTURE_2D, 0, GLES31Ext.GL_COMPRESSED_RGBA_ASTC_8x8_KHR,
                xSize.toInt(), ySize.toInt(), 0, len.toInt(),
                tBuffer
        )

    }
}