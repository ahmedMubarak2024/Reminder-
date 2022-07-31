package com.udacity.project4.locationreminders.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.util.DataUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

@ExperimentalCoroutinesApi

class RemindersLocalRepositoryTest {
    lateinit var remindersLocalRepository: RemindersLocalRepository
    lateinit var dataSource: RemindersDao
    val ioDispatcher = TestCoroutineDispatcher()

    @Before
    fun before() {
        val reminderDao = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build().reminderDao()
        dataSource = Mockito.spy(reminderDao)
        remindersLocalRepository = RemindersLocalRepository(
            dataSource,
            ioDispatcher
        )
    }

    @Test
    fun getRemindersTest() {
        runTest(ioDispatcher) {
            dataSource.saveReminder(DataUtil.getReminderDto())
            dataSource.saveReminder(DataUtil.getReminderDto())
            val reminders = remindersLocalRepository.getReminders()
            Assert.assertNotNull(reminders)
            Assert.assertTrue(reminders is Result.Success)
            Assert.assertEquals(2, (reminders as Result.Success).data.size)
//            dataSource.setReturnError(true)
//            val reminders2 = remindersLocalRepository.getReminders()
//            Assert.assertNotNull(reminders2)
//            println(reminders)
//            println(dataSource.shouldReturnError)
//            Assert.assertTrue(reminders2 is Result.Error)
            dataSource.deleteAllReminders()
            val result = remindersLocalRepository.getReminders()
            Assert.assertTrue(result is Result.Success)
            Assert.assertEquals(0, (result as Result.Success).data.size)
        }

    }

    @Test
    fun saveReminderTest() {
        runTest(ioDispatcher) {
            val reminder = DataUtil.getReminderDto()
            remindersLocalRepository.saveReminder(reminder)
            Mockito.verify(dataSource).saveReminder(reminder)
        }

    }

    @Test
    fun getReminderTest() {
        runTest(ioDispatcher) {

            val reminder = DataUtil.getReminderDto()
            dataSource.saveReminder(reminder)
            val reminders = remindersLocalRepository.getReminder(reminder.id)
            Assert.assertNotNull(reminders)
            Assert.assertTrue(reminders is Result.Success)
            Assert.assertEquals(reminder, (reminders as Result.Success).data)

            val errorResult = remindersLocalRepository.getReminder("WrongID")
//            dataSource.setReturnError(true)
//            val reminders2 = remindersLocalRepository.getReminder(reminder.id)
            Assert.assertNotNull(errorResult)
            Assert.assertTrue(errorResult is Result.Error)
            Assert.assertEquals("Reminder not found!", (errorResult as Result.Error).message)
        }

    }

    @Test
    fun deleteAllRemindersTest() {
        runTest(ioDispatcher) {

            remindersLocalRepository.deleteAllReminders()
            Mockito.verify(dataSource).deleteAllReminders()

        }

    }


//    TODO: Add testing implementation to the RemindersLocalRepository.kt

}