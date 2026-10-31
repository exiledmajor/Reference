/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.createmod.catnip.config.ui.BaseConfigScreen
 *  net.neoforged.fml.IExtensionPoint
 *  net.neoforged.fml.ModContainer
 *  net.neoforged.neoforge.client.gui.IConfigScreenFactory
 */
package com.chromeware.client.config;

import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public final class ConfigScreen {
    public static void register(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (IExtensionPoint)((IConfigScreenFactory)(mod, parent) -> new BaseConfigScreen(parent, "chromeware").searchForConfigSpecs()));
    }
}

