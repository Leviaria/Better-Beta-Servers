package net.minecraft.src;

import java.util.List;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class GuiMultiplayer extends GuiScreen {

    public GuiMultiplayer(GuiScreen guiscreen) {
        parentScreen = guiscreen;
    }

    public void initGui() {
        mc.displayGuiScreen(new BBSGuiMultiplayer(parentScreen));
    }

    public void drawScreen(int i, int j, float f) {
        drawDefaultBackground();
    }

    private GuiScreen parentScreen;
}
