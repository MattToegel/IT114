package HNS.common;

import java.io.Serializable;

public class GameOptions implements Serializable {
    private int seeksPerRound = 1;
    private boolean isEliminationMode = false;
    private int blockage = 60;

    public int getSeeksPerRound() {
        return seeksPerRound;
    }

    public void setSeeksPerRound(int seeksPerRound) {
        if (seeksPerRound < 1) {
            seeksPerRound = 1;
        } else if (seeksPerRound > 10) {
            seeksPerRound = 10;
        }
        this.seeksPerRound = seeksPerRound;
    }

    public boolean isEliminationMode() {
        return isEliminationMode;
    }

    public void setEliminationMode(boolean isEliminationMode) {
        this.isEliminationMode = isEliminationMode;
    }

    public int getBlockage() {
        return blockage;
    }

    public void setBlockage(int blockage) {
        if (blockage < 0) {
            blockage = 0;
        } else if (blockage > 95) {
            blockage = 95;
        }
        this.blockage = blockage;
    }

    public void reset() {
        blockage = 60;
        seeksPerRound = 1;
        isEliminationMode = false;
    }
}
