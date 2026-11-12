package com.bobr.exam.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;
import com.bobr.exam.ui.util.ThemeHelper;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bobr.exam.R;
import com.bobr.exam.data.remote.ApiService;
import com.bobr.exam.ui.auth.LoginActivity;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ProfileActivity extends AppCompatActivity {

    @Inject ApiService api;
    private EditText etNick, etEmail, etOldPass, etNewPass;
    private TextView tvStats;
    private String avatar = "beaver1";

    private String token() {
        return "Bearer " + getSharedPreferences("bobr", MODE_PRIVATE).getString("jwt", "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Spinner spTheme = findViewById(R.id.spTheme);
        String[] themes = {"Системная", "Светлая", "Тёмная"};
        spTheme.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, themes));
        String saved = getSharedPreferences("bobr", MODE_PRIVATE).getString("theme", "system");
        spTheme.setSelection(saved.equals("light") ? 1 : saved.equals("dark") ? 2 : 0);
        spTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, android.view.View v, int pos, long id) {
                String t = pos == 1 ? "light" : pos == 2 ? "dark" : "system";
                ThemeHelper.save(ProfileActivity.this, t);
            }
            @Override public void onNothingSelected(AdapterView<?> p) { }
        });
        com.bobr.exam.ui.util.PressAnim.apply(this);
        etNick = findViewById(R.id.etNick);
        etEmail = findViewById(R.id.etEmail);
        etOldPass = findViewById(R.id.etOldPass);
        etNewPass = findViewById(R.id.etNewPass);
        tvStats = findViewById(R.id.tvStats);
        ImageView iv = findViewById(R.id.ivAvatar);
        iv.setOnClickListener(v -> {
            avatar = avatar.equals("beaver1") ? "beaver2" : avatar.equals("beaver2") ? "beaver3" : "beaver1";
            iv.setColorFilter(avatar.equals("beaver1") ? 0 : avatar.equals("beaver2") ? 0xFF8BC34A : 0xFFFFC107);
            Toast.makeText(this, "Аватар: " + avatar, Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
        findViewById(R.id.btnPass).setOnClickListener(v -> pass());
        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
        findViewById(R.id.btnDelete).setOnClickListener(v -> delete());
        load();
    }

    private void load() {
        api.getMyProfile(token()).enqueue(new Callback<ApiService.UserProfile>() {
            @Override public void onResponse(Call<ApiService.UserProfile> c, Response<ApiService.UserProfile> r) {
                if (r.isSuccessful() && r.body() != null) {
                    ApiService.UserProfile u = r.body();
                    etNick.setText(u.nickname);
                    etEmail.setText(u.email);
                    if (u.avatar != null) avatar = u.avatar;
                    tvStats.setText("XP: " + u.xp + "   Стрик: " + u.streak + "   HP: " + u.hearts);
                }
            }
            @Override public void onFailure(Call<ApiService.UserProfile> c, Throwable t) { }
        });
    }

    private void save() {
        api.updateProfile(token(), new ApiService.ProfileUpdateRequest(
                etNick.getText().toString(), etEmail.getText().toString(), avatar, null))
           .enqueue(new Callback<ApiService.UserProfile>() {
            @Override public void onResponse(Call<ApiService.UserProfile> c, Response<ApiService.UserProfile> r) {
                Toast.makeText(ProfileActivity.this, r.isSuccessful() ? "Сохранено" : "Ошибка: " + r.code(), Toast.LENGTH_SHORT).show();
            }
            @Override public void onFailure(Call<ApiService.UserProfile> c, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Сеть: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pass() {
        api.changePassword(token(), new ApiService.PasswordChangeRequest(
                etOldPass.getText().toString(), etNewPass.getText().toString()))
           .enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r) {
                Toast.makeText(ProfileActivity.this, r.isSuccessful() ? "Пароль изменён" : "Ошибка: " + r.code(), Toast.LENGTH_SHORT).show();
            }
            @Override public void onFailure(Call<ResponseBody> c, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Сеть: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        getSharedPreferences("bobr", MODE_PRIVATE).edit().remove("jwt").apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void delete() {
        api.deleteAccount(token()).enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r) {
                if (r.isSuccessful()) {
                    getSharedPreferences("bobr", MODE_PRIVATE).edit().remove("jwt").apply();
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                    finish();
                } else Toast.makeText(ProfileActivity.this, "Ошибка: " + r.code(), Toast.LENGTH_SHORT).show();
            }
            @Override public void onFailure(Call<ResponseBody> c, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Сеть: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
