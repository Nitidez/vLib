package tech.nitidez.valarlibrary.commands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import tech.nitidez.valarlibrary.libraries.entity.FakeEntity;
import tech.nitidez.valarlibrary.libraries.entity.giantitem.GiantItem;
import tech.nitidez.valarlibrary.libraries.entity.npc.NPC;
import tech.nitidez.valarlibrary.libraries.entity.npc.skin.NPCSkinData;
import tech.nitidez.valarlibrary.libraries.hologram.Hologram;
//import tech.nitidez.valarlibrary.libraries.tablist.TagLib;

public class DebugCommand extends Commands {

    public DebugCommand() {
        super("vdebug", "valardebug");
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        Player plr = ((Player) sender);
        switch (Integer.parseInt(args[0])) {
        case 1:
            NPC newNPC = new NPC(plr.getLocation());
            newNPC.getTrait().setSkin(new NPCSkinData("ewogICJ0aW1lc3RhbXAiIDogMTczMTE2NjY0MTIzMiwKICAicHJvZmlsZUlkIiA6ICJjY2MxNGM2ZDUwMDE0MjBmYmMxYjkyMTM2Y2JmOWU4MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJab25lX1gwODE1IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzJlZmI1M2ZlNzJhODM0YTM1MDdlNzEwYTVjNWVkOTVkODFlMzIxNzI2YzBmOWFiYmRkNGE4ZWQyNzA3ZmQwZjgiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==", 
            "GyeMe2p1TDpEfFJ12c+619C89w2KYQE7LFa6Y9jsHYU4usDemMHNDZKpGsnFaTmMW5CCXX1ar2S0eK9PHkdl4VfH50yjmpOjzSKiXUxkTayzBRtfWWQKZV0QGJgWa1TP+zLRYJUH4uVaphbpi0vkgNFykccH8EZl1GTtvrS/TF/CQen410S49uno6wQIntENVqOAYD1GZWjLDk1PHedkycuyfeBoOY0ZYcNLbQaGrFRSiYByu8THOK91Wcjn8oOLcWqzC5whlrsKz8TQtnEWXh65BqCk8WKn+xC9aPozbX5zLp4529PObd3Sd2dF5r0srcijTAvxuB8NcSceOBTNgMoxOFDcbUCkGTYODYLLHgMdQgR8HmS4kVvQSGnUPuKQhUITMtyg28URYXlrCUqF5OWK4aK66IJ60ps2CyomvMBrpWEJyrFXZ1V+HUWDTYFikaqp9cxvM2QIYYISHNpqlkJwRi0UVfTBRU1FJ7ULs4vli5paTEMcfk4FKD5J7fBWLRm2iUs5PLIcAKK/w/PGx1m04RjenMTwgtleZhfHmoMFAUSm5uRCB1Shd8iyQYfuDWzHBk1HVVxQJj3uz88bZc1y67Ul/M1T32jfx+rduI+VejC2xvj06OXE14u+Th8ofXiAXbrJQhHiy0CmXCs8TnzGMAeu9m6hK3EXWNJE7yc="));
            newNPC.spawn(plr);
            break;
        case 2:
            GiantItem gi = new GiantItem(new ItemStack(Material.DIAMOND_CHESTPLATE), plr.getLocation(), false);
            gi.spawn(plr);
            plr.sendMessage("Pos: "+gi.getItemLocation());
            break;
        case 3:
            FakeEntity entity = new FakeEntity(EntityType.CHICKEN, plr.getLocation());
            entity.spawn(plr);
            break;
        case 4:
            GiantItem gi2 = new GiantItem(new ItemStack(Material.DIAMOND_SWORD), plr.getLocation(), true);
            gi2.spawn(plr);
            break;
        case 5:
            Hologram holo = new Hologram(plr.getLocation(), "Este é apenas\num holograma de\nteste :D\nPalavra 1\nPalavra 2\nPalavra 3");
            holo.spawn(plr);
            break;
        case 6:
            Hologram holo2 = Hologram.getHolograms().stream().findFirst().orElse(null);
            holo2.setText("Holograma\ntotalmente\nalterado!");
            holo2.setTouchable(true);
            holo2.setLocation(holo2.getLocation().clone().add(4, 4, 4));
            break;
        case 7:
            //TagLib.setPrefix(plr, "TestPrefix");
            break;
        case 8:
            //TagLib.setSuffix(plr, "TestSuffix");
            break;
        case 9:
            //TagLib.setPriority(plr, 2);
        case 10:
            //TagLib.setPriority(plr, 3);
        default:
            sender.sendMessage("nothing");
            break;

        }
    }
    
}
