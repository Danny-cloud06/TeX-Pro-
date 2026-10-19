package com.example.ui.screens.studio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.VirtualStudioPreset

@Composable
fun VirtualStudioBackdrop(
    preset: VirtualStudioPreset,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(preset.primaryColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            when (preset) {
                VirtualStudioPreset.NEWSROOM -> {
                    // Deep broadcast background with radial studio spotlight
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1E3A8A).copy(alpha = 0.7f),
                                Color(0xFF0F172A),
                                Color(0xFF030712)
                            ),
                            center = Offset(w * 0.5f, h * 0.4f),
                            radius = w * 0.85f
                        )
                    )

                    // Architectural digital display panels (horizontal broadcast grid)
                    val panelHeight = h * 0.05f
                    for (i in 0..7) {
                        val y = h * 0.15f + i * (panelHeight * 1.8f)
                        val alpha = if (i % 2 == 0) 0.25f else 0.12f
                        drawRoundRect(
                            color = Color(0xFF38BDF8).copy(alpha = alpha),
                            topLeft = Offset(w * 0.08f, y),
                            size = Size(w * 0.84f, panelHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                        )
                    }

                    // Bright studio broadcast accent line
                    drawLine(
                        color = Color(0xFF00E676),
                        start = Offset(w * 0.05f, h * 0.78f),
                        end = Offset(w * 0.95f, h * 0.78f),
                        strokeWidth = 3f
                    )

                    // Lower news desk framing gradient
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xFF0A0F1D).copy(alpha = 0.95f)),
                            startY = h * 0.72f,
                            endY = h
                        ),
                        topLeft = Offset(0f, h * 0.72f),
                        size = Size(w, h * 0.28f)
                    )
                }

                VirtualStudioPreset.EXECUTIVE_LIBRARY -> {
                    // Warm mahogany background with soft lamp vignette
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF451A03).copy(alpha = 0.85f),
                                Color(0xFF291206),
                                Color(0xFF130903)
                            ),
                            center = Offset(w * 0.75f, h * 0.35f),
                            radius = w * 0.75f
                        )
                    )

                    // Bookshelf horizontal shelves & leather book spines
                    val shelfCount = 5
                    val shelfSpacing = h / (shelfCount + 1)
                    for (s in 1..shelfCount) {
                        val shelfY = s * shelfSpacing
                        // Shelf plank
                        drawRect(
                            color = Color(0xFF1E0E04),
                            topLeft = Offset(0f, shelfY),
                            size = Size(w, 14f)
                        )
                        drawLine(
                            color = Color(0xFF78350F).copy(alpha = 0.5f),
                            start = Offset(0f, shelfY),
                            end = Offset(w, shelfY),
                            strokeWidth = 2f
                        )

                        // Books on shelf
                        var bookX = w * 0.05f
                        val bookColors = listOf(
                            Color(0xFF7F1D1D), // Deep Crimson
                            Color(0xFF14532D), // Forest Emerald
                            Color(0xFF1E3A8A), // Midnight Navy
                            Color(0xFF78350F), // Rich Leather
                            Color(0xFF312E81)  // Royal Indigo
                        )
                        var colorIdx = 0
                        while (bookX < w * 0.92f) {
                            val bookW = 18f + (bookX % 22f)
                            val bookH = shelfSpacing * 0.65f + (bookX % 16f)
                            drawRect(
                                color = bookColors[colorIdx % bookColors.size].copy(alpha = 0.75f),
                                topLeft = Offset(bookX, shelfY - bookH),
                                size = Size(bookW, bookH)
                            )
                            bookX += bookW + 3f
                            colorIdx++
                        }
                    }

                    // Warm amber lamp glow overlay
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFF59E0B).copy(alpha = 0.35f), Color.Transparent),
                            center = Offset(w * 0.8f, h * 0.28f),
                            radius = w * 0.45f
                        ),
                        radius = w * 0.45f,
                        center = Offset(w * 0.8f, h * 0.28f)
                    )
                }

                VirtualStudioPreset.CREATOR_LOFT -> {
                    // Modern urban dark loft brickwork
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF27272A), Color(0xFF18181B), Color(0xFF09090B))
                        )
                    )

                    // Exposed brick mortar grid lines
                    val rowHeight = 28f
                    val brickWidth = 72f
                    var row = 0
                    var curY = 0f
                    while (curY < h) {
                        val offset = if (row % 2 == 0) 0f else brickWidth / 2f
                        var curX = -offset
                        while (curX < w + brickWidth) {
                            drawRoundRect(
                                color = Color(0xFF3F3F46).copy(alpha = 0.25f),
                                topLeft = Offset(curX + 2f, curY + 2f),
                                size = Size(brickWidth - 4f, rowHeight - 4f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                            )
                            curX += brickWidth
                        }
                        curY += rowHeight
                        row++
                    }

                    // Top warm hanging pendant studio light glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF10B981).copy(alpha = 0.3f), Color.Transparent),
                            center = Offset(w * 0.25f, h * 0.25f),
                            radius = w * 0.5f
                        ),
                        radius = w * 0.5f,
                        center = Offset(w * 0.25f, h * 0.25f)
                    )

                    // Ambient floor gradient
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xFF000000).copy(alpha = 0.85f)),
                            startY = h * 0.7f,
                            endY = h
                        ),
                        topLeft = Offset(0f, h * 0.7f),
                        size = Size(w, h * 0.3f)
                    )
                }

                VirtualStudioPreset.CYBERPUNK_STUDIO -> {
                    // Dark futuristic tech studio
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF3B0764).copy(alpha = 0.6f), Color(0xFF09090B)),
                            center = Offset(w * 0.5f, h * 0.5f),
                            radius = w * 0.7f
                        )
                    )

                    // Vertical dual neon light strips (Cyan on left, Neon Green on right)
                    drawLine(
                        color = Color(0xFF06B6D4),
                        start = Offset(w * 0.08f, 0f),
                        end = Offset(w * 0.08f, h),
                        strokeWidth = 6f
                    )
                    drawLine(
                        color = Color(0xFF00E676),
                        start = Offset(w * 0.92f, 0f),
                        end = Offset(w * 0.92f, h),
                        strokeWidth = 6f
                    )

                    // Soft dual neon glows
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF06B6D4).copy(alpha = 0.25f), Color.Transparent),
                            startX = 0f,
                            endX = w * 0.35f
                        ),
                        topLeft = Offset.Zero,
                        size = Size(w * 0.35f, h)
                    )
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color(0xFF00E676).copy(alpha = 0.25f)),
                            startX = w * 0.65f,
                            endX = w
                        ),
                        topLeft = Offset(w * 0.65f, 0f),
                        size = Size(w * 0.35f, h)
                    )

                    // Geometric soundproofing foam diamond patterns in center
                    val step = 60f
                    for (x in 0..(w / step).toInt()) {
                        for (y in 0..(h * 0.7f / step).toInt()) {
                            drawCircle(
                                color = Color(0xFFA855F7).copy(alpha = 0.08f),
                                center = Offset(x * step, y * step),
                                radius = 8f
                            )
                        }
                    }
                }

                VirtualStudioPreset.MINIMAL_STUDIO -> {
                    // Sleek Apple-style minimalist slate gradient
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF334155),
                                Color(0xFF1E293B),
                                Color(0xFF0F172A)
                            ),
                            center = Offset(w * 0.5f, h * 0.38f),
                            radius = w * 0.75f
                        )
                    )

                    // Center subtle halo highlight
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.15f), Color.Transparent),
                            center = Offset(w * 0.5f, h * 0.38f),
                            radius = w * 0.45f
                        ),
                        radius = w * 0.45f,
                        center = Offset(w * 0.5f, h * 0.38f)
                    )

                    // Subtle studio horizon line
                    drawLine(
                        color = Color(0xFF475569).copy(alpha = 0.35f),
                        start = Offset(0f, h * 0.75f),
                        end = Offset(w, h * 0.75f),
                        strokeWidth = 1.5f
                    )
                }
            }
        }
    }
}
