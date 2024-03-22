package framework.presentation.compose


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonElevation
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ButtonOutlineFind(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = ButtonDefaults.elevation(defaultElevation = 0.dp),
    shape: Shape = MaterialTheme.shapes.small
        .copy( all =  CornerSize(20.dp)),
    contentColor: Color = Color.Black,
    backgroundColor: Color = Color(0x0800FF00),
    disabledContentColor: Color = Color.Gray,
    borderSize: Dp = 2.dp,
    borderColor: Color = Color(0xFF22AA12),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        elevation = elevation,
        shape = shape,
        border = if (borderSize > 0.dp) BorderStroke(
            borderSize,
            borderColor
        ) else null,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = contentColor,
            backgroundColor = backgroundColor,
            disabledContentColor = disabledContentColor
        ),
        contentPadding= contentPadding,
        content = content
    )
}