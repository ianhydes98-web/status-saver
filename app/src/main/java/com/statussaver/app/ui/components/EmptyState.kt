package com.statussaver.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.statussaver.app.ui.theme.TextPrimary
import com.statussaver.app.ui.theme.TextSecondary

@Composable
fun EmptyState(
    isVideo: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val type = if (isVideo) "videos" else "images"
        Text(
            text = "No $type yet.",
            color = TextPrimary,
            fontSize = 17.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "View a WhatsApp status to see it here.",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}
