package io.github.omeryol.akisgesture.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import io.github.omeryol.akisgesture.R

enum class GestureVisualDirection(@DrawableRes val drawableRes: Int) {
    LEFT_EDGE_UP(R.drawable.gesture_phosphor_arrow_elbow_right_up),
    LEFT_EDGE_DOWN(R.drawable.gesture_phosphor_arrow_elbow_right_down),
    RIGHT_EDGE_UP(R.drawable.gesture_phosphor_arrow_elbow_left_up),
    RIGHT_EDGE_DOWN(R.drawable.gesture_phosphor_arrow_elbow_left_down),
    BOTTOM_LEFT(R.drawable.gesture_phosphor_arrow_elbow_up_left),
    BOTTOM_RIGHT(R.drawable.gesture_phosphor_arrow_elbow_up_right),
}

/** Pack-independent Phosphor gesture geometry. Action icon families never alter this path. */
@Composable
fun GestureVisualIcon(
    direction: GestureVisualDirection,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
) {
    val resolvedColor = if (color == Color.Unspecified) {
        androidx.compose.material3.LocalContentColor.current
    } else {
        color
    }
    Image(
        painter = painterResource(direction.drawableRes),
        contentDescription = null,
        modifier = modifier,
        colorFilter = ColorFilter.tint(resolvedColor),
    )
}
