package com.bobr.exam.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
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

    private String token() {
        SharedPreferences sp = getSharedPreferences("bobr", MODE_PRIVATE);
        return "Bearer " + sp.getString("jwt", "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);
        btnLogin.setOnClickListener(v -> login());
        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void route() {
        api.getMyProfile(token()).enqueue(new Callback<ApiService.UserProfile>() {
            @Override
            public void onResponse(Call<ApiService.UserProfile> c,
                                   Response<ApiService.UserProfile> r) {
                Intent in;
                if (r.isSuccessful() && r.body() != null
                        && r.body().subjects != null && !r.body().subjects.isEmpty()) {
                    in = new Intent(LoginActivity.this, PathActivity.class);
                } else {
                    in = new Intent(LoginActivity.this, SetupActivity.class);
                }
                startActivity(in);
                finish();
            }
            @Override
            public void onFailure(Call<ApiService.UserProfile> c, Throwable t) {
                startActivity(new Intent(LoginActivity.this, SetupActivity.class));
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
        api.login(new ApiService.LoginRequest(e, p))
           .enqueue(new Callback<ApiService.AuthResponse>() {
            @Override
            public void onResponse(Call<ApiService.AuthResponse> c,
                                   Response<ApiService.AuthResponse> r) {
                if (r.isSuccessful() && r.body() != null
                        && r.body().access_token != null) {
                    getSharedPreferences("bobr", MODE_PRIVATE).edit()
                            .putString("jwt", r.body().access_token).apply();
                    route();
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Неверный email или пароль", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiService.AuthResponse> c, Throwable t) {
                Toast.makeText(LoginActivity.this,
                        "Сеть: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
