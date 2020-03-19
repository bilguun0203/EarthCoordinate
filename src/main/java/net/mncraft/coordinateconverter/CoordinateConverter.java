package net.mncraft.coordinateconverter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class CoordinateConverter extends JavaPlugin {

    FileConfiguration config = getConfig();
    int scale;

    public CoordinateConverter() {
        this.scale = config.getInt("scale");
    }

    @Override
    public void onEnable() {
        config.addDefault("scale", 2000);
        config.options().copyDefaults(true);
        saveConfig();
        getLogger().info("Scale: " + scale);
    }

    @Override
    public void onDisable() {
        //
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String permissionDenied = "&aCC &e&l> &cPermission denied";
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("coordinateconverter.reload") || sender.hasPermission("coordinateconverter.*")) {
                    this.reloadConfig();
                    config = getConfig();
                    scale = config.getInt("scale");
                    getLogger().info("Scale: " + scale);
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aCC &e&l> &aConfiguration reloaded."));
                    return true;
                }
                else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', permissionDenied));
                    return true;
                }
            }
        }
        if (sender.hasPermission("coordinateconverter.basic") || sender.hasPermission("coordinateconverter.*")) {
            if (args.length == 2) {
                boolean validator = true;
                float latitude = 0;
                float longitude = 0;
                try {
                    latitude = Float.parseFloat(args[0]);
                    longitude = Float.parseFloat(args[1]);
                } catch (Exception e) {
                    validator = false;
                }
                if ((latitude > 90 || latitude < -90) || (longitude < -180 || longitude > 180)) {
                    validator = false;
                }
                if (validator) {
                    int[] coords = this.converter(latitude, longitude);
                    String result = "&aCC &e&l>&3 X: &b" + coords[0] + " &3Z: &b" + coords[1];
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', result));
                } else {
                    String result = "&aCC &e&l>&c Invalid coordinates";
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', result));
                }
            } else {
                String header = "&8&m-----[ &aCoordinate Converter &8&m]-----";
                String usage = "&dUsage: &b/" + command.getName() + " &3<lat> <long>";
                String example = "&dExample: &b/" + command.getName() + " &347.9184676 106.9177016";
                String aliases = "&dAliases: &b" + String.join("&a, &b", command.getAliases());
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', usage));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', example));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aliases));
            }
            return true;
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', permissionDenied));
        return true;
    }

    private int[] converter(float latitude, float longitude) {
        int x = Math.round(longitude * 120000 / this.scale);
        int z = -1 * Math.round(latitude * 120000 / this.scale);
        return new int[]{x, z};
    }
}
