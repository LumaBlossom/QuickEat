package luma.blossom;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class QuickEatClient implements ClientModInitializer {
    private static QuickEatConfig config;
    private static KeyMapping quickEatKey;
    
    @Override
    public void onInitializeClient() {
        AutoConfig.register(QuickEatConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(QuickEatConfig.class).getConfig();
        
        quickEatKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.quickeat.eat",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "category.quickeat.main"
        ));
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && !client.isPaused()) {
                QuickEatHandler.getInstance().tick(client);
                
                while (quickEatKey.consumeClick()) {
                    QuickEatHandler.getInstance().startEating(client);
                }
            }
        });
        
        QuickEat.LOGGER.info("QuickEat client initialized!");
    }
    
    public static QuickEatConfig getConfig() {
        return config;
    }
    
    public static KeyMapping getQuickEatKey() {
        return quickEatKey;
    }
}

