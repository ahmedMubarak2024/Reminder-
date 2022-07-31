package com.udacity.project4.locationreminders.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.util.DataUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    lateinit var database: RemindersDatabase
    lateinit var dao: RemindersDao

    //    TODO: Add testing implementation to the RemindersDao.kt
    @Before
    fun before() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
        dao = database.reminderDao()
    }

    @Test
    fun saveReminderTest() {
        runTest {
            val reminderDto = DataUtil.getReminderDto()
            dao.saveReminder(reminderDto)
            val reminderDTO = dao.getReminderById(reminderDto.id)
            Assert.assertEquals(reminderDto, reminderDTO)
        }

    }

    @Test
    fun getRemindersTest() {
        runTest {
            val reminderDto = DataUtil.getReminderDto()
            dao.saveReminder(reminderDto)
            dao.saveReminder(DataUtil.getReminderDto())
            dao.saveReminder(DataUtil.getReminderDto())
            dao.saveReminder(DataUtil.getReminderDto())
            val reminders = dao.getReminders()
            Assert.assertEquals(4, reminders.size)
        }

    }

    @Test
    fun deleteAllTest() {
        runTest {
            val reminderDto = DataUtil.getReminderDto()
            dao.saveReminder(reminderDto)
            dao.saveReminder(DataUtil.getReminderDto())
            dao.saveReminder(DataUtil.getReminderDto())
            dao.saveReminder(DataUtil.getReminderDto())
            dao.deleteAllReminders()
            val reminders = dao.getReminders()
            Assert.assertEquals(0, reminders.size)
        }

    }

    @Test
    fun saveReminder_OnConflict_ReplaceTest() {
        runTest {
            val reminderDto = DataUtil.getReminderDto()
            val reminderDtoUpdated = DataUtil.getReminderDto()
            reminderDtoUpdated.id = reminderDto.id
            dao.saveReminder(reminderDto)
            dao.saveReminder(reminderDtoUpdated)
            val reminderDTOFromDataBase = dao.getReminderById(reminderDtoUpdated.id)
            Assert.assertEquals(reminderDtoUpdated, reminderDTOFromDataBase)
        }

    }

}