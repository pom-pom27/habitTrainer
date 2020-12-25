package com.example.habbittrainer

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.habbittrainer.databinding.ActivityCreateHabitBinding
import com.example.habbittrainer.db.HabitDbTable
import java.io.IOException

class CreateHabitActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateHabitBinding
    private var imageBitmap: Bitmap? = null
    private val TAG = CreateHabitActivity::class.simpleName
    private val CHOOSE_IMAGE_REQUEST = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateHabitBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun chooseImage(view: View) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        val chooser = Intent.createChooser(
            intent,
            "Choose image for habit"
        )
        startActivityForResult(chooser, CHOOSE_IMAGE_REQUEST)

        Log.d(TAG, "Intent to choose image sent...")
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CHOOSE_IMAGE_REQUEST && resultCode == Activity
                .RESULT_OK && data != null && data.data != null
        ) {
            Log.d(TAG, "An image was chosen by the user.")

            val bitmap = tryReadingBitmap(data.data!!)

            bitmap?.let {
                binding.ivImage.setImageBitmap(bitmap)

                imageBitmap = bitmap
                Log.d(TAG, "Read iamge bitmap and updated image view")
            }
        }

    }

    private fun tryReadingBitmap(data: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(
                    contentResolver,
                    data
                )
            } else {
                val source = ImageDecoder.createSource(contentResolver, data)
                ImageDecoder.decodeBitmap(source)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun storeImage(view: View) {
        if (binding.etTitle.isBlank() || binding.etDesc.isBlank()) {
            displayError(getString(R.string.title_desc_required))
            Log.d(TAG, "Cant store habit: titile or description missing")
            return
        } else if (imageBitmap == null) {
            Log.d(TAG, "Cant store habit: titile or description missing")
            displayError(getString(R.string.image_required))
            return
        }

        val title = binding.etTitle.text.toString()
        val desc = binding.etDesc.text.toString()

        val habit = Habit(title, desc, imageBitmap!!)
        val id = HabitDbTable(this).store(habit)

        if (id == -1L) {
            displayError("Habit could not be stored")
        } else {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun displayError(message: String) {
        with(binding.tvError) {
            text = message
            visibility = View.VISIBLE
        }
    }

    private fun EditText.isBlank() = this.text.isBlank()
}