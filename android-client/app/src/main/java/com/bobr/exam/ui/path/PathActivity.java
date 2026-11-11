package com.bobr.exam.ui.path;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bobr.exam.R;
import com.bobr.exam.data.remote.ApiService;
import com.bobr.exam.ui.lesson.LessonActivity;
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
        return "Bearer " + getSharedPreferences("bobr", MODE_PRIVATE)
                .getString("jwt", "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path);
        TextView title = findViewById(R.id.tvPathTitle);
        RecyclerView rv = findViewById(R.id.rvPath);
        rv.setLayoutManager(new LinearLayoutManager(this));
        title.setText("Твой курс подготовки");

        api.getMyCourse(token()).enqueue(new Callback<List<ApiService.CourseUnit>>() {
            @Override
            public void onResponse(Call<List<ApiService.CourseUnit>> c,
                                   Response<List<ApiService.CourseUnit>> r) {
                List<PathNode> nodes = new ArrayList<>();
                if (r.isSuccessful() && r.body() != null) {
                    int i = 0;
                    for (ApiService.CourseUnit u : r.body()) {
                        String label = u.subject + " (" + u.tasks.size() + " заданий)";
                        nodes.add(new PathNode(label, i == 0));
                        i++;
                    }
                }
                if (nodes.isEmpty()) {
                    nodes.add(new PathNode("Курс пуст. Проверь seed и setup", false));
                }
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
