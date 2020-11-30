package com.x.selfchat

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        var bmp= BitmapFactory.decodeResource(resources,R.mipmap.ic_launcher)
        var buffer=ByteBuffer.allocate(bmp.byteCount)
        bmp.copyPixelsToBuffer(buffer)
        var obuffer= nativeTest( buffer.array(),bmp.width,bmp.height)
        bmp.recycle()
        var fos= FileOutputStream(Environment.getExternalStorageDirectory().path+"/test.jpg")
        fos.write(obuffer)
        Log.e("cpp", "onCreate: "+ obuffer.size)

    }
    companion object{
        init {
            System.loadLibrary("native-lib")
        }
        external fun nativeTest(byteArray: ByteArray,width:Int,height:Int):ByteArray
    }
}

