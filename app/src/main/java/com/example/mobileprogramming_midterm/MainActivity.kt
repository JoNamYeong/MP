package com.example.mobileprogramming_midterm

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.SystemClock
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    lateinit var tabHost: TabHost
    //일정
    lateinit var spinnerSubject: Spinner
    lateinit var datePicker: DatePicker
    lateinit var btnRegister: Button
    lateinit var scheduleLayout: LinearLayout
    lateinit var ratingBar: RatingBar
    //타이머
    lateinit var chronometer: Chronometer
    lateinit var btnStart: Button
    lateinit var btnStop: Button
    lateinit var btnReset: Button
    var isRunning = false
    var pauseOffset: Long = 0

    //과목
    val subjects = arrayOf("국어","영어","수학","과학","사회","역사","음악","미술")
    data class ScheduleItem(val date: String, val subjects: MutableList<String>)
    val schedules = ArrayList<ScheduleItem>()

    //날짜
    @SuppressLint("SimpleDateFormat")
    val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //탭 구분
        tabHost = findViewById(R.id.tabHost)
        tabHost.setup()
        val tabSpec1 = tabHost.newTabSpec("Tab1").setIndicator("일정 등록")
        tabSpec1.setContent(R.id.tab1)
        tabHost.addTab(tabSpec1)
        val tabSpec2 = tabHost.newTabSpec("Tab2").setIndicator("공부 타이머")
        tabSpec2.setContent(R.id.tab2)
        tabHost.addTab(tabSpec2)
        //패딩
        val tabWidget = tabHost.findViewById<TabWidget>(android.R.id.tabs)
        for (i in 0 until tabWidget.childCount) {
            val tabView = tabWidget.getChildAt(i)
            tabView.setPadding(0, 80, 0, 20)
        }

        //일정 탭
        spinnerSubject = findViewById(R.id.spinnerSubject)
        datePicker = findViewById(R.id.datePicker)
        btnRegister = findViewById(R.id.btnRegister)
        scheduleLayout = findViewById(R.id.scheduleLayout)
        ratingBar = findViewById(R.id.ratingBar)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, subjects)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSubject.adapter = adapter

        btnRegister.setOnClickListener {
            val selectedSubject = spinnerSubject.selectedItem.toString()

            val year = datePicker.year
            val month = datePicker.month
            val day = datePicker.dayOfMonth

            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)
            val date = calendar.time
            val formattedDate = dateFormat.format(date)

            //일정 등록 알림
            registerSchedule(formattedDate, selectedSubject)
            Toast.makeText(this, "학습 일정이 등록되었습니다.", Toast.LENGTH_SHORT).show()
        }

        updateScheduleUI()


        //타이머 탭
        chronometer = findViewById(R.id.chronometer1)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        btnReset = findViewById(R.id.btnReset)

        //시작 버튼
        btnStart.setOnClickListener {
            if (!isRunning) {
                chronometer.base = SystemClock.elapsedRealtime() - pauseOffset
                chronometer.start()
                isRunning = true
            }
        }
        //정지 버튼
        btnStop.setOnClickListener {
            if (isRunning) {
                chronometer.stop()
                pauseOffset = SystemClock.elapsedRealtime() - chronometer.base
                isRunning = false
            }
        }
        //초기화 버튼
        btnReset.setOnClickListener {
            chronometer.stop()
            chronometer.base = SystemClock.elapsedRealtime()
            pauseOffset = 0
            isRunning = false
        }
    }

    //일정 등록
    fun registerSchedule(date: String, subject: String) {
        // 해당 날짜의 일정이 이미 있는지 확인
        val existingSchedule = schedules.find { it.date == date }

        if (existingSchedule != null) {
            //그날 일정이 있으면
            if (!existingSchedule.subjects.contains(subject)) {
                //해당 과목이 없으면
                existingSchedule.subjects.add(subject)
            }
        } else {
            //그날 일정이 없으면
            val newSchedule = ScheduleItem(date, mutableListOf(subject))
            schedules.add(newSchedule)
        }

        updateScheduleUI()
    }

    //일정 목록 업데이트
    @SuppressLint("SetTextI18n")
    fun updateScheduleUI() {
        scheduleLayout.removeAllViews()

        // 일정이 없으면
        if (schedules.isEmpty()) {
            val textView = TextView(this).apply {
                text = "등록된 학습 일정이 없습니다."
            }
            scheduleLayout.addView(textView)
            return
        }

        //일정 목록
        for (schedule in schedules) {
            //날짜 헤더
            val dateHeader = TextView(this).apply {
                text = "📅 ${schedule.date}"
            }
            scheduleLayout.addView(dateHeader)

            //해당 날짜 과목들
            val sortedSubjects = schedule.subjects.sorted()
            for (subject in sortedSubjects) {
                val scheduleItem = TextView(this).apply {
                    text = "- $subject"
                }
                scheduleLayout.addView(scheduleItem)
            }
        }


        //평점
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            Toast.makeText(this, "앱을 ${rating}점으로 평가했습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}