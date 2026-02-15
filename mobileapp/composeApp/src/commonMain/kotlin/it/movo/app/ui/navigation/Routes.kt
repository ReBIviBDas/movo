package it.movo.app.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object LoginRoute : NavKey

@Serializable
data object RegisterRoute : NavKey

@Serializable
data object ForgotPasswordRoute : NavKey

@Serializable
data object MapRoute : NavKey

@Serializable
data class VehicleDetailRoute(val vehicleId: String) : NavKey

@Serializable
data class BookingRoute(val vehicleId: String) : NavKey

@Serializable
data class ActiveRideRoute(val rentalId: String) : NavKey

@Serializable
data class TripSummaryRoute(val rentalId: String) : NavKey

@Serializable
data class SplitPaymentRoute(val rentalId: String) : NavKey

@Serializable
data object HistoryRoute : NavKey

@Serializable
data object ProfileRoute : NavKey

@Serializable
data object WalletRoute : NavKey

@Serializable
data class ReportIssueRoute(val vehicleId: String? = null, val rentalId: String? = null) : NavKey

@Serializable
data object NotificationSettingsRoute : NavKey

@Serializable
data object SubscriptionRoute : NavKey

@Serializable
data object PromotionRoute : NavKey

@Serializable
data object PenaltyRoute : NavKey


