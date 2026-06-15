package org.apptolast.menuadmin.presentation.screens.menus.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.Dish
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.apptolast.menuadmin.presentation.theme.Red100
import org.apptolast.menuadmin.presentation.theme.Red500

private val allergenHeaders = mapOf(
    AllergenType.GLUTEN to "Gluten",
    AllergenType.CRUSTACEANS to "Crust.",
    AllergenType.EGGS to "Huevos",
    AllergenType.FISH to "Pescado",
    AllergenType.PEANUTS to "Cacah.",
    AllergenType.SOY to "Soja",
    AllergenType.DAIRY to "Lacteos",
    AllergenType.TREE_NUTS to "Fr.Secos",
    AllergenType.CELERY to "Apio",
    AllergenType.MUSTARD to "Mostaza",
    AllergenType.SESAME to "Sesamo",
    AllergenType.SULFITES to "Sulfitos",
    AllergenType.LUPINS to "Altram.",
    AllergenType.MOLLUSKS to "Moluscos",
)

private const val DISH_NAME_WEIGHT = 3f
private const val ALLERGEN_CELL_WEIGHT = 1f

@Composable
fun AllergenMatrixTable(
    dishes: List<Dish>,
    selectedCategory: String?,
    modifier: Modifier = Modifier,
) {
    val filteredDishes = if (selectedCategory != null) {
        dishes.filter { it.category == selectedCategory }
    } else {
        dishes
    }

    val allergens = AllergenType.entries

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant),
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(MaterialTheme.colorScheme.background),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Dish name header
            Box(
                modifier = Modifier
                    .weight(DISH_NAME_WEIGHT)
                    .fillMaxHeight()
                    .borderEnd()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = "PRODUCTO / PLATO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp,
                )
            }
            // Allergen column headers
            allergens.forEach { allergen ->
                AllergenHeaderCell(
                    text = allergenHeaders[allergen] ?: "",
                    modifier = Modifier.weight(ALLERGEN_CELL_WEIGHT),
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

        // Data rows
        filteredDishes.forEachIndexed { index, dish ->
            DishAllergenRow(
                dish = dish,
                allergens = allergens,
            )
            if (index < filteredDishes.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun RowScope.AllergenHeaderCell(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .borderEnd()
            .padding(vertical = 10.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DishAllergenRow(
    dish: Dish,
    allergens: List<AllergenType>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Dish name cell
        Box(
            modifier = Modifier
                .weight(DISH_NAME_WEIGHT)
                .fillMaxHeight()
                .borderEnd()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = dish.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        // Allergen cells
        allergens.forEach { allergen ->
            val hasAllergen = allergen in dish.allergens
            Box(
                modifier = Modifier
                    .weight(ALLERGEN_CELL_WEIGHT)
                    .fillMaxHeight()
                    .background(if (hasAllergen) Red100 else MaterialTheme.colorScheme.surface)
                    .borderEnd()
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (hasAllergen) "X" else "–",
                    fontSize = 12.sp,
                    fontWeight = if (hasAllergen) FontWeight.Bold else FontWeight.Normal,
                    color = if (hasAllergen) Red500 else MenuAdminTheme.colors.textMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun Modifier.borderEnd(): Modifier =
    this.border(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

@Composable
fun AllergenTableLegend(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = shape)
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        // Left side: LEYENDA
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "LEYENDA",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 0.5.sp,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Red100)
                        .border(
                            width = 1.dp,
                            color = Red500.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(3.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "X",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Red500,
                    )
                }
                Text(
                    text = "Contiene el alérgeno",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier.size(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "–",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MenuAdminTheme.colors.textMuted,
                    )
                }
                Text(
                    text = "No contiene",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.width(32.dp))

        // Right side: Nota Informativa
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Nota Informativa:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Esta información ha sido elaborada en base a las fichas técnicas/fotografías de " +
                    "listado de ingredientes facilitadas por nuestros clientes.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
            )
        }
    }
}

@Preview
@Composable
private fun AllergenTableLegendPreview() {
    MenuAdminTheme {
        AllergenTableLegend()
    }
}

@Preview
@Composable
private fun AllergenMatrixTablePreview() {
    MenuAdminTheme {
        AllergenMatrixTable(
            dishes = listOf(
                Dish(
                    id = "d1",
                    name = "Croquetas Ibericas del Puchero",
                    category = "Entrantes",
                    allergens = setOf(
                        AllergenType.GLUTEN,
                        AllergenType.EGGS,
                        AllergenType.DAIRY,
                        AllergenType.SULFITES,
                    ),
                ),
                Dish(
                    id = "d2",
                    name = "Patatas Rebeldes Bravas Piconera",
                    category = "Entrantes",
                    allergens = setOf(
                        AllergenType.GLUTEN,
                        AllergenType.EGGS,
                        AllergenType.SULFITES,
                        AllergenType.MOLLUSKS,
                    ),
                ),
                Dish(
                    id = "d3",
                    name = "Ensaladilla Ekaterina",
                    category = "Guarnicion",
                    allergens = setOf(AllergenType.EGGS, AllergenType.FISH),
                ),
                Dish(
                    id = "d4",
                    name = "Burrata Campana con Granizado",
                    category = "Principales",
                    allergens = setOf(AllergenType.PEANUTS, AllergenType.DAIRY),
                ),
            ),
            selectedCategory = null,
        )
    }
}
