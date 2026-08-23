package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    borderColor: Color = CyberCardBorder,
    backgroundColor: Color = CyberSurface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(12.dp),
        content = content
    )
}

@Composable
fun MethodBadge(
    method: String,
    modifier: Modifier = Modifier
) {
    val color = getMethodColor(method)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = method.uppercase(),
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = CodeFont
        )
    }
}

@Composable
fun StatusCodeBadge(
    code: Int,
    statusText: String = "",
    modifier: Modifier = Modifier
) {
    val color = getStatusColor(code)
    val displayText = if (code == 0) "FAIL" else code.toString()
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = displayText,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CodeFont
            )
            if (statusText.isNotBlank()) {
                Text(
                    text = statusText.take(12),
                    color = color.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ResourceTypeBadge(
    type: String,
    modifier: Modifier = Modifier
) {
    val color = getResourceTypeColor(type)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = type.uppercase(),
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = CodeFont
        )
    }
}

@Composable
fun PulsingRecordingIndicator(
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isRecording) CyberRose.copy(alpha = 0.15f) else CyberTextMuted.copy(alpha = 0.15f))
            .border(
                1.dp,
                if (isRecording) CyberRose.copy(alpha = alpha) else CyberTextMuted.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isRecording) CyberRose.copy(alpha = alpha) else CyberTextMuted)
        )
        Text(
            text = if (isRecording) "REC" else "PAUSED",
            color = if (isRecording) CyberRose else CyberTextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = CodeFont
        )
    }
}

@Composable
fun CyberSectionHeader(
    title: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = CyberTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (action != null) {
            action()
        }
    }
}

@Composable
fun KeyValueRow(
    keyText: String,
    valueText: String,
    onCopy: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(CyberSurfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = keyText,
                color = CyberCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = CodeFont
            )
            Text(
                text = valueText,
                color = CyberTextPrimary,
                fontSize = 12.sp,
                fontFamily = CodeFont,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (onCopy != null) {
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = CyberTextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
