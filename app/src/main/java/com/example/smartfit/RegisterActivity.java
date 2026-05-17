package com.example.smartfit;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        DatabaseHelper db = new DatabaseHelper(this);

        findViewById(R.id.btnRegister).setOnClickListener(v -> {
            String user = ((EditText)findViewById(R.id.etUserReg)).getText().toString();
            String pass = ((EditText)findViewById(R.id.etPassReg)).getText().toString();
            if (db.register(user, pass)) {
                Toast.makeText(this, "Успех!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}