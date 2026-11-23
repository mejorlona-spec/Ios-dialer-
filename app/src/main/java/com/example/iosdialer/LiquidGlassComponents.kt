package com.example.iosdialer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IOSKeypadScreen() {
    var dialedNumber by remember { mutableStateOf("") }
    val context = LocalContext.current
    val view = LocalView.current

    val backgroundGradient = Brush.radialGradient(
        colors = listOf(
            Color(0xFF2C1E4A),
            Color(0xFF0F1016),
            Color(0xFF000000)
        ),
        radius = 1400f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Dialed Number Display
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = dialedNumber,
                    fontSize = if (dialedNumber.length > 9) 32.sp else 42.sp,
                    fontWeight = FontWeight.ExtraLight,
                    color = Color.White,
                    maxLines = 1
                )
                if (dialedNumber.isNotEmpty()) {
                    Text(
                        text = "Add Number",
                        fontSize = 15.sp,
                        color = Color(0xFF0A84FF),
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clickable { /* Add to Contacts */ }
                    )
                }
            }

            // Dialpad Grid (Liquid Glass)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val keypadRows = listOf(
                    listOf("1" to "", "2" to "A B C", "3" to "D E F"),
                    listOf("4" to "G H I", "5" to "J K L", "6" to "M N O"),
                    listOf("7" to "P Q R S", "8" to "T U V", "9" to "W X Y Z"),
                    listOf("*" to "", "0" to "+", "#" to "")
                )

                for (row in keypadRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for ((digit, letters) in row) {
                            LiquidGlassButton(
                                digit = digit,
                                letters = letters,
                                onClick = { dialedNumber += digit }
                            )
                        }
                    }
                }

                // Call Action Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.size(78.dp))

                    LiquidCallButton {
                        if (dialedNumber.isNotEmpty()) {
                            makeDirectCall(context, dialedNumber)
                        }
                    }

                    Box(
                        modifier = Modifier.size(78.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dialedNumber.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Backspace,
                                contentDescription = "Delete",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        dialedNumber = dialedNumber.dropLast(1)
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiquidGlassButton(
    digit: String,
    letters: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "glassScale"
    )

    val glassFill = Brush.verticalGradient(
        colors = if (isPressed)
        
