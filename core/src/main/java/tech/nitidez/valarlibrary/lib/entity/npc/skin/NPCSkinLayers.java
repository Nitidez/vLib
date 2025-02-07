package tech.nitidez.valarlibrary.lib.entity.npc.skin;

public class NPCSkinLayers {
    private boolean cape;
    private boolean jacket;
    private boolean leftSleeve;
    private boolean rightSleeve;
    private boolean leftLeg;
    private boolean rightLeg;
    private boolean hat;

    public NPCSkinLayers() {
        this.cape = true;
        this.jacket = true;
        this.leftSleeve = true;
        this.rightSleeve = true;
        this.leftLeg = true;
        this.rightLeg = true;
        this.hat = true;
    }

    public Byte getDisplayedParts() {
        byte bitSize = 1;
        byte result = 0;
        boolean[] bools = getBoolArray();
        for (int i = 0; i < bools.length; i++) {
            if (bools[i]) result += bitSize;
                    bitSize *= 2;
        }
        return result;
    }

    public boolean[] getBoolArray() {
        return new boolean[] {cape, jacket, leftSleeve, rightSleeve, leftLeg, rightLeg, hat};
    }
}
