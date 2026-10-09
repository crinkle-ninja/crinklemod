package ninja.crinkle.mod.blocks.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import ninja.crinkle.mod.blocks.CrinkleBlocks;
import ninja.crinkle.mod.blocks.DunnyBlock;
import ninja.crinkle.mod.items.handlers.AdaptedItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nonnull;

public class DunnyBlockEntity extends BlockEntity implements GeoBlockEntity {
    public static final String ITEMS_INPUT_TAG = "Input";
    public static final String ITEMS_OUTPUT_TAG = "Output";
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_INPUT_COUNT = 1;
    public static final int SLOT_OUTPUT = 0;
    public static final int SLOT_OUTPUT_COUNT = 6;
    public static final int SLOT_COUNT = SLOT_INPUT_COUNT + SLOT_OUTPUT_COUNT;
    protected static final RawAnimation ANIMATION_CLOSE = RawAnimation.begin()
            .thenPlay("animation.Dunny.seat.close.deploy")
            .thenLoop("animation.Dunny.seat.close.idle");
    protected static final RawAnimation ANIMATION_OPEN = RawAnimation.begin()
            .thenPlay("animation.Dunny.seat.open.deploy")
            .thenLoop("animation.Dunny.seat.open.idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ItemStackHandler inputItems = createItemHandler(SLOT_INPUT_COUNT);
    private final LazyOptional<IItemHandler> inputItemHandler =
            LazyOptional.of(() -> new AdaptedItemHandler(inputItems) {
                @Override
                public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                    return ItemStack.EMPTY;
                }
            });
    private final ItemStackHandler outputItems = createItemHandler(SLOT_OUTPUT_COUNT);
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new CombinedInvWrapper(inputItems,
            outputItems));
    private final LazyOptional<IItemHandler> outputItemHandler =
            LazyOptional.of(() -> new AdaptedItemHandler(outputItems) {
                @Override
                public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                    return stack;
                }
            });

    public DunnyBlockEntity(BlockPos pos, BlockState state) {
        super(CrinkleBlocks.DUNNY_BLOCK_ENTITY.get(), pos, state);
    }

    @Nonnull
    private ItemStackHandler createItemHandler(int slots) {
        return new ItemStackHandler(slots) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    public ItemStackHandler getInputItems() {
        return inputItems;
    }

    public ItemStackHandler getOutputItems() {
        return outputItems;
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        inputItemHandler.invalidate();
        outputItemHandler.invalidate();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) {
                return itemHandler.cast();
            } else if (side == Direction.DOWN) {
                return outputItemHandler.cast();
            } else {
                return inputItemHandler.cast();
            }
        } else {
            return super.getCapability(cap, side);
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains(ITEMS_INPUT_TAG)) {
            inputItems.deserializeNBT(tag.getCompound(ITEMS_INPUT_TAG));
        }
        if (tag.contains(ITEMS_OUTPUT_TAG)) {
            outputItems.deserializeNBT(tag.getCompound(ITEMS_OUTPUT_TAG));
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(ITEMS_INPUT_TAG, inputItems.serializeNBT());
        tag.put(ITEMS_OUTPUT_TAG, outputItems.serializeNBT());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "dunny_controller", 0,
                this::predicate));

    }

    private PlayState predicate(AnimationState<DunnyBlockEntity> state) {
        if (state.isCurrentAnimation(ANIMATION_OPEN) && !getBlockState().getValue(DunnyBlock.OPEN)) {
            state.getController().setAnimation(ANIMATION_CLOSE);
        } else if (!state.isCurrentAnimation(ANIMATION_OPEN) && getBlockState().getValue(DunnyBlock.OPEN)) {
            state.getController().setAnimation(ANIMATION_OPEN);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}