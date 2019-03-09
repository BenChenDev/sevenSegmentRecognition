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
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_NONE;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY_INV;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.findContours;


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
            Mat binMat = new Mat();
            Mat image_dil = new Mat();
            Mat middle = new Mat();
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inSampleSize = 4;

            int width = imageBitmap.getWidth();
            int heigh = imageBitmap.getHeight();
            Bitmap result = Bitmap.createBitmap(width, heigh, Bitmap.Config.RGB_565);

            //bitmap to mat
            Utils.bitmapToMat(imageBitmap, Rgba);
            Imgproc.cvtColor(Rgba, grayMat, Imgproc.COLOR_BGR2GRAY);
            Utils.matToBitmap(grayMat, result);
            Imgproc.GaussianBlur(grayMat, grayMat, new Size(5, 5),0.0);
            Imgproc.Canny(grayMat, grayMat, 80, 200);
            Utils.matToBitmap(grayMat, result);
            picture.setImageBitmap(result);


            Imgproc.threshold(grayMat, binMat, 80, 255, THRESH_BINARY_INV);
//            Utils.matToBitmap(binMat, result);
//            picture.setImageBitmap(result);

            final Size kernelSize = new Size(3, 3);
            Mat element = Imgproc.getStructuringElement(MORPH_RECT, kernelSize);
            Imgproc.dilate(binMat, image_dil, element);
//            Utils.matToBitmap(image_dil, grayBitmap);
//            picture.setImageBitmap(grayBitmap);


            List<MatOfPoint> contours_out = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(image_dil, contours_out, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
            int size = contours_out.size();
            ArrayList<Rect> num_location = new ArrayList<>();


            for(int i = 0; i < size; i++){
//                num_location.add(boundingRect(contours_out.get(i)));



                // Minimum size allowed for consideration
                MatOfPoint2f approxCurve = new MatOfPoint2f();
                MatOfPoint2f contour2f = new MatOfPoint2f( contours_out.get(i).toArray() );
                //Processing on mMOP2f1 which is in type MatOfPoint2f
                double approxDistance = Imgproc.arcLength(contour2f, true)*0.02;
                Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

                //Convert back to MatOfPoint
                MatOfPoint points = new MatOfPoint( approxCurve.toArray() );

                // Get bounding rect of contour
                Rect rect = Imgproc.boundingRect(points);

                Imgproc.rectangle(image_dil, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 0, 0, 255), 3);



            }

//            Utils.matToBitmap(image_dil, grayBitmap);
//            picture.setImageBitmap(grayBitmap);
        }


    }
}
