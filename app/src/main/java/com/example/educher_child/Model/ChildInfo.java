package com.example.educher_child.Model;

public class ChildInfo {

    private String Child_key;
    private String phone;

    public ChildInfo() {
    }

    public ChildInfo(String child_key, String phone) {
        Child_key = child_key;
        this.phone = phone;
    }

    public String getChild_key() {
        return Child_key;
    }

    public void setChild_key(String child_key) {
        Child_key = child_key;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
