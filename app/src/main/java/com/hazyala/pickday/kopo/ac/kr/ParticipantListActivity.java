package com.hazyala.pickday.kopo.ac.kr;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.hazyala.pickday.kopo.ac.kr.data.DummyDataSource;

import java.util.List;

public class ParticipantListActivity extends AppCompatActivity {

    public static final String EXTRA_ROOM_ID = "extra_room_id";

    private AppCompatButton btnBack;
    private TextView tvTitle;
    private TextView tvParticipantCount;
    private LinearLayout layoutParticipantList;
    private String roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant_list);

        initViews();
        readRoomData();
        renderParticipants();
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvParticipantCount = findViewById(R.id.tvParticipantCount);
        layoutParticipantList = findViewById(R.id.layoutParticipantList);
    }

    private void readRoomData() {
        roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);

        if (roomId == null || roomId.isEmpty()) {
            roomId = DummyDataSource.DEFAULT_ROOM_ID;
        }
    }

    private void renderParticipants() {
        DummyDataSource.MyMeetupRoom room = DummyDataSource.getMeetupRoomById(roomId);
        List<DummyDataSource.AvailabilityResponse> responses =
                DummyDataSource.getAvailabilityResponses(room.roomId);

        tvTitle.setText("참여자");
        tvParticipantCount.setText("전체 " + room.participantCount + "명");
        layoutParticipantList.removeAllViews();

        if (responses.isEmpty()) {
            layoutParticipantList.addView(createEmptyView());
            return;
        }

        for (DummyDataSource.AvailabilityResponse response : responses) {
            layoutParticipantList.addView(createParticipantRow(response));
        }
    }

    private View createParticipantRow(DummyDataSource.AvailabilityResponse response) {
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(62)
        ));
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(14), 0, dp(14), 0);
        row.setBackgroundResource(R.drawable.pickday_card_white);

        TextView avatar = new TextView(this);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(38), dp(38));
        avatarParams.setMargins(0, 0, dp(12), 0);
        avatar.setLayoutParams(avatarParams);
        avatar.setBackgroundResource(R.drawable.bg_participant_avatar);
        avatar.setGravity(Gravity.CENTER);
        avatar.setIncludeFontPadding(false);
        avatar.setText(getInitial(response.participantName));
        avatar.setTextColor(Color.parseColor("#5B4CDB"));
        avatar.setTextSize(14);
        avatar.setTypeface(null, Typeface.BOLD);

        TextView name = new TextView(this);
        name.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        name.setIncludeFontPadding(false);
        name.setText(response.participantName);
        name.setTextColor(Color.parseColor("#252538"));
        name.setTextSize(14);
        name.setTypeface(null, Typeface.BOLD);

        TextView status = new TextView(this);
        status.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        status.setIncludeFontPadding(false);
        status.setText(response.submitted ? "응답 완료" : "응답 대기");
        status.setTextColor(response.submitted
                ? Color.parseColor("#5B4CDB")
                : Color.parseColor("#8D8AA5"));
        status.setTextSize(12);
        status.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams rowParams = (LinearLayout.LayoutParams) row.getLayoutParams();
        rowParams.setMargins(0, 0, 0, dp(8));
        row.setLayoutParams(rowParams);

        row.addView(avatar);
        row.addView(name);
        row.addView(status);

        return row;
    }

    private View createEmptyView() {
        TextView emptyView = new TextView(this);
        emptyView.setText("아직 참여자 응답 정보가 없어요");
        emptyView.setTextColor(Color.parseColor("#8D8AA5"));
        emptyView.setTextSize(13);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setMinHeight(dp(180));
        emptyView.setBackgroundResource(R.drawable.pickday_card_white);
        return emptyView;
    }

    private String getInitial(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }

        return name.substring(0, 1);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
