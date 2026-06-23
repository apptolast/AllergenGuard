package org.apptolast.menuadmin.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import menuadmin.adminapp.generated.resources.Res
import menuadmin.adminapp.generated.resources.logo_android
import org.apptolast.menuadmin.AppInfo
import org.apptolast.menuadmin.navigation.BackupRestoreRoute
import org.apptolast.menuadmin.navigation.DashboardRoute
import org.apptolast.menuadmin.navigation.IngredientsRoute
import org.apptolast.menuadmin.navigation.PlatformAccountDetailRoute
import org.apptolast.menuadmin.navigation.PlatformAccountsRoute
import org.apptolast.menuadmin.navigation.ProfileRoute
import org.apptolast.menuadmin.navigation.RestaurantDetailRoute
import org.apptolast.menuadmin.navigation.RestaurantsRoute
import org.apptolast.menuadmin.navigation.SettingsRoute
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.apptolast.menuadmin.presentation.theme.SidebarDark
import org.apptolast.menuadmin.presentation.theme.SidebarDarkSurface
import org.jetbrains.compose.resources.painterResource

@Composable
fun Sidebar(
    currentDestination: NavDestination?,
    onNavigate: (Any) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    // Admin-only tools (Backup/Restore) are hidden for RESTAURANT_MANAGER. Defaults to true so previews
    // and the admin flow show everything.
    isAccountAdmin: Boolean = true,
    // The Platform section is shown only to platform owners (SUPER_ADMIN). Defaults to false (hidden).
    isSuperAdmin: Boolean = false,
) {
    // True if the given typed route is anywhere in the current destination's hierarchy.
    fun isRoute(predicate: (NavDestination) -> Boolean): Boolean = currentDestination?.hierarchy?.any(predicate) == true

    Column(
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(SidebarDark)
            .padding(vertical = 24.dp, horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        // Logo area
        Box(
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier.padding(horizontal = 12.dp),
        ) {
            Icon(
                painter = painterResource(Res.drawable.logo_android),
                contentDescription = "Allergen Guard",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(160.dp).offset(x = (-40).dp),
            )
            Text(
                text = "Allergen\nGuard",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(x = (90).dp),
            )
//            Box(
//                modifier = Modifier
//                    .clip(RoundedCornerShape(4.dp))
//                    .background(Blue500)
//                    .padding(horizontal = 6.dp, vertical = 2.dp),
//            ) {
//                Text(
//                    text = "PRO",
//                    color = Color.White,
//                    fontSize = 10.sp,
//                    fontWeight = FontWeight.Bold,
//                )
//            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        HorizontalDivider(color = SidebarDarkSurface)

        Spacer(modifier = Modifier.height(20.dp))

        // Section: MENU PRINCIPAL
        SectionHeader(title = "MENU PRINCIPAL")

        Spacer(modifier = Modifier.height(8.dp))

        NavItem(
            icon = Icons.Outlined.Dashboard,
            label = "Dashboard",
            isSelected = isRoute { it.hasRoute<DashboardRoute>() },
            onClick = { onNavigate(DashboardRoute) },
        )
        NavItem(
            icon = Icons.Outlined.Inventory2,
            label = "Ingredientes",
            isSelected = isRoute { it.hasRoute<IngredientsRoute>() },
            onClick = { onNavigate(IngredientsRoute) },
        )
        NavItem(
            icon = Icons.Outlined.Storefront,
            label = "Restaurantes",
            isSelected = isRoute { it.hasRoute<RestaurantsRoute>() || it.hasRoute<RestaurantDetailRoute>() },
            onClick = { onNavigate(RestaurantsRoute) },
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Section: PLATAFORMA (platform owners only)
        if (isSuperAdmin) {
            SectionHeader(title = "PLATAFORMA")
            Spacer(modifier = Modifier.height(8.dp))
            NavItem(
                icon = Icons.Outlined.Business,
                label = "Cuentas",
                isSelected = isRoute {
                    it.hasRoute<PlatformAccountsRoute>() || it.hasRoute<PlatformAccountDetailRoute>()
                },
                onClick = { onNavigate(PlatformAccountsRoute) },
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Section: HERRAMIENTAS
        SectionHeader(title = "HERRAMIENTAS")

        Spacer(modifier = Modifier.height(8.dp))

        // Backup/Restore writes the whole catalog (ingredients), an ACCOUNT_ADMIN-only operation.
        if (isAccountAdmin) {
            NavItem(
                icon = Icons.Outlined.Storage,
                label = "Backup / Restaurar",
                isSelected = isRoute { it.hasRoute<BackupRestoreRoute>() },
                onClick = { onNavigate(BackupRestoreRoute) },
            )
        }
        NavItem(
            icon = Icons.Outlined.Settings,
            label = "Configuracion",
            isSelected = isRoute { it.hasRoute<SettingsRoute>() },
            onClick = { onNavigate(SettingsRoute) },
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Section: CUENTA
        SectionHeader(title = "CUENTA")

        Spacer(modifier = Modifier.height(8.dp))

        NavItem(
            icon = Icons.Outlined.Person,
            label = "Mi Perfil",
            isSelected = isRoute { it.hasRoute<ProfileRoute>() },
            onClick = { onNavigate(ProfileRoute) },
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Versión ${AppInfo.VERSION}",
            color = MenuAdminTheme.colors.textMuted,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )

        HorizontalDivider(color = SidebarDarkSurface)

        Spacer(modifier = Modifier.height(8.dp))

        NavItem(
            icon = Icons.AutoMirrored.Outlined.Logout,
            label = "Cerrar sesion",
            isSelected = false,
            onClick = onLogout,
            destructive = true,
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        color = MenuAdminTheme.colors.textMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
    )
}

@Preview
@Composable
private fun PreviewSidebar() {
    MenuAdminTheme {
        Sidebar(
            currentDestination = null,
            onNavigate = {},
            onLogout = {},
        )
    }
}
