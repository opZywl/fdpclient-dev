package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui;

import net.ccbluex.liquidbounce.config.BoolValue;
import net.ccbluex.liquidbounce.config.ColorValue;
import net.ccbluex.liquidbounce.config.FloatValue;
import net.ccbluex.liquidbounce.config.IntValue;
import net.ccbluex.liquidbounce.config.ListValue;
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
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Animation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.DecelerateAnimation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil;
import net.ccbluex.liquidbounce.ui.font.Fonts;

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

    private int cardX;
    private int cardWidth;

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
        for(Value setting : module.getValues()) {
            if(setting instanceof BoolValue){
                this.downwards.add(new BoolSetting((BoolValue) setting,this));
            }
            if(setting instanceof FloatValue || setting instanceof IntValue){
                this.downwards.add(new Numbersetting(setting,this));
            }
            if(setting instanceof ListValue){
                this.downwards.add(new StringsSetting((ListValue) setting ,this));
            }
            if (setting instanceof ColorValue){
                this.downwards.add(new ColorSetting((ColorValue) setting,this));
            }
        }
    }

    public int getHeight() {
        int h = 20;
        for (Value s : module.getValues().stream().filter(Value::shouldRender).collect(Collectors.toList())){
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

        for (NlModule tabModule : NlSub.getLayoutModules()) {
            if (tabModule == this) {
                break;
            } else {
                if (tabModule.lef){
                    leftAdd += tabModule.getHeight() + 18 ;
                }else {
                    rightAdd += tabModule.getHeight() + 18;
                }

            }
        }

        return lef ? leftAdd : rightAdd;
    }

    public void draw(int mx, int my){

        int contentWidth = w - 120;
        int columnSpacing = 18;
        int columnWidth = (contentWidth - columnSpacing) / 2;

        posx = lef ? 0 : columnWidth + columnSpacing;
        posy = getY();

        cardWidth = columnWidth;
        cardX = x + 95 + posx;

        int toggleX = cardX + cardWidth - 35;
        int toggleY = y + NeverloseGui.getInstance().getContentTopOffset() + posy + scrollY + 6;

        int cardY = y + NeverloseGui.getInstance().getContentTopOffset() + posy + scrollY;

        RoundedUtil.drawRound(cardX, cardY,cardWidth,getHeight(),2, NeverloseGui.getInstance().getLight() ? new Color(245,245,245) : new Color(3,13,26));

        Fonts.Nl.Nl_18.getNl_18().drawString(module.getName(),cardX + 5, cardY + 5,NeverloseGui.getInstance().getLight() ? new Color(95,95,95).getRGB() : -1);

        RoundedUtil.drawRound(cardX + 5, cardY + 15,cardWidth - 10,0.7f,0, NeverloseGui.getInstance().getLight() ? new Color(213,213,213) : new Color(9,21,34));

        HoveringAnimation.setDirection(RenderUtil.isHovering(toggleX,toggleY, 16, 4.5f,mx,my) ? Direction.FORWARDS : Direction.BACKWARDS );

        int cheigt = 20;
        for (Downward downward : downwards.stream().filter(s -> s.setting.shouldRender()).collect(Collectors.toList())){
            downward.setX(posx);
            downward.setY(getY() + cheigt);
            cheigt += 20;

            downward.draw(mx,my);
        }
        rendertoggle();

        if (module.getValues().isEmpty()) {
            Fonts.Nl.Nl_22.getNl_22().drawString("No Settings.", cardX + 5, cardY + 22, NeverloseGui.getInstance().getLight() ? new Color(95,95,95).getRGB() :-1);
        }
    }

    public void rendertoggle(){
        Color darkRectColor =  new Color(29, 29, 39, 255);

        Color darkRectHover = RenderUtil.brighter(darkRectColor, .8f);

        Color accentCircle =  RenderUtil.darker(neverlosecolor, .5f);

        toggleAnimation.setDirection(module.getState()? Direction.FORWARDS : Direction.BACKWARDS);

        int toggleX = getToggleX();
        int toggleY = getToggleY();

        RoundedUtil.drawRound(toggleX, toggleY, 16, 4.5f,
                2, RenderUtil.interpolateColorC(RenderUtil.applyOpacity(darkRectHover, .5f), accentCircle, (float) toggleAnimation.getOutput()));

        RenderUtil.fakeCircleGlow((float) (toggleX + 3 + ((11)* toggleAnimation.getOutput())),
                toggleY + 2 , 6, Color.BLACK, .3f);

        RenderUtil.resetColor();

        RoundedUtil.drawRound((float) (toggleX + ((11)* toggleAnimation.getOutput())),
                toggleY -1, 6.5f,
                6.5f, 3, module.getState()?  neverlosecolor : NeverloseGui.getInstance().getLight() ? new Color(255,255,255) : new Color((int) (68 - (28 * HoveringAnimation.getOutput())), (int) (82 + (44 * HoveringAnimation.getOutput())), (int) (87 +( 83 * HoveringAnimation.getOutput()))));
    }

    public int getToggleX() {
        return cardX + cardWidth - 35;
    }

    public int getToggleY() {
        return y + NeverloseGui.getInstance().getContentTopOffset() + posy + scrollY + 6;
    }

    public void keyTyped(char typedChar,int keyCode){
        downwards.forEach( e -> e.keyTyped(typedChar,keyCode));
    }

    public void released(int mx ,int my,int mb) {
        downwards.stream().filter(e -> e.setting.shouldRender()).forEach(e -> e.mouseReleased(mx,my,mb));
    }

    public void click(int mx ,int my,int mb){
        downwards.stream().filter(e -> e.setting.shouldRender()).forEach(e -> e.mouseClicked(mx,my,mb));

        if (RenderUtil.isHovering(getToggleX(), getToggleY(), 16, 4.5f,mx,my) && mb == 0){
            module.toggle();
        }
    }

}