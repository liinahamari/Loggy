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
import dagger.BindsInstance
import dagger.Component
import dev.liinahamari.loggy_sdk.base.BaseFragment
import dev.liinahamari.loggy_sdk.helper.FlightRecorder
import javax.inject.Named
import javax.inject.Singleton

const val USER_ID_QUALIFIER = "q--user_id"
const val INTEGRATORS_EMAIL_QUALIFIER = "q--email"

@Singleton
@Component(modules = [MainModule::class, ViewModelBuilderModule::class, DbModule::class])
interface LoggyComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance fun application(app: Application): Builder
        @BindsInstance fun email(@Named(INTEGRATORS_EMAIL_QUALIFIER) email: String): Builder
        @BindsInstance fun userId(@Named(USER_ID_QUALIFIER) userId: String): Builder

        fun build(): LoggyComponent
    }

    fun inject(fragment: BaseFragment)
    fun inject(flightRecorder: FlightRecorder)
}