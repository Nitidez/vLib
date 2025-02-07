package tech.nitidez.valarlibrary.lib.hologram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import tech.nitidez.valarlibrary.lib.entity.hologram.HologramLine;

public class Hologram {
    private Set<Player> playersShown;
    private Set<HologramLine> lines;
    private Location location;
    private boolean touchable;
    private BiConsumer<Hologram, Player> touchCallback;
    private static final double distance = 0.25;
    private static Set<Hologram> hologramSet = new LinkedHashSet<>();

    public Hologram(Location location, String text) {
        this.playersShown = new HashSet<>();
        this.lines = new LinkedHashSet<>();
        this.location = location;
        hologramSet.add(this);
        addLine(text);
    }

    public Set<HologramLine> getLines() {
        return lines;
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
        if (text.replace(" ", "").length() == 0) {
            addedLine = new HologramLine(location.clone().add(0, spaceIndex*distance, 0));
        } else {
            addedLine = new HologramLine(location.clone().add(0, spaceIndex*distance, 0), text);
        }
        addedLine.setTouchable(touchable);

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
        removedLine.setTouchable(false);
        removedLine.uncache();
        linesList.remove(index);
        lines = new LinkedHashSet<>(linesList);
    }

    public void setLine(int index, String text) {
        String[] textSplit = text.split("\n");
        if (textSplit.length > 1) {
            textSplit = Arrays.stream(textSplit).skip(1).toArray(String[]::new);
            addLine(index+1, String.join("\n", textSplit));
        }
        (new ArrayList<>(lines)).get(index).setText(text);
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

    public boolean isTouchable() {
        return touchable;
    }

    public void setTouchable(boolean touchable) {
        this.touchable = touchable;
        for (HologramLine line : lines) {
            line.setTouchable(touchable);
        }
    }

    public void onTouch(BiConsumer<Hologram, Player> callback) {
        this.touchCallback = callback;
    }

    public void touch(Player player) {
        if (this.touchCallback != null) {
            this.touchCallback.accept(this, player);
        }
    }

    public void setLocation(Location location) {
        List<HologramLine> linesList = new ArrayList<>(lines);
        this.location = location;
        for (int i = 0; i < lines.size(); i++) {
            int distanceI = lines.size()-i-1;
            linesList.get(i).move(location.clone().add(0, distance*distanceI, 0));
        }
    }

    public static Set<Hologram> getHolograms() {
        return hologramSet;
    }
    
}
