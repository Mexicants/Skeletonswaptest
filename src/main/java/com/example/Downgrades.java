/*
 * Copyright (c) 2018, Paradoxis <https://github.com/Paradoxis>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.downgrades;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;

import net.runelite.api.*;
import net.runelite.api.VarClientInt;
import net.runelite.api.events.ConfigChanged;

import net.runelite.api.events.VarClientIntChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.cache.definitions.ModelDefinition;
import net.runelite.cache.definitions.loaders.ModelLoader;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.interfacestyles.InterfaceStylesPlugin;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.compress.utils.IOUtils;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@PluginDescriptor(
        name = "Downgrades",
        description = "Downgrade models, textures, interfaces and objects to that of the 2005 / 2006 client.",
        tags = {"downgrades", "npcs", "textures", "interfaces", "models", "objects"},
        enabledByDefault = false
)
@Slf4j
public class DowngradesPlugin extends Plugin
{
    int WIDGET_ACCOUNT_MANAGEMENT = 35913767;

    //<editor-fold desc="Dependency Injection & Configuration">
    @Inject
    private Client client;

    @Inject
    private DowngradesConfig config;


    @Provides
    DowngradesConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(DowngradesConfig.class);
    }
    //</editor-fold>

    //<editor-fold desc="Startup / Shutdown event handlers">
    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (event.getKey().equals("replaceIgnoreListIcon")) {
            if (event.getNewValue().equals("true")) {
                updateIgnoreListIcon();
            } else {
                restoreIgnoreListIcon();
            }
        }

        if (event.getKey().equals("replaceModels")) {
            if (event.getNewValue().equals("true")) {
                updateModels();
            } else {
                restoreModels();
            }
        }
    }

    public void startUp()
    {
        if (config.replaceIgnoreListIcon()) {
            updateIgnoreListIcon();
        }

        if (config.replaceModels()) {
            updateModels();
        }
    }

    public void shutDown()
    {
        if (config.replaceIgnoreListIcon()) {
            restoreIgnoreListIcon();
        }

        if (config.replaceModels()) {
            restoreModels();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Model replacements">
    private void updateModels()
    {

        try {

            int BUCKET_ITEM_ID = 1925;
            int BUCKET_MODEL_ID = 2397;

            // Raw data
            byte[] data = loadModel(BUCKET_MODEL_ID);

            // Runelite
            // r:92 -> g:19 -> g:22
            // r = client[panel]
            ItemComposition item = client.getItemDefinition(BUCKET_ITEM_ID);

            // New
            ModelLoader loader = new ModelLoader();
            ModelDefinition model = loader.load(BUCKET_MODEL_ID, data);

            log.debug("Test");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] loadModel(int id) throws IOException
    {
        String path = "models/" + Integer.toString(id) + ".mdl";
        InputStream file = DowngradesPlugin.class.getResourceAsStream(path);
        return IOUtils.toByteArray(file);
    }

    private void restoreModels()
    {
        // TODO
    }


//    @Subscribe
//    public void onClientTick(ClientTick event)
//    {
//        int SKELETON_MAGE = 84;
//
//        // Skeleton composition ID's
//        //  - 74
//        //  - 75
//        //  - 76
//        //
//        //  comp.k = ['attack']
//        //  comp.l = 'Skeleon'
//        //  comp.ck = <composition id>
//        //  comp.w = <models>
//
//        NPCComposition basecomp = client.getNpcDefinition(SKELETON_MAGE);
//        int[] basemodels = basecomp.getModels();
//
//
//        for (NPC npc: client.getNpcs()) {
//            NPCComposition comp = npc.getComposition();
//            int[] models = comp.getModels();
//
//            if (Arrays.equals(models, basemodels)) {
//                continue;
//            }
//
//            tryReplaceComposition(comp, models, basemodels);
//        }
//    }
//
//    public void tryReplaceComposition(Object parent, Object find, Object replace)
//    {
//        try {
//            String memoryFieldName = getFieldName(parent, find);
//
//            if (memoryFieldName == null) {
//                log.error("Failed to lookup object name, can't replace field. Is your find object inside the parent?");
//                return;
//            }
//
//            Field field = parent.getClass().getDeclaredField(memoryFieldName);
//            field.setAccessible(true);
//            field.set(parent, replace);
//
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public String getFieldName(Object parent, Object find)
//    {
//        for (Field field : parent.getClass().getDeclaredFields())
//        {
//            field.setAccessible(true);
//
//            try {
//                if (field.get(parent) != null && field.get(parent).equals(find)) {
//                    return field.getName();
//                }
//            } catch (IllegalAccessException e) {
//                return null;
//            }
//        }
//
//        return null;
//    }
    //</editor-fold>


    //<editor-fold desc="Ignore list icon">
    private void updateIgnoreListIcon()
    {
        String path = "sprites/" + Integer.toString(SpriteID.TAB_IGNORES) + ".png";

        try (InputStream inputStream = DowngradesPlugin.class.getResourceAsStream(path))
        {
            log.debug("Loading: " + path);
            BufferedImage spriteImage = ImageIO.read(inputStream);
            SpritePixels spritePixels = ImageUtil.getImageSpritePixels(spriteImage, client);

            if (spritePixels != null)
            {
                client.getWidgetSpriteOverrides().put(WIDGET_ACCOUNT_MANAGEMENT, spritePixels);
            }
        }
        catch (IOException ex)
        {
            log.debug("Unable to load image: ", ex);
        }
        catch (IllegalArgumentException ex)
        {
            log.debug("Input stream of file path " + path + " could not be read: ", ex);
        }
    }

    private void restoreIgnoreListIcon()
    {
        client.getWidgetSpriteOverrides().remove(WIDGET_ACCOUNT_MANAGEMENT);
    }
    //</editor-fold>
}
