package com.hazyala.pickday.kopo.ac.kr;

import com.hazyala.pickday.kopo.ac.kr.model.MyMeetupRoom;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hazyala.pickday.kopo.ac.kr.ui.PickDayActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.hazyala.pickday.kopo.ac.kr.data.LocalMeetupRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InviteMembersActivity extends PickDayActivity {

    public static final String EXTRA_ROOM_ID = "extra_room_id";

    private TextView btnBack;
    private TextView btnRoomInfo;
    private TextView btnCopy;
    private TextView btnInviteDone;
    private TextView tvInviteLink;
    private TextView tvInviteRoomTitle;
    private TextView tvInviteDeadline;
    private TextView tvInviteParticipants;
    private String roomId;
    private String inviteLink;

    private LinearLayout btnKakaoShare;

    private SwitchCompat switchNotify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_members);

        initView();
        if (!setDummyData()) return;
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (roomId != null) setDummyData();
    }

    private void initView() {

        btnBack = findViewById(R.id.btnBack);
        btnRoomInfo = findViewById(R.id.btnRoomInfo);
        btnCopy = findViewById(R.id.btnCopy);
        btnInviteDone = findViewById(R.id.btnInviteDone);

        tvInviteLink = findViewById(R.id.tvInviteLink);
        tvInviteRoomTitle = findViewById(R.id.tvInviteRoomTitle);
        tvInviteDeadline = findViewById(R.id.tvInviteDeadline);
        tvInviteParticipants = findViewById(R.id.tvInviteParticipants);

        btnKakaoShare = findViewById(R.id.btnKakaoShare);

        switchNotify = findViewById(R.id.switchNotify);
    }

    private boolean setDummyData() {
        roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);

        if (roomId == null || roomId.isEmpty()) {
            roomId = LocalMeetupRepository.getCurrentDraftRoomId();
        }

        MyMeetupRoom room =
                LocalMeetupRepository.getMeetupRoomById(roomId);

        if (room == null) {
            Toast.makeText(this, getString(R.string.error_room_not_found), Toast.LENGTH_SHORT).show();
            finish(); return false;
        }
        inviteLink = LocalMeetupRepository.getInviteLink(roomId);

        tvInviteLink.setText(inviteLink);
        tvInviteRoomTitle.setText(room.title);
        tvInviteDeadline.setText(getString(R.string.room_deadline_datetime, formatDate(room.deadlineDateIso), room.deadlineTimeText));
        tvInviteParticipants.setText(getString(R.string.room_participants_capacity, room.participantCount, room.maxParticipants));

        ((TextView) findViewById(R.id.tvInviteCurrentCount)).setText(getString(R.string.people_count, room.participantCount));
        ((TextView) findViewById(R.id.tvInviteCurrentSummary)).setText(
                getString(R.string.response_submitted_count, LocalMeetupRepository.getRoomAvailabilitySummary(roomId).completedResponseCount));
        switchNotify.setChecked(room.notifyOnJoin);
        return true;
    }

    private void setListener() {

        btnBack.setOnClickListener(v -> {
            finish();
        });

        btnRoomInfo.setOnClickListener(v -> {

            Intent intent =
                    new Intent(InviteMembersActivity.this,
                            RoomDetailActivity.class);

            intent.putExtra(RoomDetailActivity.EXTRA_ROOM_ID, roomId);
            startActivity(intent);
        });

        btnCopy.setOnClickListener(v -> {

            ClipboardManager clipboardManager =
                    (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

            ClipData clipData =
                    ClipData.newPlainText("invite_link", inviteLink);

            if (clipboardManager != null) {

                clipboardManager.setPrimaryClip(clipData);

                Toast.makeText(
                        InviteMembersActivity.this,
                        getString(R.string.invite_members_set_listener_text),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        btnKakaoShare.setOnClickListener(v -> {

            Intent shareIntent = new Intent(Intent.ACTION_SEND);

            shareIntent.setType("text/plain");

            shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    getString(R.string.invite_share_message, inviteLink)
            );

            startActivity(Intent.createChooser(
                    shareIntent,
                    getString(R.string.action_share)
            ));
        });

        switchNotify.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    LocalMeetupRepository.getMeetupRoomById(roomId).notifyOnJoin = isChecked;
                    if (isChecked) {

                        Toast.makeText(
                                this,
                                getString(R.string.invite_members_set_listener_text_4),
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        Toast.makeText(
                                this,
                                getString(R.string.invite_members_set_listener_text_5),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        btnInviteDone.setOnClickListener(v -> {

            Intent intent =
                    new Intent(InviteMembersActivity.this,
                            HomeActivity.class);

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            );

            startActivity(intent);

            finish();
        });
    }

    private String formatDate(String dateIso) {
        if (dateIso == null || dateIso.isEmpty()) {
            return getString(R.string.schedule_undecided);
        }

        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN);
            Date date = parser.parse(dateIso);
            SimpleDateFormat formatter = new SimpleDateFormat("M.d (E)", Locale.KOREAN);
            return formatter.format(date);
        } catch (ParseException e) {
            return dateIso;
        }
    }
}
