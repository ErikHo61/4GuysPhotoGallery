package com.example.a4guysphotogallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String mostRecentPhoto;
    ImageView imageView;
    EditText captionText;
    Button captionBtn;
    List<String> photoPaths;
    int curIndex;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);

        captionText = findViewById(R.id.captionText);
        captionBtn = findViewById(R.id.captionBtn);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Bundle bundle = getIntent().getExtras();
        Long startDate = null;
        Long endDate = null;
        String keyword = null;

        if (bundle != null) {
            Log.d("bundle", "bundle not null");
            startDate = getIntent().getExtras().getLong("EXTRA_START_DATE");
            endDate = getIntent().getExtras().getLong("EXTRA_END_DATE");
            keyword = getIntent().getExtras().getString("EXTRA_KEYWORD");
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

    public void sendSearch(View view) {
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
    @SuppressLint("MissingPermission")
    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + "_Default Caption_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.d("lastloc", location.getLatitude() + ", " + location.getLongitude());
                        } else {
                            Log.d("lastlocfail", "locnull");
                        }
                    }
                });

        // Save a file: path for use with ACTION_VIEW intents
        mostRecentPhoto = image.getAbsolutePath();
        Log.d("filepath", image.getPath());
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
            photoPaths = getPhotos();
            Bitmap imageBitmap = BitmapFactory.decodeFile(photoPath);
            imageView.setImageBitmap(imageBitmap);
            String caption = photoPath.split("_")[2];
            captionText.setText(caption);
            curIndex = photoPaths.indexOf(photoPath);

            ExifInterface exif = new ExifInterface(photoPath);

            String lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);

            String lng = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);

            Log.d("latlng", "lat: " + lat + ", lng: " + lng);
        } catch (NullPointerException e) {
            Toast.makeText(this,
                    "Image not found: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            Log.e(null, e.toString());
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(this,
                    "Image not found: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            Log.e(null, e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Location not found: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
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
            Arrays.sort(photos);
            if (photos != null) {
                for (File photo : photos) {
                    String path = photo.getPath();
                    if (!path.contains(".jpg")) continue;

                    // no filters
                    if (keyword == null && (startDate == null || endDate == null)) {
                        photoPaths.add(path);
                        continue;
                    }

                    // check for three cases:
                    // keyword has filter but no date filter
                    // date has filter but no keyword
                    // both filters are on

                    // filter the params
                    boolean containKeyword = keyword != null && path.contains(keyword);


                    String[] args = photo.getName().split("_");
                    for (String s:
                         args) {
                        Log.d("args",s);
                    }
                    boolean validLastModified = startDate != null && endDate != null
                            && startDate < Long.parseLong(args[0]) && endDate < Long.parseLong(args[0]);

                    Log.d(null, "containkeyword" + containKeyword);
                    Log.d(null, "validLastModified" + validLastModified);
                    if (containKeyword && validLastModified){
                        photoPaths.add(path);
                    }
                }
            }
        }
        Toast.makeText(this,
                photoPaths.size() + " photos loaded",
                Toast.LENGTH_SHORT).show();
        return photoPaths;
    }

    /**
     * Get all the photos from our storage.
     */
    private List<String> getPhotos() {
        return getPhotos(null, null, null);
    }

    public void captionBtnClick(View view){
        if(curIndex == -1){
            Toast.makeText(this,
                    "Cannot assign caption",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        editCaption(photoPaths.get(curIndex), captionText.getText().toString());
    }

    private void editCaption(String path, String caption){
        String[] attr = path.split("_");
        File to = new File(attr[0] + "_" + attr[1] + "_" + caption + "_" + attr[3]);
        File from =  new File(path);
        from.renameTo(to);
        photoPaths = getPhotos();
        showPhoto(photoPaths.get(photoPaths.indexOf(to.getPath())));
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

    /**
     * Share the current photo that's being displayed to Twitter.
     * @param view
     */
    public void sharePhoto(View view) {
        // retrieve the current photo
        ImageView curPhotoView = findViewById(R.id.imageView);
        Bitmap photo = ((BitmapDrawable) curPhotoView.getDrawable()).getBitmap();

        // set up intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        intent.putExtra("image", photo);

    }
}
