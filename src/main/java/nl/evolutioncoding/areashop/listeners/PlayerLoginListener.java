
package nl.evolutioncoding.areashop.listeners;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import nl.evolutioncoding.areashop.regions.RentRegion;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.scheduler.BukkitRunnable;

/** Checks for placement of signs for this plugin
 * 
 * @author NLThijs48 */
public final class PlayerLoginListener implements Listener {
	AreaShop plugin;
	
	/** Constructor
	 * 
	 * @param plugin The AreaShop plugin */
	public PlayerLoginListener(AreaShop plugin) {
		this.plugin = plugin;
	}
	
	/** Called when a sign is changed
	 * 
	 * @param event The event */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (event.getResult() != Result.ALLOWED) return;
		final Player player = event.getPlayer();
		// Schedule task to check for notifications, prevents a lag spike at login
		new BukkitRunnable() {
			@Override
			public void run() {
				// Delay until all regions are loaded
				if (!plugin.isReady()) return;
				if (!player.isOnline()) {
					this.cancel();
					return;
				}
				// Notify for rents that almost run out
				for (RentRegion region : plugin.getFileManager().getRents()) {
					if (region.isRenter(player)) {
						String warningSetting = region.getStringSetting("rent.warningOnLoginTime");
						if (warningSetting == null || warningSetting.isEmpty()) {
							continue;
						}
						long warningTime = plugin.durationStringToLong(warningSetting);
						if (region.getTimeLeft() < warningTime) {
							// Send the warning message later to let it appear after general MOTD messages
							AreaShop.getInstance().message(player, "rent-expireWarning", region);
						}
					}
				}
				this.cancel();
			}
		}.runTaskTimer(plugin, 25, 25);
		// Check if the player has regions that use an old name of him and update them
		final List<GeneralRegion> regions = new ArrayList<GeneralRegion>(plugin.getFileManager().getRegions());
		new BukkitRunnable() {
			private int current = 0;
			
			@Override
			public void run() {
				// Delay until all regions are loaded
				if (!plugin.isReady()) return;
				// Check all regions
				for (int i = 0; i < plugin.getConfig().getInt("nameupdate.regionsPerTick"); i++) {
					if (current < regions.size()) {
						GeneralRegion region = regions.get(current);
						if (region.isOwner(player)) {
							if (region.isBuyRegion()) {
								if (!player.getName().equals(region.getStringSetting("buy.buyerName"))) {
									region.setSetting("buy.buyerName", player.getName());
									region.updateRegionFlags();
									region.updateSigns();
								}
							}
							else if (region.isRentRegion()) {
								if (!player.getName().equals(region.getStringSetting("rent.renterName"))) {
									region.setSetting("rent.renterName", player.getName());
									region.updateRegionFlags();
									region.updateSigns();
								}
							}
						}
						current++;
					}
				}
				if (current >= regions.size()) {
					this.cancel();
				}
			}
		}.runTaskTimer(plugin, 22, 1); // Wait a bit before starting to prevent a lot of stress on the
																		// server when a player joins (a lot of plugins already do stuff
																		// then)
	}
}
