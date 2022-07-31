package com.udacity.project4.locationreminders.reminderslist

import com.udacity.project4.locationreminders.BaseTest
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

@ExperimentalCoroutinesApi
class RemindersListViewModelTest : BaseTest() {
    // Set the main coroutines dispatcher for unit testing.
    lateinit var remindersListViewModel: RemindersListViewModel
    val datasource = Mockito.spy(FakeDataSource())

    @Before
    fun init() {
        remindersListViewModel = RemindersListViewModel(application, datasource)
    }

    @Test
    fun loadRemindersTest() {
        runTest {
            datasource.saveReminder(ReminderDTO("Title 1", "Desc", "Location", 0.0, 0.0))
            datasource.saveReminder(ReminderDTO("Title 2", "Desc 2", "Location 2", 0.0, 0.0))

            remindersListViewModel.loadReminders()

            val value = remindersListViewModel.remindersList.getOrAwaitValue()
            Assert.assertFalse(value.isNullOrEmpty())
            Assert.assertEquals("Title 1", value?.firstOrNull()?.title)
            Assert.assertEquals("Desc", value?.firstOrNull()?.description)
            Assert.assertEquals("Location", value?.firstOrNull()?.location)
            Assert.assertEquals(0.0, value?.firstOrNull()?.latitude)
            Assert.assertEquals(0.0, value?.firstOrNull()?.longitude)
            Assert.assertEquals("Title 2", value?.get(1)?.title)
            Assert.assertEquals("Desc 2", value?.get(1)?.description)
            Assert.assertEquals("Location 2", value?.get(1)?.location)
            Assert.assertEquals(0.0, value?.get(1)?.latitude)
            Assert.assertEquals(0.0, value?.get(1)?.longitude)

            datasource.setReturnError(true)
            remindersListViewModel.loadReminders()
            Assert.assertEquals("Error", remindersListViewModel.showSnackBar.getOrAwaitValue())
        }

    }

    @Test
    fun clearRemindersTest() {
        runTest {
            remindersListViewModel.clearReminders()
            Mockito.verify(datasource).deleteAllReminders()
        }


    }

}