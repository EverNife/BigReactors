package erogenousbeef.bigreactors.api.registry;

import java.util.Map;
import java.util.TreeMap;

import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.StatCollector;

public class Keys {

	private static final String category = StatCollector.translateToLocal("itemGroup.BigReactors");
	private static Map<String, KeyBinding> keys = new TreeMap<String, KeyBinding>(); 

    public static void register(){
    	for(KeyBinding key : keys.values())
    		setRegister(key);
    }
    
    public static void addKey(String name, String local,  int key) {
    	keys.put(name, new KeyBinding(local, key, category));
    }
    
    public static KeyBinding getKey(String name) {
    	return keys.get(name);
    }

    private static void setRegister(KeyBinding binding){
        ClientRegistry.registerKeyBinding(binding);
    }
	
}
