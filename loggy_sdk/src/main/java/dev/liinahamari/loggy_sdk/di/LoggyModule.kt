/*
Copyright 2021 liinahamari

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package dev.liinahamari.loggy_sdk.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dev.liinahamari.loggy_sdk.helper.BaseComposers
import dev.liinahamari.loggy_sdk.helper.BaseSchedulerProvider
import dev.liinahamari.loggy_sdk.helper.SchedulersProvider
import dev.liinahamari.loggy_sdk.screens.logs.LoggerInteractor
import java.io.File
import javax.inject.Named
import javax.inject.Singleton

const val APPLICATION_CONTEXT = "app_context"

@Module
internal class LoggyModule {
    @Provides
    @Singleton
    fun provideLoggerInteractor(@Named(APPLICATION_CONTEXT) appContext: Context, composers: BaseComposers, logFile: File): LoggerInteractor = LoggerInteractor(appContext, composers, logFile)

    @Provides
    @Singleton
    @Named(APPLICATION_CONTEXT)
    fun provideContext(application: Application): Context = application.applicationContext

    @Provides
    @Singleton
    fun provideComposers(schedulerProvider: SchedulersProvider): BaseComposers = BaseComposers(schedulerProvider)

    @Provides
    @Singleton
    fun provideSchedulerProvider(): SchedulersProvider = BaseSchedulerProvider()
}