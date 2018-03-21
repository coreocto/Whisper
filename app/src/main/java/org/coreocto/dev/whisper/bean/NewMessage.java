package org.coreocto.dev.whisper.bean;

/**
 * Created by John on 3/20/2018.
 */

public class NewMessage {
    private String from;
    private String to;
    private String content;
    private long createDt;

    public NewMessage(String from, String to, String content, long createDt) {
        this.from = from;
        this.to = to;
        this.content = content;
        this.createDt = createDt;
    }

    public NewMessage() {
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreateDt() {
        return createDt;
    }

    public void setCreateDt(long createDt) {
        this.createDt = createDt;
    }

}
