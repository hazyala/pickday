package com.hazyala.pickday.kopo.ac.kr;

import com.hazyala.pickday.kopo.ac.kr.model.AvailabilityResponse;
import com.hazyala.pickday.kopo.ac.kr.model.MyMeetupRoom;

import android.graphics.Color;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hazyala.pickday.kopo.ac.kr.ui.PickDayActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.hazyala.pickday.kopo.ac.kr.data.LocalMeetupRepository;

import java.util.List;

public class ParticipantListActivity extends PickDayActivity {

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
        if (LocalMeetupRepository.getMeetupRoomById(roomId) == null) {
            Toast.makeText(this, getString(R.string.error_room_not_found), Toast.LENGTH_SHORT).show();
            finish(); return;
        }
        renderParticipants();
        findViewById(R.id.btnAddParticipant).setOnClickListener(v -> showAddParticipant());
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
            roomId = LocalMeetupRepository.DEFAULT_ROOM_ID;
        }
    }

    private void renderParticipants() {
        MyMeetupRoom room = LocalMeetupRepository.getMeetupRoomById(roomId);
        List<AvailabilityResponse> responses =
                LocalMeetupRepository.getAvailabilityResponses(room.roomId);

        tvTitle.setText(getString(R.string.participants_title));
        tvParticipantCount.setText(getString(R.string.room_participants_capacity, room.participantCount, room.maxParticipants));
        ((TextView) findViewById(R.id.tvParticipantHelp)).setText(
                !room.confirmedDateIso.isEmpty() || LocalMeetupRepository.isDeadlinePassed(room)
                        ? R.string.participants_closed_help : R.string.participants_edit_help);
        View addButton = findViewById(R.id.btnAddParticipant);
        addButton.setEnabled(room.confirmedDateIso.isEmpty() && !LocalMeetupRepository.isDeadlinePassed(room) && room.participantCount < room.maxParticipants);
        addButton.setAlpha(addButton.isEnabled() ? 1f : 0.5f);
        layoutParticipantList.removeAllViews();

        if (responses.isEmpty()) {
            layoutParticipantList.addView(createEmptyView());
            return;
        }

        for (AvailabilityResponse response : responses) {
            layoutParticipantList.addView(createParticipantRow(response));
        }
    }

    private View createParticipantRow(AvailabilityResponse response) {
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
        MyMeetupRoom room = LocalMeetupRepository.getMeetupRoomById(roomId);
        boolean closed = !room.confirmedDateIso.isEmpty() || LocalMeetupRepository.isDeadlinePassed(room);
        status.setText(closed ? response.submitted ? getString(R.string.response_completed) : getString(R.string.participant_list_create_participant_row_text)
                : response.submitted ? getString(R.string.participant_list_create_participant_row_text_2) : getString(R.string.participant_list_create_participant_row_text_3));
        status.setTextColor(response.submitted
                ? Color.parseColor("#5B4CDB")
                : Color.parseColor("#8D8AA5"));
        status.setTextSize(12);
        status.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams rowParams = (LinearLayout.LayoutParams) row.getLayoutParams();
        rowParams.setMargins(0, 0, 0, dp(8));
        row.setLayoutParams(rowParams);

        row.setEnabled(!closed);
        row.setClickable(!closed);
        row.setFocusable(true);
        row.setContentDescription(getString(R.string.participant_response_description, response.participantName));
        row.setOnClickListener(v -> {
            if (!LocalMeetupRepository.getMeetupRoomById(roomId).confirmedDateIso.isEmpty()) {
                Toast.makeText(this, getString(R.string.participant_list_create_participant_row_text_4), Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, ResponseSelectionActivity.class);
            intent.putExtra(ResponseSelectionActivity.EXTRA_ROOM_ID, roomId);
            intent.putExtra(ResponseSelectionActivity.EXTRA_PARTICIPANT_NAME, response.participantName);
            startActivity(intent);
        });
        row.addView(avatar);
        row.addView(name);
        row.addView(status);

        return row;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (roomId != null && LocalMeetupRepository.getMeetupRoomById(roomId) != null) renderParticipants();
    }

    private void showAddParticipant() {
        EditText input = new EditText(this);
        input.setHint(getString(R.string.participant_list_show_add_participant_text));
        input.setSingleLine(true);
        input.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(20)});
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(getString(R.string.participant_list_add_participant_text))
                .setMessage(getString(R.string.participant_list_show_add_participant_text_2))
                .setView(input).setNegativeButton(getString(R.string.action_cancel), null).setPositiveButton(getString(R.string.action_add), null).create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            try {
                LocalMeetupRepository.addParticipant(roomId, input.getText().toString());
                renderParticipants();
                dialog.dismiss();
            } catch (IllegalArgumentException error) {
                input.setError(error.getMessage());
            }
        }));
        dialog.show();
    }

    private View createEmptyView() {
        TextView emptyView = new TextView(this);
        emptyView.setText(getString(R.string.participant_list_create_empty_view_text));
        emptyView.setTextColor(Color.parseColor("#8D8AA5"));
        emptyView.setTextSize(13);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setLineSpacing(dp(4), 1f);
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
