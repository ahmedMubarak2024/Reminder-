package com.udacity.project4

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import org.hamcrest.core.AllOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class ReminderDescriptionActivityTest {
    private val dataBindingIdlingResource = DataBindingIdlingResource()

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

    @Test
    fun testOpenDetailScreen_showDetailData() {
        val reminderDataItem =
            ReminderDataItem("Test One", "Test Desc", "Location Detail", null, null)
        val intent = ReminderDescriptionActivity.newIntent(
            getApplicationContext(),
            reminderDataItem
        )

        val activityScenario = ActivityScenario.launch<ReminderDescriptionActivity>(intent)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        Espresso.onView(withId(R.id.tv_titile))
            .check(
                ViewAssertions.matches(
                    AllOf.allOf(
                        withText(reminderDataItem.title),
                        isDisplayed()
                    )
                )
            )
        Espresso.onView(withId(R.id.tv_desc))
            .check(
                ViewAssertions.matches(
                    AllOf.allOf(
                        withText(reminderDataItem.description),
                        isDisplayed()
                    )
                )
            )
        Espresso.onView(withId(R.id.tv_location))
            .check(
                ViewAssertions.matches(
                    AllOf.allOf(
                        withText(reminderDataItem.location),
                        isDisplayed()
                    )
                )
            )
        activityScenario.close()

    }
}