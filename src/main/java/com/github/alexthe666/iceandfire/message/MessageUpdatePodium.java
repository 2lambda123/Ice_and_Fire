package com.github.alexthe666.iceandfire.message;

import com.github.alexthe666.citadel.server.message.PacketBufferUtils;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityPodium;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageUpdatePodium {

    public long blockPos;
    public ItemStack heldStack;

    public MessageUpdatePodium(long blockPos, ItemStack heldStack) {
        this.blockPos = blockPos;
        this.heldStack = heldStack;

    }

    public MessageUpdatePodium() {
    }

    public static MessageUpdatePodium read(PacketBuffer buf) {
        return new MessageUpdatePodium(buf.readLong(), PacketBufferUtils.readItemStack(buf));
    }

    public static void write(MessageUpdatePodium message, PacketBuffer buf) {
        buf.writeLong(message.blockPos);
        PacketBufferUtils.writeItemStack(buf, message.heldStack);
    }

    public static class Handler {
        public Handler() {
        }

        public static void handle(MessageUpdatePodium message, Supplier<NetworkEvent.Context> context) {
            context.get().setPacketHandled(true);
            PlayerEntity player = context.get().getSender();
            if(context.get().getDirection().getReceptionSide() == LogicalSide.CLIENT){
                player = IceAndFire.PROXY.getClientSidePlayer();
            }
            if (player != null) {
                if (player.level != null) {
                    BlockPos pos = BlockPos.of(message.blockPos);
                    if (player.level.getBlockEntity(pos) != null) {
                        if (player.level.getBlockEntity(pos) instanceof TileEntityPodium) {
                            TileEntityPodium podium = (TileEntityPodium) player.level.getBlockEntity(pos);
                            podium.setItem(0, message.heldStack);
                        }
                    }
                }
            }
        }
    }

}