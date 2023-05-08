package com.boo.sample.samplesaf

import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files.size
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    val TAG = this::class.java.simpleName

    private val btn_open by lazy { findViewById<Button>(R.id.btn_open) }
    private val btn_write by lazy { findViewById<Button>(R.id.btn_write) }
    private val btn_folder by lazy { findViewById<Button>(R.id.btn_folder) }
    private val btn_read by lazy { findViewById<Button>(R.id.btn_read) }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 119)

        btn_open.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                type = "*/*"
            }

            fileOpenLauncher.launch(intent)
        }

        btn_write.setOnClickListener {
            /*val fileName = "NewImage.jpg"
            val makeFileIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/jpg"
                putExtra(Intent.EXTRA_TITLE, fileName)
            }

            fileWriteLauncher.launch(makeFileIntent)*/

            try {
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "appg")
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS.plus("/APPG"))

                val uri = contentResolver.insert(
                    MediaStore.Files.getContentUri("external"),
                    contentValues
                )

                val outputStream = uri?.let { _uri -> contentResolver.openOutputStream(_uri) }
                outputStream?.write("This is appg test file modified2".toByteArray())
                outputStream?.close()

                Log.d(TAG, "writing file is succeeded")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d(TAG, "writing file is failed")
            }





        }

        btn_folder.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if(Environment.isExternalStorageManager()){
                    Log.d(TAG, "권한 허용상태")
                } else {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION, Uri.parse("pacakge:${BuildConfig.APPLICATION_ID}"))
//                        intent.addCategory("android.intent.category.DEFAULT")
                        intent.data = Uri.parse(String.format("package:%s", BuildConfig.APPLICATION_ID))
                        fileManagerLauncher.launch(intent)
                    } catch (e : Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                Log.d(TAG, "버전 30 이하")
            }


            /*try {
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "appg")
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                contentValues.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "/APPG"
                )


                val uri = contentResolver.insert(
                    MediaStore.Files.getContentUri("external"),
                    contentValues
                )

                val outputStream = uri?.let { _uri -> contentResolver.openOutputStream(_uri) }

                outputStream?.write("This is appg test file".toByteArray())

                outputStream?.close()
                Log.d(TAG, "writing file is succeeded")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d(TAG, "writing file is failed")
            }*/
        }

        btn_read.setOnClickListener {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q) readFile()
        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun readFile() {
        //test
        val contentUri = MediaStore.Files.getContentUri("external")
//        val contentUri = MediaStore.Files.getContentUri(VOLUME_EXTERNAL_PRIMARY)
        val selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?"
        val selectionArgs = arrayOf(Environment.DIRECTORY_DOCUMENTS.plus("/APPG/"))
//        val selectionArgs = arrayOf(Environment.DIRECTORY_DOWNLOADS)
        val cursor = contentResolver.query(contentUri, null, selection, selectionArgs, null)

        if(cursor?.count == 0) {
            Toast.makeText(this, "No file found in \"" + Environment.DIRECTORY_DOCUMENTS + "/APPG", Toast.LENGTH_LONG).show();
        } else {
            while(cursor?.moveToNext() == true) {
                val index_name = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                val fileName = cursor.getString(index_name)

                val index_id = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
                val id = cursor.getLong(index_id)

                val uri = ContentUris.withAppendedId(contentUri, id)
                Log.d(TAG, "fileUri is $uri")

                //파일 읽기
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val size = inputStream?.available()
                    val bytes = size?.let { ByteArray(it) }
                    inputStream?.read(bytes)
                    inputStream?.close()

                    val content = bytes?.let { String(it, UTF_8) }
                    Log.d(TAG, "readFile: $content")

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "result : ${grantResults[0]}")
    }

    val fileManagerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.d(TAG, "result is RESULT_OK")

            val value = result.data?.data
            Log.d(TAG, "data is $value")

        }
    }

    val fileOpenLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "result is RESULT_OK")

                val uri = result.data?.data
                Log.d(TAG, "uri is $uri")

            }
        }

    val fileWriteLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "result is RESULT_OK")

                val uri = result.data?.data
                if (uri != null) {
                    contentResolver.openFileDescriptor(uri, "w").use {
                        FileOutputStream(it!!.fileDescriptor).use { outStream ->
                            val imageInputStream = resources.openRawResource(R.raw.app_icon)
                            while (true) {
                                val data = imageInputStream.read()
                                if (data == -1) {
                                    break
                                }
                                outStream.write(data)
                            }
                            imageInputStream.close()
                        }
                    }
                }
            }
        }


}