package com.mrcrayfish.controllable.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mrcrayfish.controllable.client.BindingRegistry;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.gui.widget.ButtonBindingButton;
import com.mrcrayfish.controllable.client.gui.widget.ImageButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.*;

/**
 * Author: MrCrayfish
 */
public class ButtonBindingList extends AbstractOptionList<ButtonBindingList.Entry>
{
    private Screen parent;
    private Map<String, List<ButtonBinding>> categories = new LinkedHashMap<>();

    public ButtonBindingList(Screen parent, Minecraft mc, int widthIn, int heightIn, int topIn, int bottomIn, int itemHeightIn)
    {
        super(mc, widthIn, heightIn, topIn, bottomIn, itemHeightIn);
        this.parent = parent;
        this.updateList(false);
    }

    public void updateList(boolean showUnbound)
    {
        // Initialize map with categories to have a predictable order (map is linked)
        this.categories.put("key.categories.movement", new ArrayList<>());
        this.categories.put("key.categories.gameplay", new ArrayList<>());
        this.categories.put("key.categories.inventory", new ArrayList<>());
        this.categories.put("key.categories.creative", new ArrayList<>());
        this.categories.put("key.categories.multiplayer", new ArrayList<>());
        this.categories.put("key.categories.ui", new ArrayList<>());
        this.categories.put("key.categories.misc", new ArrayList<>());

        // Add all button bindings to the appropriate category or create a new one
        BindingRegistry.getInstance().getBindings().stream().filter(ButtonBinding::isNotReserved).forEach(binding ->
        {
            // Only show unbound bindings for select binding screen for radial menu
            if(showUnbound && binding.getButton() != -1) return;
            List<ButtonBinding> list = this.categories.computeIfAbsent(binding.getCategory(), category -> new ArrayList<>());
            list.add(binding);
        });

        // Sorts the button binding list then adds new entries to the option list for each category
        this.categories.forEach((category, list) ->
        {
            if(!list.isEmpty())
            {
                Collections.sort(list);
                this.addEntry(new CategoryEntry(new TranslationTextComponent(category)));
                list.forEach(binding -> this.addEntry(new BindingEntry(binding)));
            }
        });
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(this.parent instanceof ButtonBindingScreen)
        {
            if(((ButtonBindingScreen) this.parent).isWaitingForButtonInput())
            {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    abstract class Entry extends AbstractOptionList.Entry<Entry> {}

    protected class CategoryEntry extends Entry
    {
        private final ITextComponent label;
        private final int labelWidth;

        protected CategoryEntry(ITextComponent label)
        {
            this.label = label;
            this.labelWidth = ButtonBindingList.this.minecraft.fontRenderer.getStringPropertyWidth(this.label);
        }

        @Override
        public boolean changeFocus(boolean focus)
        {
            return false;
        }

        @Override
        public List<? extends IGuiEventListener> getEventListeners()
        {
            return Collections.emptyList();
        }

        @Override
        public void render(MatrixStack matrixStack, int x, int y, int p_230432_4_, int p_230432_5_, int itemHeight, int p_230432_7_, int p_230432_8_, boolean selected, float partialTicks)
        {
            float labelX = ButtonBindingList.this.minecraft.currentScreen.width / 2F - this.labelWidth / 2F;
            float labelY = y + itemHeight - 9 - 1;
            ButtonBindingList.this.minecraft.fontRenderer.func_243248_b(matrixStack, this.label, labelX, labelY, 0xFFFFFFFF);
        }
    }

    public class BindingEntry extends Entry
    {
        private ButtonBinding binding;
        private TextComponent label;
        private Button bindingButton;
        private Button deleteButton;

        protected BindingEntry(ButtonBinding binding)
        {
            this.binding = binding;
            this.label = new TranslationTextComponent(binding.getDescription());
            if(ButtonBindingList.this.parent instanceof ButtonBindingScreen)
            {
                this.bindingButton = new ButtonBindingButton(0, 0, binding, button ->
                {
                    if(ButtonBindingList.this.parent instanceof ButtonBindingScreen)
                    {
                        ((ButtonBindingScreen) ButtonBindingList.this.parent).setSelectedBinding(this.binding);
                    }
                });
                this.deleteButton = new ImageButton(0, 0, 20, ControllerLayoutScreen.TEXTURE, 108, 0, 16, 16, button ->
                {
                    binding.reset();
                    BindingRegistry registry = BindingRegistry.getInstance();
                    registry.resetBindingHash();
                    registry.save();
                });
            }
            else if(ButtonBindingList.this.parent instanceof SelectButtonBindingScreen)
            {
                SelectButtonBindingScreen screen = (SelectButtonBindingScreen) ButtonBindingList.this.parent;
                List<ButtonBindingData> bindings = screen.getRadialConfigureScreen().getBindings();
                this.bindingButton = new ImageButton(0, 0, 20, ControllerLayoutScreen.TEXTURE, 88, 25, 10, 10, button ->
                {
                    bindings.add(new ButtonBindingData(this.binding, TextFormatting.YELLOW));
                    this.bindingButton.active = false;
                    this.deleteButton.active = true;
                });
                this.deleteButton = new ImageButton(0, 0, 20, ControllerLayoutScreen.TEXTURE, 98, 15, 10, 10, button ->
                {
                    bindings.removeIf(entry -> entry.getBinding() == this.binding);
                    this.bindingButton.active = true;
                    this.deleteButton.active = false;
                });
                this.bindingButton.active = bindings.stream().noneMatch(entry -> entry.getBinding() == this.binding);
                this.deleteButton.active = bindings.stream().anyMatch(entry -> entry.getBinding() == this.binding);
            }
        }

        public void updateButtons()
        {
            if(ButtonBindingList.this.parent instanceof SelectButtonBindingScreen)
            {
                SelectButtonBindingScreen screen = (SelectButtonBindingScreen) ButtonBindingList.this.parent;
                List<ButtonBindingData> bindings = screen.getRadialConfigureScreen().getBindings();
                this.bindingButton.active = bindings.stream().noneMatch(entry -> entry.getBinding() == this.binding);
                this.deleteButton.active = bindings.stream().anyMatch(entry -> entry.getBinding() == this.binding);
            }
        }

        @Override
        public List<? extends IGuiEventListener> getEventListeners()
        {
            return ImmutableList.of(this.bindingButton, this.deleteButton);
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void render(MatrixStack matrixStack, int x, int y, int left, int width, int p_230432_6_, int mouseX, int mouseY, boolean selected, float partialTicks)
        {
            int color = this.binding.isConflictingContext() ? TextFormatting.RED.getColor() : TextFormatting.GRAY.getColor();
            ButtonBindingList.this.minecraft.fontRenderer.func_243246_a(matrixStack, this.label, left - 15, y + 6, color);
            this.bindingButton.x = left + width - 45;
            this.bindingButton.y = y;
            this.bindingButton.render(matrixStack, mouseX, mouseY, partialTicks);
            this.deleteButton.x = left + width - 20;
            this.deleteButton.y = y;
            if(ButtonBindingList.this.parent instanceof ButtonBindingScreen)
            {
                this.deleteButton.active = !this.binding.isDefault();
            }
            this.deleteButton.render(matrixStack, mouseX, mouseY, partialTicks);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button)
        {
            if(ButtonBindingList.this.parent instanceof ButtonBindingScreen)
            {
                if(button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && this.bindingButton.isHovered())
                {
                    this.binding.setButton(-1);
                    this.bindingButton.playDownSound(Minecraft.getInstance().getSoundHandler());
                    return true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
