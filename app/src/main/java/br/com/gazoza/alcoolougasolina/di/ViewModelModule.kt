package br.com.gazoza.alcoolougasolina.di

import br.com.gazoza.alcoolougasolina.features.history.HistoryViewModel
import br.com.gazoza.alcoolougasolina.features.main.MainViewModel
import br.com.gazoza.alcoolougasolina.features.notifications.NotificationsViewModel
import br.com.gazoza.alcoolougasolina.features.start.StartViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule =
    module {
        viewModelOf(::StartViewModel)
        viewModelOf(::MainViewModel)
        viewModelOf(::HistoryViewModel)
        viewModelOf(::NotificationsViewModel)
    }
