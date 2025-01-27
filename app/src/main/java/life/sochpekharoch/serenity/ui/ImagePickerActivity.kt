package life.sochpekharoch.serenity.ui

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import life.sochpekharoch.serenity.ImageCropActivity

abstract class ImagePickerActivity : ComponentActivity() {
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { launchImageCrop(it) }
    }

    private val cropImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val croppedUri = result.data?.getParcelableExtra<Uri>("croppedUri")
            croppedUri?.let { onImageCropped(it) }
        }
    }

    private fun launchImageCrop(uri: Uri) {
        val intent = Intent(this, ImageCropActivity::class.java).apply {
            putExtra("imageUri", uri)
        }
        cropImage.launch(intent)
    }

    fun launchImagePicker() {
        pickImage.launch("image/*")
    }

    abstract fun onImageCropped(uri: Uri)
} 