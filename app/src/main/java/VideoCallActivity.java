package com.example.swapapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.swapapp.database.DatabaseHelper;

import java.util.Collections;

public class VideoCallActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 101;

    private TextureView textureView;

    private TextView tvCallStatus;
    private TextView tvRoomName;

    private Button btnMute;
    private Button btnSwitchCamera;
    private Button btnEndCall;

    private LinearLayout layoutRating;
    private RatingBar ratingBar;
    private Button btnSubmitRating;

    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;

    private HandlerThread backgroundThread;
    private Handler backgroundHandler;

    private boolean usingFrontCamera = true;
    private boolean sessionEnded = false;

    private String roomName;

    private int transactionId;
    private int providerId;
    private int requesterId;
    private int currentUserId;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_call);

        // ==============================
        // CONNECT XML
        // ==============================

        textureView = findViewById(R.id.textureView);

        tvCallStatus = findViewById(R.id.tvCallStatus);
        tvRoomName = findViewById(R.id.tvRoomName);

        btnMute = findViewById(R.id.btnMute);
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera);
        btnEndCall = findViewById(R.id.btnEndCall);

        layoutRating = findViewById(R.id.layoutVideoRating);
        ratingBar = findViewById(R.id.videoRatingBar);
        btnSubmitRating = findViewById(R.id.btnSubmitVideoRating);

        // ==============================
        // DATABASE
        // ==============================

        dbHelper = new DatabaseHelper(this);

        // ==============================
        // CURRENT USER
        // ==============================

        SharedPreferences prefs =
                getSharedPreferences(
                        "SwapAppPrefs",
                        Context.MODE_PRIVATE
                );

        currentUserId =
                prefs.getInt(
                        "CURRENT_USER_ID",
                        -1
                );

        // ==============================
        // GET SESSION DATA
        // ==============================

        Intent intent = getIntent();

        roomName =
                intent.getStringExtra("ROOM_NAME");

        transactionId =
                intent.getIntExtra(
                        "TRANSACTION_ID",
                        -1
                );

        providerId =
                intent.getIntExtra(
                        "PROVIDER_ID",
                        -1
                );

        requesterId =
                intent.getIntExtra(
                        "REQUESTER_ID",
                        -1
                );


        if (roomName == null) {
            roomName = "Swap Live Session";
        }

        tvRoomName.setText(roomName);

        tvCallStatus.setText(
                "Live Session"
        );


        cameraManager =
                (CameraManager)
                        getSystemService(
                                CAMERA_SERVICE
                        );


        // Rating hidden while call running
        layoutRating.setVisibility(
                View.GONE
        );


        // ==============================
        // CAMERA PREVIEW
        // ==============================

        textureView.setSurfaceTextureListener(
                textureListener
        );


        // ==============================
        // SWITCH CAMERA
        // ==============================

        btnSwitchCamera.setOnClickListener(v -> {

            if (sessionEnded) {
                return;
            }

            usingFrontCamera =
                    !usingFrontCamera;

            closeCamera();

            openCamera();
        });


        // ==============================
        // MUTE
        // ==============================

        btnMute.setOnClickListener(v -> {

            if (sessionEnded) {
                return;
            }

            if ("Mute".contentEquals(
                    btnMute.getText())) {

                btnMute.setText(
                        "Unmute"
                );

                Toast.makeText(
                        this,
                        "Microphone muted",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                btnMute.setText(
                        "Mute"
                );

                Toast.makeText(
                        this,
                        "Microphone active",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });


        // ==============================
        // END SESSION
        // ==============================

        btnEndCall.setOnClickListener(v -> {

            endLiveSession();
        });


        // ==============================
        // SUBMIT RATING
        // ==============================

        btnSubmitRating.setOnClickListener(v -> {

            submitRating();
        });
    }


    // =====================================================
    // END LIVE SESSION
    // =====================================================

    private void endLiveSession() {

        if (sessionEnded) {
            return;
        }

        sessionEnded = true;

        // Stop camera immediately
        closeCamera();

        // Hide camera controls
        textureView.setVisibility(
                View.GONE
        );

        btnMute.setVisibility(
                View.GONE
        );

        btnSwitchCamera.setVisibility(
                View.GONE
        );

        btnEndCall.setVisibility(
                View.GONE
        );

        tvCallStatus.setText(
                "Session Ended"
        );


        // =========================================
        // REQUESTER / LEARNER MUST RATE
        // =========================================

        if (currentUserId == requesterId) {

            layoutRating.setVisibility(
                    View.VISIBLE
            );

            Toast.makeText(
                    this,
                    "Please rate your session",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            /*
             * Provider does not rate themselves.
             * Provider returns to Sessions.
             */

            Toast.makeText(
                    this,
                    "Session ended. Waiting for learner rating.",
                    Toast.LENGTH_LONG
            ).show();

            returnToSessions();
        }
    }


    // =====================================================
    // SUBMIT RATING
    // =====================================================

    private void submitRating() {

        int stars =
                Math.round(
                        ratingBar.getRating()
                );


        if (stars < 1 ||
                stars > 5) {

            Toast.makeText(
                    this,
                    "Please select 1 to 5 stars",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        if (transactionId == -1 ||
                providerId == -1) {

            Toast.makeText(
                    this,
                    "Session information missing",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        btnSubmitRating.setEnabled(
                false
        );


        boolean success =
                dbHelper
                        .completeSessionTransaction(
                                transactionId,
                                providerId,
                                stars
                        );


        if (success) {

            Toast.makeText(
                    this,
                    stars +
                            " star rating submitted",
                    Toast.LENGTH_LONG
            ).show();


            /*
             * DatabaseHelper now:
             *
             * status = Completed
             * rating = stars
             * rated_user_id = provider
             * provider receives credits
             */

            returnToSessions();

        } else {

            btnSubmitRating.setEnabled(
                    true
            );

            Toast.makeText(
                    this,
                    "Could not complete session",
                    Toast.LENGTH_LONG
            ).show();
        }
    }


    // =====================================================
    // RETURN TO SESSION SCREEN
    // =====================================================

    private void returnToSessions() {

        Intent intent =
                new Intent(
                        VideoCallActivity.this,
                        SessionsActivity.class
                );

        /*
         * Do not create lots of duplicate
         * SessionsActivity screens.
         */

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(intent);

        finish();
    }


    // =====================================================
    // CAMERA PERMISSION
    // =====================================================

    private void checkCameraPermission() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.CAMERA
                    },
                    CAMERA_PERMISSION_REQUEST
            );

        } else {

            openCamera();
        }
    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );


        if (requestCode ==
                CAMERA_PERMISSION_REQUEST) {

            if (grantResults.length > 0 &&
                    grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED) {

                openCamera();

            } else {

                Toast.makeText(
                        this,
                        "Camera permission required",
                        Toast.LENGTH_LONG
                ).show();

                finish();
            }
        }
    }


    // =====================================================
    // TEXTURE LISTENER
    // =====================================================

    private final TextureView.SurfaceTextureListener
            textureListener =
            new TextureView.SurfaceTextureListener() {

                @Override
                public void onSurfaceTextureAvailable(
                        @NonNull SurfaceTexture surface,
                        int width,
                        int height) {

                    if (!sessionEnded) {
                        checkCameraPermission();
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(
                        @NonNull SurfaceTexture surface,
                        int width,
                        int height) {
                }

                @Override
                public boolean onSurfaceTextureDestroyed(
                        @NonNull SurfaceTexture surface) {

                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(
                        @NonNull SurfaceTexture surface) {
                }
            };


    // =====================================================
    // FIND CAMERA
    // =====================================================

    private String findCameraId() {

        try {

            String fallbackCamera =
                    null;


            for (String cameraId :
                    cameraManager
                            .getCameraIdList()) {


                if (fallbackCamera == null) {

                    fallbackCamera =
                            cameraId;
                }


                CameraCharacteristics characteristics =
                        cameraManager
                                .getCameraCharacteristics(
                                        cameraId
                                );


                Integer facing =
                        characteristics.get(
                                CameraCharacteristics
                                        .LENS_FACING
                        );


                if (facing == null) {
                    continue;
                }


                if (usingFrontCamera &&
                        facing ==
                                CameraCharacteristics
                                        .LENS_FACING_FRONT) {

                    return cameraId;
                }


                if (!usingFrontCamera &&
                        facing ==
                                CameraCharacteristics
                                        .LENS_FACING_BACK) {

                    return cameraId;
                }
            }


            return fallbackCamera;


        } catch (CameraAccessException e) {

            e.printStackTrace();

            return null;
        }
    }


    // =====================================================
    // OPEN CAMERA
    // =====================================================

    private void openCamera() {

        if (sessionEnded) {
            return;
        }


        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED) {

            return;
        }


        String cameraId =
                findCameraId();


        if (cameraId == null) {

            Toast.makeText(
                    this,
                    "No camera available",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        try {

            cameraManager.openCamera(
                    cameraId,
                    stateCallback,
                    backgroundHandler
            );

        } catch (CameraAccessException e) {

            e.printStackTrace();
        }
    }


    // =====================================================
    // CAMERA CALLBACK
    // =====================================================

    private final CameraDevice.StateCallback
            stateCallback =
            new CameraDevice.StateCallback() {

                @Override
                public void onOpened(
                        @NonNull CameraDevice camera) {

                    cameraDevice =
                            camera;

                    createCameraPreview();
                }


                @Override
                public void onDisconnected(
                        @NonNull CameraDevice camera) {

                    camera.close();

                    cameraDevice =
                            null;
                }


                @Override
                public void onError(
                        @NonNull CameraDevice camera,
                        int error) {

                    camera.close();

                    cameraDevice =
                            null;
                }
            };


    // =====================================================
    // CAMERA PREVIEW
    // =====================================================

    private void createCameraPreview() {

        if (cameraDevice == null ||
                sessionEnded) {

            return;
        }


        SurfaceTexture surfaceTexture =
                textureView
                        .getSurfaceTexture();


        if (surfaceTexture == null) {
            return;
        }


        surfaceTexture
                .setDefaultBufferSize(
                        1280,
                        720
                );


        Surface surface =
                new Surface(
                        surfaceTexture
                );


        try {

            captureRequestBuilder =
                    cameraDevice
                            .createCaptureRequest(
                                    CameraDevice
                                            .TEMPLATE_PREVIEW
                            );


            captureRequestBuilder
                    .addTarget(
                            surface
                    );


            cameraDevice
                    .createCaptureSession(
                            Collections.singletonList(
                                    surface
                            ),

                            new CameraCaptureSession
                                    .StateCallback() {

                                @Override
                                public void onConfigured(
                                        @NonNull
                                        CameraCaptureSession session) {

                                    if (cameraDevice == null ||
                                            sessionEnded) {

                                        return;
                                    }


                                    cameraCaptureSession =
                                            session;


                                    try {

                                        captureRequestBuilder
                                                .set(
                                                        CaptureRequest
                                                                .CONTROL_MODE,

                                                        CaptureRequest
                                                                .CONTROL_MODE_AUTO
                                                );


                                        cameraCaptureSession
                                                .setRepeatingRequest(
                                                        captureRequestBuilder
                                                                .build(),
                                                        null,
                                                        backgroundHandler
                                                );


                                    } catch (
                                            CameraAccessException e) {

                                        e.printStackTrace();
                                    }
                                }


                                @Override
                                public void onConfigureFailed(
                                        @NonNull
                                        CameraCaptureSession session) {

                                }
                            },

                            backgroundHandler
                    );


        } catch (
                CameraAccessException e) {

            e.printStackTrace();
        }
    }


    // =====================================================
    // CLOSE CAMERA
    // =====================================================

    private void closeCamera() {

        try {

            if (cameraCaptureSession != null) {

                cameraCaptureSession.close();

                cameraCaptureSession =
                        null;
            }


            if (cameraDevice != null) {

                cameraDevice.close();

                cameraDevice =
                        null;
            }


        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =====================================================
    // BACKGROUND THREAD
    // =====================================================

    private void startBackgroundThread() {

        if (backgroundThread != null) {
            return;
        }


        backgroundThread =
                new HandlerThread(
                        "SwapCameraThread"
                );


        backgroundThread.start();


        backgroundHandler =
                new Handler(
                        backgroundThread
                                .getLooper()
                );
    }


    private void stopBackgroundThread() {

        if (backgroundThread == null) {
            return;
        }


        backgroundThread.quitSafely();


        try {

            backgroundThread.join();

        } catch (
                InterruptedException e) {

            e.printStackTrace();
        }


        backgroundThread =
                null;

        backgroundHandler =
                null;
    }


    // =====================================================
    // LIFECYCLE
    // =====================================================

    @Override
    protected void onResume() {
        super.onResume();

        if (sessionEnded) {
            return;
        }

        startBackgroundThread();


        if (textureView.isAvailable()) {

            checkCameraPermission();

        } else {

            textureView
                    .setSurfaceTextureListener(
                            textureListener
                    );
        }
    }


    @Override
    protected void onPause() {

        closeCamera();

        stopBackgroundThread();

        super.onPause();
    }


    @Override
    public void onBackPressed() {

        if (!sessionEnded) {

            endLiveSession();

        } else {

            super.onBackPressed();
        }
    }
}