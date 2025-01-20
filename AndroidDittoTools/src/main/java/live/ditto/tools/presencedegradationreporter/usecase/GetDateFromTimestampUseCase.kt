package live.ditto.tools.presencedegradationreporter.usecase

import java.text.SimpleDateFormat
import java.util.Locale

class GetDateFromTimestampUseCase {
    private val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

    operator fun invoke(timestamp: Long): String = simpleDateFormat.format(timestamp)
}