package tfar.transparencychanger;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.commons.lang3.tuple.Pair;
import tfar.transparencychanger.mixin.BlockStateAccess;

import java.util.Collection;
import java.util.List;

@Mod(Constants.MOD_ID)
public class TransparencyChangerForge {


    public static final Config CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        final Pair<Config, ForgeConfigSpec> specPair2 = new ForgeConfigSpec.Builder().configure(Config::new);
        CLIENT_SPEC = specPair2.getRight();
        CLIENT = specPair2.getLeft();
    }

    public TransparencyChangerForge() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(
                        // Send any version from server to client, since we will be accepting any version as well
                        () -> "dQw4w9WgXcQ",// Accept any version on the client, from server or from save
                        (remoteVersion, isFromServer) -> true));

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(PocketDatagen::gather);

        if (FMLEnvironment.dist.isClient()) {
            // This method is invoked by the Forge mod loader when it is ready
            // to load your mod. You can access Forge and Common code in this
            // project.
            // Use Forge to bootstrap the Common mod.
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
            bus.addListener(this::setup);
            CommonClass.init();
        } else {
            Constants.LOG.warn("This is a clientside only mod that should be removed from the server");
        }
    }

    static class PocketDatagen{
        static void gather(GatherDataEvent event) {
            DataGenerator generator = event.getGenerator();
            PackOutput output = generator.getPackOutput();
            ExistingFileHelper helper = event.getExistingFileHelper();
            generator.addProvider(true,new ModBlockStateProvider(output,helper));
        }

        static class ModBlockStateProvider extends BlockStateProvider {

            public ModBlockStateProvider(PackOutput output,  ExistingFileHelper exFileHelper) {
                super(output, Constants.MOD_ID, exFileHelper);
            }

            @Override
            protected void registerStatesAndModels() {
                simpleBlock(Blocks.COBBLESTONE,models().cubeAll("cobblestone",mcLoc("block/light_blue_stained_glass")));
            }
        }
    }

    public static class Config{
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> c;
        public Config(ForgeConfigSpec.Builder builder) {
            builder.push("general");
            c = builder.defineListAllowEmpty("transparent",List.of(),o -> true);
            builder.pop();
        }
    }

    /**
     *    private static StainedGlassBlock stainedGlass(DyeColor color) {
     *       return new StainedGlassBlock(color, BlockBehaviour.Properties.of().mapColor(color).instrument(NoteBlockInstrument.HAT)
     *       .strength(0.3F).sound(SoundType.GLASS).noOcclusion().isValidSpawn(Blocks::never).isRedstoneConductor(Blocks::never)
     *       .isSuffocating(Blocks::never).isViewBlocking(Blocks::never));
     *    }
     * @param event
     */

    void setup(FMLClientSetupEvent event) {
        List<? extends String> strings = CLIENT.c.get();
        for (String s : strings) {
            Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(s));
            ItemBlockRenderTypes.setRenderLayer(block,RenderType.translucent());
            Collection<BlockState> states = block.getStateDefinition().getPossibleStates();
            states.forEach(state -> ((BlockStateAccess)state).setCanOcclude(false));
        }
    }
}