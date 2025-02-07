package tech.nitidez.valarlibrary.lib.entity.npc;

import tech.nitidez.valarlibrary.lib.entity.npc.skin.*;

public class NPCTrait {
    private NPCSkinData skin;
    private NPCSkinLayers skinLayers;
    private boolean tablist;

    public NPCTrait() {}

    public NPCSkinData getSkin() {
        return this.skin;
    }

    public NPCSkinLayers getSkinLayers() {
        return this.skinLayers;
    }

    public Byte getSkinLayersByte() {
        if (this.skinLayers != null) {
            return this.skinLayers.getDisplayedParts();
        }
        return 127;
    }

    public boolean isTablist() {
        return this.tablist;
    }

    public void setSkin(NPCSkinData skin) {
        this.skin = skin;
    }

    public void setSkinLayers(NPCSkinLayers skinLayers) {
        this.skinLayers = skinLayers;
    }

    public void setTablist(boolean tablist) {
        this.tablist = tablist;
    }
}
