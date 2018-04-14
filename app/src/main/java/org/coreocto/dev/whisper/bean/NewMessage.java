package org.coreocto.dev.whisper.bean;

import java.util.Objects;

/**
 * Created by John on 3/20/2018.
 */

public class NewMessage {
    private String from;
    private String to;
    private String content;
    private long createDt;
    private int status;
    public NewMessage(String from, String to, String content, long createDt, int status) {
        this.from = from;
        this.to = to;
        this.content = content;
        this.createDt = createDt;
        this.status = status;
    }
    public NewMessage() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewMessage that = (NewMessage) o;
        return createDt == that.createDt &&
                Objects.equals(from, that.from) &&
                Objects.equals(to, that.to) &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {

        return Objects.hash(from, to, content, createDt);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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
