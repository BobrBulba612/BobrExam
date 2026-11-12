package com.bobr.exam.ui.leaderboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bobr.exam.R;
import com.bobr.exam.data.remote.ApiService;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class LeaderboardActivity extends AppCompatActivity {

    @Inject ApiService api;
    private final List<ApiService.LeaderEntry> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        RecyclerView rv = findViewById(R.id.rvLeader);
        rv.setLayoutManager(new LinearLayoutManager(this));
        Adapter ad = new Adapter();
        rv.setAdapter(ad);
        api.getLeaderboard().enqueue(new Callback<List<ApiService.LeaderEntry>>() {
            @Override public void onResponse(Call<List<ApiService.LeaderEntry>> c, Response<List<ApiService.LeaderEntry>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    data.clear();
                    data.addAll(r.body());
                    ad.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(Call<List<ApiService.LeaderEntry>> c, Throwable t) { }
        });
    }

    class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new VH(v);
        }
        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            ApiService.LeaderEntry e = data.get(position);
            h.tv.setText(e.rank + ". " + e.nickname + "  —  " + e.xp + " XP  (стрик " + e.streak + ")");
            h.tv.setTextColor(0xFF4E342E);
        }
        @Override
        public int getItemCount() { return data.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tv;
            VH(@NonNull View itemView) {
                super(itemView);
                tv = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
