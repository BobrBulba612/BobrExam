package com.bobr.exam.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bobr.exam.R;
import com.bobr.exam.data.remote.ApiService;
import com.bobr.exam.ui.path.PathActivity;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class SetupActivity extends AppCompatActivity {

    @Inject ApiService api;
    private Spinner spGrade;
    private final List<CheckBox> checks = new ArrayList<>();

    private static final String[] SUBJECTS = {
        "Математика", "Русский язык", "Физика", "Информатика",
        "История", "Биология", "География", "Химия",
        "Обществознание", "Английский язык", "Литература"
    };

    private String token() {
        return "Bearer " + getSharedPreferences("bobr", MODE_PRIVATE)
                .getString("jwt", "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        com.bobr.exam.ui.util.PressAnim.apply(this);
        spGrade = findViewById(R.id.spGrade);
        LinearLayout box = findViewById(R.id.boxSubjects);
        Button btn = findViewById(R.id.btnSetup);

        Integer[] grades = {5, 6, 7, 8, 9, 10, 11};
        spGrade.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, grades));
        spGrade.setSelection(4);

        for (String s : SUBJECTS) {
            CheckBox cb = new CheckBox(this);
            cb.setText(s);
            cb.setOnCheckedChangeListener((v, on) -> {
                if (on && count() > 5) {
                    cb.setChecked(false);
                    Toast.makeText(this, "Максимум 5 предметов",
                            Toast.LENGTH_SHORT).show();
                }
            });
            checks.add(cb);
            box.addView(cb);
        }
        btn.setOnClickListener(v -> submit());
    }

    private int count() {
        int n = 0;
        for (CheckBox c : checks) if (c.isChecked()) n++;
        return n;
    }

    private void submit() {
        List<String> subs = new ArrayList<>();
        for (CheckBox c : checks) if (c.isChecked()) subs.add(c.getText().toString());
        if (subs.isEmpty()) {
            Toast.makeText(this, "Выбери хотя бы 1 предмет", Toast.LENGTH_SHORT).show();
            return;
        }
        int grade = (Integer) spGrade.getSelectedItem();
        api.setup(token(), new ApiService.SetupRequest(grade, subs))
           .enqueue(new Callback<ApiService.UserProfile>() {
            @Override
            public void onResponse(Call<ApiService.UserProfile> c,
                                   Response<ApiService.UserProfile> r) {
                if (r.isSuccessful()) {
                    startActivity(new Intent(SetupActivity.this, PathActivity.class));
                    finish();
                } else {
                    Toast.makeText(SetupActivity.this,
                            "Ошибка: " + r.code(), Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<ApiService.UserProfile> c, Throwable t) {
                Toast.makeText(SetupActivity.this,
                        "Сеть: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}