package life.sochpekharoch.serenity.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ImagePickerButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text("Add Image")
    }
} 