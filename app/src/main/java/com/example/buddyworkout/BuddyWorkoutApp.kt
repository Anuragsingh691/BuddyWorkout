package com.example.buddyworkout

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Hilt's entry point. Firebase itself needs no explicit init — the
 * `google-services` plugin bakes the config from `app/google-services.json` into
 * resources, and `FirebaseInitProvider` starts the default app before
 * [onCreate] runs.
 */
@HiltAndroidApp
class BuddyWorkoutApp : Application()
