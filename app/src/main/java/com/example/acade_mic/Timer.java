package com.example.acade_mic;

import android.os.Handler;
import android.os.Looper;

public class Timer {
    public interface OnTimerTickListener {
        void onTimerTick(String duration);
    }

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    private long duration = 0L;
    private long delay = 100L;

    public Timer(OnTimerTickListener listener) {
        runnable = new Runnable() {
            @Override
            public void run() {
                duration += delay;
                handler.postDelayed(runnable, delay);
                listener.onTimerTick(format());
            }
        };
    }

    public void start() {
        handler.postDelayed(runnable, delay);
    }

    public void pause() {
        handler.removeCallbacks(runnable);
    }

    public void stop() {
        handler.removeCallbacks(runnable);
        duration = 0L;
    }

    public String format() {
        long millis = duration % 1000;
        long seconds = (duration / 1000) % 60;
        long minutes = (duration / (1000 * 60)) % 60;
        long hours = (duration / (1000 * 60 * 60));

        String formatted;
        if (hours > 0)
            formatted = String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, millis / 10);
        else
            formatted = String.format("%02d:%02d.%02d", minutes, seconds, millis / 10);

        return formatted;
    }
}
