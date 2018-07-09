@file:Suppress("MemberVisibilityCanPrivate", "LoopToCallChain", "unused")

package com.example.long_pc.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Build
import android.support.v4.util.ArraySet
import android.support.v4.view.GestureDetectorCompat
import android.support.v4.view.ViewCompat
import android.support.v4.view.animation.FastOutLinearInInterpolator
import android.text.*
import android.text.format.DateUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.widget.OverScroller
import com.example.long_pc.myapplication.model.EventSummary
import java.util.*
import kotlin.collections.ArrayList


class WeekView : View {

    private enum class Direction {
        NONE, LEFT, RIGHT, VERTICAL
    }

    companion object {
        const val BUFFER_DAY = 7 // total = BUFFER_DAY// * 2 + mNumberOfVisibleDays
        const val DAY_PAST_ALPHA = 100
        const val PAST_ALPHA = 160
        const val NORMAL_ALPHA = 255
        const val STROKE_HIGHLIGHT_WIDTH = 4F
        const val DEFAULT_STROKE_WIDTH = 2F
        val STROKE_HIGHLIGHT_COLOR = Color.parseColor("#c7666666")
        const val DISTANCE_FROM_TOP = 60
        const val BLOCK = 15
    }

    private var NumberStaff: Int = 0
    private var mContext: Context
    private val mTeamTextPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private var mTeamTextWidth: Float = 0F
    private var mTeamCorrectBackground: Float = 0F
    private var mTeamTextPadding: Float = 0F
    private var mTeamTextHeight: Float = 0F
    private val mTimeTextPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private var mTimeTextWidth: Float = 0F
    private var mTimeTextHeight: Float = 0F
    private val mHeaderTextPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private var mHeaderTextHeight: Float = 0F
    private var mHeaderHeight: Float = 0F
    private lateinit var mGestureDetector: GestureDetectorCompat
    private var mScroller: OverScroller? = null
    private val mCurrentOrigin = PointF(0f, 0f)
    private var mCurrentScrollDirection = Direction.NONE
    private val mHeaderBackgroundPaint: Paint by lazy { Paint() }
    private var mWidthPerDay: Float = 0.toFloat()
    private val mDayBackgroundPaint: Paint by lazy { Paint() }
    private val mHourSeparatorPaint: Paint by lazy { Paint() }
    private var mHeaderMarginBottom: Float = 0F
    private val mTodayBackgroundPaint: Paint by lazy { Paint() }
    //    private val mFutureBackgroundPaint: Paint by lazy { Paint() }
//    private val mFutureWeekendBackgroundPaint: Paint by lazy { Paint() }
//    private val mPastWeekendBackgroundPaint: Paint by lazy { Paint() }
//    private val mNowLinePaint: Paint by lazy { Paint() }
//    private val mTodayHeaderTextPaint: TextPaint by lazy { TextPaint(Paint.ANTI_ALIAS_FLAG) }
    private val mEventBackgroundPaint: Paint by lazy { Paint() }

    /**
     * Width of the first column (time 01:00 - 23:00)
     */
    private var mHeaderColumnWidth: Float = 0F

    private var mEventRects: ArrayList<EventRect> = ArrayList()
    //    private var mPreviousPeriodEvents: List<EventSummary>? = null
    private var mCurrentPeriodEvents: List<EventSummary>? = null
    //    private var mNextPeriodEvents: List<EventSummary>? = null
    private val mEventTextPaint: TextPaint by lazy { TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.LINEAR_TEXT_FLAG) }
    private val mHeaderColumnBackgroundPaint: Paint by lazy { Paint() }
    private var mFetchedPeriod = -1 // the middle period the calendar has fetched.
    private var mRefreshEvents = false
    private var mCurrentFlingDirection = Direction.NONE

    private val mBitmapCollapse = ViewUtils.getBitmapFromXml(context, R.drawable.ic_arrow_up)
    private val mBitmapExpand = ViewUtils.getBitmapFromXml(context, R.drawable.ic_arrow_down)
    private val mPaintBitmapExpandCollapse = Paint()
    private val mDestRectBitmapExpandCollapse: Rect = Rect()
    private val mSrcRectBitmapExpandCollapse: Rect = Rect()

    private val mAllDayText = context.getString(R.string.AP4002_LBL_1)
    private val mAllDayTextPaint: TextPaint by lazy { TextPaint(Paint.ANTI_ALIAS_FLAG) }
    private var mAllDayTextWidth: Float = 0F
    private var mAllDayTextHeight: Float = 0F

    /**
     * The first visible day in the week view.
     */
    var mFirstVisibleDay: Calendar? = null
        private set

    /**
     * The last visible day in the week view.
     */
    var mLastVisibleDay: Calendar? = null
        private set

    var mShowFirstDayOfWeekFirst = false

    private var mMinimumFlingVelocity = 0
    private var mScaledTouchSlop = 0

    // Attributes and their default values.
    var mHourHeight = 50
        set(value) {
            field = value
            invalidate()
        }

    private var mMinHourHeight = 0 //no minimum specified (will be dynamic, based on screen)
//    private var mEffectiveMinHourHeight = mMinHourHeight //compensates for the fact that you can't keep zooming out.
//    private var mMaxHourHeight = 250

    var mFirstDayOfWeek = Calendar.MONDAY
        /**
         * Set the first day of the week. First day of the week is used only when the week view is first
         * drawn. It does not of any effect after user starts scrolling horizontally.
         * <p>
         * <b>Note:</b> This method will only work if the week view is set to display more than 6 days at
         * once.
         * </p>
         *
         * @param value The supported values are {@link Calendar#SUNDAY},
         *                       {@link Calendar#MONDAY}, {@link Calendar#TUESDAY},
         *                       {@link Calendar#WEDNESDAY}, {@link Calendar#THURSDAY},
         *                       {@link Calendar#FRIDAY}.
         */
        set(value) {
            field = value
            invalidate()
        }
    /**
     * Returns the last day of week.
     *
     * @return the last day of week
     */
    val mLastDayOfWeek: Int
        get() = if (mFirstDayOfWeek == Calendar.MONDAY) Calendar.SUNDAY else Calendar.SATURDAY

    var mTextSize = 16
        set(value) {
            field = value
//            mTodayHeaderTextPaint.textSize = mTextSize.toFloat()
            mHeaderTextPaint.textSize = mTextSize.toFloat()
            mTimeTextPaint.textSize = mTextSize.toFloat()
            invalidate()
        }

    private var mTextSizeTime = 14
    var mHeaderColumnPadding = 10
        set(value) {
            field = value
            invalidate()
        }

    var mHeaderColumnTextColor = Color.BLACK
        set(value) {
            field = value
            mHeaderTextPaint.color = mHeaderColumnTextColor
            mTimeTextPaint.color = mHeaderColumnTextColor
            invalidate()
        }

    private var mHeaderSundayColumnTextColor = Color.BLACK
    private var mHeaderSaturdayColumnTextColor = Color.BLACK
    private var mHeaderColumnTextColorTime = Color.BLACK

    /**
     * The number of visible days in a week.
     */
    private var mNumberOfVisibleDays = 3
        /**
         * Set the number of visible days in a week.
         *
         * @param value The number of visible days in a week.
         */
        set(value) {
            field = value
            mCurrentOrigin.x = 0f
            mCurrentOrigin.y = 0f
            invalidate()
        }

    var mHeaderRowPadding = 10
        set(value) {
            field = value
            invalidate()
        }

    var mHeaderRowBackgroundColor = Color.BLUE
        set(value) {
            field = value
            mHeaderBackgroundPaint.color = field
            invalidate()
        }

    var mDayBackgroundColor = Color.rgb(245, 245, 245)
        set(value) {
            field = value
            mDayBackgroundPaint.color = field
            invalidate()
        }

//    private var mPastBackgroundColor = Color.rgb(227, 227, 227)
//    private var mFutureBackgroundColor = Color.rgb(245, 245, 245)
//    private var mPastWeekendBackgroundColor = 0
//    private var mFutureWeekendBackgroundColor = 0

//    var mShowNowLine = false
//        /**
//         * Get whether "now" line should be displayed. "Now" line is defined by the attributes
//         * `nowLineColor` and `nowLineThickness`.
//         *
//         * @return True if "now" line should be displayed.
//         */
//        get
//        /**
//         * Set whether "now" line should be displayed. "Now" line is defined by the attributes
//         * `nowLineColor` and `nowLineThickness`.
//         *
//         * @param value True if "now" line should be displayed.
//         */
//        set(value) {
//            field = value
//            invalidate()
//        }

//    var mNowLineColor = Color.rgb(102, 102, 102)
//        /**
//         * Get the "now" line color.
//         *
//         * @return The color of the "now" line.
//         */
//        get
//        /**
//         * Set the "now" line color.
//         *
//         * @param value The color of the "now" line.
//         */
//        set(value) {
//            field = value
//            invalidate()
//        }

//    var mNowLineThickness = 5
//        /**
//         * Get the "now" line thickness.
//         *
//         * @return The thickness of the "now" line.
//         */
//        get
//        /**
//         * Set the "now" line thickness.
//         *
//         * @param value The thickness of the "now" line.
//         */
//        set(value) {
//            field = value
//            invalidate()
//        }

    var mHourSeparatorColor = Color.rgb(230, 230, 230)
        set(value) {
            field = value
            mHourSeparatorPaint.color = field
            invalidate()
        }

    var mTodayBackgroundColor = Color.rgb(239, 247, 254)
        set(value) {
            field = value
            mTodayBackgroundPaint.color = field
            invalidate()
        }

    var mHourSeparatorHeight = 2
        set(value) {
            field = value
            mHourSeparatorPaint.strokeWidth = field.toFloat()
            invalidate()
        }

//    var mTodayHeaderTextColor = Color.rgb(39, 137, 228)
//        set(value) {
//            field = value
//            mTodayHeaderTextPaint.color = field
//            invalidate()
//        }

    var mEventTextSize = 9
        set(value) {
            field = value
            mEventTextPaint.textSize = field.toFloat()
            invalidate()
        }

    var mEventTextColor = Color.BLACK
        set(value) {
            field = value
            mEventTextPaint.color = field
            invalidate()
        }

    var mEventPadding = 8
        set(value) {
            field = value
            invalidate()
        }

    var mHeaderColumnBackgroundColor = Color.WHITE
        set(value) {
            field = value
            mHeaderColumnBackgroundPaint.color = field
            invalidate()
        }

    private var mIsFirstDraw = true
    private var mAreDimensionsInvalid = true

    var mOverlappingEventGap = 0
        /**
         * Set the gap between overlapping events.
         *
         * @param value The gap between overlapping events.
         */
        set(value) {
            field = value
            invalidate()
        }

    var mEventMarginVertical = 0
        /**
         * Set the top and bottom margin of the event. The event will release this margin from the top
         * and bottom edge. This margin is useful for differentiation consecutive events.
         *
         * @param value The top and bottom margin.
         */
        set(value) {
            field = value
            invalidate()
        }

    /**
     * The scrolling speed factor in horizontal direction.
     */
    var mXScrollingSpeed = 0.6f

    private var mScrollToDay: Calendar? = null
    private var mScrollToHour = -1.0

    /**
     * Corner radius for event rect.
     */
    var mEventCornerRadius = 0

    /**
     * Whether weekends should have a background color different from the normal day background
     * color. The weekend background colors are defined by the attributes
     * `futureWeekendBackgroundColor` and `pastWeekendBackgroundColor`.
     */
    var mShowDistinctWeekendColor = false
        /**
         * Set whether weekends should have a background color different from the normal day background
         * color. The weekend background colors are defined by the attributes
         * `futureWeekendBackgroundColor` and `pastWeekendBackgroundColor`.
         *
         * @param value True if weekends should have different background colors.
         */
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Whether past and future days should have two different background colors. The past and
     * future day colors are defined by the attributes `futureBackgroundColor` and
     * `pastBackgroundColor`.
     */
    var mShowDistinctPastFutureColor = false
        /**
         * Set whether weekends should have a background color different from the normal day background
         * color. The past and future day colors are defined by the attributes `futureBackgroundColor`
         * and `pastBackgroundColor`.
         *
         * @param value True if past and future should have two different
         *                                    background colors.
         */
        set(value) {
            field = value
            invalidate()
        }

    /**
     * The flag to determine whether the week view should fling horizontally or not.
     */
    var mHorizontalFlingEnabled = true

    /**
     * The flag to determine whether the week view should fling vertically or not.
     */
    var mVerticalFlingEnabled = true

    var mAllDayEventHeight: Int = 0

    /**
     * The height of an event in all day event section
     */
    private var mAllDayEventItemHeight = DimensionUtils.dpToPx((mEventTextSize * 1.75).toFloat())

    /**
     * The maximum of number event which users can see in the all day section
     */
    private val mMaxVisibleAllDayEventNum = 3

    var mScrollDuration = 250

    // All-day events
    private var mPositionFilled: Array<MutableSet<Int>> = Array(BUFFER_DAY * 2 + mNumberOfVisibleDays, { ArraySet<Int>() })

    private var mAllDayEventNumArray: IntArray = IntArray(BUFFER_DAY * 2 + mNumberOfVisibleDays)
    private var mOriginalAllDayEvent: BooleanArray = BooleanArray(BUFFER_DAY * 2 + mNumberOfVisibleDays)
    private var mLimitedAllDayEvents = true
    private var mToggleList: ArrayList<RectF> = arrayListOf()
    private var mNeedToScrollAllDayEvents = false
    private val mVisibleHeaderBackgroundPaint: Paint by lazy { Paint() }
    private val mBackgroundColor = Color.parseColor("#fffefc")
    private var mMinYWhenScrollingAllDayEvents: Int = 0

    /**
     * The actual number of events in all day event section
     */
    private var mMaxInColumn: Int = 0
    private var mMaxInColumnToShowToggleButton: Int = 0 // for displaying the toggle button

    private val mAllDayEventSeparatorPaint: Paint by lazy { Paint() }
    private val mEventSeparatorWidth = DimensionUtils.dpToPx(1F)
    private val mHolidays = HashMap<Calendar, Boolean>()
    private var mHasAllDayEvents: Boolean = false

    // Listeners.
    var mInitListener: InitListener? = null
    var mEventClickListener: EventClickListener? = null
    var mEventLongPressListener: EventLongPressListener? = null
    var mEventTouchListener: EventTouchListener? = null
    //    var mEmptyViewClickListener: EmptyViewClickListener? = null
