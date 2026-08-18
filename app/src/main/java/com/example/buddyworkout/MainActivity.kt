package com.example.buddyworkout

import android.content.Intent
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * The Activity is `singleTop`, so an invite App Link opened while the app
     * is already running arrives here rather than in the launch intent. Compose
     * Navigation only reads the launch intent, so without this relay such links
     * would be silently dropped.
     */
    private val newIntents = MutableSharedFlow<Intent>(extraBufferCapacity = 1)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        newIntents.tryEmit(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BuddyWorkoutTheme {
                BuddyWorkoutRoot(newIntents)
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
private fun BuddyWorkoutRoot(newIntents: Flow<Intent>) {
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
            BuddyWorkoutNavHost(startDestination = startDestination, newIntents = newIntents)
        }
    }
}
