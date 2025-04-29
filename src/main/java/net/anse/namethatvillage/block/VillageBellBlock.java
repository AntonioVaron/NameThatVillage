package net.anse.namethatvillage.block;

import com.mojang.serialization.MapCodec;
import net.anse.namethatvillage.block.entity.VillageBellBlockEntity;
import net.anse.namethatvillage.init.ModBlockEntities;
import net.anse.namethatvillage.screen.custom.VillageBellScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

public class VillageBellBlock extends BaseEntityBlock {
    public static final MapCodec<VillageBellBlock> CODEC = simpleCodec(VillageBellBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<BellAttachType> ATTACHMENT = BlockStateProperties.BELL_ATTACHMENT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final VoxelShape NORTH_SOUTH_FLOOR_SHAPE = Block.box(0.0, 0.0, 4.0, 16.0, 16.0, 12.0);
    private static final VoxelShape EAST_WEST_FLOOR_SHAPE = Block.box(4.0, 0.0, 0.0, 12.0, 16.0, 16.0);
    private static final VoxelShape BELL_TOP_SHAPE = Block.box(5.0, 6.0, 5.0, 11.0, 13.0, 11.0);
    private static final VoxelShape BELL_BOTTOM_SHAPE = Block.box(4.0, 4.0, 4.0, 12.0, 6.0, 12.0);
    private static final VoxelShape BELL_SHAPE = Shapes.or(BELL_BOTTOM_SHAPE, BELL_TOP_SHAPE);
    private static final VoxelShape NORTH_SOUTH_BETWEEN = Shapes.or(BELL_SHAPE, Block.box(7.0, 13.0, 0.0, 9.0, 15.0, 16.0));
    private static final VoxelShape EAST_WEST_BETWEEN = Shapes.or(BELL_SHAPE, Block.box(0.0, 13.0, 7.0, 16.0, 15.0, 9.0));
    private static final VoxelShape TO_WEST = Shapes.or(BELL_SHAPE, Block.box(0.0, 13.0, 7.0, 13.0, 15.0, 9.0));
    private static final VoxelShape TO_EAST = Shapes.or(BELL_SHAPE, Block.box(3.0, 13.0, 7.0, 16.0, 15.0, 9.0));
    private static final VoxelShape TO_NORTH = Shapes.or(BELL_SHAPE, Block.box(7.0, 13.0, 0.0, 9.0, 15.0, 13.0));
    private static final VoxelShape TO_SOUTH = Shapes.or(BELL_SHAPE, Block.box(7.0, 13.0, 3.0, 9.0, 15.0, 16.0));
    private static final VoxelShape CEILING_SHAPE = Shapes.or(BELL_SHAPE, Block.box(7.0, 13.0, 7.0, 9.0, 16.0, 9.0));
    public static final int EVENT_BELL_RING = 1;

    public VillageBellBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(ATTACHMENT, BellAttachType.FLOOR)
                        .setValue(POWERED, Boolean.FALSE)
        );
    }

    @Override
    public MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean isMoving) {
        boolean hasSignal = level.hasNeighborSignal(pos);
        if (hasSignal != state.getValue(POWERED)) {
            if (hasSignal) {
                this.attemptToRing(level, pos, null);
            }

            level.setBlock(pos, state.setValue(POWERED, Boolean.valueOf(hasSignal)), 3);
        }
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        Entity entity = projectile.getOwner();
        Player player = entity instanceof Player ? (Player)entity : null;
        this.onHit(level, state, hit, player, true);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {

        if(level.getBlockEntity(pos) instanceof VillageBellBlockEntity villageBellBlockEntity) {
            if (player.isCrouching() && !level.isClientSide()) {
                ((ServerPlayer) player).openMenu(new SimpleMenuProvider(villageBellBlockEntity, Component.literal("Village")), pos);
            }
        }
        return this.onHit(level, state, hit, player, true) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    public boolean onHit(Level level, BlockState state, BlockHitResult result, @Nullable Player player, boolean canRingBell) {
        Direction direction = result.getDirection();
        BlockPos blockpos = result.getBlockPos();
        boolean properHit = !canRingBell || this.isProperHit(state, direction, result.getLocation().y - (double)blockpos.getY());



        if (properHit) {
            boolean rang = this.attemptToRing(player, level, blockpos, direction);
            if (rang && player != null) {
                player.awardStat(Stats.BELL_RING);
            }

            return true;
        } else {
            return false;
        }
    }

    private boolean isProperHit(BlockState state, Direction direction, double distanceY) {
        if (direction.getAxis() != Direction.Axis.Y && !(distanceY > 0.8124F)) {
            Direction facing = state.getValue(FACING);
            BellAttachType attachType = state.getValue(ATTACHMENT);

            switch (attachType) {
                case FLOOR:
                    return facing.getAxis() == direction.getAxis();
                case SINGLE_WALL:
                case DOUBLE_WALL:
                    return facing.getAxis() != direction.getAxis();
                case CEILING:
                    return true;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    public boolean attemptToRing(Level level, BlockPos pos, @Nullable Direction direction) {
        return this.attemptToRing(null, level, pos, direction);
    }

    public boolean attemptToRing(@Nullable Entity entity, Level level, BlockPos pos, @Nullable Direction direction) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!level.isClientSide && blockEntity instanceof VillageBellBlockEntity villageBell) {

            if (direction == null) {
                direction = level.getBlockState(pos).getValue(FACING);
            }

            villageBell.onRing();
            level.playSound(null, pos, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 2.0F, 1.0F);

            level.blockEvent(pos, this, 1, direction == null ? 0 : direction.get3DDataValue());
            level.gameEvent(entity, GameEvent.BLOCK_CHANGE, pos);
            return true;
        } else {
            return false;
        }
    }

    private VoxelShape getVoxelShape(BlockState state) {
        Direction direction = state.getValue(FACING);
        BellAttachType attachType = state.getValue(ATTACHMENT);

        if (attachType == BellAttachType.FLOOR) {
            return direction != Direction.NORTH && direction != Direction.SOUTH ? EAST_WEST_FLOOR_SHAPE : NORTH_SOUTH_FLOOR_SHAPE;
        } else if (attachType == BellAttachType.CEILING) {
            return CEILING_SHAPE;
        } else if (attachType == BellAttachType.DOUBLE_WALL) {
            return direction != Direction.NORTH && direction != Direction.SOUTH ? EAST_WEST_BETWEEN : NORTH_SOUTH_BETWEEN;
        } else if (direction == Direction.NORTH) {
            return TO_NORTH;
        } else if (direction == Direction.SOUTH) {
            return TO_SOUTH;
        } else {
            return direction == Direction.EAST ? TO_EAST : TO_WEST;
        }
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.getVoxelShape(state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.getVoxelShape(state);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getClickedFace();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        Direction.Axis axis = direction.getAxis();

        if (axis == Direction.Axis.Y) {
            BlockState state = this.defaultBlockState()
                    .setValue(ATTACHMENT, direction == Direction.DOWN ? BellAttachType.CEILING : BellAttachType.FLOOR)
                    .setValue(FACING, context.getHorizontalDirection());

            if (state.canSurvive(level, pos)) {
                return state;
            }
        } else {
            boolean isDoubleSided = axis == Direction.Axis.X
                    && level.getBlockState(pos.west()).isFaceSturdy(level, pos.west(), Direction.EAST)
                    && level.getBlockState(pos.east()).isFaceSturdy(level, pos.east(), Direction.WEST)
                    || axis == Direction.Axis.Z
                    && level.getBlockState(pos.north()).isFaceSturdy(level, pos.north(), Direction.SOUTH)
                    && level.getBlockState(pos.south()).isFaceSturdy(level, pos.south(), Direction.NORTH);

            BlockState state = this.defaultBlockState()
                    .setValue(FACING, direction.getOpposite())
                    .setValue(ATTACHMENT, isDoubleSided ? BellAttachType.DOUBLE_WALL : BellAttachType.SINGLE_WALL);

            if (state.canSurvive(level, pos)) {
                return state;
            }

            boolean hasFloorSupport = level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
            state = state.setValue(ATTACHMENT, hasFloorSupport ? BellAttachType.FLOOR : BellAttachType.CEILING);

            if (state.canSurvive(level, pos)) {
                return state;
            }
        }

        return null;
    }

    @Override
    protected void onExplosionHit(
            BlockState state, ServerLevel level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> itemDropper
    ) {
        if (explosion.canTriggerBlocks()) {
            this.attemptToRing(level, pos, null);
        }

        super.onExplosionHit(state, level, pos, explosion, itemDropper);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess tick,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random
    ) {
        BellAttachType attachType = state.getValue(ATTACHMENT);
        Direction connectedDirection = getConnectedDirection(state).getOpposite();

        if (direction == connectedDirection && !state.canSurvive(level, pos) && attachType != BellAttachType.DOUBLE_WALL) {
            return Blocks.AIR.defaultBlockState();
        } else {
            if (direction.getAxis() == state.getValue(FACING).getAxis()) {
                if (attachType == BellAttachType.DOUBLE_WALL && !neighborState.isFaceSturdy(level, neighborPos, direction)) {
                    return state.setValue(ATTACHMENT, BellAttachType.SINGLE_WALL).setValue(FACING, direction.getOpposite());
                }

                if (attachType == BellAttachType.SINGLE_WALL
                        && connectedDirection.getOpposite() == direction
                        && neighborState.isFaceSturdy(level, neighborPos, state.getValue(FACING))) {
                    return state.setValue(ATTACHMENT, BellAttachType.DOUBLE_WALL);
                }
            }

            return super.updateShape(state, level, tick, pos, direction, neighborPos, neighborState, random);
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = getConnectedDirection(state).getOpposite();
        return direction == Direction.UP
                ? Block.canSupportCenter(level, pos.above(), Direction.DOWN)
                : FaceAttachedHorizontalDirectionalBlock.canAttach(level, pos, direction);
    }

    private static Direction getConnectedDirection(BlockState state) {
        switch (state.getValue(ATTACHMENT)) {
            case FLOOR:
                return Direction.UP;
            case CEILING:
                return Direction.DOWN;
            default:
                return state.getValue(FACING).getOpposite();
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ATTACHMENT, POWERED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VillageBellBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        return createTickerHelper(entityType, ModBlockEntities.VILLAGE_BELL.get(), VillageBellBlockEntity::tick);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathType) {
        return false;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}