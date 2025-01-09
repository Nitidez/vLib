package tech.nitidez.valarlibrary.libraries.entity.npc.skin;

public class NPCSkinData {
    private String texture;
    private String signature;

    public NPCSkinData(String texture, String signature) {
        this.texture = texture;
        this.signature = signature;
    }

    public String getTexture() {
        return this.texture;
    }

    public String getSignature() {
        return this.signature;
    }

    public void setSkin(String texture, String signature) {
        this.texture = texture;
        this.signature = signature;
    }
}
