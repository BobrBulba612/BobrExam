package com.bobr.exam.ui.path;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bobr.exam.R;
import com.bobr.exam.data.remote.ApiService;
import com.bobr.exam.ui.auth.LoginActivity;
import com.bobr.exam.ui.auth.SetupActivity;
import com.bobr.exam.ui.leaderboard.LeaderboardActivity;
import com.bobr.exam.ui.lesson.LessonActivity;
import com.bobr.exam.ui.profile.ProfileActivity;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class PathActivity extends AppCompatActivity {

    @Inject ApiService api;

    private String token() {
        return "Bearer " + getSharedPreferences("bobr", MODE_PRIVATE).getString("jwt", "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path);
        com.bobr.exam.ui.util.PressAnim.apply(this);
        TextView title = findViewById(R.id.tvPathTitle);
        RecyclerView rv = findViewById(R.id.rvPath);
        rv.setLayoutManager(new LinearLayoutManager(this));
        title.setText("Твой курс");

        ImageView beaver = findViewById(R.id.ivBeaver);
        ObjectAnimator bounce = ObjectAnimator.ofFloat(beaver, "translationY", 0f, -18f, 0f);
        bounce.setDuration(1600);
        bounce.setRepeatCount(ObjectAnimator.INFINITE);
        bounce.start();

        Button menu = findViewById(R.id.btnMenu);
        menu.setOnClickListener(v -> {
            PopupMenu p = new PopupMenu(this, v);
            p.getMenu().add("Профиль");
            p.getMenu().add("Лидерборд");
            p.getMenu().add("Сменить предметы");
            p.getMenu().add("Выйти");
            p.setOnMenuItemClickListener(item -> {
                String t = String.valueOf(item.getTitle());
                if (t.equals("Профиль")) startActivity(new Intent(this, ProfileActivity.class));
                else if (t.equals("Лидерборд")) startActivity(new Intent(this, LeaderboardActivity.class));
                else if (t.equals("Сменить предметы")) startActivity(new Intent(this, SetupActivity.class));
                else if (t.equals("Выйти")) {
                    getSharedPreferences("bobr", MODE_PRIVATE).edit().remove("jwt").apply();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }
                return true;
            });
            p.show();
        });

        load(rv);
    }

    private void load(RecyclerView rv) {
        api.getMyCourse(token()).enqueue(new Callback<List<ApiService.CourseUnit>>() {
            @Override
            public void onResponse(Call<List<ApiService.CourseUnit>> c, Response<List<ApiService.CourseUnit>> r) {
                List<PathNode> nodes = new ArrayList<>();
                if (r.isSuccessful() && r.body() != null) {
                    for (ApiService.CourseUnit u : r.body()) {
                        nodes.add(new PathNode(u.subject + " (" + u.tasks.size() + ")", true));
                    }
                }
                if (nodes.isEmpty()) nodes.add(new PathNode("Курс пуст. Проверь seed", false));
                rv.setAdapter(new PathAdapter(nodes, node -> {
                    if (node.unlocked) {
                        String subj = node.title.split(" \\(")[0];
                        Intent in = new Intent(PathActivity.this, LessonActivity.class);
                        in.putExtra("subject", subj);
                        startActivity(in);
                    }
                }));
            }
            @Override
            public void onFailure(Call<List<ApiService.CourseUnit>> c, Throwable t) {
                List<PathNode> nodes = new ArrayList<>();
                nodes.add(new PathNode("Ошибка сети", false));
                rv.setAdapter(new PathAdapter(nodes, n -> { }));
            }
        });
    }
}
