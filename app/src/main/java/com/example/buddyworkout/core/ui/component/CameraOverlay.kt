package com.example.buddyworkout.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwRadius

/**
 * The record-workout viewfinder surface: dark rounded panel, live rep count,
 * form chip and session footer.
 *
 * [cameraPreview] is where the CameraX `PreviewView` goes once the rep counter
 * is wired up (see `docs/architecture/05-lld-pushup-counter.md`). Until then the
 * panel renders the static pose skeleton from the mockup.
 */
@Composable
fun CameraOverlay(
    reps: Int,
    formLabel: String?,
    footerLabel: String,
    footerValue: String,
    modifier: Modifier = Modifier,
    showPoseSkeleton: Boolean = true,
    cameraPreview: @Composable BoxScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .background(BwColors.CameraBg, RoundedCornerShape(BwRadius.Tile))
            .clip(RoundedCornerShape(BwRadius.Tile)),
    ) {
        cameraPreview()

        if (showPoseSkeleton) {
            PoseSkeleton(Modifier.fillMaxSize())
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = reps.toString(),
                color = Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayLarge,
            )
            Text(
                text = "PUSHUPS",
                color = BwColors.CameraSubtle,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                ),
            )
        }

        if (formLabel != null) {
            Text(
                text = "● $formLabel",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 18.dp, end = 16.dp)
                    .background(BwColors.Primary.copy(alpha = 0.9f), RoundedCornerShape(BwRadius.Pill))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(BwRadius.Control))
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text = footerLabel,
                color = BwColors.CameraSubtle,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = footerValue,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
            )
        }
    }
}

/**
 * The pose wireframe from the mockup, drawn in the export's own 324×460
 * coordinate space and scaled to fit (SVG `xMidYMid meet`).
 */
@Composable
private fun PoseSkeleton(modifier: Modifier = Modifier) {
    val joints = listOf(
        120f to 150f, 210f to 150f, 250f to 210f, 95f to 215f,
        210f to 250f, 135f to 250f, 245f to 320f, 120f to 330f,
    )
    val bones = listOf(
        (120f to 150f) to (210f to 150f),
        (210f to 150f) to (250f to 210f),
        (120f to 150f) to (95f to 215f),
        (210f to 150f) to (210f to 250f),
        (120f to 150f) to (135f to 250f),
        (210f to 250f) to (245f to 320f),
        (135f to 250f) to (120f to 330f),
    )
    Canvas(modifier) {
        val scale = minOf(size.width / 324f, size.height / 460f)
        val dx = (size.width - 324f * scale) / 2f
        val dy = (size.height - 460f * scale) / 2f
        fun point(p: Pair<Float, Float>) = Offset(dx + p.first * scale, dy + p.second * scale)

        bones.forEach { (from, to) ->
            drawLine(
                color = BwColors.Primary,
                start = point(from),
                end = point(to),
                strokeWidth = 3f * scale,
                cap = StrokeCap.Round,
                alpha = 0.85f,
            )
        }
        joints.forEach { drawCircle(BwColors.PoseJoint, radius = 7f * scale, center = point(it)) }
    }
}

@Preview(showBackground = true, widthDp = 340, heightDp = 500)
@Composable
private fun CameraOverlayPreview() = BuddyWorkoutTheme {
    Box(Modifier.fillMaxSize().padding(18.dp)) {
        CameraOverlay(
            reps = 12,
            formLabel = "Good form",
            footerLabel = "PUSHUP CHALLENGE · 1st of 4",
            footerValue = "128 total · +12 this session",
            modifier = Modifier.fillMaxSize(),
        )
    }
}
