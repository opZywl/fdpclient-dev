package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.Category;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Animation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.DecelerateAnimation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.EaseInOutQuad;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.SmoothStepAnimation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui.neverlosecolor;

public class NlSub {

    public int x, y, w, h, y2;

    public Category.SubCategory subCategory;

    public List<NlModule> nlModules = new ArrayList<>();

    public Animation alphaani = new EaseInOutQuad(150, 1, Direction.BACKWARDS);

    private float maxScroll = 0, minScroll = 0, rawScroll;

    private float scroll;

    private Animation scrollAnimation = new SmoothStepAnimation(0, 0, Direction.BACKWARDS);

    public NlSub(Category.SubCategory subCategory, int y2) {
        this.subCategory = subCategory;
        this.y2 = y2;

        int count = 0;

        for (Module holder : FDPClient.moduleManager) {
            if (holder.getSubCategory() == subCategory) {
                nlModules.add(new NlModule(this, holder, count % 2 == 0));
                count++;
            }
        }

    }

    public void draw(int mx, int my) {


        alphaani.setDirection(isSelected() ? Direction.FORWARDS : Direction.BACKWARDS);

        if (isSelected()) {
            RoundedUtil.drawRound(x + 7, y + y2 + 8, 76, 15, 2, NeverloseGui.getInstance().getLight() ? new Color(200, 200, 200, (int) (100 + (155 * alphaani.getOutput()))) : new Color(8, 48, 70, (int) (100 + (155 * alphaani.getOutput()))));
        }

        Fonts.NlIcon.getNlfont_20().getNlfont_20().drawString(subCategory.getIcon(), x + 10, y + y2 + 14, neverlosecolor.getRGB());

        Fonts.Nl.getNl_18().getNl_18().drawString(subCategory.toString(), x + 10 + Fonts.NlIcon.getNlfont_20().getNlfont_20().stringWidth(subCategory.getIcon()) + 8, y + y2 + 13, NeverloseGui.getInstance().getLight() ? new Color(18, 18, 19).getRGB() : -1);

        if (isSelected() && !(subCategory == Category.SubCategory.CONFIGS)) {
            if (nlModules.isEmpty()) {
                maxScroll = 0;
                rawScroll = Math.max(Math.min(minScroll, rawScroll), -maxScroll);
                return;
            }

            double scrolll = getScroll();
            for (NlModule nlModule : nlModules) {
                nlModule.scrollY = (int) MathUtil.roundToHalf(scrolll);
            }
            onScroll(40);

            int contentBottom = nlModules.get(nlModules.size() - 1).y + 50 + nlModules.get(nlModules.size() - 1).posy + nlModules.get(nlModules.size() - 1).getHeight();
            int visibleHeight = h - 40;
            int contentHeight = contentBottom - (y + 40);
            maxScroll = Math.max(0, contentHeight - visibleHeight);

            for (NlModule nlModule : nlModules) {
                nlModule.x = x;
                nlModule.y = y;
                nlModule.w = w;
                nlModule.h = h;

                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                RenderUtil.scissor(x + 90, y + 40, w - 90, h - 40);

                nlModule.draw(mx, my);
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            }

        }

        if (isSelected() && (subCategory == Category.SubCategory.CONFIGS)) {
            double scrolll = getScroll();

            NeverloseGui.getInstance().configs.scy = (int) MathUtil.roundToHalf(scrolll);
            onScroll(40);

            int contentHeight = NeverloseGui.getInstance().configs.getYY() - (y + 40);
            int visibleHeight = h - 40;
            maxScroll = Math.max(0, contentHeight - visibleHeight);

            int x2 = 0, i = 0;
            for (Category type : Category.values()) {
                if (type.name().equalsIgnoreCase("World") ||
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

                RoundedUtil.drawRoundOutline(x + 170 + x2, NeverloseGui.getInstance().y + 13, 15, 15, 1, 0.1f, NeverloseGui.getInstance().configs.loads[i] ? new Color(10, 122, 182) : new Color(15, 15, 19), NeverloseGui.getInstance().configs.loads[i] ? new Color(10, 122, 182) : new Color(22, 22, 24));

                Fonts.ICONFONT.ICONFONT_17.ICONFONT_17.drawString(l, x + 173.5f + x2, NeverloseGui.getInstance().y + 19, -1);

                x2 += 20;
                i++;
            }



            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            RenderUtil.scissor(x + 90, y + 40, w - 90, h - 40);
            NeverloseGui.getInstance().configs.posx = x;
            NeverloseGui.getInstance().configs.posy = y;
            NeverloseGui.getInstance().configs.draw(mx, my);
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

        if (isSelected() && (subCategory == Category.SubCategory.CONFIGS)){
            int x2 = 0,i=0;
            for (Category type : Category.values()){
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
