package ninja.crinkle.mod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import ninja.crinkle.mod.CrinkleMod;
import ninja.crinkle.mod.network.messages.AccidentEventMessage;
import ninja.crinkle.mod.network.messages.DiaperDesignSyncMessage;
import ninja.crinkle.mod.network.messages.MetabolismUpdateMessage;
import ninja.crinkle.mod.network.messages.UndergarmentUpdateMessage;

/**
 * A network channel that is used to sync the metabolism of a player to the client or server.
 *
 * @see MetabolismUpdateMessage
 * @see net.minecraftforge.network.simple.SimpleChannel
 */
public class CrinkleChannel {
    private static final String NAME = "crinklenet";
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(CrinkleMod.MODID, NAME))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public static void register() {
        int id = 0;
        INSTANCE.messageBuilder(MetabolismUpdateMessage.class, ++id)
                .decoder(MetabolismUpdateMessage::decoder)
                .encoder(MetabolismUpdateMessage::encoder)
                .consumerMainThread(MetabolismUpdateMessage::messageConsumer)
                .add();
        INSTANCE.messageBuilder(UndergarmentUpdateMessage.class, ++id)
                .decoder(UndergarmentUpdateMessage::decoder)
                .encoder(UndergarmentUpdateMessage::encoder)
                .consumerMainThread(UndergarmentUpdateMessage::messageConsumer)
                .add();
        INSTANCE.messageBuilder(AccidentEventMessage.class, ++id)
                .decoder(AccidentEventMessage::decoder)
                .encoder(AccidentEventMessage::encoder)
                .consumerMainThread(AccidentEventMessage::messageConsumer)
                .add();
        INSTANCE.messageBuilder(DiaperDesignSyncMessage.class, ++id)
                .decoder(DiaperDesignSyncMessage::decoder)
                .encoder(DiaperDesignSyncMessage::encoder)
                .consumerMainThread(DiaperDesignSyncMessage::messageConsumer)
                .add();
    }
}
