
package nl.evolutioncoding.areashop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.Utils;
import nl.evolutioncoding.areashop.regions.BuyRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import nl.evolutioncoding.areashop.regions.RentRegion;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelfriendCommand extends CommandAreaShop {
	
	public DelfriendCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop delfriend";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if (target.hasPermission("areashop.delfriendall")) return plugin.getLanguageManager().getLang("help-delFriendAll");
		else if (target.hasPermission("areashop.delfriend")) return plugin.getLanguageManager().getLang("help-delFriend");
		return null;
	}
	
	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if (!sender.hasPermission("areashop.delfriend") && !sender.hasPermission("areashop.delfriendall")) {
			plugin.message(sender, "delfriend-noPermission");
			return;
		}
		if (args.length < 2) {
			plugin.message(sender, "delfriend-help");
			return;
		}
		GeneralRegion region = null;
		if (args.length <= 2) {
			if (sender instanceof Player) {
				// get the region by location
				List<GeneralRegion> regions = Utils.getAllApplicableRegions(((Player) sender).getLocation());
				if (regions.isEmpty()) {
					plugin.message(sender, "cmd-noRegionsAtLocation");
					return;
				}
				else if (regions.size() > 1) {
					plugin.message(sender, "cmd-moreRegionsAtLocation");
					return;
				}
				else {
					region = regions.get(0);
				}
			}
			else {
				plugin.message(sender, "cmd-automaticRegionOnlyByPlayer");
				return;
			}
		}
		else {
			region = plugin.getFileManager().getRegion(args[2]);
			if (region == null) {
				plugin.message(sender, "cmd-notRegistered", args[2]);
				return;
			}
		}
		if (sender.hasPermission("areashop.delfriendall")) {
			if ((region.isRentRegion() && !((RentRegion) region).isRented()) || (region.isBuyRegion() && !((BuyRegion) region).isSold())) {
				plugin.message(sender, "delfriend-noOwner");
				return;
			}
			OfflinePlayer friend = Bukkit.getOfflinePlayer(args[1]);
			if (!region.getFriends().contains(friend.getName())) {
				plugin.message(sender, "delfriend-notAdded", friend.getName());
				return;
			}
			region.deleteFriend(friend.getName());
			region.updateRegionFlags();
			region.updateSigns();
			plugin.message(sender, "delfriend-successOther", friend.getName(), region.getName());
		}
		else {
			if (sender.hasPermission("areashop.delfriend") && sender instanceof Player) {
				if (region.isOwner((Player) sender)) {
					OfflinePlayer friend = Bukkit.getOfflinePlayer(args[1]);
					if (!region.getFriends().contains(friend.getName())) {
						plugin.message(sender, "delfriend-notAdded", friend.getName());
						return;
					}
					region.deleteFriend(friend.getName());
					region.updateRegionFlags();
					region.updateSigns();
					plugin.message(sender, "delfriend-success", friend.getName(), region.getName());
				}
				else {
					plugin.message(sender, "delfriend-noPermissionOther");
				}
			}
			else {
				plugin.message(sender, "delfriend-noPermission");
			}
		}
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		ArrayList<String> result = new ArrayList<String>();
		if (toComplete == 2) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				result.add(player.getName());
			}
		}
		else if (toComplete == 3) {
			result.addAll(plugin.getFileManager().getRegionNames());
		}
		return result;
	}
}
