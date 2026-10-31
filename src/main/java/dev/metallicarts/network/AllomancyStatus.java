package dev.metallicarts.network;
import dev.metallicarts.MetallicArts;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
/** Two bits per supported metal; overload/fatigue flags only. Private grants stay owner-only. */
public record AllomancyStatus(int modes,int flags) implements CustomPacketPayload {
    public static final Type<AllomancyStatus> TYPE=new Type<>(MetallicArts.id("allomancy_status_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf,AllomancyStatus> CODEC=StreamCodec.composite(
        ByteBufCodecs.VAR_INT,AllomancyStatus::modes,ByteBufCodecs.VAR_INT,AllomancyStatus::flags,AllomancyStatus::new);
    public Type<? extends CustomPacketPayload> type(){return TYPE;}
    public int mode(int index){return (modes>>(index*2))&3;}
}
