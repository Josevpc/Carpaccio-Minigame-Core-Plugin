package carpaccio.minigameCore;

import carpaccio.minigameCore.utils.Cuboid;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class MinigameCore extends JavaPlugin implements Listener {

    private Cuboid cuboid;

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("Minigame Core has started!");

        Bukkit.getPluginManager().registerEvents(this, this);

        Cuboid cuboid = new Cuboid(
                new Location(Bukkit.getWorld("world"), 0 , 0 , 0),
                new Location(Bukkit.getWorld("world"), 1 ,1 ,1));

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){

        if (cuboid.contains(e.getPlayer().getLocation())) {
            //e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, )
        }


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
