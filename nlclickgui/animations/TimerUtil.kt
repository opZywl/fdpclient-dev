package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations

class TimerUtil {
    var lastMS = System.currentTimeMillis()

    fun reset() {
        lastMS = System.currentTimeMillis()
    }

    fun hasTimeElapsed(time: Long, reset: Boolean): Boolean {
        if (System.currentTimeMillis() - lastMS > time) {
            if (reset) reset()
            return true
        }
        return false
    }

    fun hasTimeElapsed(time: Long): Boolean = System.currentTimeMillis() - lastMS > time

    fun getTime(): Long = System.currentTimeMillis() - lastMS

    fun setTime(time: Long) {
        lastMS = time
    }
}
