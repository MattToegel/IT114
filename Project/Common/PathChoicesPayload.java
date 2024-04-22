package Project.Common;

import java.util.ArrayList;
import java.util.List;

public class PathChoicesPayload extends Payload {
    private List<String> choices = new ArrayList<String>();

    public PathChoicesPayload() {
        setPayloadType(PayloadType.CHOICES);
    }

    public List<String> getChoices() {
        return choices;
    }

    public void setChoices(List<String> choices) {
        this.choices = choices;
    }
}
