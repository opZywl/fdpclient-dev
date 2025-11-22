package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings;

import net.ccbluex.liquidbounce.config.BoolValue;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Downward;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NlModule;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Animation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.DecelerateAnimation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil;
import net.ccbluex.liquidbounce.ui.font.Fonts;

import java.awt.*;

import static net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui.neverlosecolor;


public class BoolSetting extends Downward<BoolValue> {

    public BoolSetting(BoolValue s, NlModule moduleRender) {
        super(s, moduleRender);
    }

    public Animation toggleAnimation = new DecelerateAnimation(225, 1, Direction.BACKWARDS);

    public Animation HoveringAnimation = new DecelerateAnimation(225, 1, Direction.BACKWARDS);


    @Override
    public void draw(int mouseX, int mouseY) {
        int mainx = NeverloseGui.getInstance().x;
        int mainy = NeverloseGui.getInstance().y;

        int booly = (int) (getY() + getScrollY());

        Fonts.Nl_16.drawString(setting.getName(),mainx + 100 + getX(),mainy + booly + 57,NeverloseGui.getInstance().getLight() ? new Color(95,95,95).getRGB() :-1 );

        Color darkRectColor = new Color(29, 29, 39, 255);

        Color darkRectHover = RenderUtil.brighter(darkRectColor, .8f);

        Color accentCircle =  RenderUtil.darker(neverlosecolor, .5f);

        toggleAnimation.setDirection(setting.get()? Direction.FORWARDS : Direction.BACKWARDS);

        HoveringAnimation.setDirection(RenderUtil.isHovering(NeverloseGui.getInstance().x + 265 - 32 + getX(), NeverloseGui.getInstance().y + (int) (getY() + getScrollY()) + 57, 16, 4.5f,mouseX,mouseY)? Direction.FORWARDS : Direction.BACKWARDS);


        //背景
        RoundedUtil.drawRound(mainx + 265 - 32 + getX(),mainy + booly + 57, 16, 4.5f,
                2,NeverloseGui.getInstance().getLight()? RenderUtil.interpolateColorC(new Color(230,230,230), new Color(0,112,186), (float) toggleAnimation.getOutput()) : RenderUtil.interpolateColorC(RenderUtil.applyOpacity(darkRectHover, .5f), accentCircle, (float) toggleAnimation.getOutput()));

        //Glow
        RenderUtil.fakeCircleGlow((float) (mainx + 265 + 3 - 32 + getX() +( (11)* toggleAnimation.getOutput())),
                mainy + booly + 57 + 2 , 6, Color.BLACK, .3f);

        RenderUtil.resetColor();

        //画圆
        RoundedUtil.drawRound((float) (mainx + 265 - 32 + getX() +( (11)* toggleAnimation.getOutput())),
                mainy + booly + 57 -1, 6.5f,
                6.5f, 3, setting.get() ?  neverlosecolor : NeverloseGui.getInstance().getLight() ? new Color(255,255,255) : new Color((int) (68 - (28 * HoveringAnimation.getOutput())), (int) (82 + (44 * HoveringAnimation.getOutput())), (int) (87 +( 83 * HoveringAnimation.getOutput()))));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton ==0 ){
            if (RenderUtil.isHovering(NeverloseGui.getInstance().x + 265 - 32 + getX(), NeverloseGui.getInstance().y + (int) (getY() + getScrollY()) + 57, 16, 4.5f,mouseX,mouseY)){
                setting.set(!setting.get());
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }
}
