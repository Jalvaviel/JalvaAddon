package com.jalvaviel.addon.utils;

import com.jalvaviel.addon.modules.MushroomBiomeColors;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPBlockData;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.biome.Biome;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ESPBiomeBlock {
    private static final BlockPos.Mutable blockPos = new BlockPos.Mutable();

    //public ESPBiomeGroup group;
    private static final MushroomBiomeColors mushroomBiomeColors = Modules.get().get(MushroomBiomeColors.class);
    public static final int FO = 1 << 1;
    public static final int FO_RI = 1 << 2;
    public static final int RI = 1 << 3;
    public static final int BA_RI = 1 << 4;
    public static final int BA = 1 << 5;
    public static final int BA_LE = 1 << 6;
    public static final int LE = 1 << 7;
    public static final int FO_LE = 1 << 8;

    public static final int TO = 1 << 9;
    public static final int TO_FO = 1 << 10;
    public static final int TO_BA = 1 << 11;
    public static final int TO_RI = 1 << 12;
    public static final int TO_LE = 1 << 13;
    public static final int BO = 1 << 14;
    public static final int BO_FO = 1 << 15;
    public static final int BO_BA = 1 << 16;
    public static final int BO_RI = 1 << 17;
    public static final int BO_LE = 1 << 18;

    public static final int[] SIDES = { FO, BA, LE, RI, TO, BO };

    public final int x, y, z;
    private BlockState state;
    public int neighbours;

    public boolean loaded = true;
    public ESPBiomeBlock(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public ESPBiomeBlock getSideBlock(int side) {
        switch (side) {
            case FO: return mushroomBiomeColors.getBlock(x, y, z + 1);
            case BA: return mushroomBiomeColors.getBlock(x, y, z - 1);
            case LE: return mushroomBiomeColors.getBlock(x - 1, y, z);
            case RI: return mushroomBiomeColors.getBlock(x + 1, y, z);
            case TO: return mushroomBiomeColors.getBlock(x, y + 1, z);
            case BO: return mushroomBiomeColors.getBlock(x, y - 1, z);
        }

        return null;
    }

    public void update() {
        state = mc.world.getBlockState(blockPos.set(x, y, z));
        neighbours = 0;

        // Update neighbours only if the neighbour is in a different biome
        if (isNeighbourDifferentBiome(Direction.SOUTH)) neighbours |= FO;
        if (isNeighbourDifferentBiomeDiagonal(1, 0, 1)) neighbours |= FO_RI;
        if (isNeighbourDifferentBiome(Direction.EAST)) neighbours |= RI;
        if (isNeighbourDifferentBiomeDiagonal(1, 0, -1)) neighbours |= BA_RI;
        if (isNeighbourDifferentBiome(Direction.NORTH)) neighbours |= BA;
        if (isNeighbourDifferentBiomeDiagonal(-1, 0, -1)) neighbours |= BA_LE;
        if (isNeighbourDifferentBiome(Direction.WEST)) neighbours |= LE;
        if (isNeighbourDifferentBiomeDiagonal(-1, 0, 1)) neighbours |= FO_LE;

        if (isNeighbourDifferentBiome(Direction.UP)) neighbours |= TO;
        if (isNeighbourDifferentBiomeDiagonal(0, 1, 1)) neighbours |= TO_FO;
        if (isNeighbourDifferentBiomeDiagonal(0, 1, -1)) neighbours |= TO_BA;
        if (isNeighbourDifferentBiomeDiagonal(1, 1, 0)) neighbours |= TO_RI;
        if (isNeighbourDifferentBiomeDiagonal(-1, 1, 0)) neighbours |= TO_LE;
        if (isNeighbourDifferentBiome(Direction.DOWN)) neighbours |= BO;
        if (isNeighbourDifferentBiomeDiagonal(0, -1, 1)) neighbours |= BO_FO;
        if (isNeighbourDifferentBiomeDiagonal(0, -1, -1)) neighbours |= BO_BA;
        if (isNeighbourDifferentBiomeDiagonal(1, -1, 0)) neighbours |= BO_RI;
        if (isNeighbourDifferentBiomeDiagonal(-1, -1, 0)) neighbours |= BO_LE;

        //if (group == null) assignGroup();
    }

    /*
    private boolean isNeighbour(Direction dir) {
        RegistryKey<Biome> biomeState = mc.world.getBiome(blockPos).getKey().get();
        blockPos.set(x + dir.getOffsetX(), y + dir.getOffsetY(), z + dir.getOffsetZ());
        BlockState neighbourState = mc.world.getBlockState(blockPos);
        RegistryKey<Biome> biome = mc.world.getBiome(blockPos).getKey().get();

        if (biomeState.equals(biome)) return false; //if (neighbourState.getBlock() != state.getBlock()) return false;

        VoxelShape cube = VoxelShapes.fullCube();
        VoxelShape shape = state.getOutlineShape(mc.world, blockPos);
        VoxelShape neighbourShape = neighbourState.getOutlineShape(mc.world, blockPos);

        if (shape.isEmpty()) shape = cube;
        if (neighbourShape.isEmpty()) neighbourShape = cube;

        switch (dir) {
            case SOUTH:
                if (shape.getMax(Direction.Axis.Z) == 1 && neighbourShape.getMin(Direction.Axis.Z) == 0) return true;
                break;

            case NORTH:
                if (shape.getMin(Direction.Axis.Z) == 0 && neighbourShape.getMax(Direction.Axis.Z) == 1) return true;
                break;

            case EAST:
                if (shape.getMax(Direction.Axis.X) == 1 && neighbourShape.getMin(Direction.Axis.X) == 0) return true;
                break;

            case WEST:
                if (shape.getMin(Direction.Axis.X) == 0 && neighbourShape.getMax(Direction.Axis.X) == 1) return true;
                break;

            case UP:
                if (shape.getMax(Direction.Axis.Y) == 1 && neighbourShape.getMin(Direction.Axis.Y) == 0) return true;
                break;

            case DOWN:
                if (shape.getMin(Direction.Axis.Y) == 0 && neighbourShape.getMax(Direction.Axis.Y) == 1) return true;
                break;
        }

        return false;
    }

    private boolean isNeighbourDiagonal(double x, double y, double z) {
        RegistryKey<Biome> biomeState = mc.world.getBiome(blockPos).getKey().get();
        blockPos.set(this.x + x, this.y + y, this.z + z);
        RegistryKey<Biome> biome = mc.world.getBiome(blockPos).getKey().get();
        return biomeState != biome;//state.getBlock() == mc.world.getBlockState(blockPos).getBlock() || biomeState != biome;
    }

     */
    private boolean isDifferentBiomeForEdge(BlockPos blockPos, Direction direction1, Direction direction2) {
        BlockPos neighbor1 = blockPos.offset(direction1);
        BlockPos neighbor2 = blockPos.offset(direction2);
        return !mc.world.getBiome(neighbor1).equals(mc.world.getBiome(neighbor2));
    }

    private boolean isDifferentBiomeForEdge(BlockPos blockPos, Direction direction1, Direction direction2, Direction verticalDirection) {
        BlockPos neighbor1 = blockPos.offset(direction1);
        BlockPos neighbor2 = blockPos.offset(direction2);
        BlockPos verticalNeighbor = blockPos.offset(verticalDirection);
        return !mc.world.getBiome(neighbor1).equals(mc.world.getBiome(verticalNeighbor)) ||
            !mc.world.getBiome(neighbor2).equals(mc.world.getBiome(verticalNeighbor));
    }

    private boolean isDifferentBiomeForSide(BlockPos blockPos, Direction direction) {
        BlockPos neighbor = blockPos.offset(direction);
        return !mc.world.getBiome(blockPos).equals(mc.world.getBiome(neighbor));
    }

    private boolean isNeighbourDifferentBiome(Direction dir) {
        RegistryKey<Biome> biomeState = mc.world.getBiome(blockPos).getKey().get();
        blockPos.set(x + dir.getOffsetX(), y + dir.getOffsetY(), z + dir.getOffsetZ());
        RegistryKey<Biome> biome = mc.world.getBiome(blockPos).getKey().get();

        return !biomeState.equals(biome);
    }

    private boolean isNeighbourDifferentBiomeDiagonal(double x, double y, double z) {
        RegistryKey<Biome> biomeState = mc.world.getBiome(blockPos).getKey().get();
        blockPos.set(this.x + x, this.y + y, this.z + z);
        RegistryKey<Biome> biome = mc.world.getBiome(blockPos).getKey().get();

        return !biomeState.equals(biome);
    }


    public void render(Render3DEvent event) {
        if (neighbours == 0) return; // Skip rendering if all neighbours are in the same biome

        double x1;
        double y1;
        double z1;
        double x2;
        double y2;
        double z2;

        VoxelShape shape = VoxelShapes.fullCube();
        //VoxelShape shape = state.getOutlineShape(mc.world, blockPos);

        x1 = x + shape.getMin(Direction.Axis.X);
        y1 = y + shape.getMin(Direction.Axis.Y);
        z1 = z + shape.getMin(Direction.Axis.Z);
        x2 = x + shape.getMax(Direction.Axis.X);
        y2 = y + shape.getMax(Direction.Axis.Y);
        z2 = z + shape.getMax(Direction.Axis.Z);

        ShapeMode shapeMode = ShapeMode.Both;
        Color lineColor = mushroomBiomeColors.outlineColor.get();
        Color sideColor = mushroomBiomeColors.sideColor.get();
        if (shapeMode.lines()) {
            // Vertical, BA_LE
            if (((neighbours & LE) != LE && (neighbours & BA) != BA) || ((neighbours & LE) == LE && (neighbours & BA) == BA && (neighbours & BA_LE) != BA_LE)) {
                event.renderer.line(x1, y1, z1, x1, y2, z1, lineColor);
            }
            // Vertical, FO_LE
            if (((neighbours & LE) != LE && (neighbours & FO) != FO) || ((neighbours & LE) == LE && (neighbours & FO) == FO && (neighbours & FO_LE) != FO_LE)) {
                event.renderer.line(x1, y1, z2, x1, y2, z2, lineColor);
            }
            // Vertical, BA_RI
            if (((neighbours & RI) != RI && (neighbours & BA) != BA) || ((neighbours & RI) == RI && (neighbours & BA) == BA && (neighbours & BA_RI) != BA_RI)) {
                event.renderer.line(x2, y1, z1, x2, y2, z1, lineColor);
            }
            // Vertical, FO_RI
            if (((neighbours & RI) != RI && (neighbours & FO) != FO) || ((neighbours & RI) == RI && (neighbours & FO) == FO && (neighbours & FO_RI) != FO_RI)) {
                event.renderer.line(x2, y1, z2, x2, y2, z2, lineColor);
            }

            // Horizontal bottom, BA_LE - BA_RI
            if (((neighbours & BA) != BA && (neighbours & BO) != BO) || ((neighbours & BA) != BA && (neighbours & BO_BA) == BO_BA)) {
                event.renderer.line(x1, y1, z1, x2, y1, z1, lineColor);
            }
            // Horizontal bottom, FO_LE - FO_RI
            if (((neighbours & FO) != FO && (neighbours & BO) != BO) || ((neighbours & FO) != FO && (neighbours & BO_FO) == BO_FO)) {
                event.renderer.line(x1, y1, z2, x2, y1, z2, lineColor);
            }
            // Horizontal top, BA_LE - BA_RI
            if (((neighbours & BA) != BA && (neighbours & TO) != TO) || ((neighbours & BA) != BA && (neighbours & TO_BA) == TO_BA)) {
                event.renderer.line(x1, y2, z1, x2, y2, z1, lineColor);
            }
            // Horizontal top, FO_LE - FO_RI
            if (((neighbours & FO) != FO && (neighbours & TO) != TO) || ((neighbours & FO) != FO && (neighbours & TO_FO) == TO_FO)) {
                event.renderer.line(x1, y2, z2, x2, y2, z2, lineColor);
            }

            // Horizontal bottom, BA_LE - FO_LE
            if (((neighbours & LE) != LE && (neighbours & BO) != BO) || ((neighbours & LE) != LE && (neighbours & BO_LE) == BO_LE)) {
                event.renderer.line(x1, y1, z1, x1, y1, z2, lineColor);
            }
            // Horizontal bottom, BA_RI - FO_RI
            if (((neighbours & RI) != RI && (neighbours & BO) != BO) || ((neighbours & RI) != RI && (neighbours & BO_RI) == BO_RI)) {
                event.renderer.line(x2, y1, z1, x2, y1, z2, lineColor);
            }
            // Horizontal top, BA_LE - FO_LE
            if (((neighbours & LE) != LE && (neighbours & TO) != TO) || ((neighbours & LE) != LE && (neighbours & TO_LE) == TO_LE)) {
                event.renderer.line(x1, y2, z1, x1, y2, z2, lineColor);
            }
            // Horizontal top, BA_RI - FO_RI
            if (((neighbours & RI) != RI && (neighbours & TO) != TO) || ((neighbours & RI) != RI && (neighbours & TO_RI) == TO_RI)) {
                event.renderer.line(x2, y2, z1, x2, y2, z2, lineColor);
            }
        }

        // Sides
        if (shapeMode.sides()) {
            // Bottom
            if ((neighbours & BO) != BO) {
                event.renderer.quadHorizontal(x1, y1, z1, x2, z2, sideColor);
            }
            // Top
            if ((neighbours & TO) != TO) {
                event.renderer.quadHorizontal(x1, y2, z1, x2, z2, sideColor);
            }
            // Front
            if ((neighbours & FO) != FO) {
                event.renderer.quadVertical(x1, y1, z2, x2, y2, z2, sideColor);
            }
            // Back
            if ((neighbours & BA) != BA) {
                event.renderer.quadVertical(x1, y1, z1, x2, y2, z1, sideColor);
            }
            // Right
            if ((neighbours & RI) != RI) {
                event.renderer.quadVertical(x2, y1, z1, x2, y2, z2, sideColor);
            }
            // Left
            if ((neighbours & LE) != LE) {
                event.renderer.quadVertical(x1, y1, z1, x1, y2, z2, sideColor);
            }
        }
    }
        //event.renderer.blockSides(x, y, z, sideColor, 0);
        //event.renderer.blockLines(x, y, z, lineColor, 0);
        /*
        if (shapeMode.lines()) {
            // Vertical, BA_LE
            if (isDifferentBiomeForEdge(blockPos, Direction.WEST, Direction.SOUTH) ||
                isDifferentBiomeForEdge(blockPos, Direction.WEST, Direction.SOUTH, Direction.DOWN)) {
                event.renderer.line(x1, y1, z1, x1, y2, z1, lineColor);
            }

            // Vertical, FO_LE
            if (isDifferentBiomeForEdge(blockPos, Direction.WEST, Direction.NORTH) ||
                isDifferentBiomeForEdge(blockPos, Direction.WEST, Direction.NORTH, Direction.DOWN)) {
                event.renderer.line(x1, y1, z2, x1, y2, z2, lineColor);
            }

            // Vertical, BA_RI
            if (isDifferentBiomeForEdge(blockPos, Direction.EAST, Direction.SOUTH) ||
                isDifferentBiomeForEdge(blockPos, Direction.EAST, Direction.SOUTH, Direction.DOWN)) {
                event.renderer.line(x2, y1, z1, x2, y2, z1, lineColor);
            }

            // Vertical, FO_RI
            if (isDifferentBiomeForEdge(blockPos, Direction.EAST, Direction.NORTH) ||
                isDifferentBiomeForEdge(blockPos, Direction.EAST, Direction.NORTH, Direction.DOWN)) {
                event.renderer.line(x2, y1, z2, x2, y2, z2, lineColor);
            }

            // Horizontal bottom, BA_LE - BA_RI
            if (isDifferentBiomeForEdge(blockPos, Direction.SOUTH, Direction.WEST) ||
                isDifferentBiomeForEdge(blockPos, Direction.SOUTH, Direction.WEST, Direction.DOWN)) {
                event.renderer.line(x1, y1, z1, x2, y1, z1, lineColor);
            }

            // Horizontal bottom, FO_LE - FO_RI
            if (isDifferentBiomeForEdge(blockPos, Direction.NORTH, Direction.WEST) ||
                isDifferentBiomeForEdge(blockPos, Direction.NORTH, Direction.WEST, Direction.DOWN)) {
                event.renderer.line(x1, y1, z2, x2, y1, z2, lineColor);
            }

            // Horizontal top, BA_LE - BA_RI
            if (isDifferentBiomeForEdge(blockPos, Direction.SOUTH, Direction.WEST, Direction.UP) ||
                isDifferentBiomeForEdge(blockPos, Direction.SOUTH, Direction.WEST, Direction.UP)) {
                event.renderer.line(x1, y2, z1, x2, y2, z1, lineColor);
            }

            // Horizontal top, FO_LE - FO_RI
            if (isDifferentBiomeForEdge(blockPos, Direction.NORTH, Direction.WEST, Direction.UP) ||
                isDifferentBiomeForEdge(blockPos, Direction.NORTH, Direction.WEST, Direction.UP)) {
                event.renderer.line(x1, y2, z2, x2, y2, z2, lineColor);
            }

            // Horizontal bottom, BA_LE - FO_LE
            if (isDifferentBiomeForEdge(blockPos, Direction.WEST, Direction.SOUTH) ||
                isDifferentBiomeForEdge(blockPos, Direction.WEST, Direction.SOUTH, Direction.DOWN)) {
                event.renderer.line(x1, y1, z1, x1, y1, z2, lineColor);
            }

            // Horizontal bottom, BA_RI - FO_RI
            if (isDifferentBiomeForEdge(blockPos, Direction.EAST, Direction.SOUTH) ||
                isDifferentBiomeForEdge(blockPos, Direction.EAST, Direction.SOUTH, Direction.DOWN)) {
                event.renderer.line(x2, y1, z1, x2, y1, z2, lineColor);
            }

            // Horizontal top, BA_LE - FO_LE
            if (isDifferentBiomeForEdge(blockPos, Direction.WEST, Direction.UP) ||
                isDifferentBiomeForEdge(blockPos, Direction.WEST, Direction.UP, Direction.DOWN)) {
                event.renderer.line(x1, y2, z1, x1, y2, z2, lineColor);
            }

            // Horizontal top, BA_RI - FO_RI
            if (isDifferentBiomeForEdge(blockPos, Direction.EAST, Direction.UP) ||
                isDifferentBiomeForEdge(blockPos, Direction.EAST, Direction.UP, Direction.DOWN)) {
                event.renderer.line(x2, y2, z1, x2, y2, z2, lineColor);
            }
        }

        // Sides
        if (shapeMode.sides()) {
            // Bottom
            if (isDifferentBiomeForSide(blockPos, Direction.DOWN)) {
                event.renderer.quadHorizontal(x1, y1, z1, x2, z2, sideColor);
            }

            // Top
            if (isDifferentBiomeForSide(blockPos, Direction.UP)) {
                event.renderer.quadHorizontal(x1, y2, z1, x2, z2, sideColor);
            }

            // Front
            if (isDifferentBiomeForSide(blockPos, Direction.NORTH)) {
                event.renderer.quadVertical(x1, y1, z2, x2, y2, z2, sideColor);
            }

            // Back
            if (isDifferentBiomeForSide(blockPos, Direction.SOUTH)) {
                event.renderer.quadVertical(x1, y1, z1, x2, y2, z1, sideColor);
            }

            // Right
            if (isDifferentBiomeForSide(blockPos, Direction.EAST)) {
                event.renderer.quadVertical(x2, y1, z1, x2, y2, z2, sideColor);
            }

            // Left
            if (isDifferentBiomeForSide(blockPos, Direction.WEST)) {
                event.renderer.quadVertical(x1, y1, z1, x1, y2, z2, sideColor);
            }
        }
         */


    public static long getKey(int x, int y, int z) {
        return ((long) y << 16) | ((long) (z & 15) << 8) | ((long) (x & 15));
    }

    public static long getKey(BlockPos blockPos) {
        return getKey(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }
}
