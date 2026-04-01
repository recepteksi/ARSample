package com.trendhive.arsample.presentation.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

actual val AddIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Add",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(19f, 13f)
            horizontalLineTo(13f)
            verticalLineTo(19f)
            horizontalLineTo(11f)
            verticalLineTo(13f)
            horizontalLineTo(5f)
            verticalLineTo(11f)
            horizontalLineTo(11f)
            verticalLineTo(5f)
            horizontalLineTo(13f)
            verticalLineTo(11f)
            horizontalLineTo(19f)
            close()
        }
    }.build()

actual val DeleteIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Delete",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(6f, 19f)
            curveTo(6f, 20.1f, 6.9f, 21f, 8f, 21f)
            horizontalLineTo(16f)
            curveTo(17.1f, 21f, 18f, 20.1f, 18f, 19f)
            verticalLineTo(7f)
            horizontalLineTo(6f)
            verticalLineTo(19f)
            close()
            moveTo(19f, 4f)
            horizontalLineTo(15.5f)
            lineTo(14.5f, 3f)
            horizontalLineTo(9.5f)
            lineTo(8.5f, 4f)
            horizontalLineTo(5f)
            verticalLineTo(6f)
            horizontalLineTo(19f)
            verticalLineTo(4f)
            close()
        }
    }.build()

actual val ArrowBackIcon: ImageVector
    get() = ImageVector.Builder(
        name = "ArrowBack",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(20f, 11f)
            horizontalLineTo(7.83f)
            lineTo(13.42f, 5.41f)
            lineTo(12f, 4f)
            lineTo(4f, 12f)
            lineTo(12f, 20f)
            lineTo(13.41f, 18.59f)
            lineTo(7.83f, 13f)
            horizontalLineTo(20f)
            verticalLineTo(11f)
            close()
        }
    }.build()

actual val MenuIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Menu",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(3f, 18f)
            horizontalLineTo(21f)
            verticalLineTo(16f)
            horizontalLineTo(3f)
            verticalLineTo(18f)
            close()
            moveTo(3f, 13f)
            horizontalLineTo(21f)
            verticalLineTo(11f)
            horizontalLineTo(3f)
            verticalLineTo(13f)
            close()
            moveTo(3f, 6f)
            verticalLineTo(8f)
            horizontalLineTo(21f)
            verticalLineTo(6f)
            horizontalLineTo(3f)
            close()
        }
    }.build()

actual val PlayArrowIcon: ImageVector
    get() = ImageVector.Builder(
        name = "PlayArrow",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(8f, 5f)
            verticalLineTo(19f)
            lineTo(19f, 12f)
            close()
        }
    }.build()

actual val ListIcon: ImageVector
    get() = ImageVector.Builder(
        name = "List",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(3f, 13f)
            horizontalLineTo(5f)
            verticalLineTo(11f)
            horizontalLineTo(3f)
            verticalLineTo(13f)
            close()
            moveTo(3f, 17f)
            horizontalLineTo(5f)
            verticalLineTo(15f)
            horizontalLineTo(3f)
            verticalLineTo(17f)
            close()
            moveTo(3f, 9f)
            horizontalLineTo(5f)
            verticalLineTo(7f)
            horizontalLineTo(3f)
            verticalLineTo(9f)
            close()
            moveTo(7f, 17f)
            horizontalLineTo(21f)
            verticalLineTo(15f)
            horizontalLineTo(7f)
            verticalLineTo(17f)
            close()
            moveTo(7f, 13f)
            horizontalLineTo(21f)
            verticalLineTo(11f)
            horizontalLineTo(7f)
            verticalLineTo(13f)
            close()
            moveTo(7f, 9f)
            horizontalLineTo(21f)
            verticalLineTo(7f)
            horizontalLineTo(7f)
            verticalLineTo(9f)
            close()
        }
    }.build()
