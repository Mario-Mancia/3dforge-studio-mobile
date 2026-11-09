package com.example.a3df_studio_app.data.di

import android.content.Context
import com.example.a3df_studio_app.data.external.WhatsAppLauncher
import com.example.a3df_studio_app.data.remote.auth.GoogleCredentialProvider
import com.example.a3df_studio_app.data.repositories.AuthRepository
import com.example.a3df_studio_app.data.repositories.CatalogRepository
import com.example.a3df_studio_app.data.repositories.ClienteRepository
import com.example.a3df_studio_app.data.repositories.CotizacionRepository
import com.example.a3df_studio_app.data.repositories.CrmRepository
import com.example.a3df_studio_app.data.repositories.EncargoRepository
import com.example.a3df_studio_app.data.repositories.FeedbackRepository
import com.example.a3df_studio_app.data.repositories.NegocioRepository
import com.example.a3df_studio_app.data.repositories.PagoRepository
import com.example.a3df_studio_app.data.repositories.PreferencesRepository
import com.example.a3df_studio_app.domain.usecases.auth.GetCurrentUserUseCase
import com.example.a3df_studio_app.domain.usecases.auth.GetLastEmailUseCase
import com.example.a3df_studio_app.domain.usecases.auth.GetPendingVerificationEmailUseCase
import com.example.a3df_studio_app.domain.usecases.auth.LoginWithEmailUseCase
import com.example.a3df_studio_app.domain.usecases.auth.LogoutUseCase
import com.example.a3df_studio_app.domain.usecases.auth.ObserveAuthStateUseCase
import com.example.a3df_studio_app.domain.usecases.auth.RegisterWithEmailUseCase
import com.example.a3df_studio_app.domain.usecases.auth.ResendVerificationCodeUseCase
import com.example.a3df_studio_app.domain.usecases.auth.SendPasswordResetUseCase
import com.example.a3df_studio_app.domain.usecases.auth.SignInWithGoogleUseCase
import com.example.a3df_studio_app.domain.usecases.auth.VerifyEmailCodeUseCase
import com.example.a3df_studio_app.domain.usecases.catalogo.GetCategoriasServiciosUseCase
import com.example.a3df_studio_app.domain.usecases.catalogo.GetNegocioInfoUseCase
import com.example.a3df_studio_app.domain.usecases.catalogo.GetProductoDetailUseCase
import com.example.a3df_studio_app.domain.usecases.catalogo.GetProductosUseCase
import com.example.a3df_studio_app.domain.usecases.catalogo.GetPromocionesActivasUseCase
import com.example.a3df_studio_app.domain.usecases.catalogo.GetServicioDetailUseCase
import com.example.a3df_studio_app.domain.usecases.catalogo.GetServiciosUseCase
import com.example.a3df_studio_app.domain.usecases.cotizaciones.GetMisCotizacionesUseCase
import com.example.a3df_studio_app.domain.usecases.cotizaciones.RequestAdvisoryUseCase
import com.example.a3df_studio_app.domain.usecases.cotizaciones.RequestQuoteUseCase
import com.example.a3df_studio_app.domain.usecases.crm.RegisterCrmInteractionUseCase
import com.example.a3df_studio_app.domain.usecases.encargos.CreateProductOrderUseCase
import com.example.a3df_studio_app.domain.usecases.encargos.GetEncargoDetailUseCase
import com.example.a3df_studio_app.domain.usecases.encargos.GetMisEncargosUseCase
import com.example.a3df_studio_app.domain.usecases.feedback.GetMisResenasUseCase
import com.example.a3df_studio_app.domain.usecases.feedback.GetMisSolicitudesSoporteUseCase
import com.example.a3df_studio_app.domain.usecases.feedback.SubmitResenaUseCase
import com.example.a3df_studio_app.domain.usecases.feedback.SubmitSupportRequestUseCase
import com.example.a3df_studio_app.domain.usecases.pagos.GetMisFacturasUseCase
import com.example.a3df_studio_app.domain.usecases.pagos.GetMisPagosUseCase
import com.example.a3df_studio_app.domain.usecases.pagos.ProcessSimulatedPaymentUseCase
import com.example.a3df_studio_app.domain.usecases.perfil.GetClienteProfileUseCase
import com.example.a3df_studio_app.domain.usecases.perfil.ObserveClienteProfileUseCase
import com.example.a3df_studio_app.domain.usecases.perfil.UpdateClienteProfileUseCase
import com.example.a3df_studio_app.domain.usecases.preferencias.HasSeenOnboardingUseCase
import com.example.a3df_studio_app.domain.usecases.preferencias.MarkOnboardingSeenUseCase

object AppContainer {

    private lateinit var appContext: Context

    val isInitialized: Boolean
        get() = ::appContext.isInitialized

    fun initialize(context: Context) {
        if (!isInitialized) appContext = context.applicationContext
    }

    private fun context(): Context {
        check(isInitialized) { "AppContainer.initialize(context) debe llamarse en MainActivity.onCreate()" }
        return appContext
    }

