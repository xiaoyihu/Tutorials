package net.yazilimsal.codelabs.jsf.bean;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

@ManagedBean
@RequestScoped
public class LoginBean {

    private String username;

    private String password;

    public String doLogin() throws IllegalAccessException {
        if ("admin".equals(username) && "123".equals(password)) {
            return "home?faces-redirect=true";
        } else {
            throw new RuntimeException("Username and/or password invalid!");
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}