package com.aanand.EncryptionDemoApp.di

import com.aanand.EncryptionDemoApp.services.JcaService
import com.aanand.EncryptionDemoApp.services.KeystoreService
import com.aanand.EncryptionDemoApp.services.TinkService
import com.aanand.EncryptionDemoApp.viewmodels.AndroidKeystoreViewModel
import com.aanand.EncryptionDemoApp.viewmodels.ComparisonViewModel
import com.aanand.EncryptionDemoApp.viewmodels.KeyGeneratorViewModel
import com.aanand.EncryptionDemoApp.viewmodels.TinkViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Services
    single { JcaService() }
    single { KeystoreService() }
    single { TinkService(androidContext()) }

    // ViewModels
    viewModel { KeyGeneratorViewModel(get()) }
    viewModel { AndroidKeystoreViewModel(get()) }
    viewModel { TinkViewModel(get()) }
    viewModel { ComparisonViewModel(get(), get(), get()) }
}
