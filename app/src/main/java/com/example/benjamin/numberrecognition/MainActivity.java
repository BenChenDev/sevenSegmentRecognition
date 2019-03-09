package com.example.benjamin.numberrecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;


public class MainActivity extends AppCompatActivity {
    Button btntakePicture;
    ImageView picture;
    private static final int PHOTO_REQUEST = 10;
    private Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(OpenCVLoader.initDebug()){
            Toast.makeText(this, "opencv good", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "opencv failed", Toast.LENGTH_SHORT).show();
        }
        btntakePicture = findViewById(R.id.takePictureButton);
        picture = findViewById(R.id.OCRImage);

        btntakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

    }

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, PHOTO_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            picture.setImageBitmap(imageBitmap);
        }
    }
}