//    var mEmptyViewLongPressListener: EmptyViewLongPressListener? = null

    /**
     * Event loaders define the  interval after which the events
     * are loaded in week view. For a MonthLoader events are loaded for every month. You can define
     * your custom event loader by extending WeekViewLoader.
     */
    var mWeekViewLoader: WeekViewLoader? = null

    private val mDateTimeInterpreter: DateTimeInterpreter by lazy {
        object : DateTimeInterpreter {

            override fun interpretDate(date: Calendar): String {
                return try {
                    val dateFormat = TimeUtils.getDateFormat(context.getString(R.string.TIME_FORMAT_WEEK))
                    dateFormat.applyPattern(context.getString(R.string.TIME_FORMAT_WEEK))
                    dateFormat.format(date.time)
                } catch (e: Exception) {
                    e.printStackTrace()
                    ""
                }
            }

            override fun interpretTime(hour: Int): String {
                return if (hour < 10) {
                    "0$hour:00"
                } else {
                    "$hour:00"
                }
            }
        }
    }
    //    var mScrollListener: ScrollListener? = null
    var mTitleChangeListener: TitleChangeListener? = null
    var mScrollStateChangeListener: ScrollStateChangeListener? = null

    // Event touch screen
    private val mGestureListener = object : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            mEventTouchListener?.onDown()
            goToNearestOrigin()
            return true
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            when (mCurrentScrollDirection) {
                Direction.NONE -> {
                    // Allow scrolling only in one direction.
                    mCurrentScrollDirection = if (Math.abs(distanceX) > Math.abs(distanceY)) {
                        if (distanceX > 0) {
                            Direction.LEFT
                        } else {
                            Direction.RIGHT
                        }
                    } else {
                        Direction.VERTICAL
                    }
                }
                Direction.LEFT -> {
                    // Change direction if there was enough change.
                    if (Math.abs(distanceX) > Math.abs(distanceY) && distanceX < -mScaledTouchSlop) {
                        mCurrentScrollDirection = Direction.RIGHT
                    }
                }
                Direction.RIGHT -> {
                    // Change direction if there was enough change.
                    if (Math.abs(distanceX) > Math.abs(distanceY) && distanceX > mScaledTouchSlop) {
                        mCurrentScrollDirection = Direction.LEFT
                    }
                }
                else -> {
                    //do nothing
                }
            }

            // Calculate the new origin after scroll.
            when (mCurrentScrollDirection) {
                Direction.LEFT, Direction.RIGHT -> {
                    mCurrentOrigin.x -= distanceX * mXScrollingSpeed
                    ViewCompat.postInvalidateOnAnimation(this@WeekView)
                }
                Direction.VERTICAL -> {
                    if (e1.y > mHeaderHeight || mNeedToScrollAllDayEvents) {
                        mCurrentOrigin.y -= distanceY
                        ViewCompat.postInvalidateOnAnimation(this@WeekView)
                    }
                }
                else -> {
                    //do nothing
                }
            }
            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (mCurrentFlingDirection == Direction.LEFT && !mHorizontalFlingEnabled ||
                    mCurrentFlingDirection == Direction.RIGHT && !mHorizontalFlingEnabled ||
                    mCurrentFlingDirection == Direction.VERTICAL && !mVerticalFlingEnabled) {
                return true
            }

            mScroller?.forceFinished(true)

            mCurrentFlingDirection = mCurrentScrollDirection
            when (mCurrentFlingDirection) {
                Direction.LEFT, Direction.RIGHT -> {
                    mScroller?.fling(mCurrentOrigin.x.toInt(), mCurrentOrigin.y.toInt(), (velocityX * mXScrollingSpeed).toInt(), 0, Integer.MIN_VALUE, Integer.MAX_VALUE, (-((mHourHeight * 24).toFloat() + mHeaderHeight + mHeaderMarginBottom + mTimeTextHeight / 2 - height)).toInt(), 0)
                    ViewCompat.postInvalidateOnAnimation(this@WeekView)
                }
                Direction.VERTICAL -> if (e1.y > mHeaderHeight || mNeedToScrollAllDayEvents) {
                    mScroller?.fling(mCurrentOrigin.x.toInt(), mCurrentOrigin.y.toInt(), 0, velocityY.toInt(), Integer.MIN_VALUE, Integer.MAX_VALUE, if (mNeedToScrollAllDayEvents) mMinYWhenScrollingAllDayEvents else (-((mHourHeight * 24).toFloat() + mHeaderHeight + mHeaderMarginBottom + mTimeTextHeight / 2 - height)).toInt(), 0)
                    ViewCompat.postInvalidateOnAnimation(this@WeekView)
                }
                else -> {
                    //do nothing
                }
            }
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            for (rectF in mToggleList) {
                if (e.x > rectF.left && e.x < rectF.right && e.y > rectF.top && e.y < rectF.bottom) {
                    toggleAllDayEvents()
                    playSoundEffect(SoundEffectConstants.CLICK)
                    return super.onSingleTapConfirmed(e)
                }
            }

            // If the tap was on an event then trigger the callback.
            if (mEventClickListener != null) {
                val reversedEventRects = mEventRects
                for (event in reversedEventRects) {
                    if (e.y <= mHeaderHeight && !event.originalEvent.allDay) {
                        continue
                    }
                    if (event.rectF != null && e.x > event.rectF!!.left && e.x < event.rectF!!.right && e.y > event.rectF!!.top && e.y < event.rectF!!.bottom) {
                        mEventClickListener?.onEventClick(event.originalEvent, event.rectF!!)
                        playSoundEffect(SoundEffectConstants.CLICK)
                        return super.onSingleTapConfirmed(e)
                    }
                }
            }

            // If the tap was on in an empty space, then trigger the callback.
//            if (mEmptyViewClickListener != null && e.x > mHeaderColumnWidth && e.y > mHeaderHeight + mHeaderMarginBottom) {
//                val selectedTime = getTimeFromPoint(e.x, e.y)
//                if (selectedTime != null) {
//                    playSoundEffect(SoundEffectConstants.CLICK)
//                    mEmptyViewClickListener?.onEmptyViewClicked(selectedTime)
//                }
//            }

            return super.onSingleTapConfirmed(e)
        }

        override fun onLongPress(e: MotionEvent) {
            super.onLongPress(e)

            if (mEventLongPressListener != null) {
                val reversedEventRects = mEventRects
                //                Collections.reverse(reversedEventRects);
                for (event in reversedEventRects) {
                    if (event.rectF != null && e.x > event.rectF!!.left && e.x < event.rectF!!.right && e.y > event.rectF!!.top && e.y < event.rectF!!.bottom) {
                        mEventLongPressListener?.onEventLongPress(event.originalEvent, event.rectF!!)
                        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        return
                    }
                }
            }

            // If the tap was on in an empty space, then trigger the callback.
//            if (mEmptyViewLongPressListener != null && e.x > mHeaderColumnWidth && e.y > mHeaderHeight + mHeaderMarginBottom) {
//                val selectedTime = getTimeFromPoint(e.x, e.y)
//                if (selectedTime != null) {
//                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
//                    mEmptyViewLongPressListener?.onEmptyViewLongPress(selectedTime)
//                }
//            }
        }
    }

    constructor (context: Context) : this(context, null)

    constructor (context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor (context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        // Hold references.
        mContext = context

        // Get the attribute values (if any).
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.WeekView, 0, 0)
        try {
            mFirstDayOfWeek = a.getInteger(R.styleable.WeekView_firstDayOfWeek, mFirstDayOfWeek)
            mHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_hourHeight, mHourHeight)
            mMinHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_minHourHeight, mMinHourHeight)
//            mEffectiveMinHourHeight = mMinHourHeight
//            mMaxHourHeight = a.getDimensionPixelSize(R.styleable.WeekView_maxHourHeight, mMaxHourHeight)
            mTextSize = a.getDimensionPixelSize(R.styleable.WeekView_textSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSize.toFloat(), context.resources.displayMetrics).toInt())
            mTextSizeTime = a.getDimensionPixelSize(R.styleable.WeekView_textSizeTime, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSizeTime.toFloat(), context.resources.displayMetrics).toInt())
            mHeaderColumnPadding = a.getDimensionPixelSize(R.styleable.WeekView_headerColumnPadding, mHeaderColumnPadding)
            mHeaderColumnTextColor = a.getColor(R.styleable.WeekView_headerColumnTextColor, mHeaderColumnTextColor)
            mHeaderColumnTextColorTime = a.getColor(R.styleable.WeekView_headerColumnTextColorTime, mHeaderColumnTextColorTime)
            mHeaderSundayColumnTextColor = a.getColor(R.styleable.WeekView_headerColumnTextColorSunday, mHeaderSundayColumnTextColor)
            mHeaderSaturdayColumnTextColor = a.getColor(R.styleable.WeekView_headerColumnTextColorSaturday, mHeaderSaturdayColumnTextColor)
            mNumberOfVisibleDays = a.getInteger(R.styleable.WeekView_noOfVisibleDays, mNumberOfVisibleDays)
            mShowFirstDayOfWeekFirst = a.getBoolean(R.styleable.WeekView_showFirstDayOfWeekFirst, mShowFirstDayOfWeekFirst)
            mHeaderRowPadding = a.getDimensionPixelSize(R.styleable.WeekView_headerRowPadding, mHeaderRowPadding)
            mHeaderRowBackgroundColor = a.getColor(R.styleable.WeekView_headerRowBackgroundColor, mHeaderRowBackgroundColor)
            mDayBackgroundColor = a.getColor(R.styleable.WeekView_dayBackgroundColor, mDayBackgroundColor)
//            mFutureBackgroundColor = a.getColor(R.styleable.WeekView_futureBackgroundColor, mFutureBackgroundColor)
//            mPastBackgroundColor = a.getColor(R.styleable.WeekView_pastBackgroundColor, mPastBackgroundColor)
//            mFutureWeekendBackgroundColor = a.getColor(R.styleable.WeekView_futureWeekendBackgroundColor, mFutureBackgroundColor) // If not set, use the same color as in the week
//            mPastWeekendBackgroundColor = a.getColor(R.styleable.WeekView_pastWeekendBackgroundColor, mPastBackgroundColor)
//            mNowLineColor = a.getColor(R.styleable.WeekView_nowLineColor, mNowLineColor)
//            mNowLineThickness = a.getDimensionPixelSize(R.styleable.WeekView_nowLineThickness, mNowLineThickness)
            mHourSeparatorColor = a.getColor(R.styleable.WeekView_hourSeparatorColor, mHourSeparatorColor)
            mTodayBackgroundColor = a.getColor(R.styleable.WeekView_todayBackgroundColor, mTodayBackgroundColor)
            mHourSeparatorHeight = a.getDimensionPixelSize(R.styleable.WeekView_hourSeparatorHeight, mHourSeparatorHeight)
