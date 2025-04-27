package com.iam1xd.gfx;

public class items {
    private String url;
    private String name;
    public items() {}

    public items (String name, String url) {
        this.name = name;
        this.url = url;
    }
    public String getUrl() {
        return url;
    }
    public String getName() {
        return name;
    }
}
