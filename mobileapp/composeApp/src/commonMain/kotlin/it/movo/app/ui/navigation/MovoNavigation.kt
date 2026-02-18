package it.movo.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import it.movo.app.ui.auth.ForgotPasswordScreen
import it.movo.app.ui.auth.ForgotPasswordViewModel
import it.movo.app.ui.auth.LoginScreen
import it.movo.app.ui.auth.LoginViewModel
import it.movo.app.ui.auth.RegisterScreen
import it.movo.app.ui.auth.RegisterViewModel
import it.movo.app.ui.booking.BookingScreen
import it.movo.app.ui.booking.BookingViewModel
import it.movo.app.ui.history.HistoryScreen
import it.movo.app.ui.history.HistoryViewModel
import it.movo.app.ui.issue.ReportIssueScreen
import it.movo.app.ui.issue.ReportIssueViewModel
import it.movo.app.ui.map.MapScreen
import it.movo.app.ui.map.MapViewModel
import it.movo.app.ui.payment.SplitPaymentScreen
import it.movo.app.ui.payment.SplitPaymentViewModel
import it.movo.app.ui.penalty.PenaltyScreen
import it.movo.app.ui.penalty.PenaltyViewModel
import it.movo.app.ui.profile.ProfileScreen
import it.movo.app.ui.profile.ProfileViewModel
import it.movo.app.ui.promotion.PromotionScreen
import it.movo.app.ui.promotion.PromotionViewModel
import it.movo.app.ui.ride.ActiveRideScreen
import it.movo.app.ui.ride.ActiveRideViewModel
import it.movo.app.ui.ride.TripSummaryScreen
import it.movo.app.ui.ride.TripSummaryViewModel
import it.movo.app.ui.settings.NotificationSettingsScreen
import it.movo.app.ui.settings.NotificationSettingsViewModel
import it.movo.app.ui.subscription.SubscriptionScreen
import it.movo.app.ui.subscription.SubscriptionViewModel
import it.movo.app.ui.vehicle.VehicleDetailScreen
import it.movo.app.ui.vehicle.VehicleDetailViewModel
import it.movo.app.ui.wallet.WalletScreen
import it.movo.app.ui.wallet.WalletViewModel
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.viewmodel.koinViewModel

enum class BottomNavTab(val label: String) {
    EXPLORE("Explore"),
    TRIPS("Trips"),
    WALLET("Wallet"),
    ACCOUNT("Account")
}

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(LoginRoute::class, LoginRoute.serializer())
            subclass(RegisterRoute::class, RegisterRoute.serializer())
            subclass(ForgotPasswordRoute::class, ForgotPasswordRoute.serializer())
            subclass(MapRoute::class, MapRoute.serializer())
            subclass(VehicleDetailRoute::class, VehicleDetailRoute.serializer())
            subclass(BookingRoute::class, BookingRoute.serializer())
            subclass(ActiveRideRoute::class, ActiveRideRoute.serializer())
            subclass(TripSummaryRoute::class, TripSummaryRoute.serializer())
            subclass(SplitPaymentRoute::class, SplitPaymentRoute.serializer())
            subclass(HistoryRoute::class, HistoryRoute.serializer())
            subclass(ProfileRoute::class, ProfileRoute.serializer())
            subclass(WalletRoute::class, WalletRoute.serializer())
            subclass(ReportIssueRoute::class, ReportIssueRoute.serializer())
            subclass(NotificationSettingsRoute::class, NotificationSettingsRoute.serializer())
            subclass(SubscriptionRoute::class, SubscriptionRoute.serializer())
            subclass(PromotionRoute::class, PromotionRoute.serializer())
            subclass(PenaltyRoute::class, PenaltyRoute.serializer())
        }
    }
}

