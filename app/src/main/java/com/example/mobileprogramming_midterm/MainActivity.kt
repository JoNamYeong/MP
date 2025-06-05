package com.example.mobileprogramming_midterm

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    lateinit var tabHost: TabHost
    //일정
    lateinit var spinnerSubject: Spinner
    lateinit var datePicker: DatePicker
    lateinit var btnRegister: Button
    lateinit var scheduleListView: ListView
    lateinit var ratingBar: RatingBar
    lateinit var scheduleAdapter: ScheduleAdapter
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
        scheduleListView = findViewById(R.id.scheduleListView)
        ratingBar = findViewById(R.id.ratingBar)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, subjects)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSubject.adapter = adapter

        // 스케줄 어댑터 초기화
        scheduleAdapter = ScheduleAdapter()
        scheduleListView.adapter = scheduleAdapter

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

        //평점
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            Toast.makeText(this, "앱을 ${rating}점으로 평가했습니다.", Toast.LENGTH_SHORT).show()
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
                existingSchedule.subjects.sort() // 과목 정렬
            }
        } else {
            //그날 일정이 없으면
            val newSchedule = ScheduleItem(date, mutableListOf(subject))
            schedules.add(newSchedule)
            // 날짜순으로 정렬
            schedules.sortBy { it.date }
        }

        // 어댑터에 데이터 변경 알림
        scheduleAdapter.notifyDataSetChanged()
    }

    // 커스텀 어댑터 클래스
    inner class ScheduleAdapter : BaseAdapter() {

        override fun getCount(): Int {
            if (schedules.isEmpty()) return 1 // 빈 상태 메시지용
            var totalCount = 0
            for (schedule in schedules) {
                totalCount += 1 + schedule.subjects.size // 날짜 헤더 + 과목들
            }
            return totalCount
        }

        override fun getItem(position: Int): Any? {
            return null
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            if (schedules.isEmpty()) {
                // 등록된 일정이 없을 때
                val textView = TextView(this@MainActivity).apply {
                    text = "등록된 학습 일정이 없습니다."
                    textSize = 16f
                    setPadding(40, 40, 40, 40)
                    gravity = android.view.Gravity.CENTER
                }
                return textView
            }

            var currentPosition = 0

            for (schedule in schedules) {
                // 날짜 헤더
                if (currentPosition == position) {
                    val dateHeader = TextView(this@MainActivity).apply {
                        text = "📅 ${schedule.date}"
                        textSize = 18f
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        setPadding(40, 30, 40, 15)
                        setBackgroundColor(android.graphics.Color.parseColor("#E0F7FF"))
                    }
                    return dateHeader
                }
                currentPosition++

                // 각 과목들
                for (subject in schedule.subjects.sorted()) {
                    if (currentPosition == position) {
                        val subjectItem = TextView(this@MainActivity).apply {
                            text = "  📚 $subject"
                            textSize = 16f
                            setPadding(60, 20, 40, 20)
                            setOnClickListener {
                                // 과목 클릭 시 삭제 확인
                                showDeleteDialog(schedule, subject)
                            }
                        }
                        return subjectItem
                    }
                    currentPosition++
                }
            }

            return TextView(this@MainActivity)
        }
    }

    // 삭제 확인
    private fun showDeleteDialog(schedule: ScheduleItem, subject: String) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("일정 삭제")
        builder.setMessage("'${schedule.date}'의 '$subject' 일정을 삭제하시겠습니까?")

        builder.setPositiveButton("삭제") { _, _ ->
            schedule.subjects.remove(subject)

            // 해당 날짜에 과목이 없으면 날짜 자체를 삭제
            if (schedule.subjects.isEmpty()) {
                schedules.remove(schedule)
            }

            scheduleAdapter.notifyDataSetChanged()
            Toast.makeText(this, "일정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("취소", null)
        builder.show()
    }
}