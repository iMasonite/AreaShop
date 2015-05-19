
package nl.evolutioncoding.areashop.managers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.regions.GeneralRegion;

import org.bukkit.configuration.file.YamlConfiguration;

public class LanguageManager {
	private AreaShop plugin = null;
	private String languages[] = { "EN" };
	private HashMap<String, String> currentLanguage, defaultLanguage;
	
	/** Constructor
	 * 
	 * @param plugin The AreaShop plugin */
	public LanguageManager(AreaShop plugin) {
		this.plugin = plugin;
		startup();
	}
	
	/** Save the default language files and open the current and backup language file */
	public void startup() {
		this.saveDefaults();
		this.loadLanguage();
	}
	
	/** Saves the default language files if not already present */
	public void saveDefaults() {
		/* Create the language folder if it not exists */
		File langFolder;
		langFolder = new File(plugin.getDataFolder() + File.separator + AreaShop.languageFolder);
		if (!langFolder.exists()) {
			langFolder.mkdirs();
		}
		
		/* Create the language files, overwrites if a file already exists */
		/* Overriding is necessary because otherwise with an update the new lang */
		/* files would not be used, when translating your own use another */
		/* file name as the default */
		File langFile;
		for (String language : languages) {
			langFile = new File(plugin.getDataFolder() + File.separator + AreaShop.languageFolder + File.separator + language + ".yml");
			InputStream input = null;
			OutputStream output = null;
			try {
				input = plugin.getResource(AreaShop.languageFolder + "/" + language + ".yml");
				if (input == null) {
					plugin.getLogger().warning("Could not save default language to the '" + AreaShop.languageFolder + "' folder: " + language + ".yml");
					continue;
				}
				output = new FileOutputStream(langFile);
				
				int read = 0;
				byte[] bytes = new byte[1024];
				while ((read = input.read(bytes)) != -1) {
					output.write(bytes, 0, read);
				}
				input.close();
				output.close();
			}
			catch (IOException e) {
				try {
					input.close();
					output.close();
				}
				catch (IOException e1) {
				}
				catch (NullPointerException e2) {
				}
				
				plugin.getLogger().warning("Something went wrong saving a default language file: " + langFile.getPath());
			}
		}
		
	}
	
	/** Loads the current language file specified in the config */
	public void loadLanguage() {
		Map<String, Object> map;
		Set<String> set;
		YamlConfiguration ymlFile;
		
		/* Save the current language file to the HashMap */
		currentLanguage = new HashMap<String, String>();
		File file = new File(plugin.getDataFolder() + File.separator + AreaShop.languageFolder + File.separator + plugin.getConfig().getString("language") + ".yml");
		ymlFile = YamlConfiguration.loadConfiguration(file);
		map = ymlFile.getValues(true);
		set = map.keySet();
		try {
			for (String key : set) {
				currentLanguage.put(key, (String) map.get(key));
			}
		}
		catch (ClassCastException e) {
		}
		
		/* Save the default strings to the HashMap */
		defaultLanguage = new HashMap<String, String>();
		File standard = new File(plugin.getDataFolder() + File.separator + AreaShop.languageFolder + "/" + languages[0] + ".yml");
		ymlFile = YamlConfiguration.loadConfiguration(standard);
		map = ymlFile.getValues(true);
		set = map.keySet();
		try {
			for (String key : set) {
				defaultLanguage.put(key, (String) map.get(key));
			}
		}
		catch (ClassCastException e) {
		}
	}
	
	/** Function to get the string in the language that has been set
	 * 
	 * @param key Key to the language string
	 * @param params The replacements for the %1% tags
	 * @return String The language string specified with the key */
	public String getLang(String key, Object... params) {
		String result = null;
		
		/* Get the language string */
		if (currentLanguage.containsKey(key)) {
			result = currentLanguage.get(key);
		}
		else {
			result = defaultLanguage.get(key);
		}
		
		if (result == null) {
			plugin.getLogger().info("Wrong key for getting translation: " + key + ", please contact the author about this");
		}
		else {
			/* Replace all tags like %0% and if given a GeneralRegion apply all replacements */
			int number = 0;
			for (Object param : params) {
				if (param != null) {
					if (param instanceof GeneralRegion) {
						result = ((GeneralRegion) param).applyAllReplacements(result);
					}
					else {
						result = result.replace("%" + number + "%", param.toString());
						number++;
					}
				}
			}
		}
		
		return result;
	}
}
