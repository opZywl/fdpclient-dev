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
import java.util.stream.Collectors;

import static net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui.neverlosecolor;
import static net.ccbluex.liquidbounce.utils.extensions.MathExtensionsKt.roundToHalf;

public class NlSub {

    public int x, y, w, h, y2;

    private final Category parentCategory;
    public Category.SubCategory subCategory;

    public List<NlModule> nlModules = new ArrayList<>();
    private List<NlModule> visibleModules = new ArrayList<>();

    public Animation alphaani = new EaseInOutQuad(150, 1, Direction.BACKWARDS);

    private float maxScroll = Float.MAX_VALUE, minScroll = 0, rawScroll;

    private float scroll;

    private Animation scrollAnimation = new SmoothStepAnimation(0, 0, Direction.BACKWARDS);

    public NlSub(Category parentCategory, Category.SubCategory subCategory, int y2) {
        this.parentCategory = parentCategory;
        this.subCategory = subCategory;
        this.y2 = y2;

        int count = 0;

        for (Module holder : FDPClient.moduleManager) {
            if (holder.getCategory() == parentCategory && holder.getSubCategory() == subCategory) {
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

        Fonts.NlIcon.nlfont_20.getNlfont_20().drawString(getIcon(), x + 10, y + y2 + 14, neverlosecolor.getRGB());

        Fonts.Nl.Nl_18.getNl_18().drawString(subCategory.toString(), x + 10 + Fonts.NlIcon.nlfont_20.getNlfont_20().stringWidth(getIcon()) + 8, y + y2 + 13, NeverloseGui.getInstance().getLight() ? new Color(18, 18, 19).getRGB() : -1);

        if (isSelected() && !(subCategory == Category.SubCategory.CONFIGS)) {
            double scrolll = getScroll();
            visibleModules = getVisibleModules();
            for (NlModule nlModule : visibleModules) {
                nlModule.scrollY = (int) roundToHalf(scrolll);
            }
            onScroll(40);

            if (!visibleModules.isEmpty()) {
                NlModule lastModule = visibleModules.get(visibleModules.size() - 1);
                maxScroll = Math.max(0, lastModule.y + 50 + lastModule.posy + lastModule.getHeight());
            } else {
                maxScroll = 0;
            }

            for (NlModule nlModule : visibleModules) {
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
            NeverloseGui.getInstance().configs.setScroll((int) roundToHalf(scrolll));
            NeverloseGui.getInstance().configs.setBounds(x + 90, y + 40, w - 110);
            onScroll(40);
            maxScroll = Math.max(0, NeverloseGui.getInstance().configs.getContentHeight() - (h - 40));

            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            RenderUtil.scissor(x + 90, y + 40, w - 90, h - 40);
            NeverloseGui.getInstance().configs.draw(mx, my);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }

    }
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
        if (isSelected() && subCategory != Category.SubCategory.CONFIGS) {
            nlModules.forEach(e -> e.click(mx, my, mb));
        }

        if (isSelected() && (subCategory == Category.SubCategory.CONFIGS)){
            NeverloseGui.getInstance().configs.click(mx,my,mb);
        }
    }

    public boolean isSelected() {
        return NeverloseGui.getInstance().selectedSub == this;
    }

    public List<NlModule> getLayoutModules() {
        return visibleModules.isEmpty() && NeverloseGui.getInstance().isSearching() ? visibleModules : (visibleModules.isEmpty() ? nlModules : visibleModules);
    }

    private List<NlModule> getVisibleModules() {
        if (!NeverloseGui.getInstance().isSearching()) {
            return nlModules;
        }
        String query = NeverloseGui.getInstance().getSearchText().toLowerCase();
        return nlModules.stream().filter(module -> module.module.getName().toLowerCase().contains(query)).collect(Collectors.toList());
    }

    private String getIcon() {
        return subCategory.getIcon();
    }
}