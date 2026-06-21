package com.ssnlva.di

import com.ssnlva.domain.piano.PianoPreferencesRepository
import com.ssnlva.domain.piano.SharedPreferencesPianoRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val domainModule = module {
    single<PianoPreferencesRepository> { SharedPreferencesPianoRepository(androidContext()) }
}