package com.mrcrayfish.controllable.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrcrayfish.controllable.client.RadialMenuHandler;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Author: MrCrayfish
 */
public class SelectButtonBindingScreen extends Screen
{
    private RadialMenuConfigureScreen parentScreen;
    private ButtonBindingList bindingList;

    public SelectButtonBindingScreen(RadialMenuConfigureScreen parentScreen)
    {
        super(new TranslationTextComponent("controllable.gui.title.select_bindings"));
        this.parentScreen = parentScreen;
    }

    public RadialMenuConfigureScreen getRadialConfigureScreen()
    {
        return this.parentScreen;
    }

    @Override
    protected void init()
    {
        this.bindingList = new ButtonBindingList(this, this.minecraft, this.width + 10, this.height, 45, this.height - 44, 20);
        this.children.add(this.bindingList);
        this.addButton(new Button(this.width / 2 - 155, this.height - 29, 150, 20, new TranslationTextComponent("controllable.gui.restoreDefaults"), (button) -> {
            this.parentScreen.getBindings().clear();
            this.parentScreen.getBindings().addAll(RadialMenuHandler.instance().getDefaults());
            this.bindingList.getEventListeners().stream().filter(entry -> entry instanceof ButtonBindingList.BindingEntry).map(entry -> (ButtonBindingList.BindingEntry) entry).forEach(ButtonBindingList.BindingEntry::updateButtons);
        }));
        this.addButton(new Button(this.width / 2 + 5, this.height - 29, 150, 20, DialogTexts.GUI_DONE, (button) -> {
            this.minecraft.displayGuiScreen(this.parentScreen);
        }));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        this.bindingList.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}
