package HNS.common;

/**
 * Created so we don't need to modify the base payload
 * to have an unused array 99% of the time
 */
public class RoomResultPayload extends Payload {
    private String[] rooms;

    public RoomResultPayload() {
        super();
        setPayloadType(PayloadType.GET_ROOMS);
    }

    /**
     * @return the rooms
     */
    public String[] getRooms() {
        return rooms;
    }

    /**
     * @param rooms the rooms to set
     */
    public void setRooms(String[] rooms) {
        this.rooms = rooms;
    }
}
