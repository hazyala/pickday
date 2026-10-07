package com.hazyala.pickday.kopo.ac.kr.ui;

import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public abstract class PickDayActivity extends AppCompatActivity {
    @Override
    public void setContentView(int layoutResId) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        super.setContentView(layoutResId);
        View root = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        int left = root.getPaddingLeft(), top = root.getPaddingTop();
        int right = root.getPaddingRight(), bottom = root.getPaddingBottom();
        // 시스템 바와 키보드 중 더 큰 하단 영역을 확보해 입력·완료 버튼을 가리지 않습니다.
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(left + bars.left, top + bars.top,
                    right + bars.right, bottom + Math.max(bars.bottom, ime.bottom));
            return insets;
        });
        WindowCompat.getInsetsController(getWindow(), root).setAppearanceLightStatusBars(true);
        WindowCompat.getInsetsController(getWindow(), root).setAppearanceLightNavigationBars(true);
        ViewCompat.requestApplyInsets(root);
    }
}
