package com.example.benjamin.numberrecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


public class MainActivity extends AppCompatActivity {

    Button btntakePicture;
    ImageView picture;
    final String TAG = ".MainActivity";
    private static final int PHOTO_REQUEST = 10;


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

    @Override
    protected void onResume() {
        super.onResume();
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

            Mat Rgba = new Mat();
            Mat grayMat = new Mat();
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inSampleSize = 4;

            int width = imageBitmap.getWidth();
            int heigh = imageBitmap.getHeight();
            Bitmap grayBitmap = Bitmap.createBitmap(width, heigh, Bitmap.Config.RGB_565);

            //bitmap to mat
            Utils.bitmapToMat(imageBitmap, Rgba);
            Imgproc.cvtColor(Rgba, grayMat, Imgproc.COLOR_BGR2GRAY);
            Utils.matToBitmap(grayMat, grayBitmap);



            Imgproc.GaussianBlur(grayMat, grayMat, new Size(5, 5),0.0);
            picture.setImageBitmap(grayBitmap);
        }
    }
}
