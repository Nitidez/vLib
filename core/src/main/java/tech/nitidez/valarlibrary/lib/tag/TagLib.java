package tech.nitidez.valarlibrary.lib.tag;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;

public class TagLib {

    private static final Map<Player, Scoreboard> playerScoreboards = new HashMap<>();
    private static final Map<Player, Map<Player, TagTeam>> tagData = new HashMap<>();

    public static void setPrefix(Player player, Player target, String prefix) {
        TagTeam oldTag = getOrCreateTagTeam(player, target);
        TagTeam newTag = (oldTag == null) ? new TagTeam(target, prefix, "", 0) : oldTag.clone();
        newTag.setPrefix(prefix);
        updateTag(player, target, oldTag, newTag);
    }

    public static void setSuffix(Player player, Player target, String suffix) {
        TagTeam oldTag = getOrCreateTagTeam(player, target);
        TagTeam newTag = (oldTag == null) ? new TagTeam(target, "", suffix, 0) : oldTag.clone();
        newTag.setSuffix(suffix);
        updateTag(player, target, oldTag, newTag);
    }

    public static void setPriority(Player player, Player target, int priority) {
        TagTeam oldTag = getOrCreateTagTeam(player, target);
        TagTeam newTag = (oldTag == null) ? new TagTeam(target, "", "", priority) : oldTag.clone();
        newTag.setPriority(priority);
        updateTag(player, target, oldTag, newTag);
    }

    private static TagTeam getOrCreateTagTeam(Player player, Player target) {
        return tagData.computeIfAbsent(player, k -> new HashMap<>()).get(target);
    }

    private static void updateTag(Player player, Player target, TagTeam oldTag, TagTeam newTag) {
        Scoreboard scoreboard = getOrCreateScoreboard(player);
        Map<Player, TagTeam> teams = tagData.computeIfAbsent(player, k -> new HashMap<>());

        if (oldTag != null) {
            removeTeam(scoreboard, oldTag);
            teams.remove(target);
        }

        teams.put(target, newTag);
        createOrUpdateTeam(scoreboard, newTag);
    }

    private static Scoreboard getOrCreateScoreboard(Player player) {
        return playerScoreboards.compute(player, (p, oldBoard) -> {
            Scoreboard newBoard = Bukkit.getScoreboardManager().getNewScoreboard();
            if (oldBoard != null) {
                for (Objective oldObjective : oldBoard.getObjectives()) {
                    Objective newObjective = newBoard.registerNewObjective(oldObjective.getName(), oldObjective.getCriteria());
                    newObjective.setDisplaySlot(oldObjective.getDisplaySlot());
                    newObjective.setDisplayName(oldObjective.getDisplayName());

                    for (String entry : oldBoard.getEntries()) {
                        int score = oldBoard.getObjective(oldObjective.getName()).getScore(entry).getScore();
                        newObjective.getScore(entry).setScore(score);
                    }
                }
            }

            p.setScoreboard(newBoard);
            return newBoard;
        });
    }

    private static void createOrUpdateTeam(Scoreboard scoreboard, TagTeam tagTeam) {
        String teamName = formatTeamName(tagTeam.getPriority(), tagTeam.getPlayer().getName());
        Team team = scoreboard.getTeam(teamName);

        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }

        team.setPrefix(tagTeam.getPrefix());
        team.setSuffix(tagTeam.getSuffix());
        team.setNameTagVisibility(NameTagVisibility.ALWAYS);

        if (!team.hasEntry(tagTeam.getPlayer().getName())) {
            team.addEntry(tagTeam.getPlayer().getName());
        }
    }

    private static void removeTeam(Scoreboard scoreboard, TagTeam tagTeam) {
        String teamName = formatTeamName(tagTeam.getPriority(), tagTeam.getPlayer().getName());
        Team team = scoreboard.getTeam(teamName);
        if (team != null) {
            team.unregister();
        }
    }

    private static String formatTeamName(int priority, String playerName) {
        String priorityStr = String.format("%05d", priority);
        String playerStr = playerName.length() > 11 ? playerName.substring(0, 11) : playerName;
        return priorityStr + playerStr;
    }

    public static class TagTeam {
        private final Player player;
        private String prefix;
        private String suffix;
        private int priority;

        public TagTeam(Player player, String prefix, String suffix, int priority) {
            this.player = player;
            this.prefix = prefix;
            this.suffix = suffix;
            this.priority = priority;
        }

        public Player getPlayer() {
            return player;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getSuffix() {
            return suffix;
        }

        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public TagTeam clone() {
            return new TagTeam(this.player, this.prefix, this.suffix, this.priority);
        }
    }
}