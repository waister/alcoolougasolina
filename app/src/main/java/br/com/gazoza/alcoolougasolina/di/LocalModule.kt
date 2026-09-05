package br.com.gazoza.alcoolougasolina.di

import br.com.gazoza.alcoolougasolina.data.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val localModule = module {
    single { AppDatabase.getDatabase(androidContext()) }
    single { get<AppDatabase>().comparisonDao() }
}
