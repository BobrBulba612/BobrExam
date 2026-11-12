package com.bobr.exam.ui.util;

import android.content.Context;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeHelper {

    public static void applySaved(Context c) {
        String t = c.getSharedPreferences("bobr", Context.MODE_PRIVATE)
                .getString("theme", "system");
        apply(t);
    }

    public static void apply(String t) {
        if ("light".equals(t)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if ("dark".equals(t)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    public static void save(Context c, String t) {
        c.getSharedPreferences("bobr", Context.MODE_PRIVATE)
                .edit().putString("theme", t).apply();
        apply(t);
    }
}
