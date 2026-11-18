package fluffical.radiusinteract;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class TargetBlock {
    public BlockPos BlockPosition;
    public BlockHitResult HitResult;
    public Direction Direction;
    public Vec3 ClosestPoint;
    public boolean CanInteract = false;
}
