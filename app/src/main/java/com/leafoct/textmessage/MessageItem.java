package com.leafoct.textmessage;

public class MessageItem {
    private int image_id;
    private String content;

    public MessageItem(int i, String c) {
        image_id = i;
        content = c;
    }

    public int getImage_id() {
        return image_id;
    }

    public void setImage_id(int image_id) {
        this.image_id = image_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
