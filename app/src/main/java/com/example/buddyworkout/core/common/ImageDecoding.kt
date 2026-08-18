package com.example.buddyworkout.core.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

/**
 * Decodes [uri] downsampled so its longest edge is roughly [targetPx].
 *
 * A modern phone photo is several thousand pixels on a side; decoding one at
 * full size to draw a 96dp circle, or to upload as an avatar, is tens of
 * megabytes of bitmap for no benefit. The bounds-only first pass is what makes
 * the sample size knowable before committing that memory.
 *
 * Returns null if the image cannot be read or decoded.
 */
fun decodeSampledBitmap(context: Context, uri: Uri, targetPx: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, bounds)
    } ?: return null

    var sample = 1
    val longest = maxOf(bounds.outWidth, bounds.outHeight)
    // Halve until the next halving would go under the target, so the decoded
    // image is always at least as large as asked for.
    while (longest / sample > targetPx * 2) sample *= 2

    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    return context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, options)
    }
}
