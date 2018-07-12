package com.example.long_pc.myapplication

import android.graphics.RectF
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import butterknife.ButterKnife
import com.example.long_pc.myapplication.model.EventSummary
import com.example.long_pc.myapplication.model.SettingCalendar
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
    lateinit var presenter: CalendarWeekPresenter

    @BindView(R.id.weekView)
    lateinit var weekView: WeekView

    private var title: String = ""
    private var isInitDatePicker = false
    private var isScrolling: Boolean = false

    init {
        presenter= CalendarWeekPresenter()
    }
    val currentTime: Calendar?
        get() {
            return Calendar.getInstance()
        }
    //region Week view events
    private val eventClickListener: WeekView.EventClickListener = object : WeekView.EventClickListener {
        override fun onEventClick(event: EventSummary, eventRect: RectF) {
            when (event.calendarType) {
                DJCalendarEnum.Type.MEETING_DECIDED -> {
                    val isFixed = event.end.after(Date())
//                    if (isFixed) {
//                        startActivityIgnoreNetwork(ConfirmedMeetingActivity.createIntent(context, event.meetingId))
//                    } else {
//                        startActivityIgnoreNetwork(FinishedMeetingDetailActivity.createIntent(context, event.meetingId
//                                ?: "", false))
//                    }
                }
                DJCalendarEnum.Type.RQ_MEETING_MEDIATOR -> {
                    val isFixed = event.end.after(Date())
                 //   startActivityIgnoreNetwork(FinishedMeetingDetailActivity.createIntent(context, event.meetingId
                          //  ?: "", false, Pair(true, isFixed)))
                }
                else -> {
                   // startActivityForResultIgnoreNetwork(EventDetailActivity.createIntent(context, event), CalendarFragment.REQUEST_DETAIL_EVENT)
                }
            }
        }
    }

    private val monthChangeListener: MonthLoader.MonthChangeListener = object : MonthLoader.MonthChangeListener {
        override fun onMonthChange(periodIndex: Int): List<EventSummary> {
            if (presenter.currentTime == null) {
                presenter.currentTime = weekView.mFirstVisibleDay
            }
           // presenter.checkToGetEventFromApi(periodIndex)
            return presenter.getEventFromCache(periodIndex)
        }
    }

    private val onTitleChangedListener: WeekView.TitleChangeListener = object : WeekView.TitleChangeListener {
        override fun onTitleChange(calendar: Calendar) {
//            presenter.currentTime = calendar
//            title = String.format(screenTitle, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
//            parentView?.updateTitle(CalendarFragment.CALENDAR_WEEK_TAB, title)
        }
    }

    private val scrollSateChangeListener: WeekView.ScrollStateChangeListener = object : WeekView.ScrollStateChangeListener {
        override fun onScrollSateChanged(isIdle: Boolean) {
            isScrolling = !isIdle
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
        val view= inflater.inflate(R.layout.fragment_calendar_week, container,false)
        ButterKnife.bind(this,view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
    }

    private fun initView() {
        weekView.mEventClickListener = eventClickListener
        weekView.setMonthChangeListener(monthChangeListener)
        weekView.mTitleChangeListener = onTitleChangedListener
        weekView.mScrollStateChangeListener = scrollSateChangeListener
        weekView.mInitListener = initListener
    }

    private fun initData() {
        presenter.loadCalendarSetting(SettingCalendar())
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
}
