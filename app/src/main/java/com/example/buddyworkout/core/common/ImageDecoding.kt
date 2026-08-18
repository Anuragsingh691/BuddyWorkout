package com.example.buddyworkout.core.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

/**
 * `inSampleSize` for an image whose longest edge is [longestEdge], targeting
 * [targetPx].
 *
 * Halves until the *next* halving would go under the target, so the decoded
 * image is never smaller than asked for. BitmapFactory only honours powers of
 * two, which is why this doubles rather than dividing to fit.
 */
fun sampleSizeFor(longestEdge: Int, targetPx: Int): Int {
    if (longestEdge <= 0 || targetPx <= 0) return 1

    var sample = 1
    while (longestEdge / sample > targetPx * 2) sample *= 2
    return sample
}

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
    context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, bounds)
    }

    // `outWidth` is the only signal that the first pass worked. A bounds-only
    // decode returns null by contract, so testing its return value instead
    // rejects every image, including the ones that read perfectly.
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSizeFor(maxOf(bounds.outWidth, bounds.outHeight), targetPx)
    }
    return context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, options)
    }
}

/** Decodes stored avatar bytes. Returns null rather than throwing on junk. */
fun decodeAvatar(bytes: ByteArray): Bitmap? =
    runCatching { BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }.getOrNull()
