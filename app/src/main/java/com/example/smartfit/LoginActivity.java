package com.example.smartfit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        DatabaseHelper db = new DatabaseHelper(this);

        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            String user = ((EditText)findViewById(R.id.etUser)).getText().toString();
            String pass = ((EditText)findViewById(R.id.etPass)).getText().toString();
            if (db.checkLogin(user, pass)) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("USER_NAME", user);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Ошибка входа", Toast.LENGTH_SHORT).show();
            }
        });
    }
}