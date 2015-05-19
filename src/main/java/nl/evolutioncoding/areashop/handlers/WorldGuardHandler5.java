package nl.evolutioncoding.areashop.handlers;

import java.util.HashSet;
import java.util.Set;

import nl.evolutioncoding.areashop.interfaces.AreaShopInterface;
import nl.evolutioncoding.areashop.interfaces.WorldGuardInterface;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardHandler5 extends WorldGuardInterface {

	public WorldGuardHandler5(AreaShopInterface pluginInterface) {
		super(pluginInterface);
	}

	public void setOwners(ProtectedRegion region, String input) {
		// Split the string and parse all values
		String[] names = input.split(", ");
		DefaultDomain owners = region.getOwners();
		owners.removeAll();
		for(String owner : names) {
			if(owner != null && !owner.isEmpty()) {
				// Check for groups
				if(owner.startsWith("g:")) {
					if(owner.length() > 2) {
						owners.addGroup(owner.substring(2));
					}
				} else if(owner.startsWith("n:")) {
					if(owner.length() > 2) {
						owners.addPlayer(owner);
					}							
				} else {
					String playerName;						
					try {
						playerName = owner;
					} catch(IllegalArgumentException e) {
						System.out.println("Tried using '" + owner + "' as uuid for a region owner, is your flagProfiles section correct?");
						playerName = null;
					}
					if(playerName != null) {
						OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
						if(offlinePlayer != null && offlinePlayer.getName() != null) {
							owners.addPlayer(offlinePlayer.getName());
						}
					}
				}
			}
		}
		region.setOwners(owners);
		//System.out.println("  Flag " + flagName + " set: " + owners.toUserFriendlyString());		
	}

	public void setMembers(ProtectedRegion region, String input) {
		// Split the string and parse all values
		String[] names = input.split(", ");
		DefaultDomain members = region.getMembers();
		members.removeAll();
		for(String member : names) {
			if(member != null && !member.isEmpty()) {
				// Check for groups
				if(member.startsWith("g:")) {
					if(member.length() > 2) {
						members.addGroup(member.substring(2));
					}
				} else if(member.startsWith("n:")) {
					if(member.length() > 2) {
						members.addPlayer(member);
					}							
				} else {
					String playerName;						
					try {
						playerName = member;
					} catch(IllegalArgumentException e) {
						System.out.println("Tried using '" + member + "' as uuid for a region member, is your flagProfiles section correct?");
						playerName = null;
					}
					if(playerName != null) {
						OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
						if(offlinePlayer != null && offlinePlayer.getName() != null) {
							members.addPlayer(offlinePlayer.getName());
						}
					}
				}
			}
		}
		region.setMembers(members);
		//System.out.println("  Flag " + flagName + " set: " + members.toUserFriendlyString());		
	}

	@Override
	public Set<ProtectedRegion> getApplicableRegionsSet(Location location) {
		Set<ProtectedRegion> result = new HashSet<ProtectedRegion>();
		ApplicableRegionSet regions = pluginInterface.getWorldGuard().getRegionManager(location.getWorld()).getApplicableRegions(location);
		for(ProtectedRegion region : regions) {
			result.add(region);
		}
		return result;
	}
	
}