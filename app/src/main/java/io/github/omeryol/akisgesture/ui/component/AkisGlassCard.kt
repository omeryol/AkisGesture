package io.github.omeryol.akisgesture.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class AkisFlowGlyph {
    EDGE_MAP,
    MOTION,
    PRESETS,
    SUMMARY,
}

@Composable
fun AkisFlowGlyphIcon(
    glyph: AkisFlowGlyph,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 2.4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path()
        when (glyph) {
            AkisFlowGlyph.EDGE_MAP -> {
                drawRoundRect(color, topLeft = androidx.compose.ui.geometry.Offset(8f, 2f), size = androidx.compose.ui.geometry.Size(16f, 28f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f), style = stroke)
                drawLine(color, androidx.compose.ui.geometry.Offset(4f, 10f), androidx.compose.ui.geometry.Offset(8f, 10f), strokeWidth = 2.4f, cap = StrokeCap.Round)
                drawLine(color, androidx.compose.ui.geometry.Offset(24f, 20f), androidx.compose.ui.geometry.Offset(28f, 20f), strokeWidth = 2.4f, cap = StrokeCap.Round)
                drawCircle(color, radius = 1.8f, center = androidx.compose.ui.geometry.Offset(16f, 16f))
            }
            AkisFlowGlyph.MOTION -> {
                path.moveTo(4f, 12f); path.cubicTo(10f, 12f, 10f, 4f, 16f, 4f); path.cubicTo(22f, 4f, 22f, 12f, 28f, 12f)
                drawPath(path, color, style = stroke)
                path.reset(); path.moveTo(4f, 20f); path.cubicTo(10f, 20f, 10f, 28f, 16f, 28f); path.cubicTo(22f, 28f, 22f, 20f, 28f, 20f)
                drawPath(path, color, style = stroke)
                drawCircle(color, radius = 2f, center = androidx.compose.ui.geometry.Offset(4f, 12f))
                drawCircle(color, radius = 2f, center = androidx.compose.ui.geometry.Offset(28f, 20f))
            }
            AkisFlowGlyph.PRESETS -> {
                path.moveTo(5f, 20f); path.cubicTo(10f, 20f, 10f, 8f, 16f, 8f); path.cubicTo(22f, 8f, 22f, 20f, 27f, 20f)
                drawPath(path, color, style = stroke)
                drawCircle(color, radius = 2f, center = androidx.compose.ui.geometry.Offset(5f, 20f))
                drawCircle(color, radius = 2f, center = androidx.compose.ui.geometry.Offset(27f, 20f))
                drawCircle(color, radius = 2.2f, center = androidx.compose.ui.geometry.Offset(16f, 8f))
            }
            AkisFlowGlyph.SUMMARY -> {
                path.moveTo(4f, 24f); path.cubicTo(9f, 24f, 10f, 10f, 16f, 10f); path.cubicTo(22f, 10f, 22f, 18f, 28f, 5f)
                drawPath(path, color, style = stroke)
                drawCircle(color, radius = 2f, center = androidx.compose.ui.geometry.Offset(4f, 24f))
                drawCircle(color, radius = 2f, center = androidx.compose.ui.geometry.Offset(28f, 5f))
            }
        }
    }
}

@Composable
fun AkisGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color? = null,
    borderColor: Color? = null,
    accentTint: Color? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dark = isSystemInDarkTheme()
    val scheme = MaterialTheme.colorScheme
    
    val baseBg = containerColor ?: if (dark) {
        scheme.surfaceVariant.copy(alpha = 0.35f)
    } else {
        scheme.surface.copy(alpha = 0.85f)
    }
    
    // Card fill stays neutral everywhere; accentTint only colors the border to avoid a patchwork look.
    val bg = baseBg

    val border = borderColor ?: if (accentTint != null) {
        accentTint.copy(alpha = 0.40f)
    } else if (dark) {
        scheme.outlineVariant.copy(alpha = 0.25f)
    } else {
        scheme.outlineVariant.copy(alpha = 0.40f)
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.5.dp, border),
            colors = CardDefaults.cardColors(containerColor = bg),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.5.dp, border),
            colors = CardDefaults.cardColors(containerColor = bg),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                content = content
            )
        }
    }
}

@Composable
fun AkisSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    flowGlyph: AkisFlowGlyph? = null,
    action: (@Composable () -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 2.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (flowGlyph != null) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(scheme.primaryContainer.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center,
            ) {
                AkisFlowGlyphIcon(flowGlyph, modifier = Modifier.size(28.dp), color = scheme.primary)
            }
            Spacer(Modifier.width(10.dp))
        } else if (icon != null) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(scheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = scheme.onPrimaryContainer,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }
        if (action != null) {
            action()
        }
    }
}

@Composable
fun AkisFluidSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
) {
    val dark = isSystemInDarkTheme()
    val thumbOffset by animateDpAsState(targetValue = if (checked) 18.dp else 2.dp, label = "switch")
    val trackBg = if (checked) activeColor else if (dark) Color(0xFF333545) else Color(0xFFE2E4EC)
    
    Box(
        modifier = modifier
            .width(42.dp)
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(trackBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
fun AkisSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(scheme.secondaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = scheme.onSecondaryContainer,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(10.dp))
        AkisFluidSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AkisFluidSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
) {
    val dark = isSystemInDarkTheme()
    val inactiveColor = if (dark) Color(0xFF2D2F3F) else Color(0xFFE0E2EC)
    
    var internalValue by remember(valueRange) { mutableFloatStateOf(value) }

    androidx.compose.runtime.LaunchedEffect(value) {
        internalValue = value
    }

    Slider(
        value = internalValue,
        onValueChange = { newValue ->
            internalValue = newValue
        },
        onValueChangeFinished = { onValueChange(internalValue) },
        valueRange = valueRange,
        modifier = modifier.fillMaxWidth().height(18.dp),
        thumb = {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        },
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState,
                modifier = Modifier.height(2.5.dp),
                colors = SliderDefaults.colors(
                    activeTrackColor = activeColor,
                    inactiveTrackColor = inactiveColor,
                )
            )
        }
    )
}

@Composable
fun AkisSliderRow(
    title: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    steps: Int = 0,
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.primary
            )
        }
        AkisFluidSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AkisFluidRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    activeColor: Color = MaterialTheme.colorScheme.primary,
) {
    val dark = isSystemInDarkTheme()
    val inactiveColor = if (dark) Color(0xFF2D2F3F) else Color(0xFFE0E2EC)

    RangeSlider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
        modifier = modifier.fillMaxWidth().height(18.dp),
        startThumb = {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        },
        endThumb = {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        },
        track = { rangeSliderState ->
            SliderDefaults.Track(
                rangeSliderState = rangeSliderState,
                modifier = Modifier.height(2.5.dp),
                colors = SliderDefaults.colors(
                    activeTrackColor = activeColor,
                    inactiveTrackColor = inactiveColor,
                )
            )
        }
    )
}

@Composable
fun AkisRangeSliderRow(
    title: String,
    valueText: String,
    value: ClosedFloatingPointRange<Float>,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    steps: Int = 0,
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.primary
            )
        }
        AkisFluidRangeSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
