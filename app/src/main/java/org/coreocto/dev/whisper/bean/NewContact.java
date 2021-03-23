package org.coreocto.dev.whisper.bean;

/**
 * Created by John on 3/20/2018.
 */

public class NewContact {
    private String email;
    private String recipient;
    private long createDt;

    public NewContact() {
    }

    public NewContact(String email, String recipient, long createDt) {
        this.email = email;
        this.recipient = recipient;
        this.createDt = createDt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public long getCreateDt() {
        return createDt;
    }

    public void setCreateDt(long createDt) {
        this.createDt = createDt;
    }
}
