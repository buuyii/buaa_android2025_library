package com.example.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SeatReservationFragment extends Fragment {

    private static final long CHECK_INTERVAL_MS = 60 * 1000; // 1 minute
    private static final long NO_SHOW_GRACE_PERIOD_MS = 30 * 60 * 1000; // 30 minutes

    private TextView seatStatusText;
    private Spinner floorSpinner, seatSpinner, timeslotSpinner;
    private Button reserveButton, cancelButton, checkinButton, checkoutButton;
    private View selectionLayout;

    private AppDataBase db;
    private final int studentId = 1; // Hardcoded student ID

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private ActivityResultLauncher<Intent> graphicalSeatLauncher;
    private Button graphicalSelectButton;
    private Runnable seatCheckRunnable;

    public SeatReservationFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seat_reservation, container, false);
        db = AppDataBase.getInstance(getContext());
        initializeViews(view);
        setupListeners();
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 2. 注册一个回调来处理Activity返回的结果
        graphicalSeatLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // 当GraphicalSeatSelectionActivity关闭后，这里会被调用
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        int seatNumber = data.getIntExtra("SELECTED_SEAT_NUMBER", -1);
                        if (seatNumber != -1) {
                            // 更新UI
                            updateSpinnerSelection(seatSpinner, seatNumber);
                        }
                    }
                });
    }
    @Override
    public void onResume() {
        super.onResume();
        updateUIState();
        startPeriodicChecks();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopPeriodicChecks();
    }

    private void initializeViews(View view) {
        seatStatusText = view.findViewById(R.id.seat_status);
        floorSpinner = view.findViewById(R.id.floor_spinner);
        seatSpinner = view.findViewById(R.id.seat_spinner);
        timeslotSpinner = view.findViewById(R.id.timeslot_spinner);
        reserveButton = view.findViewById(R.id.reserve_button);
        cancelButton = view.findViewById(R.id.cancel_button);
        checkinButton = view.findViewById(R.id.checkin_button);
        checkoutButton = view.findViewById(R.id.checkout_button);
        selectionLayout = view.findViewById(R.id.selection_layout);
        graphicalSelectButton = view.findViewById(R.id.graphical_select_button);

        populateSpinners();
    }

    private void setupListeners() {
        reserveButton.setOnClickListener(v -> handleReservation());
        cancelButton.setOnClickListener(v -> handleCancellation());
        checkinButton.setOnClickListener(v -> handleCheckIn());
        checkoutButton.setOnClickListener(v -> handleCheckOut());
        graphicalSelectButton.setOnClickListener(v -> navigateToGraphicalActivity());
    }
    
    // ... (populateSpinners, handleReservation, etc. remain the same) ...

    private void startPeriodicChecks() {
        seatCheckRunnable = () -> {
            executor.execute(this::runSeatReleaseChecks);
            handler.postDelayed(seatCheckRunnable, CHECK_INTERVAL_MS);
        };
        handler.post(seatCheckRunnable);
    }

    private void stopPeriodicChecks() {
        handler.removeCallbacks(seatCheckRunnable);
    }

    private void runSeatReleaseChecks() {
        Date now = new Date();
        Date today = getStartOfDay(now);
        
        // 1. Check for no-shows
        List<ReservationRecord> reservations = db.reservationRecordDao().getReservationsByDate(today);
        for (ReservationRecord r : reservations) {
            StudyRecord sr = db.studyRecordDao().findActiveStudyRecordByStudentAndSeat(r.studentId, r.seatId);
            if (sr == null) { // Not checked in
                TimeSlot ts = db.timeSlotDao().getTimeSlotById(r.timeSlotId);
                Date deadline = getDateForTimeString(ts.startTime, today);
                if (deadline != null && now.getTime() > deadline.getTime() + NO_SHOW_GRACE_PERIOD_MS) {
                    db.reservationRecordDao().deleteReservation(r.studentId, r.seatId, r.timeSlotId, r.reservationDate);
                    // Seat was never 'occupied', so no status change needed
                }
            }
        }

        // 2. Check for overdue checkouts for reservations
        List<StudyRecord> activeStudies = db.studyRecordDao().getAllActiveStudyRecords();
        for (StudyRecord sr : activeStudies) {
            ReservationRecord r = db.reservationRecordDao().findReservationByUserAndDate(sr.studentId, today);
            if (r != null) {
                TimeSlot ts = db.timeSlotDao().getTimeSlotById(r.timeSlotId);
                Date endTime = getDateForTimeString(ts.endTime, today);
                if (endTime != null && now.after(endTime)) {
                    sr.endTime = endTime; // Set end time to the slot's end
                    db.studyRecordDao().update(sr);
                    db.seatDao().updateSeatStatus(sr.seatId, "available");
                    db.reservationRecordDao().deleteReservation(r.studentId, r.seatId, r.timeSlotId, r.reservationDate);
                }
            } else {
                // Handle walk-in users who have no reservation
                // Auto-checkout at midnight
                Calendar cal = Calendar.getInstance();
                cal.setTime(now);
                if (cal.get(Calendar.HOUR_OF_DAY) == 23 && cal.get(Calendar.MINUTE) >= 59) {
                    sr.endTime = now;
                    db.studyRecordDao().update(sr);
                    db.seatDao().updateSeatStatus(sr.seatId, "available");
                }
            }
        }
        
        // Refresh UI if the current user might be affected
        handler.post(this::updateUIState);
    }

    // Helper to parse time string like "17:00" into a Date object for a specific day
    private Date getDateForTimeString(String time, Date day) {
        try {
            String[] parts = time.split(":");
            Calendar cal = Calendar.getInstance();
            cal.setTime(day);
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
            cal.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        } catch (Exception e) {
            return null;
        }
    }

    // ... (rest of the methods like updateUIState, handleReservation, handleCheckIn, etc.)
    private void populateSpinners() {
        // Floors
        ArrayAdapter<Integer> floorAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, getFloors());
        floorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        floorSpinner.setAdapter(floorAdapter);

        // Seats
        ArrayAdapter<Integer> seatAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, getSeats());
        seatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seatSpinner.setAdapter(seatAdapter);

        // TimeSlots
        executor.execute(() -> {
            List<TimeSlot> timeSlots = db.timeSlotDao().getAllTimeSlots();
            handler.post(() -> {
                ArrayAdapter<TimeSlot> timeSlotAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, timeSlots);
                timeSlotAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                timeslotSpinner.setAdapter(timeSlotAdapter);
            });
        });
    }

    private void updateUIState() {
        executor.execute(() -> {
            Date today = getStartOfDay(new Date());
            ReservationRecord reservation = db.reservationRecordDao().findReservationByUserAndDate(studentId, today);
            StudyRecord studyRecord = db.studyRecordDao().getCurrentStudyRecordForStudent(studentId);

            handler.post(() -> {
                if (studyRecord != null) {
                    // State: Checked-in
                    selectionLayout.setVisibility(View.GONE);
                    reserveButton.setVisibility(View.GONE);
                    cancelButton.setVisibility(View.GONE);
                    checkinButton.setVisibility(View.GONE);
                    checkoutButton.setVisibility(View.VISIBLE);
                    seatStatusText.setText("状态: 已签到，正在学习");
                } else if (reservation != null) {
                    // State: Reserved
                    selectionLayout.setVisibility(View.GONE);
                    reserveButton.setVisibility(View.GONE);
                    cancelButton.setVisibility(View.VISIBLE);
                    checkinButton.setVisibility(View.VISIBLE);
                    checkoutButton.setVisibility(View.GONE);
                    seatStatusText.setText("状态: 已预约，等待签到");

                    checkinButton.setEnabled(true);
                } else {
                    // State: No reservation
                    selectionLayout.setVisibility(View.VISIBLE);
                    reserveButton.setVisibility(View.VISIBLE);
                    cancelButton.setVisibility(View.GONE);
                    checkinButton.setVisibility(View.GONE);
                    checkoutButton.setVisibility(View.GONE);
                    seatStatusText.setText("状态: 未预约");
                }
            });
        });
    }

    private void handleReservation() {

        if (floorSpinner.getSelectedItem() == null || seatSpinner.getSelectedItem() == null || timeslotSpinner.getSelectedItem() == null) {
            Toast.makeText(getContext(), "请完成所有选择", Toast.LENGTH_SHORT).show();
            return;
        }

        TimeSlot selectedTimeSlot = (TimeSlot) timeslotSpinner.getSelectedItem();
        int selectedSeatNum = (int) seatSpinner.getSelectedItem();
        int selectedFloor = (int) floorSpinner.getSelectedItem();

        if (selectedTimeSlot == null) {
            Toast.makeText(getContext(), "Please select a time slot", Toast.LENGTH_SHORT).show();
            return;
        }
        
        executor.execute(() -> {
            Seat seat = db.seatDao().findSeatByFloorAndNumber(selectedFloor, selectedSeatNum);
            if (seat == null) {
                handler.post(() -> Toast.makeText(getContext(), "Selected seat is invalid", Toast.LENGTH_SHORT).show());
                return;
            }

            Date today = getStartOfDay(new Date());

            List<ReservationRecord> existingReservations = db.reservationRecordDao().findReservationsForSeat(seat.id, selectedTimeSlot.id, today);
            if (!existingReservations.isEmpty()) {
                handler.post(() -> Toast.makeText(getContext(), "This seat is already reserved for the selected time slot.", Toast.LENGTH_LONG).show());
                return;
            }
            
            ReservationRecord userExistingReservation = db.reservationRecordDao().findReservationByUserAndDate(studentId, today);
            if (userExistingReservation != null) {
                 handler.post(() -> Toast.makeText(getContext(), "You already have a reservation for today.", Toast.LENGTH_LONG).show());
                return;
            }

            ReservationRecord newRecord = new ReservationRecord(studentId, seat.id, selectedTimeSlot.id, today);
            db.reservationRecordDao().insert(newRecord);
            handler.post(() -> {
                Toast.makeText(getContext(), "Reservation successful!", Toast.LENGTH_SHORT).show();
                updateUIState();
            });
        });
    }

    private void handleCancellation() {
        executor.execute(() -> {
            Date today = getStartOfDay(new Date());
            ReservationRecord reservation = db.reservationRecordDao().findReservationByUserAndDate(studentId, today);
            if (reservation != null) {
                db.reservationRecordDao().deleteReservation(reservation.studentId, reservation.seatId, reservation.timeSlotId, reservation.reservationDate);
                handler.post(() -> {
                    Toast.makeText(getContext(), "预约已取消", Toast.LENGTH_SHORT).show();
                    updateUIState();
                });
            }
        });
    }

    private void handleCheckIn() {
        executor.execute(() -> {
            Date today = getStartOfDay(new Date());
            ReservationRecord reservation = db.reservationRecordDao().findReservationByUserAndDate(studentId, today);
            if (reservation != null) {
                StudyRecord newStudyRecord = new StudyRecord(studentId, reservation.seatId, new Date());
                db.studyRecordDao().insert(newStudyRecord);
                db.seatDao().updateSeatStatus(reservation.seatId, "occupied");
                handler.post(() -> {
                    Toast.makeText(getContext(), "签到成功!", Toast.LENGTH_SHORT).show();
                    updateUIState();
                });
            }
        });
    }

    private void handleCheckOut() {
        executor.execute(() -> {
            StudyRecord currentStudy = db.studyRecordDao().getCurrentStudyRecordForStudent(studentId);
            if (currentStudy != null) {
                currentStudy.endTime = new Date();
                db.studyRecordDao().update(currentStudy);
                db.seatDao().updateSeatStatus(currentStudy.seatId, "available");

                Date today = getStartOfDay(new Date());
                ReservationRecord reservation = db.reservationRecordDao().findReservationByUserAndDate(studentId, today);
                if(reservation != null) {
                    db.reservationRecordDao().deleteReservation(reservation.studentId, reservation.seatId, reservation.timeSlotId, reservation.reservationDate);
                }

                handler.post(() -> {
                    Toast.makeText(getContext(), "签退成功!", Toast.LENGTH_SHORT).show();
                    updateUIState();
                });
            }
        });
    }

    private List<Integer> getFloors() {
        List<Integer> floors = new ArrayList<>();
        for (int i = 1; i <= 6; i++) floors.add(i);
        return floors;
    }

    private List<Integer> getSeats() {
        List<Integer> seats = new ArrayList<>();
        for (int i = 1; i <= 100; i++) seats.add(i);
        return seats;
    }

    private Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private void navigateToGraphicalActivity() {
        if (floorSpinner.getSelectedItem() == null) {
            Toast.makeText(getContext(), "请先选择楼层", Toast.LENGTH_SHORT).show();
            return;
        }
        int floor = (int) floorSpinner.getSelectedItem();

        // 3. 创建Intent并启动Activity
        Intent intent = new Intent(getActivity(), GraphicalSeatSelectionActivity.class);
        intent.putExtra("FLOOR_NUMBER", floor); // 传递楼层号

        graphicalSeatLauncher.launch(intent); // 使用launcher启动
    }
    private void updateSpinnerSelection(Spinner spinner, int value) {
        ArrayAdapter<Integer> adapter = (ArrayAdapter<Integer>) spinner.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i) == value) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }

}