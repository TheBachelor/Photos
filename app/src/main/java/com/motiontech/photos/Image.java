package com.motiontech.photos;

/**
 * Created by manengamungandi on 2017/08/21.
 */

public class Image extends Object {

    private String url;
    private String score;
    private String name;

    public Image(String url) {
        this.setUrl(url);
        this.setScore("");
        this.setName("");
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
