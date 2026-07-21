package com.example.chessclock.di

import android.os.SystemClock
import com.example.chessclock.data.clock.BuiltInTimeControlProvider
import com.example.chessclock.domain.clock.engine.ChessClockEngine
import com.example.chessclock.domain.clock.engine.StandardChessClockEngine
import com.example.chessclock.domain.clock.provider.TimeControlProvider
import com.example.chessclock.domain.time.TimeProvider
import com.example.chessclock.presentation.clock.ClockTimeFormatter
import com.example.chessclock.presentation.clock.ClockUiStateMapper
import com.example.chessclock.presentation.clock.DefaultClockUiStateMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideChessClockEngine(): ChessClockEngine {
        return StandardChessClockEngine()
    }

    @Provides
    @Singleton
    fun provideClockTimeFormatter(): ClockTimeFormatter {
        return ClockTimeFormatter()
    }

    @Provides
    @Singleton
    fun provideClockUiStateMapper(timeFormatter: ClockTimeFormatter): ClockUiStateMapper {
        return DefaultClockUiStateMapper(timeFormatter)
    }

    @Provides
    @Singleton
    @MainDispatcher
    fun provideMainImmediateDispatcher(): CoroutineDispatcher {
        return Dispatchers.Main.immediate
    }

    @Provides
    @Singleton
    fun provideTimeProvider(): TimeProvider {
        return object : TimeProvider {
            override fun getElapsedRealtime(): Long = SystemClock.elapsedRealtime()
        }
    }

    @Provides
    @Singleton
    fun provideTimeControlProvider(): TimeControlProvider {
        return BuiltInTimeControlProvider()
    }
}
