package com.akashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.akashboard.R
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.akashboard.ui.theme.AkashBoardTheme

class OnboardingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AkashBoardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OnboardingScreen(
                        onEnableKeyboard = {
                            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                        },
                        onSelectKeyboard = {
                            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.showInputMethodPicker()
                        },
                        onFinish = {
                            launchMainApp()
                        },
                        isKeyboardEnabled = ::isKeyboardEnabled,
                        isKeyboardSelected = ::isKeyboardSelected
                    )
                }
            }
        }
    }

    private fun isKeyboardEnabled(): Boolean {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return imm.enabledInputMethodList.any { it.packageName == packageName }
    }

    private fun isKeyboardSelected(): Boolean {
        val currentIME = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        return currentIME?.contains(packageName) == true
    }

    private fun launchMainApp() {
        getSharedPreferences("akashboard_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("onboarding_complete", true)
            .apply()
        startActivity(Intent(this, SettingsActivity::class.java))
        finish()
    }
}

@Composable
fun OnboardingScreen(
    onEnableKeyboard: () -> Unit,
    onSelectKeyboard: () -> Unit,
    onFinish: () -> Unit,
    isKeyboardEnabled: () -> Boolean,
    isKeyboardSelected: () -> Boolean
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var isEnabled by remember { mutableStateOf(isKeyboardEnabled()) }
    var isSelected by remember { mutableStateOf(isKeyboardSelected()) }

    // Re-check status when returning to the activity
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isEnabled = isKeyboardEnabled()
                isSelected = isKeyboardSelected()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val currentStep = when {
        !isEnabled -> 1
        !isSelected -> 2
        else -> 3
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Hero Icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✨",
                fontSize = 48.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.onboarding_welcome_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "The keyboard that becomes YOU.
Fast, secure, and 100% offline.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Progress indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepCircle(step = 1, isActive = currentStep == 1, isCompleted = currentStep > 1)
            StepLine(isActive = currentStep > 1)
            StepCircle(step = 2, isActive = currentStep == 2, isCompleted = currentStep > 2)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (currentStep) {
                    1 -> {
                        Text(
                            text = stringResource(R.string.onboarding_step_enable_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.onboarding_step_enable_desc),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onEnableKeyboard,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(stringResource(R.string.onboarding_step_enable_btn), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    2 -> {
                        Text(
                            text = stringResource(R.string.onboarding_step_select_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.onboarding_step_select_desc),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onSelectKeyboard,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(stringResource(R.string.onboarding_step_select_btn), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    3 -> {
                        var testText by remember { mutableStateOf("") }
                        Text(
                            text = stringResource(R.string.onboarding_step_done_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.onboarding_step_done_desc),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = testText,
                            onValueChange = { testText = it },
                            placeholder = { Text("Tap here to test AkashBoard!") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onFinish,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(stringResource(R.string.onboarding_step_done_btn), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        
        TextButton(
            onClick = onFinish,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(stringResource(R.string.onboarding_skip), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun StepCircle(step: Int, isActive: Boolean, isCompleted: Boolean) {
    val color = when {
        isCompleted -> MaterialTheme.colorScheme.primary
        isActive -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    }
    
    val textColor = when {
        isCompleted || isActive -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Text("✓", color = textColor, fontWeight = FontWeight.Bold)
        } else {
            Text("$step", color = textColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StepLine(isActive: Boolean) {
    Box(
        modifier = Modifier
            .width(60.dp)
            .height(4.dp)
            .background(
                if (isActive) MaterialTheme.colorScheme.primary 
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
    )
}
