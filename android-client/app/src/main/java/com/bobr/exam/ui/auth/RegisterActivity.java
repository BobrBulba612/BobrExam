package com.bobr.exam.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bobr.exam.R;
import com.bobr.exam.data.remote.ApiService;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {

    @Inject ApiService api;

    private boolean strong(String p) {
        if (p == null || p.length() < 8) return false;
        boolean low = false, up = false, dig = false;
        for (char c : p.toCharArray()) {
            if (Character.isDigit(c)) dig = true;
            else if (Character.isUpperCase(c)) up = true;
            else if (Character.isLowerCase(c)) low = true;
        }
        return low && up && dig;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        EditText email = findViewById(R.id.etEmail);
        EditText nick = findViewById(R.id.etNick);
        EditText pass = findViewById(R.id.etPass);
        Spinner lang = findViewById(R.id.spLang);
        lang.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Русский", "English"}));
        Button btn = findViewById(R.id.btnReg);
        btn.setOnClickListener(v -> {
            String e = email.getText().toString().trim();
            String n = nick.getText().toString().trim();
            String p = pass.getText().toString();
            if (e.isEmpty() || n.isEmpty()) {
                Toast.makeText(this, "Заполни email и ник", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!strong(p)) {
                Toast.makeText(this, "Пароль: 8+ символов, буква, ЗАГЛАВНАЯ и цифра", Toast.LENGTH_LONG).show();
                return;
            }
            String l = lang.getSelectedItemPosition() == 1 ? "en" : "ru";
            api.register(new ApiService.RegisterRequest(e, n, p, l))
               .enqueue(new Callback<ApiService.AuthResponse>() {
                @Override public void onResponse(Call<ApiService.AuthResponse> c, Response<ApiService.AuthResponse> r) {
                    if (r.isSuccessful() && r.body() != null && r.body().access_token != null) {
                        getSharedPreferences("bobr", MODE_PRIVATE).edit().putString("jwt", r.body().access_token).apply();
                        startActivity(new Intent(RegisterActivity.this, SetupActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Email или ник занят", Toast.LENGTH_LONG).show();
                    }
                }
                @Override public void onFailure(Call<ApiService.AuthResponse> c, Throwable t) {
                    Toast.makeText(RegisterActivity.this, "Сеть: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
