package upm.lssp;

import java.util.Date;

public class Message {
    private String sender;
    private String receiver;
    private Date time;
    private String text;

    public Message(String sender, String receiver, Date time, String text) {
        this.sender = sender;
        this.receiver = receiver;
        this.time = time;
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public Date getTime() {
        return time;
    }

    public String getText() {
        return text;
    }
}
