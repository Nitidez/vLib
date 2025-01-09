package tech.nitidez.valarlibrary.libraries.hologram;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import tech.nitidez.valarlibrary.libraries.entity.hologram.HologramLine;

public class Hologram {
    private Set<Player> playersShown;
    private Set<HologramLine> lines;
    private Location location;
    public static final String itemRegex = "\\{item:([A-Za-z0-9_]+)\\}";
    private static final double distance = 0.25;
    private static Set<Hologram> hologramSet = new LinkedHashSet<>();

    public Hologram(Location location, String text) {
        this.playersShown = new HashSet<>();
        this.lines = new LinkedHashSet<>();
        this.location = location;
        hologramSet.add(this);
        addLine(text);
    }

    public Location getLocation() {
        return location;
    }

    private void refreshPlayers() {
        playersShown = Bukkit.getOnlinePlayers().stream().filter(p -> playersShown.contains(p)).collect(Collectors.toSet());
    }

    public Set<Player> getPlayers() {
        refreshPlayers();
        return playersShown;
    }

    public void addLine(String text) {
        addLine(lines.size(), text);
    }

    public void addLine(int index, String text) {
        String[] splitText = text.split("\n");
        for (int i = 0; i < splitText.length - 1; i++) {
            addLine(index+i+1, splitText[i+1]);
        }
        text = splitText[0];
        List<HologramLine> linesList = new ArrayList<>(lines);
        if (index > linesList.size()) index = linesList.size();
        if (index < 0) index = linesList.size()+1 - (index == 0 ? linesList.size()+1 : ((index * -1) % (linesList.size()+1)));
        for (int i = 0; i < index; i++) {
            HologramLine oHL = linesList.get(i);
            oHL.move(oHL.getLocation().clone().add(0, distance, 0));
        }
        int spaceIndex = linesList.size() - index - 1;
        HologramLine addedLine;
        Pattern pattern = Pattern.compile(itemRegex);
        Matcher matcher = pattern.matcher(text.replace(" ", ""));
        if (text.replace(" ", "").length() == 0) {
            addedLine = new HologramLine(location.clone().add(0, spaceIndex*distance, 0));
        } else if (matcher.find()) {
            String item = matcher.group(1);
            addedLine = new HologramLine(location.clone().add(0, spaceIndex*distance, 0), new ItemStack(Material.valueOf(item.toUpperCase())));
        } else {
            addedLine = new HologramLine(location.clone().add(0, spaceIndex*distance, 0), text);
        }

        linesList.add(index, addedLine);
        lines = new LinkedHashSet<>(linesList);
        for (Player p : getPlayers()) {
            addedLine.spawn(p);
        }
    }

    public void removeLine(int index) {
        List<HologramLine> linesList = new ArrayList<>(lines);
        if (linesList.size() == 0) return;
        if (index > linesList.size() - 1) index = linesList.size() - 1;
        if (index < 0) {
            index = (index * -1) % linesList.size();
            index = index == 0 ? linesList.size() : index;
            index = linesList.size() - index;
        }
        for (int i = 0; i < index; i++) {
            HologramLine oHL = linesList.get(i);
            oHL.move(oHL.getLocation().clone().add(0, -distance, 0));
        }
        HologramLine removedLine = linesList.get(index);
        removedLine.despawn();
        linesList.remove(index);
        lines = new LinkedHashSet<>(linesList);
    }

    public void setLine(int index, String text) {
        removeLine(index);
        addLine(index, text);
    }

    public void clear() {
        for (HologramLine line : lines) {
            removeLine((new ArrayList<>(lines)).indexOf(line));
        }
    }

    public void setText(String text) {
        clear();
        addLine(text);
    }

    public void spawn(Player... players) {
        for (Player p : players) {
            if (!playersShown.contains(p)) {
                playersShown.add(p);
                for (HologramLine line : lines) {
                    line.spawn(p);
                }
            }
        }
    }

    public void despawn(Player... players) {
        for (Player p : players) {
            if (playersShown.contains(p)) {
                playersShown.remove(p);
                for (HologramLine line : lines) {
                    line.despawn(p);
                }
            }
        }
    }

    public static Set<Hologram> getHolograms() {
        return hologramSet;
    }
    
}
