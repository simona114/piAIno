package com.ssnlva.di

import com.ssnlva.ui.piano.PianoViewModel
import com.ssnlva.ui.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::SettingsViewModel)
    viewModelOf(::PianoViewModel)
}
