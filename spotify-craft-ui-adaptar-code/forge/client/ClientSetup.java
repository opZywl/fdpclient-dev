package com.opzywl.spotifycraft.forge.client;

import com.opzywl.spotifycraft.Main;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    public static KeyMapping openSpotifyKey;

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        openSpotifyKey = new KeyMapping(
                "key.spotifycraft.open_spotify", // Translation key
                GLFW.GLFW_KEY_Z,               // Default key
                "key.categories.spotifycraft"    // Category
        );
        event.register(openSpotifyKey);
    }
}