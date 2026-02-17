package it.movo.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BillingPeriod {
    @SerialName("monthly")
    MONTHLY,
    @SerialName("yearly")
    YEARLY
}

@Serializable
data class CursorPagination(
    @SerialName("next_cursor") val nextCursor: String? = null,
    @SerialName("has_more") val hasMore: Boolean = false,
    val limit: Int = 20
)

@Serializable
data class ProblemDetails(
    val type: String = "",
    val title: String = "",
    val status: Int = 0,
    val detail: String? = null,
    val instance: String? = null
)

@Serializable
data class MessageResponse(val message: String)

@Serializable
data class DeleteAccountResponse(
    val message: String,
    @SerialName("deletion_scheduled_at") val deletionScheduledAt: String
)

@Serializable
data class ParkingArea(
    val id: String,
    val name: String,
    val type: ParkingAreaType = ParkingAreaType.STANDARD,
    val geometry: GeoPolygon = GeoPolygon(),
    @SerialName("bonus_minutes") val bonusMinutes: Int? = null,
    val description: String? = null,
    val capacity: Int? = null,
    @SerialName("current_vehicles") val currentVehicles: Int? = null
)

@Serializable
enum class ParkingAreaType {
    @SerialName("standard")
    STANDARD,
    @SerialName("bonus")
    BONUS,
    @SerialName("restricted")
    RESTRICTED
}

@Serializable
data class Penalty(
    val id: String,
    @SerialName("rental_id") val rentalId: String? = null,
    val type: PenaltyType,
    val description: String,
    @SerialName("amount_cents") val amountCents: Int,
    val status: PenaltyStatus = PenaltyStatus.PENDING,
    @SerialName("evidence_urls") val evidenceUrls: List<String> = emptyList(),
    @SerialName("contest_reason") val contestReason: String? = null,
    @SerialName("resolution_notes") val resolutionNotes: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("due_date") val dueDate: String? = null,
    @SerialName("paid_at") val paidAt: String? = null
)

@Serializable
enum class PenaltyType {
    @SerialName("parking_violation")
    PARKING_VIOLATION,
    @SerialName("geofence_exit")
    GEOFENCE_EXIT,
    @SerialName("damage")
    DAMAGE,
    @SerialName("traffic_fine")
    TRAFFIC_FINE,
    @SerialName("late_return")
    LATE_RETURN,
    @SerialName("other")
    OTHER
}

@Serializable
enum class PenaltyStatus {
    @SerialName("pending")
    PENDING,
    @SerialName("paid")
    PAID,
    @SerialName("contested")
    CONTESTED,
    @SerialName("cancelled")
    CANCELLED
}

@Serializable
data class SubscriptionPlan(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("price_cents") val priceCents: Int,
    @SerialName("billing_period") val billingPeriod: BillingPeriod,
    @SerialName("included_minutes") val includedMinutes: Int? = null,
    @SerialName("discount_percentage") val discountPercentage: Int = 0,
    val features: List<String> = emptyList()
)

@Serializable
data class UserSubscription(
    val id: String,
    val plan: SubscriptionPlan? = null,
    @SerialName("plan_id") val planId: String,
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    @SerialName("auto_renew") val autoRenew: Boolean = true,
    @SerialName("started_at") val startedAt: String = "",
    @SerialName("current_period_start") val currentPeriodStart: String? = null,
    @SerialName("current_period_end") val currentPeriodEnd: String = "",
    @SerialName("minutes_used") val minutesUsed: Int = 0,
    @SerialName("minutes_remaining") val minutesRemaining: Int? = null
)

@Serializable
enum class SubscriptionStatus {
    @SerialName("active")
    ACTIVE,
    @SerialName("cancelling")
    CANCELLING,
    @SerialName("cancelled")
    CANCELLED,
    @SerialName("expired")
    EXPIRED
}

@Serializable
enum class PromotionType {
    @SerialName("percentage")
    PERCENTAGE,
    @SerialName("fixed_amount")
    FIXED_AMOUNT,
    @SerialName("free_minutes")
    FREE_MINUTES
}

@Serializable
data class Promotion(
    val id: String,
    val code: String,
    val type: PromotionType,
    val value: Double,
    val description: String? = null,
    @SerialName("valid_from") val validFrom: String? = null,
    @SerialName("valid_until") val validUntil: String? = null,
    @SerialName("min_rental_minutes") val minRentalMinutes: Int? = null,
    @SerialName("max_uses") val maxUses: Int? = null,
    @SerialName("uses_remaining") val usesRemaining: Int? = null
)

@Serializable
data class ApplyPromoRequest(@SerialName("promo_code") val promoCode: String)

@Serializable
data class SubscribeRequest(
    @SerialName("plan_id") val planId: String,
    @SerialName("auto_renew") val autoRenew: Boolean = true
)

@Serializable
data class SubscriptionPlansResponse(val plans: List<SubscriptionPlan>)

@Serializable
data class AutoRenewRequest(@SerialName("auto_renew") val autoRenew: Boolean)

@Serializable
data class CancelSubscriptionResponse(
    @SerialName("subscription_id") val subscriptionId: String,
    val status: String,
    @SerialName("active_until") val activeUntil: String
)

@Serializable
data class PromotionsResponse(val promotions: List<Promotion>)

@Serializable
data class ParkingAreasResponse(@SerialName("parking_areas") val parkingAreas: List<ParkingArea>)

@Serializable
data class PenaltiesResponse(
    val data: List<Penalty>,
    val pagination: CursorPagination? = null
)

@Serializable
data class ContestPenaltyRequest(val reason: String)
