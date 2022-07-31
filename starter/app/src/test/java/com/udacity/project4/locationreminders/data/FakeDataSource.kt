package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
open class FakeDataSource : ReminderDataSource {

    val reminderList: MutableList<ReminderDTO> = mutableListOf()
    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (shouldReturnError) Result.Error("Error", 11)
        else Result.Success(reminderList)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val firstOrNull = reminderList.firstOrNull { it.id == id }
        return if (shouldReturnError) Result.Error("Error")
        else if (firstOrNull == null) Result.Error("Error")
        else Result.Success(firstOrNull)
    }

    override suspend fun deleteAllReminders() {
        reminderList.clear()
    }


}