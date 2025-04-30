package net.anse.namethatvillage.screen.custom;

import net.anse.namethatvillage.block.entity.VillageBellBlockEntity;
import net.anse.namethatvillage.init.ModBlocks;
import net.anse.namethatvillage.screen.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

public class VillageBellMenu extends AbstractContainerMenu
{

    public final VillageBellBlockEntity blockEntity;
    private final Level level;

    public VillageBellMenu(int containerId, Inventory inv, FriendlyByteBuf extraData)
    {
        this(containerId, inv, inv.player.level().getBlockEntity((extraData.readBlockPos())));
    }

    public VillageBellMenu(int containerId, Inventory inv, BlockEntity blockEntity)
    {
        super(ModMenuTypes.VILLAGE_BELL_MENU.get(), containerId);
        this.blockEntity = ((VillageBellBlockEntity) blockEntity);
        this.level = inv.player.level();

        this.addStandardInventorySlots(inv, 58, 84);

    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ModBlocks.VILLAGE_BELL.get());
    }

    public VillageBellBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
