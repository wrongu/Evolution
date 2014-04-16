package applet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import org.apache.commons.configuration.*;

/**
 * This class manages a static instance of a configuration file
 * 
 * @author wrongu
 *
 */
public class Config {
	
	public static PropertiesConfiguration instance;
	private static final String SYSTEM_SETTINGS = "default.config";
	
	/**
	 * load the requested configuration file. the ".config" extension is optional
	 * 
	 * @return true iff successful
	 */
	public static boolean load(String name){		
		// check for proper extension and fix it!
		if(!name.endsWith(".config")) name += ".config";
		
		System.out.println("Loading config file: "+name);
		
		// using composite configuration so that any values not set by the user will default
		// 	to the system settings (see configurations/default.config)
		CompositeConfiguration config = new CompositeConfiguration();
		
		// by adding user_config_file first, it will always be checked first
		// (thus overriding default values)
		try {
			instance = new PropertiesConfiguration(name);
			config.addConfiguration(instance);
		} catch (ConfigurationException e) {
			System.err.println("Cannot create local configuration file. Only defaults will be used.");
			return false;
		}
		
		// add the system (default) properties
		try {
			SystemConfiguration.setSystemProperties(SYSTEM_SETTINGS);
			config.addConfiguration(new SystemConfiguration());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		System.out.println("Loaded configuration!");
		Iterator<String> itr = instance.getKeys();
		while(itr.hasNext()) System.out.println("'"+itr.next()+"'");
		
		instance.setThrowExceptionOnMissing(true);
		return true;
	}

	
	/**
	 * cross-platform method for locating user configuration information
	 */
	private static File getUserConfigFile(String name){
		// thanks to http://stackoverflow.com/questions/3784657/what-is-the-best-way-to-save-user-settings-in-java-application
		String path = System.getProperty("user.home") // user.home is essentially the '~' directory
				+ File.separator + ".evolution-sim"
				+ File.separator + name;
		return new File(path);
	}
	
	/**
	 * Use a ClassLoader to find the default config file in the packaged JAR
	 * @return a File object pointing to the config file
	 */
	private static File getSystemConfigFile(){
		// search the classpath for system settings (configurations/default.config)
		URL config_file_URL = Config.class.getClassLoader().getResource(SYSTEM_SETTINGS);
		if(config_file_URL == null){
			System.err.println("could not find default config file '"+SYSTEM_SETTINGS+"'. aborting.");
			System.exit(1);
		}
		return new File(config_file_URL.toString());
	}

	/**
	 * Save all properties that have been modified by setProperty()
	 */
	public static void saveUserConfig() {
		if(instance != null){
			try {
				instance.save();
			} catch (ConfigurationException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void setProperty(String name, Object value){
		instance.setProperty(name, value);
	}
}
