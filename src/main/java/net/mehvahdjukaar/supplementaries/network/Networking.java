package net.mehvahdjukaar.supplementaries.network;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;


public class Networking{
    public static SimpleChannel INSTANCE;
    private static int ID = 0;
    private static final String PROTOCOL_VERSION = "1";
    public static int nextID() { return ID++; }

    public static void registerMessages() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(Supplementaries.MOD_ID, "splmchannel"), () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

        INSTANCE.registerMessage(nextID(), SendSpeakerBlockMessagePacket.class, SendSpeakerBlockMessagePacket::buffer,
                SendSpeakerBlockMessagePacket::new, SendSpeakerBlockMessagePacket::handler);
/*
        INSTANCE.registerMessage(nextID(), PackedUpdateServerHangingSign.class, PackedUpdateServerHangingSign::toBytes,
                PackedUpdateServerHangingSign::new, PackedUpdateServerHangingSign::handle);
        INSTANCE.registerMessage(nextID(), PackedUpdateServerSignPost.class, PackedUpdateServerSignPost::toBytes, PackedUpdateServerSignPost::new,
                PackedUpdateServerSignPost::handle);
                */

    }
}