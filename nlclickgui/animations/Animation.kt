package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.normal.TimerUtil

abstract class Animation @JvmOverloads constructor(
    var duration: Int,
    var endPoint: Double, // Mudado para 'var' público para substituir get/setEndPoint manuais
    direction: Direction = Direction.FORWARDS // Removido 'var' do construtor para definir a lógica no corpo da classe
) {

    val timerUtil = TimerUtil()

    // A lógica de setDirection agora fica aqui dentro
    var direction: Direction = direction
        set(value) {
            if (field != value) {
                field = value
                timerUtil.setTime(
                    System.currentTimeMillis() - (duration - duration.coerceAtMost(timerUtil.getTime().toInt()).toInt())
                )
            }
        }

    fun finished(direction: Direction): Boolean = isDone() && this.direction == direction

    val linearOutput: Double
        get() = 1 - timerUtil.getTime() / duration.toDouble() * endPoint

    fun reset() {
        timerUtil.reset()
    }

    fun isDone(): Boolean = timerUtil.hasTimeElapsed(duration.toLong())

    fun changeDirection() {
        // Ao atribuir valor aqui, o Kotlin chama automaticamente o bloco 'set' definido acima
        direction = direction.opposite()
    }

    // Removidas as funções manuais getDirection, setDirection, getEndPoint, setEndPoint, setDuration
    // para corrigir os conflitos de assinatura da JVM.

    protected open fun correctOutput(): Boolean = false

    open fun getOutput(): Double {
        return if (direction == Direction.FORWARDS) {
            if (isDone()) endPoint else getEquation(timerUtil.getTime().toDouble()) * endPoint
        } else {
            if (isDone()) {
                0.0
            } else if (correctOutput()) {
                val revTime = duration.coerceAtMost((duration - timerUtil.getTime()).coerceAtLeast(0).toInt()).toDouble()
                getEquation(revTime) * endPoint
            } else {
                (1 - getEquation(timerUtil.getTime().toDouble())) * endPoint
            }
        }
    }

    protected abstract fun getEquation(x: Double): Double
}