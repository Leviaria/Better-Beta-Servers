package net.minecraft.src;

import java.util.List;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class BBSGuiMultiplayer extends GuiScreen {

    private static final int BTN_JOIN        = 0;
    private static final int BTN_ADD         = 1;
    private static final int BTN_EDIT        = 2;
    private static final int BTN_REMOVE      = 3;
    private static final int BTN_FAVORITE    = 4;
    private static final int BTN_DIRECT      = 5;
    private static final int BTN_HISTORY     = 6;
    private static final int BTN_CANCEL      = 7;
    private static final int BTN_REFRESH     = 8;
    private static final int BTN_COPY_IP     = 9;

    private final GuiScreen parentScreen;
    private BBSServerList serverList;
    private BBSGuiServerListSlot listSlot;

    private int selectedIndex = -1;
    private boolean showingHistory = false;

    private GuiButton btnJoin;
    private GuiButton btnEdit;
    private GuiButton btnRemove;
    private GuiButton btnFavorite;
    private GuiButton btnHistory;
    private GuiButton btnCopyIp;

    public BBSGuiMultiplayer(GuiScreen parent) {
        this.parentScreen = parent;
    }

    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        controlList.clear();

        serverList = new BBSServerList(Minecraft.getMinecraftDir());
        serverList.load();

        // Layout: two columns of buttons at the bottom, each button 150px wide, 4px gap
        // Total width: 150 + 4 + 150 = 304px, centered
        int bw  = 150;
        int bh  = 20;
        int gap = 4;
        int left  = width / 2 - bw - gap / 2;   // left column x
        int right = width / 2 + gap / 2;          // right column x

        int row1 = height - 28;
        int row2 = height - 52;
        int row3 = height - 76;

        // Row 1 (bottom): Add Server | Cancel
        GuiButton btnAdd    = new GuiButton(BTN_ADD,    left,  row1, bw, bh, "Add Server");
        GuiButton btnCancel = new GuiButton(BTN_CANCEL, right, row1, bw, bh, "Cancel");

        // Row 2 (middle): Direct Connect | History/Server List
        GuiButton btnDirect = new GuiButton(BTN_DIRECT,  left,  row2, bw, bh, "Direct Connect");
        btnHistory          = new GuiButton(BTN_HISTORY, right, row2, bw, bh, showingHistory ? "Server List" : "History");

        // Row 3 (top): Join Server | Refresh
        btnJoin             = new GuiButton(BTN_JOIN,    left,  row3, bw, bh, "Join Server");
        GuiButton btnRefresh = new GuiButton(BTN_REFRESH, right, row3, bw, bh, "Refresh");

        // Selection-sensitive row above row3: Edit | Remove | Favorite | Copy IP
        // These are placed above the main rows in a tight 4-button row
        int selRow = height - 100;
        int sw = 72;  // narrower for 4 buttons: 4*72 + 3*4 = 300px total, centered
        int selStart = width / 2 - (4 * sw + 3 * gap) / 2;

        btnEdit     = new GuiButton(BTN_EDIT,     selStart,                  selRow, sw, bh, "Edit");
        btnRemove   = new GuiButton(BTN_REMOVE,   selStart +   (sw + gap),   selRow, sw, bh, "Remove");
        btnFavorite = new GuiButton(BTN_FAVORITE, selStart + 2*(sw + gap),   selRow, sw, bh, "Favorite");
        btnCopyIp   = new GuiButton(BTN_COPY_IP,  selStart + 3*(sw + gap),   selRow, sw, bh, "Copy IP");

        controlList.add(btnJoin);
        controlList.add(btnEdit);
        controlList.add(btnRemove);
        controlList.add(btnFavorite);
        controlList.add(btnAdd);
        controlList.add(btnDirect);
        controlList.add(btnHistory);
        controlList.add(btnCancel);
        controlList.add(btnRefresh);
        controlList.add(btnCopyIp);

        List activeList = showingHistory ? serverList.getHistory() : serverList.getServers();
        listSlot = new BBSGuiServerListSlot(this, mc, activeList, height - 108);

        selectedIndex = -1;
        updateButtonStates();
        startPingAll();
    }

    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    private void updateButtonStates() {
        boolean sel = selectedIndex >= 0 && selectedIndex < getActiveList().size();
        btnJoin.enabled = sel;
        btnEdit.enabled = sel && !showingHistory;
        btnRemove.enabled = sel && !showingHistory;
        btnFavorite.enabled = sel && !showingHistory;
        btnCopyIp.enabled = sel;

        if (sel && !showingHistory) {
            BBSServerEntry e = (BBSServerEntry) getActiveList().get(selectedIndex);
            btnFavorite.displayString = e.favorite ? "Unfavorite" : "Favorite";
        }
    }

    private List getActiveList() {
        return showingHistory ? serverList.getHistory() : serverList.getServers();
    }

    private void startPingAll() {
        List list = getActiveList();
        for (int i = 0; i < list.size(); i++) {
            BBSServerEntry e = (BBSServerEntry) list.get(i);
            e.pingState = BBSServerEntry.PING_UNKNOWN;
            new BBSPingThread(e).start();
        }
    }

    protected void actionPerformed(GuiButton btn) {
        if (!btn.enabled) return;

        List active = getActiveList();

        if (btn.id == BTN_CANCEL) {
            mc.displayGuiScreen(parentScreen);
            return;
        }

        if (btn.id == BTN_ADD) {
            mc.displayGuiScreen(new BBSGuiAddServer(this, -1, null));
            return;
        }

        if (btn.id == BTN_DIRECT) {
            mc.displayGuiScreen(new BBSGuiDirectConnect(this, serverList));
            return;
        }

        if (btn.id == BTN_HISTORY) {
            showingHistory = !showingHistory;
            selectedIndex = -1;
            mc.displayGuiScreen(new BBSGuiMultiplayer(parentScreen));
            return;
        }

        if (btn.id == BTN_REFRESH) {
            startPingAll();
            return;
        }

        if (selectedIndex < 0 || selectedIndex >= active.size()) return;
        BBSServerEntry entry = (BBSServerEntry) active.get(selectedIndex);

        if (btn.id == BTN_JOIN) {
            connectTo(entry);
        } else if (btn.id == BTN_EDIT && !showingHistory) {
            mc.displayGuiScreen(new BBSGuiAddServer(this, selectedIndex, entry));
        } else if (btn.id == BTN_REMOVE && !showingHistory) {
            serverList.removeServer(selectedIndex);
            selectedIndex = -1;
            reloadList();
        } else if (btn.id == BTN_FAVORITE && !showingHistory) {
            serverList.toggleFavorite(selectedIndex);
            reloadList();
        } else if (btn.id == BTN_COPY_IP) {
            copyToClipboard(entry.getAddress());
        }
    }

    private void copyToClipboard(String text) {
        try {
            java.awt.datatransfer.StringSelection sel = new java.awt.datatransfer.StringSelection(text);
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
        } catch (Exception e) {
            System.out.println("[BBS] Clipboard copy failed: " + e.getMessage());
        }
    }

    public void connectTo(BBSServerEntry entry) {
        serverList.pushHistory(entry.host, entry.port);
        mc.gameSettings.lastServer = (entry.host + ":" + entry.port).replaceAll(":", "_");
        mc.gameSettings.saveOptions();
        mc.displayGuiScreen(new GuiConnecting(mc, entry.host, entry.port));
    }

    public void connectToSelected() {
        List active = getActiveList();
        if (selectedIndex >= 0 && selectedIndex < active.size()) {
            connectTo((BBSServerEntry) active.get(selectedIndex));
        }
    }

    public void selectServer(int index) {
        selectedIndex = index;
        updateButtonStates();
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public BBSServerList getServerList() {
        return serverList;
    }

    public FontRenderer getFontRenderer() {
        return fontRenderer;
    }

    public void reloadList() {
        serverList.load();
        List activeList = showingHistory ? serverList.getHistory() : serverList.getServers();
        listSlot = new BBSGuiServerListSlot(this, mc, activeList, height - 108);
        updateButtonStates();
        startPingAll();
    }

    public void drawScreen(int mx, int my, float partial) {
        listSlot.drawScreen(mx, my, partial);

        String title = showingHistory ? "Direct Connect History" : "Better Beta Servers";
        drawCenteredString(fontRenderer, title, width / 2, 14, 0xFFFFFF);

        List active = getActiveList();
        if (active.isEmpty()) {
            String hint = showingHistory
                    ? "No direct connect history yet."
                    : "No servers saved. Click 'Add Server' to get started.";
            drawCenteredString(fontRenderer, hint, width / 2, (height - 108) / 2 + 16, 0x888888);
        }

        super.drawScreen(mx, my, partial);
    }
}
