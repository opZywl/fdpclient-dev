package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui;

import cn.distance.Distance;
import cn.distance.module.Module;
import cn.distance.module.Render.ClickGui;
import cn.distance.ui.cfont.impl.Fonts;
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

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui.neverlosecolor;

public class NlSub {

    public int x,y,w,h,y2;

    public Module.Category.SubCategory subCategory;

    public List<NlModule> nlModules = new ArrayList<>();

    public Animation alphaani = new EaseInOutQuad(150,1, Direction.BACKWARDS);


    private float maxScroll = Float.MAX_VALUE, minScroll = 0, rawScroll;

    private float scroll;

    private Animation scrollAnimation = new SmoothStepAnimation(0, 0, Direction.BACKWARDS);



    public NlSub(Module.Category.SubCategory subCategory, int y2){
        this.subCategory = subCategory;
        this.y2 = y2;

        int count=0;

        for (Module holder : Distance.instance.moduleManager.moduleList) {
            if (holder.getSubCategory().equals(subCategory)) {
                nlModules.add(new NlModule(this,holder,count %2 ==0));
                count++;
            }
        }

    }

    public void draw(int mx, int my){


        alphaani.setDirection(isSelected() ? Direction.FORWARDS : Direction.BACKWARDS);

        if (isSelected()) {
            RoundedUtil.drawRound(x + 7, y + y2 + 8, 76, 15, 2, NeverloseGui.getInstance().getLight() ? new Color(200,200,200,(int) (100 + (155 * alphaani.getOutput()))) :  new Color(8, 48, 70, (int) (100 + (155 * alphaani.getOutput()))));
        }

        if (subCategory.getIcon().equalsIgnoreCase("g") || subCategory.getIcon().equalsIgnoreCase("f")){
            Fonts.NlIcon.nlfont_20.nlfont_20.drawString(subCategory.getIcon(),x + 10,y  + y2 + 14,neverlosecolor.getRGB());

            Fonts.Nl.Nl_18.Nl_18.drawString(subCategory.toString(),x + 10 + Fonts.NlIcon.nlfont_20.nlfont_20.stringWidth(subCategory.getIcon()) + 8,y  + y2 + 13, NeverloseGui.getInstance().getLight()? new Color(18,18,19).getRGB() : -1);

        }else {
            Fonts.NlIcon.nlfont_18.nlfont_18.drawString(subCategory.getIcon(),x + 10,y  + y2 + 14,neverlosecolor.getRGB());

            Fonts.Nl.Nl_18.Nl_18.drawString(subCategory.toString(),x + 10 + Fonts.NlIcon.nlfont_18.nlfont_18.stringWidth(subCategory.getIcon()) + 8,y  + y2 + 13, NeverloseGui.getInstance().getLight()? new Color(18,18,19).getRGB() : -1);
        }

      //  if (subCategory == Module.Category.SubCategory.Configs)return;

        if (isSelected() && !(subCategory == Module.Category.SubCategory.Configs)) {
            double scrolll = getScroll();
            for (NlModule nlModule : nlModules)
            {
                nlModule.scrollY = (int) MathUtil.roundToHalf(scrolll);
            }
            ClickGui clickGui = (ClickGui)Distance.instance.moduleManager.getModuleByClass(ClickGui.class);
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

        if (isSelected() && (subCategory == Module.Category.SubCategory.Configs)){
            double scrolll = getScroll();

            NeverloseGui.getInstance().configs.scy = (int) MathUtil.roundToHalf(scrolll);

            ClickGui clickGui = (ClickGui)Distance.instance.moduleManager.getModuleByClass(ClickGui.class);
            onScroll(clickGui.Scroll.get());
            //判断
            maxScroll = Math.max(0,NeverloseGui.getInstance().configs.getYY()) ;
            //

            int x2 = 0,i=0;
            for (Module.Category type : Module.Category.values()){
                if(type.name().equalsIgnoreCase("World") ||
                        type.name().equalsIgnoreCase("Interface") ||
                        type.name().equalsIgnoreCase("Config")) continue;

                String l = "";
                if (type.name().equalsIgnoreCase("Combat")) {
                    l = "D";
                } else if (type.name().equalsIgnoreCase("Movement")) {
                    l = "A";
                } else if (type.name().equalsIgnoreCase("Player")) {
                    l = "B";
                } else if (type.name().equalsIgnoreCase("Render")) {
                    l = "C";
                }

                RoundedUtil.drawRoundOutline(x + 170 + x2, NeverloseGui.getInstance().y + 13,15,15,1,0.1f, NeverloseGui.getInstance().configs.loads[i] ? new Color(10,122,182) : new Color(15,15,19), NeverloseGui.getInstance().configs.loads[i] ? new Color(10,122,182) :new Color(22,22,24));

                Fonts.ICONFONT.ICONFONT_17.ICONFONT_17.drawString(l,x + 173.5f + x2, NeverloseGui.getInstance().y + 19,-1);

                x2 += 20 ;
                i++;
            }




            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            RenderUtil.scissor(x + 90,y + 40,w - 90,h - 40);
            NeverloseGui.getInstance().configs.posx = x;
            NeverloseGui.getInstance().configs.posy = y;
            NeverloseGui.getInstance().configs.draw(mx,my);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
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

        if (isSelected() && (subCategory == Module.Category.SubCategory.Configs)){
            int x2 = 0,i=0;
            for (Module.Category type : Module.Category.values()){
                if(type.name().equalsIgnoreCase("World") ||
                        type.name().equalsIgnoreCase("Interface") ||
                        type.name().equalsIgnoreCase("Config")) continue;

                if (RenderUtil.isHovering(x + 170 + x2, NeverloseGui.getInstance().y + 13,15,15,mx,my)) {
                    NeverloseGui.getInstance().configs.loads[i] = !NeverloseGui.getInstance().configs.loads[i];
                }


                x2 += 20 ;
                i++;
            }

            NeverloseGui.getInstance().configs.click(mx,my,mb);
        }
    }



    public boolean isSelected() {
        return NeverloseGui.getInstance().subCategory == this.subCategory;
    }

}
