package health.brook.googlefitsync.models

data class BloodReading (
    val systolicAverage: Float,
    val diastolicAverage: Float,
    val date: Long
    )