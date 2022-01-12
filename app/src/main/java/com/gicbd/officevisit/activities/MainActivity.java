package com.gicbd.officevisit.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.gicbd.officevisit.R;
import com.gicbd.officevisit.globals.Constants;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {


    Boolean clicked = false;
    String qr_code = "";
    private LottieAnimationView mAnimationView;
    private LinearLayout mLoadingLayout;
    private LinearLayout layout_success;
    private LinearLayout layout_error;
    private TextView mWelcomeText;
    private ZXingScannerView scannerView;
    private ImageView goBack, setFlash, switchCamera;
    private ImageButton sendQRCodeButton;
    private EditText inputQRCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // For Hide Status/Notification Bar background color;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        //find all image button & others item here.
        mAnimationView = findViewById(R.id.animation_view);

        mLoadingLayout = findViewById(R.id.loading_layout);
        layout_success = findViewById(R.id.layout_success);
        layout_error = findViewById(R.id.layout_error);

        mWelcomeText = findViewById(R.id.welcome_text);

        scannerView = findViewById(R.id.scanner_view);
        goBack = (ImageView) findViewById(R.id.back_id);
        setFlash = (ImageView) findViewById(R.id.flash_id);
        switchCamera = (ImageView) findViewById(R.id.camera_id);
        sendQRCodeButton = findViewById(R.id.qr_code_send_button);
        inputQRCode = findViewById(R.id.input_qr_phone);
        //Request for camera permission and others here.
        checkPermissions();
        //Switch camera when user clicked cameraSwitch Button.
        switchCamera();
        //set camera flash when user clicked flash Button.
        setFlash();
        //Send QR code with button click.
        sendQRCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qr_code = inputQRCode.getText().toString();
                if (checkConnection()) {
                    showAnimation();
                    sendScannedQRCodeRequest("880" + qr_code);
                } else {
                    errorDialog();
                }
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        scannerView.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT); //SET Camera id 1  for front camera
        scannerView.setAutoFocus(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result) {
        if (result.getText() != null && !result.getText().isEmpty()) {
            qr_code = result.getText().toString();
            if (checkConnection()) {
                showAnimation();
                sendScannedQRCodeRequest(qr_code);
            } else {
                errorDialog();
            }
        } else
            Toast.makeText(getApplicationContext(), "Scanning failed! , try again", Toast.LENGTH_SHORT).show();

    }


    //Switch camera by clicking switchCamera button
    public void switchCamera() {
        switchCamera = (ImageView) findViewById(R.id.camera_id);
        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicked = !clicked;
                if (clicked) {
                    scannerView.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                    // startCamera();
                } else
                    scannerView.startCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                //startCamera();
            }
        });

    }

    //set Flash on/off by clicking flash button.
    public void setFlash() {
        setFlash = (ImageView) findViewById(R.id.flash_id);
        setFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clicked = !clicked;
                if (clicked) {

                    scannerView.setFlash(true);
                } else
                    scannerView.setFlash(false);
            }
        });
    }


    private void checkPermissions() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        scannerView.setResultHandler(MainActivity.this);
                        scannerView.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                        scannerView.setAutoFocus(true);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(MainActivity.this, "You must accept this permission", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();
    }

    ///////////////////REVERSED//////////////////////


    //Get Request with scanned QR Code Data.
    private void sendScannedQRCodeRequest(String scan_qr) {
        //start loading
        //   showAnimation();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL) //base url
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitClient ourClient = retrofit.create(RetrofitClient.class);
        Call<Response> res = ourClient.getResponse(scan_qr);

        res.enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                //hide loading
                hideAnimation();
                Log.e("SUC ", response.code() + "");

                if (response.isSuccessful()) {
                    //show success & hide error
                    Log.d("Input_QR", "success");
                    Log.d("Input_QR", response.message() + "");

                    inputQRCode.setText("");

                    // Toast.makeText(getApplicationContext(), "Welcome Mr. " + response.body().getFirstName() + " " + response.body().getLastName() + "", Toast.LENGTH_SHORT).show();
                    mWelcomeText.setText("" + response.body().getFirstName() + " " + response.body().getLastName() + "");

                    layout_success.setVisibility(View.VISIBLE);
                    layout_error.setVisibility(View.GONE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            layout_success.setVisibility(View.GONE);
                            startCamera();
                        }
                    }, 3000);

                } else {
                    layout_success.setVisibility(View.GONE);
                    layout_error.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            layout_error.setVisibility(View.GONE);
                            startCamera();
                        }
                    }, 3000);
                }
            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                //hide loading
                hideAnimation();

                Log.e("FAILURE", "" + t.getStackTrace());

                tryAgainDialog();

//                if (t.getMessage().toString() == "stream was reset: PROTOCOL_ERROR") {
//                    tryAgainDialog();
//                }

                layout_success.setVisibility(View.GONE);
                layout_error.setVisibility(View.GONE);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        layout_error.setVisibility(View.GONE);
                        startCamera();
                    }
                }, 3000);

            }
        });

    }

    public void startCamera() {
        scannerView.setResultHandler(MainActivity.this);
        scannerView.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT); //SET Camera id 1  for front camera
        scannerView.setAutoFocus(true);
    }


    //Check internet connection from ConnectivityManager
    private boolean checkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    //Dialog about no Internet connection available alert.
    private void errorDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("No Internet Connection!")
                .setMessage("Please check your internet connection and try again.")
                .setNegativeButton("Try again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        startCamera();
                    }
                });
        dialog.show();
    }

    private void tryAgainDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Poor Internet Connection!")
                .setMessage("Please check your internet connection and try again.")
                .setNegativeButton("Try again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        startCamera();
                    }
                });
        dialog.show();
    }

    //Show animation method will call when sendRequest to the server for get some response.
    private void showAnimation() {
        mLoadingLayout.setVisibility(View.VISIBLE);
        mAnimationView.playAnimation();
        mAnimationView.loop(true);
        layout_error.setVisibility(View.GONE);
        layout_success.setVisibility(View.GONE);
    }


    //Hide animation method call when response will fail or success.
    private void hideAnimation() {
        if (mAnimationView.isAnimating()) mAnimationView.cancelAnimation();
        mLoadingLayout.setVisibility(View.GONE);
    }

}
