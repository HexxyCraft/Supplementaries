package net.mehvahdjukaar.supplementaries.entities.goals;

import net.mehvahdjukaar.supplementaries.block.blocks.FodderBlock;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;

import java.util.EnumSet;
import java.util.Random;

public class EatFodderGoal extends MoveToBlockGoal {

    private final AnimalEntity animal;
    private final int blockBreakingTime;
    private int ticksSinceReachedGoal;
    protected int lastBreakProgress = -1;

    public EatFodderGoal(AnimalEntity entity, double speedModifier, int searchRange, int verticalSearchRange, int breakTime) {
        super(entity, speedModifier, searchRange, verticalSearchRange);
        this.animal = entity;
        this.blockBreakingTime = breakTime;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.animal.canFallInLove() || this.animal.getAge() > 0) return false;
        if (!net.minecraftforge.common.ForgeHooks.canEntityDestroy(this.animal.level, this.blockPos, this.animal)) {
            return false;
        } else if (this.nextStartTick > 0) {
            --this.nextStartTick;
            return false;
        } else if (this.tryFindBlock()) {
            //this.nextStartTick = 20;
            return true;
        } else {
            //no blocks around. cooldown
            this.nextStartTick = nextStartTick(this.mob);
            return false;
        }
    }


    @Override
    public boolean canContinueToUse() {
        //try is low so they dont get stuck
        return this.tryTicks >= -100 && this.tryTicks <= 500 && this.isValidTarget(this.mob.level, this.blockPos);
    }

    private boolean tryFindBlock() {
        return this.blockPos != null && this.isValidTarget(this.mob.level, this.blockPos) || this.findNearestBlock();
    }

    @Override
    public void stop() {
        super.stop();
        this.animal.fallDistance = 1.0F;
    }

    @Override
    public void start() {
        super.start();
        this.ticksSinceReachedGoal = 0;
    }

    @Override
    public double acceptedDistance() {
        return 1.375;
    }

    public void playDestroyProgressSound(IWorld p_203114_1_, BlockPos p_203114_2_) {
    }

    private final BlockState FODDER_STATE = ModRegistry.FODDER.get().defaultBlockState();

    @Override
    public void tick() {
        super.tick();
        World world = this.animal.level;
        //BlockPos blockpos = this.removerMob.blockPosition();
        //BlockPos blockpos1 = this.getPosWithBlock(blockpos, world);

        Random random = this.animal.getRandom();
        if (this.isReachedTarget()) {

            BlockPos targetPos = this.getMoveToTarget().below();
            Vector3d vector3d = Vector3d.atBottomCenterOf(targetPos).add(0.0D, 0.6F, 0.0D);
            this.mob.getLookControl().setLookAt(vector3d.x(), vector3d.y(), vector3d.z());
            if (this.ticksSinceReachedGoal > 0) {

                if (!world.isClientSide && ticksSinceReachedGoal % 2 == 0) {

                    ((ServerWorld) world).sendParticles(new BlockParticleData(ParticleTypes.BLOCK, FODDER_STATE),
                            (double) targetPos.getX() + 0.5D, (double) targetPos.getY() + 0.7D, (double) targetPos.getZ() + 0.5D, 3,
                            ((double) random.nextFloat() - 0.5D) * 0.08D, ((double) random.nextFloat() - 0.5D) * 0.08D, ((double) random.nextFloat() - 0.5D) * 0.08D, 0.15F);
                }
            }

            //sound
            if (this.ticksSinceReachedGoal % 6 == 0) {
                this.playDestroyProgressSound(world, this.blockPos);
                //Minecraft.getInstance().particleEngine.destroy(p_180439_3_, blockstate);
            }


            //breaking animation
            int k = (int) ((float) this.ticksSinceReachedGoal / (float) this.blockBreakingTime * 10.0F);
            if (k != this.lastBreakProgress) {
                this.mob.level.destroyBlockProgress(this.mob.getId(), this.blockPos, k);
                this.lastBreakProgress = k;
            }

            //break block
            if (this.ticksSinceReachedGoal > this.blockBreakingTime) {
                int layers = world.getBlockState(targetPos).getValue(FodderBlock.LAYERS);
                if (layers > 1) {
                    world.levelEvent(2001, targetPos, Block.getId(FODDER_STATE));
                    world.setBlock(targetPos, FODDER_STATE.setValue(FodderBlock.LAYERS, layers - 1), 2);
                } else {
                    world.destroyBlock(targetPos, false);
                }
                if(!world.isClientSide) {
                    ((ServerWorld) world).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            this.animal.getX(), this.animal.getY(), this.animal.getZ(), 4,
                            this.animal.getBbWidth(), this.animal.getBbHeight(), this.animal.getBbWidth(), 0);
                }
                //so it stops
                this.nextStartTick = this.nextStartTick(this.mob);
                this.tryTicks = 800;
                if(!this.animal.isBaby()) this.animal.setInLove(null);
                this.animal.ate();
            }

            ++this.ticksSinceReachedGoal;
        }
    }

    @Override
    protected boolean isValidTarget(IWorldReader world, BlockPos pos) {
        IChunk ichunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
        if (ichunk == null) {
            return false;
        } else {
            return ichunk.getBlockState(pos).is(ModRegistry.FODDER.get()) && ichunk.getBlockState(pos.above()).isAir();
        }
    }
}