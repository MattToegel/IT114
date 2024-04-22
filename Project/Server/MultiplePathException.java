package Project.Server;

import java.util.ArrayList;
import java.util.List;

public class MultiplePathException extends Exception {
    private List<String> options = new ArrayList<String>();

    public MultiplePathException(List<String> opts) {
        this.options = opts;
    }

    public List<String> getOptions() {
        return options;
    }
}
