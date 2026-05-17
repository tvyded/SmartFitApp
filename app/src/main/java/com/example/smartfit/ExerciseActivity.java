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

        // Инициализируем БД и принимаем данные из Intent
        db = new DatabaseHelper(this);
        workoutId = getIntent().getLongExtra("WORKOUT_ID", -1);
        exerciseType = getIntent().getStringExtra("EXERCISE_TYPE");

        webView = findViewById(R.id.webViewExercise);

        // Запрашиваем разрешение на уровне операционной системы Android
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            startWebViewTrain();
        }
    }

    private void startWebViewTrain() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Разрешаем JS для работы MediaPipe
        webSettings.setDomStorageEnabled(true);  // Включаем DOM-хранилище
        webSettings.setMediaPlaybackRequiresUserGesture(false); // Автозапуск видео-стрима

        webView.setWebViewClient(new WebViewClient());

        // Предоставляем WebView доступ к аппаратной камере телефона
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> request.grant(request.getResources()));
            }
        });

        // Создаем мост для связи JavaScript -> Java под именем AndroidBridge
        webView.addJavascriptInterface(new WebAppInterface(), "AndroidBridge");

        // Загружаем HTML файл из папки assets
        webView.loadUrl("file:///android_asset/index.html?type=" + exerciseType);
    }

    // Класс-мост, методы которого можно вызывать из JavaScript внутри index.html
    public class WebAppInterface {

        @JavascriptInterface
        public void saveCapturedSet(final int reps) {
            runOnUiThread(() -> {
                if (workoutId != -1 && exerciseType != null) {
                    // Сохраняем данные в таблицу sets через твой DatabaseHelper
                    db.saveSet(workoutId, exerciseType, reps);

                    Toast.makeText(ExerciseActivity.this,
                            "Подход сохранен: " + reps + " повт.", Toast.LENGTH_SHORT).show();

                    // Закрываем камеру и возвращаемся к списку упражнений
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
}