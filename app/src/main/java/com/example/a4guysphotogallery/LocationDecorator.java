package com.example.a4guysphotogallery;

public class LocationDecorator extends PhotoDecorator {
    String lat;
    String lng;
    public LocationDecorator(PhotoBase thisPhoto, String lat, String lng) {
        super(thisPhoto);
        this.lat = lat;
        this.lng = lng;
    }

    public String getPath(){
        return thisPhoto.getPath();
    }

    @Override
    public void setPath(String input){
        thisPhoto.setPath(input);
    }

    @Override
    public String getPhotoType(){
        return thisPhoto.getPhotoType() + " with location";
    }
    public String getLat(){
        return lat;
    }

    public String getLng(){
        return lng;
    }
}
