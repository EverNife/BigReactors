package erogenousbeef.bigreactors.common;

import java.io.File;

import erogenousbeef.bigreactors.Tags;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.Loader;

public class BRConfig {
	/**
	 * The version of Big Reactors.
	 * These are replaced by Gradle when built.
	 */
	public static final String VERSION = Tags.VERSION;
	public static final String MINECRAFT_VERSION = "[1.7.10]";

	public static final int WORLDGEN_VERSION = 1; // Bump this when changing world generation so the world regens

	/**
	 * The Big Reactors configuration file.
	 */
	public static final Configuration CONFIGURATION = new Configuration(new File(Loader.instance().getConfigDir(), "BigReactors" + File.separator + "BigReactors.cfg"));

	static
	{
		/**
		 * Loads the configuration and sets all the values.
		 */
		CONFIGURATION.load();
		CONFIGURATION.save();
	}
}
