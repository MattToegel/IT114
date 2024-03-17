package Project.Common;

import java.util.ArrayList;
import java.util.List;

public class RoomResultsPayload extends Payload {
    private List<String> rooms = new ArrayList<String>();
    /**
     * Used to limit the returned result set. Added after video recording.
     */
    private int limit = 10;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public RoomResultsPayload() {
        setPayloadType(PayloadType.LIST_ROOMS);
    }

    public List<String> getRooms() {
        return rooms;
    }

    public void setRooms(List<String> rooms) {
        this.rooms = rooms;
    }
}
