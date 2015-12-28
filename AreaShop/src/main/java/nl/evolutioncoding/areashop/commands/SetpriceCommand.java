package nl.evolutioncoding.areashop.commands;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.Utils;
import nl.evolutioncoding.areashop.regions.BuyRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import nl.evolutioncoding.areashop.regions.RentRegion;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SetpriceCommand extends CommandAreaShop {

	public SetpriceCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop setprice";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.setprice")) {
			return plugin.getLanguageManager().getLang("help-setprice");
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.setprice") && (!sender.hasPermission("areashop.setprice.landlord") && sender instanceof Player)) {
			plugin.message(sender, "setprice-noPermission");
			return;
		}		
		if(args.length < 2 || args[1] == null) {
			plugin.message(sender, "setprice-help");
			return;
		}
		GeneralRegion region;
		if(args.length < 3) {
			if (sender instanceof Player) {
				// get the region by location
				List<GeneralRegion> regions = Utils.getAllApplicableRegions(((Player) sender).getLocation());
				if (regions.isEmpty()) {
					plugin.message(sender, "cmd-noRegionsAtLocation");
					return;
				} else if (regions.size() > 1) {
					plugin.message(sender, "cmd-moreRegionsAtLocation");
					return;
				} else {
					region = regions.get(0);
				}
			} else {
				plugin.message(sender, "cmd-automaticRegionOnlyByPlayer");
				return;
			}		
		} else {
			region = plugin.getFileManager().getRegion(args[2]);
		}		
		if(region == null) {
			plugin.message(sender, "setprice-notRegistered", args[2]);
			return;
		}
		if(!sender.hasPermission("areashop.setprice") && !(sender instanceof Player && region.isLandlord(((Player)sender).getUniqueId()))) {
			plugin.message(sender, "setprice-noLandlord", region);
			return;
		}
		if("default".equalsIgnoreCase(args[1]) || "reset".equalsIgnoreCase(args[1])) {
			if(region.isRentRegion()) {
				((RentRegion)region).removePrice();
			} else if(region.isBuyRegion()) {
				((BuyRegion)region).removePrice();
			}
			region.update();
			plugin.message(sender, "setprice-successRemoved", region);
			return;
		}
		double price;
		try {
			price = Double.parseDouble(args[1]);
		} catch(NumberFormatException e) {
			plugin.message(sender, "setprice-wrongPrice", args[1]);
			return;
		}
		if(region.isRentRegion()) {
			((RentRegion)region).setPrice(price);
			plugin.message(sender, "setprice-successRent", region);
		} else if(region.isBuyRegion()) {
			((BuyRegion)region).setPrice(price);
			plugin.message(sender, "setprice-successBuy", region);
		}
		region.update();
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		List<String> result = new ArrayList<>();
		if(toComplete == 3) {
			result = plugin.getFileManager().getRegionNames();		
		}
		return result;
	}

}