//            mTodayHeaderTextColor = a.getColor(R.styleable.WeekView_todayHeaderTextColor, mTodayHeaderTextColor)
            mEventTextSize = a.getDimensionPixelSize(R.styleable.WeekView_eventTextSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mEventTextSize.toFloat(), context.resources.displayMetrics).toInt())
            mEventTextColor = a.getColor(R.styleable.WeekView_eventTextColor, mEventTextColor)
            mEventPadding = a.getDimensionPixelSize(R.styleable.WeekView_eventPadding, mEventPadding)
            mHeaderColumnBackgroundColor = a.getColor(R.styleable.WeekView_headerColumnBackground, mHeaderColumnBackgroundColor)
            mOverlappingEventGap = a.getDimensionPixelSize(R.styleable.WeekView_overlappingEventGap, mOverlappingEventGap)
            mEventMarginVertical = a.getDimensionPixelSize(R.styleable.WeekView_eventMarginVertical, mEventMarginVertical)
            mXScrollingSpeed = a.getFloat(R.styleable.WeekView_xScrollingSpeed, mXScrollingSpeed)
            mEventCornerRadius = a.getDimensionPixelSize(R.styleable.WeekView_eventCornerRadius, mEventCornerRadius)
            mShowDistinctPastFutureColor = a.getBoolean(R.styleable.WeekView_showDistinctPastFutureColor, mShowDistinctPastFutureColor)
            mShowDistinctWeekendColor = a.getBoolean(R.styleable.WeekView_showDistinctWeekendColor, mShowDistinctWeekendColor)
//            mShowNowLine = a.getBoolean(R.styleable.WeekView_showNowLine, mShowNowLine)
            mHorizontalFlingEnabled = a.getBoolean(R.styleable.WeekView_horizontalFlingEnabled, mHorizontalFlingEnabled)
            mVerticalFlingEnabled = a.getBoolean(R.styleable.WeekView_verticalFlingEnabled, mVerticalFlingEnabled)
