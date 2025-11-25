package com.example.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class SeatReservationFragment extends Fragment {

    private TextView seatStatusText;
    private Button reserveButton;
    private Button checkInButton;
    private boolean isReserved = false;
    private boolean isCheckedIn = false;

    public SeatReservationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seat_reservation, container, false);

        seatStatusText = view.findViewById(R.id.seat_status);
        reserveButton = view.findViewById(R.id.reserve_button);
        checkInButton = view.findViewById(R.id.checkin_button);

        updateUI();

        reserveButton.setOnClickListener(v -> {
            if (!isReserved) {
                isReserved = true;
                Toast.makeText(getContext(), "座位预约成功！", Toast.LENGTH_SHORT).show();
            } else {
                isReserved = false;
                isCheckedIn = false;
                Toast.makeText(getContext(), "已取消预约", Toast.LENGTH_SHORT).show();
            }
            updateUI();
        });

        checkInButton.setOnClickListener(v -> {
            if (isReserved && !isCheckedIn) {
                isCheckedIn = true;
                Toast.makeText(getContext(), "签到成功！", Toast.LENGTH_SHORT).show();
            } else if (!isReserved) {
                Toast.makeText(getContext(), "请先预约座位", Toast.LENGTH_SHORT).show();
            } else if (isCheckedIn) {
                Toast.makeText(getContext(), "您已经签到了", Toast.LENGTH_SHORT).show();
            }
            updateUI();
        });

        return view;
    }

    private void updateUI() {
        if (!isReserved) {
            seatStatusText.setText("状态: 未预约");
            reserveButton.setText("预约座位");
            checkInButton.setEnabled(false);
        } else if (isReserved && !isCheckedIn) {
            seatStatusText.setText("状态: 已预约，未签到");
            reserveButton.setText("取消预约");
            checkInButton.setEnabled(true);
            checkInButton.setText("签到");
        } else if (isReserved && isCheckedIn) {
            seatStatusText.setText("状态: 已签到");
            reserveButton.setText("取消预约");
            checkInButton.setText("已签到");
            checkInButton.setEnabled(false);
        }
    }
}
