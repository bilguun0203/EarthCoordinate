package net.mncraft.earthcoordinate;

import net.md_5.bungee.api.chat.ClickEvent;
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
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EarthCoordinate extends JavaPlugin {

    FileConfiguration config = getConfig();
    int scale;

    public EarthCoordinate() {
        this.scale = config.getInt("scale");
    }

    @Override
    public void onEnable() {
        config.addDefault("scale", 2000);
        config.addDefault("map_url", "http://maps.google.com/maps?q=%latitude%,%longitude%&ll=%latitude%,%longitude%&z=5");
        config.addDefault("lang.prefix", "&aEC &e&l> ");
        config.addDefault("lang.no_permission", "&cYou don't have permission");
        config.addDefault("lang.config_reloaded", "&aConfiguration reloaded");
        config.addDefault("lang.convert_tomc", "&3X: &b%x% &3Z: &b%z%");
        config.addDefault("lang.convert_tomc_invalid", "&cInvalid coordinates");
        config.addDefault("lang.convert_toearth", "&3Latitude: &b%latitude% &3Longitude: &b%longitude%");
        config.addDefault("lang.open_map", "Open in Google Maps");
        config.addDefault("lang.latitude", "latitude");
        config.addDefault("lang.longitude", "longitude");
        config.addDefault("lang.help.header", "&8&m-----[ &aCoordinate Converter &8&m]-----");
        config.addDefault("lang.help.tomc", "&d/ecoord &btomc &3<latitude> <longitude> &e- &aConverts Earth coordinates to Minecraft coordinates");
        config.addDefault("lang.help.toearth", "&d/ecoord &btoearth &3[x] [z] &e- &aConverts Minecraft coordinates to Earth coordinates");
        config.addDefault("lang.help.reload", "&d/ecoord &breload &e- &aReloads the plugin configuration");
        config.options().copyDefaults(true);
        saveConfig();
        getLogger().info("Scale: " + scale);
        getCommand("earthcoordinate").setTabCompleter(this);
    }

    @Override
    public void onDisable() {
        //
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> commands = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission( "earthcoordinate.reload"))
                commands.add("reload");
            if (sender.hasPermission( "earthcoordinate.help"))
                commands.add("help");
            if (sender.hasPermission( "earthcoordinate.convert.tomc"))
                commands.add("tomc");
            if (sender.hasPermission( "earthcoordinate.convert.toearth.self") || sender.hasPermission( "earthcoordinate.convert.toearth.coords"))
                commands.add("toearth");
            StringUtil.copyPartialMatches(args[0], commands, completions);
        } else if (args.length == 2) {
            if (args[0].equals("tomc")) {
                if (sender.hasPermission("earthcoordinate.convert.tomc"))
                    commands.add("<" + this.config.getString("lang.latitude") + ">");
            }
            if (args[0].equals("toearth")) {
                if (sender.hasPermission("earthcoordinate.convert.toearth.coords"))
                    commands.add("<x>");
            }
            StringUtil.copyPartialMatches(args[1], commands, completions);
        } else if (args.length == 3) {
            if (args[0].equals("tomc")) {
                if (sender.hasPermission("earthcoordinate.convert.tomc"))
                    commands.add("<" + this.config.getString("lang.longitude") + ">");
            }
            if (args[0].equals("toearth")) {
                if (sender.hasPermission("earthcoordinate.convert.toearth.coords"))
                    commands.add("<z>");
            }
            StringUtil.copyPartialMatches(args[2], commands, completions);
        }
        Collections.sort(completions);
        return completions;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            String cmdStr = args[0].toLowerCase();
            switch (cmdStr) {
                case "reload":
                    if (sender.hasPermission("earthcoordinate.reload")) {
                        this.commandECReload(sender);
                    } else {
                        this.sendNoPermission(sender);
                    }
                    return true;
                case "toearth":
                    if (args.length == 1) {
                        if(sender.hasPermission("earthcoordinate.convert.toearth.self")) {
                            this.commandMC2Earth(sender);
                        }
                        else {
                            this.sendNoPermission(sender);
                        }
                        return true;
                    }
                    else if (args.length == 3) {
                        if(sender.hasPermission("earthcoordinate.convert.toearth.coords")) {
                            this.commandMC2Earth(sender, args);
                        }
                        else {
                            this.sendNoPermission(sender);
                        }
                        return true;
                    }
                    break;
                case "tomc":
                    if(args.length == 3) {
                        if (sender.hasPermission("earthcoordinate.convert.tomc")) {
                            this.commandEarth2MC(sender, args);
                        }
                        else {
                            this.sendNoPermission(sender);
                        }
                        return true;
                    }
                    break;
            }
        }
        if(sender.hasPermission("earthcoordinate.help")) {
            this.commandECHelp(sender);
        }
        else {
            this.sendNoPermission(sender);
        }
        return true;
    }

    private void commandECReload(CommandSender sender) {
        this.reloadConfig();
        config = getConfig();
        scale = config.getInt("scale");
        getLogger().info("Scale: " + scale);
        sender.sendMessage(this.getMessage("lang.help.header"));
    }

    private void commandECHelp(CommandSender sender) {
        sender.sendMessage(this.getMessageWithoutPrefix("lang.help.header"));
        sender.sendMessage(this.getMessageWithoutPrefix("lang.help.tomc"));
        sender.sendMessage(this.getMessageWithoutPrefix("lang.help.toearth"));
        sender.sendMessage(this.getMessageWithoutPrefix("lang.help.reload"));
    }

    private void commandEarth2MC(CommandSender sender, String[] args) {
        double latitude;
        double longitude;
        try {
            latitude = Double.parseDouble(args[1]);
            longitude = Double.parseDouble(args[2]);
            if (this.validateEarthCoordinates(latitude, longitude)) {
                this.commandEarth2MC(sender, latitude, longitude);
            } else {
                sender.sendMessage(this.getMessage("lang.convert_tomc_invalid"));
            }
        } catch (Exception ignored) {}
    }

    private void commandEarth2MC(CommandSender sender, double latitude, double longitude) {
        double[] coords = this.converterEarth2MC(latitude, longitude);
        sender.sendMessage(this.getMessage("lang.convert_tomc")
                .replace("%x%", String.format("%.2f", coords[0]))
                .replace("%z%", String.format("%.2f", coords[1])));
    }

    private void commandMC2Earth(CommandSender sender) {
        this.commandMC2Earth(sender, ((Player)sender).getLocation());
    }

    private void commandMC2Earth(CommandSender sender, String[] args) {
        double x;
        double z;
        try {
            x = Double.parseDouble(args[1]);
            z = Double.parseDouble(args[2]);
            this.commandMC2Earth(sender, x, z);
        } catch (Exception ignored) {}
    }

    private void commandMC2Earth(CommandSender sender, Location loc) {
        this.commandMC2Earth(sender, loc.getX(), loc.getZ());
    }

    private void commandMC2Earth(CommandSender sender, double x, double z) {
        double[] coords = this.converterMC2Earth(x, z);
        String mapUrl = ("" + this.config.getString("map_url"))
                .replace("%latitude%", String.format("%.6f", coords[0]))
                .replace("%longitude%", String.format("%.6f", coords[1]));

        TextComponent msg = new TextComponent(this.getMessage("lang.convert_toearth")
                .replace("%latitude%", String.format("%.4f", coords[0]))
                .replace("%longitude%", String.format("%.4f", coords[1])));
        msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(this.getMessageWithoutPrefix("lang.open_map"))));
        msg.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, mapUrl));
        sender.spigot().sendMessage(msg);
    }

    private void sendNoPermission(CommandSender sender) {
        sender.sendMessage(this.getMessage("lang.no_permission"));
    }

    private double[] converterEarth2MC(double latitude, double longitude) {
        double x = longitude * 120000 / this.scale;
        double z = -1 * latitude * 120000 / this.scale;
        return new double[]{x, z};
    }

    private double[] converterMC2Earth(double x, double z) {
        double longitude = x * this.scale / 120000;
        double latitude = -1 * z * this.scale / 120000;
        return new double[]{latitude, longitude};
    }

    private boolean validateEarthCoordinates(double latitude, double longitude) {
        return ((latitude <= 90 && latitude >= -90) && (longitude >= -180 && longitude <= 180));
    }

    public String getMessageWithoutPrefix(String path) {
        return ChatColor.translateAlternateColorCodes('&', "" + config.getString(path));
    }

    public String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', config.getString("lang.prefix") + config.getString(path));
    }
}
