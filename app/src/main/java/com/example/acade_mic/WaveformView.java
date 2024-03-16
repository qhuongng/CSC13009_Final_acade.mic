package com.example.acade_mic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

public class WaveformView extends View {

    private Paint paint = new Paint();
    private ArrayList<Float> amplitudes = new ArrayList<>();
    private ArrayList<RectF> spikes = new ArrayList<>();

    private float radius = 6f;
    private float w = 9f;
    private float d = 6f;

    private float sw = 0f;
    private float sh = 400f;

    private int maxSpikes = 0;

    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.rgb(244, 81, 30));

        sw = getResources().getDisplayMetrics().widthPixels;

        maxSpikes = (int) (sw / (w + d));
    }

    public void addAmplitude(float amp) {
        float norm = Math.min(amp / 7, 400);
        amplitudes.add(norm);

        spikes.clear();
        ArrayList<Float> amps = new ArrayList<>(amplitudes.subList(Math.max(amplitudes.size() - maxSpikes, 0), amplitudes.size()));
        for (int i = 0; i < amps.size(); i++) {
            float left = sw - i * (w + d);
            float top = sh / 2 - amps.get(i) / 2;
            float right = left + w;
            float bottom = top + amps.get(i);
            spikes.add(new RectF(left, top, right, bottom));
        }

        invalidate();
    }

    public ArrayList<Float> clear() {
        ArrayList<Float> amps = new ArrayList<>(amplitudes);
        amplitudes.clear();
        spikes.clear();
        invalidate();

        return amps;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (RectF rect : spikes) {
            canvas.drawRoundRect(rect, 6f, 6f, paint);
        }
    }
}
