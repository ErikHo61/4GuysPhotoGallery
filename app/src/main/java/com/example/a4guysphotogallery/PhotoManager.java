package com.example.a4guysphotogallery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Handles everything related to retrieving photos from storage.
 */
public class PhotoManager extends AppCompatActivity {
    String mostRecentPhoto;
    List<String> photoPaths;
    int curIndex;
    private Context mainActivityContext;

    // keep track of the singleton
    private static PhotoManager manager;

    /**
     * Don't use this, only use for the Android Manifest.
     */
    public PhotoManager(Context mainActivityContext) {
        super();
        this.mainActivityContext = mainActivityContext;
        photoPaths = getPhotos();
    }

    /**
     * Get the PhotoManager instance.
     * @return the PhotoManager.
     */
    public static PhotoManager getManager(Context mainActivityContext) {
        if (manager == null) {
            manager = new PhotoManager(mainActivityContext);
        }
        return manager;
    }

    /**
     * Create an image file in the file system.
     * Taken from https://developer.android.com/training/camera/photobasics#TaskCaptureIntent
     * @return the created image File.
     * @throws IOException
     */
    @SuppressLint("MissingPermission")
    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + "_Default Caption_";
        File storageDir = mainActivityContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File emptyImage = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mostRecentPhoto = emptyImage.getAbsolutePath();
        return emptyImage;
    }

    /**
     * Get all the photos from our storage.
     * @param startDate, the startDate that we are searching for
     * @param endDate, the startDate that we are searching for
     * @param lat, the latitude.
     * @param lng, the longitude.
     * @param keyword, the keyword that we are searching for
     */
    public List<String> getPhotos(Long startDate, Long endDate, Double lat, Double lng, String keyword) {
        File storageDir = mainActivityContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        ArrayList<String> photoPaths = new ArrayList<>();

        if (storageDir != null) {
            File[] files = storageDir.listFiles();
            if (files != null) {
                Arrays.sort(files);
                boolean noFilter = keyword == null && (startDate == null || endDate == null) && lat == null && lng == null;
                for (File file : files) {
                    String path = file.getPath();
                    if (!path.contains(".jpg")) continue;

                    // no filters
                    if (noFilter) {
                        photoPaths.add(path);
                        continue;
                    }

                    // check for three cases:
                    // keyword has filter but no date filter
                    // date has filter but no keyword
                    // both filters are on

                    // filter the params
                    boolean containKeyword = keyword != null && path.contains(keyword);

                    String[] args = file.getName().split("_");
                    boolean validLastModified = startDate != null && endDate != null
                            && startDate < Long.parseLong(args[0]) && endDate < Long.parseLong(args[0]);
                    boolean validLoc = true;
                    if (lat != 0.0 || lng != 0.0){
                        try {
                            ExifInterface exifInterface = new ExifInterface(file);
                            if (lat != 0.0) {
                                validLoc = convertToDegree(exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE)) < lat + 0.1 && convertToDegree(exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE)) > lat - 0.1;
                            }
                            if (lng != 0.0){
                                validLoc = validLoc && convertToDegree(exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)) < lng + 0.1 && convertToDegree(exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)) > lng - 0.1;
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    Log.d(null, "containkeyword" + containKeyword);
                    Log.d(null, "validLastModified" + validLastModified);
                    Log.d(null, "validLoc" + validLoc);
                    if (containKeyword && validLastModified && validLoc){
                        photoPaths.add(path);
                    }
                }
            }
        }

        Toast.makeText(mainActivityContext,
                photoPaths.size() + " photos loaded",
                Toast.LENGTH_SHORT).show();
        return photoPaths;
    }

    /**
     * Get all the photos from our storage.
     */
    public List<String> getPhotos() {
        return getPhotos(null, null, null, null, null);
    }

    /**
     * Convert the input into geographical degrees
     * @param input
     * @return
     */
    private Double convertToDegree(String input){
        String[] strsplit = input.split(",",3);

        String[] strDegrees = strsplit[0].split("/", 2);
        Double d0 = Double.parseDouble(strDegrees[0]);
        Double d1 = Double.parseDouble(strDegrees[1]);
        double dResult = d0/d1;

        String[] strMinutes = strsplit[1].split("/", 2);
        Double m0 = Double.parseDouble(strMinutes[0]);
        Double m1 = Double.parseDouble(strMinutes[1]);
        double mResult = m0/m1;

        String[] strSeconds = strsplit[2].split("/", 2);
        Double s0 = Double.parseDouble(strSeconds[0]);
        Double s1 = Double.parseDouble(strSeconds[1]);
        double sResult = s0/s1;

        return dResult + (mResult/60) + (sResult/3600);
    }

    /**
     * Get the previous photo from our storage.
     * @return the path to the previous photo
     */
    public String getPreviousPhoto() {
        // if there's no previous picture
        if (curIndex <= 0) {
            Toast.makeText(mainActivityContext,
                    "No more pictures",
                    Toast.LENGTH_SHORT).show();
            return mostRecentPhoto;
        }
        return photoPaths.get(--curIndex);
    }

    /**
     * Get the next photo from our storage.
     * @return the path to the next photo
     */
    public String getNextPhoto() {
        // if there's no next picture
        if (curIndex >= photoPaths.size() - 1) {
            Toast.makeText(mainActivityContext,
                    "No more pictures",
                    Toast.LENGTH_SHORT).show();
            return mostRecentPhoto;
        }
        return photoPaths.get(++curIndex);
    }
}
