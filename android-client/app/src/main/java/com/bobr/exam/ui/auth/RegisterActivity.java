package com.bobr.exam.ui.auth;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bobr.exam.R;
import com.bobr.exam.data.remote.ApiService;
import com.bobr.exam.ui.util.PressAnim;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {

    @Inject ApiService api;
    private EditText email, nick, pass;

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
        PressAnim.apply(this);
        email = findViewById(R.id.etEmail);
        nick = findViewById(R.id.etNick);
        pass = findViewById(R.id.etPass);
        Button btn = findViewById(R.id.btnReg);
        btn.setOnClickListener(v -> startRegister());
    }

    private void startRegister() {
        String e = email.getText().toString().trim();
        String n = nick.getText().toString().trim();
        String p = pass.getText().toString();
        if (!e.contains("@") || n.isEmpty()) {
            Toast.makeText(this, "Заполни email и ник", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!strong(p)) {
            Toast.makeText(this, "Пароль: 8+ символов, буква, ЗАГЛАВНАЯ и цифра", Toast.LENGTH_LONG).show();
            return;
        }
        api.sendCode(new ApiService.EmailRequest(e)).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r) {
                if (r.isSuccessful()) askCode();
                else Toast.makeText(RegisterActivity.this, "Не удалось отправить код: " + r.code(), Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(Call<ResponseBody> c, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Сеть: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void askCode() {
        EditText input = new EditText(this);
        input.setHint("Код из письма");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(this)
                .setTitle("Подтверждение почты")
                .setMessage("Мы отправили 6-значный код на " + email.getText().toString().trim())
                .setView(input)
                .setCancelable(false)
                .setPositiveButton("Подтвердить", (d, w) -> verify(input.getText().toString().trim()))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void verify(String code) {
        String e = email.getText().toString().trim();
        if (code.length() != 6) {
            Toast.makeText(this, "Код из 6 цифр", Toast.LENGTH_SHORT).show();
            return;
        }
        api.verifyEmail(new ApiService.VerifyRequest(e, code)).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r) {
                if (r.isSuccessful()) doRegister();
                else Toast.makeText(RegisterActivity.this, "Неверный или истёкший код", Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(Call<ResponseBody> c, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Сеть: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void doRegister() {
        String e = email.getText().toString().trim();
        String n = nick.getText().toString().trim();
        String p = pass.getText().toString();
        api.register(new ApiService.RegisterRequest(e, n, p, "ru")).enqueue(new Callback<ApiService.AuthResponse>() {
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
    }
}
