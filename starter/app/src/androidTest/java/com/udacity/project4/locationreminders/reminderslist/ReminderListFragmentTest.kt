package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.firebase.auth.FirebaseUser
import com.udacity.project4.R
import com.udacity.project4.locationreminders.LoginViewModel
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.*
import com.udacity.project4.utils.AppDispatchers
import com.udacity.project4.utils.FirebaseUserLiveData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest :
    AutoCloseKoinTest() {
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = DispatchersIdlingResourceRule()
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    val firebaseUserLiveData = FireBaseLiveDataTesting()
    val remindersDatabase = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        RemindersDatabase::class.java
    ).build() as RemindersDatabase

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }


    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {

        stopKoin()//stop the original app koin
        appContext = getApplicationContext()

        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            viewModel {
                LoginViewModel()
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { EspressoDispatchers() as AppDispatchers }
            single(qualifier = named("IO")) { get<AppDispatchers>().IO }
            single(qualifier = named("MAIN")) { get<AppDispatchers>().Main }
            single { RemindersLocalRepository(get(), get(named("IO"))) as ReminderDataSource }
            single {
                remindersDatabase
            }
            single { get<RemindersDatabase>().reminderDao() as RemindersDao }
            factory { firebaseUserLiveData as FirebaseUserLiveData }
            single(named("isTesting")) { true }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }

        androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread {
            firebaseUserLiveData.setInternalValue(Mockito.mock(FirebaseUser::class.java))
        }
    }

    @After
    fun after() {
        remindersDatabase.close()
    }

    //    TODO: test the navigation of the fragments.
    @Test
    fun testNavigation() {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        activityScenario.onActivity {
            println(it.navController.currentDestination)
            Assert.assertTrue(it.navController.currentDestination?.id == R.id.saveReminderFragment)
        }

        activityScenario.close()
    }

    //    TODO: test the displayed data on the UI.
    @Test
    fun noReminderData_showNoData() {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        onView(withId(R.id.noDataTextView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        activityScenario.close()
    }

    @Test
    fun testReminderList() {
        val reminderDTO = get<RemindersDao>()
        runTest {
            reminderDTO.saveReminder(
                ReminderDTO(
                    "Title 1",
                    "Description 1",
                    "Location 1",
                    null,
                    null
                )
            )
        }


        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Thread.sleep(3000)
        onView(ViewMatchers.withText("Title 1")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withText("Description 1")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withText("Location 1")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        activityScenario.close()
    }

    //    TODO: add testing for the error messages.
    @Test
    fun testErrorMessage() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())
        activityScenario.onActivity {

        }
        onView(withText(R.string.err_enter_title)).check(
            ViewAssertions.matches(
                isDisplayed()
            )
        )


    }
}