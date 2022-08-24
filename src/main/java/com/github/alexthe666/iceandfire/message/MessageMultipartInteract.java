package com.github.alexthe666.iceandfire.message;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageMultipartInteract {

    public int creatureID;
    public float dmg;

    public MessageMultipartInteract(int creatureID, float dmg) {
        this.creatureID = creatureID;
        this.dmg = dmg;
    }

    public MessageMultipartInteract() {
    }

    public static MessageMultipartInteract read(PacketBuffer buf) {
        return new MessageMultipartInteract(buf.readInt(), buf.readFloat());
    }

    public static void write(MessageMultipartInteract message, PacketBuffer buf) {
        buf.writeInt(message.creatureID);
        buf.writeFloat(message.dmg);
    }

    public static class Handler {
        public Handler() {
        }

        public static void handle(MessageMultipartInteract message, Supplier<NetworkEvent.Context> context) {
            context.get().setPacketHandled(true);
            PlayerEntity player = context.get().getSender();
            if(context.get().getDirection().getReceptionSide() == LogicalSide.CLIENT){
                player = IceAndFire.PROXY.getClientSidePlayer();
            }
            if (player != null) {
                if (player.level != null) {
                    Entity entity = player.level.getEntity(message.creatureID);
                    if (entity != null && entity instanceof LivingEntity) {
                        double dist = player.distanceTo(entity);
                        LivingEntity mob = (LivingEntity) entity;
                        if (dist < 100) {
                            if (message.dmg > 0F) {
                                mob.hurt(DamageSource.mobAttack(player), message.dmg);
                            } else {
                                mob.interact(player, Hand.MAIN_HAND);
                            }
                        }
                    }
                }
            }
        }
    }
}