package com.app.miniproject.iiita.visionassistant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.miniproject.iiita.visionassistant.customview.OverlayView;
import com.app.miniproject.iiita.visionassistant.databinding.ActivityCaptureBinding;
import com.app.miniproject.iiita.visionassistant.env.ImageUtils;
import com.app.miniproject.iiita.visionassistant.env.Logger;
import com.app.miniproject.iiita.visionassistant.env.Utils;
import com.app.miniproject.iiita.visionassistant.tflite.Detector;
import com.app.miniproject.iiita.visionassistant.tflite.TFLiteObjectDetectionAPIModel;
import com.app.miniproject.iiita.visionassistant.tracking.MultiBoxTracker;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class CaptureActivity extends AppCompatActivity {

    public static final int TF_OD_API_INPUT_SIZE = 300;
    private static final Float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    private static final String TAG = "MyTag";
    private static final int CAMERA_PERMISSION_CODE = 101;
    private static final int READ_STORAGE_PERMISSION_CODE = 102;
    private static final int WRITE_STORAGE_PERMISSION_CODE = 103;
    private static final Logger LOGGER = new Logger();
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
    private static final String TF_OD_API_LABELS_FILE = "labelmap.txt";
    // Minimum detection confidence to track a detection.
    private static final boolean MAINTAIN_ASPECT = false;
    private final Integer sensorOrientation = 90;
    protected int previewWidth = 0;
    protected int previewHeight = 0;
    ActivityCaptureBinding binding;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private Bitmap sourceBitmap;
    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;
    private MultiBoxTracker tracker;
    private OverlayView trackingOverlay;
    private Bitmap croppedBitmap;
    private Detector detector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = com.app.miniproject.iiita.visionassistant.databinding.ActivityCaptureBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent data = result.getData();
            try {
                assert data != null;
                Bitmap picture = (Bitmap) data.getExtras().get("data");
                sourceBitmap = (Bitmap) data.getExtras().get("data");
                binding.inputImv.setImageBitmap(picture);
            } catch (Exception e) {
                Log.e(TAG, "cameraLauncher's onActivityResult : " + e.getMessage());
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent data = result.getData();
            try {
                assert data != null;
                Bitmap picture = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                sourceBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                binding.inputImv.setImageBitmap(picture);
            } catch (Exception e) {
                Log.e(TAG, "cameraLauncher's onActivityResult : " + e.getMessage());
            }
        });


        binding.choosePictureMb.setOnClickListener(view -> {
            String[] options = {"Camera", "Gallery"};
            AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
            builder.setTitle("Select an Option");
            builder.setItems(options, (dialogInterface, i) -> {
                if (i == 0) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraLauncher.launch(cameraIntent);
                } else {
                    Intent storageIntent = new Intent();
                    storageIntent.setType("image/*");
                    storageIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryLauncher.launch(storageIntent);
                }
            });
            builder.show();
        });


        binding.detectMb.setOnClickListener(view -> {
            croppedBitmap = Utils.processBitmap(sourceBitmap, TF_OD_API_INPUT_SIZE);
            createModel();
            Handler handler = new Handler();

            new Thread(() -> {
                final List<Detector.Recognition> results = detector.recognizeImage(croppedBitmap);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        handleResult(croppedBitmap, results);
                    }
                });
            }).start();
        });
    }

    private void handleResult(Bitmap croppedBitmap, List<Detector.Recognition> results) {
        final Canvas canvas = new Canvas(croppedBitmap);
        final Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);

        String objects = "";

        final List<Detector.Recognition> mappedRecognitions =
                new LinkedList<Detector.Recognition>();

        for (final Detector.Recognition result : results) {
            final RectF location = result.getLocation();
            if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                canvas.drawRect(location, paint);
//                cropToFrameTransform.mapRect(location);
//
//                result.setLocation(location);
//                mappedRecognitions.add(result);
            }
            objects += result.getTitle() + " " + result.getConfidence() + "\n";
        }
//        tracker.trackResults(mappedRecognitions, new Random().nextInt());
//        trackingOverlay.postInvalidate();
        binding.inputImv.setImageBitmap(croppedBitmap);
        binding.resultTv.setText(objects);
    }

    private void createModel() {
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

        int cropSize = TF_OD_API_INPUT_SIZE;

        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            this,
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
            cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing Detector!");
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Detector could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
    }

    private void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission Already Granted.", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, READ_STORAGE_PERMISSION_CODE);
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_STORAGE_PERMISSION_CODE);
            } else {
                Toast.makeText(this, "Read Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == WRITE_STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "All permissions Granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Write Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}