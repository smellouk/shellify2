package io.shellify.app.domain.usecase

import java.util.Calendar

class IsDndActiveUseCase {
    operator fun invoke(
        dndStartHour: Int,
        dndEndHour: Int,
        hour: Int = currentHour(),
    ): Boolean {
        if (dndStartHour == -1 || dndEndHour == -1 || dndStartHour == dndEndHour) return false
        return if (dndStartHour > dndEndHour) {
            hour >= dndStartHour || hour < dndEndHour
        } else {
            hour in dndStartHour until dndEndHour
        }
    }

    private fun currentHour(): Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
}
