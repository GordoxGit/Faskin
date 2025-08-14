package fr.heneriacore.cmd;

import fr.heneriacore.HeneriaCore;
import fr.heneriacore.db.SQLiteManager;
import fr.heneriacore.prefs.PreferencesManager;
import fr.heneriacore.skin.SkinServiceImpl;
import fr.heneriacore.skin.TextureCache;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class DebugCommand {
    private final HeneriaCore plugin;
    private final SQLiteManager db;
    private final PreferencesManager prefs;
    private final SkinServiceImpl skinService;
    private final TextureCache cache;

    public DebugCommand(HeneriaCore plugin, SQLiteManager db, PreferencesManager prefs,
                        SkinServiceImpl skinService, TextureCache cache) {
        this.plugin = plugin;
        this.db = db;
        this.prefs = prefs;
        this.skinService = skinService;
        this.cache = cache;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("heneria.debug")) {
            sender.sendMessage("No permission");
            return true;
        }
        List<String> lines = snapshot();
        if (args.length >= 2 && args[1].equalsIgnoreCase("export")) {
            if (!plugin.getConfig().getBoolean("debug.export_enabled", true)) {
                sender.sendMessage("Export disabled");
                return true;
            }
            String dirPath = plugin.getConfig().getString("debug.export_dir", plugin.getDataFolder() + "/debug");
            File dir = new File(dirPath);
            dir.mkdirs();
            File out = new File(dir, "heneria-debug-" + System.currentTimeMillis() + ".log");
            try {
                Files.write(out.toPath(), String.join("\n", lines).getBytes(StandardCharsets.UTF_8));
                sender.sendMessage("Debug exported to " + out.getPath());
            } catch (IOException e) {
                sender.sendMessage("Export failed: " + e.getMessage());
            }
            return true;
        }
        for (String line : lines) {
            sender.sendMessage(line);
        }
        return true;
    }

    private List<String> snapshot() {
        List<String> lines = new ArrayList<>();
        lines.add("HeneriaCore v" + plugin.getDescription().getVersion());
        lines.add("Applier: " + skinService.getApplierName());
        lines.add("TextureCache: entries=" + cache.size());
        lines.add("DB: path=" + db.getDbFile().getPath());
        lines.add("OptedOutCount: " + prefs.countOptedOut());
        return lines;
    }
}
