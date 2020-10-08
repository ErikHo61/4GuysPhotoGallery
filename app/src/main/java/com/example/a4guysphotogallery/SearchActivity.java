package com.example.a4guysphotogallery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
    }

    public void search(View view) {
        Intent intent = new Intent(this, MainActivity.class);

        EditText startDateInput = (EditText) findViewById(R.id.dateSchText);
        Long startDate = null;
        Log.d("startdateinput", "|" + startDateInput.getText().toString() + "|" + startDateInput.getText().toString().equals(""));
        if (!startDateInput.getText().toString().equals("")){
            startDate = Long.parseLong(startDateInput.getText().toString().replace("/",""));
        }

        EditText endDateInput = (EditText) findViewById(R.id.dateSchText2);
        Long endDate = null;
        if (!endDateInput.getText().toString().equals("")) {
            endDate = Long.parseLong(endDateInput.getText().toString().replace("/",""));
        }

        //Log.d(null, startDate);

        EditText keywordInput = (EditText) findViewById(R.id.keySchText);
        String keyword = keywordInput.getText().toString();

        EditText latInput = (EditText) findViewById(R.id.latText);
        EditText lngInput = (EditText) findViewById(R.id.lngText);
        String lat = latInput.getText().toString();
        String lng = lngInput.getText().toString();

        Bundle extras = new Bundle();
        if(startDate!= null)
            extras.putLong("EXTRA_START_DATE", startDate);
        if(endDate!= null)
            extras.putLong("EXTRA_END_DATE", endDate);
        extras.putString("EXTRA_KEYWORD", keyword);
        if(lat.trim() != null){
            extras.putDouble("EXTRA_LAT", Double.parseDouble(lat));
        }
        //extras.putDouble("EXTRA_LAT", Double.parseDouble(lat));
        if(lng.trim() != null){
            extras.putDouble("EXTRA_LNG", Double.parseDouble(lng));
        }
        //extras.putDouble("EXTRA_LNG", Double.parseDouble(lng));
        intent.putExtras(extras);
        startActivity(intent);
    }

    public void sendBack(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}