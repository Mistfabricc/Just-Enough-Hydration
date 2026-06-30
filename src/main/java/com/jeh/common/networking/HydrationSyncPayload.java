package com.jeh.common.networking;
 
import com.jeh.JEH;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record HydrationSyncPayload(int level) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<HydrationSyncPayload> TYPE =
        new CustomPacketPayload.Type<>(JEH.HYDRATION_SYNC_ID);

    public static final StreamCodec<FriendlyByteBuf, HydrationSyncPayload> CODEC =
        StreamCodec.ofMember(HydrationSyncPayload::write, HydrationSyncPayload::new);

    public HydrationSyncPayload(FriendlyByteBuf buf) {
        this(buf.readInt());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(level);
    }

    @Override
    public CustomPacketPayload.Type<HydrationSyncPayload> type() {
        return TYPE;
    }
}
