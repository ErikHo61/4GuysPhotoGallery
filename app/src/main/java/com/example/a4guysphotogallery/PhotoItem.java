package com.example.a4guysphotogallery;

public class PhotoItem implements PhotoBase{
    String path;

    public PhotoItem(String pathname){
        path = pathname;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String input) {
        path = input;
    }

    public String getPhotoType() {
        return "Photo";
    }

}
