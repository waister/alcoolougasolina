package br.com.gazoza.alcoolougasolina.di

import br.com.gazoza.alcoolougasolina.data.repository.HistoryRepository
import br.com.gazoza.alcoolougasolina.data.repository.HistoryRepositoryImpl
import br.com.gazoza.alcoolougasolina.data.repository.NotificationRepository
import br.com.gazoza.alcoolougasolina.data.repository.NotificationRepositoryImpl
import br.com.gazoza.alcoolougasolina.data.repository.PreferencesRepository
import br.com.gazoza.alcoolougasolina.data.repository.PreferencesRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single<PreferencesRepository> { PreferencesRepositoryImpl(androidContext()) }
    single<HistoryRepository> { HistoryRepositoryImpl(get()) }
    single<NotificationRepository> { NotificationRepositoryImpl(get()) }
}
