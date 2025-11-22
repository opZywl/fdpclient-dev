package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui;

import cn.distance.ui.cfont.impl.Fonts;
import net.ccbluex.liquidbounce.config.Value;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings.BoolSetting;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings.ColorSetting;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings.Numbersetting;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings.StringsSetting;

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

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui.neverlosecolor;

public class NlModule {

    public int x,y,w,h;

    public NlSub NlSub;

    public Module module;

    public int leftAdd,rightAdd;


    public int posx, posy;

    public int height = 0;

    public List<Downward> downwards = new ArrayList<>();

    public int scrollY;

    public Animation toggleAnimation = new DecelerateAnimation(225, 1, Direction.BACKWARDS);

    public Animation HoveringAnimation = new DecelerateAnimation(225, 1, Direction.BACKWARDS);


    public boolean lef ;

    public NlModule(NlSub sub , Module module,boolean lef){
        this.NlSub = sub;
        this.module = module;
        this.lef = lef;
        this.posx = lef ? 0 : 170;
        //临时变量计算value

        for(Value setting : module.getValues()) {
            if(setting instanceof Option){
                this.downwards.add(new BoolSetting((Option) setting,this));
            }
            if(setting instanceof Numbers){
                this.downwards.add(new Numbersetting((Numbers) setting,this));
            }
            if(setting instanceof Mode){
                this.downwards.add(new StringsSetting((Mode) setting ,this));
            }
            if (setting instanceof ColorSetting){
                this.downwards.add(new cn.distance.ui.clickguis.nlclickgui.Settings.ColorSetting((ColorSetting) setting,this));
            }
        }
    }

    public int getHeight() {
        //获取背景的height
        int h = 20;
        for (Value s : module.getValues().stream().filter(Value::getDisplayableFunc).collect(Collectors.toList())){
            h += 20;
        }
        if (module.getValues().isEmpty())
        {
            h += 20;
        }
        return h;
    }


    public int getY() {

        leftAdd = 0;
        rightAdd =0;

        for (NlModule tabModule : NlSub.nlModules) {
            if (tabModule == this) {
                break;
            } else {
                if (tabModule.lef){
                    //10 间隔
                    leftAdd += tabModule.getHeight() + 10 ;
                }else {
                    rightAdd += tabModule.getHeight() + 10;
                }

            }
        }

        return lef ? leftAdd : rightAdd;
    }


    //左+ 0 右+ 180
    public void draw(int mx, int my){

        posy = getY();

        RoundedUtil.drawRound(x + 95 + posx,y + 50 + posy + scrollY,160,getHeight(),2, NeverloseGui.getInstance().getLight() ? new Color(245,245,245) : new Color(3,13,26));

        Fonts.Nl.Nl_18.Nl_18.drawString(module.getName(),x + 100 + posx,y + posy + 55 + scrollY,NeverloseGui.getInstance().getLight() ? new Color(95,95,95).getRGB() : -1);

        RoundedUtil.drawRound(x + 100 + posx,y + 65 + posy + scrollY,150,0.7f,0, NeverloseGui.getInstance().getLight() ? new Color(213,213,213) : new Color(9,21,34));

        HoveringAnimation.setDirection(RenderUtil.isHovering(x + 265 - 32 + posx,y + posy + scrollY + 56, 16, 4.5f,mx,my) ? Direction.FORWARDS : Direction.BACKWARDS );

        //value的高度
        int cheigt = 20;
        for (Downward downward : downwards.stream().filter(s -> s.setting.getDisplayableFunc()).collect(Collectors.toList())){
            downward.setX(posx);
            downward.setY(getY() + cheigt);
            cheigt += 20;

            downward.draw(mx,my);
        }
       // RoundedUtil.drawRound(x + 120 ,y + 50,170,50,2,new Color(3,13,26));

        rendertoggle();

        if (module.getValues().isEmpty()) {
            Fonts.Nl.Nl_22.Nl_22.drawString("No Settings.", x + 100 + posx, y + posy + scrollY + 72, NeverloseGui.getInstance().getLight() ? new Color(95,95,95).getRGB() :-1);
        }
    }

    //95,95,95

    public void rendertoggle(){
        Color darkRectColor =  new Color(29, 29, 39, 255);

        Color darkRectHover = RenderUtil.brighter(darkRectColor, .8f);

        Color accentCircle =  RenderUtil.darker(neverlosecolor, .5f);

        toggleAnimation.setDirection(module.isEnabled()? Direction.FORWARDS : Direction.BACKWARDS);

        //背景
        RoundedUtil.drawRound(x + 265 - 32 + posx,y + posy + scrollY + 56, 16, 4.5f,
                2, RenderUtil.interpolateColorC(RenderUtil.applyOpacity(darkRectHover, .5f), accentCircle, (float) toggleAnimation.getOutput()));

        //Glow
        RenderUtil.fakeCircleGlow((float) (x + 265 + 3 - 32 + posx +( (11)* toggleAnimation.getOutput())),
                y + posy + scrollY + 56 + 2 , 6, Color.BLACK, .3f);

        RenderUtil.resetColor();

        //画圆
        RoundedUtil.drawRound((float) (x + 265 - 32 + posx +( (11)* toggleAnimation.getOutput())),
                y + posy + scrollY + 56 -1, 6.5f,
                6.5f, 3, module.isEnabled()?  neverlosecolor : NeverloseGui.getInstance().getLight() ? new Color(255,255,255) : new Color((int) (68 - (28 * HoveringAnimation.getOutput())), (int) (82 + (44 * HoveringAnimation.getOutput())), (int) (87 +( 83 * HoveringAnimation.getOutput()))));
    }

    public void keyTyped(char typedChar,int keyCode){
        downwards.forEach( e -> e.keyTyped(typedChar,keyCode));
    }

    public void released(int mx ,int my,int mb) {
        downwards.stream().filter(e -> e.setting.getDisplayableFunc()).forEach(e -> e.mouseReleased(mx,my,mb));
    }

    public void click(int mx ,int my,int mb){
        downwards.stream().filter(e -> e.setting.getDisplayableFunc()).forEach(e -> e.mouseClicked(mx,my,mb));

        if (RenderUtil.isHovering(x + 265 - 32 + posx,y + posy + scrollY + 56, 16, 4.5f,mx,my) && mb == 0){
            module.toggle();
        }
    }

}
