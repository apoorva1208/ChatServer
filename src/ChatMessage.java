import java.io.Serializable;

/**
 * @author Apoorva Gupta, gupta481@purdue.edu
\ */

final class ChatMessage implements Serializable {

    private static final long serialVersionUID = 6898543889087L;

    private String message;
    private int type;

    public ChatMessage(String message, int type) {
        this.message = message;
        this.type = type;
    }

    public ChatMessage() {
        this.message = "";
        this.type = 0;
    }

    public int getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
