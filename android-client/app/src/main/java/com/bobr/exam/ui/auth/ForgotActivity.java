package com.bobr.exam.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
public class ForgotActivity extends AppCompatActivity {

    @Inject ApiService api;
    private LinearLayout stepCode, stepPass;
    private EditText etEmail, etCode, etP1, etP2;
    private String email = "";
    private String code = "";

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
        setContentView(R.layout.activity_forgot);
        PressAnim.apply(this);
        stepCode = findViewById(R.id.stepCode);
        stepPass = findViewById(R.id.stepPass);
        etEmail = findViewById(R.id.etEmail);
        etCode = findViewById(R.id.etCode);
        etP1 = findViewById(R.id.etP1);
        etP2 = findViewById(R.id.etP2);
        Button send = findViewById(R.id.btnSend);
        Button next = findViewById(R.id.btnNext);
        Button reset = findViewById(R.id.btnReset);
        send.setOnClickListener(v -> sendCode());
        next.setOnClickListener(v -> checkCode());
        reset.setOnClickListener(v -> reset());
    }

    private void sendCode() {
        String e = etEmail.getText().toString().trim();
        if (!e.contains("@")) {
            Toast.makeText(this, "Введи почту", Toast.LENGTH_SHORT).show();
            return;
        }
        api.sendCode(new ApiService.EmailRequest(e)).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r) {
                Toast.makeText(ForgotActivity.this,
                        r.isSuccessful() ? "Код отправлен на почту" : "Ошибка: " + r.code(),
                        Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(Call<ResponseBody> c, Throwable t) {
                Toast.makeText(ForgotActivity.this, "Сеть: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkCode() {
        email = etEmail.getText().toString().trim();
        code = etCode.getText().toString().trim();
        if (code.length() != 6) {
            Toast.makeText(this, "Код из 6 цифр", Toast.LENGTH_SHORT).show();
            return;
        }
        api.checkCode(new ApiService.VerifyRequest(email, code)).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r) {
                if (r.isSuccessful()) {
                    stepCode.setVisibility(View.GONE);
                    stepPass.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(ForgotActivity.this, "Неверный или истёкший код", Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(Call<ResponseBody> c, Throwable t) {
                Toast.makeText(ForgotActivity.this, "Сеть: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void reset() {
        String p1 = etP1.getText().toString();
        String p2 = etP2.getText().toString();
        if (!p1.equals(p2)) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!strong(p1)) {
            Toast.makeText(this, "Пароль: 8+ символов, буква, ЗАГЛАВНАЯ и цифра", Toast.LENGTH_LONG).show();
            return;
        }
        api.resetPassword(new ApiService.ResetRequest(email, code, p1)).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r) {
                if (r.isSuccessful()) {
                    Toast.makeText(ForgotActivity.this, "Пароль изменён, войди", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(ForgotActivity.this, "Ошибка: " + r.code(), Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(Call<ResponseBody> c, Throwable t) {
                Toast.makeText(ForgotActivity.this, "Сеть: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
