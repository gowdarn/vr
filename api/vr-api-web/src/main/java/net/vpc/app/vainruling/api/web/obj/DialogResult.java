/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.api.web.obj;

/**
 *
 * @author vpc
 */
public class DialogResult {
    private Object value;
    private String userInfo;

    public DialogResult() {
    }

    public DialogResult(Object value, String userInfo) {
        this.value = value;
        this.userInfo = userInfo;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }
    
}
