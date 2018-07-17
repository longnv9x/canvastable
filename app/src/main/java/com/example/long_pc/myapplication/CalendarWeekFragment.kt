package com.example.long_pc.myapplication

import android.os.Bundle
import android.os.Handler
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.example.long_pc.myapplication.model.SettingCalendar
import com.example.long_pc.myapplication.model.ShiftData
import com.example.long_pc.myapplication.model.ShiftItem
import java.util.*


/**
 * AP4002 カレンダー(週表示)
 */
class CalendarWeekFragment : Fragment(), CalendarWeekView {

    companion object {
        @JvmStatic
        fun newInstance(): CalendarWeekFragment {
            val ret = CalendarWeekFragment()
            return ret
        }
    }

    @BindView(R.id.bs_plan_preview)
    lateinit var planPreviewLayout: RelativeLayout

    private lateinit var bsPlanPreview: NonDraggingBottomSheet<RelativeLayout>
    lateinit var presenter: CalendarWeekPresenter
    @BindView(R.id.weekView)
    lateinit var weekView: WeekView

    private var title: String = ""
    private var isInitDatePicker = false
    private var isScrolling: Boolean = false

    init {
        presenter = CalendarWeekPresenter()
    }

    val currentTime: Calendar?
        get() {
            return Calendar.getInstance()
        }

    //region Week view events
    private val eventClickListener: WeekView.ViewClickListener = object : WeekView.ViewClickListener {
        override fun onDepartmentClick(mCurrentDepartment: String?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onEventClick(event: ShiftItem) {
            showPlanPreviewBottomSheet(true, event, event.date)
        }
    }

    private val monthChangeListener: MonthLoader.MonthChangeListener = object : MonthLoader.MonthChangeListener {
        override fun onDayChange(firstDay: Calendar): ShiftData? {
            if (presenter.currentTime == null) {
                presenter.currentTime = weekView.mFirstVisibleDay
            }
            return presenter.getEventFromCache(firstDay)
        }

        override fun onMonthChange(periodIndex: Int): ShiftData? {
            if (presenter.currentTime == null) {
                presenter.currentTime = weekView.mFirstVisibleDay
            }
            return presenter.getEventFromCache(periodIndex)
        }
    }

    private val onTitleChangedListener: WeekView.TitleChangeListener = object : WeekView.TitleChangeListener {
        override fun onTitleChange(calendar: Calendar) {
            presenter.currentTime = calendar
        }
    }

    private var lastScrollUpdate: Long = -1
    private val scrollSateChangeListener: WeekView.ScrollStateChangeListener = object : WeekView.ScrollStateChangeListener {
        override fun onScrollSateChanged(isIdle: Boolean) {
            isScrolling = !isIdle
            if (isScrolling) {
                showPlanPreviewBottomSheet(false)
            }
        }
    }

    private inner class ScrollStateHandler : Runnable {

        override fun run() {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastScrollUpdate > 500) {
                lastScrollUpdate = -1
                onScrollEnd()
            } else {
                Handler().postDelayed(this, 100)
            }
        }
    }

    private fun onScrollEnd() {
        weekView.getDataEvents()
    }

    private val scrollListener: WeekView.ScrollListener = object : WeekView.ScrollListener {
        override fun onFirstVisibleDayChanged(newFirstVisibleDay: Calendar, oldFirstVisibleDay: Calendar) {
            if (lastScrollUpdate == (-1).toLong()) {
                Handler().postDelayed(ScrollStateHandler(), 100)
            }
            lastScrollUpdate = System.currentTimeMillis()
        }
    }

    private val initListener: WeekView.InitListener = object : WeekView.InitListener {
        override fun onViewCreated() {
            goToDate(presenter.currentTime?.time, presenter.currentFocusedDay)
        }
    }
    //endregion Week view event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.attachView(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_calendar_week, container, false)
        ButterKnife.bind(this, view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
    }

    private fun initView() {
        //BEGIN: init bottom sheet
        bsPlanPreview = BottomSheetBehavior.from(planPreviewLayout) as NonDraggingBottomSheet<RelativeLayout>
        bsPlanPreview.peekHeight = 0
        bsPlanPreview.isHideable = true
        bsPlanPreview.state = BottomSheetBehavior.STATE_HIDDEN
        //END: init bottom sheet

        weekView.mViewClickListener = eventClickListener
        weekView.setMonthChangeListener(monthChangeListener)
        weekView.mTitleChangeListener = onTitleChangedListener
        weekView.mScrollStateChangeListener = scrollSateChangeListener
        weekView.mScrollHorizontal = scrollListener
        weekView.mInitListener = initListener
    }

    private fun initData() {
        presenter.loadCalendarSetting(SettingCalendar())
        focusToday()
    }

