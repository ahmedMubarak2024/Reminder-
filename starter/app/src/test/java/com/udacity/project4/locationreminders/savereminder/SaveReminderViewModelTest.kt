package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import com.udacity.project4.locationreminders.BaseTest
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest : BaseTest() {

    lateinit var saveReminderViewModel: SaveReminderViewModel
    val dataSource = Mockito.spy(FakeDataSource())

    @Before
    fun init() {
        saveReminderViewModel = SaveReminderViewModel(application as Application, dataSource)
    }

    @Test
    fun onClearTest() {
        saveReminderViewModel.reminderTitle.value = ""
        saveReminderViewModel.longitude.value = 0.0
        saveReminderViewModel.latitude.value = 0.0
        saveReminderViewModel.reminderSelectedLocationStr.value = ""
        saveReminderViewModel.reminderDescription.value = ""

        saveReminderViewModel.onClear()

        Assert.assertNull(saveReminderViewModel.reminderDescription.value)
        Assert.assertNull(saveReminderViewModel.longitude.value)
        Assert.assertNull(saveReminderViewModel.reminderTitle.value)
        Assert.assertNull(saveReminderViewModel.latitude.value)
        Assert.assertNull(saveReminderViewModel.reminderSelectedLocationStr.value)

    }

    @Test
    fun saveReminderTest() {
        saveReminderViewModel.reminderTitle.value = "Test"
        saveReminderViewModel.longitude.value = 1.0
        saveReminderViewModel.latitude.value = 2.0
        saveReminderViewModel.reminderSelectedLocationStr.value = "Location"
        saveReminderViewModel.reminderDescription.value = "Desc"

        val reminderData = ReminderDataItem("Test", "Desc", "location", 1.1, 2.2)
        saveReminderViewModel.saveReminder(reminderData)
        runTest {
            Mockito.verify(dataSource).saveReminder(any(ReminderDTO::class.java))
            val result = dataSource.getReminders()
            Assert.assertTrue(result is Result.Success)
            val data = (result as Result.Success).data
            val item = data.firstOrNull()
            Assert.assertNotNull(item)
            Assert.assertEquals("Test", item?.title)
            Assert.assertEquals("Desc", item?.description)
            Assert.assertEquals("location", item?.location)
            Assert.assertEquals(1.1, item?.latitude)
            Assert.assertEquals(2.2, item?.longitude)

        }


    }


}