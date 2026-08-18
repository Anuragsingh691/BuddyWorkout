package com.example.buddyworkout.core.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * The 27 icons used across the mockups.
 *
 * These are the Feather-style outline icons inlined as SVG in the Figma export.
 * The path data below is copied verbatim from that file and parsed at build time
 * with [PathParser], so the icons are pixel-identical to the design.
 *
 * We do not depend on `material-icons-extended`: it is deprecated, frozen at
 * 1.7.8, and no longer published in the Compose BOM — pulling it in would drag a
 * stale `compose-material` alongside the current BOM.
 *
 * Stroke icons are drawn in black and expected to be tinted by the caller
 * (`Icon(..., tint = ...)`). [Google] is multi-colour and must be rendered with
 * `tint = Color.Unspecified` or via `Image`.
 */
object BwIcons {

    val Dumbbell = stroke("Dumbbell", "M2 12h2M20 12h2M5 9v6M19 9v6M8 7v10M16 7v10M8 12h8")

    val DumbbellSmall =
        stroke("DumbbellSmall", "M6 9v6M18 9v6M4 12h2M18 12h2M8 8v8M16 8v8M8 12h8")

    val ArrowRight = stroke("ArrowRight", "M5 12h14M13 6l6 6-6 6")

    val ChevronLeft = stroke("ChevronLeft", "M15 18l-6-6 6-6", width = 2.2f)

    val ChevronRight = stroke("ChevronRight", "M9 6l6 6-6 6", width = 2.2f)

    val Plus = stroke("Plus", "M12 5v14M5 12h14")

    val Close = stroke("Close", "M18 6 6 18M6 6l12 12")

    val Check = stroke("Check", "M20 6 9 17l-5-5")

    val Home = stroke("Home", "M3 10l9-7 9 7v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2Z")

    val List = stroke("List", "M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01")

    val Person = stroke("Person", circle(12f, 8f, 4f), "M4 21c0-4 3.6-7 8-7s8 3 8 7")

    val PersonAdd = stroke(
        "PersonAdd",
        "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2",
        circle(9f, 7f, 4f),
        "M19 8v6M22 11h-6",
    )

    val Users = stroke(
        "Users",
        "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2",
        circle(9f, 7f, 4f),
        "M22 21v-2a4 4 0 0 0-3-3.9",
        "M16 3.1a4 4 0 0 1 0 7.8",
    )

    val Bell = stroke(
        "Bell",
        "M18 8a6 6 0 1 0-12 0c0 7-3 9-3 9h18s-3-2-3-9",
        "M13.7 21a2 2 0 0 1-3.4 0",
    )

    val Eye = stroke("Eye", "M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z", circle(12f, 12f, 3f))

    val Clock = stroke("Clock", circle(12f, 12f, 9f), "M12 8v4l3 2")

    val Info = stroke("Info", circle(12f, 12f, 9f), "M12 8v4M12 16h.01")

    val Calendar = stroke("Calendar", rect(3f, 4f, 18f, 18f, 3f), "M3 9h18M8 2v4M16 2v4")

    val Lock = stroke("Lock", rect(5f, 11f, 14f, 10f, 2f), "M8 11V8a4 4 0 0 1 8 0v3")

    val Video = stroke("Video", "M23 7l-7 5 7 5V7Z", rect(1f, 5f, 15f, 14f, 2f))

    val Search = stroke("Search", circle(11f, 11f, 7f), "m21 21-4.3-4.3")

    val Link = stroke(
        "Link",
        "M10 13a5 5 0 0 0 7 0l3-3a5 5 0 0 0-7-7l-1 1",
        "M14 11a5 5 0 0 0-7 0l-3 3a5 5 0 0 0 7 7l1-1",
    )

    val Share = stroke(
        "Share",
        circle(18f, 5f, 3f),
        circle(6f, 12f, 3f),
        circle(18f, 19f, 3f),
        "M8.6 13.5l6.8 4M15.4 6.5l-6.8 4",
    )

    val AlertTriangle = stroke(
        "AlertTriangle",
        "M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0Z",
        "M12 9v4M12 17h.01",
    )

    val MoreVertical = filled(
        "MoreVertical",
        circle(12f, 5f, 1.4f) to Color.Black,
        circle(12f, 12f, 1.4f) to Color.Black,
        circle(12f, 19f, 1.4f) to Color.Black,
    )

    val Stop = filled("Stop", rect(6f, 6f, 12f, 12f, 2f) to Color.Black)

    /** Multi-colour — render with `tint = Color.Unspecified`. */
    val Google = filled(
        "Google",
        "M22 12.2c0-.7-.1-1.4-.2-2H12v3.9h5.6a4.8 4.8 0 0 1-2.1 3.1v2.6h3.4c2-1.8 3.1-4.5 3.1-7.6Z"
            to Color(0xFF4285F4),
        "M12 23c2.8 0 5.2-.9 6.9-2.5l-3.4-2.6c-.9.6-2.1 1-3.5 1-2.7 0-5-1.8-5.8-4.3H2.7v2.7A10.5 10.5 0 0 0 12 23Z"
            to Color(0xFF34A853),
        "M6.2 14.6a6.3 6.3 0 0 1 0-4V7.9H2.7a10.5 10.5 0 0 0 0 9.4l3.5-2.7Z"
            to Color(0xFFFBBC05),
        "M12 5.4c1.5 0 2.9.5 4 1.5l3-3A10.5 10.5 0 0 0 2.7 7.9l3.5 2.7C7 8.1 9.3 5.4 12 5.4Z"
            to Color(0xFFEA4335),
    )
}

// ---------------------------------------------------------------- builders

private fun builder(name: String) = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
)

private fun stroke(name: String, vararg pathData: String, width: Float = 2f): ImageVector =
    builder(name).apply {
        pathData.forEach {
            addPath(
                pathData = PathParser().parsePathString(it).toNodes(),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = width,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

private fun filled(name: String, vararg parts: Pair<String, Color>): ImageVector =
    builder(name).apply {
        parts.forEach { (data, color) ->
            addPath(
                pathData = PathParser().parsePathString(data).toNodes(),
                fill = SolidColor(color),
            )
        }
    }.build()

/** SVG `<circle>` expressed as path data, so one parser handles every shape. */
private fun circle(cx: Float, cy: Float, r: Float): String =
    "M${cx - r},$cy a$r,$r 0 1,0 ${r * 2},0 a$r,$r 0 1,0 ${-r * 2},0"

/** SVG `<rect rx>` expressed as path data. */
private fun rect(x: Float, y: Float, w: Float, h: Float, r: Float): String =
    "M${x + r},$y " +
        "h${w - 2 * r} a$r,$r 0 0,1 $r,$r " +
        "v${h - 2 * r} a$r,$r 0 0,1 ${-r},$r " +
        "h${-(w - 2 * r)} a$r,$r 0 0,1 ${-r},${-r} " +
        "v${-(h - 2 * r)} a$r,$r 0 0,1 $r,${-r} z"