//            mAllDayEventHeight = a.getDimensionPixelSize(R.styleable.WeekView_allDayEventHeight, mAllDayEventHeight)
            mScrollDuration = a.getInt(R.styleable.WeekView_scrollDuration, mScrollDuration)
        } finally {
            a.recycle()
        }

        init()
    }

    private fun init() {
        // Scrolling initialization.
        mGestureDetector = GestureDetectorCompat(mContext, mGestureListener)
        mScroller = OverScroller(mContext, FastOutLinearInInterpolator())

        mMinimumFlingVelocity = ViewConfiguration.get(mContext).scaledMinimumFlingVelocity
        mScaledTouchSlop = ViewConfiguration.get(mContext).scaledTouchSlop

        // Measure settings for time column.
        val rect = Rect()
        mTimeTextPaint.getTextBounds("00 PM", 0, "00 PM".length, rect)
        mTimeTextHeight = rect.height().toFloat()
        mHeaderMarginBottom = -mTimeTextHeight / 2
        mTimeTextPaint.textAlign = Paint.Align.RIGHT
        mTimeTextPaint.textSize = mTextSizeTime.toFloat()
        mTimeTextPaint.color = mHeaderColumnTextColorTime
        mTeamTextPaint.textAlign = Paint.Align.RIGHT
        mTeamTextPaint.textSize = mTextSizeTime.toFloat()
        mTeamTextPaint.color = Color.WHITE
        mTeamCorrectBackground = 5F
        mTeamTextPadding = 5F
        mTeamTextHeight = mTimeTextHeight

        initTextTimeWidth()

        // Measure settings for header row.
        mHeaderTextPaint.color = mHeaderColumnTextColor
        mHeaderTextPaint.textAlign = Paint.Align.CENTER
        mHeaderTextPaint.textSize = mTextSize.toFloat()
        mHeaderTextPaint.getTextBounds("00 PM", 0, "00 PM".length, rect)
        mHeaderTextHeight = rect.height().toFloat()
        //mHeaderTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        // Measure setting for text all day
        mAllDayTextPaint.color = mHeaderColumnTextColorTime
        mAllDayTextPaint.textSize = (mEventTextSize + 2).toFloat()
        mAllDayTextPaint.getTextBounds(mAllDayText, 0, mAllDayText.length, rect)
        mAllDayTextWidth = rect.width().toFloat()
        mAllDayTextHeight = rect.height().toFloat()

        // Prepare header background paint.
        mHeaderBackgroundPaint.color = mHeaderRowBackgroundColor
        mHeaderBackgroundPaint.style = Paint.Style.STROKE
        mHeaderBackgroundPaint.strokeWidth = DEFAULT_STROKE_WIDTH

        // Prepare visible header background pain
        mVisibleHeaderBackgroundPaint.color = mBackgroundColor
        mVisibleHeaderBackgroundPaint.style = Paint.Style.FILL

        mAllDayEventSeparatorPaint.color = mHeaderRowBackgroundColor
        mAllDayEventSeparatorPaint.style = Paint.Style.STROKE
        mAllDayEventSeparatorPaint.strokeWidth = DEFAULT_STROKE_WIDTH

        // Prepare day background color paint.
        mDayBackgroundPaint.color = mHeaderRowBackgroundColor
        mDayBackgroundPaint.style = Paint.Style.STROKE
        mDayBackgroundPaint.pathEffect = DashPathEffect(floatArrayOf(7f, 7f), 0f)
        mDayBackgroundPaint.strokeWidth = DEFAULT_STROKE_WIDTH

//        mFutureBackgroundPaint.color = mFutureBackgroundColor

//        mPastBackgroundPaint.color = mPastBackgroundColor

//        mFutureWeekendBackgroundPaint.color = mFutureWeekendBackgroundColor

//        mPastWeekendBackgroundPaint.color = mPastWeekendBackgroundColor

        // Prepare hour separator color paint.
        mHourSeparatorPaint.color = mHourSeparatorColor
        mHourSeparatorPaint.style = Paint.Style.STROKE
        mHourSeparatorPaint.pathEffect = DashPathEffect(floatArrayOf(7f, 7f), 0f)
        mHourSeparatorPaint.strokeWidth = DEFAULT_STROKE_WIDTH

        // Prepare the "now" line color paint
//        mNowLinePaint.strokeWidth = mNowLineThickness.toFloat()
//        mNowLinePaint.color = mNowLineColor

        // Prepare today background color paint.
        mTodayBackgroundPaint.color = mTodayBackgroundColor

        // Prepare today header text color paint.
//        mTodayHeaderTextPaint.textAlign = Paint.Align.CENTER
//        mTodayHeaderTextPaint.textSize = mTextSize.toFloat()
//        mTodayHeaderTextPaint.color = mHeaderColumnTextColor

        // Prepare event background color.
        mEventBackgroundPaint.color = Color.rgb(174, 208, 238)

        // Prepare header column background color.
        mHeaderColumnBackgroundPaint.color = mHeaderColumnBackgroundColor

        // Prepare event text size and color.
        mEventTextPaint.style = Paint.Style.FILL
        mEventTextPaint.color = mEventTextColor
        mEventTextPaint.textSize = mEventTextSize.toFloat()
    }

    // fix rotation changes
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mAreDimensionsInvalid = true
    }

    /**
     * Initialize time column width. Calculate value with all possible hours (supposed widest text).
     */
    private fun initTextTimeWidth() {
        mTimeTextWidth = (0..100)
                .map {
                    // Measure time string and get max width.
                    mDateTimeInterpreter.interpretTime(it)
                }
                .map { mTimeTextPaint.measureText(it) }
                .max()
                ?: 0f
        mTeamTextWidth = mTimeTextPaint.measureText("Team")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(mBackgroundColor)
        canvas.drawLine(0F, DEFAULT_STROKE_WIDTH / 2, mHeaderColumnWidth + DEFAULT_STROKE_WIDTH, DEFAULT_STROKE_WIDTH / 2, mHeaderBackgroundPaint)
        canvas.drawLine(0F, height - DEFAULT_STROKE_WIDTH / 2, width.toFloat(), height - DEFAULT_STROKE_WIDTH / 2, mHeaderBackgroundPaint)

        drawHeaderRowAndEvents(canvas)

        drawTimeColumnAndAxes(canvas)
    }

    /**
     * Calculates the height of the header.
     */
    private fun calculateHeaderHeight() {
        //Make sure the header is the right size (depends on AllDay events)
        var containsAllDayEvent = false
        mNeedToScrollAllDayEvents = false
        if (mEventRects.isNotEmpty()) {
            mMaxInColumn = 0
            mMaxInColumnToShowToggleButton = 0
            var foundFirstDayOfWeek = true
            var foundLastDayOfWeek: Boolean
            var stopped = false
            for (dayNumber in 0 until mNumberOfVisibleDays) {
                foundLastDayOfWeek = false
                val day = mFirstVisibleDay?.clone() as Calendar
                day.add(Calendar.DATE, dayNumber)
                var allDayEventNum = 0
//                if (dayNumber > -BUFFER_DAY && day.get(Calendar.DAY_OF_WEEK) == mFirstDayOfWeek) {
//                    foundFirstDayOfWeek = true
//                } else if (foundFirstDayOfWeek && day.get(Calendar.DAY_OF_WEEK) == mLastDayOfWeek) {
//                    if (dayNumber > mNumberOfVisibleDays - 1) {
//                        foundLastDayOfWeek = true
//                    }
//                }
                if (!foundFirstDayOfWeek || stopped) {
                    continue
                }
                for (i in mEventRects.indices) {
                    if (day.isTheSameDay(mEventRects[i].event.startCalendar) && mEventRects[i].event.allDay) {
                        containsAllDayEvent = true
                        allDayEventNum++
                    }
                }
                mMaxInColumn = Math.max(mMaxInColumn, allDayEventNum)
                if (dayNumber in 0..(mNumberOfVisibleDays - 1)) {
                    mMaxInColumnToShowToggleButton = Math.max(mMaxInColumnToShowToggleButton, allDayEventNum)
                }
                if (foundLastDayOfWeek) {
                    stopped = true
                }
            }
            if (!mLimitedAllDayEvents && mMaxInColumn > 0) {
                val itemAllDayCount = Math.max(mMaxInColumn, mMaxVisibleAllDayEventNum)
                mAllDayEventHeight = (itemAllDayCount * mAllDayEventItemHeight + ((itemAllDayCount + 2) * (mEventPadding / 2))).toInt()

                if (mHeaderTextHeight + mHeaderRowPadding * 2 + mAllDayEventHeight > height) {
                    mMinYWhenScrollingAllDayEvents = (height - (mHeaderTextHeight + mHeaderRowPadding * 2 + mAllDayEventHeight)).toInt()
                    mAllDayEventHeight = (height.toFloat() - mHeaderTextHeight - mHeaderRowPadding * 2).toInt()
                    mNeedToScrollAllDayEvents = true
                }
            } else {
                val itemAllDayCount = Math.min(mMaxInColumn, mMaxVisibleAllDayEventNum)
                mAllDayEventHeight = (itemAllDayCount * mAllDayEventItemHeight + ((itemAllDayCount + 2) * (mEventPadding / 2))).toInt()
            }
        }
        mHasAllDayEvents = containsAllDayEvent
        mHeaderHeight = mHeaderTextHeight + mHeaderRowPadding * 2 + if (containsAllDayEvent) mAllDayEventHeight else 0
    }

    /**
     * Draws the time column and all the axes/separators.
     *
     * @param canvas
     */
    private fun drawTimeColumnAndAxes(canvas: Canvas) {
        // Draw the background color for the header column.
        canvas.drawRect(0f, mHeaderHeight, mHeaderColumnWidth, height.toFloat(), mHeaderColumnBackgroundPaint)

        // Clip to paint in time column only.
        canvas.clipRect(0f, mHeaderHeight, mHeaderColumnWidth, height.toFloat(), Region.Op.REPLACE)
        canvas.drawLine(mHeaderColumnWidth, 0f, mHeaderColumnWidth, height.toFloat(), mHeaderBackgroundPaint)
        if (mNeedToScrollAllDayEvents) {
            return
        }

        for (i in 0..100) {
            val topLine = mCurrentOrigin.y + (mHourHeight * i).toFloat()  + mHeaderMarginBottom + mHeaderHeight
            val top = mHeaderHeight + mCurrentOrigin.y + (mHourHeight * i).toFloat() + mHourHeight/2 + mHeaderMarginBottom*2

            // Draw the text if its y position is not outside of the visible area. The pivot point of the text is the point at the bottom-right corner.
          //  val name = mDateTimeInterpreter.interpretTime(i)
            val name = "name $i"
            val teamColor = "Team"
            if (top < height) {
                canvas.drawLine(0F, topLine, mHeaderColumnWidth, topLine, mHeaderBackgroundPaint)
                val background = getTextBackgroundSize(mTeamTextWidth, top + mTeamTextHeight, teamColor, mTeamTextPaint)
                mHeaderColumnBackgroundPaint.color = Color.RED
                canvas.drawRoundRect(background, mTeamCorrectBackground, mTeamCorrectBackground, mHeaderColumnBackgroundPaint)
                canvas.drawText(teamColor, mTeamTextWidth + mHeaderColumnPadding * 2, top + mTeamTextHeight, mTeamTextPaint)
                mHeaderColumnBackgroundPaint.color = Color.WHITE
                canvas.drawText(name, mTeamTextWidth + mTimeTextWidth + mHeaderColumnPadding * 2, top + mTimeTextHeight, mTimeTextPaint)
            }
        }
    }

    private fun getTextBackgroundSize(x: Float, y: Float, text: String, paint: Paint): RectF {
        val fontMetrics = paint.fontMetrics
        val halfTextLength = paint.measureText(text) / 2 + 5
        return RectF(x - halfTextLength, y + fontMetrics.top, x + halfTextLength, y + fontMetrics.bottom)
    }

    /**
     * Draws the header row.
     *
     * @param canvas
     */
    private fun drawHeaderRowAndEvents(canvas: Canvas) {
        // Calculate the available width for each day.
        mHeaderColumnWidth = mTimeTextWidth + mTeamTextWidth + mHeaderColumnPadding * 2 + mTeamTextPadding * 2
        mWidthPerDay = width.toFloat() - mHeaderColumnWidth
        mWidthPerDay /= mNumberOfVisibleDays

        calculateHeaderHeight() //Make sure the header is the right size (depends on AllDay events)

        val today = TimeUtils.today()

        if (mAreDimensionsInvalid) {
            mAreDimensionsInvalid = false
            if (mScrollToDay != null)
                goToDate(mScrollToDay!!)

            mAreDimensionsInvalid = false
            if (mScrollToHour >= 0)
                goToHour(mScrollToHour)

            mScrollToDay = null
            mScrollToHour = -1.0
            mAreDimensionsInvalid = false
        }

        // Iterate through each day.
//        val oldFirstVisibleDay = mFirstVisibleDay
        mFirstVisibleDay = today.clone() as Calendar
//        mFirstVisibleDay!!.add(Calendar.DATE, -Math.round(mCurrentOrigin.x / mWidthPerDay))
//        if (mFirstVisibleDay != oldFirstVisibleDay && mScrollListener != null) {
//            if (oldFirstVisibleDay != null) {
//                mScrollListener?.onFirstVisibleDayChanged(mFirstVisibleDay!!, oldFirstVisibleDay)
//            }
//        }

        if (mIsFirstDraw) {
            mIsFirstDraw = false

            mInitListener?.onViewCreated()
            // If the week view is being drawn for the first time, then consider the first day of the week.
            if (mNumberOfVisibleDays >= 7 && today.get(Calendar.DAY_OF_WEEK) != mFirstDayOfWeek && mShowFirstDayOfWeekFirst) {
                val difference = today.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek
                mCurrentOrigin.x += mWidthPerDay * difference
            }

        }

        // If the new mCurrentOrigin.y is invalid, make it valid.
        if (mNeedToScrollAllDayEvents) {
            mCurrentOrigin.y = Math.max(mCurrentOrigin.y, mMinYWhenScrollingAllDayEvents.toFloat())
        } else if (mCurrentOrigin.y < height.toFloat() - (mHourHeight * 24).toFloat() - mHeaderHeight - (mHeaderRowPadding * 2).toFloat() - mHeaderMarginBottom - mTimeTextHeight / 2) {
            mCurrentOrigin.y = height.toFloat() - (mHourHeight * 24).toFloat() - mHeaderHeight - (mHeaderRowPadding * 2).toFloat() - mHeaderMarginBottom - mTimeTextHeight / 2
        }

        // Don't put an "else if" because it will trigger a glitch when completely zoomed out and
        // scrolling vertically.
        if (mCurrentOrigin.y > 0) {
            mCurrentOrigin.y = 0f
        }

        // Consider scroll offset.
        var leftDaysWithGaps = (-Math.ceil(((if (mCurrentOrigin.x == 0f) mCurrentOrigin.x else mCurrentOrigin.x - 5) / mWidthPerDay).toDouble())).toInt()
        val startFromPixel = mCurrentOrigin.x + mWidthPerDay * leftDaysWithGaps +
                mHeaderColumnWidth - DEFAULT_STROKE_WIDTH
        var startPixel = startFromPixel

        //Draw events START
        // Prepare to iterate for each day.
        var day = today.clone() as Calendar
        day.add(Calendar.HOUR, 6)

        // Clear the cache for event rectangles.
        mEventRects.forEach {
            it.rectF = null
        }

        // Clip to paint events only.
        canvas.clipRect(mHeaderColumnWidth, mHeaderHeight + mHeaderMarginBottom + mTimeTextHeight / 2, width.toFloat(), height.toFloat(), Region.Op.REPLACE)

        mFirstVisibleDay!!.add(Calendar.DATE, -Math.round(mCurrentOrigin.x / mWidthPerDay))

        val dashPath = Path()
        for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + mNumberOfVisibleDays + 1) {

            // Check if the day is today.
            day = today.clone() as Calendar
            mLastVisibleDay = day.clone() as Calendar
            day.add(Calendar.DATE, dayNumber - 1)
            mLastVisibleDay?.add(Calendar.DATE, dayNumber - 2)
            val sameDay = day.isTheSameDay(today)

            if (mTitleChangeListener != null && dayNumber == leftDaysWithGaps + 2) {
                mTitleChangeListener?.onTitleChange(day)
            }

            // Get more events if necessary. We want to store the events 3 months beforehand. Get
            // events only when it is the first iteration of the loop.
            if (mRefreshEvents || dayNumber == leftDaysWithGaps + 1 && mFetchedPeriod != TimeUtils.toWeekViewPeriodIndex(day).toInt()) {
                getMoreEvents(day)
                mRefreshEvents = false
            }

            // Draw background color for each day.
            val start = if (startPixel < mHeaderColumnWidth) mHeaderColumnWidth else startPixel
            if (mWidthPerDay + startPixel - start > 0) {
                if (day.before(today)) {
                    mDayBackgroundPaint.alpha = PAST_ALPHA
                    mHourSeparatorPaint.alpha = PAST_ALPHA
                } else {
                    mDayBackgroundPaint.alpha = NORMAL_ALPHA
                    mHourSeparatorPaint.alpha = NORMAL_ALPHA
                }

                //Draw the line separating days in normal event section: if the day is the first day of week -> draw thick separator else draw dash separator
//                if (day.get(Calendar.DAY_OF_WEEK) == mFirstDayOfWeek && dayNumber != leftDaysWithGaps + 1) {
//                    mDayBackgroundPaint.color = STROKE_HIGHLIGHT_COLOR
//                    mDayBackgroundPaint.strokeWidth = STROKE_HIGHLIGHT_WIDTH
//                    canvas.drawLine(startPixel - DEFAULT_STROKE_WIDTH, mHeaderHeight + mTimeTextHeight / 2 + mHeaderMarginBottom, startPixel - DEFAULT_STROKE_WIDTH, height.toFloat(), mDayBackgroundPaint)
//                    mDayBackgroundPaint.color = mHeaderRowBackgroundColor
//                    mDayBackgroundPaint.strokeWidth = DEFAULT_STROKE_WIDTH
//                } else {
//                    dashPath.moveTo(start, mHeaderHeight + mTimeTextHeight / 2 + mHeaderMarginBottom)
//                    dashPath.lineTo(start, height.toFloat())
//                    canvas.drawPath(dashPath, mDayBackgroundPaint)
//                    dashPath.reset()
//                }
                dashPath.moveTo(start, mHeaderHeight + mTimeTextHeight / 2 + mHeaderMarginBottom)
                dashPath.lineTo(start, height.toFloat())
                canvas.drawPath(dashPath, mDayBackgroundPaint)
                dashPath.reset()
                if (sameDay) {
                    canvas.drawRect(start + DEFAULT_STROKE_WIDTH, mHeaderHeight + mTimeTextHeight / 2 + mHeaderMarginBottom, startPixel + mWidthPerDay - DEFAULT_STROKE_WIDTH, height - DEFAULT_STROKE_WIDTH / 2, mTodayBackgroundPaint)
                }
            }

            if (!mNeedToScrollAllDayEvents) {
                // Draw the lines for hours.
                val path = Path()
                for (hourNumber in 0..100) {
                    val top = mHeaderHeight + mCurrentOrigin.y + (mHourHeight * hourNumber).toFloat() + mTimeTextHeight + mHeaderMarginBottom + DISTANCE_FROM_TOP
                    if (top > mHeaderHeight + mTimeTextHeight / 2 + mHeaderMarginBottom - mHourSeparatorHeight && top < height && startPixel + mWidthPerDay - start > 0) {
                        path.moveTo(start, top)
                        path.lineTo(start + mWidthPerDay, top)
                        canvas.drawPath(path, mHourSeparatorPaint)
                        path.reset()
                    }
                }

                // Draw the events.
                mWidthPerDay -= (mEventSeparatorWidth * 2)
                drawEvents(day, startPixel + mEventSeparatorWidth * 2, canvas)
                mWidthPerDay += (mEventSeparatorWidth * 2)
            }

            // Draw the line at the current time.
//            if (mShowNowLine && sameDay) {
//                val startY = mHeaderHeight + mTimeTextHeight / 2 + mHeaderMarginBottom + mCurrentOrigin.y
//                val now = Calendar.getInstance()
//                val beforeNow = (now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60.0f) * mHourHeight
//                canvas.drawLine(start, startY + beforeNow, startPixel + mWidthPerDay, startY + beforeNow, mNowLinePaint)
//            }

            // In the next iteration, start from the next day.
            startPixel += mWidthPerDay
        }
        //Draw events END


        //Draw header, time, column START
        mHeaderBackgroundPaint.alpha = NORMAL_ALPHA

        // Hide everything in the first cell (top left corner).
        canvas.clipRect(0f, 0f, mTimeTextWidth + (mHeaderColumnPadding * 2).toFloat() + DEFAULT_STROKE_WIDTH, height.toFloat(), Region.Op.REPLACE)

        //draw line separator between column time and the rest
        canvas.drawLine(mHeaderColumnWidth, mHeaderTextHeight + mHeaderRowPadding * 2, mHeaderColumnWidth, mHeaderHeight, mHeaderBackgroundPaint)

        // Draw the partial of line separating the header (not include all day event) and all-day events
        run {
            val y: Float
            if (mHasAllDayEvents) {
                y = mHeaderTextHeight + mHeaderRowPadding * 2
                canvas.drawLine(0f, y, mHeaderColumnWidth + DEFAULT_STROKE_WIDTH, y, mAllDayEventSeparatorPaint)
            } else {
                y = mHeaderTextHeight + mHeaderRowPadding * 2 - DEFAULT_STROKE_WIDTH / 2
                canvas.drawLine(0f, y, mHeaderColumnWidth + DEFAULT_STROKE_WIDTH, y, mAllDayEventSeparatorPaint)
            }
        }

        //Draw the rect of time in the left column
        run {
            val y: Float
            if (mHasAllDayEvents) {
                y = mHeaderTextHeight + mHeaderRowPadding * 2 + mAllDayEventHeight - DEFAULT_STROKE_WIDTH
                canvas.drawLine(0F, y, mHeaderColumnWidth + DEFAULT_STROKE_WIDTH, y, mHeaderBackgroundPaint)
            }
        }
        run {
            val x = mHeaderColumnWidth
            canvas.drawLine(x, mHeaderHeight, x, height.toFloat(), mHeaderBackgroundPaint)
        }
        run {
            val y = height - DEFAULT_STROKE_WIDTH / 2
            canvas.drawLine(0F, y, mHeaderColumnWidth, y, mHeaderBackgroundPaint)
        }

        // Draw the expand collapse bitmap in case there is more than maxAllDayEvent
        if (mHasAllDayEvents && mMaxInColumnToShowToggleButton > mMaxVisibleAllDayEventNum) {
            val width: Int
            val height: Int

            if (mLimitedAllDayEvents) {
                width = mBitmapExpand!!.width
                height = mBitmapExpand.height
            } else {
                width = mBitmapCollapse!!.width
                height = mBitmapCollapse.height
            }
            var rectF = RectF(mHeaderColumnWidth / 2 - width / 3, 0f, 0f, mAllDayEventHeight.toFloat() + mHeaderColumnPadding.toFloat() + mHeaderTextHeight)
            rectF.right = (rectF.left + width / 1.5).toFloat()
            rectF.top = (rectF.bottom - height / 1.5).toFloat()

            mSrcRectBitmapExpandCollapse.set(0, 0, width, height)
            mDestRectBitmapExpandCollapse.set(rectF.left.toInt(), rectF.top.toInt(), rectF.right.toInt(), rectF.bottom.toInt())

            if (mLimitedAllDayEvents) {
                canvas.drawBitmap(mBitmapExpand, mSrcRectBitmapExpandCollapse, mDestRectBitmapExpandCollapse, mPaintBitmapExpandCollapse)
            } else {
                canvas.drawBitmap(mBitmapCollapse, mSrcRectBitmapExpandCollapse, mDestRectBitmapExpandCollapse, mPaintBitmapExpandCollapse)
            }

            rectF = RectF(0f, rectF.top - mHeaderColumnPadding, mHeaderColumnWidth, rectF.bottom + mHeaderColumnPadding)
            mToggleList.add(rectF)
        }

        //draw text all day
        if (mHasAllDayEvents) {
            canvas.drawText(mAllDayText, (mHeaderColumnWidth - mAllDayTextWidth) / 2, mHeaderTextHeight + mHeaderRowPadding * 2 + mEventPadding + mAllDayTextHeight, mAllDayTextPaint)
        }

        // Clip to paint header row only.
        canvas.clipRect(mHeaderColumnWidth + DEFAULT_STROKE_WIDTH, 0f, width.toFloat(), mHeaderHeight, Region.Op.REPLACE)

        // Draw the header background.
        canvas.drawRect(0f, 0f, width.toFloat(), mHeaderTextHeight + mHeaderRowPadding * 2, mVisibleHeaderBackgroundPaint)

        // Draw the line in the top of week view
        canvas.drawLine(mHeaderColumnWidth, DEFAULT_STROKE_WIDTH / 2, width.toFloat(), DEFAULT_STROKE_WIDTH / 2, mAllDayEventSeparatorPaint)

        //Draw the line separating the header (include all day event section) and normal event section
        run {
            val y: Float
            if (mHasAllDayEvents) {
                y = mHeaderTextHeight + mHeaderRowPadding * 2 + mAllDayEventHeight - DEFAULT_STROKE_WIDTH
                canvas.drawLine(mHeaderColumnWidth - DEFAULT_STROKE_WIDTH, y, width.toFloat(), y, mHeaderBackgroundPaint)
            } else {
                y = mHeaderTextHeight + mHeaderRowPadding * 2 - DEFAULT_STROKE_WIDTH / 2
                canvas.drawLine(mHeaderColumnWidth - DEFAULT_STROKE_WIDTH, y, width.toFloat(), y, mHeaderBackgroundPaint)
            }
        }

        // Draw the line separating the header (not include all day event section) and all-day events
        if (mHasAllDayEvents) {
            canvas.drawLine(mHeaderColumnWidth, mHeaderTextHeight + mHeaderRowPadding * 2, width.toFloat(), mHeaderTextHeight + mHeaderRowPadding * 2, mAllDayEventSeparatorPaint)
        }

        // Draw background for the all-day section.
        startPixel = startFromPixel
        for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + mNumberOfVisibleDays + 1) {
            // Check if the day is today.
            day = today.clone() as Calendar
            day.add(Calendar.DATE, dayNumber - 1)
//            if (day.before(today)) {
//                mHeaderBackgroundPaint.alpha = PAST_ALPHA
//            } else {
//                mHeaderBackgroundPaint.alpha = NORMAL_ALPHA
//            }
            //draw line separating days in event all day section: if the day is not first day of week so don't draw
            if (day.get(Calendar.DAY_OF_WEEK) != mFirstDayOfWeek) {
                canvas.drawLine(startPixel, mHeaderTextHeight + mHeaderRowPadding * 2, startPixel, height.toFloat(), mHeaderBackgroundPaint)
            }

            //draw background all day section if the day is today
            if (day.isTheSameDay(today)) {
                canvas.drawRect(startPixel + DEFAULT_STROKE_WIDTH, DEFAULT_STROKE_WIDTH, startPixel + mWidthPerDay - DEFAULT_STROKE_WIDTH, (height - DEFAULT_STROKE_WIDTH), mTodayBackgroundPaint)
            }
            startPixel += mWidthPerDay
        }
        //Draw header, time, column END


        // Draws all-day events START
        startPixel = startFromPixel - BUFFER_DAY * mWidthPerDay
        leftDaysWithGaps -= BUFFER_DAY
        run {
            var dayNumber = leftDaysWithGaps + 1
            var i = 0
            while (dayNumber <= leftDaysWithGaps + BUFFER_DAY * 2 + mNumberOfVisibleDays && i < mPositionFilled.size) {
                mPositionFilled[i] = HashSet()
                mAllDayEventNumArray[i] = 0
                mOriginalAllDayEvent[i] = false
                day = today.clone() as Calendar
                day.add(Calendar.DATE, dayNumber - 1)
                for (eventRect in mEventRects) {
                    if (eventRect.event.allDay && day.isTheSameDay(eventRect.event.startCalendar)) {
                        mAllDayEventNumArray[i]++
                    }
                    if (eventRect.event.allDay && day.isTheSameDay(eventRect.originalEvent.startCalendar)) {
                        mOriginalAllDayEvent[i] = true
                    }
                }
                dayNumber++
                i++
            }
        }

        run {
            var dayNumber = leftDaysWithGaps + 1
            var i = 0
            while (dayNumber <= leftDaysWithGaps + BUFFER_DAY * 2 + mNumberOfVisibleDays && i < mPositionFilled.size) {
                day = today.clone() as Calendar
                day.add(Calendar.DATE, dayNumber - 1)
                drawAllDayEvents(day, startPixel + mEventSeparatorWidth * 2, canvas, i)
                startPixel += mWidthPerDay
                dayNumber++
                i++
            }
        }
        // Draws all-day events END

        // Draw the separator between weeks START
        startPixel = startFromPixel
        for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + mNumberOfVisibleDays + 1) {
            // Draw the line separating the day in the header (include all day event)
//            canvas.drawLine(startPixel + mWidthPerDay, 0f, startPixel + mWidthPerDay, mHeaderTextHeight + mHeaderColumnPadding * 2, mHeaderBackgroundPaint)

            day = today.clone() as Calendar
            day.add(Calendar.DATE, dayNumber - 1)
            // if (day.get(Calendar.DAY_OF_WEEK) == mFirstDayOfWeek && dayNumber != leftDaysWithGaps + 1) {
            //  mDayBackgroundPaint.color = STROKE_HIGHLIGHT_COLOR
            // mDayBackgroundPaint.strokeWidth = STROKE_HIGHLIGHT_WIDTH

            // Draw the line separating the week in the all day section
            canvas.drawLine(startPixel - DEFAULT_STROKE_WIDTH, 0f, startPixel - DEFAULT_STROKE_WIDTH, height.toFloat(), mDayBackgroundPaint)
            //  mDayBackgroundPaint.color = mHeaderRowBackgroundColor
            //  mDayBackgroundPaint.strokeWidth = DEFAULT_STROKE_WIDTH
            //   }
            startPixel += mWidthPerDay
        }
        // Draw the separator between weeks END

        // Draw the header row texts START
        startPixel = startFromPixel
        leftDaysWithGaps += BUFFER_DAY
        for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + mNumberOfVisibleDays + 1) {
            // Check if the day is today.
            day = today.clone() as Calendar
            day.add(Calendar.DATE, dayNumber - 1)
            val isSunday = day.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
            val isSaturday = day.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
            // Draw the day labels.
            val dayLabel = mDateTimeInterpreter.interpretDate(day)
            val x = startPixel + (mWidthPerDay / 2)
            if (!isSaturday && !isSunday && (mHolidays[day] == null || mHolidays[day] == false)) {
                mHeaderTextPaint.color = mHeaderColumnTextColor
                if (day.before(today)) {
                    mHeaderTextPaint.alpha = DAY_PAST_ALPHA
                } else {
                    mHeaderTextPaint.alpha = NORMAL_ALPHA
                }
                canvas.drawText(dayLabel, x, mHeaderTextHeight + mHeaderRowPadding, mHeaderTextPaint)
            } else {
                mHeaderTextPaint.color = if (isSaturday && (mHolidays[day] == null || mHolidays[day] == false)) mHeaderSaturdayColumnTextColor else mHeaderSundayColumnTextColor
                if (day.before(today)) {
                    mHeaderTextPaint.alpha = DAY_PAST_ALPHA
                } else {
                    mHeaderTextPaint.alpha = NORMAL_ALPHA
                }

                // May be is draw in case of holiday, hmm... I'm still not understand what the below part code does
                // so I temporarily comment those and use my code
//                var strDay = TimeUtils.getDayOfMonth(day)
//                var strDay = day.get(Calendar.DAY_OF_MONTH).toString()
//                canvas.drawText(strDay, x, mHeaderTextHeight + mHeaderRowPadding, mHeaderTextPaint)
//                val rect = Rect()
//                strDay += "()"
//                mHeaderTextPaint.getTextBounds(strDay, 0, strDay.length, rect)
//                canvas.drawText(TimeUtils.getDayOfWeek(day), x + rect.width(), mHeaderTextHeight + mHeaderRowPadding, mHeaderTextPaint)

                // my temporary code
                canvas.drawText(dayLabel, x, mHeaderTextHeight + mHeaderRowPadding, mHeaderTextPaint)
            }
            startPixel += mWidthPerDay
        }
        // Draw the header row texts START
    }

    /**
     * Get the time and date where the user clicked on.
     *
     * @param x The x position of the touch event.
     * @param y The y position of the touch event.
     * @return The time and date at the clicked position.
     */
    private fun getTimeFromPoint(x: Float, y: Float): Calendar? {
        val leftDaysWithGaps = (-Math.ceil((mCurrentOrigin.x / mWidthPerDay).toDouble())).toInt()
        var startPixel = mCurrentOrigin.x + mWidthPerDay * leftDaysWithGaps +
                mHeaderColumnWidth
        for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + mNumberOfVisibleDays + 1) {
            val start = if (startPixel < mHeaderColumnWidth) mHeaderColumnWidth else startPixel
            if (mWidthPerDay + startPixel - start > 0 && x > start && x < startPixel + mWidthPerDay) {
                val day = TimeUtils.today()
                day.add(Calendar.DATE, dayNumber - 1)
                val pixelsFromZero = y - mCurrentOrigin.y - mHeaderHeight
                -(mHeaderRowPadding * 2).toFloat() - mTimeTextHeight / 2 - mHeaderMarginBottom
                val hour = (pixelsFromZero / mHourHeight).toInt()
                val minute = (60 * (pixelsFromZero - hour * mHourHeight) / mHourHeight).toInt()
                day.add(Calendar.HOUR, hour)
                day.set(Calendar.MINUTE, minute)
                return day
            }
            startPixel += mWidthPerDay
        }
        return null
    }

    /**
     * Draw all the events of a particular day.
     *
     * @param day           The day.
     * @param startFromPixel The left position of the day area. The events will never go any left from this value.
     * @param canvas         The canvas to draw upon.
     */
    private fun drawEvents(day: Calendar, startFromPixel: Float, canvas: Canvas) {
        if (mEventRects.size > 0) {
            for (i in mEventRects.indices) {
                if (day.isTheSameDay(mEventRects[i].event.startCalendar) && !mEventRects[i].event.allDay) {

                    // Calculate top.
                    var top = mEventRects[i].top
                    top = mHourHeight.toFloat() * 24f * top / 1440 + mCurrentOrigin.y + mHeaderHeight + mHeaderMarginBottom + mTimeTextHeight / 2 + mEventMarginVertical.toFloat() + DISTANCE_FROM_TOP
                    top += mEventSeparatorWidth

                    // Calculate bottom.
                    var bottom = mEventRects[i].bottom
                    bottom = mHourHeight.toFloat() * 24f * bottom / 1440 + mCurrentOrigin.y + mHeaderHeight + mHeaderMarginBottom + mTimeTextHeight / 2 - mEventMarginVertical + DISTANCE_FROM_TOP
                    bottom -= mEventSeparatorWidth

                    // Calculate left and right.
                    var left = startFromPixel + mEventRects[i].left * mWidthPerDay
                    if (left < startFromPixel)
                        left += mOverlappingEventGap.toFloat()
                    var right = left + mEventRects[i].width * mWidthPerDay - mEventSeparatorWidth - 2 * DEFAULT_STROKE_WIDTH
                    if (right < startFromPixel + mWidthPerDay)
                        right -= mOverlappingEventGap.toFloat()

                    // Draw the event and the event name on top of it.
                    if (left < right &&
                            left < width &&
                            top < height &&
                            right > mHeaderColumnWidth &&
                            bottom > mHeaderHeight + mTimeTextHeight / 2 + mHeaderMarginBottom) {
                        if (bottom - top < 4) {
                            top -= 4 - bottom + top
                        }
                        mEventRects[i].rectF = RectF(left, top, right, bottom)
                        mEventBackgroundPaint.color = Color.parseColor(mEventRects[i].event.color)
                        if (mEventRects[i].originalEvent.endCalendar.timeInMillis < System.currentTimeMillis()) {
                            mEventBackgroundPaint.alpha = PAST_ALPHA
//                            mEventTextPaint.alpha = PAST_ALPHA
                        }
                        canvas.drawRoundRect(mEventRects[i].rectF, mEventCornerRadius.toFloat(), mEventCornerRadius.toFloat(), mEventBackgroundPaint)
                        drawEventTitle(mEventRects[i].event, mEventRects[i].rectF!!, canvas, top, left)
                        mEventBackgroundPaint.alpha = NORMAL_ALPHA
//                        mEventTextPaint.alpha = NORMAL_ALPHA
                    } else {
                        mEventRects[i].rectF = null
                    }
                }
            }
        }
    }

    /**
     * Draw all the all-day events of a particular day.
     *
     * @param day           The day.
     * @param startFromPixel The left position of the day area. The events will never go any left from this value.
     * @param canvas         The canvas to draw upon.
     * @param columnIndex
     */
    private fun drawAllDayEvents(day: Calendar, startFromPixel: Float, canvas: Canvas, columnIndex: Int) {
        if (mEventRects.size > 0) {
            var stopped = false
            var moreAllDayEventIndex = -1
            for (i in mEventRects.indices) {
                if (mEventRects[i].event.allDay && day.isTheSameDay(mEventRects[i].event.startCalendar)) {
                    if (day.isTheSameDay(mEventRects[i].originalEvent.startCalendar)) {
                        // Calculate top.
                        var top: Float = (if (mNeedToScrollAllDayEvents) mCurrentOrigin.y else 0F) + (mHeaderRowPadding * 2).toFloat() + mTimeTextHeight / 2 + mEventMarginVertical.toFloat() + mHeaderRowPadding.toFloat()
                        var validPosition = 0
                        while (mPositionFilled[columnIndex].contains(validPosition)) {
                            validPosition++
                        }

                        var daysBetween = TimeUtils.daysBetween(mEventRects[i].originalEvent.startCalendar,
                                mEventRects[i].originalEvent.endCalendar)
                        val endColumn = columnIndex + daysBetween - 1
                        val canDrawInEndRow = checkCanDrawInEndRow(columnIndex, endColumn, validPosition)
                        if (validPosition >= mMaxVisibleAllDayEventNum - 1 && !canDrawInEndRow) {
                            daysBetween = 1
                            validPosition = mMaxVisibleAllDayEventNum - 1
                        }
                        if (stopped) {
                            continue
                        }

                        top += (validPosition * mAllDayEventItemHeight) + ((mEventPadding / 2) * (validPosition + 1))

                        // Calculate bottom.
                        val bottom = top + mAllDayEventItemHeight

                        // Calculate left and right.
                        val right = startFromPixel + mWidthPerDay * daysBetween - mEventSeparatorWidth * 4

                        // Draw the event and the event name on top of it.
                        if (startFromPixel < right && top < height && bottom > 0) {
                            mEventRects[i].rectF = RectF(startFromPixel, top, right, bottom)
                            mEventBackgroundPaint.color = Color.parseColor(mEventRects[i].event.color)
                            if (validPosition >= mMaxVisibleAllDayEventNum - 1 && !canDrawInEndRow) {
                                val oldColor = mEventTextPaint.color
                                mEventTextPaint.color = mHeaderColumnTextColor
                                val rect = Rect()
                                val text = context.getString(R.string.AP4001_AP4002_more_event, mAllDayEventNumArray[columnIndex] - getFilledRowNum(columnIndex))
                                mEventTextPaint.getTextBounds(text, 0, text.length, rect)
                                val x = startFromPixel + mWidthPerDay - rect.width().toFloat() - mHeaderColumnPadding.toFloat()
                                val y = top + mEventTextSize + mEventPadding / 2
                                canvas.drawText(text, x, y, mEventTextPaint)
                                mEventTextPaint.color = oldColor
                                mToggleList.add(mEventRects[i].rectF!!)
                                stopped = true
                            } else {
                                if (mEventRects[i].originalEvent.endCalendar.before(TimeUtils.today())) {
                                    mEventBackgroundPaint.alpha = PAST_ALPHA
//                                    mEventTextPaint.alpha = PAST_ALPHA
                                }
                                canvas.drawRoundRect(mEventRects[i].rectF, mEventCornerRadius.toFloat(), mEventCornerRadius.toFloat(), mEventBackgroundPaint)
                                drawEventTitle(mEventRects[i].event, mEventRects[i].rectF!!, canvas, top, startFromPixel)
                                mEventBackgroundPaint.alpha = NORMAL_ALPHA
//                                mEventTextPaint.alpha = NORMAL_ALPHA
                            }
                        } else {
                            mEventRects[i].rectF = null
                        }
                    } else if (mLimitedAllDayEvents && !canDrawInEndRow(mAllDayEventNumArray[columnIndex])) {
                        val p: Int = getMoreAllDayEventIndex(columnIndex)
                        if (p > -1) {
                            moreAllDayEventIndex = p
                        }
                    }
                }
            }
            if (mLimitedAllDayEvents && !stopped && moreAllDayEventIndex > -1) {
                drawMoreAllDayEvents(startFromPixel, moreAllDayEventIndex, columnIndex, canvas)
            }
        }
    }

    /**
     * Returns true if an all-day event can be drawn in the end row of a particular day.
     *
     * @param allDayEventNum the number of all-day events of a particular day.
     * @return
     */
    private fun canDrawInEndRow(allDayEventNum: Int): Boolean {
        return mMaxVisibleAllDayEventNum >= allDayEventNum
    }

    /**
     * For drawing the all-day events, checks if whether it can draw an all-day event in the end row or the end row should be used to draw the text indicating the other events which can't be displayed.
     *
     * @param columnIndex the start column of event
     * @param endColumn   the end column of event
     * @param rowIndex    the current row index
     * @return
     */
    private fun checkCanDrawInEndRow(columnIndex: Int, endColumn: Int, rowIndex: Int): Boolean {
        var canDrawInEndRow = true
        var j = columnIndex
        while (j < mPositionFilled.size && j <= endColumn) {
            mPositionFilled[j].add(rowIndex)
            canDrawInEndRow = canDrawInEndRow && canDrawInEndRow(mAllDayEventNumArray[j])
            j++
        }
        return if (!mLimitedAllDayEvents) true else canDrawInEndRow
    }

    /**
     * Return the position that will be used to draw the text indicating the other events which can't be displayed.
     *
     * @param columnIndex the column index of the day
     * @return
     */
    private fun getMoreAllDayEventIndex(columnIndex: Int): Int {
        val rowSet = mPositionFilled[columnIndex]
        return rowSet.firstOrNull { it >= mMaxVisibleAllDayEventNum - 1 } ?: -1
    }

    /**
     * Returns the number of all-day events which has already drawn of a particular day.
     *
     * @param columnIndex the column index of the day
     * @return
     */
    private fun getFilledRowNum(columnIndex: Int): Int {
        val rowSet = mPositionFilled[columnIndex]
        return (0 until mMaxVisibleAllDayEventNum - 1)
                .count { rowSet.contains(it) }
    }

    /**
     * Draws the text indicating the other events which can't be displayed of a particular day.
     *
     * @param startFromPixel The left position of the day area. The events will never go any left from this value.
     * @param eventIndex
     * @param columnIndex
     * @param canvas The canvas to draw upon.
     * @return
     */
    private fun drawMoreAllDayEvents(startFromPixel: Float, eventIndex: Int, columnIndex: Int, canvas: Canvas): Boolean {
        var top = (mHeaderRowPadding * 2).toFloat() + mTimeTextHeight / 2 + mEventMarginVertical.toFloat() + mHeaderRowPadding.toFloat()
        top += ((mMaxVisibleAllDayEventNum - 1) * mAllDayEventItemHeight)
        // Calculate bottom.
        val bottom = top + mAllDayEventItemHeight - 4
        // Calculate left and right.
        val right = startFromPixel + mWidthPerDay

        // Draw the event and the event name on top of it.
        if (startFromPixel < right && top < height && bottom > 0) {
            mEventBackgroundPaint.color = Color.parseColor(mEventRects[eventIndex].event.color)
            val oldColor = mEventTextPaint.color
            mEventTextPaint.color = mHeaderColumnTextColor
            val rect = Rect()
            val text = context.getString(R.string.AP4001_AP4002_more_event, mAllDayEventNumArray[columnIndex] - getFilledRowNum(columnIndex))
            mEventTextPaint.getTextBounds(text, 0, text.length, rect)
            val x = startFromPixel + mWidthPerDay - rect.width().toFloat() - mHeaderColumnPadding.toFloat()
            val y = top + mHeaderTextHeight - mEventPadding.toFloat()
            canvas.drawText(text, x, y, mEventTextPaint)
            mEventTextPaint.color = oldColor
            mEventRects[eventIndex].rectF = RectF(startFromPixel, top, right, bottom)
            mToggleList.add(mEventRects[eventIndex].rectF!!)
            return true
        } else {
            mEventRects[eventIndex].rectF = null
        }
        return false
    }

    /**
     * Toggles the value of `'mLimitedAllDayEvents'`, then refreshes the week view.
     */
    private fun toggleAllDayEvents() {
        mLimitedAllDayEvents = !mLimitedAllDayEvents
        notifyDataSetChanged()
    }

    /**
     * Draw the name of the event on top of the event rectangle.
     *
     * @param event        The event of which the title (and location) should be drawn.
     * @param rect         The rectangle on which the text is to be drawn.
     * @param canvas       The canvas to draw upon.
     * @param originalTop  The original top position of the rectangle. The rectangle may have some of its portion outside of the visible area.
     * @param originalLeft The original left position of the rectangle. The rectangle may have some of its portion outside of the visible area.
     */
    private fun drawEventTitle(event: EventSummary, rect: RectF, canvas: Canvas, originalTop: Float, originalLeft: Float) {
        if (rect.right - rect.left - (mEventPadding * 2).toFloat() < 0) return
        if (rect.bottom - rect.top - mEventPadding * 2 < 0) return
        if (rect.bottom - rect.top < 0) return

        // Prepare the name of the event.
        val title = SpannableStringBuilder()
        when (event.calendarType) {
            DJCalendarEnum.Type.MY -> {
                title.append(event.title)
            }
            DJCalendarEnum.Type.STAFF -> {
                title.append(event.title)
            }
            DJCalendarEnum.Type.INSIDE_GROUP -> {
                title.append(event.title)
            }
            DJCalendarEnum.Type.OUTSIDE_GROUP -> {
                title.append(event.title)
            }
            DJCalendarEnum.Type.MEETING_DECIDED,
            DJCalendarEnum.Type.RQ_MEETING_MEDIATOR -> {
                val office = event.mr?.office ?: ""
                val name = if (event.mr?.name.isNullOrEmpty()) "" else "(${event.mr?.name})"
                title.append(context.getString(R.string.AP4002_TITLE_MEETING_DECIDED, office, name))
            }
            DJCalendarEnum.Type.MEETING_ACCEPTING -> {
                title.append(context.getString(R.string.AP4002_TITLE_MEETING_ACCEPTING))
            }
            DJCalendarEnum.Type.MEETING_MEDIATOR -> {
                title.append(context.getString(R.string.AP4002_TITLE_MEETING_MEDIATOR))
            }
            DJCalendarEnum.Type.BRIEFING -> {
                val office = "#briefing"
                val name = ""
                title.append(context.getString(R.string.AP4002_TITLE_BRIEFING, office, name))
            }
            DJCalendarEnum.Type.VISIT -> {
                val visitor = event.title
                title.append(context.getString(R.string.AP4002_TITLE_VISIT, visitor))
            }
            DJCalendarEnum.Type.SOCIETY -> {
                title.append(context.getString(R.string.AP4002_TITLE_SOCIETY, event.title))
            }
            DJCalendarEnum.Type.HOLIDAY -> {
                title.append(event.title)
            }
            DJCalendarEnum.Type.EXTERNAL -> {
                title.append(event.title)
            }
        }

        val availableHeight = (rect.bottom - originalTop - (mEventPadding * 2))
        val availableWidth = (rect.right - originalLeft - (mEventPadding * 2))

        if (event.allDay) {
            // Prepare to draw text.
            val metrics = BoringLayout.Metrics()
            metrics.width = Math.ceil(mEventTextPaint.measureText(title, 0, title.length).toDouble()).toInt()
            val temp = mEventTextPaint.fontMetricsInt
            metrics.ascent = temp.ascent
            metrics.bottom = temp.bottom
            metrics.descent = temp.descent
            metrics.top = temp.top
            metrics.leading = temp.leading
            val textLayout = BoringLayout.make(ellipsize(title, rect.right - originalLeft + mEventPadding * 2), mEventTextPaint, (rect.right - originalLeft - (mEventPadding * 2).toFloat()).toInt(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 1.0f, metrics, false)
            // Draw text.
            canvas.save()
            canvas.clipRect(originalLeft + mEventPadding, originalTop, rect.right, originalTop + mAllDayEventItemHeight)
            canvas.translate(originalLeft + mEventPadding, originalTop + (mAllDayEventItemHeight - (if (mEventTextSize >= 10) 0 else 4) - textLayout.height) / 2)
            textLayout.draw(canvas)
            canvas.restore()
        } else {
            val rec = Rect()
            val strName = "MR"
            mEventTextPaint.getTextBounds(strName, 0, strName.length, rec)
            var textLayout = StaticLayout(title, mEventTextPaint, availableWidth.toInt(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false)

            val lineHeight = textLayout.height / textLayout.lineCount
            val availableLineCount = Math.max(Math.ceil(availableHeight / lineHeight.toDouble()), 1.0).toInt()
            textLayout = StaticLayout(ellipsize(title, (availableLineCount * availableWidth)), mEventTextPaint, (rect.right - originalLeft - (mEventPadding * 2).toFloat()).toInt(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false)

            canvas.save()
            canvas.clipRect(originalLeft + mEventPadding, originalTop - mEventSeparatorWidth + mEventPadding, rect.right, rect.bottom + mEventPadding)
            canvas.translate(originalLeft + mEventPadding, originalTop - mEventSeparatorWidth + mEventPadding)
            textLayout.draw(canvas)
            canvas.restore()
        }
    }

    fun ellipsize(original: CharSequence, avail: Float): CharSequence {
        return ellipsize(original, avail, mEventTextPaint)
    }

    fun ellipsize(original: CharSequence, avail: Float, textPaint: TextPaint): CharSequence {
        if (avail <= 0) {
            return ""
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            try {
                val c = Class.forName("android.text.TextUtils")
                val m = c.getMethod("ellipsize", CharSequence::class.java, TextPaint::class.java, Float::class.javaPrimitiveType, TextUtils.TruncateAt::class.java, Boolean::class.javaPrimitiveType, TextUtils.EllipsizeCallback::class.java, TextDirectionHeuristic::class.java, String::class.java)
                return m.invoke(null, original, textPaint, avail + 14, TextUtils.TruncateAt.END, false, null, TextDirectionHeuristics.FIRSTSTRONG_LTR, "") as CharSequence
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return TextUtils.ellipsize(original, textPaint, avail, TextUtils.TruncateAt.END)
    }

    /**
     * Gets more events of one/more month(s) if necessary. This method is called when the user is
     * scrolling the week view. The week view stores the events of three months: the visible month,
     * the previous month, the next month.
     *
     * @param day The day where the user is currently is.
     */
    private fun getMoreEvents(day: Calendar) {

        // Get more events if the month is changed.
        if (mWeekViewLoader == null && !isInEditMode) {
            throw IllegalStateException("You must provide a MonthChangeListener")
        }
        // If a refresh was requested then reset some variables.
        if (mRefreshEvents) {
            mEventRects.clear()
//            mPreviousPeriodEvents = null
            mCurrentPeriodEvents = null
//            mNextPeriodEvents = null
            mFetchedPeriod = -1
        }

        val periodToFetch: Int = TimeUtils.toWeekViewPeriodIndex(day).toInt()
        if (!isInEditMode && (mFetchedPeriod < 0 || mFetchedPeriod != periodToFetch || mRefreshEvents)) {
            var currentPeriodEvents: List<EventSummary>? = null

            if (currentPeriodEvents == null) {
                currentPeriodEvents = mWeekViewLoader?.onLoad(periodToFetch)
            }

            // Clear events.
            mEventRects.clear()
            sortAndCacheEvents(currentPeriodEvents!!)
            determineHolidays()
            calculateHeaderHeight()

            mCurrentPeriodEvents = currentPeriodEvents
            mFetchedPeriod = periodToFetch
        }

        // Prepare to calculate positions of each events.
        val tempEvents = mEventRects
        mEventRects = ArrayList()

        // Iterate through each day with events to calculate the position of the events.
        while (tempEvents.size > 0) {
            val eventRects = ArrayList<EventRect>(tempEvents.size)

            // Get first event for a day.
            val eventRect1 = tempEvents.removeAt(0)
            eventRects.add(eventRect1)

            var i = 0
            while (i < tempEvents.size) {
                // Collect all other events for same day.
                val eventRect2 = tempEvents[i]
                if (eventRect1.event.startCalendar.isTheSameDay(eventRect2.event.startCalendar)) {
                    tempEvents.removeAt(i)
                    eventRects.add(eventRect2)
                } else {
                    i++
                }
            }
            computePositionOfEvents(eventRects)
        }
    }

    /**
     * Cache the event for smooth scrolling functionality.
     *
     * @param event The event to cache.
     */
    private fun cacheEvent(event: EventSummary) {
        if (!event.allDay && event.startCalendar >= event.endCalendar)
            return
        val splitedEvents = event.splitEvents()
        for (splitedEvent in splitedEvents) {
            mEventRects.add(EventRect(splitedEvent, event, null))
        }
    }

    /**
     * Sort and cache events.
     *
     * @param events The events to be sorted and cached.
     */
    private fun sortAndCacheEvents(events: List<EventSummary>) {
        val sortedEvents = sortEvents(events)
        for (event in sortedEvents) {
            cacheEvent(event)
        }
    }

    /**
     * Sorts the events in ascending order.
     *
     * @param events The events to be sorted.
     */
    private fun sortEvents(events: List<EventSummary>): List<EventSummary> {
        val allDayEventList = ArrayList<EventSummary>()
        val normalEventList = ArrayList<EventSummary>()

        for (event in events) {
            if (event.allDay) {
                allDayEventList.add(event)
            } else {
                normalEventList.add(event)
            }
        }
        Collections.sort(normalEventList) { event1, event2 ->
            val start1 = event1.startCalendar.timeInMillis
            val start2 = event2.startCalendar.timeInMillis
            var comparator = if (start1 > start2) 1 else if (start1 < start2) -1 else 0
            if (comparator == 0) {
                val end1 = event1.endCalendar.timeInMillis
                val end2 = event2.endCalendar.timeInMillis
                comparator = if (end1 > end2) 1 else if (end1 < end2) -1 else 0
            }
            return@sort comparator
        }
        normalEventList.addAll(allDayEventList)
        return normalEventList
    }

    /**
     * Calculates the left and right positions of each events. This comes handy specially if events
     * are overlapping.
     *
     * @param eventRects The events along with their wrapper class.
     */
    private fun computePositionOfEvents(eventRects: List<EventRect>) {
        // Make "collision groups" for all events that collide with others.
        val collisionGroups = ArrayList<ArrayList<EventRect>>()
        for (eventRect in eventRects) {
            var isPlaced = false

            outerLoop@ for (collisionGroup in collisionGroups) {
                for (groupEvent in collisionGroup) {
                    if (isEventsCollide(groupEvent.event, eventRect.event) && groupEvent.event.allDay == eventRect.event.allDay) {
                        collisionGroup.add(eventRect)
                        isPlaced = true
                        break@outerLoop
                    }
                }
            }

            if (!isPlaced) {
                val newGroup = ArrayList<EventRect>()
                newGroup.add(eventRect)
                collisionGroups.add(newGroup)
            }
        }

        for (collisionGroup in collisionGroups) {
            expandEventsToMaxWidth(collisionGroup)
        }
    }

    /**
     * Expands all the events to maximum possible width. The events will try to occupy maximum
     * space available horizontally.
     *
     * @param collisionGroup The group of events which overlap with each other.
     */
    private fun expandEventsToMaxWidth(collisionGroup: List<EventRect>) {
        // Expand the events to maximum possible width.
        val columns = ArrayList<ArrayList<EventRect>>()
        columns.add(ArrayList())
        for (eventRect in collisionGroup) {
            var isPlaced = false
            for (column in columns) {
                if (column.isEmpty()) {
                    column.add(eventRect)
                    isPlaced = true
                } else if (!isEventsCollide(eventRect.event, column.last().event)) {
                    column.add(eventRect)
                    isPlaced = true
                    break
                }
            }
            if (!isPlaced) {
                val newColumn = ArrayList<EventRect>()
                newColumn.add(eventRect)
                columns.add(newColumn)
            }
        }

        // Calculate left and right position for all the events.
        // Get the maxRowCount by looking in all columns.
        val maxRowCount = columns
                .map { it.size }
                .max()
                ?: 0
        for (i in 0 until maxRowCount) {
            // Set the left and right values of the event.
            var j = 0f
            for (column in columns) {
                if (column.size >= i + 1) {
                    val eventRect = column[i]
                    eventRect.width = 1f / columns.size
                    eventRect.left = j / columns.size
                    if (!eventRect.event.allDay) {
                        //#13750
                        val hourStart = eventRect.event.startCalendar.get(Calendar.HOUR_OF_DAY)
                        var minuteStart = eventRect.event.startCalendar.get(Calendar.MINUTE)
                        if (hourStart == 23 && minuteStart >= 45) {
                            minuteStart = 45
                        }
                        eventRect.top = (hourStart * 60 + minuteStart).toFloat()
                        var hourEnd = eventRect.event.endCalendar.get(Calendar.HOUR_OF_DAY)
                        var minuteEnd = eventRect.event.endCalendar.get(Calendar.MINUTE)
                        if (hourEnd == 0) {
                            if (minuteEnd == 0) {
                                hourEnd = 23
                                minuteEnd = 59
                            }
                            if (minuteEnd <= 15) {
                                minuteEnd = 15
                            }
                        }

                        if (hourStart == hourEnd && minuteEnd - minuteStart < BLOCK) {
                            minuteEnd = minuteStart + 15
                        }

                        eventRect.bottom = (hourEnd * 60 + minuteEnd).toFloat()
                        if (eventRect.top == eventRect.bottom) {
                            eventRect.top--
                        }
                    } else {
                        eventRect.top = 0f
                        eventRect.bottom = mAllDayEventHeight.toFloat()
                    }
                    mEventRects.add(eventRect)
                }
                j++
            }
        }
    }

    /**
     * Checks if two events overlap.
     *
     * @param event1 The first event.
     * @param event2 The second event.
     * @return true if the events overlap.
     */
    private fun isEventsCollide(event1: EventSummary, event2: EventSummary): Boolean {
        val start1 = event1.startCalendar.zeroSECONDAndMILLSECOND()
        val end1 = event1.endCalendar.zeroSECONDAndMILLSECOND()
        val start2 = event2.startCalendar.zeroSECONDAndMILLSECOND()
        val end2 = event2.endCalendar.zeroSECONDAndMILLSECOND()

        return !(start1 >= end2 || end1 <= start2)
    }

    private fun Calendar.zeroSECONDAndMILLSECOND(): Long {
        return apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Checks if time1 occurs after (or at the same time) time2.
     *
     * @param time1 The time to check.
     * @param time2 The time to check against.
     * @return true if time1 and time2 are equal or if time1 is after time2. Otherwise false.
     */
    private fun isTimeAfterOrEquals(time1: Calendar?, time2: Calendar?): Boolean {
        return !(time1 == null || time2 == null) && time1.timeInMillis >= time2.timeInMillis
    }

    override fun invalidate() {
        super.invalidate()
        mAreDimensionsInvalid = true
    }

    private fun determineHolidays() {
        mHolidays.clear()

        var i = 0
        while (i < mEventRects.size) {
            val event = mEventRects[i].event
            val day = event.startCalendar.clone() as Calendar
            day.set(Calendar.HOUR_OF_DAY, 0)
            day.set(Calendar.MINUTE, 0)
            day.set(Calendar.SECOND, 0)
            day.set(Calendar.MILLISECOND, 0)
            if (mHolidays[day] == null || mHolidays[day] == false) {
                mHolidays.put(day, event.calendarType == DJCalendarEnum.Type.HOLIDAY)
            }
            if (!event.visible) {
                mEventRects.removeAt(i)
                i--
            }
            i++
        }
    }

    fun setMonthChangeListener(monthChangeListener: MonthLoader.MonthChangeListener) {
        this.mWeekViewLoader = MonthLoader(monthChangeListener)
    }

    /**
     * Reload calendar setting
     * @param fontSize
     * @param firstDayOfWeek It can be SUNDAY: 0 or MONDAY: 1
     */
    fun updateCalendarSettings(fontSize: Float, firstDayOfWeek: Int) {
        var isSettingChanged = false

        val newFontSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, fontSize, context.resources.displayMetrics).toInt()
        if (mEventTextSize != newFontSize) {
            mEventTextSize = newFontSize
            mEventTextPaint.textSize = mEventTextSize.toFloat()
            mAllDayEventItemHeight = DimensionUtils.dpToPx(fontSize * 1.75F)
            isSettingChanged = true
        }

        val firstDayOfWeekInCalendar = if (firstDayOfWeek == TimeUtils.SUNDAY) {
            Calendar.SUNDAY
        } else {
            Calendar.MONDAY
        }
        if (mFirstDayOfWeek != firstDayOfWeekInCalendar) {
            mFirstDayOfWeek = firstDayOfWeekInCalendar
            isSettingChanged = true
        }

        if (isSettingChanged) {
            invalidate()
        }
    }

    /**
     * A class to hold reference to the events and their visual representation. An EventRect is
     * actually the rectangle that is drawn on the calendar for a given event. There may be more
     * than one rectangle for a single event (an event that expands more than one day). In that
     * case two instances of the EventRect will be used for a single event. The given event will be
     * stored in "originalEvent". But the event that corresponds to rectangle the rectangle
     * instance will be stored in "event".
     */
    private inner class EventRect
    /**
     * Create a new instance of event rect. An EventRect is actually the rectangle that is drawn
     * on the calendar for a given event. There may be more than one rectangle for a single
     * event (an event that expands more than one day). In that case two instances of the
     * EventRect will be used for a single event. The given event will be stored in
     * "originalEvent". But the event that corresponds to rectangle the rectangle instance will
     * be stored in "event".
     *
     * @param event         Represents the event which this instance of rectangle represents.
     * @param originalEvent The original event that was passed by the user.
     * @param rectF         The rectangle.
     */
    (var event: EventSummary, var originalEvent: EventSummary, var rectF: RectF?) {
        var left: Float = 0F
        var width: Float = 0F
        var top: Float = 0F
        var bottom: Float = 0F
    }

    /////////////////////////////////////////////////////////////////
    //
    //      Functions related to scrolling.
    //
    /////////////////////////////////////////////////////////////////

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val result = mGestureDetector.onTouchEvent(event)

        // Check after call of mGestureDetector, so mCurrentFlingDirection and mCurrentScrollDirection are set.
        if (event.action == MotionEvent.ACTION_UP && mCurrentFlingDirection == Direction.NONE) {
            if (mCurrentScrollDirection == Direction.RIGHT || mCurrentScrollDirection == Direction.LEFT) {
                goToNearestOrigin()
            } else {
                if (mScrollStateChangeListener != null) {
                    mScrollStateChangeListener?.onScrollSateChanged(true)
                }
            }
            mCurrentScrollDirection = Direction.NONE
        }

        return result
    }

    private fun goToNearestOrigin() {
        var leftDays = (mCurrentOrigin.x / mWidthPerDay).toDouble()

        leftDays = when {
            mCurrentFlingDirection != Direction.NONE -> // snap to nearest day
                Math.round(leftDays).toDouble()
            mCurrentScrollDirection == Direction.LEFT -> // snap to last day
                Math.floor(leftDays)
            mCurrentScrollDirection == Direction.RIGHT -> // snap to next day
                Math.ceil(leftDays)
            else -> // snap to nearest day
                Math.round(leftDays).toDouble()
        }

        if (mCurrentScrollDirection == Direction.NONE) {
            mScrollStateChangeListener?.onScrollSateChanged(false)
        } else if (mCurrentScrollDirection == Direction.LEFT || mCurrentScrollDirection == Direction.RIGHT || mCurrentScrollDirection == Direction.VERTICAL) {
            mScrollStateChangeListener?.onScrollSateChanged(true)
        }

        val nearestOrigin = (mCurrentOrigin.x - leftDays * mWidthPerDay).toInt()

        if (nearestOrigin != 0) {
            // Stop current animation.
            mScroller?.forceFinished(true)
            // Snap to date.
            mScroller?.startScroll(mCurrentOrigin.x.toInt(), mCurrentOrigin.y.toInt(), -nearestOrigin, 0, (Math.abs(nearestOrigin) / mWidthPerDay * mScrollDuration).toInt())
            ViewCompat.postInvalidateOnAnimation(this@WeekView)
        }
        // Reset scrolling and fling direction.
        mCurrentFlingDirection = Direction.NONE
        mCurrentScrollDirection = mCurrentFlingDirection
    }

    override fun computeScroll() {
        super.computeScroll()

        if (mScroller == null) {
            return
        }

        if (mScroller!!.isFinished) {
            if (mCurrentFlingDirection != Direction.NONE) {
                // Snap to day after fling is finished.
                goToNearestOrigin()
            }
        } else {
            if (mCurrentFlingDirection != Direction.NONE && forceFinishScroll()) {
                goToNearestOrigin()
            } else if (mScroller!!.computeScrollOffset()) {
                mCurrentOrigin.y = mScroller!!.currY.toFloat()
                mCurrentOrigin.x = mScroller!!.currX.toFloat()
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }
    }

    /**
     * Check if scrolling should be stopped.
     *
     * @return true if scrolling should be stopped before reaching the end of animation.
     */
    private fun forceFinishScroll(): Boolean = false

    /////////////////////////////////////////////////////////////////
    //
    //      Public methods.
    //
    /////////////////////////////////////////////////////////////////

    /**
     * Show today on the week view.
     */
    fun goToToday() {
        mEventRects.clear()
        val today = Calendar.getInstance()
        goToDate(today)
    }

    /**
     * Show a specific day on the week view.
     *
     * @param date The date to show.
     */
    fun goToDate(date: Calendar) {
        mScroller?.forceFinished(true)
        mCurrentFlingDirection = Direction.NONE
        mCurrentScrollDirection = mCurrentFlingDirection

        date.set(Calendar.HOUR_OF_DAY, 0)
        date.set(Calendar.MINUTE, 0)
        date.set(Calendar.SECOND, 0)
        date.set(Calendar.MILLISECOND, 0)

        if (mAreDimensionsInvalid) {
            mScrollToDay = date
            return
        }

        mRefreshEvents = true

        val today = TimeUtils.today()

        val day = DateUtils.DAY_IN_MILLIS
        val dateInMillis = date.timeInMillis + date.timeZone.getOffset(date.timeInMillis)
        val todayInMillis = today.timeInMillis + today.timeZone.getOffset(today.timeInMillis)
        val dateDifference = dateInMillis / day - todayInMillis / day
        mCurrentOrigin.x = -dateDifference * mWidthPerDay
        invalidate()
    }

    /**
     * Refreshes the view and loads the events again.
     */
    fun notifyDataSetChanged() {
        mRefreshEvents = true
        invalidate()
    }

    /**
     * Vertically scroll to a specific hour in the week view.
     *
     * @param hour The hour to scroll to in 24-hour format. Supported values are 0-24.
     */
    fun goToHour(hour: Double) {
        if (mAreDimensionsInvalid) {
            mScrollToHour = hour
            return
        }

        var verticalOffset = 0
        if (hour > 24)
            verticalOffset = mHourHeight * 24
        else if (hour > 0)
            verticalOffset = (mHourHeight * hour).toInt()

        if (verticalOffset > (mHourHeight * 24 - height).toFloat() + mHeaderHeight + mHeaderMarginBottom)
            verticalOffset = ((mHourHeight * 24 - height).toFloat() + mHeaderHeight + mHeaderMarginBottom).toInt()

        mCurrentOrigin.y = (-verticalOffset).toFloat()
        invalidate()
    }

    /**
     * Get the first hour that is visible on the screen.
     *
     * @return The first hour that is visible.
     */
    fun getFirstVisibleHour(): Double {
        return (-mCurrentOrigin.y / mHourHeight).toDouble()
    }

    fun clearEvents() {
        mEventRects.clear()
    }

    /////////////////////////////////////////////////////////////////
    //
    //      Interfaces.
    //
    /////////////////////////////////////////////////////////////////

    interface EventTouchListener {

        /**
         * Triggered when touched on WeekView
         */
        fun onDown()
    }

    interface EventClickListener {
        /**
         * Triggered when clicked on one existing event
         *
         * @param event:     event clicked.
         * @param eventRect: view containing the clicked event.
         */
        fun onEventClick(event: EventSummary, eventRect: RectF)
    }

    interface EventLongPressListener {
        /**
         * Similar to [EventClickListener] but with a long press.
         *
         * @param event:     event clicked.
         * @param eventRect: view containing the clicked event.
         */
        fun onEventLongPress(event: EventSummary, eventRect: RectF)
    }

    interface EmptyViewClickListener {
        /**
         * Triggered when the users clicks on a empty space of the calendar.
         *
         * @param time: [Calendar] object set with the date and time of the clicked position on the view.
         */
        fun onEmptyViewClicked(time: Calendar)
    }

    interface EmptyViewLongPressListener {
        /**
         * Similar to [EmptyViewClickListener] but with long press.
         *
         * @param time: [Calendar] object set with the date and time of the long pressed position on the view.
         */
        fun onEmptyViewLongPress(time: Calendar)
    }

    interface ScrollListener {
        /**
         * Called when the first visible day has changed.
         *
         *
         * (this will also be called during the first draw of the weekView)
         *
         * @param newFirstVisibleDay The new first visible day
         * @param oldFirstVisibleDay The old first visible day (is null on the first call).
         */
        fun onFirstVisibleDayChanged(newFirstVisibleDay: Calendar, oldFirstVisibleDay: Calendar)
    }

    interface TitleChangeListener {
        /**
         * Called when the first visible day has changed.
         *
         * @param calendar The first visible day
         */
        fun onTitleChange(calendar: Calendar)
    }

    interface ScrollStateChangeListener {
        /**
         * Triggered when the scroll state of weekview is changed
         *
         * @param isIdle true if the state is idling and false if state is dragging or fling
         */
        fun onScrollSateChanged(isIdle: Boolean)
    }

    interface InitListener {
        /**
         * Called after the first draw of view
         */
        fun onViewCreated()
    }
}