@Composable
fun MovoNavigation(isLoggedIn: Boolean) {
    val backStack = rememberNavBackStack(navConfig, if (isLoggedIn) MapRoute else LoginRoute)

    val currentRoute = backStack.lastOrNull()
    val showBottomBar = currentRoute != null &&
            currentRoute !is LoginRoute &&
            currentRoute !is RegisterRoute &&
            currentRoute !is ForgotPasswordRoute &&
            currentRoute !is ActiveRideRoute &&
            currentRoute !is TripSummaryRoute

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                MovoBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = { tab ->
                        val route: NavKey = when (tab) {
                            BottomNavTab.EXPLORE -> MapRoute
                            BottomNavTab.TRIPS -> HistoryRoute
                            BottomNavTab.WALLET -> WalletRoute
                            BottomNavTab.ACCOUNT -> ProfileRoute
                        }
                        // Pop back to root and navigate
                        while (backStack.size > 1) backStack.removeLastOrNull()
                        if (backStack.lastOrNull() != route) {
                            backStack.removeLastOrNull()
                            backStack.add(route)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(innerPadding),
            entryProvider = entryProvider {
                entry<LoginRoute> {
                    val viewModel = koinViewModel<LoginViewModel>()
                    LoginScreen(
                        viewModel = viewModel,
                        onNavigateToRegister = { backStack.add(RegisterRoute) },
                        onNavigateToForgotPassword = { backStack.add(ForgotPasswordRoute) },
                        onNavigateBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                        onLoginSuccess = {
                            while (backStack.size > 1) backStack.removeLastOrNull()
                            backStack.removeLastOrNull()
                            backStack.add(MapRoute)
                        }
                    )
                }
                entry<RegisterRoute> {
                    val viewModel = koinViewModel<RegisterViewModel>()
                    RegisterScreen(
                        viewModel = viewModel,
                        onNavigateBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                        onRegistrationSuccess = {
                            backStack.removeLastOrNull()
                            backStack.add(LoginRoute)
                        }
                    )
                }
                entry<ForgotPasswordRoute> {
                    val viewModel = koinViewModel<ForgotPasswordViewModel>()
                    ForgotPasswordScreen(
                        viewModel = viewModel,
                        onNavigateBack = { if (backStack.size > 1) backStack.removeLastOrNull() }
                    )
                }
                entry<MapRoute> {
                    val viewModel = koinViewModel<MapViewModel>()
                    MapScreen(
                        viewModel = viewModel,
                        onBookVehicle = { vehicleId ->
                            backStack.add(BookingRoute(vehicleId))
                        },
                        onReserveVehicle = { vehicleId ->
                            backStack.add(BookingRoute(vehicleId))
                        },
                        onNavigateToVehicleDetail = { vehicleId ->
                            backStack.add(VehicleDetailRoute(vehicleId))
                        }
                    )
                }
                entry<VehicleDetailRoute> { route ->
                    val viewModel = koinViewModel<VehicleDetailViewModel>()
                    VehicleDetailScreen(
                        viewModel = viewModel,
                        vehicleId = route.vehicleId,
                        onNavigateBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                        onBookVehicle = { vehicleId ->
                            backStack.add(BookingRoute(vehicleId))
                        }
                    )
                }
                entry<BookingRoute> { route ->
                    val viewModel = koinViewModel<BookingViewModel>()
                    LaunchedEffect(route.vehicleId) {
                        viewModel.createBooking(route.vehicleId)
                    }
                    BookingScreen(
                        viewModel = viewModel,
                        onNavigateBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                        onRentalStarted = { rentalId ->
                            backStack.removeLastOrNull()
                            backStack.add(ActiveRideRoute(rentalId))
                        },
                        onBookingCancelled = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
                entry<ActiveRideRoute> { route ->
                    val viewModel = koinViewModel<ActiveRideViewModel>()
                    LaunchedEffect(route.rentalId) {
                        viewModel.loadRental(route.rentalId)
                    }
                    ActiveRideScreen(
                        viewModel = viewModel,
                        onNavigateBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                        onRideEnded = { rentalId ->
                            backStack.removeLastOrNull()
                            backStack.add(TripSummaryRoute(rentalId))
                        },
                        onReportIssue = { backStack.add(ReportIssueRoute(rentalId = route.rentalId)) }
                    )
                }
                entry<TripSummaryRoute> { route ->
                    val viewModel = koinViewModel<TripSummaryViewModel>()
                    LaunchedEffect(route.rentalId) {
                        viewModel.loadTripSummary(route.rentalId)
                    }
                    TripSummaryScreen(
                        viewModel = viewModel,
                        onDone = {
                            // Navigate back to map
                            while (backStack.size > 1) backStack.removeLastOrNull()
                            if (backStack.lastOrNull() != MapRoute) {
                                backStack.removeLastOrNull()
                                backStack.add(MapRoute)
                            }
                        },
                        onSplitPayment = {
                            backStack.add(SplitPaymentRoute(route.rentalId))
                        }
                    )
                }
                entry<SplitPaymentRoute> { route ->
                    val viewModel = koinViewModel<SplitPaymentViewModel>()
                    SplitPaymentScreen(
                        viewModel = viewModel,
                        rentalId = route.rentalId,
                        onNavigateBack = { if (backStack.size > 1) backStack.removeLastOrNull() }
                    )
                }
                entry<HistoryRoute> {
                    val viewModel = koinViewModel<HistoryViewModel>()
                    HistoryScreen(
                        viewModel = viewModel,
                        onRentalClick = { rentalId ->
                            backStack.add(TripSummaryRoute(rentalId))
                        },
                        onReportIssue = { rentalId, vehicleId ->
                            backStack.add(ReportIssueRoute(rentalId = rentalId, vehicleId = vehicleId))
                        }
                    )
                }
                entry<ProfileRoute> {
                    val viewModel = koinViewModel<ProfileViewModel>()
                    ProfileScreen(
                        viewModel = viewModel,
                        onNavigateBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                        onLogout = {
                            while (backStack.size > 1) backStack.removeLastOrNull()
                            backStack.removeLastOrNull()
                            backStack.add(LoginRoute)
                        },
                        onNotificationSettings = {
                            backStack.add(NotificationSettingsRoute)
                        },
                        onSubscription = {
                            backStack.add(SubscriptionRoute)
                        },
                        onPromotion = {
                            backStack.add(PromotionRoute)
                        },
                        onPenalty = {
                            backStack.add(PenaltyRoute)
                        }
                    )
                }
                entry<WalletRoute> {
                    val viewModel = koinViewModel<WalletViewModel>()
                    WalletScreen(
                        viewModel = viewModel,
                        onNavigateBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                        onViewTransactions = { }
                    )
                }
                entry<ReportIssueRoute> { route ->
                    val viewModel = koinViewModel<ReportIssueViewModel>()
                    LaunchedEffect(route.rentalId, route.vehicleId) {
                        route.rentalId?.let { viewModel.setRentalId(it, route.vehicleId) }
                    }
                    ReportIssueScreen(
                        viewModel = viewModel,
                        onNavigateBack = { if (backStack.size > 1) backStack.removeLastOrNull() }
                    )
                }
                entry<NotificationSettingsRoute> {
                    val viewModel = koinViewModel<NotificationSettingsViewModel>()
                    NotificationSettingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { if (backStack.size > 1) backStack.removeLastOrNull() }
                    )
                }
                entry<SubscriptionRoute> {
                    val viewModel = koinViewModel<SubscriptionViewModel>()
                    SubscriptionScreen(
                        viewModel = viewModel,
                        onNavigateBack = { if (backStack.size > 1) backStack.removeLastOrNull() }
                    )
                }
                entry<PromotionRoute> {
                    val viewModel = koinViewModel<PromotionViewModel>()
                    PromotionScreen(
                        viewModel = viewModel,
                        onNavigateBack = { if (backStack.size > 1) backStack.removeLastOrNull() }
                    )
                }
                entry<PenaltyRoute> {
                    val viewModel = koinViewModel<PenaltyViewModel>()
                    PenaltyScreen(
                        viewModel = viewModel,
                        onNavigateBack = { if (backStack.size > 1) backStack.removeLastOrNull() }
                    )
                }
            }
        )
    }
}

@Composable
private fun MovoBottomBar(
    currentRoute: NavKey?,
    onTabSelected: (BottomNavTab) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = {
                val isSelected = currentRoute is MapRoute
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Explore"
                )
            },
            label = { Text("Explore") },
            selected = currentRoute is MapRoute,
            onClick = { onTabSelected(BottomNavTab.EXPLORE) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.History, contentDescription = "Trips") },
            label = { Text("Trips") },
            selected = currentRoute is HistoryRoute,
            onClick = { onTabSelected(BottomNavTab.TRIPS) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Wallet, contentDescription = "Wallet") },
            label = { Text("Wallet") },
            selected = currentRoute is WalletRoute,
            onClick = { onTabSelected(BottomNavTab.WALLET) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Account") },
            label = { Text("Account") },
            selected = currentRoute is ProfileRoute,
            onClick = { onTabSelected(BottomNavTab.ACCOUNT) }
        )
    }
}

