package com.example.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import java.util.Collections;
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
    private TextView seatAvailabilityInline; 
    private TextView seatAvailabilityBelow;  

    private int pendingSeatSelection = -1; // To hold the seat number from graphical selection

    public SeatReservationFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seat_reservation, container, false);
        db = AppDataBase.getInstance(getContext());
        initializeViews(view);
        setupBanner();
        setupListeners();
        populateFloorSpinner();
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
                        int floorNumber = data.getIntExtra("SELECTED_FLOOR_NUMBER", -1);

                        if (seatNumber != -1 && floorNumber != -1) {
                            int floorPosition = floorNumber - 1;

                            if (floorSpinner.getSelectedItemPosition() == floorPosition) {
                                updateSeatSpinner(floorNumber, () -> updateSpinnerSelection(seatSpinner, seatNumber));
                            } else {
                                this.pendingSeatSelection = seatNumber;
                                floorSpinner.setSelection(floorPosition);
                            }
                        }
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAvailableTimeSlots();
        updateUIState();
        startPeriodicChecks();
        startBannerAutoScroll();
        updateSeatAvailability();
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
        seatAvailabilityInline = view.findViewById(R.id.seat_availability_inline);
        seatAvailabilityBelow = view.findViewById(R.id.seat_availability_below);
    }

    private void setupBanner() {
        int[] bannerImages = { R.drawable.banner1, R.drawable.banner2, R.drawable.banner3, R.drawable.banner4 };
        bannerAdapter = new BannerAdapter(bannerImages);
        announcementBanner.setAdapter(bannerAdapter);
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

        floorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selectedFloor = position + 1;
                if (pendingSeatSelection != -1) {
                    final int seatToSelect = pendingSeatSelection;
                    pendingSeatSelection = -1; // Reset after capturing
                    updateSeatSpinner(selectedFloor, () -> updateSpinnerSelection(seatSpinner, seatToSelect));
                } else {
                    updateSeatSpinner(selectedFloor, null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (getContext() != null) {
                    seatSpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new ArrayList<Integer>()));
                }
            }
        });
    }
    
    private void populateFloorSpinner() {
        if (getContext() == null) return;
        ArrayAdapter<String> floorAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, getFloors());
        floorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        floorSpinner.setAdapter(floorAdapter);
    }
    
    private void updateAvailableTimeSlots() {
        if (getContext() == null) return;
        executor.execute(() -> {
            List<TimeSlot> allTimeSlots = db.timeSlotDao().getAllTimeSlots();
            List<TimeSlot> availableTimeSlots = new ArrayList<>();
            Date now = new Date();
            Date today = getStartOfDay(now);

            for (TimeSlot ts : allTimeSlots) {
                Date startTime = getDateForTimeString(ts.startTime, today);
                Date endTime = getDateForTimeString(ts.endTime, today);
                
                if (endTime != null && now.before(endTime) && (startTime != null && (now.before(startTime) || !startTime.after(now)))) {
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

    private void updateSeatSpinner(int floorNumber, @Nullable Runnable onFinished) {
        executor.execute(() -> {
            List<Seat> seatsForFloor = db.seatDao().getSeatsByFloor(floorNumber);
            List<Integer> seatNumbers = new ArrayList<>();
            for (Seat seat : seatsForFloor) {
                seatNumbers.add(seat.seatNumber);
            }
            Collections.sort(seatNumbers);

            handler.post(() -> {
                if (getContext() != null) {
                    ArrayAdapter<Integer> seatAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, seatNumbers);
                    seatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    seatSpinner.setAdapter(seatAdapter);
                    if (onFinished != null) {
                        onFinished.run();
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
            db.seatDao().updateSeatStatus(newRecord.seatId, "occupied");
            handler.post(() -> {
                Toast.makeText(getContext(), "预约成功!", Toast.LENGTH_SHORT).show();
                updateUIState();
                updateSeatAvailability();
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
        if (seatCheckRunnable != null) {
            handler.removeCallbacks(seatCheckRunnable);
        }
    }

    private void runSeatReleaseChecks() {
        Date now = new Date();
        Date today = getStartOfDay(now);
        final boolean[] availabilityHasChanged = {false};

        List<ReservationRecord> reservations = db.reservationRecordDao().getReservationsByDate(today);
        for (ReservationRecord r : reservations) {
            StudyRecord sr = db.studyRecordDao().findActiveStudyRecordByStudentAndSeat(r.studentId, r.seatId);
            if (sr == null) { // Not checked in
                TimeSlot ts = db.timeSlotDao().getTimeSlotById(r.timeSlotId);
                if (ts == null) continue;

                Date slotStartTime = getDateForTimeString(ts.startTime, today);
                long reservationTime = r.reservationTimestamp;

                if (slotStartTime != null) {
                    long checkInDeadline = (reservationTime < slotStartTime.getTime()) 
                        ? slotStartTime.getTime() + NO_SHOW_GRACE_PERIOD_MS 
                        : reservationTime + NO_SHOW_GRACE_PERIOD_MS;

                    if (now.getTime() > checkInDeadline) {
                        db.reservationRecordDao().deleteReservation(r.studentId, r.seatId, r.timeSlotId, r.reservationDate);
                        db.seatDao().updateSeatStatus(r.seatId, "available");
                        availabilityHasChanged[0] = true;

                        if (r.studentId == studentId) {
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
                    availabilityHasChanged[0] = true;
                    db.reservationRecordDao().deleteReservation(r.studentId, r.seatId, r.timeSlotId, r.reservationDate);

                    if (sr.studentId == studentId) {
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
        
        if(availabilityHasChanged[0]){
            handler.post(this::updateSeatAvailability);
        }
    }

    private void handleCancellation() {
        executor.execute(() -> {
            Date today = getStartOfDay(new Date());
            ReservationRecord reservation = db.reservationRecordDao().findReservationByUserAndDate(studentId, today);
            if (reservation != null) {
                db.reservationRecordDao().deleteReservation(reservation.studentId, reservation.seatId, reservation.timeSlotId, reservation.reservationDate);
                db.seatDao().updateSeatStatus(reservation.seatId, "available");
                handler.post(() -> {
                    Toast.makeText(getContext(), "预约已取消", Toast.LENGTH_SHORT).show();
                    updateUIState();
                    updateSeatAvailability();
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
                    updateSeatAvailability();
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
                    updateSeatAvailability();
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
                    seatAvailabilityInline.setVisibility(View.GONE);
                    seatAvailabilityBelow.setVisibility(View.VISIBLE);
                } else if (reservation != null) {
                    selectionLayout.setVisibility(View.GONE);
                    reserveButton.setVisibility(View.GONE);
                    cancelButton.setVisibility(View.VISIBLE);
                    checkinButton.setVisibility(View.VISIBLE);
                    checkoutButton.setVisibility(View.GONE);
                    seatStatusText.setText("状态: 已预约，等待签到");
                    checkinButton.setEnabled(true);
                    seatAvailabilityInline.setVisibility(View.GONE);
                    seatAvailabilityBelow.setVisibility(View.VISIBLE);
                } else {
                    selectionLayout.setVisibility(View.VISIBLE);
                    reserveButton.setVisibility(View.VISIBLE);
                    cancelButton.setVisibility(View.GONE);
                    checkinButton.setVisibility(View.GONE);
                    checkoutButton.setVisibility(View.GONE);
                    seatStatusText.setText("状态: 未预约");
                    seatAvailabilityInline.setVisibility(View.VISIBLE);
                    seatAvailabilityBelow.setVisibility(View.GONE);
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
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).equals(value)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void updateSeatAvailability() {
        executor.execute(() -> {
            List<Seat> allSeats = db.seatDao().getAllSeats(); 
            if (allSeats == null || allSeats.isEmpty()) {
                handler.post(() -> {
                    seatAvailabilityInline.setText("余量: 加载失败");
                    seatAvailabilityBelow.setText("座位信息加载失败");
                });
                return;
            }

            long availableCount = 0;
            for (Seat seat : allSeats) {
                if ("available".equals(seat.status)) { 
                    availableCount++;
                }
            }
            final String availabilityInfoInline = "座位余量: " + availableCount + "/" + allSeats.size();
            final String availabilityInfoBelow = "座位余量：" + availableCount + " / " + allSeats.size();

            handler.post(() -> {
                seatAvailabilityInline.setText(availabilityInfoInline);
                seatAvailabilityBelow.setText(availabilityInfoBelow);
            });
        });
    }
}
