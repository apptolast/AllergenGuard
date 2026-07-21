package com.apptolast.menufrontend.features.dishdetail.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.apptolast.menufrontend.core.theme.AllergenGuardTheme
import com.apptolast.menufrontend.core.theme.extendedColors
import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.domain.model.Dish
import com.apptolast.menufrontend.features.components.allergenLabels
import com.apptolast.menufrontend.features.components.iconResource
import com.apptolast.menufrontend.features.dishdetail.data.DishDetailState
import com.apptolast.menufrontend.resources.Res
import com.apptolast.menufrontend.resources.back
import com.apptolast.menufrontend.resources.dish_allergen_warning
import com.apptolast.menufrontend.resources.dish_allergens
import com.apptolast.menufrontend.resources.dish_detail_title
import com.apptolast.menufrontend.resources.dish_disclaimer
import com.apptolast.menufrontend.resources.dish_ingredients
import com.apptolast.menufrontend.resources.dish_safe_message
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DishDetailScreenRoot(
    dishId: String,
    restaurantId: String,
    onNavigateBack: () -> Unit,
    viewModel: DishDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(dishId, restaurantId) {
        viewModel.loadDish(dishId, restaurantId)
    }

    DishDetailScreen(
        state = state,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DishDetailScreen(
    state: DishDetailState,
    onNavigateBack: () -> Unit,
) {
    val allergenLabels = allergenLabels()
    val colors = MaterialTheme.extendedColors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(Res.string.dish_detail_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = state.restaurantName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            val dish = state.dish ?: return@Scaffold

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Dish image (falls back to a letter placeholder when none is set)
                val imageUrl = dish.imageUrl
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = dish.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp)),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = dish.name.take(1),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Name + description + price grouped in a card (matches the mobile design)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = dish.name.uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (dish.description.isNotBlank()) {
                            Text(
                                text = dish.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        val priceText = "${
                            dish.price.toString().let {
                                val parts = it.split(".")
                                if (parts.size == 2) "${parts[0]},${parts[1].padEnd(2, '0').take(2)}" else it
                            }
                        } \u20AC"
                        Text(
                            text = priceText,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                // Ingredients
                Text(
                    text = stringResource(Res.string.dish_ingredients),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    dish.ingredients.forEach { ingredient ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        ) {
                            Text(
                                text = ingredient,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Allergens
                if (dish.allergens.isNotEmpty()) {
                    Text(
                        text = stringResource(Res.string.dish_allergens),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        dish.allergens.forEach { allergen ->
                            val isDangerous = allergen in state.userAllergens
                            AllergenDetailBadge(
                                allergen = allergen,
                                label = allergenLabels[allergen] ?: allergen.name,
                                isDangerous = isDangerous,
                            )
                        }
                    }
                }

                // Warning or safe banner
                if (state.containsUserAllergens) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.dangerContainer),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = colors.onDangerContainer,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = stringResource(
                                    Res.string.dish_allergen_warning,
                                    state.dangerousAllergens.size,
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onDangerContainer,
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.safeContainer),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(Res.string.dish_safe_message),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSafeContainer,
                            )
                        }
                    }
                }

                // Always-visible, low-emphasis reminder that the allergen data comes from the
                // restaurant and should be confirmed before ordering.
                Text(
                    text = stringResource(Res.string.dish_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AllergenDetailBadge(
    allergen: Allergen,
    label: String,
    isDangerous: Boolean,
) {
    val colors = MaterialTheme.extendedColors
    val bgColor = if (isDangerous) colors.dangerContainer else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isDangerous) colors.onDangerContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isDangerous) colors.onDangerContainer else MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = Modifier
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            painter = painterResource(allergen.iconResource()),
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = contentColor,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Preview
@Composable
private fun PreviewDishDetailScreen() {
    AllergenGuardTheme {
        DishDetailScreen(
            state = DishDetailState(
                dish = Dish(
                    id = "1",
                    restaurantId = "r1",
                    name = "Lasaña boloñesa",
                    description = "Pasta al horno con bechamel y carne",
                    price = 12.5,
                    ingredients = listOf("Pasta", "Carne", "Leche", "Tomate"),
                    allergens = setOf(Allergen.GLUTEN, Allergen.DAIRY),
                ),
                restaurantName = "Hotel Valsequillo",
                userAllergens = setOf(Allergen.GLUTEN),
            ),
            onNavigateBack = {},
        )
    }
}
