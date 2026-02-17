package it.movo.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentMethod(
    val id: String,
    val type: PaymentMethodType = PaymentMethodType.CARD,
    val brand: String? = null,
    @SerialName("last_four") val lastFour: String = "",
    @SerialName("expiry_month") val expiryMonth: Int? = null,
    @SerialName("expiry_year") val expiryYear: Int? = null,
    @SerialName("is_default") val isDefault: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
enum class PaymentMethodType {
    @SerialName("card")
    CARD,
    @SerialName("paypal")
    PAYPAL,
    @SerialName("apple_pay")
    APPLE_PAY,
    @SerialName("google_pay")
    GOOGLE_PAY
}

@Serializable
data class AddPaymentMethodRequest(
    @SerialName("stripe_token") val stripeToken: String,
    @SerialName("set_as_default") val setAsDefault: Boolean? = null
)

@Serializable
data class PaymentMethodsResponse(val methods: List<PaymentMethod>)

@Serializable
data class Payment(
    val id: String,
    val type: PaymentType,
    @SerialName("rental_id") val rentalId: String? = null,
    @SerialName("subscription_id") val subscriptionId: String? = null,
    @SerialName("penalty_id") val penaltyId: String? = null,
    @SerialName("amount_cents") val amountCents: Int,
    val status: PaymentStatus = PaymentStatus.PENDING,
    @SerialName("payment_method_id") val paymentMethodId: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("completed_at") val completedAt: String? = null
)

@Serializable
enum class PaymentType {
    @SerialName("rental")
    RENTAL,
    @SerialName("subscription")
    SUBSCRIPTION,
    @SerialName("penalty")
    PENALTY,
    @SerialName("refund")
    REFUND
}

@Serializable
enum class PaymentStatus {
    @SerialName("pending")
    PENDING,
    @SerialName("completed")
    COMPLETED,
    @SerialName("failed")
    FAILED,
    @SerialName("refunded")
    REFUNDED
}

@Serializable
data class SplitRequest(
    val id: String,
    @SerialName("rental_id") val rentalId: String,
    val status: SplitRequestStatus = SplitRequestStatus.PENDING,
    val participants: List<SplitParticipant> = emptyList(),
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null
)

@Serializable
enum class SplitRequestStatus {
    @SerialName("pending")
    PENDING,
    @SerialName("accepted")
    ACCEPTED,
    @SerialName("rejected")
    REJECTED,
    @SerialName("expired")
    EXPIRED
}

@Serializable
enum class SplitParticipantStatus {
    @SerialName("pending")
    PENDING,
    @SerialName("accepted")
    ACCEPTED,
    @SerialName("rejected")
    REJECTED
}

@Serializable
data class SplitParticipant(
    @SerialName("user_id") val userId: String,
    val percentage: Double,
    @SerialName("amount_cents") val amountCents: Int = 0,
    val status: SplitParticipantStatus = SplitParticipantStatus.PENDING
)

@Serializable
data class CreateSplitRequest(
    @SerialName("rental_id") val rentalId: String,
    val participants: List<SplitParticipant>
)

@Serializable
data class TransactionsResponse(
    val data: List<Payment>,
    val pagination: CursorPagination
)
