package com.example.educher_child.Model;

public class AppInfo {
    private int id;
    private String name;
    private String packageName;
    private Boolean isLocked;

    public AppInfo() {
    }

    public AppInfo(int id, String name, String packageName, Boolean isLocked) {
        this.id = id;
        this.name = name;
        this.packageName = packageName;
        this.isLocked = isLocked;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Boolean getLocked() {
        return isLocked;
    }

    public void setLocked(Boolean locked) {
        isLocked = locked;
    }
}
