package com.example.smartfit;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ExerciseActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 200;
    private WebView webView;

    private DatabaseHelper db;
    private long workoutId;
    private String exerciseType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        db = new DatabaseHelper(this);
        workoutId = getIntent().getLongExtra("WORKOUT_ID", -1);
        exerciseType = getIntent().getStringExtra("EXERCISE_TYPE");

        webView = findViewById(R.id.webViewExercise);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            startWebViewTrain();
        }
    }

    private void startWebViewTrain() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebViewClient(new WebViewClient());

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> request.grant(request.getResources()));
            }
        });

        webView.addJavascriptInterface(new WebAppInterface(), "AndroidBridge");

        webView.loadUrl("file:///android_asset/index.html?type=" + exerciseType);
    }


    public class WebAppInterface {

        @JavascriptInterface
        public void saveCapturedSet(final int reps) {
            runOnUiThread(() -> {
                if (workoutId != -1 && exerciseType != null) {
                    db.saveSet(workoutId, exerciseType, reps);

                    Toast.makeText(ExerciseActivity.this,
                            "Подход сохранен: " + reps + " повт.", Toast.LENGTH_SHORT).show();

                    finish();
                } else {
                    Toast.makeText(ExerciseActivity.this,
                            "Ошибка: Неверный ID тренировки (" + workoutId + ")", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startWebViewTrain();
            } else {
                Toast.makeText(this, "Камера необходима для работы ИИ", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.loadUrl("about:blank");

            webView.onPause();
            webView.removeAllViews();


            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}