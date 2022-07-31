package com.udacity.project4

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseUser
import com.udacity.project4.locationreminders.LoginViewModel
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.*
import com.udacity.project4.utils.AppDispatchers
import com.udacity.project4.utils.FirebaseUserLiveData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
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


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {
    // Extended Koin Test - embed autoclose @after method to close Koin after every test
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = DispatchersIdlingResourceRule()
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    val firebaseUserLiveData = FireBaseLiveDataTesting()

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
                Room.inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    RemindersDatabase::class.java
                ).build().reminderDao()
            }
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


    }


    //    TODO: add End to End testing to the app
    @Test
    fun noReminderData_showNoData() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        onView(withId(R.id.noDataTextView))
            .check(matches(isDisplayed()))
        activityScenario.close()
    }

    @Test
    fun testAddReminder() {


        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        val saveReminderViewModel = get<SaveReminderViewModel>()
        var activitySpy: RemindersActivity? = null
        activityScenario.onActivity {
            it.runOnUiThread {
                firebaseUserLiveData.setInternalValue(Mockito.mock(FirebaseUser::class.java))
                saveReminderViewModel.reminderSelectedLocationStr.value = "Location"
            }
            activitySpy = Mockito.spy(it)
        }

        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText("Test A"))
        onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText("Test A Description"))
        onView(isRoot()).perform(ViewActions.closeSoftKeyboard())
        onView(withId(R.id.saveReminder)).perform(ViewActions.click())
        onView(withText(R.string.reminder_saved)).inRoot(
            withDecorView(
                not(
                    `is`(
                        activitySpy?.getWindow()?.getDecorView()
                    )
                )
            )
        ).check(
            matches(
                isDisplayed()
            )
        )
        val reminderDataSource = get<ReminderDataSource>()
        runTest {
            val data = (reminderDataSource.getReminders() as Result.Success).data
            val reminderDTO = data.firstOrNull()
            Assert.assertNotNull(reminderDTO)
            Assert.assertEquals("Test A", reminderDTO?.title)
            Assert.assertEquals("Test A Description", reminderDTO?.description)
            Assert.assertEquals("Location", reminderDTO?.location)

        }



        activityScenario.close()

    }


}
