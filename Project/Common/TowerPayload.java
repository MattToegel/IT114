package Project.Common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
        try {
            // Serialize to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(tower);
            out.flush();
            byte[] bytes = bos.toByteArray();

            // Deserialize from byte array
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream in = new ObjectInputStream(bis);
            this.tower = (Tower) in.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public TowerPayload(int x, int y) {
        super(x, y);
        setPayloadType(PayloadType.TOWER_STATUS);
    }

    public TowerPayload(int x, int y, Tower t) {
        super(x, y);
        setPayloadType(PayloadType.TOWER_STATUS);
        setTower(t);
    }

    @Override
    public String toString() {
        return super.toString() + tower;
    }
}
