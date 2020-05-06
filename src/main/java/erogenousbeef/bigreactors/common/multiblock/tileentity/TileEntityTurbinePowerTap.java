package erogenousbeef.bigreactors.common.multiblock.tileentity;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import erogenousbeef.bigreactors.common.multiblock.interfaces.INeighborUpdatableEntity;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.tile.IEnergyStorage;

public class TileEntityTurbinePowerTap extends TileEntityTurbinePartStandard implements IEnergyStorage, IEnergySource, INeighborUpdatableEntity {

	IEnergyAcceptor	euNetwork;
	
	public TileEntityTurbinePowerTap() {
		super();
		euNetwork = null;
	}

	// INeighborUpdatableEntity
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

	public boolean isAttachedToPowerNetwork() {
		return euNetwork != null;
	}
	
	// IMultiblockPart
	@Override
	public void onAttached(MultiblockControllerBase newController) {
		super.onAttached(newController);
		
		checkForConnections(this.worldObj, xCoord, yCoord, zCoord);
		
		this.notifyNeighborsOfTileChange();
	}
	
	@Override
	public void onMachineAssembled(MultiblockControllerBase multiblockControllerBase) {
		super.onMachineAssembled(multiblockControllerBase);

		
		checkForConnections(this.worldObj, xCoord, yCoord, zCoord);
		
		this.notifyNeighborsOfTileChange();
	}
	
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
		if(wasConnected != isConnected && worldObj.isRemote) {
			// Re-render on clients
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
		return this.getTurbine().getEnergyStored(from);
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
		return this.getTurbine().extractEnergy(direction, (int)this.getOutputEnergyUnitsPerTick(), false);
	}

	@Override
	public double getOutputEnergyUnitsPerTick() {
		if(this.getTurbine().getEnergyStored() >= 500)
			return this.getTurbine().getEnergyGeneratedLastTick() + 500;
		else
			return this.getTurbine().getEnergyGeneratedLastTick() + this.getTurbine().getEnergyStored();
	}

	@Override
	public boolean isTeleporterCompatible(ForgeDirection side) {
		return false;
	}}
