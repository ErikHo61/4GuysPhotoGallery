package com.example.a4guysphotogallery;

public abstract class PhotoDecorator implements PhotoBase{
    protected PhotoBase thisPhoto;

    public PhotoDecorator(PhotoBase thisPhoto){
        this.thisPhoto = thisPhoto;
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public String getPhotoType() {
        return thisPhoto.getPhotoType();
    }
}
