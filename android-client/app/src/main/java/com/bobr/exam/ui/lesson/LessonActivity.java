package com.bobr.exam.ui.lesson;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bobr.exam.R;
import com.bobr.exam.data.remote.ApiService;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class LessonActivity extends AppCompatActivity {

    @Inject ApiService api;

    private TextView tvQuestion, tvHearts, tvXp, tvSource, tvAi;
    private EditText etAnswer;
    private LinearLayout optionsBox;
    private Button btnCheck, btnAi;

    private List<ApiService.TaskDto> tasks = new ArrayList<>();
    private int index = 0;
    private int hearts = 5;
    private int xp = 0;

    private String token() {
        return "Bearer " + getSharedPreferences("bobr", MODE_PRIVATE)
                .getString("jwt", "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvHearts = findViewById(R.id.tvHearts);
        tvXp = findViewById(R.id.tvXp);
        tvSource = findViewById(R.id.tvSource);
        tvAi = findViewById(R.id.tvAi);
        etAnswer = findViewById(R.id.etAnswer);
        optionsBox = findViewById(R.id.optionsBox);
        btnCheck = findViewById(R.id.btnCheck);
        btnAi = findViewById(R.id.btnAi);

        String subject = getIntent().getStringExtra("subject");
        if (subject == null) subject = "Математика";

        btnCheck.setOnClickListener(v -> check());
        btnAi.setOnClickListener(v -> askAi());
        stats();
        load(subject);
    }

    private void stats() {
        tvHearts.setText("HP: " + hearts);
        tvXp.setText("XP: " + xp);
    }

    private void load(String subject) {
        tvQuestion.setText("Бобр составляет курс...");
        api.getMyCourse(token()).enqueue(new Callback<List<ApiService.CourseUnit>>() {
            @Override
            public void onResponse(Call<List<ApiService.CourseUnit>> c,
                                   Response<List<ApiService.CourseUnit>> r) {
                if (r.isSuccessful() && r.body() != null && !r.body().isEmpty()) {
                    for (ApiService.CourseUnit u : r.body()) {
                        if (u.subject.equals(subject)) {
                            tasks = u.tasks;
                            break;
                        }
                    }
                    if (tasks.isEmpty()) tasks = r.body().get(0).tasks;
                    show();
                } else if (r.code() == 400) {
                    tvQuestion.setText("Сначала выбери класс и предметы");
                } else {
                    tvQuestion.setText("Курс пуст: " + r.code());
                }
            }
            @Override
            public void onFailure(Call<List<ApiService.CourseUnit>> c, Throwable t) {
                tvQuestion.setText("Сеть: " + t.getMessage());
            }
        });
    }

    private ApiService.TaskDto cur() { return tasks.get(index); }

    private void show() {
        if (index >= tasks.size()) {
            tvQuestion.setText("Урок пройден! XP: " + xp);
            tvSource.setText("");
            optionsBox.removeAllViews();
            etAnswer.setVisibility(View.GONE);
            btnCheck.setEnabled(false);
            btnAi.setEnabled(false);
            return;
        }
        ApiService.TaskDto t = cur();
        tvAi.setText("");
        tvQuestion.setText(t.questionText);
        String src = t.source == null ? "" : t.source;
        tvSource.setText(src + "  |  тема: " + t.topic);
        optionsBox.removeAllViews();
        etAnswer.setText("");

        boolean choice = "multiple_choice".equals(t.type) && t.optionsJson != null;
        if (choice) {
            etAnswer.setVisibility(View.GONE);
            try {
                JSONArray arr = new JSONArray(t.optionsJson);
                for (int i = 0; i < arr.length(); i++) {
                    final String opt = arr.getString(i);
                    Button b = new Button(this);
                    b.setText(opt);
                    b.setAllCaps(false);
                    b.setOnClickListener(v -> {
                        etAnswer.setText(opt);
                        check();
                    });
                    optionsBox.addView(b);
                }
            } catch (Exception ignored) { }
        } else {
            etAnswer.setVisibility(View.VISIBLE);
        }
    }

    private void check() {
        if (tasks.isEmpty() || index >= tasks.size()) return;
        ApiService.TaskDto t = cur();
        String answer = etAnswer.getText().toString();
        if (answer.trim().isEmpty()) {
            Toast.makeText(this, "Введи ответ", Toast.LENGTH_SHORT).show();
            return;
        }
        btnCheck.setEnabled(false);
        api.submit(token(), new ApiService.SubmitRequest(t.id, answer))
           .enqueue(new Callback<ApiService.SubmitResponse>() {
            @Override
            public void onResponse(Call<ApiService.SubmitResponse> c,
                                   Response<ApiService.SubmitResponse> r) {
                btnCheck.setEnabled(true);
                if (!r.isSuccessful() || r.body() == null) {
                    Toast.makeText(LessonActivity.this,
                            "Сервер: " + r.code(), Toast.LENGTH_LONG).show();
                    return;
                }
                ApiService.SubmitResponse s = r.body();
                xp = s.xp;
                hearts = s.hearts;
                stats();
                if (s.correct) {
                    Toast.makeText(LessonActivity.this,
                            "Верно! Бобр грызёт ветку", Toast.LENGTH_SHORT).show();
                    index++;
                    show();
                } else {
                    String ex = s.explanation == null ? "" : s.explanation;
                    tvAi.setText("Подсказка: " + ex);
                    Toast.makeText(LessonActivity.this,
                            "Ошибка. HP: " + hearts, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiService.SubmitResponse> c, Throwable e) {
                btnCheck.setEnabled(true);
                Toast.makeText(LessonActivity.this,
                        "Сеть: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void askAi() {
        if (tasks.isEmpty() || index >= tasks.size()) return;
        ApiService.TaskDto t = cur();
        String answer = etAnswer.getText().toString();
        tvAi.setText("Бобр-Профессор думает...");
        ApiService.AiRequest req = new ApiService.AiRequest(
                t.id, t.examType, t.subject, t.questionText, answer);
        api.explain(req).enqueue(new Callback<ApiService.AiResponse>() {
            @Override
            public void onResponse(Call<ApiService.AiResponse> c,
                                   Response<ApiService.AiResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    tvAi.setText(r.body().explanation);
                } else {
                    tvAi.setText("ИИ недоступен: " + r.code());
                }
            }
            @Override
            public void onFailure(Call<ApiService.AiResponse> c, Throwable e) {
                tvAi.setText("Сеть: " + e.getMessage());
            }
        });
    }
}