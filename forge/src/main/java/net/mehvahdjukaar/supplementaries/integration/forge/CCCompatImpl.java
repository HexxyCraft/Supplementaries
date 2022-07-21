package net.mehvahdjukaar.supplementaries.integration.forge;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import dan200.computercraft.shared.Capabilities;
import dan200.computercraft.shared.media.items.ItemPrintout;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SpeakerBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SpeakerBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public class CCCompatImpl {

    public static void initialize() {
        ComputerCraftAPI.registerPeripheralProvider((IPeripheralProvider) ModRegistry.SPEAKER_BLOCK.get());
    }

    public static int getPages(ItemStack itemstack) {
        return ItemPrintout.getPageCount(itemstack);
    }

    public static String[] getText(ItemStack itemstack) {
        return ItemPrintout.getText(itemstack);
    }

    public static boolean isPrintedBook(Item item) {
        return item instanceof ItemPrintout;
    }

    //TODO: maybe this isn't needed since tile alredy provides it
    public static SpeakerBlock makeSpeaker(BlockBehaviour.Properties properties) {
        //try loading this now, freaking classloader
        class SpeakerCC extends SpeakerBlock implements IPeripheralProvider {

            public SpeakerCC(Properties properties) {
                super(properties);
            }

            @NotNull
            @Override
            public LazyOptional<IPeripheral> getPeripheral(@NotNull Level world, @NotNull BlockPos pos, @NotNull Direction side) {
                var tile = world.getBlockEntity(pos);
                if (tile != null) {
                    return tile.getCapability(Capabilities.CAPABILITY_PERIPHERAL, side);
                }
                return LazyOptional.empty();
            }
        }
        return new SpeakerCC(properties);
    }

    public static boolean isPeripheralCap(Capability<?> cap) {
        return cap == Capabilities.CAPABILITY_PERIPHERAL;
    }

    public static LazyOptional<Object> getPeripheralSupplier(SpeakerBlockTile tile) {
        return LazyOptional.of(() -> new SpeakerPeripheral(tile));
    }

}