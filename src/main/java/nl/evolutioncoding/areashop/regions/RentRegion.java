
package nl.evolutioncoding.areashop.regions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import net.milkbowl.vault.economy.EconomyResponse;
import nl.evolutioncoding.areashop.AreaShop;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class RentRegion extends GeneralRegion {
	private long warningsDoneUntil = Calendar.getInstance().getTimeInMillis();
	
	/** Constructor
	 * 
	 * @param plugin The areashop plugin
	 * @param settings All settings of this region
	 * @throws RegionCreateException */
	public RentRegion(AreaShop plugin, YamlConfiguration config) {
		super(plugin, config);
	}
	
	/** Create a new RentRegion
	 * 
	 * @param plugin The AreaShop plugin
	 * @param name The name of the region (correct casing)
	 * @param world The world of the WorldGuard region */
	public RentRegion(AreaShop plugin, String name, World world) {
		super(plugin, name, world);
	}
	
	@Override
	public RegionType getType() {
		return RegionType.RENT;
	}
	
	@Override
	public RegionState getState() {
		if (isRented()) return RegionState.RENTED;
		else return RegionState.FORRENT;
	}
	
	/** Get the UUID of the player renting the region
	 * 
	 * @return The UUID of the renter */
	public String getRenter() {
		String renter = getStringSetting("rent.renter");
		if (renter != null) {
			try {
				return renter;
			}
			catch (IllegalArgumentException e) {
			}
		}
		return null;
	}
	
	/** Check if a player is the renter of this region
	 * 
	 * @param player Player to check
	 * @return true if this player rents this region, otherwise false */
	public boolean isRenter(Player player) {
		if (player == null) return false;
		else return isRenter(player.getName());
	}
	
	public boolean isRenter(String player) {
		String renter = getRenter();
		if (player == null || renter == null) return false;
		else return renter.equals(player);
	}
	
	/** Set the renter of this region
	 * 
	 * @param renter The UUID of the player that should be set as the renter */
	public void setRenter(String renter) {
		if (renter == null) {
			setSetting("rent.renter", null);
			setSetting("rent.renterName", null);
		}
		else {
			setSetting("rent.renter", renter.toString());
			setSetting("rent.renterName", renter);
		}
	}
	
	/** Get the max number of extends of this region
	 * 
	 * @return -1 if infinite otherwise the maximum number */
	public int getMaxExtends() {
		return getIntegerSetting("rent.maxExtends");
	}
	
	/** Get how many times the rent has already been extended
	 * 
	 * @return The number of times extended */
	public int getTimesExtended() {
		return config.getInt("rent.timesExtended");
	}
	
	/** Set the number of times the region has been extended
	 * 
	 * @param times The number of times the region has been extended */
	public void setTimesExtended(int times) {
		if (times < 0) {
			setSetting("rent.timesExtended", null);
		}
		else {
			setSetting("rent.timesExtended", times);
		}
	}
	
	@Override
	public HashMap<String, Object> getSpecificReplacements() {
		// Fill the replacements map with things specific to a RentRegion
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put(AreaShop.tagPrice, getFormattedPrice());
		result.put(AreaShop.tagRawPrice, getPrice());
		result.put(AreaShop.tagDuration, getDurationString());
		result.put(AreaShop.tagPlayerName, getPlayerName());
		result.put(AreaShop.tagPlayerUUID, getRenter());
		SimpleDateFormat date = new SimpleDateFormat(plugin.getConfig().getString("timeFormatChat"));
		String dateString = date.format(new Date(getRentedUntil()));
		result.put(AreaShop.tagRentedUntil, dateString);
		date = new SimpleDateFormat(plugin.getConfig().getString("timeFormatSign"));
		dateString = date.format(new Date(getRentedUntil()));
		result.put(AreaShop.tagRentedUntilShort, dateString);
		result.put(AreaShop.tagTimeLeft, getTimeLeftString());
		result.put(AreaShop.tagMoneyBackAmount, getFormattedMoneyBackAmount());
		result.put(AreaShop.tagRawMoneyBackAmount, getMoneyBackAmount());
		double moneyBackPercent = getMoneyBackPercentage();
		if ((moneyBackPercent % 1.0) == 0.0) {
			result.put(AreaShop.tagMoneyBackPercentage, (int) moneyBackPercent);
		}
		else {
			result.put(AreaShop.tagMoneyBackPercentage, moneyBackPercent);
		}
		result.put(AreaShop.tagMaxExtends, this.getMaxExtends());
		result.put(AreaShop.tagExtendsLeft, getMaxExtends() - getTimesExtended());
		result.put(AreaShop.tagMaxRentTime, this.millisToHumanFormat(getMaxRentTime() * 60 * 1000));
		result.put(AreaShop.tagMaxInactiveTime, this.getFormattedInactiveTimeUntilUnrent());
		return result;
	}
	
	/** Check if the region is rented
	 * 
	 * @return true if the region is rented, otherwise false */
	public boolean isRented() {
		return getRenter() != null;
	}
	
	/** Get the name of the player renting this region
	 * 
	 * @return Name of the player renting this region, if unavailable by UUID it will return the old
	 *         cached name, if that is unavailable it will return <UNKNOWN> */
	public String getPlayerName() {
		String result = getRenter();
		if (result == null || result.isEmpty()) {
			result = config.getString("rent.renterName");
			if (result == null || result.isEmpty()) {
				result = "<UNKNOWN>";
			}
		}
		return result;
	}
	
	/** Get the time until this region is rented (time from 1970 epoch)
	 * 
	 * @return */
	public long getRentedUntil() {
		return getLongSetting("rent.rentedUntil");
	}
	
	/** Set the time until the region is rented (milliseconds from 1970, system time)
	 * 
	 * @param rentedUntil The time until the region is rented */
	public void setRentedUntil(Long rentedUntil) {
		if (rentedUntil == null) {
			setSetting("rent.rentedUntil", null);
		}
		else {
			setSetting("rent.rentedUntil", rentedUntil);
		}
	}
	
	/** Get the price of the region
	 * 
	 * @return The price of the region */
	public double getPrice() {
		return getDoubleSetting("rent.price");
	}
	
	/** Get the formatted string of the price (includes prefix and suffix)
	 * 
	 * @return The formatted string of the price */
	public String getFormattedPrice() {
		return plugin.formatCurrency(getPrice());
	}
	
	/** Get the duration of 1 rent period
	 * 
	 * @return The duration in milliseconds of 1 rent period */
	public long getDuration() {
		return plugin.durationStringToLong(getDurationString());
	}
	
	/** Get the duration string, includes number<space>indentifier
	 * 
	 * @return The duration string */
	public String getDurationString() {
		return getStringSetting("rent.duration");
	}
	
	/** Get the time that is left on the region
	 * 
	 * @return The time left on the region */
	public long getTimeLeft() {
		if (isRented()) return this.getRentedUntil() - Calendar.getInstance().getTimeInMillis();
		else return 0;
	}
	
	/** Get a formatted string indicating the rent time that is left
	 * 
	 * @return Time left on the rent, for example '29 days', '3 months', '1 second' */
	public String getTimeLeftString() {
		return millisToHumanFormat(getTimeLeft());
	}
	
	/** Minutes until automatic unrent when player is offline
	 * 
	 * @return The number of minutes until the region is unrented while player is offline */
	public long getInactiveTimeUntilUnrent() {
		return plugin.getDurationFromMinutesOrString("rent.inactiveTimeUntilUnrent");
	}
	
	/** Get a human readable string indicating how long the player can be offline until automatic
	 * unrent
	 * 
	 * @return String indicating the inactive time until unrent */
	public String getFormattedInactiveTimeUntilUnrent() {
		return this.millisToHumanFormat(getInactiveTimeUntilUnrent() * 60 * 1000);
	}
	
	/** Change the price of the region
	 * 
	 * @param price */
	public void setPrice(double price) {
		setSetting("rent.price", price);
	}
	
	/** Remove the price so that the price will be taken from a group or the default.yml file */
	public void removePrice() {
		setSetting("rent.price", null);
	}
	
	/** Set the duration of the rent
	 * 
	 * @param duration The duration of the rent (as specified on the documentation pages) */
	public void setDuration(String duration) {
		setSetting("rent.duration", duration);
	}
	
	/** Get the moneyBack percentage
	 * 
	 * @return The % of money the player will get back when unrenting */
	public double getMoneyBackPercentage() {
		return getDoubleSetting("rent.moneyBack");
	}
	
	/** Get the amount of money that should be paid to the player when unrenting the region
	 * 
	 * @return The amount of money the player should get back */
	public double getMoneyBackAmount() {
		Long currentTime = Calendar.getInstance().getTimeInMillis();
		Double timeLeft = (double) ((getRentedUntil() - currentTime));
		double percentage = (getMoneyBackPercentage()) / 100.0;
		Double timePeriod = (double) (getDuration());
		double periods = timeLeft / timePeriod;
		double moneyBack = periods * getPrice() * percentage;
		return moneyBack;
	}
	
	/** Get the formatted string of the amount of the moneyBack amount
	 * 
	 * @return String with currency symbols and proper fractional part */
	public String getFormattedMoneyBackAmount() {
		return plugin.formatCurrency(getMoneyBackAmount());
	}
	
	/** Get the maximum time the player can rent the region in advance (minutes)
	 * 
	 * @return The maximum rent time in minutes */
	public long getMaxRentTime() {
		return plugin.getDurationFromMinutesOrString("rent.maxRentTime") / 1000;
	}
	
	/** Check if the rent should expire
	 * 
	 * @return true if the rent has expired and has been unrented, false otherwise */
	public boolean checkExpiration() {
		long now = Calendar.getInstance().getTimeInMillis();
		if (!isDeleted() && isRented() && now > getRentedUntil()) {
			/* Send message to the player if online */
			Player player = Bukkit.getPlayer(getRenter());
			if (player != null) {
				plugin.message(player, "unrent-expired", getName());
			}
			unRent(false);
			return true;
		}
		return false;
	}
	
	/** Send the expiration warnings from the selected profile which is specified in the config Sends
	 * all warnings since previous call until now+<normal delay>, delay can be found in the config as
	 * well */
	public void sendExpirationWarnings() {
		// send from warningsDoneUntil to current+delay
		if (isDeleted() || !isRented()) return;
		Player player = Bukkit.getPlayer(getRenter());
		if (player != null) {
			long sendUntil = Calendar.getInstance().getTimeInMillis() + (plugin.getConfig().getInt("expireWarning.delay") * 60 * 1000);
			// loop through warning defined in the config for the profile that is set for this region
			String configPath = "expirationWarningProfiles." + getStringSetting("rent.expirationWarningProfile");
			ConfigurationSection section = plugin.getConfig().getConfigurationSection(configPath);
			if (section == null) return;
			for (String timeBefore : section.getKeys(false)) {
				long timeBeforeParsed = plugin.durationStringToLong(timeBefore);
				if (timeBeforeParsed <= 0) return;
				long checkTime = getRentedUntil() - timeBeforeParsed;
				
				if (checkTime > warningsDoneUntil && checkTime <= sendUntil) {
					if (plugin.getConfig().getBoolean(configPath + "." + timeBefore + ".warnPlayer")) {
						plugin.message(player, "rent-expireWarning", this);
					}
					this.runCommands(Bukkit.getConsoleSender(), plugin.getConfig().getStringList(configPath + "." + timeBefore + ".commands"));
				}
			}
			warningsDoneUntil = sendUntil;
		}
	}
	
	/** Rent a region
	 * 
	 * @param player The player that wants to rent the region
	 * @param regionName The name of the region you want to rent
	 * @return true if it succeeded and false if not */
	public boolean rent(Player player) {
		/* Check if the player has permission */
		if (player.hasPermission("areashop.rent")) {
			if (plugin.getEconomy() == null) {
				plugin.message(player, "general-noEconomy");
				return false;
			}
			if (getWorld() == null) {
				plugin.message(player, "general-noWorld", getWorldName());
				return false;
			}
			if (getRegion() == null) {
				plugin.message(player, "general-noRegion", getName());
				return false;
			}
			boolean extend = false;
			if (getRenter() != null && player.getName().equals(getRenter())) {
				extend = true;
			}
			// Check if the region is available for renting or if the player wants to extend the rent
			if (!isRented() || extend) {
				// Check if the players needs to be in the world or region for buying
				if (restrictedToRegion() && (!player.getWorld().getName().equals(getWorldName()) || !getRegion().contains(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()))) {
					plugin.message(player, "rent-restrictedToRegion", getName());
					return false;
				}
				if (restrictedToWorld() && !player.getWorld().getName().equals(getWorldName())) {
					plugin.message(player, "rent-restrictedToWorld", getWorldName(), player.getWorld().getName());
					return false;
				}
				// Check region limits if this is not extending
				if (!extend) {
					LimitResult limitResult = this.limitsAllowRenting(player);
					AreaShop.debug("LimitResult: " + limitResult.toString());
					if (!limitResult.actionAllowed()) {
						if (limitResult.getLimitingFactor() == LimitType.TOTAL) {
							plugin.message(player, "total-maximum", limitResult.getMaximum(), limitResult.getCurrent(), limitResult.getLimitingGroup());
							return false;
						}
						if (limitResult.getLimitingFactor() == LimitType.RENTS) {
							plugin.message(player, "rent-maximum", limitResult.getMaximum(), limitResult.getCurrent(), limitResult.getLimitingGroup());
							return false;
						}
						// Should not be reached, but is safe like this
						return false;
					}
				}
				
				// Check if the player can still extend this rent
				if (extend && !player.hasPermission("areashop.rentextendbypass")) {
					if (getMaxExtends() >= 0 && getTimesExtended() >= getMaxExtends()) {
						plugin.message(player, "rent-maxExtends", getMaxExtends());
						return false;
					}
				}
				
				// Check if there is enough time left before hitting maxRentTime
				long timeNow = Calendar.getInstance().getTimeInMillis();
				long timeRented = 0;
				long maxRentTime = getMaxRentTime();
				if (isRented()) {
					timeRented = getRentedUntil() - timeNow;
				}
				if ((timeRented + getDuration()) > (maxRentTime * 60 * 1000) && !player.hasPermission("areashop.renttimebypass") && maxRentTime != -1) {
					int timeRentedMinutes = (int) (timeRented / 1000.0 / 60.0 + 1);
					plugin.message(player, "rent-maxRentTime", maxRentTime, timeRentedMinutes);
					return false;
				}
				
				if (plugin.getEconomy().has(player.getName(), getWorldName(), getPrice())) {
					/* Substract the money from the players balance */
					EconomyResponse r = plugin.getEconomy().withdrawPlayer(player.getName(), getWorldName(), getPrice());
					if (!r.transactionSuccess()) {
						plugin.message(player, "rent-payError");
						return false;
					}
					// Optionally give money to the landlord
					if (getLandlord() != null) {
						OfflinePlayer landlord = Bukkit.getOfflinePlayer(getLandlord());
						if (landlord != null) {
							r = plugin.getEconomy().depositPlayer(landlord.getName(), getWorldName(), getPrice());
							if (!r.transactionSuccess()) {
								plugin.getLogger().warning("Something went wrong with paying '" + landlord.getName() + "' " + getFormattedPrice() + " for his rent of region " + getName() + " to " + player.getName());
							}
						}
					}
					if (!extend) {
						// Run commands
						runEventCommands(RegionEvent.RENTED, true);
					}
					else {
						// Run commands
						runEventCommands(RegionEvent.EXTENDED, true);
					}
					
					/* Get the time until the region will be rented */
					Calendar calendar = Calendar.getInstance();
					if (extend) {
						calendar.setTimeInMillis(getRentedUntil());
					}
					calendar.setTimeInMillis(calendar.getTimeInMillis() + getDuration());
					SimpleDateFormat dateFull = new SimpleDateFormat(plugin.getConfig().getString("timeFormatChat"));
					AreaShop.debug(player.getName() + " has rented region " + getName() + " for " + getFormattedPrice() + " until " + dateFull.format(calendar.getTime()));
					
					/* Add values to the rent and send it to FileManager */
					setRentedUntil(calendar.getTimeInMillis());
					setRenter(player.getName());
					
					// Fire schematic event and updated times extended
					if (!extend) {
						this.handleSchematicEvent(RegionEvent.RENTED);
						setTimesExtended(0);
					}
					else {
						setTimesExtended(getTimesExtended() + 1);
					}
					
					/* Change the sign and the region flags */
					updateSigns();
					updateRegionFlags(RegionState.RENTED);
					
					/* Send message to the player */
					if (extend) {
						plugin.message(player, "rent-extended", getName(), dateFull.format(calendar.getTime()));
					}
					else {
						plugin.message(player, "rent-rented", getName(), dateFull.format(calendar.getTime()));
						plugin.message(player, "rent-extend");
					}
					if (!extend) {
						// Run commands
						this.runEventCommands(RegionEvent.RENTED, false);
					}
					else {
						// Run commands
						this.runEventCommands(RegionEvent.EXTENDED, false);
					}
					return true;
				}
				else {
					/* Player has not enough money */
					if (extend) {
						plugin.message(player, "rent-lowMoneyExtend", plugin.formatCurrency(plugin.getEconomy().getBalance(player.getName(), getWorldName())), getFormattedPrice());
					}
					else {
						plugin.message(player, "rent-lowMoneyRent", plugin.formatCurrency(plugin.getEconomy().getBalance(player.getName(), getWorldName())), getFormattedPrice());
					}
				}
			}
			else {
				plugin.message(player, "rent-someoneElse");
			}
		}
		else {
			plugin.message(player, "rent-noPermission");
		}
		return false;
	}
	
	/** Unrent a region, reset to unrented
	 * 
	 * @param regionName Region that should be unrented */
	public void unRent(boolean giveMoneyBack) {
		// Run commands
		this.runEventCommands(RegionEvent.UNRENTED, true);
		double moneyBack = getMoneyBackAmount();
		if (moneyBack > 0 && giveMoneyBack) {
			/* Give back the money */
			OfflinePlayer player = Bukkit.getOfflinePlayer(getRenter());
			if (player != null) {
				EconomyResponse r = null;
				boolean error = false;
				try {
					r = plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(getRenter()).getName(), getWorldName(), moneyBack);
				}
				catch (Exception e) {
					error = true;
				}
				if (error || r == null || !r.transactionSuccess()) {
					plugin.getLogger().warning("Something went wrong with paying back to " + getPlayerName() + " money while unrenting region " + getName());
				}
			}
		}
		
		/* Debug message */
		AreaShop.debug(getPlayerName() + " has unrented " + getName() + ", got " + plugin.formatCurrency(moneyBack) + " money back");
		
		/* Update the signs and region flags */
		handleSchematicEvent(RegionEvent.UNRENTED);
		updateRegionFlags(RegionState.FORRENT);
		
		/* Remove friends, the owner and renteduntil values */
		clearFriends();
		setRenter(null);
		setRentedUntil(null);
		setTimesExtended(-1);
		
		updateSigns();
		// Run commands
		this.runEventCommands(RegionEvent.UNRENTED, false);
	}
	
	@Override
	public boolean checkInactive() {
		if (isDeleted() || !isRented()) return false;
		OfflinePlayer player = Bukkit.getOfflinePlayer(getRenter());
		long inactiveSetting = getInactiveTimeUntilUnrent();
		if (inactiveSetting <= 0 || player.isOp()) return false;
		// AreaShop.debug("currentTime=" + Calendar.getInstance().getTimeInMillis() +
		// ", getLastPlayed()=" + player.getLastPlayed() + ", timeInactive=" +
		// (Calendar.getInstance().getTimeInMillis()-player.getLastPlayed()) +
		// ", inactiveSetting*60*1000=" + inactiveSetting * 60 * 1000);
		if (Calendar.getInstance().getTimeInMillis() > (player.getLastPlayed() + inactiveSetting * 60 * 1000)) {
			plugin.getLogger().info("Region " + getName() + " unrented because of inactivity for player " + getPlayerName());
			AreaShop.debug("currentTime=" + Calendar.getInstance().getTimeInMillis() + ", getLastPlayed()=" + player.getLastPlayed() + ", timeInactive=" + (Calendar.getInstance().getTimeInMillis() - player.getLastPlayed()) + ", inactiveSetting*60*1000=" + inactiveSetting * 60 * 1000);
			this.unRent(true);
			return true;
		}
		return false;
	}
	
}
