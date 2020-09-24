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
        String startDate = startDateInput.getText().toString();

        EditText endDateInput = (EditText) findViewById(R.id.dateSchText2);
        String endDate = startDateInput.getText().toString();

        Log.d(null, startDate);

        EditText keywordInput = (EditText) findViewById(R.id.keySchText);
        String keyword = keywordInput.getText().toString();

        startActivity(intent);
    }

    public void sendBack(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}