    val preferencesRepository: PreferencesRepository by lazy { PreferencesRepository(context()) }
    val authRepository: AuthRepository by lazy { AuthRepository(GoogleCredentialProvider()) }
    val clienteRepository: ClienteRepository by lazy { ClienteRepository() }
    val catalogRepository: CatalogRepository by lazy { CatalogRepository() }
    val negocioRepository: NegocioRepository by lazy { NegocioRepository() }
    val cotizacionRepository: CotizacionRepository by lazy { CotizacionRepository() }
    val encargoRepository: EncargoRepository by lazy { EncargoRepository() }
    val pagoRepository: PagoRepository by lazy { PagoRepository() }
    val feedbackRepository: FeedbackRepository by lazy { FeedbackRepository() }
    val crmRepository: CrmRepository by lazy { CrmRepository() }
    val whatsAppLauncher: WhatsAppLauncher by lazy { WhatsAppLauncher() }

    val observeAuthStateUseCase by lazy { ObserveAuthStateUseCase(authRepository) }
    val getCurrentUserUseCase by lazy { GetCurrentUserUseCase(authRepository) }
    val registerWithEmailUseCase by lazy { RegisterWithEmailUseCase(authRepository, clienteRepository, preferencesRepository) }
    val verifyEmailCodeUseCase by lazy { VerifyEmailCodeUseCase(authRepository, clienteRepository, preferencesRepository) }
    val resendVerificationCodeUseCase by lazy { ResendVerificationCodeUseCase(authRepository) }
    val getPendingVerificationEmailUseCase by lazy { GetPendingVerificationEmailUseCase(preferencesRepository) }
    val getLastEmailUseCase by lazy { GetLastEmailUseCase(preferencesRepository) }
    val loginWithEmailUseCase by lazy { LoginWithEmailUseCase(authRepository, clienteRepository, preferencesRepository) }
    val signInWithGoogleUseCase by lazy { SignInWithGoogleUseCase(authRepository, clienteRepository, preferencesRepository) }
    val sendPasswordResetUseCase by lazy { SendPasswordResetUseCase(authRepository) }
    val logoutUseCase by lazy { LogoutUseCase(authRepository, clienteRepository) }

    val getClienteProfileUseCase by lazy { GetClienteProfileUseCase(authRepository, clienteRepository) }
    val observeClienteProfileUseCase by lazy { ObserveClienteProfileUseCase(clienteRepository) }
    val updateClienteProfileUseCase by lazy { UpdateClienteProfileUseCase(authRepository, clienteRepository) }

    val getProductosUseCase by lazy { GetProductosUseCase(catalogRepository) }
    val getProductoDetailUseCase by lazy { GetProductoDetailUseCase(catalogRepository) }
    val getServiciosUseCase by lazy { GetServiciosUseCase(catalogRepository) }
    val getServicioDetailUseCase by lazy { GetServicioDetailUseCase(catalogRepository) }
    val getCategoriasServiciosUseCase by lazy { GetCategoriasServiciosUseCase(catalogRepository) }
    val getPromocionesActivasUseCase by lazy { GetPromocionesActivasUseCase(catalogRepository) }
    val getNegocioInfoUseCase by lazy { GetNegocioInfoUseCase(negocioRepository) }

    val requestQuoteUseCase by lazy {
        RequestQuoteUseCase(authRepository, cotizacionRepository, crmRepository, negocioRepository, whatsAppLauncher)
    }
    val getMisCotizacionesUseCase by lazy { GetMisCotizacionesUseCase(authRepository, cotizacionRepository) }
    val requestAdvisoryUseCase by lazy {
        RequestAdvisoryUseCase(authRepository, crmRepository, negocioRepository, whatsAppLauncher)
    }

    val createProductOrderUseCase by lazy { CreateProductOrderUseCase(authRepository, encargoRepository) }
    val getMisEncargosUseCase by lazy { GetMisEncargosUseCase(authRepository, encargoRepository) }
    val getEncargoDetailUseCase by lazy { GetEncargoDetailUseCase(authRepository, encargoRepository) }

    val getMisFacturasUseCase by lazy { GetMisFacturasUseCase(authRepository, pagoRepository) }
    val getMisPagosUseCase by lazy { GetMisPagosUseCase(authRepository, pagoRepository) }
    val processSimulatedPaymentUseCase by lazy { ProcessSimulatedPaymentUseCase(authRepository, pagoRepository) }

    val submitResenaUseCase by lazy { SubmitResenaUseCase(authRepository, clienteRepository, feedbackRepository) }
    val submitSupportRequestUseCase by lazy {
        SubmitSupportRequestUseCase(authRepository, clienteRepository, feedbackRepository)
    }
    val getMisResenasUseCase by lazy { GetMisResenasUseCase(authRepository, feedbackRepository) }
    val getMisSolicitudesSoporteUseCase by lazy { GetMisSolicitudesSoporteUseCase(authRepository, feedbackRepository) }

    val registerCrmInteractionUseCase by lazy { RegisterCrmInteractionUseCase(authRepository, crmRepository) }

    val hasSeenOnboardingUseCase by lazy { HasSeenOnboardingUseCase(preferencesRepository) }
    val markOnboardingSeenUseCase by lazy { MarkOnboardingSeenUseCase(preferencesRepository) }
}
