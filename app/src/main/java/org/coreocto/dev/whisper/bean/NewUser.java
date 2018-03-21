package org.coreocto.dev.whisper.bean;

/**
 * Created by John on 3/20/2018.
 */

public class NewUser {
    private String email;
    private long createDt;
    private String token;

    public NewUser(String email, long createDt, String token) {
        this.email = email;
        this.createDt = createDt;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getCreateDt() {
        return createDt;
    }

    public void setCreateDt(long createDt) {
        this.createDt = createDt;
    }
}
