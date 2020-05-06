package erogenousbeef.bigreactors.common.multiblock.tileentity;

import erogenousbeef.bigreactors.common.BRConfig;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.interfaces.INeighborUpdatableEntity;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.tile.IEnergyStorage;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityReactorPowerTap extends TileEntityReactorPart implements IEnergyStorage, IEnergySource, INeighborUpdatableEntity {
	IEnergyAcceptor	euNetwork;
	
	public TileEntityReactorPowerTap() {
		super();
		
		euNetwork = null;
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlock) {
		if(isConnected()) {
			checkForConnections(world, x, y, z);
		}
	}

	@Override
	public void onNeighborTileChange(IBlockAccess world, int x, int y, int z, int neighborX, int neighborY, int neighborZ) {
		if(isConnected()) {
			checkForConnections(world, x, y, z);
		}
	}

	// IMultiblockPart
	@Override
	public void onAttached(MultiblockControllerBase newController) {
		super.onAttached(newController);
		
		checkForConnections(this.worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public void onMachineAssembled(MultiblockControllerBase multiblockControllerBase) {
		super.onMachineAssembled(multiblockControllerBase);

		checkForConnections(this.worldObj, xCoord, yCoord, zCoord);
		
		// Force a connection to the power taps
		this.notifyNeighborsOfTileChange();
	}

	// Custom PowerTap methods
	/**
	 * Check for a world connection, if we're assembled.
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 */
	protected void checkForConnections(IBlockAccess world, int x, int y, int z) {
		boolean wasConnected = (euNetwork != null);
		ForgeDirection out = getOutwardsDir();
		if(out == ForgeDirection.UNKNOWN) {
			wasConnected = false;
			euNetwork = null;
		}
		else {
			// See if our adjacent non-reactor coordinate has a TE
			euNetwork = null;

			TileEntity te = world.getTileEntity(x + out.offsetX, y + out.offsetY, z + out.offsetZ);
			if(!(te instanceof TileEntityReactorPowerTap)) {
				// Skip power taps, as they implement these APIs and we don't want to shit energy back and forth
				if(te instanceof IEnergyAcceptor) {
					IEnergyAcceptor handler = (IEnergyAcceptor)te;
					if(handler.acceptsEnergyFrom(this, out.getOpposite())) {
						euNetwork = handler;
					}
				}
			}
			
		}
		
		boolean isConnected = (euNetwork != null);
		if(wasConnected != isConnected) {
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	/** This will be called by the Reactor Controller when this tap should be providing power.
	 * @return Power units remaining after consumption.
	 */
	public int onProvidePower(int units) {
		if(euNetwork == null) {
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
		}else {
			ForgeDirection approachDirection = getOutwardsDir().getOpposite();
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
		}
		return units;
	}

	public boolean hasEnergyConnection() { return euNetwork != null; }

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction) {
		if(!this.isConnected())
			return false;
		if(direction == getOutwardsDir()) {
			if(receiver instanceof IEnergyAcceptor) {
				IEnergyAcceptor handler = (IEnergyAcceptor)receiver;
				handler.acceptsEnergyFrom(receiver, direction);
				return true;
			}
		}
		return false;
	}

	@Override
	public double getOfferedEnergy() {
		if(!this.isConnected())
			return 0;
		return this.getOutput();
	}

	@Override
	public void drawEnergy(double amount) {}

	@Override
	public int getSourceTier() {
		return 4;
	}

	@Override
	public int getStored() {
		ForgeDirection from = getOutwardsDir().getOpposite();
		return this.getReactorController().getEnergyStored(from);
	}

	@Override
	public void setStored(int energy) {}

	@Override
	public int addEnergy(int amount) {
		return 0;
	}

	@Override
	public int getCapacity() {
		return 0;
	}

	@Override
	public int getOutput() {
		if(!this.isConnected())
			return 0;
		ForgeDirection direction = getOutwardsDir().getOpposite();
		return this.getReactorController().extractEnergy(direction, (int)this.getOutputEnergyUnitsPerTick(), false);
	}

	@Override
	public double getOutputEnergyUnitsPerTick() {
		if(this.getReactorController().getEnergyStored() >= 500)
			return this.getReactorController().getEnergyGeneratedLastTick() + 500;
		else
			return this.getReactorController().getEnergyGeneratedLastTick() + this.getReactorController().getEnergyStored();
	}

	@Override
	public boolean isTeleporterCompatible(ForgeDirection side) {
		return false;
	}

}
