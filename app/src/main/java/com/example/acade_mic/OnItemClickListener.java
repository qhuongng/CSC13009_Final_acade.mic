package com.example.acade_mic;

import android.os.Bundle;

public interface OnItemClickListener {
    void onResume(Bundle savedInstanceState);

    void onItemClickListener(int position);
    void onItemLongClickListener(int position);
}
