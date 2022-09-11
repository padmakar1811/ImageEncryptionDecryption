package com.padmakar.imageencryptiondecryption

import android.Manifest
import android.widget.EditText
import android.os.Bundle
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.content.Intent
import android.widget.Toast
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.ImageDecoder
import android.os.Build
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.io.IOException

class ImageEncryptActivity : AppCompatActivity() {
    var encryptBtn: Button? = null
    var decryptBtn: Button? = null
    var sImage: String? = null
    var imageView: ImageView? = null
    var encImgToText: EditText? = null
    var clipboardManager: ClipboardManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        encryptBtn = findViewById(R.id.enc_img_btn)
        decryptBtn = findViewById(R.id.dec_img_btn)
        encImgToText = findViewById(R.id.encText)
        imageView = findViewById(R.id.imageView)
        clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        encryptBtn?.setOnClickListener(View.OnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this@ImageEncryptActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@ImageEncryptActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    100
                )
            } else {
                selectImage()
            }
        })
        with(decryptBtn) {
            encryptBtn = findViewById(R.id.enc_img_btn)
            decryptBtn = findViewById(R.id.dec_img_btn)
            encImgToText = findViewById(R.id.encText)
            imageView = findViewById(R.id.imageView)
            clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            encryptBtn?.setOnClickListener(View.OnClickListener {
                if (ContextCompat.checkSelfPermission(
                        this@ImageEncryptActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@ImageEncryptActivity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        100
                    )
                } else {
                    selectImage()
                }
            })
            decryptBtn?.setOnClickListener(View.OnClickListener {
                val bytes = Base64.decode(encImgToText!!.text.toString(), Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imageView!!.setImageBitmap(bitmap)
            })
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 100)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImage()
        } else {
            Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            val bitmap: Bitmap
            var source: ImageDecoder.Source? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                source = ImageDecoder.createSource(this.contentResolver, uri!!)
                try {
                    bitmap = ImageDecoder.decodeBitmap(source)
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    val bytes = stream.toByteArray()
                    sImage = Base64.encodeToString(bytes, Base64.DEFAULT)
                    encImgToText!!.setText(sImage)
                    Toast.makeText(
                        this,
                        "Image encrypted! Click on Decrypt to restore!",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun copyImgCode(view: View?) {
        val data = encImgToText!!.text.toString().trim { it <= ' ' }
        if (!data.isEmpty()) {
            val temp = ClipData.newPlainText("text", data)
            clipboardManager!!.setPrimaryClip(temp)
            Toast.makeText(this, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
        }
    }
}