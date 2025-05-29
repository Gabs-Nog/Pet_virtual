package com.example.petvirtual;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MinigamesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minigames);

        findViewById(R.id.btnPong).setOnClickListener(v -> {
            startActivity(new Intent(this, PongActivity.class));
        });

        findViewById(R.id.btnVoltar).setOnClickListener(v -> {
            finish(); // Volta para MainActivity
        });
    }
}