    /**
     * Initialize date picker view
     */
    private fun initDatePicker() {
        isInitDatePicker = true

    }

    /**
     * Triggered when selected a date on date picker
     */
    private fun onSelectedDateListener(date: Calendar) {
        weekView.goToDate(date)
    }

    /**
     * Triggered when touch outside date picker view
     */
    private fun onTouchOutsideDatePicker() {
    }

    private fun onDatePickerVisibilityChangedListener(visible: Int) {
        val rotate = if (visible == View.VISIBLE) 180F else 0F
    }

    /**
     * Triggered when click on title toolbar to show layout picker,
     * if week view is scrolling, so don't display date picker
     */
    private fun changeVisibilityDatePicker() {
        if (isScrolling) {
            return
        }
    }

    /**
     * Close date picker
     */
    fun closeDatePicker() {
    }

    /**
     * Scroll to today on the week view.
     */
    fun focusToday() {
        val today = Calendar.getInstance()

        if (weekView.mFirstVisibleDay == null) {
            presenter.currentFocusedDay = today.time
            return
        }

        if (!weekView.mFirstVisibleDay?.isTheSameDay(today)!!) {
            weekView.goToDate(today)
        }
    }

    /**
     * Scroll to a specific day on the week view. If `currentFocusedDay` is not `null`, scroll to that day
     * and if `currentFocusedDay` is `null`, scroll to the first day of month of `date`
     *
     * @param date The date to show.
     * @param currentFocusedDay The day which is selecting in Month Fragment
     */
    fun goToDate(date: Date?, currentFocusedDay: Date?) {
        if (date != null) {
            presenter.currentTime = Calendar.getInstance().apply { time = date }
        }
        presenter.currentFocusedDay = currentFocusedDay

        if (weekView.mFirstVisibleDay == null) {
            return
        }
        val isFocusedADay = currentFocusedDay != null

        val dayWillScrollTo: Calendar
        if (isFocusedADay) {
            dayWillScrollTo = Calendar.getInstance()
            dayWillScrollTo.time = currentFocusedDay
        } else {
            dayWillScrollTo = TimeUtils.getFirstDayOfMonth(date)
        }

        if (!weekView.mFirstVisibleDay?.isTheSameDay(dayWillScrollTo)!!) {
            weekView.goToDate(dayWillScrollTo)
        }
    }


    fun loadEventEventFromApiSuccessfully(startDate: Date) {
        presenter.checkToRefreshView(startDate)
    }

    //region implement CalendarWeekView
    override fun changeSetting(fontSize: Float, firstDayOfWeek: Int) {
        //change first day of week date picker

        //change font size, first day of week week view
        weekView.updateCalendarSettings(fontSize, firstDayOfWeek)
    }

    override fun showEvents() {
        weekView.notifyDataSetChanged()
    }

    override fun getEventFromCache(startTime: Date, endTime: Date): ShiftData? {
        return presenter.getEventFromCacheForWeek(startTime, endTime)
    }

    override fun showPlanPreviewBottomSheet(isShow: Boolean, events: ShiftItem?, dateSelected: Date?) {
        var state = StateShiftEnum.HIDDEN
        if (isShow) {
//            val eventsData: ShiftItem = events ?: ShiftItem()
//            setPreviewEventShowNumber(previewNum, presenter.calendarSetting.fontSize, weekNumber)
//            state = StateShiftEnum.SHOW
//            // tvEmptyPreview.visibility = if (eventsData.isEmpty()) View.VISIBLE else View.INVISIBLE
//            previewAdapter.setPreviewEvents(presenter.fillEvents(eventsData))
//            title = TimeUtils.getDateFormat(getString(R.string.AP4001_TITLE_SELECTED)).format(dateSelected)
        }
        //#6535
//        monthAreBeingDisplayed = title
//        parentView?.updateTitle(CalendarFragment.CALENDAR_MONTH_TAB, monthAreBeingDisplayed, state)
        bsPlanPreview.state = if (isShow) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun setPreviewEventShowNumber(showNumber: Int, textSize: Int, weekNumber: Int) {
        // to ensure that we can get height of calendar's RecyclerView
        val itemHeight = context?.resources?.getDimension(R.dimen.ap4001_preview_item_height) ?: 0F
//            val rowWeekHeight = WeekView.calculateHeight(context, showNumber, textSize)//#8535
        val newPreviewHeight = showNumber * itemHeight.toInt()
        val previewLayoutParams = planPreviewLayout.layoutParams
        if (previewLayoutParams.height != newPreviewHeight) {
            previewLayoutParams.height = newPreviewHeight
            planPreviewLayout.layoutParams = previewLayoutParams
            planPreviewLayout.requestLayout()
        }
        planPreviewLayout.post {
            //scrollToWeekNumberWhenShowPreview(weekNumber)
        }
    }
}
