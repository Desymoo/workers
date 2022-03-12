package com.talhanation.workers.network;

import com.talhanation.workers.entities.MinerEntity;
import com.talhanation.workers.entities.ShepherdEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;

public class MessageOpenGuiShepherd implements Message<MessageOpenGuiShepherd> {

    private UUID uuid;
    private UUID worker;


    public MessageOpenGuiShepherd() {
        this.uuid = new UUID(0, 0);
    }

    public MessageOpenGuiShepherd(PlayerEntity player, UUID worker) {
        this.uuid = player.getUUID();
        this.worker = worker;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        if (!context.getSender().getUUID().equals(uuid)) {
            return;
        }

        ServerPlayerEntity player = context.getSender();
        player.level.getEntitiesOfClass(ShepherdEntity.class, player.getBoundingBox()
                .inflate(16.0D), v -> v
                .getUUID()
                .equals(this.worker))
                .stream()
                .filter(Entity::isAlive)
                .findAny()
                .ifPresent(worker -> worker.openGUI(player));
    }

    @Override
    public MessageOpenGuiShepherd fromBytes(PacketBuffer buf) {
        this.uuid = buf.readUUID();
        this.worker = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeUUID(uuid);
        buf.writeUUID(worker);
    }

}