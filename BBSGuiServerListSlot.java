package net.minecraft.src;

import java.util.List;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class BBSGuiServerListSlot extends GuiSlot {

    private final BBSGuiMultiplayer parent;
    private final List serverList;

    public BBSGuiServerListSlot(BBSGuiMultiplayer parent, Minecraft mc, List serverList, int bottom) {
        super(mc, parent.width, parent.height, 32, bottom, 36);
        this.parent   = parent;
        this.serverList = serverList;
    }

    protected int getSize() {
        return serverList.size();
    }

    protected void elementClicked(int index, boolean doubleClick) {
        parent.selectServer(index);
        if (doubleClick) {
            parent.connectToSelected();
        }
    }

    protected boolean isSelected(int index) {
        return index == parent.getSelectedIndex();
    }

    protected void drawBackground() {
        parent.drawDefaultBackground();
    }

    protected void drawSlot(int index, int x, int y, int height, Tessellator tess) {
        if (index < 0 || index >= serverList.size()) {
            return;
        }
        BBSServerEntry entry = (BBSServerEntry) serverList.get(index);
        FontRenderer fr = parent.getFontRenderer();

        String nameStr = (entry.favorite ? "\u00a7e\u2605 " : "") + entry.name;
        fr.drawStringWithShadow(nameStr, x + 3, y + 1, 0xFFFFFF);

        String addr = entry.getAddress();
        fr.drawStringWithShadow(addr, x + 3, y + 12, 0x999999);

        drawPingAndInfo(entry, x, y, fr);
    }

    private void drawPingAndInfo(BBSServerEntry entry, int x, int y, FontRenderer fr) {
        int infoX = x + 220;

        if (entry.pingState == BBSServerEntry.PING_PENDING) {
            fr.drawStringWithShadow("...", infoX, y + 1, 0xAAAAAA);
            return;
        }

        if (entry.pingState == BBSServerEntry.PING_OFFLINE) {
            fr.drawStringWithShadow("Offline", infoX, y + 1, 0xFF4444);
            return;
        }

        if (entry.pingState == BBSServerEntry.PING_OK) {
            int pingColor;
            if (entry.pingMs < 0) {
                pingColor = 0xAAAAAA;
            } else if (entry.pingMs < 100) {
                pingColor = 0x55FF55;
            } else if (entry.pingMs < 300) {
                pingColor = 0xFFFF55;
            } else {
                pingColor = 0xFF5555;
            }

            String pingStr = entry.pingMs >= 0 ? entry.pingMs + "ms" : "?ms";
            fr.drawStringWithShadow(pingStr, infoX, y + 1, pingColor);

            if (entry.playersOnline >= 0 && entry.playersMax >= 0) {
                String playersStr = entry.playersOnline + "/" + entry.playersMax;
                fr.drawStringWithShadow(playersStr, infoX, y + 12, 0xAAAAAA);
            }

            if (entry.motd != null && entry.motd.length() > 0) {
                String motdDisplay = entry.motd;
                if (motdDisplay.length() > 28) {
                    motdDisplay = motdDisplay.substring(0, 28) + "...";
                }
                fr.drawStringWithShadow(motdDisplay, x + 3, y + 24, 0x888888);
            }
        }
    }
}
