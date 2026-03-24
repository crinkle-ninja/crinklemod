package ninja.crinkle.mod.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.blocks.entities.DunnyBlockEntity;

public class CrinkleBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
            CrinkleMod.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CrinkleMod.MODID);

    public static final RegistryObject<Block> DUNNY_BLOCK = BLOCKS.register("dunny", DunnyBlock::new);

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        BLOCK_ENTITIES.register(bus);
    }

    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<DunnyBlockEntity>> DUNNY_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("dunny",
                    () -> BlockEntityType.Builder.of(DunnyBlockEntity::new, DUNNY_BLOCK.get())
                            .build(null));


}
