package com.panicbar;

import com.panicbar.config.ClientConfig;
import com.panicbar.config.ServerConfig;
import com.panicbar.network.PanicNetwork;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("panicbar")
public class PanicBarMod {

    public PanicBarMod() {
        FMLJavaModLoadingContext.get().getModEventBus();

        PanicNetwork.register();

        
        net.minecraftforge.fml.ModLoadingContext.get()
                .registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC); 

        net.minecraftforge.fml.ModLoadingContext.get()
                .registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC); // gets loaded on both client and server, but only used on server for the sync stuff 
    }
}
// honestly I could've made a datapack why did I make a mod for this? I'm too deep to go back now 