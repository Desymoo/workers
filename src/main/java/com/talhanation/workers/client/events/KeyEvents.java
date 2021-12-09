package com.talhanation.workers.client.events;

import com.talhanation.workers.Main;
import com.talhanation.workers.entities.MinerEntity;
import com.talhanation.workers.network.MessageStartPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class KeyEvents {


    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPlayerEntity clientPlayerEntity = minecraft.player;
        if (clientPlayerEntity == null)
            return;

        if (Main.C_KEY.isDown()) {
            RayTraceResult rayTraceResult = minecraft.hitResult;
            if (rayTraceResult != null) {
                if (rayTraceResult.getType() == RayTraceResult.Type.BLOCK) {
                    BlockRayTraceResult blockraytraceresult = (BlockRayTraceResult) rayTraceResult;
                    BlockPos blockpos = blockraytraceresult.getBlockPos();
                    Main.SIMPLE_CHANNEL.sendToServer(new MessageStartPos(clientPlayerEntity.getUUID(), blockpos));
                }
            }
        }
    }
}
