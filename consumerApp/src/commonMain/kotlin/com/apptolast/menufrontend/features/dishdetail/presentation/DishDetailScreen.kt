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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apptolast.menufrontend.core.theme.AllergenActiveBg
import com.apptolast.menufrontend.core.theme.AllergenActiveText
import com.apptolast.menufrontend.core.theme.DangerRedLight
import com.apptolast.menufrontend.core.theme.SafeGreen
import com.apptolast.menufrontend.core.theme.SafeGreenBorder
import com.apptolast.menufrontend.core.theme.SafeGreenLight
import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.features.components.icon
import com.apptolast.menufrontend.features.dishdetail.data.DishDetailState
import com.apptolast.menufrontend.resources.Res
import com.apptolast.menufrontend.resources.allergen_celery
import com.apptolast.menufrontend.resources.allergen_crustaceans
import com.apptolast.menufrontend.resources.allergen_dairy
import com.apptolast.menufrontend.resources.allergen_eggs
import com.apptolast.menufrontend.resources.allergen_fish
import com.apptolast.menufrontend.resources.allergen_gluten
import com.apptolast.menufrontend.resources.allergen_lupin
import com.apptolast.menufrontend.resources.allergen_mollusks
import com.apptolast.menufrontend.resources.allergen_mustard
import com.apptolast.menufrontend.resources.allergen_peanuts
import com.apptolast.menufrontend.resources.allergen_sesame
import com.apptolast.menufrontend.resources.allergen_soy
import com.apptolast.menufrontend.resources.allergen_sulfites
import com.apptolast.menufrontend.resources.allergen_tree_nuts
import com.apptolast.menufrontend.resources.back
import com.apptolast.menufrontend.resources.dish_allergen_warning
import com.apptolast.menufrontend.resources.dish_allergens
import com.apptolast.menufrontend.resources.dish_detail_title
import com.apptolast.menufrontend.resources.dish_ingredients
import com.apptolast.menufrontend.resources.dish_safe_message
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
    val allergenLabels = allergenLabelMap()

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
                // Dish image placeholder
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

                // Dish name
                Text(
                    text = dish.name.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                // Description
                Text(
                    text = dish.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Price
                val priceText = "${dish.price.toString().let {
                    val parts = it.split(".")
                    if (parts.size == 2) "${parts[0]},${parts[1].padEnd(2, '0').take(2)}" else it
                }} \u20AC"
                Text(
                    text = priceText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SafeGreen,
                )

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
                        colors = CardDefaults.cardColors(containerColor = DangerRedLight),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = AllergenActiveText,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = stringResource(
                                    Res.string.dish_allergen_warning,
                                    state.dangerousAllergens.size,
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = AllergenActiveText,
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SafeGreenLight),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(Res.string.dish_safe_message),
                                style = MaterialTheme.typography.bodySmall,
                                color = SafeGreen,
                            )
                        }
                    }
                }

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
    val bgColor = if (isDangerous) AllergenActiveBg else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isDangerous) AllergenActiveText else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isDangerous) AllergenActiveText else MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = Modifier
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = allergen.icon(),
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

@Composable
private fun allergenLabelMap(): Map<Allergen, String> = mapOf(
    Allergen.GLUTEN to stringResource(Res.string.allergen_gluten),
    Allergen.FISH to stringResource(Res.string.allergen_fish),
    Allergen.PEANUTS to stringResource(Res.string.allergen_peanuts),
    Allergen.DAIRY to stringResource(Res.string.allergen_dairy),
    Allergen.EGGS to stringResource(Res.string.allergen_eggs),
    Allergen.SOY to stringResource(Res.string.allergen_soy),
    Allergen.SULFITES to stringResource(Res.string.allergen_sulfites),
    Allergen.MOLLUSKS to stringResource(Res.string.allergen_mollusks),
    Allergen.CRUSTACEANS to stringResource(Res.string.allergen_crustaceans),
    Allergen.TREE_NUTS to stringResource(Res.string.allergen_tree_nuts),
    Allergen.CELERY to stringResource(Res.string.allergen_celery),
    Allergen.MUSTARD to stringResource(Res.string.allergen_mustard),
    Allergen.SESAME to stringResource(Res.string.allergen_sesame),
    Allergen.LUPIN to stringResource(Res.string.allergen_lupin),
)
