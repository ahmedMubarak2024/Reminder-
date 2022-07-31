package com.udacity.project4.locationreminders

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.AppDispatchers
import com.udacity.project4.utils.ProductionDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mockito.Mockito

open class BaseTest {
    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    var application = Mockito.mock(Application::class.java)
    fun <T> any(type: Class<T>): T = Mockito.any<T>(type)

    @Before
    fun before() {
        val applicationContext: Application =
            application


    }

    @Before
    fun initKoin() {
        stopKoin()//stop the original app koin
        val applicationContext: Application =
            application
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    applicationContext,
                    get() as ReminderDataSource
                )
            }
            viewModel {
                LoginViewModel()
            }
            single {
                SaveReminderViewModel(
                    applicationContext,
                    get() as ReminderDataSource
                )
            }
            single { ProductionDispatchers() as AppDispatchers }
            single(qualifier = named("IO")) { get<AppDispatchers>().IO }
            single(qualifier = named("MAIN")) { get<AppDispatchers>().Main }
            single { RemindersLocalRepository(get(), get(named("IO"))) as ReminderDataSource }

            single(named("isTesting")) { true }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
    }
}