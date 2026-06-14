package org.apptolast.menuadmin.presentation.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import menuadmin.adminapp.generated.resources.Res
import menuadmin.adminapp.generated.resources.lucide
import org.jetbrains.compose.resources.Font

@Composable
fun LucideIcon(
    codepoint: Char,
    modifier: Modifier = Modifier,
    size: TextUnit = 20.sp,
    color: Color = Color.Unspecified,
) {
    val lucideFont = FontFamily(Font(Res.font.lucide))
    Text(
        text = codepoint.toString(),
        fontFamily = lucideFont,
        fontSize = size,
        color = color,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}
