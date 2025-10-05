package carpaccio.minigameCore.commands;

import carpaccio.minigameCore.core.MobSpawnManager;
import carpaccio.minigameCore.core.MobSpawnSystem;
import carpaccio.minigameCore.utils.RegionPreview;
import carpaccio.minigameCore.utils.SelectionManager;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class MiniCoreCommand implements CommandExecutor {

    private final MobSpawnManager spawnManager;
    private final SelectionManager selectionManager;
    private final RegionPreview regionPreview;

    public MiniCoreCommand(MobSpawnManager spawnManager, SelectionManager selectionManager, RegionPreview regionPreview) {
        this.spawnManager = spawnManager;
        this.selectionManager = selectionManager;
        this.regionPreview = regionPreview;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return true;
        }

        Player p = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("minicore")) {
            if (args.length == 0) {
                sendHelp(p);
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "wand":
                    selectionManager.giveWand(p);
                    break;

                case "create":
                    handleCreate(p, args);
                    break;

                case "confirm":
                    handleConfirm(p);
                    break;

                case "cancel":
                    selectionManager.clearSelections(p);
                    regionPreview.stopPreview(p);
                    p.sendMessage("§aSeleções e área pendente canceladas!");
                    break;

                case "remove":
                    handleRemove(p, args);
                    break;

                case "start":
                    handleStart(p, args);
                    break;

                case "stop":
                    handleStop(p, args);
                    break;

                case "toggle":
                    handleToggle(p, args);
                    break;

                case "clear":
                    handleClear(p, args);
                    break;

                case "info":
                    handleInfo(p, args);
                    break;

                case "list":
                    handleList(p);
                    break;

                case "update":
                    handleUpdate(p, args);
                    break;

                case "startall":
                    spawnManager.startAll();
                    p.sendMessage("§aTodas as áreas foram iniciadas!");
                    break;

                case "stopall":
                    spawnManager.stopAll();
                    p.sendMessage("§cTodas as áreas foram paradas!");
                    break;

                case "clearall":
                    int total = spawnManager.clearAll();
                    p.sendMessage("§e" + total + " mobs foram removidos de todas as áreas!");
                    break;

                case "reload":
                    spawnManager.reloadAreas();
                    p.sendMessage("§aÁreas recarregadas da configuração!");
                    break;

                case "stats":
                    handleStats(p);
                    break;

                default:
                    p.sendMessage("§cComando inválido! Use /minicore para ver os comandos.");
            }
            return true;
        }
        return false;
    }

    private void handleCreate(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cUso: /minicore create <nome>");
            return;
        }

        String name = args[1];

        if (spawnManager.hasArea(name)) {
            p.sendMessage("§cJá existe uma área com este nome!");
            return;
        }

        Location pos1 = selectionManager.getPos1(p);
        Location pos2 = selectionManager.getPos2(p);

        if (pos1 == null || pos2 == null) {
            p.sendMessage("§cDefina as posições 1 e 2 usando a varinha de seleção!");
            return;
        }

        if (!pos1.getWorld().equals(pos2.getWorld())) {
            p.sendMessage("§cAs posições devem estar no mesmo mundo!");
            return;
        }

        selectionManager.setPendingArea(p, name);
        regionPreview.showColoredPreview(p, pos1, pos2, Color.RED);
        p.sendMessage("§aPrévia da área exibida! Use /minicore confirm para criar a área '" + name + "'.");
    }

    private void handleConfirm(Player p) {
        String name = selectionManager.getPendingArea(p);

        if (name == null) {
            p.sendMessage("§cNenhuma área pendente para confirmar! Use /minicore create primeiro.");
            return;
        }

        Location pos1 = selectionManager.getPos1(p);
        Location pos2 = selectionManager.getPos2(p);

        if (pos1 == null || pos2 == null) {
            p.sendMessage("§cPosições não definidas! Use a varinha para selecionar novamente.");
            selectionManager.clearPendingArea(p);
            return;
        }

        EntityType[] mobs = {EntityType.COW, EntityType.SHEEP, EntityType.PIG, EntityType.CHICKEN};
        boolean created = spawnManager.createArea(name, pos1, pos2, mobs, 20, 100, 10);

        if (created) {
            p.sendMessage("§aÁrea '" + name + "' criada com sucesso!");
            p.sendMessage("§6Use /minicore start " + name + " para ativar");
            selectionManager.clearSelections(p);
            regionPreview.stopPreview(p);
        } else {
            p.sendMessage("§cErro ao criar a área!");
        }
    }

    private void handleRemove(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cUso: /minicore remove <nome>");
            return;
        }

        String name = args[1];

        if (spawnManager.removeArea(name)) {
            p.sendMessage("§aÁrea '" + name + "' removida!");
        } else {
            p.sendMessage("§cÁrea não encontrada!");
        }
    }

    private void handleStart(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cUso: /minicore start <nome>");
            return;
        }

        String name = args[1];

        if (spawnManager.startArea(name)) {
            p.sendMessage("§aÁrea '" + name + "' iniciada!");
        } else {
            p.sendMessage("§cÁrea não encontrada ou já está ativa!");
        }
    }

    private void handleStop(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cUso: /minicore stop <nome>");
            return;
        }

        String name = args[1];

        if (spawnManager.stopArea(name)) {
            p.sendMessage("§cÁrea '" + name + "' parada!");
        } else {
            p.sendMessage("§cÁrea não encontrada!");
        }
    }

    private void handleToggle(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cUso: /minicore toggle <nome>");
            return;
        }

        String name = args[1];

        if (spawnManager.toggleArea(name)) {
            MobSpawnSystem system = spawnManager.getSystem(name);
            String status = system.isActive() ? "§ainiciada" : "§cparada";
            p.sendMessage("§eÁrea '" + name + "' " + status + "!");
        } else {
            p.sendMessage("§cÁrea não encontrada!");
        }
    }

    private void handleClear(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cUso: /minicore clear <nome>");
            return;
        }

        String name = args[1];
        int count = spawnManager.clearArea(name);

        if (count > 0) {
            p.sendMessage("§e" + count + " mobs removidos da área '" + name + "'!");
        } else {
            p.sendMessage("§cÁrea não encontrada ou sem mobs!");
        }
    }

    private void handleInfo(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cUso: /minicore info <nome>");
            return;
        }

        String name = args[1];
        String info = spawnManager.getAreaInfo(name);
        p.sendMessage(info);
    }

    private void handleList(Player p) {
        p.sendMessage("§e=== Áreas de Spawn ===");

        java.util.List<String> areas = spawnManager.listAreas();

        if (areas.isEmpty()) {
            p.sendMessage("§7Nenhuma área cadastrada");
        } else {
            areas.forEach(p::sendMessage);
        }

        p.sendMessage("§7Total: §f" + areas.size() + " áreas");
    }

    private void handleUpdate(Player p, String[] args) {
        if (args.length < 4) {
            p.sendMessage("§cUso: /minicore update <nome> <max> <interval>");
            return;
        }

        String name = args[1];

        try {
            int maxMobs = Integer.parseInt(args[2]);
            int interval = Integer.parseInt(args[3]);

            if (spawnManager.updateArea(name, maxMobs, interval)) {
                p.sendMessage("§aÁrea '" + name + "' atualizada!");
                p.sendMessage("§6Max Mobs: §f" + maxMobs);
                p.sendMessage("§6Intervalo: §f" + interval + " ticks");
            } else {
                p.sendMessage("§cÁrea não encontrada!");
            }
        } catch (NumberFormatException e) {
            p.sendMessage("§cValores inválidos!");
        }
    }

    private void handleStats(Player p) {
        p.sendMessage("§e=== Estatísticas Globais ===");
        p.sendMessage("§6Total de Áreas: §f" + spawnManager.getAreaNames().size());
        p.sendMessage("§6Áreas Ativas: §f" + spawnManager.getActiveAreasCount());
        p.sendMessage("§6Total de Mobs: §f" + spawnManager.getTotalMobs());
        p.sendMessage("");
        p.sendMessage("§6Áreas Ativas:");
        spawnManager.getActiveAreas().forEach(name ->
                p.sendMessage("  §a✓ §f" + name)
        );
    }

    private void sendHelp(Player p) {
        p.sendMessage("§e=== Mob Spawn Manager ===");
        p.sendMessage("§6/minicore wand §f- Recebe a varinha de seleção");
        p.sendMessage("§6/minicore create <nome> §f- Inicia criação de uma área");
        p.sendMessage("§6/minicore confirm §f- Confirma a criação da área");
        p.sendMessage("§6/minicore cancel §f- Cancela a criação da área");
        p.sendMessage("§6/minicore remove <nome> §f- Remove uma área");
        p.sendMessage("§6/minicore start <nome> §f- Inicia uma área");
        p.sendMessage("§6/minicore stop <nome> §f- Para uma área");
        p.sendMessage("§6/minicore toggle <nome> §f- Liga/desliga uma área");
        p.sendMessage("§6/minicore clear <nome> §f- Limpa mobs de uma área");
        p.sendMessage("§6/minicore info <nome> §f- Informações da área");
        p.sendMessage("§6/minicore list §f- Lista todas as áreas");
        p.sendMessage("§6/minicore update <nome> <max> <interval> §f- Atualiza área");
        p.sendMessage("§6/minicore startall §f- Inicia todas as áreas");
        p.sendMessage("§6/minicore stopall §f- Para todas as áreas");
        p.sendMessage("§6/minicore clearall §f- Limpa todos os mobs");
        p.sendMessage("§6/minicore reload §f- Recarrega configuração");
        p.sendMessage("§6/minicore stats §f- Estatísticas globais");
    }
}

