package com.example.benjamin.numberrecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorSpace;
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
    final String TAG = "debug";
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
            Mat edgeMat = new Mat();
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

            //detect edge
            Imgproc.Canny(grayMat, edgeMat, 80, 200);
//            Utils.matToBitmap(edgeMat, result);
//            picture.setImageBitmap(result);


            List<MatOfPoint> contours = new ArrayList<>();
            Mat hier = new Mat();
            Imgproc.findContours(edgeMat, contours, hier, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

            //find large contour area
            double maxVal = 0;
            int maxValIdx = 0;
            for (int i = 0; i < contours.size(); i++)
            {
                MatOfPoint2f approx = new MatOfPoint2f();
                MatOfPoint2f contour2f = new MatOfPoint2f( contours.get(i).toArray() );

                double distance = Imgproc.arcLength(contour2f, true)*0.02;
                Imgproc.approxPolyDP(contour2f, approx, distance, true);
                MatOfPoint points = new MatOfPoint( approx.toArray() );
                if(points.toArray().length == 4){
                    Log.d(TAG, "one contour with 4 points found.");
                    double contourArea = Imgproc.contourArea(contours.get(i));
                    if (maxVal < contourArea)
                    {
                        maxVal = contourArea;
                        maxValIdx = i;
                    }

                }

            }

            MatOfPoint2f approx_big = new MatOfPoint2f();
            MatOfPoint2f contour2f_big = new MatOfPoint2f( contours.get(maxValIdx).toArray() );

            double distance_big = Imgproc.arcLength(contour2f_big, true)*0.02;
            Imgproc.approxPolyDP(contour2f_big, approx_big, distance_big, true);
            MatOfPoint points_big = new MatOfPoint( approx_big.toArray() );
//            Rect rect_big = Imgproc.boundingRect(points_big);
//            Imgproc.rectangle(Rgba, new Point(rect_big.x, rect_big.y), new Point(rect_big.x + rect_big.width, rect_big.y + rect_big.height), new Scalar(255, 0, 0, 255), 1);
            int l = points_big.toArray().length;
            Point p1 = points_big.toArray()[0];
            Point p2 = points_big.toArray()[1];
            Point p3 = points_big.toArray()[2];
            Point p4 = points_big.toArray()[3];

            double minx = 1000.0, miny = 1000.0;
            double maxx=0.0, maxy=0.0;
            for(int i = 0; i < 4; i++){
                if(points_big.toArray()[i].x < minx){
                    minx = points_big.toArray()[i].x;
                }
                if(points_big.toArray()[i].y < miny){
                    miny = points_big.toArray()[i].y;
                }
                if(points_big.toArray()[i].x > maxx){
                    maxx = points_big.toArray()[i].x;
                }
                if(points_big.toArray()[i].y > maxy){
                    maxy = points_big.toArray()[i].y;
                }
            }

            Rect roi = new Rect((int)minx, (int)miny, (int)(maxx-minx), (int)(maxy-miny));
            Mat cropped = new Mat(Rgba, roi);

            int cropwidth = (int)(maxx-minx);
            int cropheigh = (int)(maxy-miny);
            Bitmap cropresult = Bitmap.createBitmap(cropwidth, cropheigh, Bitmap.Config.RGB_565);
            Utils.matToBitmap(cropped, cropresult);
            picture.setImageBitmap(cropresult);

//            Utils.matToBitmap(edgeMat, result);
//            picture.setImageBitmap(result);

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
