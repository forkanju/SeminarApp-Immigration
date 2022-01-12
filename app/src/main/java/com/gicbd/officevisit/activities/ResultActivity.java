package com.gicbd.officevisit.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.gicbd.officevisit.R;
import com.gicbd.officevisit.globals.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ResultActivity extends AppCompatActivity {

    public static int Activity_Timeout = 2500;
    private LottieAnimationView animationView;
    private LinearLayout layout_success;
    private LinearLayout layout_error;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        animationView = (LottieAnimationView) findViewById(R.id.animation_view);
        layout_success = (LinearLayout) findViewById(R.id.layout_success);
        layout_error = (LinearLayout) findViewById(R.id.layout_error);


        Intent intent = getIntent();
        String scan_qr = intent.getStringExtra("scan_qr");
        Log.d("Input_QR", scan_qr + "");


        if (scan_qr != null) {
//            String scan_qr = getIntent().getStringExtra("scan_qr");
            if (checkConnection()) sendScannedQRCodeRequest(scan_qr);
                //else here alert dialog added for user instruction about no internet connection
            else {
                errorDialog();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, 3000);

            }

        }

    }


    //Retrofit request functionality
    //Get Request with scanned QR Code Data.
    private void sendScannedQRCodeRequest(String scan_qr) {
        //start loading
        showAnimation();

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
                if (response.isSuccessful()) {
                    //show success & hide error
                    Log.d("Input_QR", "success");
                    Log.d("Input_QR", response.message() + "");

                    Toast.makeText(getApplicationContext(), response.body().getFirstName() + " " + response.body().getLastName() + "", Toast.LENGTH_SHORT).show();


                    layout_success.setVisibility(View.VISIBLE);
                    layout_error.setVisibility(View.GONE);

                } else {
                    layout_success.setVisibility(View.GONE);
                    layout_error.setVisibility(View.VISIBLE);
                }

                //Auto timeout and go to scanner activity
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                        startActivity(intent);
                        // finish();
                    }
                }, Activity_Timeout);
            }

            //if
            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                //hide loading
                hideAnimation();

                Log.d("Input_QR", t.getMessage() + "");

                if (t.getMessage().toString() == "stream was reset: PROTOCOL_ERROR") {
                    tryAgainDialog();
                }

                layout_success.setVisibility(View.GONE);
                layout_error.setVisibility(View.VISIBLE);

                //Auto timeout and go to scanner activity
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, Activity_Timeout);
            }
        });

    }

    //Show animation method will call when sendRequest to the server for get some response.
    private void showAnimation() {
        animationView.setVisibility(View.VISIBLE);
        animationView.playAnimation();
        animationView.loop(true);
    }

    //Hide animation method call when response will fail or success.
    private void hideAnimation() {
        if (animationView.isAnimating()) animationView.cancelAnimation();
        animationView.setVisibility(View.GONE);
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
                    }
                });
        dialog.show();
    }


}
