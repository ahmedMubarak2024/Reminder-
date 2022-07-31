package com.udacity.project4.util

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import java.util.*
import kotlin.random.Random

class DataUtil {
    companion object {
        fun getReminderDto() = ReminderDTO(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            Random.nextDouble(),
            Random.nextDouble()
        )

    }
}