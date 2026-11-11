package com.bobr.exam.ui.profile;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bobr.exam.R;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        TextView nick = findViewById(R.id.tvNick);
        TextView stats = findViewById(R.id.tvStats);
        nick.setText("Бобр");
        stats.setText("XP: 0   Streak: 0   HP: 5");
    }
}
