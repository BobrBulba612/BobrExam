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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class PathActivity extends AppCompatActivity {

    @Inject ApiService api;

    private String exam = "ОГЭ";
    private String subject = "Математика";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path);

        TextView title = findViewById(R.id.tvPathTitle);
        RecyclerView rv = findViewById(R.id.rvPath);
        rv.setLayoutManager(new LinearLayoutManager(this));

        title.setText(exam + " - " + subject);
        load(rv);
    }

    private void load(RecyclerView rv) {
        api.getPath(exam, subject).enqueue(new Callback<List<ApiService.TaskDto>>() {
            @Override
            public void onResponse(Call<List<ApiService.TaskDto>> c,
                                   Response<List<ApiService.TaskDto>> r) {
                List<PathNode> nodes = new ArrayList<>();
                if (r.isSuccessful() && r.body() != null) {
                    Set<String> topics = new LinkedHashSet<>();
                    for (ApiService.TaskDto t : r.body()) {
                        topics.add(t.topic);
                    }
                    int i = 0;
                    for (String tp : topics) {
                        nodes.add(new PathNode(tp, i == 0));
                        i++;
                    }
                }
                if (nodes.isEmpty()) {
                    nodes.add(new PathNode("Нет данных. Выполни seed", false));
                }
                rv.setAdapter(new PathAdapter(nodes, node -> {
                    if (node.unlocked) {
                                         in.putExtra("subject", subject);
                        startActivity(in);
                    }
                }));
            }
            @Override
            public void onFailure(Call<List<ApiService.TaskDto>> c, Throwable t) {
                List<PathNode> nodes = new ArrayList<>();
                nodes.add(new PathNode("Ошибка сети", false));
                rv.setAdapter(new PathAdapter(nodes, n -> { }));
            }
        });
    }
}
