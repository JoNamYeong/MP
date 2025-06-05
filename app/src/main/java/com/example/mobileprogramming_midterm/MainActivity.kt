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
    lateinit var subjectImageView: ImageView
    //타이머
    lateinit var chronometer: Chronometer
    lateinit var btnStart: Button
    lateinit var btnStop: Button
    lateinit var btnReset: Button
    var isRunning = false
    var pauseOffset: Long = 0

    //과목
    val subjects = arrayOf("국어","영어","수학","과학","사회","역사","음악","미술")
    data class ScheduleItem(
        val date: String,
        val subjects: MutableList<String>,
        val notes: MutableMap<String, String> = mutableMapOf(),
        val completed: MutableMap<String, Boolean> = mutableMapOf()
    )
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
        subjectImageView = findViewById(R.id.subjectImageView)

        val adapter = SubjectSpinnerAdapter(this, subjects)
        spinnerSubject.adapter = adapter

        spinnerSubject.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedSubject = subjects[position]
                updateSubjectImage(selectedSubject)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                subjectImageView.setImageResource(R.drawable.ic_launcher_foreground)
            }
        }

        scheduleAdapter = ScheduleAdapter()
        scheduleListView.adapter = scheduleAdapter

        setListViewHeightBasedOnChildren(scheduleListView)

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
            schedules.sortBy { it.date }
        }

        scheduleAdapter.notifyDataSetChanged()
        setListViewHeightBasedOnChildren(scheduleListView)
    }

    inner class ScheduleAdapter : BaseAdapter() {

        override fun getCount(): Int {
            if (schedules.isEmpty()) return 1
            var totalCount = 0
            for (schedule in schedules) {
                totalCount += 1 + schedule.subjects.size
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

                // 잉정 리스트
                for (subject in schedule.subjects.sorted()) {
                    if (currentPosition == position) {
                        // 과목 아이템을 위한 LinearLayout 생성
                        val subjectLayout = LinearLayout(this@MainActivity).apply {
                            orientation = LinearLayout.HORIZONTAL
                            setPadding(60, 20, 40, 20)
                            gravity = android.view.Gravity.CENTER_VERTICAL
                            setOnClickListener {
                                // 과목 클릭 시 세부정보 다이얼로그 표시
                                showDetailDialog(schedule, subject)
                            }
                        }

                        val checkBox = CheckBox(this@MainActivity).apply {
                            isChecked = schedule.completed[subject] ?: false
                            setOnCheckedChangeListener { _, isChecked ->
                                schedule.completed[subject] = isChecked
                                scheduleAdapter.notifyDataSetChanged()
                                setListViewHeightBasedOnChildren(scheduleListView)
                            }
                        }

                        // 과목 텍스트
                        val subjectText = TextView(this@MainActivity).apply {
                            val isCompleted = schedule.completed[subject] ?: false
                            text = "$subject"
                            textSize = 16f
                            setPadding(20, 0, 0, 0)

                            if (isCompleted) {
                                paintFlags = paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                            } else {
                                paintFlags = paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                            }
                        }

                        subjectLayout.addView(checkBox)
                        subjectLayout.addView(subjectText)

                        return subjectLayout
                    }
                    currentPosition++
                }
            }

            return TextView(this@MainActivity)
        }
    }

    // 세부정보
    private fun showDetailDialog(schedule: ScheduleItem, subject: String) {
        val dialogView = layoutInflater.inflate(android.R.layout.simple_list_item_1, null)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 40)
        }

        // 제목
        val titleText = TextView(this).apply {
            text = "학습 일정 세부정보"
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 30)
        }
        layout.addView(titleText)

        // 날짜
        val dateText = TextView(this).apply {
            text = "날짜: ${schedule.date}"
            textSize = 16f
            setPadding(0, 10, 0, 10)
        }
        layout.addView(dateText)

        // 과목
        val isCompleted = schedule.completed[subject] ?: false
        val completionText = if (isCompleted) " (완료)" else ""

        val subjectText = TextView(this).apply {
            text = "과목: $subject$completionText"
            textSize = 16f
            setPadding(0, 10, 0, 10)
        }
        layout.addView(subjectText)

        // 메모
        val noteTitle = TextView(this).apply {
            text = "학습 메모:"
            textSize = 16f
            setPadding(0, 10, 0, 5)
        }
        layout.addView(noteTitle)

        val noteEditText = EditText(this).apply {
            hint = "메모를 입력하세요"
            setText(schedule.notes[subject] ?: "")
            setPadding(20, 20, 20, 20)
            setBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"))
        }
        layout.addView(noteEditText)

        val builder = android.app.AlertDialog.Builder(this)
        builder.setView(layout)

        builder.setPositiveButton("메모 저장") { _, _ ->
            val noteText = noteEditText.text.toString()
            if (noteText.isNotEmpty()) {
                schedule.notes[subject] = noteText
                Toast.makeText(this, "메모가 저장되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNeutralButton("일정 삭제") { _, _ ->
            showDeleteConfirmDialog(schedule, subject)
        }

        builder.setNegativeButton("닫기", null)

        val dialog = builder.create()
        dialog.show()
    }



    // 삭제 확인 문구
    private fun showDeleteConfirmDialog(schedule: ScheduleItem, subject: String) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("일정 삭제")
        builder.setMessage("'${schedule.date}'의 '$subject' 일정을 정말 삭제하시겠습니까?")

        builder.setPositiveButton("삭제") { _, _ ->
            schedule.subjects.remove(subject)
            schedule.notes.remove(subject)
            schedule.completed.remove(subject)

            // 해당 날짜에 과목이 없으면
            if (schedule.subjects.isEmpty()) {
                schedules.remove(schedule)
            }

            scheduleAdapter.notifyDataSetChanged()
            Toast.makeText(this, "일정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("취소", null)
        builder.show()
    }

    private fun setListViewHeightBasedOnChildren(listView: ListView) {
        val listAdapter = listView.adapter ?: return

        var totalHeight = 0
        for (i in 0 until listAdapter.count) {
            val listItem = listAdapter.getView(i, null, listView)
            listItem.measure(0, 0)
            totalHeight += listItem.measuredHeight
        }

        val params = listView.layoutParams
        params.height = totalHeight + (listView.dividerHeight * (listAdapter.count - 1))
        listView.layoutParams = params
        listView.requestLayout()
    }

    // 과목에 따른 이미지
    private fun updateSubjectImage(subject: String) {
        val imageResource = when (subject) {
            "국어" -> R.drawable.korean
            "영어" -> R.drawable.english
            "수학" -> R.drawable.math
            "과학" -> R.drawable.science
            "사회" -> R.drawable.social
            "역사" -> R.drawable.history
            "음악" -> R.drawable.music
            "미술" -> R.drawable.art
            else -> R.drawable.ic_launcher_foreground
        }
        subjectImageView.setImageResource(imageResource)
    }

    // 커스텀 스피너 어댑터
    inner class SubjectSpinnerAdapter(context: android.content.Context, private val subjects: Array<String>)
        : ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, subjects) {

        init {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            return view
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = layoutInflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)
            val textView = view.findViewById<TextView>(android.R.id.text1)

            val subject = subjects[position]
            textView.text = subject
            textView.setPadding(40, 30, 40, 30)

            return view
        }
    }
}