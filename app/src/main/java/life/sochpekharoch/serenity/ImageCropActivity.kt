package life.sochpekharoch.serenity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.UUID

class ImageCropActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sourceUri = intent.getParcelableExtra<Uri>("imageUri")
        if (sourceUri == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        val destinationUri = Uri.fromFile(File(cacheDir, "${UUID.randomUUID()}.jpg"))

        UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(4f, 5f)  // 4:5 aspect ratio
            .withMaxResultSize(1080, 1350)  // Maximum resolution
            .withOptions(UCrop.Options().apply {
                setCompressionQuality(85)
                setHideBottomControls(false)
                setFreeStyleCropEnabled(false)
                setToolbarTitle("Crop Image")
                setStatusBarColor(getColor(R.color.white))
                setToolbarColor(getColor(R.color.white))
                setToolbarWidgetColor(getColor(R.color.black))
            })
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK && data != null) {
            val resultUri = UCrop.getOutput(data)
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra("croppedUri", resultUri)
            })
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }
} 