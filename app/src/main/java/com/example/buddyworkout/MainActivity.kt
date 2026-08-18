package com.example.buddyworkout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.buddyworkout.core.ui.gallery.ComponentGallery
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BuddyWorkoutTheme {
                ComponentGallery()
            }
        }
    }
}
