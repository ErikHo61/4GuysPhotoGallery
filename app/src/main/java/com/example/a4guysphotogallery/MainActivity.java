package com.example.a4guysphotogallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String mostRecentPhoto;
    ImageView imageView;
    List<String> photoPaths;
    int curIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);

        Bundle bundle = getIntent().getExtras();
        Long startDate = null;
        Long endDate = null;
        String keyword = null;

        if (bundle != null) {
            startDate = bundle.getLong("startDate");
            endDate = bundle.getLong("endDate");
            keyword = bundle.getString("keyword");
        }

        photoPaths = getPhotos(startDate, endDate, keyword);
        if (photoPaths.size() > 0) {
            curIndex = photoPaths.size() - 1;
            mostRecentPhoto = photoPaths.get(curIndex);
        } else {
            curIndex = -1;
            mostRecentPhoto = "";
        }
        showPhoto(mostRecentPhoto);
    }

    public void sendSearch (View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    /**
     * Take a picture using the built in camera app.
     * @param view, the view that got clicked on.
     */
    public void takePicture(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photo = null;
            try {
                photo = createImageFile();
            } catch (IOException e) {
                Context context = getApplicationContext();
                CharSequence text = String.format("Cannot access storage: %s", e.getMessage());
                int duration = Toast.LENGTH_LONG;
                Toast.makeText(context, text, duration).show();
            }

            if (photo != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.a4guysphotogallery.fileprovider",
                        photo);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    /**
     * Create an image file in the file system.
     * Taken from https://developer.android.com/training/camera/photobasics#TaskCaptureIntent
     * @return the created image File.
     * @throws IOException
     */
    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mostRecentPhoto = image.getAbsolutePath();
        return image;
    }

    /**
     * Get the picture taken by the camera and display it in the
     * ImageView.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            showPhoto(mostRecentPhoto);
            photoPaths = getPhotos();
        }
    }

    /**
     * Display the photo at photoPath in the ImageView.
     * @param photoPath, the file path to the photo
     */
    private void showPhoto(String photoPath) {
        try {
            Bitmap imageBitmap = BitmapFactory.decodeFile(photoPath);
            imageView.setImageBitmap(imageBitmap);
        } catch (NullPointerException e) {
            Toast.makeText(this,
                    "Image not found: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            Log.e(null, e.toString());
        }
    }

    /**
     * Get all the photos from our storage.
     * @param startDate, the startDate that we are searching for
     * @param endDate, the startDate that we are searching for
     * @param keyword, the keyword that we are searching for
     */
    private List<String> getPhotos(Long startDate, Long endDate, String keyword) {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        ArrayList<String> photoPaths = new ArrayList<>();
        if (storageDir != null ) {
            File[] photos = storageDir.listFiles();

            if (photos != null) {
                for (File photo : photos) {
                    String path = photo.getPath();
                    if (!path.contains(".jpg")) continue;
//                    // filter the params
//                    boolean containKeyword = keyword != null && path.contains(keyword);
//                    boolean validLastModified = startDate != null && endDate != null
//                            && startDate < photo.lastModified() && photo.lastModified() < endDate;
//
//                    if (containKeyword && validLastModified) ;
//                    Log.d(null, "containkeyword" + containKeyword);
//                    Log.d(null, "validLastModified" + validLastModified);
                    photoPaths.add(path);
                }
            }
        }

        return photoPaths;
    }

    /**
     * Get all the photos from our storage.
     */
    private List<String> getPhotos() {
        return getPhotos(null, null, null);
    }

    /**
     * Get the previous photo from our storage.
     * @param view
     */
    public void getPreviousPhoto(View view) {
        // if there's no previous picture
        Log.d(null, "" + curIndex);
        if (curIndex <= 0) {
            Toast.makeText(this,
                    "No more pictures",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String photoPath = photoPaths.get(--curIndex);
        showPhoto(photoPath);
    }

    /**
     * Get the next photo from our storage.
     * @param view
     */
    public void getNextPhoto(View view) {
        // if there's no next picture
        if (curIndex >= photoPaths.size() - 1) {
            Toast.makeText(this,
                    "No more pictures",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String photoPath = photoPaths.get(++curIndex);
        showPhoto(photoPath);
    }

}