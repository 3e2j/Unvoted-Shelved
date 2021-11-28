package com.cursedcauldron.unvotedandshelved.common.entity.ai.task;

import com.cursedcauldron.unvotedandshelved.common.entity.GlareEntity;
import com.cursedcauldron.unvotedandshelved.core.UnvotedAndShelved;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Random;


//<>

public class GlowberryStrollTask extends Behavior<GlareEntity> {
    private BlockPos darkPos;
    private final int range;
    private final float speed;
    protected GroundPathNavigation groundNavigation;


    public GlowberryStrollTask(int range, float speed) {
        super(ImmutableMap.of(UnvotedAndShelved.GLOWBERRIES_GIVEN, MemoryStatus.VALUE_PRESENT));
        System.out.println("Darkness task initiated!");
        this.range = range;
        this.speed = speed;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, GlareEntity owner) {
        System.out.println("Checking start conditions...");
        System.out.println(owner.getBrain().checkMemory(UnvotedAndShelved.DARK_POS, MemoryStatus.VALUE_PRESENT));
        return !owner.isInWaterOrBubble() && (owner.getBrain().getMemory(UnvotedAndShelved.GLOWBERRIES_GIVEN).get() >= 1);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, GlareEntity glare, long time) {
        System.out.println("Checking can still use...");
        return (glare.getBrain().getMemory(UnvotedAndShelved.GLOWBERRIES_GIVEN).get() >= 1) && (this.darkPos != null);
    }

    private boolean pathfindDirectlyTowards(BlockPos blockPos, GlareEntity entity) {
        entity.getNavigation().moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1.0D);
        return entity.getNavigation().getPath() != null && entity.getNavigation().getPath().canReach();
    }


    protected void getDarkPos(ServerLevel level, GlareEntity glare) {
        if (this.darkPos == null) {
            System.out.println("Getting dark pos...");
            for (int x = getRandomNumber(0, -range); x <= getRandomNumber(0, range); x++) {
                for (int z = getRandomNumber(0, -range); z <= getRandomNumber(0, range); z++) {
                    for (int y = getRandomNumber(0, -range); y <= getRandomNumber(0, range); y++) {
                        BlockPos entityPos = glare.blockPosition();
                        BlockPos blockPos2 = new BlockPos(entityPos.getX() + x, entityPos.getY() + y, entityPos.getZ() + z);
                            if ((level.isInWorldBounds(blockPos2) && level.getBlockState(blockPos2).isAir() && level.isEmptyBlock(blockPos2) && (level.getBlockState(blockPos2).isPathfindable(level, blockPos2, PathComputationType.LAND)) &&
                                    ((level.getBrightness(LightLayer.BLOCK, blockPos2) == 0 && level.getBrightness(LightLayer.SKY, blockPos2) == 0) ||
                                            (level.getBrightness(LightLayer.BLOCK, blockPos2) == 0 && level.isNight()) ||
                                            (level.getBrightness(LightLayer.BLOCK, blockPos2) == 0 && level.isThundering())))) {
                                System.out.println(blockPos2);
                                glare.getBrain().setMemory(UnvotedAndShelved.DARK_POS, blockPos2);
                                this.darkPos = blockPos2;
                                return;
                            }
                        }
                    }
                }
            }
        }

    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    private static int getRandomOffset(Random random) {
        return random.nextInt(3) - 1;
    }

    private static BlockPos getNearbyPos(GlareEntity mob, BlockPos blockPos) {
        Random random = mob.level.random;
        return blockPos.offset(getRandomOffset(random), 0, getRandomOffset(random));
    }

