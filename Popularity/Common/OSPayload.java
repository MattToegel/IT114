package Popularity.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Occupied Status Payload
 */
public class OSPayload extends Payload {
    List<OccupiedStatus> occupiedStatuses = new ArrayList<OccupiedStatus>();

    public OSPayload(List<OccupiedStatus> oss) {
        occupiedStatuses = oss.stream().filter(os -> os.getCount() > 0).collect(Collectors.toList());
        setPayloadType(PayloadType.OCCUPIED_STATUS);
    }

    /**
     * @return the occupiedStatuses
     */
    public List<OccupiedStatus> getOccupiedStatuses() {
        return occupiedStatuses;
    }

    /**
     * @param occupiedStatuses the occupiedStatuses to set
     */
    public void setOccupiedStatuses(List<OccupiedStatus> oss) {
        occupiedStatuses = oss.stream().filter(os -> os.getCount() > 0).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return super.toString() + String.join(",", occupiedStatuses.stream().filter(os -> os.equals(os))
                .map(os -> os.toString()).collect(Collectors.toList()));
    }
}
