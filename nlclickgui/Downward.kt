package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui

import net.ccbluex.liquidbounce.config.Value
import net.minecraft.client.gui.Gui

abstract class Downward<V : Value<*>>(var setting: V, var moduleRender: NlModule) : Gui() {

    // O Kotlin já cria automaticamente getX() e setX() para essas variáveis
    var x = 0f
    var y = 0f

    private var width = 0
    private var height = 0

    abstract fun draw(mouseX: Int, mouseY: Int)

    abstract fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int)

    open fun keyTyped(typedChar: Char, keyCode: Int) {}

    abstract fun mouseReleased(mouseX: Int, mouseY: Int, state: Int)

    // REMOVIDO: fun getX(): Float = x (Desnecessário)
    // REMOVIDO: fun getY(): Float = y (Desnecessário)

    // Mantidos pois width/height são privados, então o getter público não é gerado automaticamente
    fun getHeight(): Int = height

    fun getWidth(): Int = width

    // Mantidos pois aceitam Int, enquanto o padrão do Kotlin espera Float.
    // Isso cria uma sobrecarga (overload) útil para compatibilidade.
    fun setX(x: Int) {
        this.x = x.toFloat()
    }

    fun setY(y: Int) {
        this.y = y.toFloat()
    }

    fun getScrollY(): Int = moduleRender.scrollY
}