//    @Override
//    protected void tick(ServerLevel level, GlareEntity entity, long time) {
//        System.out.println("Ticking...");
//        super.tick(level, entity, time);
//        if (this.darkPos != null) {
//            System.out.println("Navigating");
//            Brain<GlareEntity> brain = entity.getBrain();
//            BlockPos entityPos = entity.blockPosition();
//            if ((level.isInWorldBounds(darkPos) && level.getBlockState(darkPos).isAir() && level.isEmptyBlock(darkPos) && level.getBlockState(darkPos).isPathfindable(level, entityPos, PathComputationType.LAND) &&
//                    ((level.getBrightness(LightLayer.BLOCK, darkPos) == 0 && level.getBrightness(LightLayer.SKY, darkPos) == 0) ||
//                            (level.getBrightness(LightLayer.BLOCK, darkPos) == 0 && level.isNight()) ||
//                            (level.getBrightness(LightLayer.BLOCK, darkPos) == 0 && level.isThundering())))) {
//                System.out.println("Found a dark spot!");
//                int i = brain.getMemory(UnvotedAndShelved.GLOWBERRIES_GIVEN).get();
//                if (brain.getMemory(UnvotedAndShelved.GLOWBERRIES_GIVEN).isPresent()) {
//                    BlockPos blockPos = new BlockPos(this.darkPos.getX(), this.darkPos.getY(), this.darkPos.getZ());
//                    BehaviorUtils.setWalkAndLookTargetMemories(entity, getNearbyPos(entity, blockPos), this.speed, 3);
//                    if (entity.blockPosition().closerThan(darkPos, 3)) {
//                        entity.setLightblock(blockPos);
//                        entity.setGlowberries(i - 1);
//                        this.darkPos = null;
//                    }
//                }
//            } else {
//                this.darkPos = null;
//            }
//        }
//    }

    @Override
    protected void tick(ServerLevel level, GlareEntity entity, long time) {
        System.out.println("Ticking...");
        super.tick(level, entity, time);
        if (this.darkPos != null) {
            System.out.println("Navigating");
            Brain<GlareEntity> brain = entity.getBrain();
            BlockPos entityPos = entity.blockPosition();
            if ((level.isInWorldBounds(darkPos) && level.getBlockState(darkPos).isAir() && level.isEmptyBlock(darkPos) && level.getBlockState(darkPos).isPathfindable(level, entityPos, PathComputationType.LAND) &&
                    ((level.getBrightness(LightLayer.BLOCK, darkPos) == 0 && level.getBrightness(LightLayer.SKY, darkPos) == 0) ||
                            (level.getBrightness(LightLayer.BLOCK, darkPos) == 0 && level.isNight()) ||
                            (level.getBrightness(LightLayer.BLOCK, darkPos) == 0 && level.isThundering())))) {
                System.out.println("Found a dark spot!");
                int i = brain.getMemory(UnvotedAndShelved.GLOWBERRIES_GIVEN).get();
                if (brain.getMemory(UnvotedAndShelved.GLOWBERRIES_GIVEN).isPresent()) {
                    boolean bl = this.pathfindDirectlyTowards(darkPos, entity);
                    if (bl) {
                        BlockPos blockPos = new BlockPos(this.darkPos.getX(), this.darkPos.getY(), this.darkPos.getZ());
                        BehaviorUtils.setWalkAndLookTargetMemories(entity, getNearbyPos(entity, blockPos), this.speed, 3);
                        if (entity.blockPosition().closerThan(darkPos, 3)) {
                            entity.setLightblock(blockPos);
                            entity.setGlowberries(i - 1);
                            this.darkPos = null;
                        }
                    } else {
                        this.darkPos = null;
                    }
                }
            }
        }
    }

    @Override
    protected void stop(ServerLevel level, GlareEntity entity, long time) {
        System.out.println("Stopping!");
        if (this.darkPos != null) {
            System.out.println("Navigating");
            Brain<GlareEntity> brain = entity.getBrain();
            BlockPos entityPos = entity.blockPosition();
            if ((level.isInWorldBounds(darkPos) && level.getBlockState(darkPos).isAir() && level.isEmptyBlock(darkPos) && level.getBlockState(darkPos).isPathfindable(level, entityPos, PathComputationType.LAND) &&
                    ((level.getBrightness(LightLayer.BLOCK, darkPos) == 0 && level.getBrightness(LightLayer.SKY, darkPos) == 0) ||
                            (level.getBrightness(LightLayer.BLOCK, darkPos) == 0 && level.isNight()) ||
                            (level.getBrightness(LightLayer.BLOCK, darkPos) == 0 && level.isThundering())))) {
                System.out.println("Found a dark spot!");
                int i = brain.getMemory(UnvotedAndShelved.GLOWBERRIES_GIVEN).get();
                if (brain.getMemory(UnvotedAndShelved.GLOWBERRIES_GIVEN).isPresent()) {
                    if (brain.getMemory(UnvotedAndShelved.GLOWBERRIES_GIVEN).isPresent()) {
                        boolean bl = this.pathfindDirectlyTowards(darkPos, entity);
                        if (bl) {
                            BlockPos blockPos = new BlockPos(this.darkPos.getX(), this.darkPos.getY(), this.darkPos.getZ());
                            BehaviorUtils.setWalkAndLookTargetMemories(entity, getNearbyPos(entity, blockPos), this.speed, 3);
                            if (entity.blockPosition().closerThan(darkPos, 3)) {
                                entity.setLightblock(blockPos);
                                entity.setGlowberries(i - 1);
                                this.darkPos = null;
                            }
                        } else {
                            this.darkPos = null;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void start(ServerLevel level, GlareEntity entity, long time) {
        System.out.println("Starting!");
        this.groundNavigation = new GroundPathNavigation(entity, level);
        this.getDarkPos(level, entity);
        if (this.darkPos != null) {
            System.out.println("Navigating");
            Brain<GlareEntity> brain = entity.getBrain();
            BlockPos entityPos = entity.blockPosition();
            if ((level.isInWorldBounds(darkPos) && level.getBlockState(darkPos).isAir() && level.isEmptyBlock(darkPos) && level.getBlockState(darkPos).isPathfindable(level, entityPos, PathComputationType.LAND) &&
                    ((level.getBrightness(LightLayer.BLOCK, darkPos) == 0 && level.getBrightness(LightLayer.SKY, darkPos) == 0) ||
                            (level.getBrightness(LightLayer.BLOCK, darkPos) == 0 && level.isNight()) ||
                            (level.getBrightness(LightLayer.BLOCK, darkPos) == 0 && level.isThundering())))) {
                System.out.println("Found a dark spot!");
                int i = brain.getMemory(UnvotedAndShelved.GLOWBERRIES_GIVEN).get();
                if (brain.getMemory(UnvotedAndShelved.GLOWBERRIES_GIVEN).isPresent()) {
                    if (brain.getMemory(UnvotedAndShelved.GLOWBERRIES_GIVEN).isPresent()) {
                        boolean bl = this.pathfindDirectlyTowards(darkPos, entity);
                        if (bl) {
                            BlockPos blockPos = new BlockPos(this.darkPos.getX(), this.darkPos.getY(), this.darkPos.getZ());
                            BehaviorUtils.setWalkAndLookTargetMemories(entity, getNearbyPos(entity, blockPos), this.speed, 3);
                            if (entity.blockPosition().closerThan(darkPos, 3)) {
                                entity.setLightblock(blockPos);
                                entity.setGlowberries(i - 1);
                                this.darkPos = null;
                            }
                        } else {
                            this.darkPos = null;
                        }
                    }
                }
            }
        }
    }
}