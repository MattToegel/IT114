package Project.Common;

import java.util.ArrayList;
import java.util.List;

public class TowerPayload extends XYPayload {
    private Tower tower;
    private List<Long> towerIds = new ArrayList<>();
    public Tower getTower() {
        return tower;
    }

    public List<Long> getTowerIds() {
        return towerIds;
    }

    public void setTowerIds(List<Long> towerIds) {
        this.towerIds = towerIds;
    }

    public void setTower(Tower tower) {
        this.tower = tower;
    }

    public TowerPayload(int x, int y) {
        super(x, y);
        setPayloadType(PayloadType.TOWER_STATUS);
    }

    public TowerPayload(int x, int y, Tower t) {
        super(x, y);
        setPayloadType(PayloadType.TOWER_STATUS);
        this.tower = t;
    }
    @Override
    public String toString(){
        return super.toString() + tower;
    }
}
