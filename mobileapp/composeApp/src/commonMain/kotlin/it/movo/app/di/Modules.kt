package it.movo.app.di

import com.russhwolf.settings.Settings
import it.movo.app.data.auth.TokenManager
import it.movo.app.data.remote.api.AuthApi
import it.movo.app.data.remote.api.BookingApi
import it.movo.app.data.remote.api.IssueApi
import it.movo.app.data.remote.api.ParkingAreaApi
import it.movo.app.data.remote.api.PaymentApi
import it.movo.app.data.remote.api.PenaltyApi
import it.movo.app.data.remote.api.PromotionApi
import it.movo.app.data.remote.api.RentalApi
import it.movo.app.data.remote.api.SubscriptionApi
import it.movo.app.data.remote.api.UserApi
import it.movo.app.data.remote.api.VehicleApi
import it.movo.app.data.remote.createHttpClient
import it.movo.app.data.repository.AuthRepository
import it.movo.app.data.repository.BookingRepository
import it.movo.app.data.repository.IssueRepository
import it.movo.app.data.repository.ParkingAreaRepository
import it.movo.app.data.repository.PaymentRepository
import it.movo.app.data.repository.PenaltyRepository
import it.movo.app.data.repository.PromotionRepository
import it.movo.app.data.repository.RentalRepository
import it.movo.app.data.repository.SubscriptionRepository
import it.movo.app.data.repository.UserRepository
import it.movo.app.data.repository.VehicleRepository
import it.movo.app.ui.auth.ForgotPasswordViewModel
import it.movo.app.ui.auth.LoginViewModel
import it.movo.app.ui.auth.RegisterViewModel
import it.movo.app.ui.booking.BookingViewModel
import it.movo.app.ui.history.HistoryViewModel
import it.movo.app.ui.issue.ReportIssueViewModel
import it.movo.app.ui.map.MapViewModel
import it.movo.app.ui.payment.SplitPaymentViewModel
import it.movo.app.ui.penalty.PenaltyViewModel
import it.movo.app.ui.profile.ProfileViewModel
import it.movo.app.ui.promotion.PromotionViewModel
import it.movo.app.ui.ride.ActiveRideViewModel
import it.movo.app.ui.ride.TripSummaryViewModel
import it.movo.app.ui.settings.NotificationSettingsViewModel
import it.movo.app.ui.subscription.SubscriptionViewModel
import it.movo.app.ui.vehicle.VehicleDetailViewModel
import it.movo.app.ui.wallet.WalletViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { Settings() }
    single { TokenManager(get()) }

    single {
        val tokenManager: TokenManager = get()
        createHttpClient(
            tokenProvider = { tokenManager.accessToken }
        )
    }

    single { AuthApi(get()) }
    single { VehicleApi(get()) }
    single { BookingApi(get()) }
    single { RentalApi(get()) }
    single { PaymentApi(get()) }
    single { UserApi(get()) }
    single { IssueApi(get()) }
    single { SubscriptionApi(get()) }
    single { PromotionApi(get()) }
    single { ParkingAreaApi(get()) }
    single { PenaltyApi(get()) }

    single { AuthRepository(get(), get()) }
    single { VehicleRepository(get()) }
    single { BookingRepository(get()) }
    single { RentalRepository(get()) }
    single { PaymentRepository(get()) }
    single { UserRepository(get()) }
    single { IssueRepository(get()) }
    single { SubscriptionRepository(get()) }
    single { PromotionRepository(get()) }
    single { ParkingAreaRepository(get()) }
    single { PenaltyRepository(get()) }

    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get(), get()) }
    viewModel { ForgotPasswordViewModel(get()) }
    viewModel { MapViewModel(get(), get()) }
    viewModel { BookingViewModel(get(), get(), get()) }
    viewModel { ActiveRideViewModel(get(), get()) }
    viewModel { TripSummaryViewModel(get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { WalletViewModel(get()) }
    viewModel { HistoryViewModel(get()) }
    viewModel { VehicleDetailViewModel(get()) }
    viewModel { SplitPaymentViewModel(get()) }
    viewModel { ReportIssueViewModel(get(), get(), get(), get()) }
    viewModel { NotificationSettingsViewModel(get()) }
    viewModel { SubscriptionViewModel(get()) }
    viewModel { PromotionViewModel(get()) }
    viewModel { PenaltyViewModel(get()) }
}
