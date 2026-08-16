package com.panicbar.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "panicbar", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("panic_bar", new PanicBarOverlay());
    }
}
// this file was crashing the damn building process because I forgot to close a bracket... sigh