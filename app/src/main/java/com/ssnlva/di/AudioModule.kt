package com.ssnlva.di

import com.ssnlva.audio.PianoSoundPlayer
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val audioModule = module {
    single { PianoSoundPlayer(androidContext()) }
}
