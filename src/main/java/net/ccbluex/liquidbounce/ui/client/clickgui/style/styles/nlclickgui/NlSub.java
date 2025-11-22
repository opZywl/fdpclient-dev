package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.Category;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.EaseInOutQuad;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.SmoothStepAnimation;

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Downward;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NlModule;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Animation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.DecelerateAnimation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil;
import net.ccbluex.liquidbounce.ui.font.Fonts;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui.neverlosecolor;

public class NlSub {

    public int x,y,w,h,y2;

    public Category category;

    public List<NlModule> nlModules = new ArrayList<>();

    public Animation alphaani = new EaseInOutQuad(150,1, Direction.BACKWARDS);


    private float maxScroll = Float.MAX_VALUE, minScroll = 0, rawScroll;

    private float scroll;

    private Animation scrollAnimation = new SmoothStepAnimation(0, 0, Direction.BACKWARDS);



    public NlSub(Category category, int y2){
        this.category = category;
        this.y2 = y2;

        int count=0;

        for (Module holder : FDPClient.moduleManager.getModuleInCategory(category)) {
            nlModules.add(new NlModule(this, holder, count % 2 == 0));
            count++;
        }

    }

    public void draw(int mx, int my){


        alphaani.setDirection(isSelected() ? Direction.FORWARDS : Direction.BACKWARDS);

        if (isSelected()) {
            RoundedUtil.drawRound(x + 7, y + y2 + 8, 76, 15, 2, NeverloseGui.getInstance().getLight() ? new Color(200,200,200,(int) (100 + (155 * alphaani.getOutput()))) :  new Color(8, 48, 70, (int) (100 + (155 * alphaani.getOutput()))));
        }

        Fonts.Nl.Nl_18.Nl_18.drawString(category.getDisplayName(), x + 10, y + y2 + 13, NeverloseGui.getInstance().getLight()? new Color(18,18,19).getRGB() : -1);

        if (isSelected()) {
            double scrolll = getScroll();
            for (NlModule nlModule : nlModules)
            {
                nlModule.scrollY = (int) MathUtil.roundToHalf(scrolll);
            }
            onScroll(40);
            //判断
            maxScroll = Math.max(0,nlModules.get(nlModules.size()-1).y + 50 + nlModules.get(nlModules.size()-1).posy + nlModules.get(nlModules.size()-1).getHeight()) ;
            //

            for (NlModule nlModule : nlModules) {
                nlModule.x = x;
                nlModule.y = y;
                nlModule.w = w;
                nlModule.h = h;

                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                RenderUtil.scissor(x + 90,y + 40,w - 90,h - 40);

                nlModule.draw(mx, my);
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            }

         //   Fonts.SF.SF_30.SF_30.drawString(nlModules.get(nlModules.size() -1).posy + "gg",100,10,-1);
        }


    }


    //滚轮
    public void onScroll(int ms) {
        scroll = (float) (rawScroll - scrollAnimation.getOutput());
        rawScroll += Mouse.getDWheel() / 4f;
        rawScroll = Math.max(Math.min(minScroll, rawScroll), -maxScroll);
        scrollAnimation = new SmoothStepAnimation(ms, rawScroll - scroll, Direction.BACKWARDS);
    }
    public float getScroll() {
        scroll = (float) (rawScroll - scrollAnimation.getOutput());
        return scroll;
    }

    public void keyTyped(char typedChar,int keyCode){
        nlModules.forEach(e -> e.keyTyped(typedChar,keyCode));
    }

    public void released(int mx ,int my,int mb) {
        nlModules.forEach( e -> e.released(mx,my,mb));
    }

    public void click(int mx ,int my,int mb){
        if (isSelected())
        nlModules.forEach(e -> e.click(mx,my,mb));
    }



    public boolean isSelected() {
        return NeverloseGui.getInstance().selectedCategory == this.category;
    }

}
