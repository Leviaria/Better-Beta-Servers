package net.minecraft.src;

import java.util.List;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class BBSGuiDirectConnect extends GuiScreen {

    private final BBSGuiMultiplayer parent;
    private final BBSServerList serverList;

    private GuiTextField fieldAddress;
    private GuiButton btnConnect;
    private GuiButton btnCancel;
    private GuiButton btnSave;

    private int selectedHistory = -1;
    private BBSGuiHistorySlot historySlot;

    public BBSGuiDirectConnect(BBSGuiMultiplayer parent, BBSServerList serverList) {
        this.parent = parent;
        this.serverList = serverList;
    }

    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        controlList.clear();

        int cx = width / 2;
        int fieldY = height / 4 + 10;

        String lastAddr = "";
        List hist = serverList.getHistory();
        if (!hist.isEmpty()) {
            BBSServerEntry last = (BBSServerEntry) hist.get(0);
            lastAddr = last.getAddress();
        } else {
            String ls = mc.gameSettings.lastServer.replaceAll("_", ":");
            if (ls.length() > 0) lastAddr = ls;
        }

        fieldAddress = new GuiTextField(this, fontRenderer, cx - 100, fieldY, 200, 20, lastAddr);
        fieldAddress.setMaxStringLength(128);
        fieldAddress.isFocused = true;

        btnConnect = new GuiButton(0, cx - 100, fieldY + 28, 95, 20, "Connect");
        btnCancel  = new GuiButton(1, cx + 5,   fieldY + 28, 95, 20, "Cancel");
        btnSave    = new GuiButton(2, cx - 100, fieldY + 52, 200, 20, "Save to Server List");

        controlList.add(btnConnect);
        controlList.add(btnCancel);
        controlList.add(btnSave);

        int histTop = fieldY + 80;
        historySlot = new BBSGuiHistorySlot(this, mc, histTop, height - 28);

        updateConnectButton();
    }

    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    public void updateScreen() {
        fieldAddress.updateCursorCounter();
    }

    private void updateConnectButton() {
        btnConnect.enabled = fieldAddress.getText().trim().length() > 0;
        btnSave.enabled = fieldAddress.getText().trim().length() > 0;
    }

    protected void actionPerformed(GuiButton btn) {
        if (!btn.enabled) return;

        if (btn.id == 1) {
            mc.displayGuiScreen(parent);
            return;
        }

        String addr = fieldAddress.getText().trim();
        String[] parsed = parseAddress(addr);
        String host = parsed[0];
        int port = Integer.parseInt(parsed[1]);

        if (btn.id == 0) {
            serverList.pushHistory(host, port);
            mc.gameSettings.lastServer = (host + ":" + port).replaceAll(":", "_");
            mc.gameSettings.saveOptions();
            mc.displayGuiScreen(new GuiConnecting(mc, host, port));
        } else if (btn.id == 2) {
            String name = host + (port != 25565 ? ":" + port : "");
            serverList.addServer(new BBSServerEntry(name, host, port, false));
            parent.reloadList();
            mc.displayGuiScreen(parent);
        } else if (btn.id == 10) {
            List hist = serverList.getHistory();
            if (selectedHistory >= 0 && selectedHistory < hist.size()) {
                BBSServerEntry e = (BBSServerEntry) hist.get(selectedHistory);
                fieldAddress.setText(e.getAddress());
                updateConnectButton();
            }
        } else if (btn.id == 11) {
            List hist = serverList.getHistory();
            if (selectedHistory >= 0 && selectedHistory < hist.size()) {
                copyToClipboard(((BBSServerEntry) hist.get(selectedHistory)).getAddress());
            }
        }
    }

    void selectHistory(int index) {
        selectedHistory = index;
        List hist = serverList.getHistory();
        if (index >= 0 && index < hist.size()) {
            BBSServerEntry e = (BBSServerEntry) hist.get(index);
            fieldAddress.setText(e.getAddress());
            updateConnectButton();
        }
    }

    int getSelectedHistory() {
        return selectedHistory;
    }

    List getHistory() {
        return serverList.getHistory();
    }

    public FontRenderer getFontRenderer() {
        return fontRenderer;
    }

    private void copyToClipboard(String text) {
        try {
            java.awt.datatransfer.StringSelection sel = new java.awt.datatransfer.StringSelection(text);
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
        } catch (Exception e) {
            System.out.println("[BBS] Clipboard copy failed");
        }
    }

    private String[] parseAddress(String addr) {
        if (addr.startsWith("[")) {
            int closeBracket = addr.indexOf("]");
            if (closeBracket > 0) {
                String host = addr.substring(1, closeBracket);
                String rest = addr.substring(closeBracket + 1).trim();
                if (rest.startsWith(":") && rest.length() > 1) {
                    return new String[]{host, safeParsePort(rest.substring(1))};
                }
                return new String[]{host, "25565"};
            }
        }
        String[] parts = addr.split(":");
        if (parts.length == 2) {
            return new String[]{parts[0].trim(), safeParsePort(parts[1].trim())};
        }
        return new String[]{addr, "25565"};
    }

    private String safeParsePort(String s) {
        try {
            int p = Integer.parseInt(s.trim());
            if (p > 0 && p < 65536) return String.valueOf(p);
        } catch (Exception e) {}
        return "25565";
    }

    protected void keyTyped(char c, int key) {
        fieldAddress.textboxKeyTyped(c, key);
        if (c == '\r') {
            actionPerformed(btnConnect);
        }
        if (key == 1) {
            mc.displayGuiScreen(parent);
        }
        updateConnectButton();
    }

    protected void mouseClicked(int mx, int my, int btn) {
        super.mouseClicked(mx, my, btn);
        fieldAddress.mouseClicked(mx, my, btn);
    }

    public void drawScreen(int mx, int my, float partial) {
        drawDefaultBackground();
        int cx = width / 2;
        int fieldY = height / 4 + 10;

        drawCenteredString(fontRenderer, "Direct Connect", cx, fieldY - 20, 0xFFFFFF);
        drawString(fontRenderer, "Server Address:", cx - 100, fieldY - 10, 0xA0A0A0);
        fieldAddress.drawTextBox();

        List hist = serverList.getHistory();
        if (!hist.isEmpty()) {
            drawString(fontRenderer, "Recent connections:", cx - 100, fieldY + 76, 0xA0A0A0);
            historySlot.drawScreen(mx, my, partial);
        }

        super.drawScreen(mx, my, partial);
    }
}
