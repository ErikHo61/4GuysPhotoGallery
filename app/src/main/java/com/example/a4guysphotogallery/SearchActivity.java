package com.example.a4guysphotogallery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.system.ErrnoException;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.GregorianCalendar;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
    }

    public void search(View view) {
        Intent intent = new Intent(this, MainActivity.class);

        try {
            EditText startDateInput = (EditText) findViewById(R.id.dateSchText);
            String startDate = startDateInput.getText().toString();
            long startDateInMili = convertToUnixTime(startDate);

            EditText endDateInput = (EditText) findViewById(R.id.dateSchText2);
            String endDate = endDateInput.getText().toString();
            long endDateInMili = convertToUnixTime(endDate);

            EditText keywordInput = (EditText) findViewById(R.id.keySchText);
            String keyword = keywordInput.getText().toString();
            Log.d(null, "keyword from search input: " + keyword);

            intent.putExtra("startDate", startDateInMili);
            intent.putExtra("endDate", endDateInMili);
            intent.putExtra("keyword", keyword);

            startActivity(intent);
        } catch (RuntimeException e) {
            Toast.makeText(
                    this,
                    "Invalid input: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
            Log.e(null, e.toString());
        }

    }

    /**
     * Convert the dateString into miliseconds since the epoch
     * @param dateString, must be in format of "DD/MM/YYYY"
     * @return miliseconds since the epoch or 0 if dateString is empty.
     * @throws RuntimeException if dateString is not in the correct format.
     */
    private long convertToUnixTime(String dateString) throws RuntimeException {
        if (dateString.trim().equals("")) return 0;
        String[] dateParts = dateString.split("/");
        final int DAY_INDEX = 0;
        final int MONTH_INDEX = 1;
        final int YEAR_INDEX = 2;

        try {
            GregorianCalendar calendar = new GregorianCalendar(
                    Integer.parseInt(dateParts[YEAR_INDEX]),
                    Integer.parseInt(dateParts[MONTH_INDEX]) - 1,
                    Integer.parseInt(dateParts[DAY_INDEX])
            );
            return calendar.getTimeInMillis();
        } catch (IndexOutOfBoundsException e) {
            throw new RuntimeException("Invalid date string format: " + dateString);
        }

    }

    public void sendBack(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}