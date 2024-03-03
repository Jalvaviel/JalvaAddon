package com.jalvaviel.addon.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import static java.lang.Math.max;

public class CanvasBox {
    private Box box3D;
    private boolean isFlat = false;
    private Utils.Axis horizontalAxis = Utils.Axis.UNKNOWN;
    public CanvasBox(BlockPos blockPos1, BlockPos blockPos2){
        if((blockPos2.getX()-blockPos1.getX()) == 0){
            isFlat = true;
            horizontalAxis = Utils.Axis.Z;
        }else if((blockPos2.getZ()-blockPos1.getZ()) == 0){
            isFlat = true;
            horizontalAxis = Utils.Axis.X;
        }
        Box tempBox = new Box(blockPos1.toCenterPos(), blockPos2.toCenterPos());
        box3D = new Box(tempBox.minX-0.5, tempBox.minY-0.5, tempBox.minZ-0.5, tempBox.maxX+0.5, tempBox.maxY+0.5, tempBox.maxZ+0.5);
    }
    public double getMinHorizontal(){
        return box3D.getLengthX() >= box3D.getLengthZ() ? box3D.minX : box3D.minZ;
    }
    public double getMinVertical(){
        return box3D.minY;
    }
    public double getMaxHorizontal(){
        return box3D.getLengthX() >= box3D.getLengthZ() ? box3D.maxX : box3D.maxZ;
    }
    public double getMaxVertical(){
        return box3D.maxY;
    }
    public Utils.Axis getHorizontalAxis(){
        return horizontalAxis;
    }
    public double getHorizontalLength(){
        switch(horizontalAxis){
            case X:
                return box3D.getLengthX();
            case Z:
                return box3D.getLengthZ();
            default:
                return 0;
        }
    }
    public double getVerticalLength(){
        return box3D.getLengthY();
    }

    public Box getBox3D() {
        return box3D;
    }

    public boolean isFlat(){
        return isFlat;
    }

}

