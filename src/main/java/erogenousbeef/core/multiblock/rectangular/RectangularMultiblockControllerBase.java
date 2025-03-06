package erogenousbeef.core.multiblock.rectangular;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import erogenousbeef.core.multiblock.MultiblockValidationException;

public abstract class RectangularMultiblockControllerBase extends
		MultiblockControllerBase {

	protected RectangularMultiblockControllerBase(World world) {
		super(world);
	}
	
	/**
	 * @return Basis as short where X - 1, Y - 0, Z - 3 check where max, because height is max as default
	 */
	protected short basis(int dX, int dY, int dZ) {
		int max1 = Math.max(dX, dZ);
		int maxF = Math.max(max1, dY);
		if(maxF == dY)
			return 0; //default basis is Y oriented
		if(maxF == dX)
			return 1;
		if(maxF == dZ)
			return 2;
		return 0;
	}
	
	/**
	 * @return True if the machine is "whole" and should be assembled. False otherwise.
	 */
	protected void isMachineWhole() throws MultiblockValidationException {
		if(connectedParts.size() < getMinimumNumberOfBlocksForAssembledMachine()) {
			throw new MultiblockValidationException(StatCollector.translateToLocal("Machine is too small."));
		}
		
		CoordTriplet maximumCoord = getMaximumCoord();
		CoordTriplet minimumCoord = getMinimumCoord();
		
		int minChunkX = minimumCoord.x >> 4;
		int minChunkZ = minimumCoord.z >> 4;
		int maxChunkX = maximumCoord.x >> 4;
		int maxChunkZ = maximumCoord.z >> 4;
		
		// Quickly check for exceeded dimensions
		int deltaX = maximumCoord.x - minimumCoord.x + 1;
		int deltaY = maximumCoord.y - minimumCoord.y + 1;
		int deltaZ = maximumCoord.z - minimumCoord.z + 1;
		
		int maxX = getMaximumXSize();
		int maxY = getMaximumYSize();
		int maxZ = getMaximumZSize();
		int minX = getMinimumXSize();
		int minY = getMinimumYSize();
		int minZ = getMinimumZSize();
		
		short basis = basis(deltaX, deltaY, deltaZ);
		
		if(basis == 0) {
			if(maxX > 0 && deltaX > maxX) { throw new MultiblockValidationException(String.format(StatCollector.translateToLocal("Machine is too large, it may be at most %d blocks in the X dimension"), maxX)); }
			if(maxY > 0 && deltaY > maxY) { throw new MultiblockValidationException(String.format(StatCollector.translateToLocal("Machine is too large, it may be at most %d blocks in the Y dimension"), maxY)); }
			if(maxZ > 0 && deltaZ > maxZ) { throw new MultiblockValidationException(String.format(StatCollector.translateToLocal("Machine is too large, it may be at most %d blocks in the Z dimension"), maxZ)); }
		}
		if(basis == 1) {
			if(maxX > 0 && deltaY > maxX) { throw new MultiblockValidationException(String.format(StatCollector.translateToLocal("Machine is too large, it may be at most %d blocks in the Y dimension"), maxX)); }
			if(maxY > 0 && deltaX > maxY) { throw new MultiblockValidationException(String.format(StatCollector.translateToLocal("Machine is too large, it may be at most %d blocks in the X dimension"), maxY)); }
			if(maxZ > 0 && deltaZ > maxZ) { throw new MultiblockValidationException(String.format(StatCollector.translateToLocal("Machine is too large, it may be at most %d blocks in the Z dimension"), maxZ)); }
		}
		if(basis == 2) {
			if(maxX > 0 && deltaX > maxX) { throw new MultiblockValidationException(String.format(StatCollector.translateToLocal("Machine is too large, it may be at most %d blocks in the X dimension"), maxX)); }
			if(maxY > 0 && deltaZ > maxY) { throw new MultiblockValidationException(String.format(StatCollector.translateToLocal("Machine is too large, it may be at most %d blocks in the Z dimension"), maxY)); }
			if(maxZ > 0 && deltaY > maxZ) { throw new MultiblockValidationException(String.format(StatCollector.translateToLocal("Machine is too large, it may be at most %d blocks in the Y dimension"), maxZ)); }
		}
		if(deltaX < minX) { throw new MultiblockValidationException(String.format(StatCollector.translateToLocal("Machine is too small, it must be at least %d blocks in the X dimension"), minX)); }
		if(deltaY < minY) { throw new MultiblockValidationException(String.format(StatCollector.translateToLocal("Machine is too small, it must be at least %d blocks in the Y dimension"), minY)); }
		if(deltaZ < minZ) { throw new MultiblockValidationException(String.format(StatCollector.translateToLocal("Machine is too small, it must be at least %d blocks in the Z dimension"), minZ)); }

		// Now we run a simple check on each block within that volume.
		// Any block deviating = NO DEAL SIR
		TileEntity te;
		RectangularMultiblockTileEntityBase part;
		Class<? extends RectangularMultiblockControllerBase> myClass = this.getClass();

		for(int x = minimumCoord.x; x <= maximumCoord.x; x++) {
			for(int y = minimumCoord.y; y <= maximumCoord.y; y++) {
				for(int z = minimumCoord.z; z <= maximumCoord.z; z++) {
					// Okay, figure out what sort of block this should be.
					
					te = this.worldObj.getTileEntity(x, y, z);
					if(te instanceof RectangularMultiblockTileEntityBase) {
						part = (RectangularMultiblockTileEntityBase)te;
						
						// Ensure this part should actually be allowed within a cube of this controller's type
						if(!myClass.equals(part.getMultiblockControllerType())){
							throw new MultiblockValidationException(String.format(StatCollector.translateToLocal("Part @ %d, %d, %d is incompatible with machines of type %s"), x, y, z, myClass.getSimpleName()));
						}
					}
					else {
						// This is permitted so that we can incorporate certain non-multiblock parts inside interiors
						part = null;
					}
					
					// Validate block type against both part-level and material-level validators.
					int extremes = 0;
					if(x == minimumCoord.x) { extremes++; }
					if(y == minimumCoord.y) { extremes++; }
					if(z == minimumCoord.z) { extremes++; }
					
					if(x == maximumCoord.x) { extremes++; }
					if(y == maximumCoord.y) { extremes++; }
					if(z == maximumCoord.z) { extremes++; }
					
					if(extremes >= 2) {
						if(part != null) {
							part.isGoodForFrame();
						}
						else {
							isBlockGoodForFrame(this.worldObj, x, y, z);
						}
					}
					else if(extremes == 1) {
						if(y == maximumCoord.y) {
							if(part != null) {
								part.isGoodForTop();
							}
							else {
								isBlockGoodForTop(this.worldObj, x, y, z);
							}
						}
						else if(y == minimumCoord.y) {
							if(part != null) {
								part.isGoodForBottom();
							}
							else {
								isBlockGoodForBottom(this.worldObj, x, y, z);
							}
						}
						else {
							// Side
							if(part != null) {
								part.isGoodForSides();
							}
							else {
								isBlockGoodForSides(this.worldObj, x, y, z);
							}
						}
					}
					else {
						if(part != null) {
							part.isGoodForInterior();
						}
						else {
							isBlockGoodForInterior(this.worldObj, x, y, z);
						}
					}
				}
			}
		}
		
		//Chunk checks
		if(maxChunkX-minChunkX > 0 || maxChunkZ-minChunkZ > 0) {
			throw new MultiblockValidationException(StatCollector.translateToLocal("The machine can only be placed in 1 chunk"));
		}
	}	
	
}
