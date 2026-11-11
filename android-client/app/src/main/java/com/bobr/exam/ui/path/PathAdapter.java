package com.bobr.exam.ui.path;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.bobr.exam.R;
import java.util.List;

public class PathAdapter extends RecyclerView.Adapter<PathAdapter.VH> {

    public interface Click { void onClick(PathNode node); }

    private final List<PathNode> items;
    private final Click click;

    public PathAdapter(List<PathNode> items, Click click) {
        this.items = items;
        this.click = click;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_path_node, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PathNode n = items.get(position);
        h.title.setText(n.unlocked ? n.title : n.title + " (закрыто)");
        h.title.setAlpha(n.unlocked ? 1f : 0.45f);
        float bias = (position % 3 == 0) ? 0.15f : (position % 3 == 1 ? 0.5f : 0.85f);
        ConstraintLayout.LayoutParams lp =
                (ConstraintLayout.LayoutParams) h.title.getLayoutParams();
        lp.horizontalBias = bias;
        h.title.setLayoutParams(lp);
        h.itemView.setOnClickListener(v -> click.onClick(n));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title;
        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvNodeTitle);
        }
    }
}
