package carpaccio.minigameCore.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelectionManager implements Listener {
    private final Map<UUID, Location> pos1Selections = new HashMap<>();
    private final Map<UUID, Location> pos2Selections = new HashMap<>();
    private final Map<UUID, String> pendingAreas = new HashMap<>();

    public void giveWand(Player p) {
        ItemStack wand = new ItemStack(Material.STICK);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName("§6Minigame Area Wand");
        wand.setItemMeta(meta);
        p.getInventory().addItem(wand);
        p.sendMessage("§aVarinha de seleção recebida! Clique esquerdo para pos1, direito para pos2.");
    }

    public Location getPos1(Player p) {
        //p.sendMessage("§aEnviando: "+ pos1Selections.get(p.getUniqueId()));
        return pos1Selections.get(p.getUniqueId());
    }

    public Location getPos2(Player p) {
        //p.sendMessage("§aEnviando: "+ pos2Selections.get(p.getUniqueId()));
        return pos2Selections.get(p.getUniqueId());
    }

    public String getPendingArea(Player p) {
        return pendingAreas.get(p.getUniqueId());
    }

    public void setPendingArea(Player p, String name) {
        pendingAreas.put(p.getUniqueId(), name);
    }

    public void clearPendingArea(Player p) {
        pendingAreas.remove(p.getUniqueId());
    }

    public void clearSelections(Player p) {
        UUID playerId = p.getUniqueId();
        pos1Selections.remove(playerId);
        pos2Selections.remove(playerId);
        pendingAreas.remove(playerId);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();  // Use event.getItem() para detectar o item na mão usada

        if (item != null && item.getType() == Material.STICK &&
                item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                item.getItemMeta().getDisplayName().equals("§6Minigame Area Wand")) {

            Player p = event.getPlayer();

            if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getClickedBlock() != null) {
                Location pos1 = event.getClickedBlock().getLocation();
                pos1Selections.put(p.getUniqueId(), pos1);
                p.sendMessage("§aPosição 1 definida: " + pos1.toVector());
                event.setCancelled(true);
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
                Location pos2 = event.getClickedBlock().getLocation();
                pos2Selections.put(p.getUniqueId(), pos2);
                p.sendMessage("§aPosição 2 definida: " + pos2.toVector());
                event.setCancelled(true);
            }
        }
    }
}
