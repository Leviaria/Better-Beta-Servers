package net.minecraft.src;

import java.util.List;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class BBSGuiAddServer extends GuiScreen {

    private final BBSGuiMultiplayer parent;
    private final int editIndex;

    private GuiTextField fieldName;
    private GuiTextField fieldAddress;
    private GuiButton buttonDone;
    private GuiButton buttonCancel;

    private String title;

    public BBSGuiAddServer(BBSGuiMultiplayer parent, int editIndex, BBSServerEntry existing) {
        this.parent = parent;
        this.editIndex = editIndex;

        if (editIndex >= 0 && existing != null) {
            title = "Edit Server";
            this.editExisting = existing;
        } else {
            title = "Add Server";
            this.editExisting = null;
        }
    }

    private final BBSServerEntry editExisting;

    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        controlList.clear();

        int cx = width / 2;
        int cy = height / 2;

        fieldName = new GuiTextField(this, fontRenderer, cx - 100, cy - 40, 200, 20,
                editExisting != null ? editExisting.name : "");
        fieldName.setMaxStringLength(64);
        fieldName.isFocused = true;

        String addrDefault = "";
        if (editExisting != null) {
            addrDefault = editExisting.getAddress();
        }
        fieldAddress = new GuiTextField(this, fontRenderer, cx - 100, cy - 10, 200, 20, addrDefault);
        fieldAddress.setMaxStringLength(128);

        buttonDone = new GuiButton(0, cx - 100, cy + 20, 95, 20, "Done");
        buttonCancel = new GuiButton(1, cx + 5, cy + 20, 95, 20, "Cancel");

        controlList.add(buttonDone);
        controlList.add(buttonCancel);

        updateDoneButton();
    }

    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    public void updateScreen() {
        fieldName.updateCursorCounter();
        fieldAddress.updateCursorCounter();
    }

    private void updateDoneButton() {
        buttonDone.enabled = fieldName.getText().trim().length() > 0
                && fieldAddress.getText().trim().length() > 0;
    }

    protected void actionPerformed(GuiButton btn) {
        if (!btn.enabled) return;
        if (btn.id == 1) {
            mc.displayGuiScreen(parent);
            return;
        }
        if (btn.id == 0) {
            String name = fieldName.getText().trim();
            String addr = fieldAddress.getText().trim();
            String[] parsed = parseAddress(addr);
            String host = parsed[0];
            int port = Integer.parseInt(parsed[1]);

            if (editIndex >= 0) {
                parent.getServerList().updateServer(editIndex, new BBSServerEntry(name, host, port, editExisting != null && editExisting.favorite));
            } else {
                parent.getServerList().addServer(new BBSServerEntry(name, host, port, false));
            }
            parent.reloadList();
            mc.displayGuiScreen(parent);
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
        fieldName.textboxKeyTyped(c, key);
        fieldAddress.textboxKeyTyped(c, key);
        if (key == 15) {
            if (fieldName.isFocused) {
                fieldName.isFocused = false;
                fieldAddress.isFocused = true;
            } else {
                fieldAddress.isFocused = false;
                fieldName.isFocused = true;
            }
        }
        if (c == '\r') {
            actionPerformed(buttonDone);
        }
        if (key == 1) {
            mc.displayGuiScreen(parent);
        }
        updateDoneButton();
    }

    protected void mouseClicked(int mx, int my, int btn) {
        super.mouseClicked(mx, my, btn);
        fieldName.mouseClicked(mx, my, btn);
        fieldAddress.mouseClicked(mx, my, btn);
    }

    public void drawScreen(int mx, int my, float partial) {
        drawDefaultBackground();
        int cx = width / 2;
        int cy = height / 2;
        drawCenteredString(fontRenderer, title, cx, cy - 60, 0xFFFFFF);
        drawString(fontRenderer, "Server Name:", cx - 100, cy - 50, 0xA0A0A0);
        drawString(fontRenderer, "Server Address:", cx - 100, cy - 20, 0xA0A0A0);
        fieldName.drawTextBox();
        fieldAddress.drawTextBox();
        super.drawScreen(mx, my, partial);
    }

    public void selectNextField() {
        if (fieldName.isFocused) {
            fieldName.isFocused = false;
            fieldAddress.isFocused = true;
        } else {
            fieldAddress.isFocused = false;
            fieldName.isFocused = true;
        }
    }
}
