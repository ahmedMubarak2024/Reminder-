package com.udacity.project4.util

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersDao

//Use FakeDataSource that acts as a test double to the LocalDataSource
open class FakeDataSource : RemindersDao {

    val reminderList: MutableList<ReminderDTO> = mutableListOf()
    var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): List<ReminderDTO> {
        return if (shouldReturnError) throw Exception("Exception")
        else reminderList
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderList.add(reminder)
    }

    override suspend fun getReminderById(reminderId: String): ReminderDTO? {
        return if (shouldReturnError) throw Exception("Exception")
        else reminderList.firstOrNull { it.id == reminderId } ?: throw Exception("Exception")
    }

    override suspend fun deleteAllReminders() {
        reminderList.clear()
    }


}