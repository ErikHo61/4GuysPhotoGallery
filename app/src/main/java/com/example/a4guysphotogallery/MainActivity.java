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

import androidx.exifinterface.media.ExifInterface;

import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MainActivityView {
    static final int REQUEST_IMAGE_CAPTURE = 1;

    // track photo related stuff
    //String mostRecentPhoto;
    PhotoBase curPhoto;
    //List<String> photoPaths;
    List<PhotoBase> photos;
    int curIndex;

    // tracks UI
    ImageView imageView;
    EditText captionText;

    TextView latView;
    TextView lngView;

    Button captionBtn;

    // tracks various services
    private PhotoManager photoManager;
    private MainActivityPresenter presenter;
    private MainActivityInteractor interactor;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        captionText = findViewById(R.id.captionText);
        captionBtn = findViewById(R.id.captionBtn);

        latView = findViewById(R.id.latView);
        lngView = findViewById(R.id.lngView);

        interactor = new MainActivityInteractor(this);
        photoManager = PhotoManager.getManager();

        // check for extra data
        Bundle bundle = getIntent().getExtras();
        updatePhotos(bundle);
        presenter = new MainActivityPresenter(interactor);
        presenter.bind(this);
        //showPhoto(mostRecentPhoto);
        showPhoto(curPhoto);
    }

    // update the photos in the photoPaths
    // bundle is a Bundle that contains the extras from another activity
    private void updatePhotos(Bundle bundle) {
        Long startDate = null;
        Long endDate = null;
        String keyword = null;
        Double lat = null;
        Double lng = null;

        // check the extra
        if (bundle != null) {
            startDate = bundle.getLong("EXTRA_START_DATE");
            endDate = bundle.getLong("EXTRA_END_DATE");
            keyword = bundle.getString("EXTRA_KEYWORD");
            lat = bundle.getDouble("EXTRA_LAT");
            lng = bundle.getDouble("EXTRA_LNG");
        }

        //photoPaths = photoManager.getPhotos(startDate, endDate, lat, lng, keyword, this);
        photos = makePhotoList(photoManager.getPhotos(startDate, endDate, lat, lng, keyword, this));
        if (photos.size() > 0) {
            curIndex = photos.size() - 1;
            //mostRecentPhoto = photoPaths.get(curIndex);
            Log.d("curindex before default", "" + curIndex);
            curPhoto = photos.get(curIndex);
            Log.d("current default photo", curPhoto.toString());
        } else {
            curIndex = -1;
            //mostRecentPhoto = "";
        }
    }

    public List<PhotoBase> makePhotoList(List<String> pathList){
        List<PhotoBase> outputList = new ArrayList<PhotoBase>();
        Log.d("fileamount", "" + pathList.size());

        for (String s: pathList) {
            ExifInterface exif;
            try {
                exif = new ExifInterface(s);
                String lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                String lng = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                outputList.add(new LocationDecorator(new PhotoItem(s), lat, lng));
                Log.d("exifListTest", "lat: " + lat + ", lng: " + lng);
            }catch (IOException e) {
                e.printStackTrace();
                outputList.add(new PhotoItem(s));
                Log.d("exifListFail", "Failed, no location");
            }
        }

        return outputList;
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
                photo = photoManager.createImageFile(this);
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
     * Get the picture taken by the camera and display it in the
     * ImageView.
     */
    @SuppressLint("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            updatePhotos(null);
            //showPhoto(mostRecentPhoto);
            showPhoto(curPhoto);
            interactor.picture(this, curPhoto.getPath());
        }
    }
    /*
    /**
     * Display the photo at photoPath in the ImageView.
     * @param photoPath, the file path to the photo
    @SuppressLint("MissingPermission")
    private void showPhoto(String photoPath) {
        try {
            Bitmap imageBitmap = BitmapFactory.decodeFile(photoPath);
            imageView.setImageBitmap(imageBitmap);
            String caption = photoPath.split("_")[2];
            captionText.setText(caption);

        } catch (Exception e) {
            Toast.makeText(this,
                    "Image not found: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            Log.e(null, e.toString());
        }
    }
    */
    private void showPhoto(PhotoBase pb){
        try {
            Bitmap imageBitmap = BitmapFactory.decodeFile(pb.getPath());
            imageView.setImageBitmap(imageBitmap);

            if(pb instanceof LocationDecorator){
                Log.d("locationdecoratortest", Double.toString(PhotoManager.convertToDegree(((LocationDecorator) pb).getLat())));
                latView.setText(Double.toString(PhotoManager.convertToDegree(((LocationDecorator) pb).getLat())));
                lngView.setText(Double.toString(PhotoManager.convertToDegree(((LocationDecorator) pb).getLng())));
            }else{
                latView.setText(Double.toString(0.0));
                lngView.setText(Double.toString(0.0));
            }

            String caption = pb.getPath().split("_")[2];
            captionText.setText(caption);


        } catch (Exception e) {
            //Toast.makeText(this,
                    //"Image not found: " + e.getMessage(),
                    //Toast.LENGTH_LONG).show();
            Log.e(null, e.toString());
        }
    }

    public void captionBtnClick(View view){
        if(curIndex == -1){
            Toast.makeText(this,
                    "Cannot assign caption",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        //String curFilePath = photoPaths.get(curIndex);
        String curFilePath = photos.get(curIndex).getPath();
        editCaption(curFilePath, captionText.getText().toString());
    }

    // edit the caption by renaming the filepath then fetching it from storage again
    private void editCaption(String path, String caption){
        String[] attr = path.split("_");
        File to = new File(attr[0] + "_" + attr[1] + "_" + caption + "_" + attr[3]);
        File from =  new File(path);
        boolean success = from.renameTo(to);
        if (success) {
            // switch out the from photo in the photoPaths
            String photoPath = to.getPath();
            //photoPaths.set(curIndex, photoPath);

            photos.get(curIndex).setPath(photoPath);
            showPhoto(photos.get(curIndex));

        } else {
            Toast.makeText(this,
                    "Cannot change caption. File cannot be renamed",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get the previous photo from our storage.
     * @return the path to the previous photo
     */
    public void getPreviousPhoto(View view) {
        // if there's no previous picture
        if (curIndex <= 0) {
            Toast.makeText(this,
                    "No more pictures",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        //String photoPath = photoPaths.get(--curIndex);
        //showPhoto(photoPath);

        showPhoto(photos.get(--curIndex));
    }

    /**
     * Get the next photo from our storage.
     * @return the path to the next photo
     */
    public void getNextPhoto(View view) {
        // if there's no next picture
        if (curIndex >= photos.size() - 1) {
            Toast.makeText(this,
                    "No more pictures",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        //String photoPath = photoPaths.get(++curIndex);
        //showPhoto(photoPath);

        showPhoto(photos.get(++curIndex));
    }

    /**
     * Share the current photo that's being displayed to Twitter.
     * @param view
     */
    public void sharePhoto(View view) {
        // retrieve the current photo
        Uri photoUri = Uri.parse(photos.get(curIndex).getPath());

        // set up intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_STREAM, photoUri);

        //start chooser
        startActivity(Intent.createChooser(intent, "Share image using"));
    }


}
