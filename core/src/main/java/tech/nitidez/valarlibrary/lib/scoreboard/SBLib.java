package tech.nitidez.valarlibrary.lib.scoreboard;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class SBLib {
    private Player player;
    public SBLib(Player player) {
        this.player = player;
    }

    public void setBoard(String title, String... lines) {
        Scoreboard tempboard = player.getScoreboard();
        if (tempboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
            tempboard = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(tempboard);
        }

        Scoreboard board = tempboard;

        Objective obj = board.getObjective(DisplaySlot.SIDEBAR);
        if (obj == null) obj = board.registerNewObjective("sidebar", "dummy");

        Objective objective = obj;

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(title != null ? title : (objective.getDisplayName() != null ? objective.getDisplayName() : "§r"));

        if (lines == null) {
            List<String> oldEntries = board.getEntries().stream().filter(e -> objective.getScore(e).isScoreSet()).sorted(Comparator.comparingInt(entry -> objective.getScore((String) entry).getScore()).reversed()).collect(Collectors.toList());
            lines = oldEntries.size() > 0 ? oldEntries.toArray(new String[0]) : new String[]{"§f"};
        }

        board.getEntries().stream().filter(e -> objective.getScore(e).isScoreSet()).forEach(e -> {
            Map<Objective, Integer> otherScores = board.getObjectives().stream().filter(o -> !o.equals(objective) && o.getScore(e).isScoreSet()).collect(Collectors.toMap(obJ -> obJ, obJ -> obJ.getScore(e).getScore()));
            board.resetScores(e);
            otherScores.forEach((s, in) -> s.getScore(e).setScore(in));
        });

        List<String> reversed = Arrays.asList(lines);
        Collections.reverse(reversed);
        for (int i = 0; i < lines.length; i++) {
            objective.getScore(reversed.get(i)).setScore(i);
        }

    }

    public void setTitle(String title) {
        setBoard(title, (String[]) null);
    }

    public void setLines(String[] lines) {
        setBoard(null, lines);
    }

    public void setLines(String lines) {
        setLines(lines.split("\n"));
    }

    public void removeBoard() {
        Scoreboard board = this.player.getScoreboard();
        if (!board.equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
            Objective obj = board.getObjective(DisplaySlot.SIDEBAR);
            if (obj != null) obj.unregister();
        }
    }
}
