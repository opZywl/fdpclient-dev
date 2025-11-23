package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations

abstract class Animation @JvmOverloads constructor(
    var duration: Int,
    private var endPoint: Double,
    protected var direction: Direction = Direction.FORWARDS
) {

    val timerUtil = TimerUtil()

    fun finished(direction: Direction): Boolean = isDone() && this.direction == direction

    val linearOutput: Double
        get() = 1 - timerUtil.getTime() / duration.toDouble() * endPoint

    fun getEndPoint(): Double = endPoint

    fun setEndPoint(endPoint: Double) {
        this.endPoint = endPoint
    }

    fun reset() {
        timerUtil.reset()
    }

    fun isDone(): Boolean = timerUtil.hasTimeElapsed(duration.toLong())

    fun changeDirection() {
        setDirection(direction.opposite())
    }

    fun getDirection(): Direction = direction

    fun setDirection(direction: Direction) {
        if (this.direction != direction) {
            this.direction = direction
            timerUtil.setTime(
                System.currentTimeMillis() - (duration - duration.coerceAtMost(timerUtil.getTime()).toInt())
            )
        }
    }

    fun setDuration(duration: Int) {
        this.duration = duration
    }

    protected open fun correctOutput(): Boolean = false

    open fun getOutput(): Double {
        return if (direction == Direction.FORWARDS) {
            if (isDone()) endPoint else getEquation(timerUtil.getTime().toDouble()) * endPoint
        } else {
            if (isDone()) {
                0.0
            } else if (correctOutput()) {
                val revTime = duration.coerceAtMost((duration - timerUtil.getTime()).coerceAtLeast(0)).toDouble()
                getEquation(revTime) * endPoint
            } else {
                (1 - getEquation(timerUtil.getTime().toDouble())) * endPoint
            }
        }
    }

    protected abstract fun getEquation(x: Double): Double
}
