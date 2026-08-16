package com.panicbar.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// I hope client does take the calculated process from the server and renders it, if something in server not working it's probably because of this file 
public class PanicSyncPacket {

    private final float percent;
    private final boolean lockedOut;

    public PanicSyncPacket(float percent, boolean lockedOut) {
        this.percent = percent;
        this.lockedOut = lockedOut;
    }

    public static void encode(PanicSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.percent);
        buf.writeBoolean(msg.lockedOut);
    }

    public static PanicSyncPacket decode(FriendlyByteBuf buf) {
        return new PanicSyncPacket(buf.readFloat(), buf.readBoolean());
    }

    public static void handle(PanicSyncPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        com.panicbar.client.ClientPanicState.update(msg.percent, msg.lockedOut)));
        ctx.setPacketHandled(true);
    }
}
