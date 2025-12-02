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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SeatReservationFragment extends Fragment {

    private static final long CHECK_INTERVAL_MS = 60 * 1000; // 1 minute
    private static final long NO_SHOW_GRACE_PERIOD_MS = 30 * 60 * 1000; // 30 minutes
    private static final long BANNER_SWITCH_INTERVAL_MS = 2000; // 2 seconds

    private TextView seatStatusText;
    private Spinner floorSpinner, seatSpinner, timeslotSpinner;
    private Button reserveButton, cancelButton, checkinButton, checkoutButton;
    private View selectionLayout;
    private ViewPager2 announcementBanner;
    private BannerAdapter bannerAdapter;
    private Handler bannerHandler;
    private Runnable bannerRunnable;
    private int currentPage = 0;

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
        setupBanner();
        setupListeners();
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        graphicalSeatLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        int seatNumber = data.getIntExtra("SELECTED_SEAT_NUMBER", -1);
                        if (seatNumber != -1) {
                            updateSpinnerSelection(seatSpinner, seatNumber);
                        }
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        populateSpinners();
        updateUIState();
        startPeriodicChecks();
        startBannerAutoScroll();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopPeriodicChecks();
        stopBannerAutoScroll();
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
        announcementBanner = view.findViewById(R.id.announcement_banner);
    }

    private void setupBanner() {
        // Using banner images for the announcement banner
        int[] bannerImages = {
                R.drawable.banner1,
                R.drawable.banner2,
                R.drawable.banner3,
                R.drawable.banner4
        };

        bannerAdapter = new BannerAdapter(bannerImages);
        announcementBanner.setAdapter(bannerAdapter);
        
        // Set up auto-scrolling
        bannerHandler = new Handler(Looper.getMainLooper());
    }

    private void startBannerAutoScroll() {
        if (bannerRunnable == null) {
            bannerRunnable = new Runnable() {
                @Override
                public void run() {
                    if (bannerAdapter.getItemCount() == 0) return;
                    
                    currentPage = (currentPage + 1) % bannerAdapter.getItemCount();
                    announcementBanner.setCurrentItem(currentPage, true);
                    bannerHandler.postDelayed(this, BANNER_SWITCH_INTERVAL_MS);
                }
            };
        }
        bannerHandler.postDelayed(bannerRunnable, BANNER_SWITCH_INTERVAL_MS);
    }

    private void stopBannerAutoScroll() {
        if (bannerHandler != null && bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }

    private void setupListeners() {
        reserveButton.setOnClickListener(v -> handleReservation());
        cancelButton.setOnClickListener(v -> handleCancellation());
        checkinButton.setOnClickListener(v -> handleCheckIn());
        checkoutButton.setOnClickListener(v -> handleCheckOut());
        graphicalSelectButton.setOnClickListener(v -> navigateToGraphicalActivity());
    }

    private void populateSpinners() {
        if (getContext() == null) return;

        ArrayAdapter<String> floorAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, getFloors());
        floorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        floorSpinner.setAdapter(floorAdapter);

        ArrayAdapter<Integer> seatAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, getSeats());
        seatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seatSpinner.setAdapter(seatAdapter);

        executor.execute(() -> {
            List<TimeSlot> allTimeSlots = db.timeSlotDao().getAllTimeSlots();
            List<TimeSlot> availableTimeSlots = new ArrayList<>();
            Date now = new Date();
            Date today = getStartOfDay(now);

            for (TimeSlot ts : allTimeSlots) {
                Date startTime = getDateForTimeString(ts.startTime, today);
                Date endTime = getDateForTimeString(ts.endTime, today);
                
                // A time slot is available if:
                // 1. It hasn't ended yet (current time is before end time)
                // 2. Either it hasn't started yet (current time is before start time) OR
                //    it's currently ongoing (current time is after or equal to start time)
                if (endTime != null && now.before(endTime) && 
                   (startTime != null && (now.before(startTime) || !startTime.after(now)))) {
                    availableTimeSlots.add(ts);
                }
            }

            handler.post(() -> {
                if (getContext() != null) {
                    if (availableTimeSlots.isEmpty()) {
                        List<String> noSlotsMessage = new ArrayList<>();
                        noSlotsMessage.add("今日暂无可约时段");
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, noSlotsMessage);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        timeslotSpinner.setAdapter(adapter);
                        timeslotSpinner.setEnabled(false);
                        reserveButton.setEnabled(false);
                    } else {
                        ArrayAdapter<TimeSlot> timeSlotAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, availableTimeSlots);
                        timeSlotAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        timeslotSpinner.setAdapter(timeSlotAdapter);
                        timeslotSpinner.setEnabled(true);
                        reserveButton.setEnabled(true);
                    }
                }
            });
        });
    }

    private void handleReservation() {
        if (floorSpinner.getSelectedItem() == null || seatSpinner.getSelectedItem() == null || !(timeslotSpinner.getSelectedItem() instanceof TimeSlot)) {
            Toast.makeText(getContext(), "请完成所有选择", Toast.LENGTH_SHORT).show();
            return;
        }

        TimeSlot selectedTimeSlot = (TimeSlot) timeslotSpinner.getSelectedItem();
        int selectedSeatNum = (int) seatSpinner.getSelectedItem();
        int selectedFloor = floorSpinner.getSelectedItemPosition() + 1;

        executor.execute(() -> {
            Seat seat = db.seatDao().findSeatByFloorAndNumber(selectedFloor, selectedSeatNum);
            if (seat == null) {
                handler.post(() -> Toast.makeText(getContext(), "选择的座位无效", Toast.LENGTH_SHORT).show());
                return;
            }

            Date today = getStartOfDay(new Date());

            List<ReservationRecord> existingReservations = db.reservationRecordDao().findReservationsForSeat(seat.id, selectedTimeSlot.id, today);
            if (!existingReservations.isEmpty()) {
                handler.post(() -> Toast.makeText(getContext(), "此座位在该时段已被预订。", Toast.LENGTH_LONG).show());
                return;
            }

            ReservationRecord userExistingReservation = db.reservationRecordDao().findReservationByUserAndDate(studentId, today);
            if (userExistingReservation != null) {
                handler.post(() -> Toast.makeText(getContext(), "您今天已经有预约了。", Toast.LENGTH_LONG).show());
                return;
            }

            long timestamp = System.currentTimeMillis();
            ReservationRecord newRecord = new ReservationRecord(studentId, seat.id, selectedTimeSlot.id, today, timestamp);
            db.reservationRecordDao().insert(newRecord);
            handler.post(() -> {
                Toast.makeText(getContext(), "预约成功!", Toast.LENGTH_SHORT).show();
                updateUIState();
            });
        });
    }

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
        boolean shouldUpdateUi = false;

        // 1. Check for no-shows
        List<ReservationRecord> reservations = db.reservationRecordDao().getReservationsByDate(today);
        for (ReservationRecord r : reservations) {
            StudyRecord sr = db.studyRecordDao().findActiveStudyRecordByStudentAndSeat(r.studentId, r.seatId);
            if (sr == null) { // Not checked in
                TimeSlot ts = db.timeSlotDao().getTimeSlotById(r.timeSlotId);
                if (ts == null) continue;

                Date slotStartTime = getDateForTimeString(ts.startTime, today);
                long reservationTime = r.reservationTimestamp;

                if (slotStartTime != null) {
                    long checkInDeadline;
                    if (reservationTime < slotStartTime.getTime()) {
                        checkInDeadline = slotStartTime.getTime() + NO_SHOW_GRACE_PERIOD_MS;
                    } else {
                        checkInDeadline = reservationTime + NO_SHOW_GRACE_PERIOD_MS;
                    }

                    if (now.getTime() > checkInDeadline) {
                        db.reservationRecordDao().deleteReservation(r.studentId, r.seatId, r.timeSlotId, r.reservationDate);
                        if (r.studentId == studentId) {
                            shouldUpdateUi = true;
                            handler.post(() -> {
                                if (isResumed() && getContext() != null) {
                                    new AlertDialog.Builder(getContext())
                                            .setTitle("预约已过期")
                                            .setMessage("因未及时签到，您的预约已过期")
                                            .setCancelable(false)
                                            .setPositiveButton("确定", (dialog, which) -> updateUIState())
                                            .show();
                                }
                            });
                        }
                    }
                }
            }
        }

        // 2. Check for overdue checkouts
        List<StudyRecord> activeStudies = db.studyRecordDao().getAllActiveStudyRecords();
        for (StudyRecord sr : activeStudies) {
            ReservationRecord r = db.reservationRecordDao().findReservationByUserAndDate(sr.studentId, today);
            if (r != null) {
                TimeSlot ts = db.timeSlotDao().getTimeSlotById(r.timeSlotId);
                if (ts == null) continue;

                Date endTime = getDateForTimeString(ts.endTime, today);
                if (endTime != null && now.after(endTime)) {
                    sr.endTime = endTime;
                    db.studyRecordDao().update(sr);
                    db.seatDao().updateSeatStatus(sr.seatId, "available");
                    db.reservationRecordDao().deleteReservation(r.studentId, r.seatId, r.timeSlotId, r.reservationDate);

                    if (sr.studentId == studentId) {
                        shouldUpdateUi = true;
                        handler.post(() -> {
                            if (isResumed() && getContext() != null) {
                                new AlertDialog.Builder(getContext())
                                        .setTitle("预约已结束")
                                        .setMessage("您的预约已结束")
                                        .setCancelable(false)
                                        .setPositiveButton("确定", (dialog, which) -> updateUIState())
                                        .show();
                            }
                        });
                    }
                }
            }
        }

        if (!shouldUpdateUi) {
           // handler.post(this::updateUIState); // No longer needed as dialogs handle their own updates
        }
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
                if (reservation != null) {
                    db.reservationRecordDao().deleteReservation(reservation.studentId, reservation.seatId, reservation.timeSlotId, reservation.reservationDate);
                }

                handler.post(() -> {
                    Toast.makeText(getContext(), "签退成功!", Toast.LENGTH_SHORT).show();
                    updateUIState();
                });
            }
        });
    }

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

    private void updateUIState() {
        executor.execute(() -> {
            Date today = getStartOfDay(new Date());
            ReservationRecord reservation = db.reservationRecordDao().findReservationByUserAndDate(studentId, today);
            StudyRecord studyRecord = db.studyRecordDao().getCurrentStudyRecordForStudent(studentId);

            handler.post(() -> {
                if (getContext() == null) return;
                
                if (studyRecord != null) {
                    selectionLayout.setVisibility(View.GONE);
                    reserveButton.setVisibility(View.GONE);
                    cancelButton.setVisibility(View.GONE);
                    checkinButton.setVisibility(View.GONE);
                    checkoutButton.setVisibility(View.VISIBLE);
                    seatStatusText.setText("状态: 已签到，正在学习");
                } else if (reservation != null) {
                    selectionLayout.setVisibility(View.GONE);
                    reserveButton.setVisibility(View.GONE);
                    cancelButton.setVisibility(View.VISIBLE);
                    checkinButton.setVisibility(View.VISIBLE);
                    checkoutButton.setVisibility(View.GONE);
                    seatStatusText.setText("状态: 已预约，等待签到");
                    checkinButton.setEnabled(true);
                } else {
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

    private List<String> getFloors() {
        List<String> floors = new ArrayList<>();
        floors.add("一楼（开放至23:00）");
        floors.add("二楼（开放至22:00）");
        floors.add("三楼（开放至23:00）");
        floors.add("四楼（开放至23:00）");
        floors.add("五楼（开放至22:00）");
        floors.add("六楼（开放至22:00）");
        return floors;
    }

    private List<Integer> getSeats() {
        List<Integer> seats = new ArrayList<>();
        for (int i = 1; i <= 20; i++) seats.add(i);
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
        int floor = floorSpinner.getSelectedItemPosition() + 1;

        Intent intent = new Intent(getActivity(), GraphicalSeatSelectionActivity.class);
        intent.putExtra("FLOOR_NUMBER", floor);
        graphicalSeatLauncher.launch(intent);
    }

    private void updateSpinnerSelection(Spinner spinner, int value) {
        ArrayAdapter<Integer> adapter = (ArrayAdapter<Integer>) spinner.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).equals(value)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }
}