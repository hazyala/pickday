package com.hazyala.pickday.kopo.ac.kr;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hazyala.pickday.kopo.ac.kr.data.DummyDataSource;

import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout layoutNotificationContainer;
    private String currentSection = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        initViews();
        loadNotifications();
        setListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        layoutNotificationContainer = findViewById(R.id.layoutNotificationContainer);
    }

    private void loadNotifications() {
        List<DummyDataSource.Notification> notifications =
                DummyDataSource.getNotifications();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (DummyDataSource.Notification notification : notifications) {
            if (!notification.section.equals(currentSection)) {
                currentSection = notification.section;
                addSectionTitle(inflater, currentSection);
            }

            View view = inflater.inflate(
                    R.layout.item_notification,
                    layoutNotificationContainer,
                    false
            );

            TextView tvNotificationDot =
                    view.findViewById(R.id.tvNotificationDot);

            TextView tvNotificationTitle =
                    view.findViewById(R.id.tvNotificationTitle);

            TextView tvNotificationRoom =
                    view.findViewById(R.id.tvNotificationRoom);

            TextView tvNotificationMessage =
                    view.findViewById(R.id.tvNotificationMessage);

            TextView tvNotificationTime =
                    view.findViewById(R.id.tvNotificationTime);

            tvNotificationDot.setTextColor(
                    Color.parseColor(notification.accentColor)
            );

            tvNotificationTitle.setText(notification.title);
            tvNotificationRoom.setText(notification.roomTitle);
            tvNotificationTime.setText(notification.time);

            if (notification.message == null || notification.message.isEmpty()) {
                tvNotificationMessage.setVisibility(View.GONE);
            } else {
                tvNotificationMessage.setText(notification.message);
                tvNotificationMessage.setVisibility(View.VISIBLE);
            }

            layoutNotificationContainer.addView(view);
        }
    }

    private void addSectionTitle(LayoutInflater inflater, String sectionTitle) {
        TextView view = (TextView) inflater.inflate(
                R.layout.item_notification_section,
                layoutNotificationContainer,
                false
        );

        view.setText(sectionTitle);
        layoutNotificationContainer.addView(view);
    }

    private void setListeners() {
        btnBack.setOnClickListener(v -> finish());
    }
}
