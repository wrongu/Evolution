package applet;

import java.util.Iterator;

import org.apache.commons.configuration.*;

/**
 * This class manages a static instance of a configuration file
 * 
 * @author wrongu
 *
 */
public class Config {
	
	public static CompositeConfiguration instance;
	private static final String DEFAULT_SETTINGS = "default.config";
	
	/**
	 * load the requested configuration file. the ".config" extension is optional
	 * 
	 * @return true iff successful
	 */
	public static boolean load(String name){		
		// check for proper extension and fix it!
		if(!name.endsWith(".config")) name += ".config";
		
		// using composite configuration so that any values not set by the user will default
		// 	to the system settings (see configurations/default.config)
		instance = new CompositeConfiguration();
		
		// by adding user_config_file first, it will always be checked first
		// (thus overriding default values)
		try {
			PropertiesConfiguration local = new PropertiesConfiguration(name);
			instance.addConfiguration(local);
		} catch (ConfigurationException e) {
			System.err.println("Cannot create local configuration file. Only defaults will be used.");
			return false;
		}
		
		// add the default properties
		// note that the composite configuration will always check the first configuration (i.e. user) before the second (i.e. defaults)
		try {
			PropertiesConfiguration defaults = new PropertiesConfiguration(DEFAULT_SETTINGS);
			instance.addConfiguration(defaults);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		System.out.println("Loaded configuration!");
		Iterator<String> itr = instance.getKeys();
		while(itr.hasNext()) System.out.println("'"+itr.next()+"'");
		
		return true;
	}
}
