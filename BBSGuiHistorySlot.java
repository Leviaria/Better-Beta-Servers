package net.minecraft.src;

import java.util.List;
import net.minecraft.client.Minecraft;

public class BBSGuiHistorySlot extends GuiSlot {

    private final BBSGuiDirectConnect parent;

    public BBSGuiHistorySlot(BBSGuiDirectConnect parent, Minecraft mc, int top, int bottom) {
        super(mc, parent.width, parent.height, top, bottom, 20);
        this.parent = parent;
    }

    protected int getSize() {
        return parent.getHistory().size();
    }

    protected void elementClicked(int index, boolean doubleClick) {
        parent.selectHistory(index);
    }

    protected boolean isSelected(int index) {
        return index == parent.getSelectedHistory();
    }

    protected void drawBackground() {
    }

    protected void drawSlot(int index, int x, int y, int height, Tessellator tess) {
        List hist = parent.getHistory();
        if (index < 0 || index >= hist.size()) return;
        BBSServerEntry e = (BBSServerEntry) hist.get(index);
        FontRenderer fr = parent.getFontRenderer();
        fr.drawStringWithShadow(e.getAddress(), x + 3, y + 4, 0xCCCCCC);
    }
}
