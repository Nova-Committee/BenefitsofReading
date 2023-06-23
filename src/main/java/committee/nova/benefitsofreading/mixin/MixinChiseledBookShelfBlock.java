package committee.nova.benefitsofreading.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChiseledBookShelfBlock.class)
public abstract class MixinChiseledBookShelfBlock implements IForgeBlock {
    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof ChiseledBookShelfBlockEntity e)) return .0f;
        float power = .0f;
        for (int i = 0; i < 6; i++) {
            final ItemStack stack = e.getItem(i);
            if (stack.is(Items.ENCHANTED_BOOK)) {
                power++;
                continue;
            }
            if (!stack.isEmpty()) power += .66f;
        }
        return power;
    }
}
