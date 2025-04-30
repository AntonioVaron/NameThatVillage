package net.anse.namethatvillage.screen.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import net.anse.namethatvillage.NameThatVillage;
import net.anse.namethatvillage.block.VillageBellBlock;
import net.anse.namethatvillage.block.entity.VillageBellBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.client.renderer.GameRenderer;

public class VillageBellScreen extends AbstractContainerScreen<VillageBellMenu>
{
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NameThatVillage.MOD_ID, "textures/gui/village_bell/village.png");
    private final VillageBellBlockEntity blockEntity;

    public VillageBellScreen(VillageBellMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
        this.blockEntity = menu.getBlockEntity();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY)
    {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (width - 276) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(RenderType::guiTextured, GUI_TEXTURE, x, y, 0.0F, 0.0F, 276, imageHeight, 276, 166);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Título del menú
        guiGraphics.drawString(this.font, this.title, 98, 26, 0x404040, false);

        String name = blockEntity.getVillageName();
        guiGraphics.drawString(this.font, name, 98, 41, 0xFFFFFF, false);

        // Texto "Inventory" encima del inventario del jugador
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 58, this.imageHeight - 94, 0x404040, false);
    }
}
