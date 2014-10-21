package com.blastbrain.cameradiary.util;

public class ImageAndText {
	private int id;
    private String imageUrl;
    private String text;
    private String date;
 
    public ImageAndText(int id, String imageUrl, String text, String date) {
        this.imageUrl = imageUrl;
        this.text = text;
        this.id = id;
        this.date = date;
    }
    public int getId() {
    	return id;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public String getText() {
        return text;
    }
	public String getDate() {
		return date;
	}
}
