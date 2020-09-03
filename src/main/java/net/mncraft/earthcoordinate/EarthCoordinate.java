package net.mncraft.earthcoordinate;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class EarthCoordinate extends JavaPlugin {

    FileConfiguration config = getConfig();
    int scale;

    public EarthCoordinate() {
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
                }
                else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', permissionDenied));
                }
                return true;
            }
            else if (args[0].equalsIgnoreCase("whereami")) {
                if (sender.hasPermission("coordinateconverter.basic") || sender.hasPermission("coordinateconverter.*")) {
                    Location loc = ((Player) sender).getLocation();
                    double[] coords = this.converterMC2RW(loc);
                    String result = "§aCC §e§l>§3 Lat: §b" + coords[0] + " §3Long: §b" + coords[1];
                    String mapUrl = "http://maps.google.com/maps?q=" + coords[0] + "," + coords[1] + "&ll=" + coords[0] + "," + coords[1] + "&z=5";

                    TextComponent msg = new TextComponent(result);
                    msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§aGoogle Map дээр нээх")));
                    msg.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, mapUrl));
                    sender.spigot().sendMessage(msg);
                }
                else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', permissionDenied));
                }
                return true;
            }
        }
        if (sender.hasPermission("coordinateconverter.basic") || sender.hasPermission("coordinateconverter.*")) {
            if (args.length == 2) {
                boolean validator = true;
                double latitude = 0;
                double longitude = 0;
                try {
                    latitude = Double.parseDouble(args[0]);
                    longitude = Double.parseDouble(args[1]);
                } catch (Exception e) {
                    validator = false;
                }
                if ((latitude > 90 || latitude < -90) || (longitude < -180 || longitude > 180)) {
                    validator = false;
                }
                if (validator) {
                    int[] coords = this.converterRW2MC(latitude, longitude);
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

    private int[] converterRW2MC(double latitude, double longitude) {
        int x = (int) Math.round(longitude * 120000 / this.scale);
        int z = -1 * (int) Math.round(latitude * 120000 / this.scale);
        return new int[]{x, z};
    }

    private double[] converterMC2RW(Location loc) {
        double longitude = loc.getX() * this.scale / 120000;
        double latitude = -1 * loc.getZ() * this.scale / 120000;
        return new double[]{latitude, longitude};
    }
}
