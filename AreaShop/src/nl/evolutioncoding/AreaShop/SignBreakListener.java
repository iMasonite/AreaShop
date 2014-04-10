package nl.evolutioncoding.AreaShop;

import java.util.HashMap;

import nl.evolutioncoding.AreaShop.AreaShop.RegionEventType;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;

/**
 * Checks for placement of signs for this plugin
 * @author NLThijs48
 */
public final class SignBreakListener implements Listener {
	AreaShop plugin;
	
	/**
	 * Constructor
	 * @param plugin The AreaShop plugin
	 */
	public SignBreakListener(AreaShop plugin) {
		this.plugin = plugin;
	}
	
	
	/**
	 * Called when a block is broken
	 * @param event The event
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onSignBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		/* Check if it is a sign */
		if(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
			Sign sign = (Sign)(block.getState());
			/* Check if the rent sign is really the same as a saved rent */
			HashMap<String,String> rent = plugin.getShopManager().getRent(sign.getLine(1));
			HashMap<String,String> buy = plugin.getShopManager().getBuy(sign.getLine(1));
			if(rent != null && rent.get(plugin.keyWorld).equals(block.getWorld().getName())	
					&& rent.get(plugin.keyX).equals(String.valueOf(block.getX()))
					&& rent.get(plugin.keyY).equals(String.valueOf(block.getY()))
					&& rent.get(plugin.keyZ).equals(String.valueOf(block.getZ())) ) {
				/* Remove the rent if the player has permission */
				if(event.getPlayer().hasPermission("areashop.destroyrent")) {
					plugin.getShopManager().handleSchematicEvent(sign.getLine(1), true, RegionEventType.DELETED);
					boolean result = plugin.getShopManager().removeRent(sign.getLine(1));
					
					if(result) {
						event.getPlayer().sendMessage(plugin.fixColors(plugin.config().getString("chatPrefix")) + "Renting of the region succesfully removed");
					}
				} else { /* Cancel the breaking of the sign */
					event.setCancelled(true);
					event.getPlayer().sendMessage(plugin.fixColors(plugin.config().getString("chatPrefix")) + "You don't have permission for destroying a sign for renting a region");
				}
			} else if(buy != null && buy.get(plugin.keyWorld).equals(block.getWorld().getName())	
					&& buy.get(plugin.keyX).equals(String.valueOf(block.getX()))
					&& buy.get(plugin.keyY).equals(String.valueOf(block.getY()))
					&& buy.get(plugin.keyZ).equals(String.valueOf(block.getZ())) ) {
				/* Remove the buy if the player has permission */
				if(event.getPlayer().hasPermission("areashop.destroybuy")) {
					plugin.getShopManager().handleSchematicEvent(sign.getLine(1), false, RegionEventType.DELETED);
					boolean result = plugin.getShopManager().removeBuy(sign.getLine(1));
					if(result) {
						event.getPlayer().sendMessage(plugin.fixColors(plugin.config().getString("chatPrefix")) + "Buying of the region succesfully removed");
					}
				} else { /* Cancel the breaking of the sign */
					event.setCancelled(true);
					event.getPlayer().sendMessage(plugin.fixColors(plugin.config().getString("chatPrefix")) + "You don't have permission for destroying a sign for buying a region");
				}
			}
		}
	}
	
	/**
	 * Called when the physics of a block change
	 * @param event The event
	 */
	@EventHandler(priority = EventPriority.HIGH)
    public void onIndirectSignBreak(BlockPhysicsEvent event){
        Block block = event.getBlock();
        if(block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN){
            Sign sign = (Sign)block.getState();
            Block attachedTo = block.getRelative(((org.bukkit.material.Sign)sign.getData()).getAttachedFace());
            if(attachedTo.getType() == Material.AIR){
				/* Check if the rent sign is really the same as a saved rent */
				HashMap<String,String> rent = plugin.getShopManager().getRent(sign.getLine(1));
				HashMap<String,String> buy = plugin.getShopManager().getBuy(sign.getLine(1));
				if(rent != null && rent.get(plugin.keyWorld).equals(block.getWorld().getName())	
						&& rent.get(plugin.keyX).equals(String.valueOf(block.getX()))
						&& rent.get(plugin.keyY).equals(String.valueOf(block.getY()))
						&& rent.get(plugin.keyZ).equals(String.valueOf(block.getZ())) ) {
					/* Remove the rent */
					boolean result = plugin.getShopManager().removeRent(sign.getLine(1));
					if(result) {
						plugin.getLogger().info("Renting of region '" + sign.getLine(1) + "' has been removed by indirectly breaking the sign");
					}
				} else if(buy != null && buy.get(plugin.keyWorld).equals(block.getWorld().getName())	
						&& buy.get(plugin.keyX).equals(String.valueOf(block.getX()))
						&& buy.get(plugin.keyY).equals(String.valueOf(block.getY()))
						&& buy.get(plugin.keyZ).equals(String.valueOf(block.getZ())) ) {
					/* Remove the buy */
					boolean result = plugin.getShopManager().removeBuy(sign.getLine(1));
					if(result) {
						plugin.getLogger().info("Buying of region '" + sign.getLine(1) + "' has been removed by indirectly breaking the sign");
					}
				}
            }
        }
    }
}


















































