package com.example.a3df_studio_app.data.di

import android.content.Context
import com.example.a3df_studio_app.data.local.AppPreferences
import com.example.a3df_studio_app.data.remote.ApiClient
import com.example.a3df_studio_app.data.remote.auth.GoogleCredentialProvider
import com.example.a3df_studio_app.data.repositories.AccountRepository
import com.example.a3df_studio_app.data.repositories.AuthRepository
import com.example.a3df_studio_app.data.repositories.CartRepository
import com.example.a3df_studio_app.data.repositories.CatalogRepository
import com.example.a3df_studio_app.data.repositories.FileRepository
import com.example.a3df_studio_app.data.repositories.InvoiceRepository
import com.example.a3df_studio_app.data.repositories.NotificationRepository
import com.example.a3df_studio_app.data.repositories.OrderRepository
import com.example.a3df_studio_app.data.repositories.PurchaseRepository
import com.example.a3df_studio_app.data.repositories.QuoteRepository
import com.example.a3df_studio_app.data.repositories.SupportRepository
import com.example.a3df_studio_app.domain.usecases.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Contenedor manual de dependencias (sin Hilt). Se inicializa en MainActivity.onCreate(). */
object AppContainer {

    private lateinit var appContext: Context

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val isInitialized: Boolean
        get() = ::appContext.isInitialized

    fun initialize(context: Context) {
        if (!isInitialized) appContext = context.applicationContext
    }

    private fun context(): Context {
        check(isInitialized) { "AppContainer.initialize(context) debe llamarse en MainActivity.onCreate()" }
        return appContext
    }

    // ---- Infraestructura ----
    val preferences: AppPreferences by lazy { AppPreferences(context()) }
    val apiClient: ApiClient by lazy {
        ApiClient().also { cliente -> cliente.alExpirar = { appScope.launch { authRepository.cerrarLocal() } } }
    }

    // ---- Repositorios ----
    val authRepository: AuthRepository by lazy { AuthRepository(apiClient, preferences, GoogleCredentialProvider()) }
    val catalogRepository: CatalogRepository by lazy { CatalogRepository(apiClient) }
    val cartRepository: CartRepository by lazy { CartRepository(preferences) }
    val purchaseRepository: PurchaseRepository by lazy { PurchaseRepository(apiClient) }
    val quoteRepository: QuoteRepository by lazy { QuoteRepository(apiClient, context()) }
    val orderRepository: OrderRepository by lazy { OrderRepository(apiClient) }
    val invoiceRepository: InvoiceRepository by lazy { InvoiceRepository(apiClient) }
    val notificationRepository: NotificationRepository by lazy { NotificationRepository(apiClient) }
    val supportRepository: SupportRepository by lazy { SupportRepository(apiClient) }
    val accountRepository: AccountRepository by lazy { AccountRepository(apiClient) }
    val fileRepository: FileRepository by lazy { FileRepository(apiClient, context()) }

    // ---- Casos de uso: sesión ----
    val observeAuthStateUseCase by lazy { ObserveAuthStateUseCase(authRepository) }
    val restoreSessionUseCase by lazy { RestoreSessionUseCase(authRepository) }
    val getLastEmailUseCase by lazy { GetLastEmailUseCase(authRepository) }
    val loginUseCase by lazy { LoginUseCase(authRepository) }
    val loginWithGoogleUseCase by lazy { LoginWithGoogleUseCase(authRepository) }
    val registerUseCase by lazy { RegisterUseCase(authRepository) }
    val resendConfirmationUseCase by lazy { ResendConfirmationUseCase(authRepository) }
    val recoverPasswordUseCase by lazy { RecoverPasswordUseCase(authRepository) }
    val changePasswordUseCase by lazy { ChangePasswordUseCase(authRepository) }
    val logoutUseCase by lazy { LogoutUseCase(authRepository) }

    // ---- Casos de uso: catálogo y carrito ----
    val getProductosUseCase by lazy { GetProductosUseCase(catalogRepository) }
    val getProductoUseCase by lazy { GetProductoUseCase(catalogRepository) }
    val getServiciosUseCase by lazy { GetServiciosUseCase(catalogRepository) }
    val getServicioUseCase by lazy { GetServicioUseCase(catalogRepository) }
    val getCategoriasUseCase by lazy { GetCategoriasUseCase(catalogRepository) }
    val getNegocioUseCase by lazy { GetNegocioUseCase(catalogRepository) }
    val observeCartUseCase by lazy { ObserveCartUseCase(cartRepository) }
    val addToCartUseCase by lazy { AddToCartUseCase(cartRepository) }
    val updateCartQuantityUseCase by lazy { UpdateCartQuantityUseCase(cartRepository) }
    val removeFromCartUseCase by lazy { RemoveFromCartUseCase(cartRepository) }
    val clearCartUseCase by lazy { ClearCartUseCase(cartRepository) }
    val validateCartUseCase by lazy { ValidateCartUseCase(purchaseRepository, cartRepository) }
    val confirmPurchaseUseCase by lazy { ConfirmPurchaseUseCase(purchaseRepository, cartRepository) }

    // ---- Casos de uso: documentos ----
    val listQuotesUseCase by lazy { ListQuotesUseCase(quoteRepository) }
    val getQuoteUseCase by lazy { GetQuoteUseCase(quoteRepository) }
    val requestQuoteUseCase by lazy { RequestQuoteUseCase(quoteRepository) }
    val respondQuoteUseCase by lazy { RespondQuoteUseCase(quoteRepository) }
    val listOrdersUseCase by lazy { ListOrdersUseCase(orderRepository) }
    val getOrderUseCase by lazy { GetOrderUseCase(orderRepository) }
    val cancelOrderUseCase by lazy { CancelOrderUseCase(orderRepository) }
    val listInvoicesUseCase by lazy { ListInvoicesUseCase(invoiceRepository) }
    val getInvoiceUseCase by lazy { GetInvoiceUseCase(invoiceRepository) }
    val downloadFileUseCase by lazy { DownloadFileUseCase(fileRepository) }

    // ---- Casos de uso: seguimiento y cuenta ----
    val listNotificationsUseCase by lazy { ListNotificationsUseCase(notificationRepository) }
    val getUnreadCountUseCase by lazy { GetUnreadCountUseCase(notificationRepository) }
    val markNotificationReadUseCase by lazy { MarkNotificationReadUseCase(notificationRepository) }
    val markAllNotificationsReadUseCase by lazy { MarkAllNotificationsReadUseCase(notificationRepository) }
    val listSupportUseCase by lazy { ListSupportUseCase(supportRepository) }
    val getSupportUseCase by lazy { GetSupportUseCase(supportRepository) }
    val createSupportUseCase by lazy { CreateSupportUseCase(supportRepository) }
    val closeSupportUseCase by lazy { CloseSupportUseCase(supportRepository) }
    val getProfileUseCase by lazy { GetProfileUseCase(accountRepository) }
    val updateProfileUseCase by lazy { UpdateProfileUseCase(accountRepository) }
    val getPanelUseCase by lazy { GetPanelUseCase(accountRepository) }
}
