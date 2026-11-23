package com.example.iosdialer

import android.os.Bundle
import android.telecom.Call
import android.telecom.CallAudioState
import android.view.HapticFeedbackConstants
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.iosdialer.service.CallService

class CallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lockscreen par call screen show hone ke flags
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        setContent {
            IOSCallScreen(
                onEndCall = {
                    CallService.currentCall?.disconnect()
                    finish()
                }
            )
        }

        // Call cut hote hi screen apne aap band ho jaye
        CallService.callListener = { call ->
            if (call == null || call.state == Call.STATE_DISCONNECTED) {
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CallService.callListener = null
    }
}

@Composable
fun IOSCallScreen(onEndCall: () -> Unit) {
    val call = CallService.currentCall
    val phoneNumber = call?.details?.handle?.schemeSpecificPart ?: "Calling..."
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(false) }

    val darkBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1C1A27),
            Color(0xFF0D0C13),
            Color(0xFF000000)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(vertical = 36.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Caller Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Text(
                    text = phoneNumber,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "calling mobile...",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            // Middle Section: 6 Frosted Glass Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                    CallActionButton(
                        icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        label = "mute",
                        isActive = isMuted
                    ) {
                        isMuted = !isMuted
                    }
                    CallActionButton(icon = Icons.Default.Dialpad, label = "keypad") {}
                    CallActionButton(
                        icon = Icons.Default.VolumeUp,
                        label = "speaker",
                        isActive = isSpeakerOn
                    ) {
                        isSpeakerOn = !isSpeakerOn
                        CallService.currentCall?.setAudioRoute(
                            if (isSpeakerOn) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_EARPIECE
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                    CallActionButton(icon = Icons.Default.AddCall, label = "add call") {}
                    CallActionButton(icon = Icons.Default.Videocam, label = "FaceTime") {}
                    CallActionButton(icon = Icons.Default.Contacts, label = "contacts") {}
                }
            }

            // Bottom Section: End Call Button
            EndCallButton(onClick = onEndCall)
        }
    }
}

@Composable
fun CallActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    val view = LocalView.current
    val fillBrush = if (isActive) {
        Brush.verticalGradient(listOf(Color.White, Color.White.copy(alpha = 0.9f)))
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.22f),
                Color.White.copy(alpha = 0.08f)
            )
        )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(fillBrush)
                .border(
                    BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                    CircleShape
                )
                .clickable {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.Black else Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun EndCallButton(onClick: () -> Unit) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(stiffness = 300f),
        label = "endCallScale"
    )

    val redLiquidGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFF453A),
            Color(0xFFD70015)
        )
    )

    Box(
        modifier = Modifier
            .size(76.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(redLiquidGradient)
            .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                CircleShape
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.CallEnd,
            contentDescription = "End Call",
            tint = Color.White,
            modifier = Modifier.size(36.dp)
        )
    }
}
