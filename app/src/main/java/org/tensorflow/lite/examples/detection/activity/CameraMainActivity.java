package org.tensorflow.lite.examples.detection.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import org.tensorflow.lite.examples.detection.helpers.Pair;
import org.tensorflow.lite.examples.detection.R;
import org.tensorflow.lite.examples.detection.customview.OverlayView;
import org.tensorflow.lite.examples.detection.env.ImageUtils;
import org.tensorflow.lite.examples.detection.env.Logger;
import org.tensorflow.lite.examples.detection.env.Utils;
import org.tensorflow.lite.examples.detection.tflite.Classifier;
import org.tensorflow.lite.examples.detection.tflite.YoloV4Classifier;
import org.tensorflow.lite.examples.detection.tracking.MultiBoxTracker;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class CameraMainActivity extends AppCompatActivity {
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.45f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main_s);
        initPermission();
        imageView = findViewById(R.id.imageView);
        CropImage.activity().start(this);
    }

    private static final int CAMERA_PERMISSION = 100;
    private static final Logger LOGGER = new Logger();
    public static final int TF_OD_API_INPUT_SIZE = 416;

    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "yolov4-416-fp32.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/coco.txt";

    // Minimum detection confidence to track a detection.
    private static final boolean MAINTAIN_ASPECT = false;
    private final Integer sensorOrientation = 90;

    private Classifier detector;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;
    private MultiBoxTracker tracker;
    private OverlayView trackingOverlay;
    private static final Vector<Pair<Float,Float>> currentHits = new Vector<Pair<Float,Float>>();
    private static final Vector<Pair<Float,Float>> runners = new Vector<Pair<Float,Float>>();
    protected int previewWidth = 0;
    protected int previewHeight = 0;
    private Bitmap sourceBitmap;
    private Bitmap cropBitmap;
    private static final Float pixelToCM = 22.18f;

    private ImageView imageView;

    private void initBox() {
        previewHeight = TF_OD_API_INPUT_SIZE;
        previewWidth = TF_OD_API_INPUT_SIZE;
        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        tracker = new MultiBoxTracker(this);
        trackingOverlay = findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                canvas -> tracker.draw(canvas));
        tracker.setFrameConfiguration(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, sensorOrientation);

        try {
            detector =
                    YoloV4Classifier.create(
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_IS_QUANTIZED);
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
    }

    private void handleResult(Bitmap bitmap, List<Classifier.Recognition> results) {
        final Canvas canvas = new Canvas(bitmap);
        final Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);

        currentHits.clear();
        runners.clear();

        for (final Classifier.Recognition result : results) {
            final RectF location = result.getLocation();
            if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                Pair<Float,Float> p = new Pair<>(location.centerX(), location.centerY());

                if(219.5 > location.centerX() && location.centerX() > 200.5 &&
                        219.5 > location.centerY() && location.centerY() > 200.5)
                    continue;

                currentHits.add(p);
            }
        }

        float fX = averageX(), fY = averageY();

        if(checkRunner(fX, fY)) { //calc again the NPM if there is a runner
            fX = averageX();
            fY = averageY();
        }

        drawShapes(fX, fY, paint, canvas);

        ProjectActivity.singleRangeResult.setR(fY);
        ProjectActivity.singleRangeResult.setL(fX);
        ProjectActivity.maxDistance = findMaxDistance();
        ProjectActivity.bonus = checkBonus();

        imageView.setImageBitmap(bitmap);
    }

    private int checkBonus() {
        if(runners.size() > 4)
            return 0;
        if(runners.size() == 1 && currentHits.size() == 4)
            return 1;
        if(runners.size() > 1 || currentHits.size() == 4)
            return 2;
        return 0;
    }

    private void drawShapes(float cx, float cy, Paint paint, Canvas canvas) {
        //paint hits
        //paint runners
        //paint NPM
        paint.setColor(Color.GREEN);
        for(Pair<Float,Float> hit : currentHits)
            canvas.drawCircle(hit.getL(),hit.getR(),8, paint);

        paint.setColor(Color.RED);
        for(Pair<Float,Float> hit : runners)
            canvas.drawCircle(hit.getL(),hit.getR(),7, paint);

        if(currentHits.size() >= 4) {
            paint.setColor(Color.BLUE);
            canvas.drawCircle(cx,cy,7, paint); //twice!
            canvas.drawCircle(cx,cy,7, paint);
        }
    }

    private double findMaxDistance() {
        double max = 0, curr;
        Pair<Float,Float> in = new Pair<Float, Float>(0.0f,0.0f);
        Pair<Float,Float> out = new Pair<Float, Float>(0.0f,0.0f);
        for(int i = 0 ; i < currentHits.size() ; i++){
            in = currentHits.get(i);
            for(int j = i+1 ; j < currentHits.size() ; j++){
                out = currentHits.get(j);
                curr = Math.sqrt((in.getL() - out.getL()) * (in.getL() - out.getL()) + (in.getR() - out.getR()) * (in.getR() - out.getR())); //distance formula
                curr/=pixelToCM;
                if(curr > max)
                    max = curr;
            }
        }
        return max;
    }

    private boolean checkRunner(float fX, float fY) {
        double dis;
        boolean foundRunner = false;
        Vector<Pair<Float, Float>> res = new Vector<>();
        for(Pair<Float,Float> hit : currentHits){
            dis = Math.sqrt((hit.getR() - fY) * (hit.getR() - fY) + (hit.getL() - fX) * (hit.getL() - fX)); //distance formula
            dis/=pixelToCM;
            if(dis > 7) {
                foundRunner = true;
                runners.add(hit);
            }
            else
                res.add(hit);
        }
        if (foundRunner){
            currentHits.clear();
            currentHits.addAll(res);
        }
        return foundRunner;
    }

    private void initPermission() {
        if(ContextCompat.checkSelfPermission(CameraMainActivity.this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(CameraMainActivity.this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_PERMISSION){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                CropImage.activity().start(this);
            else
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == Activity.RESULT_OK && result != null) {
                    String currentPhotoPath = result.getUri().getPath();
                    this.sourceBitmap = BitmapFactory.decodeFile(currentPhotoPath);
                    startDetect();
            }
        }
    }

    public void returnTo(View view)
    {
        //finish();
    }

    private void startDetect(){
        this.cropBitmap = Utils.processBitmap(sourceBitmap, TF_OD_API_INPUT_SIZE);
        this.imageView.setImageBitmap(cropBitmap);
        initBox();
        Handler handler = new Handler();

        new Thread(() -> {
            final List<Classifier.Recognition> results = detector.recognizeImage(cropBitmap);
            handler.post(() -> handleResult(cropBitmap, results));
        }).start();
    }

    private float averageX() {
        if(currentHits.size() < 4)
            return 0;
        float sum = 0.0f;
        for (int i = 0; i < currentHits.size() ; ++i)
            sum += currentHits.get(i).getL();
        return sum/ currentHits.size();
    }

    private float averageY() {
        if(currentHits.size() < 4)
            return 0;
        float sum = 0.0f;
        for (int i = 0; i < currentHits.size() ; ++i)
            sum += currentHits.get(i).getR();
        return sum/currentHits.size();
    }

}
