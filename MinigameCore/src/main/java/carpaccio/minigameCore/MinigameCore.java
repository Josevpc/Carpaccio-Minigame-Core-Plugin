package carpaccio.minigameCore;

import carpaccio.minigameCore.core.MobSpawnSystem;
import carpaccio.minigameCore.utils.Cuboid;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;


public final class MinigameCore extends JavaPlugin {

    private Cuboid cuboid;

    private MobSpawnSystem spawnSystem;

    @Override
    public void onEnable() {
        // Plugin startup logic

        spawnSystem = new MobSpawnSystem(this);

        spawnSystem.setPosition1(new Location(Bukkit.getWorld("world"), -22, 105, 22));
        spawnSystem.setPosition2(new Location(Bukkit.getWorld("world"), 16, 105, 50));
        spawnSystem.setAllowedMobs(EntityType.CHICKEN, EntityType.PIG);
        spawnSystem.setMaxMobs(5);
        spawnSystem.setSpawnInterval(40); // 2 segundos
        spawnSystem.start();

        getLogger().info("Minigame Core has started!");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){

        if (cuboid.contains(e.getPlayer().getLocation())) {
            e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("In the region"));
        }


    }

    @Override
    public void onDisable() {
        // Desliga o sistema e limpa os animais
        if (spawnSystem != null) {
            spawnSystem.shutdown();
        }
        getLogger().info("Minigame Core has deactilala!");
    }

//    @Override
//    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
//        if (!(sender instanceof Player)) {
//            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
//            return true;
//        }
//
//        Player p = (Player) sender;
//
//        if (cmd.getName().equalsIgnoreCase("animalspawn")) {
//            if (args.length == 0) {
//                sendHelp(p);
//                return true;
//            }
//
//            switch (args[0].toLowerCase()) {
//                case "pos1":
//                    spawnSystem.setPosition1(p.getLocation());
//                    p.sendMessage("§aPosição 1 definida: §f" + formatLocation(p.getLocation()));
//                    break;
//
//                case "pos2":
//                    spawnSystem.setPosition2(p.getLocation());
//                    p.sendMessage("§aPosição 2 definida: §f" + formatLocation(p.getLocation()));
//                    break;
//
//                case "start":
//                    if (spawnSystem.start()) {
//                        p.sendMessage("§aSpawn de animais iniciado!");
//                    } else {
//                        p.sendMessage("§cDefina as duas posições primeiro!");
//                    }
//                    break;
//
//                case "stop":
//                    spawnSystem.stop();
//                    p.sendMessage("§cSpawn de animais parado!");
//                    break;
//
//                case "clear":
//                    int count = spawnSystem.clearAllMobs();
//                    p.sendMessage("§e" + count + " animais removidos!");
//                    break;
//
//                case "info":
//                    sendInfo(p);
//                    break;
//
//                case "setmax":
//                    if (args.length < 2) {
//                        p.sendMessage("§cUso: /animalspawn setmax <número>");
//                        return true;
//                    }
//                    try {
//                        int max = Integer.parseInt(args[1]);
//                        spawnSystem.setMaxMobs(max);
//                        p.sendMessage("§aMáximo de animais definido para: §f" + max);
//                    } catch (NumberFormatException e) {
//                        p.sendMessage("§cNúmero inválido!");
//                    }
//                    break;
//
//                case "setinterval":
//                    if (args.length < 2) {
//                        p.sendMessage("§cUso: /animalspawn setinterval <ticks>");
//                        return true;
//                    }
//                    try {
//                        int interval = Integer.parseInt(args[1]);
//                        spawnSystem.setSpawnInterval(interval);
//                        p.sendMessage("§aIntervalo de spawn definido para: §f" + interval + " ticks");
//                    } catch (NumberFormatException e) {
//                        p.sendMessage("§cNúmero inválido!");
//                    }
//                    break;
//
//                default:
//                    p.sendMessage("§cComando inválido! Use /animalspawn para ver os comandos.");
//            }
//            return true;
//        }
//        return false;
//    }

    private void sendHelp(Player p) {
        p.sendMessage("§e=== Animal Spawn Plugin ===");
        p.sendMessage("§6/animalspawn pos1 §f- Define a primeira posição");
        p.sendMessage("§6/animalspawn pos2 §f- Define a segunda posição");
        p.sendMessage("§6/animalspawn start §f- Inicia o spawn");
        p.sendMessage("§6/animalspawn stop §f- Para o spawn");
        p.sendMessage("§6/animalspawn clear §f- Remove todos os animais");
        p.sendMessage("§6/animalspawn info §f- Mostra informações");
        p.sendMessage("§6/animalspawn setmax <número> §f- Define máximo de animais");
        p.sendMessage("§6/animalspawn setinterval <ticks> §f- Define intervalo de spawn");
    }

    private void sendInfo(Player p) {
        p.sendMessage("§e=== Informações ===");
        p.sendMessage("§6Posição 1: §f" +
                (spawnSystem.getPosition1() != null ? formatLocation(spawnSystem.getPosition1()) : "Não definida"));
        p.sendMessage("§6Posição 2: §f" +
                (spawnSystem.getPosition2() != null ? formatLocation(spawnSystem.getPosition2()) : "Não definida"));
        p.sendMessage("§6Status: §f" + (spawnSystem.isActive() ? "§aAtivo" : "§cParado"));
        p.sendMessage("§6Animais spawnados: §f" + spawnSystem.getMobCount() + "/" + spawnSystem.getMaxMobs());
    }

    private String formatLocation(org.bukkit.Location loc) {
        return String.format("X: %d, Y: %d, Z: %d",
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    /**
     * Método público para acessar o sistema de spawn de outras classes
     */
    public MobSpawnSystem getSpawnSystem() {
        return spawnSystem;
    }
}
