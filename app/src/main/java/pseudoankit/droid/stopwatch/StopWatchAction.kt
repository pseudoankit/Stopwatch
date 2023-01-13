package pseudoankit.droid.stopwatch

enum class StopWatchAction {
    Start,
    Pause,
    Reset,
    MoveToBackground,
    MoveToForeground,
    GetStatus;

    companion object {
        fun fromString(value: String?): StopWatchAction? = values().find {
            it.name.equals(value, false)
        }
    }
}