package erogenousbeef.bigreactors.client.renderer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.api.registry.Keys;
import erogenousbeef.bigreactors.common.BigReactors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.RenderHandEvent;

public class ChunkRenderer {

	@SubscribeEvent
	public void onRenderPlayer(RenderHandEvent e){
		if(Keys.getKey("chunkDrawing").isPressed())
			BigReactors.drawChunk = !BigReactors.drawChunk;
		if(!BigReactors.drawChunk) return;
		GL11.glPushMatrix();
		EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
		if(player.inventory.getCurrentItem() != null) {
			if(player.inventory.getCurrentItem().getItem() == Item.getItemFromBlock(BigReactors.blockTurbinePart) ||
					player.inventory.getCurrentItem().getItem() == Item.getItemFromBlock(BigReactors.blockMultiblockCreativePart) ||
					player.inventory.getCurrentItem().getItem() == Item.getItemFromBlock(BigReactors.blockReactorRedstonePort) ||
					player.inventory.getCurrentItem().getItem() == Item.getItemFromBlock(BigReactors.blockMultiblockGlass) ||
					player.inventory.getCurrentItem().getItem() == Item.getItemFromBlock(BigReactors.blockTurbineRotorPart) ||
					player.inventory.getCurrentItem().getItem() == Item.getItemFromBlock(BigReactors.blockYelloriumFuelRod) ||
					player.inventory.getCurrentItem().getItem() == Item.getItemFromBlock(BigReactors.blockReactorPart)){
				translateToWorldCoords(player, e.partialTicks);
				renderChunkBounds(player);
			}
	    }
		GL11.glPopMatrix();
	}
	
    public static void translateToWorldCoords(Entity entity, float frame) {
        double interpPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * frame;
        double interpPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * frame;
        double interpPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * frame;

        GL11.glTranslated(-interpPosX, -interpPosY, -interpPosZ);
    }
	
	 private static void renderChunkBounds(Entity entity) {
	            
	        GL11.glDisable(GL11.GL_TEXTURE_2D);
	        GL11.glEnable(GL11.GL_BLEND);
	        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	        GL11.glDisable(GL11.GL_LIGHTING);
	        GL11.glLineWidth(1.5F);
	        GL11.glBegin(GL11.GL_LINES);

	       
	                double x1 = (entity.chunkCoordX) << 4;
	                double z1 = (entity.chunkCoordZ) << 4;
	                double x2 = x1 + 16;
	                double z2 = z1 + 16;

	                double dy = 128;
	                double y1 = Math.floor(entity.posY - dy / 2);
	                double y2 = y1 + dy;
	                if (y1 < 0) {
	                    y1 = 0;
	                    y2 = dy;
	                }

	                if (y1 > entity.worldObj.getHeight()) {
	                    y2 = entity.worldObj.getHeight();
	                    y1 = y2 - dy;
	                }

	                    dy = 32;
	                    y1 = Math.floor(entity.posY - dy / 2);
	                    y2 = y1 + dy;
	                    if (y1 < 0) {
	                        y1 = 0;
	                        y2 = dy;
	                    }

	                    if (y1 > entity.worldObj.getHeight()) {
	                        y2 = entity.worldObj.getHeight();
	                        y1 = y2 - dy;
	                    }
	                    GL11.glColor4d(0.9, 0, 0, 0.6);

		                GL11.glVertex3d(x2, y1, z2);
		                GL11.glVertex3d(x2, y2, z2);
		                
		                GL11.glVertex3d(x2, y1, z1);
		                GL11.glVertex3d(x2, y2, z1);
		                    
		                GL11.glVertex3d(x1, y1, z2);
		                GL11.glVertex3d(x1, y2, z2);
		                
		                GL11.glVertex3d(x1, y1, z1);
		                GL11.glVertex3d(x1, y2, z1);
		                
		                GL11.glColor4d(0, 0.9, 0, 0.4);
	                    for (double y = (int) y1; y <= y2; y++) {
	                    	GL11.glVertex3d(x2, y, z1);
	                        GL11.glVertex3d(x2, y, z2);
	                        GL11.glVertex3d(x1, y, z1);
	                        GL11.glVertex3d(x1, y, z2);
	                        GL11.glVertex3d(x1, y, z2);
	                        GL11.glVertex3d(x2, y, z2);
	                        GL11.glVertex3d(x1, y, z1);
	                        GL11.glVertex3d(x2, y, z1);
	                    }
	                    for (double h = 1; h <= 15; h++) {
	                        GL11.glVertex3d(x1 + h, y1, z1);
	                        GL11.glVertex3d(x1 + h, y2, z1);
	                        GL11.glVertex3d(x1 + h, y1, z2);
	                        GL11.glVertex3d(x1 + h, y2, z2);
	                        GL11.glVertex3d(x1, y1, z1 + h);
	                        GL11.glVertex3d(x1, y2, z1 + h);
	                        GL11.glVertex3d(x2, y1, z1 + h);
	                        GL11.glVertex3d(x2, y2, z1 + h);
	                    }

	        /*
	         *   GL11.glEnd();
	                    GL11.glColor4d(0, 0.9, 0, 0.4);
	                  //  for (double y = (int) y1; y <= y2; y++) {
	                    GL11.glBegin(GL11.GL_QUADS);
	                    GL11.glVertex3d(x2, y1, z2);
	                    GL11.glVertex3d(x1, y1, z2);
	                    GL11.glVertex3d(x1, y2, z2);
	                    GL11.glVertex3d(x2, y2, z2);
	                    GL11.glEnd();
	                    GL11.glBegin(GL11.GL_QUADS);
	                    GL11.glVertex3d(x1, y1, z1);
	                    GL11.glVertex3d(x2, y1, z1);
	                    GL11.glVertex3d(x2, y2, z1);
	                    GL11.glVertex3d(x1, y2, z1);
	                    GL11.glEnd();
	                    GL11.glBegin(GL11.GL_QUADS);
	                    GL11.glVertex3d(x1, y1, z2);
	                    GL11.glVertex3d(x1, y1, z1);
	                    GL11.glVertex3d(x1, y2, z1);
	                    GL11.glVertex3d(x1, y2, z2);
	                    GL11.glEnd();
	                    GL11.glBegin(GL11.GL_QUADS);
	                    GL11.glVertex3d(x2, y1, z1);
	                    GL11.glVertex3d(x2, y1, z2);
	                    GL11.glVertex3d(x2, y2, z2);
	                    GL11.glVertex3d(x2, y2, z1);
	                    GL11.glEnd();
	         */
	        GL11.glEnd();
	        GL11.glEnable(GL11.GL_LIGHTING);
	        GL11.glEnable(GL11.GL_TEXTURE_2D);
	        GL11.glDisable(GL11.GL_BLEND);
	    }
}
