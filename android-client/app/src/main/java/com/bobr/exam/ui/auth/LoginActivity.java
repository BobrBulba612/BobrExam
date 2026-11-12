package com.bobr.exam.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bobr.exam.R;
import com.bobr.exam.data.remote.ApiService;
import com.bobr.exam.ui.path.PathActivity;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    @Inject ApiService api;
    private EditText etEmail, etPassword;

    private String savedToken() {
        return getSharedPreferences("bobr", MODE_PRIVATE).getString("jwt", "");
    }

    private String token() {
        return "Bearer " + savedToken();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!savedToken().isEmpty()) {
            route();
            return;
        }
        setContentView(R.layout.activity_login);
        com.bobr.exam.ui.util.PressAnim.apply(this);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);
        Button btnForgot = findViewById(R.id.btnForgot);
        btnLogin.setOnClickListener(v -> login());
        btnRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        btnForgot.setOnClickListener(v -> startActivity(new Intent(this, ForgotActivity.class)));
    }

    private void route() {
        api.getMyProfile(token()).enqueue(new Callback<ApiService.UserProfile>() {
            @Override
            public void onResponse(Call<ApiService.UserProfile> c, Response<ApiService.UserProfile> r) {
                if (!r.isSuccessful()) {
                    getSharedPreferences("bobr", MODE_PRIVATE).edit().remove("jwt").apply();
                    recreate();
                    return;
                }
                Intent in;
                if (r.body() != null && r.body().subjects != null && !r.body().subjects.isEmpty()) {
                    in = new Intent(LoginActivity.this, PathActivity.class);
                } else {
                    in = new Intent(LoginActivity.this, SetupActivity.class);
                }
                startActivity(in);
                finish();
            }
            @Override
            public void onFailure(Call<ApiService.UserProfile> c, Throwable t) {
                startActivity(new Intent(LoginActivity.this, PathActivity.class));
                finish();
            }
        });
    }

    private void login() {
        String e = etEmail.getText().toString().trim();
        String p = etPassword.getText().toString().trim();
        if (e.isEmpty() || p.isEmpty()) {
            Toast.makeText(this, "Заполни все поля", Toast.LENGTH_SHORT).show();
            return;
        }
        api.login(new ApiService.LoginRequest(e, p)).enqueue(new Callback<ApiService.AuthResponse>() {
            @Override
            public void onResponse(Call<ApiService.AuthResponse> c, Response<ApiService.AuthResponse> r) {
                if (r.isSuccessful() && r.body() != null && r.body().access_token != null) {
                    getSharedPreferences("bobr", MODE_PRIVATE).edit().putString("jwt", r.body().access_token).apply();
                    route();
                } else {
                    Toast.makeText(LoginActivity.this, "Неверный email или пароль", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiService.AuthResponse> c, Throwable t) {
                Toast.makeText(LoginActivity.this, "Сеть: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
