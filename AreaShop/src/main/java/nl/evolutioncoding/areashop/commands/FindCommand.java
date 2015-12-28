package nl.evolutioncoding.areashop.commands;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.regions.BuyRegion;
import nl.evolutioncoding.areashop.regions.RegionGroup;
import nl.evolutioncoding.areashop.regions.RentRegion;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FindCommand extends CommandAreaShop {

	public FindCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop find";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.find")) {
			return plugin.getLanguageManager().getLang("help-find");
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.find")) {
			plugin.message(sender, "find-noPermission");
			return;
		}	
		if (!(sender instanceof Player)) {
			plugin.message(sender, "cmd-onlyByPlayer");
			return;
		}		
		if(args.length <= 1 || args[1] == null || (!args[1].equalsIgnoreCase("buy") && !args[1].equalsIgnoreCase("rent"))) {
			plugin.message(sender, "find-help");
			return;
		}
		Player player = (Player)sender;
		double balance = 0.0;
		if(plugin.getEconomy() != null) {
			balance = plugin.getEconomy().getBalance(player);
		}
		double maxPrice = 0;
		boolean maxPriceSet = false;
		RegionGroup group = null;
		// Parse optional price argument
		if(args.length >= 3) {
			try {
				maxPrice = Double.parseDouble(args[2]);
				maxPriceSet = true;
			} catch(NumberFormatException e) {
				plugin.message(sender, "find-wrongMaxPrice", args[2]);
				return;
			}
		}
		// Parse optional group argument
		if(args.length >= 4) {
			group = plugin.getFileManager().getGroup(args[3]);
			if(group == null) {
				plugin.message(sender, "find-wrongGroup", args[3]);
				return;
			}
		}
		if(args[1].equalsIgnoreCase("buy")) {
			List<BuyRegion> regions = plugin.getFileManager().getBuys();
			List<BuyRegion> results = new ArrayList<>();
			for(BuyRegion region : regions) {
				if(!region.isSold() 
						&& ((region.getPrice() <= balance && !maxPriceSet) || (region.getPrice() <= maxPrice && maxPriceSet)) 
						&& (group == null || group.isMember(region))) {
					results.add(region);
				}
			}
			if(!results.isEmpty()) {
				// Draw a random one
				BuyRegion region = results.get(new Random().nextInt(results.size()));
			    String onlyInGroup = "";
				if(group != null) {
					onlyInGroup = plugin.getLanguageManager().getLang("find-onlyInGroup", args[3]);
				}
				if(maxPriceSet) {
					plugin.message(player, "find-successMax", "buy", region.getName(), plugin.formatCurrency(maxPrice), onlyInGroup);
				} else {
					plugin.message(player, "find-success", "buy", region.getName(), plugin.formatCurrency(balance), onlyInGroup);
				}
				region.teleportPlayer(player, region.getBooleanSetting("general.findTeleportToSign"), false);		
			} else {
			    String onlyInGroup = "";
				if(group != null) {
					onlyInGroup = plugin.getLanguageManager().getLang("find-onlyInGroup", args[3]);
				}
				if(maxPriceSet) {
					plugin.message(player, "find-noneFoundMax", "buy", plugin.formatCurrency(maxPrice), onlyInGroup);
				} else {
					plugin.message(player, "find-noneFound", "buy", plugin.formatCurrency(balance), onlyInGroup);
				}
			}
		} else {
			List<RentRegion> regions = plugin.getFileManager().getRents();
			List<RentRegion> results = new ArrayList<>();
			for(RentRegion region : regions) {
				if(!region.isRented() 
						&& ((region.getPrice() <= balance && !maxPriceSet) || (region.getPrice() <= maxPrice && maxPriceSet))
						&& (group == null || group.isMember(region))) {
					results.add(region);
				}
			}
			if(!results.isEmpty()) {
				// Draw a random one
				RentRegion region = results.get(new Random().nextInt(results.size()));
			    String onlyInGroup = "";
				if(group != null) {
					onlyInGroup = plugin.getLanguageManager().getLang("find-onlyInGroup", args[3]);
				}
				if(maxPriceSet) {
					plugin.message(player, "find-successMax", "rent", region.getName(), plugin.formatCurrency(maxPrice), onlyInGroup);
				} else {
					plugin.message(player, "find-success", "rent", region.getName(), plugin.formatCurrency(balance), onlyInGroup);
				}
				region.teleportPlayer(player, region.getBooleanSetting("general.findTeleportToSign"), false);		
			} else {
			    String onlyInGroup = "";
				if(group != null) {
					onlyInGroup = plugin.getLanguageManager().getLang("find-onlyInGroup", args[3]);
				}
				if(maxPriceSet) {
					plugin.message(player, "find-noneFoundMax", "rent", plugin.formatCurrency(maxPrice), onlyInGroup);
				} else {
					plugin.message(player, "find-noneFound", "rent", plugin.formatCurrency(balance), onlyInGroup);
				}
			}
		}

	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		ArrayList<String> result = new ArrayList<>();
		if(toComplete == 2) {
			result.add("buy");
			result.add("rent");
		}
		return result;
	}

}



























