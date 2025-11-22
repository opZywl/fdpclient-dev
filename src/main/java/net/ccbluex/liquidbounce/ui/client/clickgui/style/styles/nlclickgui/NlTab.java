package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui;

import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings.BoolSetting;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings.Numbersetting;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction;
import net.ccbluex.liquidbounce.ui.font.Fonts;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

//单纯过渡使代码更加清晰
public class NlTab {

    public Module.Category type;

    public int x,y,w,h,y2;

    public List<NlSub> nlSubList = new ArrayList<>();



    public NlTab(Module.Category type,int y2){
        this.type = type;
        this.y2 = y2;

        int y3 = 0;

        for (Module.Category.SubCategory subCategory: type.getSubCategories()){
            nlSubList.add( new NlSub(subCategory,y2 + y3));
            y3 += 18;
        }
    }

    public void draw(int mx, int my){

        Fonts.Nl_16.drawString(type.name(),x + 10,y  + y2,  Client.instance.neverloseGui.getLight() ? new Color(194,196,198).getRGB() : new Color(66,64,62).getRGB());

        for (NlSub nlSub : nlSubList){
            nlSub.x = x;
            nlSub.y = y;
            nlSub.w = w;
            nlSub.h = h;

            //重置拉条动画
            if (!nlSub.isSelected()){
                for (NlModule nlModule : nlSub.nlModules){
                    for (Downward nlSetting : nlModule.downwards){
                        if (nlSetting instanceof Numbersetting){
                            ((Numbersetting) nlSetting).percent = 0;
                        }
                        if (nlSetting instanceof BoolSetting){
                            if (((BoolSetting) nlSetting).toggleAnimation.getDirection().equals(Direction.FORWARDS))
                            ((BoolSetting) nlSetting).toggleAnimation.reset();;
                        }
                    }
                    if (nlModule.toggleAnimation.getDirection().equals(Direction.FORWARDS))
                    nlModule.toggleAnimation.reset();
                }
            }

            nlSub.draw(mx,my);
        }

    }
    public void keyTyped(char typedChar,int keyCode){
        nlSubList.forEach(e -> e.keyTyped(typedChar,keyCode));
    }


    public void released(int mx ,int my,int mb) {
        nlSubList.forEach( e -> e.released(mx,my,mb));
    }
    public void click(int mx ,int my,int mb) {

        nlSubList.forEach( e -> e.click(mx,my,mb));

        if (mb == 0) {
            //选择面板
            for (NlSub categoryRender : nlSubList) {
                if (RenderUtil.isHovering(categoryRender.x + 7, categoryRender.y + categoryRender.y2 + 8, 76, 15,mx,my)) {
                    Client.instance.neverloseGui.subCategory = categoryRender.subCategory;
                }
            }

        }
    }

}
