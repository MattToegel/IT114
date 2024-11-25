package HotPot.Common;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardPayload extends Payload{
    private List<ScoreboardRecord> records = new ArrayList<ScoreboardRecord>();

    public ScoreboardPayload(){
        setPayloadType(PayloadType.SCOREBOARD);
    }
    public List<ScoreboardRecord> getRecords() {
        return records;
    }

    public void setRecords(List<ScoreboardRecord> records) {
        this.records = records;
    }
}
