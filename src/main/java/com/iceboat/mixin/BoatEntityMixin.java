package com.iceboat.mixin;

import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class BoatEntityMixin {
    
    @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private Vec3d modifyMoveVector(Vec3d movement) {
        Entity entity = (Entity)(Object)this;
        
        // 检查是否是船实体
        if (!(entity instanceof AbstractBoatEntity)) {
            return movement;
        }
        
        AbstractBoatEntity boat = (AbstractBoatEntity)entity;
        World world = boat.getWorld();
        
        // 只在有玩家驾驶时检查
        if (boat.getFirstPassenger() instanceof PlayerEntity) {
            BlockPos boatPos = boat.getBlockPos();
            BlockPos below = boatPos.down();
            Block blockBelow = world.getBlockState(below).getBlock();

            // 只有当船在冰上时才考虑抬升
            if (isIce(blockBelow)) {
                double yaw = Math.toRadians(boat.getYaw());
                double dx = -Math.sin(yaw);
                double dz = Math.cos(yaw);

                int frontX = boatPos.getX() + (int)Math.round(dx);
                int frontY = boatPos.getY();
                int frontZ = boatPos.getZ() + (int)Math.round(dz);
                BlockPos front = new BlockPos(frontX, frontY, frontZ);
                BlockPos frontUp = front.up();
                Block frontBlock = world.getBlockState(front).getBlock();

                // 只有当前方方块是冰，且上方是空气时才触发抬升
                if (isIce(frontBlock) && !world.isAir(front) && world.isAir(frontUp)) {
                    // 只添加垂直提升，不改变水平速度
                    return new Vec3d(
                        movement.x, 
                        movement.y + 0.8, // 垂直提升
                        movement.z
                    );
                }
            }
        }
        
        return movement;
    }

    private boolean isIce(Block block) {
        return block == Blocks.ICE || block == Blocks.PACKED_ICE || block == Blocks.BLUE_ICE || block == Blocks.FROSTED_ICE;
    }
} 