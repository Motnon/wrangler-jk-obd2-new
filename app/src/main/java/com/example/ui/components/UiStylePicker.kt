package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.obd.AppUiStyle
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalRacePalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UiStylePickerSheet(
    selectedStyle: AppUiStyle,
    onSelectStyle: (AppUiStyle) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.DashboardCustomize,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = strings.uiStyleSheetTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.uiStyleSheetSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AppUiStyle.entries.forEach { style ->
                UiStyleOptionRow(
                    style = style,
                    isSelected = selectedStyle == style,
                    onClick = {
                        onSelectStyle(style)
                        onDismiss()
                    },
                    modifier = Modifier.testTag("ui_style_sheet_option_${style.id}")
                )
            }
        }
    }
}

@Composable
fun UiStyleSelectorCard(
    selectedStyle: AppUiStyle,
    onSelectStyle: (AppUiStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.DashboardCustomize,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = strings.uiStyleCardHeader,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.uiStyleCardSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AppUiStyle.entries.forEach { style ->
                UiStyleOptionRow(
                    style = style,
                    isSelected = selectedStyle == style,
                    onClick = { onSelectStyle(style) },
                    modifier = Modifier.testTag("ui_style_card_${style.id}")
                )
            }
        }
    }
}

@Composable
private fun UiStyleOptionRow(
    style: AppUiStyle,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val palette = LocalRacePalette.current
    val accent = when (style) {
        AppUiStyle.TACTICAL_MOTORSPORT -> MaterialTheme.colorScheme.primary
        AppUiStyle.AVIONICS_HUD -> palette.zone1
        AppUiStyle.AUTOMOTIVE_INFOTAINMENT -> palette.zone2
    }
    val rowShape = when (style) {
        AppUiStyle.AVIONICS_HUD -> CutCornerShape(12.dp)
        AppUiStyle.AUTOMOTIVE_INFOTAINMENT -> RoundedCornerShape(22.dp)
        AppUiStyle.TACTICAL_MOTORSPORT -> RoundedCornerShape(14.dp)
    }

    Surface(
        shape = rowShape,
        color = if (isSelected) accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) accent else MaterialTheme.colorScheme.outline
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(rowShape)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UiStyleWireframePreview(
                style = style,
                accent = accent,
                surfaceColor = palette.podSurface,
                inkColor = palette.textSecondary,
                modifier = Modifier.size(width = 76.dp, height = 56.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = strings.uiStyleBadge(style),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        color = accent
                    )
                    if (isSelected) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = accent
                        ) {
                            Text(
                                text = strings.uiStyleActiveBadge,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = strings.uiStyleTitle(style),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = strings.uiStyleSubtitle(style),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = strings.uiStyleActiveBadge,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun UiStyleWireframePreview(
    style: AppUiStyle,
    accent: Color,
    surfaceColor: Color,
    inkColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(
                when (style) {
                    AppUiStyle.AVIONICS_HUD -> CutCornerShape(8.dp)
                    AppUiStyle.AUTOMOTIVE_INFOTAINMENT -> RoundedCornerShape(14.dp)
                    AppUiStyle.TACTICAL_MOTORSPORT -> RoundedCornerShape(8.dp)
                }
            )
            .background(surfaceColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            when (style) {
                AppUiStyle.TACTICAL_MOTORSPORT -> {
                    // Top bar
                    drawRect(
                        color = inkColor.copy(alpha = 0.25f),
                        topLeft = Offset(0f, 0f),
                        size = Size(w, h * 0.18f)
                    )
                    // 2 circular dials + bottom bar
                    drawCircle(
                        color = accent,
                        radius = h * 0.2f,
                        center = Offset(w * 0.28f, h * 0.48f),
                        style = Stroke(width = 2.5f)
                    )
                    drawCircle(
                        color = inkColor,
                        radius = h * 0.2f,
                        center = Offset(w * 0.72f, h * 0.48f),
                        style = Stroke(width = 2f)
                    )
                    drawRect(
                        color = accent.copy(alpha = 0.4f),
                        topLeft = Offset(0f, h * 0.8f),
                        size = Size(w, h * 0.2f)
                    )
                }

                AppUiStyle.AVIONICS_HUD -> {
                    // Reticle crosshairs & pitch horizon
                    val horizonY = h * 0.44f
                    drawLine(
                        color = accent,
                        start = Offset(w * 0.14f, horizonY),
                        end = Offset(w * 0.38f, horizonY),
                        strokeWidth = 2.2f
                    )
                    drawLine(
                        color = accent,
                        start = Offset(w * 0.62f, horizonY),
                        end = Offset(w * 0.86f, horizonY),
                        strokeWidth = 2.2f
                    )
                    drawCircle(
                        color = accent.copy(alpha = 0.75f),
                        radius = h * 0.11f,
                        center = Offset(w * 0.5f, horizonY),
                        style = Stroke(width = 2f)
                    )
                    // Corner HUD brackets
                    val b = Path().apply {
                        moveTo(w * 0.08f, h * 0.25f)
                        lineTo(w * 0.08f, h * 0.12f)
                        lineTo(w * 0.22f, h * 0.12f)
                        moveTo(w * 0.92f, h * 0.25f)
                        lineTo(w * 0.92f, h * 0.12f)
                        lineTo(w * 0.78f, h * 0.12f)
                    }
                    drawPath(b, color = inkColor, style = Stroke(width = 1.8f))
                    // Floating pill dock
                    drawRoundRect(
                        color = accent.copy(alpha = 0.55f),
                        topLeft = Offset(w * 0.15f, h * 0.76f),
                        size = Size(w * 0.7f, h * 0.15f),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                }

                AppUiStyle.AUTOMOTIVE_INFOTAINMENT -> {
                    // Left persistent rail dock
                    drawRoundRect(
                        color = accent.copy(alpha = 0.4f),
                        topLeft = Offset(w * 0.06f, h * 0.1f),
                        size = Size(w * 0.18f, h * 0.8f),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                    // Hero split bento tile + right 2 stacked tiles
                    drawRoundRect(
                        color = accent.copy(alpha = 0.85f),
                        topLeft = Offset(w * 0.29f, h * 0.12f),
                        size = Size(w * 0.36f, h * 0.76f),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                    drawRoundRect(
                        color = inkColor.copy(alpha = 0.45f),
                        topLeft = Offset(w * 0.69f, h * 0.12f),
                        size = Size(w * 0.25f, h * 0.35f),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                    drawRoundRect(
                        color = inkColor.copy(alpha = 0.35f),
                        topLeft = Offset(w * 0.69f, h * 0.53f),
                        size = Size(w * 0.25f, h * 0.35f),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                }
            }
        }
    }
}
