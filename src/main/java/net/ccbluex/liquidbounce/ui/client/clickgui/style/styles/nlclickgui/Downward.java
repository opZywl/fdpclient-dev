package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui;

import net.ccbluex.liquidbounce.config.Value;
import net.minecraft.client.gui.Gui;

public abstract class Downward<V extends Value> extends Gui{

    public V setting;

    private float x, y;
    private int width, height;

    public NlModule moduleRender;

    public Downward(V s, NlModule moduleRender) {
        this.setting = s;
        this.moduleRender = moduleRender;
    }

    public abstract void draw(int mouseX, int mouseY);

    public abstract void mouseClicked(int mouseX, int mouseY, int mouseButton);

    public void keyTyped(char typedChar,int keyCode){

    }

    public abstract void mouseReleased(int mouseX, int mouseY, int state);


    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    //滚轮
    public int getScrollY() {
        return moduleRender.scrollY;
    }
}

