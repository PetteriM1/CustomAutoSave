package me.petterim1.autosave;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.level.LevelUnloadEvent;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;

import java.lang.reflect.Field;

public class CustomAutoSave extends PluginBase implements Listener {

    public void onEnable() {
        Class<?> c = getServer().getClass();
        try {
            Field f = c.getDeclaredField("autoSave");
            f.setAccessible(true);
            f.set(getServer(), Boolean.FALSE);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().scheduleDelayedRepeatingTask(this, this::doAutoSave, getServer().getConfig("ticks-per.autosave", 6000), getServer().getConfig("ticks-per.autosave", 6000));
        getLogger().notice("Auto save task is now handled by CustomAutoSave plugin");
    }

    public void onDisable() {
        for (Level l : getServer().getLevels().values()) {
            l.save(true);
        }
    }

    @EventHandler
    public void levelUnload(LevelUnloadEvent e) {
        e.getLevel().save(true);
    }

    public void doAutoSave() {
        for (Player player : getServer().getOnlinePlayers().values()) {
            if (player.isOnline()) {
                player.save(true);
            } else if (!player.isConnected()) {
                getServer().removePlayer(player);
            }
        }

        for (Level level : getServer().getLevels().values()) {
            getServer().getScheduler().scheduleTask(this, () -> level.save(true), true);
        }
    }
}
