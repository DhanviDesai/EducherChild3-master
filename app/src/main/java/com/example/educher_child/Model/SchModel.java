package com.example.educher_child.Model;

public class SchModel {

    String id,pkg;

    public SchModel(String id, String pkg) {
        this.id = id;
        this.pkg = pkg;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }
}
