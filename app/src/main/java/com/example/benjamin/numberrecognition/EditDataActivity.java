package com.example.benjamin.numberrecognition;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditDataActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    final String TAG = "editPage";
    private String number;
    private String unit;
    EditText dataEditText;
    Spinner unit_spinner;
    Button saveButton;

    String[] unit_array;
    FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_date);

        //get unit array
        unit_array = getResources().getStringArray(R.array.units_arrays);

        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Records");


        dataEditText = findViewById(R.id.dataEditText);

        //get data intent
        Intent intent = getIntent();
        if(intent != null){
            number = intent.getExtras().getString("Data");
            Log.d(TAG, "Data got from camera view: " + number);
            if(number != null && !number.isEmpty()){
                dataEditText.setText(number);
            }
        }

        //setup spinner
        unit_spinner = findViewById(R.id.unit_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(EditDataActivity.this,
                R.array.units_arrays, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unit_spinner.setAdapter(adapter);

        //item select listener
        unit_spinner.setOnItemSelectedListener(this);

        //Save button
        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = dataEditText.getText().toString();
                //get current date time
                Date date = new Date();
                String dateTimeFormat = "hh:mm a dd-MM-yyyy";
                DateFormat dateFormat = new SimpleDateFormat(dateTimeFormat);
                String formattedDate = dateFormat.format(date);
                Log.d(TAG, "Current date time: " + formattedDate);


                DatabaseReference uploadData = database.getReference().child("Records");
                String id = uploadData.push().getKey();
                uploadData.child(id).child("Date").setValue(formattedDate);
                uploadData.child(id).child("Value").setValue(data);
                uploadData.child(id).child("Unit").setValue(unit);

            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "position selected: " + position);
        parent.getItemAtPosition(position);
        unit = unit_array[position];
        Log.d(TAG, "Selected unit: " + unit);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d(TAG, "Nothing selected");
    }
}
