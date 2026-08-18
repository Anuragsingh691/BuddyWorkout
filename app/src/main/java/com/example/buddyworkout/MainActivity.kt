package com.example.buddyworkout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.buddyworkout.core.navigation.AppViewModel
import com.example.buddyworkout.core.navigation.AuthState
import com.example.buddyworkout.core.navigation.BuddyWorkoutNavHost
import com.example.buddyworkout.core.navigation.Home
import com.example.buddyworkout.core.navigation.Login
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BuddyWorkoutTheme {
                BuddyWorkoutRoot()
            }
        }
    }
}

/**
 * Session gate. While the session is [AuthState.Loading] nothing but the theme
 * background is drawn — composing the NavHost early would flash the login
 * screen before Firebase restores a persisted session.
 *
 * The gate picks the entry point once; every later transition (sign-in,
 * sign-out) is explicit navigation inside the graph.
 */
@Composable
private fun BuddyWorkoutRoot() {
    val appViewModel: AppViewModel = hiltViewModel()
    val authState by appViewModel.state.collectAsStateWithLifecycle()

    when (authState) {
        AuthState.Loading -> Box(
            Modifier
                .fillMaxSize()
                .background(BwColors.Bg)
        )

        else -> {
            val startDestination = remember { if (authState == AuthState.SignedIn) Home else Login }
            BuddyWorkoutNavHost(startDestination = startDestination)
        }
    }
}
