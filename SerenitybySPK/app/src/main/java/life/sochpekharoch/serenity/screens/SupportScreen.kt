package life.sochpekharoch.serenity.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material3.Surface
import androidx.compose.ui.unit.sp
import life.sochpekharoch.serenity.R
import androidx.compose.ui.res.painterResource

@Composable
fun SupportScreen(navController: NavController) {
    val context = LocalContext.current
    val josefinSans = FontFamily(Font(R.font.josefin_sans))
    val robotoLight = FontFamily(Font(R.font.roboto_light))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Support",
                style = TextStyle(
                    fontFamily = josefinSans,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            // Empty box for alignment
            Box(modifier = Modifier.size(48.dp))
        }

        // Social Media Links
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SocialMediaLink(
                icon = painterResource(id = R.drawable.ic_website),
                text = "Visit our website",
                url = "https://www.sochpekharoch.life",
                context = context
            )
            
            SocialMediaLink(
                icon = painterResource(id = R.drawable.ic_linkedin),
                text = "Follow us on LinkedIn",
                url = "https://www.linkedin.com/company/soch-pe-kharoch/",
                context = context
            )
            
            SocialMediaLink(
                icon = painterResource(id = R.drawable.ic_twitter),
                text = "Follow us on X (Twitter)",
                url = "https://x.com/spk_psy",
                context = context
            )
            
            SocialMediaLink(
                icon = painterResource(id = R.drawable.ic_instagram),
                text = "Follow us on Instagram",
                url = "https://www.instagram.com/spk.psy/",
                context = context
            )
            
            SocialMediaLink(
                icon = painterResource(id = R.drawable.ic_threads),
                text = "Follow us on Threads",
                url = "https://www.threads.net/@spk.psy",
                context = context
            )
        }
    }
}

@Composable
private fun SocialMediaLink(
    icon: Painter,
    text: String,
    url: String,
    context: Context
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFF1F1)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = text,
                modifier = Modifier.size(24.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.Black
                )
            )
        }
    }
} 