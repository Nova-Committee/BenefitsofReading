package committee.nova.benefitsofreading.mixin;

import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.BiConsumer;

@Mixin(EnchantmentScreenHandler.class)
public abstract class MixinEnchantmentScreenHandler extends ScreenHandler {
    @Shadow
    @Final
    private Random random;

    @Shadow
    @Final
    private Property seed;

    @Shadow
    @Final
    public int[] enchantmentPower;

    @Shadow
    @Final
    public int[] enchantmentId;

    @Shadow
    @Final
    public int[] enchantmentLevel;

    protected MixinEnchantmentScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Shadow
    protected abstract List<EnchantmentLevelEntry> generateEnchantments(ItemStack stack, int slot, int level);

    @Shadow
    @Final
    private Inventory inventory;

    @Redirect(method = "onContentChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandlerContext;run(Ljava/util/function/BiConsumer;)V"))
    private void redirect$onContentChanged(ScreenHandlerContext instance, BiConsumer<World, BlockPos> function) {
        final ItemStack itemStack = this.inventory.getStack(0);
        instance.run(((world, pos) -> {
            float p = 0;
            for (BlockPos blockPos : EnchantingTableBlock.POWER_PROVIDER_OFFSETS) {
                if (EnchantingTableBlock.canAccessPowerProvider(world, pos, blockPos)) {
                    p++;
                    continue;
                }
                if (!(world.getBlockEntity(pos.add(blockPos)) instanceof ChiseledBookshelfBlockEntity c)) continue;
                for (int i = 0; i < 6; i++) {
                    final ItemStack stack = c.getStack(i);
                    if (stack.isOf(Items.ENCHANTED_BOOK)) {
                        p++;
                        continue;
                    }
                    if (!stack.isEmpty()) p += .66f;
                }
            }
            this.random.setSeed(this.seed.get());
            for (int j = 0; j < 3; ++j) {
                this.enchantmentPower[j] = EnchantmentHelper.calculateRequiredExperienceLevel(this.random, j, (int) p, itemStack);
                this.enchantmentId[j] = -1;
                this.enchantmentLevel[j] = -1;
                if (this.enchantmentPower[j] >= j + 1) continue;
                this.enchantmentPower[j] = 0;
            }
            for (int j = 0; j < 3; ++j) {
                List<EnchantmentLevelEntry> list;
                if (this.enchantmentPower[j] <= 0 || (list = this.generateEnchantments(itemStack, j, this.enchantmentPower[j])) == null || list.isEmpty())
                    continue;
                EnchantmentLevelEntry enchantmentLevelEntry = list.get(this.random.nextInt(list.size()));
                this.enchantmentId[j] = Registries.ENCHANTMENT.getRawId(enchantmentLevelEntry.enchantment);
                this.enchantmentLevel[j] = enchantmentLevelEntry.level;
            }
            this.sendContentUpdates();
        }));
    }
}
