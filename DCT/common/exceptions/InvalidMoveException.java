package DCT.common.exceptions;

import java.util.ArrayList;
import java.util.List;

public class InvalidMoveException extends Exception{
    private List<String> messages = new ArrayList<String>();
    public InvalidMoveException(String message){
        super(message);
    }
    public InvalidMoveException(List<String> messages){
        super(String.join(",", messages));
        this.messages = messages;
    }
    public List<String> getMessages(){
        return messages;
    }
}
