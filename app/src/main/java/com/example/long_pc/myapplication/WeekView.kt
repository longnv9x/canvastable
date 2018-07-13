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
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.OverScroller
import com.example.long_pc.myapplication.model.EventSummary
import com.example.long_pc.myapplication.model.ShiftData
import com.example.long_pc.myapplication.model.ShiftItem
import com.example.long_pc.myapplication.model.Staff
import jp.drjoy.app.domain.model.shift.Memo
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
        const val DEFAULT_STROKE_WIDTH = 1F
        const val DEFAULT_SPACE_WIDTH = 5F
        val STROKE_HIGHLIGHT_COLOR = Color.parseColor("#c7666666")
        const val DISTANCE_FROM_TOP = 45
        const val BLOCK = 15
        const val DEFAULT_EXPAND_WIDTH = 20
        const val DEFAULT_EXPAND_HEIGHT = 11
        const val DEFAULT_ICON_NOTE_WIDTH = 26
        const val DEFAULT_ICON_NOTE_HEIGHT = 26
    }

    private var NumberStaff: Int = 100
    private var mContext: Context
    private val mTeamTextPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private var mTeamTextWidth: Float = 0F
    private var mTeamCorrectBackground: Float = 0F
    private var mTeamTextPadding: Float = 0F
    private var mTeamTextHeight: Float = 0F
    private val mNameTextPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private var mNameTextWidth: Float = 0F
    private var mNameTextHeight: Float = 0F
    private val mHeaderDayTextPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private var mHeaderDayTextHeight: Float = 0F
    private var mHeaderHeight: Float = 0F
    private val mDepartmentPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private var mDepartmentTextWidth: Float = 0F
    private var mDepartmentTextHeight: Float = 0F
    private val mHeaderLinePaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
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
    private val mEventBackgroundPaint: Paint by lazy { Paint() }
    private val mHeaderTextMemoPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private var mHeaderTextMemoHeight: Float = 0F
    private var mHeaderTextMemoWidth: Float = 0F
    private val mHeaderTextEvenOfDayPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mDepartmentBackgroundPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mBackgroundCellPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mTitleCellPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }

    /**
     * Width of the first column (time 01:00 - 23:00)
     */
    private var mHeaderColumnWidth: Float = 0F
    private var mCurentDepartment: String? = null
    private var mCurrentPeriodEvents: ShiftData? = null
    private val mEventTextPaint: TextPaint by lazy { TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.LINEAR_TEXT_FLAG) }
    private val mHeaderColumnBackgroundPaint: Paint by lazy { Paint() }
    private var mFetchedPeriod = -1 // the middle period the calendar has fetched.
    private var mRefreshEvents = false
    private var mCurrentFlingDirection = Direction.NONE

    private val mBitmapCollapse = ViewUtils.getBitmapFromXml(context, R.drawable.ic_arrow_up)
    private val mBitmapExpand = ViewUtils.getBitmapFromXml(context, R.drawable.ic_arrow_down)
    private val mBitmapPublic = ViewUtils.getBitmapFromXml(context, R.drawable.ic_shift_ic_memo_public)
    private val mBitmapPrivate = ViewUtils.getBitmapFromXml(context, R.drawable.ic_shift_ic_memo_public)
    private val mPaintBitmapExpandCollapse = Paint()
    private val mDestRectBitmapExpandCollapse: Rect = Rect()
    private val mSrcRectBitmapExpandCollapse: Rect = Rect()

    private val mAllDayText = context.getString(R.string.AP4002_LBL_1)
    private val mAllDayTextPaint: TextPaint by lazy { TextPaint(Paint.ANTI_ALIAS_FLAG) }
    private var mAllDayTextWidth: Float = 0F
    private var mAllDayTextHeight: Float = 0F
    private var mEventRects: ArrayList<EventRect> = ArrayList()
    private var mMemos: ArrayList<Memo> = ArrayList()
    private var mShiftItems: ArrayList<ArrayList<ShiftItem>> = ArrayList()
    private var mStaffs: ArrayList<Staff> = ArrayList()
    fun getLastPositionStaff(): Int {
        return if (mStaffs.size > 0) mStaffs.size - 1 else 0
    }

    fun getStaffSize(): Int {
        return mStaffs.size
    }

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

    var mTextSizeDay = 16
        set(value) {
            field = value
            mHeaderDayTextPaint.textSize = mTextSizeDay.toFloat()
            invalidate()
        }

    var mTextSizeMemo = 10
        set(value) {
            field = value
            mHeaderTextMemoPaint.textSize = mTextSizeMemo.toFloat()
            invalidate()
        }

    private var mTextSizeDepartment = 16
        set(value) {
            field = value
            mDepartmentPaint.textSize = mTextSizeDepartment.toFloat()
            invalidate()
        }

    private var mTextSizeName = 10
        set(value) {
            field = value
            mNameTextPaint.textSize = mTextSizeName.toFloat()
            invalidate()
        }
    private var mTextSizeTeam = 10
        set(value) {
            field = value
            mTeamTextPaint.textSize = mTextSizeTeam.toFloat()
            invalidate()
        }
    private var mTextSizeEvenOfDay = 10
        set(value) {
            field = value
            mHeaderTextEvenOfDayPaint.textSize = mTextSizeEvenOfDay.toFloat()
            invalidate()
        }

    var mHeaderColumnPadding = 10
        set(value) {
            field = value
            invalidate()
        }

    var mHeaderLineColor = Color.rgb(218, 222, 223)
        set(value) {
            field = value
            mHeaderLinePaint.color = mHeaderLineColor
            invalidate()
        }
    var mHeaderColumnTextColor = Color.BLACK
        set(value) {
            field = value
            mHeaderDayTextPaint.color = mHeaderColumnTextColor
            mNameTextPaint.color = mHeaderColumnTextColor
            mHeaderTextEvenOfDayPaint.color = mHeaderColumnTextColor
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

    var mDepartmentTextColor = Color.rgb(241, 141, 0)
        set(value) {
            field = value
            mDepartmentPaint.color = field
            invalidate()
        }

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
    var mDepartmentBackgroundColor = Color.BLACK
        set(value) {
            field = value
            mDepartmentBackgroundPaint.color = field
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
    private var mPositionFilled: Array<MutableSet<Int>> = Array(BUFFER_DAY * 2 + mNumberOfVisibleDays) { ArraySet<Int>() }

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
                    mScroller?.fling(mCurrentOrigin.x.toInt(), mCurrentOrigin.y.toInt(), (velocityX * mXScrollingSpeed).toInt(), 0, Integer.MIN_VALUE, Integer.MAX_VALUE, (-((mHourHeight * getStaffSize()).toFloat() + mHeaderHeight + mHeaderMarginBottom + mNameTextHeight / 2 - height)).toInt(), 0)
                    ViewCompat.postInvalidateOnAnimation(this@WeekView)
                }
                Direction.VERTICAL -> if (e1.y > mHeaderHeight || mNeedToScrollAllDayEvents) {
                    mScroller?.fling(mCurrentOrigin.x.toInt(), mCurrentOrigin.y.toInt(), 0, velocityY.toInt(), Integer.MIN_VALUE, Integer.MAX_VALUE, if (mNeedToScrollAllDayEvents) mMinYWhenScrollingAllDayEvents else (-((mHourHeight * getStaffSize()).toFloat() + mHeaderHeight + mHeaderMarginBottom + mNameTextHeight / 2 - height)).toInt(), 0)
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
                for (eventRect in reversedEventRects) {
                    if (e.y <= mHeaderHeight) {
                        continue
                    }
                    if (eventRect.rectF != null && e.x > eventRect.rectF!!.left && e.x < eventRect.rectF!!.right && e.y > eventRect.rectF!!.top && e.y < eventRect.rectF!!.bottom) {
                        // mEventClickListener?.onEventClick(eventRect.event, eventRect.rectF!!)
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
                for (eventRect in reversedEventRects) {
                    if (eventRect.rectF != null && e.x > eventRect.rectF!!.left && e.x < eventRect.rectF!!.right && e.y > eventRect.rectF!!.top && e.y < eventRect.rectF!!.bottom) {
                        // mEventLongPressListener?.onEventLongPress(eventRect.event, eventRect.rectF!!)
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
            mTextSizeDay = a.getDimensionPixelSize(R.styleable.WeekView_textSizeDay, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSizeDay.toFloat(), context.resources.displayMetrics).toInt())
            mTextSizeName = a.getDimensionPixelSize(R.styleable.WeekView_textSizeNameStaff, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSizeName.toFloat(), context.resources.displayMetrics).toInt())
            mTextSizeDepartment = a.getDimensionPixelSize(R.styleable.WeekView_textSizeDepartment, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSizeDepartment.toFloat(), context.resources.displayMetrics).toInt())
            mTextSizeMemo = a.getDimensionPixelSize(R.styleable.WeekView_textSizeMemo, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSizeMemo.toFloat(), context.resources.displayMetrics).toInt())
            mTextSizeTeam = a.getDimensionPixelSize(R.styleable.WeekView_textSizeTeam, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSizeTeam.toFloat(), context.resources.displayMetrics).toInt())
            mTextSizeEvenOfDay = a.getDimensionPixelSize(R.styleable.WeekView_textSizeEventOfDay, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSizeEvenOfDay.toFloat(), context.resources.displayMetrics).toInt())
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
            mDepartmentBackgroundColor = a.getColor(R.styleable.WeekView_departmentColumnBackground, mHeaderColumnBackgroundColor)
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
        mNameTextPaint.getTextBounds("00 PM", 0, "00 PM".length, rect)
        mNameTextHeight = rect.height().toFloat()
        mHeaderMarginBottom = 0f
        mNameTextPaint.textAlign = Paint.Align.LEFT
        mNameTextPaint.color = mHeaderColumnTextColorTime
        mTeamTextPaint.textAlign = Paint.Align.LEFT
        mTeamTextPaint.color = Color.WHITE
        mTeamCorrectBackground = 5F
        mTeamTextPadding = 5F
        mTeamTextHeight = mNameTextHeight
        // Measure settings for header row.
        mHeaderDayTextPaint.color = mHeaderColumnTextColor
        mHeaderDayTextPaint.textAlign = Paint.Align.CENTER
        mHeaderDayTextPaint.getTextBounds("00 PM", 0, "00 PM".length, rect)
        mHeaderDayTextHeight = rect.height().toFloat()

        mHeaderTextMemoPaint.color = mHeaderColumnTextColor
        mHeaderTextMemoPaint.textAlign = Paint.Align.LEFT
        mHeaderTextMemoPaint.getTextBounds("00 PM", 0, "00 PM".length, rect)
        mHeaderTextMemoHeight = rect.height().toFloat()

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
        //  mDayBackgroundPaint.pathEffect = DashPathEffect(floatArrayOf(7f, 7f), 0f)
        mDayBackgroundPaint.strokeWidth = DEFAULT_STROKE_WIDTH

        mHourSeparatorPaint.color = mHourSeparatorColor
        mHourSeparatorPaint.style = Paint.Style.STROKE
        //   mHourSeparatorPaint.pathEffect = DashPathEffect(floatArrayOf(7f, 7f), 0f)
        mHourSeparatorPaint.strokeWidth = DEFAULT_STROKE_WIDTH
        mTodayBackgroundPaint.color = mTodayBackgroundColor

        mEventBackgroundPaint.color = Color.rgb(174, 208, 238)

        // Prepare header column background color.
        mHeaderColumnBackgroundPaint.color = mHeaderColumnBackgroundColor
        mHeaderColumnBackgroundPaint.textAlign = Paint.Align.LEFT
        // Prepare event text size and color.
        mEventTextPaint.style = Paint.Style.FILL
        mEventTextPaint.color = mEventTextColor
        mEventTextPaint.textSize = mEventTextSize.toFloat()

        mDepartmentPaint.textAlign = Paint.Align.CENTER
        mDepartmentPaint.color = mDepartmentTextColor
        mCurentDepartment = "救急救命科"
        mDepartmentPaint.getTextBounds("00 PM", 0, "00 PM".length, rect)
        mDepartmentTextHeight = rect.height().toFloat()
        mDepartmentBackgroundPaint.color = mDepartmentBackgroundColor
        mDepartmentBackgroundPaint.style = Paint.Style.STROKE
        mDepartmentBackgroundPaint.strokeWidth = DEFAULT_STROKE_WIDTH
        val today = Calendar.getInstance()
        goToDate(today)
        val color = Color.rgb(148, 102, 175)
        mStaffs = arrayListOf()
        (0..99).forEach {
            mStaffs.add(Staff().apply {
                this.memberName = "苗字 名前 $it"
                this.teamShortName = "大内"
                this.teamColor = color
                this.officeUserId = "officeUserId$it"
            })
        }
        val day = TimeUtils.today()
        (0..99).forEach {
            day.add(Calendar.DATE, it)
            when {
                it % 3 == 0 -> mMemos.add(Memo().apply {
                    this.publicNote = "苗字"
                    this.targetDate = day.time
                })
                else -> mMemos.add(Memo().apply {
                    this.targetDate = day.time
                    this.publicNote = "苗字"
                    this.privateNote = "苗字"
                })
            }
        }
        mHeaderLinePaint.color = mHeaderLineColor
        mHeaderTextEvenOfDayPaint.color = mHeaderColumnTextColor
        mHeaderTextEvenOfDayPaint.textAlign = Paint.Align.CENTER
        initTextTimeWidth()
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
        mNameTextWidth = mStaffs
                .map {
                    // Measure time string and get max width.
                    //  mDateTimeInterpreter.interpretTime(it)
                    return@map it.memberName
                }
                .map { mNameTextPaint.measureText(it) }
                .max()
                ?: 0f
        mTeamTextWidth = mStaffs
                .map {
                    // Measure time string and get max width.
                    //  mDateTimeInterpreter.interpretTime(it)
                    return@map it.teamShortName
                }
                .map { mTeamTextPaint.measureText(it) }
                .max()
                ?: 0f
        mDepartmentTextWidth = mDepartmentPaint.measureText(mCurentDepartment)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(mBackgroundColor)
        val yLineTopView = DEFAULT_STROKE_WIDTH / 2 + DISTANCE_FROM_TOP
        val startXYineTopView = 0f
        val stopXYineTopView = width.toFloat()
        canvas.drawLine(startXYineTopView, yLineTopView, stopXYineTopView, yLineTopView, mHeaderLinePaint)
        canvas.drawLine(0F, height - DEFAULT_STROKE_WIDTH / 2, width.toFloat(), height - DEFAULT_STROKE_WIDTH / 2, mHeaderLinePaint)

        drawHeaderRowAndEvents(canvas)

        drawStaffColumnAndAxes(canvas)

    }


    var isMemoPublicAndPrivate = false
    var isMemoPublicOrPrivate = false

    /**
     * Calculates the height of the header.
     */
    private fun calculateHeaderHeightAndColumnWidth() {
        initTextTimeWidth()
        // Calculate the available width for each day.
        mHeaderColumnWidth = mNameTextWidth + mTeamTextWidth + mHeaderColumnPadding * 2 + mTeamTextPadding * 2
        mWidthPerDay = width.toFloat() - mHeaderColumnWidth
        mWidthPerDay /= mNumberOfVisibleDays
        //Make sure the header is the right size (depends on AllDay events)
        mNeedToScrollAllDayEvents = false
        if (mFirstVisibleDay != null && mMemos.isNotEmpty()) {
            mMaxInColumn = 0
            mMaxInColumnToShowToggleButton = 0
            var memoTemp: ArrayList<Pair<Boolean, Boolean>> = ArrayList()
            for (dayNumber in 0 until mNumberOfVisibleDays) {
                val day = mFirstVisibleDay?.clone() as Calendar
                day.add(Calendar.DATE, dayNumber)
                Log.e("day", "$dayNumber -----${day.get(Calendar.DAY_OF_MONTH)}")
                mMemos.firstOrNull { day.isTheSameDay(it.targetDate!!) }?.let {
                    val isMemoPublic = !it.publicNote.isNullOrEmpty()
                    val isMemoPrivate = !it.privateNote.isNullOrEmpty()
                    memoTemp.add(Pair(isMemoPublic, isMemoPrivate))
                }
            }
            when {
                memoTemp.contains { it.first && it.second } -> {
                    isMemoPublicAndPrivate = true
                    isMemoPublicOrPrivate = false
                }
                memoTemp.contains { it.first } || memoTemp.contains { it.second } -> {
                    isMemoPublicAndPrivate = false
                    isMemoPublicOrPrivate = true
                }
                else -> {
                    isMemoPublicAndPrivate = false
                    isMemoPublicOrPrivate = false
                }
            }
        }

        val headerMemo = when {
            isMemoPublicAndPrivate -> DEFAULT_ICON_NOTE_HEIGHT * 2 + mHeaderRowPadding * 3
            isMemoPublicOrPrivate -> DEFAULT_ICON_NOTE_WIDTH + mHeaderRowPadding * 2
            else -> 0
        }
        mHeaderHeight = mHeaderDayTextHeight * 2 + mHeaderRowPadding * 4 + headerMemo + mHeaderDayTextHeight / 2
    }

    /**
     * Draws the time column and all the axes/separators.
     *
     * @param canvas
     */
    private fun drawStaffColumnAndAxes(canvas: Canvas) {
        // Draw the background color for the header column.
        val topRectBackgroundLeft = mHeaderHeight + DISTANCE_FROM_TOP
        canvas.drawRect(0f, topRectBackgroundLeft, mHeaderColumnWidth + DEFAULT_STROKE_WIDTH, height.toFloat(), mHeaderColumnBackgroundPaint)

        // Clip to paint in time column only.
        val topClipRectLeft = topRectBackgroundLeft
        canvas.clipRect(0f, topClipRectLeft, mHeaderColumnWidth + DEFAULT_STROKE_WIDTH, height.toFloat(), Region.Op.REPLACE)
        // draw line Header left full height
        run {
            val x = mHeaderColumnWidth - DEFAULT_STROKE_WIDTH
            val topLineLeft = mHeaderHeight + DISTANCE_FROM_TOP
            canvas.drawLine(x, topLineLeft, x, height.toFloat(), mHeaderLinePaint)
        }
        if (mNeedToScrollAllDayEvents) {
            return
        }

        mStaffs.indices.forEach { i ->
            // Draw view header left
            drawHeaderLeftByPosition(i, canvas)
        }
        //draw line bottom left
        val topLine = mHeaderHeight + mCurrentOrigin.y + (mHourHeight * mStaffs.size).toFloat() + mHeaderMarginBottom + DISTANCE_FROM_TOP
        canvas.drawLine(0f, topLine, mHeaderColumnWidth, topLine, mHeaderLinePaint)
        val maxScroll = mHeaderHeight + mCurrentOrigin.y + (mHourHeight * mStaffs.size).toFloat() + mHeaderMarginBottom + DISTANCE_FROM_TOP
        val rect = RectF(0f, topLine, mHeaderColumnWidth, maxScroll)
        mHeaderColumnBackgroundPaint.color = Color.WHITE
        canvas.drawRoundRect(rect, 0f, 0f, mHeaderColumnBackgroundPaint)
    }

    private fun drawHeaderLeftByPosition(pos: Int, canvas: Canvas) {
        // Draw line header left
        val topLine = mHeaderHeight + mCurrentOrigin.y + (mHourHeight * pos).toFloat() + mHeaderMarginBottom + DISTANCE_FROM_TOP + DEFAULT_STROKE_WIDTH
        val name = mStaffs[pos].memberName
        val teamColor = mStaffs[pos].teamColor
        val teamName = mStaffs[pos].teamShortName
        // Draw view header left
        val topText = mHeaderHeight + mCurrentOrigin.y + (mHourHeight * pos).toFloat() + mHourHeight / 2 + mHeaderMarginBottom + DISTANCE_FROM_TOP + mTeamTextHeight / 2
        val leftTeamText = mHeaderColumnPadding.toFloat() + DEFAULT_SPACE_WIDTH
        val leftNameText = leftTeamText + DEFAULT_SPACE_WIDTH * 2 + mTeamTextWidth
        val maxHeightDraw = height + mHourHeight + mHeaderMarginBottom + DEFAULT_STROKE_WIDTH
        if (topLine < maxHeightDraw) {
            // draw line
            canvas.drawLine(0f, topLine, mHeaderColumnWidth, topLine, mHeaderLinePaint)
        }
        if (topText < maxHeightDraw) {
            if (pos % 3 == 0) {
                //draw background Cell
                val topLineBellow = mHeaderHeight + mCurrentOrigin.y + (mHourHeight * (pos + 1)).toFloat() + mHeaderMarginBottom + DISTANCE_FROM_TOP
                val rect = RectF(0f, topLine, mHeaderColumnWidth - DEFAULT_STROKE_WIDTH, topLineBellow)
                mHeaderColumnBackgroundPaint.color = Color.CYAN
                canvas.drawRoundRect(rect, 0f, 0f, mHeaderColumnBackgroundPaint)
            }
            // draw background team text
            val background = getTextBackgroundSize(mHeaderColumnPadding.toFloat(), topText, teamName!!, mTeamTextPaint)
            mHeaderColumnBackgroundPaint.color = teamColor!!
            canvas.drawRoundRect(background, mTeamCorrectBackground, mTeamCorrectBackground, mHeaderColumnBackgroundPaint)
            canvas.drawText(teamName, leftTeamText, topText, mTeamTextPaint)
            mHeaderColumnBackgroundPaint.color = Color.WHITE
            canvas.drawText(name, leftNameText, topText, mNameTextPaint)
        }
    }

    private fun getTextBackgroundSize(x: Float, y: Float, text: String, paint: Paint): RectF {
        val fontMetrics = paint.fontMetrics
        val halfTextLength = paint.measureText(text) + DEFAULT_SPACE_WIDTH * 2
        return RectF(x, y + fontMetrics.top, x + halfTextLength, y + fontMetrics.bottom)
    }

    /**
     * Draws the header row.
     *
     * @param canvas
     */
    private fun drawHeaderRowAndEvents(canvas: Canvas) {

        calculateHeaderHeightAndColumnWidth() //Make sure the header is the right size (depends on AllDay events)

        val today = TimeUtils.today()

        if (mAreDimensionsInvalid) {
            mAreDimensionsInvalid = false
            if (mScrollToDay != null)
                goToDate(mScrollToDay!!)

            mAreDimensionsInvalid = false
            if (mScrollToHour >= 0)
                goToStaff(mScrollToHour)

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
        } else if (mCurrentOrigin.y < height.toFloat() - (mHourHeight * getStaffSize()).toFloat() - mHeaderHeight - (mHeaderRowPadding * 2).toFloat() - mHeaderMarginBottom - mNameTextHeight / 2) {
            mCurrentOrigin.y = height.toFloat() - (mHourHeight * getStaffSize()).toFloat() - mHeaderHeight - (mHeaderRowPadding * 2).toFloat() - mHeaderMarginBottom - mNameTextHeight / 2
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
        val topClipRectContent = mHeaderHeight + DISTANCE_FROM_TOP
        canvas.clipRect(mHeaderColumnWidth, topClipRectContent, width.toFloat(), height.toFloat(), Region.Op.REPLACE)

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
                mRefreshEvents = true
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

                dashPath.moveTo(start, mHeaderHeight + mNameTextHeight / 2 + mHeaderMarginBottom)
                dashPath.lineTo(start, height.toFloat())
                canvas.drawPath(dashPath, mDayBackgroundPaint)
                dashPath.reset()
                if (sameDay) {
                    canvas.drawRect(start + DEFAULT_STROKE_WIDTH, mHeaderHeight + mNameTextHeight / 2 + mHeaderMarginBottom, startPixel + mWidthPerDay - DEFAULT_STROKE_WIDTH, height - DEFAULT_STROKE_WIDTH / 2, mTodayBackgroundPaint)
                }
            }

            if (!mNeedToScrollAllDayEvents) {
                // Draw the lines for staff.
                val path = Path()
                mStaffs.indices.forEach { pos ->
                    val top = mHeaderHeight + mCurrentOrigin.y + (mHourHeight * (pos + 1)).toFloat() + mHeaderMarginBottom + DISTANCE_FROM_TOP + DEFAULT_STROKE_WIDTH
                    if (top > mHeaderHeight + mNameTextHeight / 2 + mHeaderMarginBottom - mHourSeparatorHeight && top < height && startPixel + mWidthPerDay - start > 0) {
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

            // In the next iteration, start from the next day.
            startPixel += mWidthPerDay
        }
        //Draw events END


        //Draw header, time, column START
        mHeaderBackgroundPaint.alpha = NORMAL_ALPHA

        // Draw the header first cell
        drawHeaderFistCell(canvas)

        // Draw the partial of line separating the header (not include all day event) and all-day events
//        run {
//            val y: Float
//            if (mHasAllDayEvents) {
//                y = mHeaderDayTextHeight + mHeaderRowPadding * 2
//                canvas.drawLine(0f, y, mHeaderColumnWidth + DEFAULT_STROKE_WIDTH, y, mAllDayEventSeparatorPaint)
//            } else {
//                y = mHeaderDayTextHeight + mHeaderRowPadding * 2 - DEFAULT_STROKE_WIDTH / 2
//                canvas.drawLine(0f, y, mHeaderColumnWidth + DEFAULT_STROKE_WIDTH, y, mAllDayEventSeparatorPaint)
//            }
//        }

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
            var rectF = RectF(mHeaderColumnWidth / 2 - width / 3, 0f, 0f, mAllDayEventHeight.toFloat() + mHeaderColumnPadding.toFloat() + mHeaderDayTextHeight)
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

        val topRect = DISTANCE_FROM_TOP
        val bottomRect = topRect + mHeaderHeight
        // Clip to paint header row only.
        canvas.clipRect(mHeaderColumnWidth + DEFAULT_STROKE_WIDTH, topRect.toFloat(), width.toFloat(), bottomRect, Region.Op.REPLACE)

        // Draw the header background.
        canvas.drawRect(0f, topRect.toFloat(), width.toFloat(), bottomRect, mVisibleHeaderBackgroundPaint)

        // Draw the line in the top of week view
        canvas.drawLine(mHeaderColumnWidth, topRect.toFloat(), width.toFloat(), topRect.toFloat(), mHeaderLinePaint)

        //Draw the line separating the header (include all day event section) and normal event section
        run {
            val y = bottomRect
            canvas.drawLine(mHeaderColumnWidth, y, width.toFloat(), y, mHeaderLinePaint)
        }

        // Draw background for the all-day section.
        startPixel = startFromPixel
        for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + mNumberOfVisibleDays + 1) {
            // Check if the day is today.
            day = today.clone() as Calendar
            day.add(Calendar.DATE, dayNumber - 1)

            //draw background all day section if the day is today
            if (day.isTheSameDay(today)) {
                canvas.drawRect(startPixel + DEFAULT_STROKE_WIDTH, DEFAULT_STROKE_WIDTH, startPixel + mWidthPerDay - DEFAULT_STROKE_WIDTH, (height - DEFAULT_STROKE_WIDTH), mTodayBackgroundPaint)
            }
            startPixel += mWidthPerDay
        }
        //Draw header, time, column END
        //-------------------------------------------------------------------------------------------------------------------------------//

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
//                day = today.clone() as Calendar
//                day.add(Calendar.DATE, dayNumber - 1)
                dayNumber++
                i++
            }
        }

//        run {
//            var dayNumber = leftDaysWithGaps + 1
//            var i = 0
//            while (dayNumber <= leftDaysWithGaps + BUFFER_DAY * 2 + mNumberOfVisibleDays && i < mPositionFilled.size) {
//                day = today.clone() as Calendar
//                day.add(Calendar.DATE, dayNumber - 1)
//                //drawAllDayEvents(day, startPixel + mEventSeparatorWidth * 2, canvas, i)
//                startPixel += mWidthPerDay
//                dayNumber++
//                i++
//            }
//        }
        // Draws all-day events END

        // Draw the separator between weeks START
        startPixel = startFromPixel
        for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + mNumberOfVisibleDays + 1) {
            day = today.clone() as Calendar
            day.add(Calendar.DATE, dayNumber - 1)

            // Draw the line bettwen two day section
            val startYLineBetweenTwoDay = DISTANCE_FROM_TOP
            val stopYLineBetweenTwoDay = DISTANCE_FROM_TOP + mHeaderHeight
            canvas.drawLine(startPixel, startYLineBetweenTwoDay.toFloat(), startPixel, stopYLineBetweenTwoDay, mHeaderLinePaint)


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
            drawHeaderTop(day, startPixel, isSaturday, isSunday, today, canvas)
            startPixel += mWidthPerDay
        }
    }

    private fun drawHeaderTop(day: Calendar, startPixel: Float, isSaturday: Boolean, isSunday: Boolean, today: Calendar, canvas: Canvas) {
        // draw text day header
        var publicNote: String? = null
        var privateNote: String? = null
        mMemos.firstOrNull { it.targetDate!!.isTheSameDay(day) }?.let {
            publicNote = it.publicNote
            privateNote = it.privateNote
        }
        val dayLabel = mDateTimeInterpreter.interpretDate(day)
        val leftDayText = startPixel + (mWidthPerDay / 2)
        val topDayText = mHeaderDayTextHeight + mHeaderRowPadding + DISTANCE_FROM_TOP
        if (!isSaturday && !isSunday && (mHolidays[day] == null || mHolidays[day] == false)) {
            mHeaderDayTextPaint.color = mHeaderColumnTextColor
            if (day.before(today)) {
                mHeaderDayTextPaint.alpha = DAY_PAST_ALPHA
            } else {
                mHeaderDayTextPaint.alpha = NORMAL_ALPHA
            }
            canvas.drawText(dayLabel, leftDayText, topDayText, mHeaderDayTextPaint)
        } else {
            mHeaderDayTextPaint.color = if (isSaturday && (mHolidays[day] == null || mHolidays[day] == false)) mHeaderSaturdayColumnTextColor else mHeaderSundayColumnTextColor
            if (day.before(today)) {
                mHeaderDayTextPaint.alpha = DAY_PAST_ALPHA
            } else {
                mHeaderDayTextPaint.alpha = NORMAL_ALPHA
            }
            canvas.drawText(dayLabel, leftDayText, topDayText, mHeaderDayTextPaint)
        }
        // draw line day header line bellow of day text
        val startXLineBellowOfDayText = startPixel
        val stopXLineBellowOfDayText = startPixel + mWidthPerDay
        val startYLineBellowOfDayText = topDayText + mHeaderRowPadding
        canvas.drawLine(startXLineBellowOfDayText, startYLineBellowOfDayText, stopXLineBellowOfDayText, startYLineBellowOfDayText, mHeaderLinePaint)
        //draw memo header

        // draw note public
        val leftIconNotePublic = startPixel + mHeaderRowPadding
        val topIconNotePublic: Float
        val rightIconNotePublic = leftIconNotePublic + DEFAULT_ICON_NOTE_WIDTH
        val bottomIconNotePublic: Float
        when {
            (isMemoPublicAndPrivate || isMemoPublicOrPrivate) && !publicNote.isNullOrEmpty() -> {
                topIconNotePublic = startYLineBellowOfDayText + mHeaderRowPadding
                bottomIconNotePublic = topIconNotePublic + DEFAULT_ICON_NOTE_HEIGHT
            }
            else -> {
                topIconNotePublic = startYLineBellowOfDayText
                bottomIconNotePublic = startYLineBellowOfDayText
            }
        }
        val destRectPublic = RectF(leftIconNotePublic, topIconNotePublic, rightIconNotePublic, bottomIconNotePublic)
        val leftTextNotePublic = rightIconNotePublic + DEFAULT_SPACE_WIDTH
        val topTextNotePublic = topIconNotePublic + DEFAULT_ICON_NOTE_HEIGHT / 2 + mHeaderTextMemoHeight / 2
        if (!publicNote.isNullOrEmpty()) {
            canvas.drawBitmap(mBitmapPublic, null, destRectPublic, mHeaderBackgroundPaint)
            canvas.drawText(publicNote, leftTextNotePublic, topTextNotePublic, mHeaderTextMemoPaint)
        }
        // draw note private
        val topIconNotePrivate: Float
        val leftIconNotePrivate = startPixel + mHeaderRowPadding
        val rightIconNotePrivate = leftIconNotePrivate + DEFAULT_ICON_NOTE_WIDTH
        val bottomIconNotePrivate: Float
        when {
            (isMemoPublicAndPrivate || isMemoPublicOrPrivate) && !publicNote.isNullOrEmpty() && !privateNote.isNullOrEmpty() -> {
                topIconNotePrivate = bottomIconNotePublic + mHeaderRowPadding
                bottomIconNotePrivate = topIconNotePrivate + DEFAULT_ICON_NOTE_HEIGHT
            }
            (isMemoPublicAndPrivate || isMemoPublicOrPrivate) && publicNote.isNullOrEmpty() && !privateNote.isNullOrEmpty() -> {
                topIconNotePrivate = startYLineBellowOfDayText + mHeaderRowPadding
                bottomIconNotePrivate = topIconNotePrivate + DEFAULT_ICON_NOTE_HEIGHT
            }
            (isMemoPublicAndPrivate || isMemoPublicOrPrivate) && !publicNote.isNullOrEmpty() && privateNote.isNullOrEmpty() -> {
                topIconNotePrivate = bottomIconNotePublic + mHeaderRowPadding
                bottomIconNotePrivate = topIconNotePrivate + DEFAULT_ICON_NOTE_HEIGHT
            }
            else -> {
                topIconNotePrivate = startYLineBellowOfDayText
                bottomIconNotePrivate = startYLineBellowOfDayText
            }
        }

        val destRectPrivate = RectF(leftIconNotePrivate, topIconNotePrivate, rightIconNotePrivate, bottomIconNotePrivate)
        val leftTextNotePrivate = rightIconNotePrivate + DEFAULT_SPACE_WIDTH
        val topTextNotePrivate = topIconNotePrivate + DEFAULT_ICON_NOTE_HEIGHT / 2 + mHeaderTextMemoHeight / 2
        if (!privateNote.isNullOrEmpty()) {
            canvas.drawBitmap(mBitmapPrivate, null, destRectPrivate, mHeaderBackgroundPaint)
            canvas.drawText(privateNote, leftTextNotePrivate, topTextNotePrivate, mHeaderTextMemoPaint)
        }

        // draw line below memmo
        val startXLineBellowOfMemo = startPixel
        val stopXLineBellowOfMemo = startPixel + mWidthPerDay
        val startYLineBellowOfMemo: Float
        when {
            isMemoPublicAndPrivate -> {
                startYLineBellowOfMemo = startYLineBellowOfDayText + DEFAULT_ICON_NOTE_HEIGHT * 2 + mHeaderRowPadding * 3
                canvas.drawLine(startXLineBellowOfMemo, startYLineBellowOfMemo, stopXLineBellowOfMemo, startYLineBellowOfMemo, mHeaderLinePaint)
            }
            isMemoPublicOrPrivate -> {
                startYLineBellowOfMemo = startYLineBellowOfDayText + DEFAULT_ICON_NOTE_HEIGHT + mHeaderRowPadding * 2
                canvas.drawLine(startXLineBellowOfMemo, startYLineBellowOfMemo, stopXLineBellowOfMemo, startYLineBellowOfMemo, mHeaderLinePaint)
            }
            else -> {
                startYLineBellowOfMemo = startYLineBellowOfDayText
            }
        }

        // draw text morning of day
        val leftTextMorning = startPixel + mWidthPerDay / 6
        val topTextMorning = startYLineBellowOfMemo + mHeaderRowPadding + mHeaderDayTextHeight
        val textMorning = "午前"
        canvas.drawText(textMorning, leftTextMorning, topTextMorning, mHeaderTextEvenOfDayPaint)
        //draw line bettwen text
        val startXLineBetweenMN = startPixel + mWidthPerDay / 3
        val startYLineBetweenMN = startYLineBellowOfMemo
        val stopYLineBetweenMN = mHeaderHeight + DISTANCE_FROM_TOP
        canvas.drawLine(startXLineBetweenMN, startYLineBetweenMN, startXLineBetweenMN, stopYLineBetweenMN, mHeaderLinePaint)
        //draw text noon of day
        val leftTextNoon = startPixel + mWidthPerDay / 2
        val topTextNoon = topTextMorning
        val textNoon = "午後"
        canvas.drawText(textNoon, leftTextNoon, topTextNoon, mHeaderTextEvenOfDayPaint)
        //draw line bettwen text
        val startXLineBetweenNN = startPixel + mWidthPerDay * 2 / 3
        val startYLineBetweenNN = startYLineBellowOfMemo
        val stopYLineBetweenNN = stopYLineBetweenMN
        canvas.drawLine(startXLineBetweenNN, startYLineBetweenNN, startXLineBetweenNN, stopYLineBetweenNN, mHeaderLinePaint)
        //draw text night of day
        val leftTextNight = startPixel + mWidthPerDay * 5 / 6
        val topTextNight = topTextMorning
        val textNight = "夜間"
        canvas.drawText(textNight, leftTextNight, topTextNight, mHeaderTextEvenOfDayPaint)
    }

    private fun drawHeaderFistCell(canvas: Canvas) {
        // draw header first cell (top left corner).
        val topClipRectFirstCell = DISTANCE_FROM_TOP
        val bottomClipRectFirstCell = topClipRectFirstCell + mHeaderHeight + DEFAULT_STROKE_WIDTH
        canvas.clipRect(0f, topClipRectFirstCell.toFloat(), mHeaderColumnWidth + DEFAULT_STROKE_WIDTH, bottomClipRectFirstCell, Region.Op.REPLACE)
        // Draw line right first cell
        val startXLineRightFirstCell = mHeaderColumnWidth
        val startYLineRightFirstCell = DISTANCE_FROM_TOP
        val stopYLineRightFirstCell = startYLineRightFirstCell + mHeaderHeight
        canvas.drawLine(startXLineRightFirstCell, startYLineRightFirstCell.toFloat(), startXLineRightFirstCell, stopYLineRightFirstCell, mHeaderLinePaint)
        // Draw line bottom first cell
        val startXLineBottomFirstCell = 0f
        val stopXLineBottomFirstCell = mHeaderColumnWidth + DEFAULT_STROKE_WIDTH
        val startYLineBottomFirstCell = mHeaderHeight + DISTANCE_FROM_TOP
        canvas.drawLine(startXLineBottomFirstCell, startYLineBottomFirstCell, stopXLineBottomFirstCell, startYLineBottomFirstCell, mHeaderLinePaint)
        // draw background first cell
        val rect = Rect()
        val topRectBackground = DISTANCE_FROM_TOP
        val bottomRectBackground = topRectBackground + DEFAULT_STROKE_WIDTH + mHeaderHeight
        val rightRectBackground = mHeaderColumnWidth + DEFAULT_STROKE_WIDTH
        val leftRectBackground = DEFAULT_STROKE_WIDTH
        rect.set(leftRectBackground.toInt(), topRectBackground.toInt(), rightRectBackground.toInt(), bottomRectBackground.toInt())
        canvas.drawRect(rect, mDepartmentBackgroundPaint)
        // drawText first cell
        mDepartmentTextWidth = mDepartmentPaint.measureText(mCurentDepartment)
        val topText = mHeaderHeight / 2 + DISTANCE_FROM_TOP + mDepartmentTextHeight / 2
        val leftText = mHeaderColumnWidth * 2 / 5
        val topIconExpand = mHeaderHeight / 2 + DISTANCE_FROM_TOP - mDepartmentTextHeight / 4
        val leftIconExpand = leftText + mDepartmentTextWidth / 2 + DEFAULT_SPACE_WIDTH
        val rightIconExpand = leftIconExpand + DEFAULT_EXPAND_WIDTH
        val bottomIconExpand = topIconExpand + DEFAULT_EXPAND_HEIGHT
        canvas.drawText(mCurentDepartment, leftText, topText, mDepartmentPaint)
        val destRect = RectF(leftIconExpand, topIconExpand, rightIconExpand, bottomIconExpand)
        canvas.drawBitmap(mBitmapExpand, null, destRect, mDepartmentPaint)
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
                -(mHeaderRowPadding * 2).toFloat() - mNameTextHeight / 2 - mHeaderMarginBottom
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
                if (mEventRects[i].event.date!!.isTheSameDay(day)) {
                    val event = mEventRects[i].event
                    val position = mStaffs.indexOfFirst { event.officeUserId == it.officeUserId }
                    if (position != -1) {
                        val leftMorning = startFromPixel - DEFAULT_STROKE_WIDTH
                        val topMorning = mHeaderHeight + mCurrentOrigin.y + (mHourHeight * position).toFloat() + mHeaderMarginBottom + DISTANCE_FROM_TOP + DEFAULT_STROKE_WIDTH
                        val rightMorning = leftMorning + mWidthPerDay / 3
                        val bottomMorning = mHeaderHeight + mCurrentOrigin.y + (mHourHeight * (position + 1)).toFloat() + mHeaderMarginBottom + DISTANCE_FROM_TOP - DEFAULT_STROKE_WIDTH
                        val leftAfternoon = startFromPixel + mWidthPerDay / 3
                        val topAfternoon = topMorning
                        val rightAfternoon = leftAfternoon + mWidthPerDay / 3
                        val bottomAfternoon = bottomMorning
                        val leftNight = startFromPixel + mWidthPerDay * 2 / 3
                        val topNight = topMorning
                        val rightNight = leftNight + mWidthPerDay / 3
                        val bottomNight = bottomMorning
                        val left = leftMorning
                        val top = topMorning
                        val right = rightNight
                        val bottom = bottomMorning
                        // Draw the event and the event name on top of it.
                        if (left < right &&
                                left < width &&
                                top < height &&
                                right > mHeaderColumnWidth &&
                                bottom > mHeaderHeight + DISTANCE_FROM_TOP) {
                            mEventRects[i].rectF = RectF(left, top, right, bottom)
                            val rectMAN = RectF(leftMorning, topMorning, rightAfternoon, bottomMorning)
                            canvas.drawText("dadasd", leftAfternoon, bottomAfternoon, mTitleCellPaint)
                            drawEventBackground(event.morning?.color, rectMAN, 0f, canvas)
                        } else {
                            mEventRects[i].rectF = null
                        }
                    }
                }
            }
        }
    }

    private fun drawEventBackground(color: Int?, rectF: RectF, radius: Float, canvas: Canvas) {
        mBackgroundCellPaint.color = color ?: Color.rgb(255, 205, 151)
        canvas.drawRoundRect(rectF, radius, radius, mEventBackgroundPaint)
    }

    /**
     * Draw all the all-day events of a particular day.
     *
     * @param day           The day.
     * @param startFromPixel The left position of the day area. The events will never go any left from this value.
     * @param canvas         The canvas to draw upon.
     * @param columnIndex
     */
//    private fun drawAllDayEvents(day: Calendar, startFromPixel: Float, canvas: Canvas, columnIndex: Int) {
//        if (mEventRects.size > 0) {
//            var stopped = false
//            var moreAllDayEventIndex = -1
//            for (i in mEventRects.indices) {
//                if (mEventRects[i].event.allDay && day.isTheSameDay(mEventRects[i].event.startCalendar)) {
//                    if (day.isTheSameDay(mEventRects[i].originalEvent.startCalendar)) {
//                        // Calculate top.
//                        var top: Float = (if (mNeedToScrollAllDayEvents) mCurrentOrigin.y else 0F) + (mHeaderRowPadding * 2).toFloat() + mNameTextHeight / 2 + mEventMarginVertical.toFloat() + mHeaderRowPadding.toFloat()
//                        var validPosition = 0
//                        while (mPositionFilled[columnIndex].contains(validPosition)) {
//                            validPosition++
//                        }
//
//                        var daysBetween = TimeUtils.daysBetween(mEventRects[i].originalEvent.startCalendar,
//                                mEventRects[i].originalEvent.endCalendar)
//                        val endColumn = columnIndex + daysBetween - 1
//                        val canDrawInEndRow = checkCanDrawInEndRow(columnIndex, endColumn, validPosition)
//                        if (validPosition >= mMaxVisibleAllDayEventNum - 1 && !canDrawInEndRow) {
//                            daysBetween = 1
//                            validPosition = mMaxVisibleAllDayEventNum - 1
//                        }
//                        if (stopped) {
//                            continue
//                        }
//
//                        top += (validPosition * mAllDayEventItemHeight) + ((mEventPadding / 2) * (validPosition + 1))
//
//                        // Calculate bottom.
//                        val bottom = top + mAllDayEventItemHeight
//
//                        // Calculate left and right.
//                        val right = startFromPixel + mWidthPerDay * daysBetween - mEventSeparatorWidth * 4
//
//                        // Draw the event and the event name on top of it.
//                        if (startFromPixel < right && top < height && bottom > 0) {
//                            mEventRects[i].rectF = RectF(startFromPixel, top, right, bottom)
//                            mEventBackgroundPaint.color = Color.parseColor(mEventRects[i].event.color)
//                            if (validPosition >= mMaxVisibleAllDayEventNum - 1 && !canDrawInEndRow) {
//                                val oldColor = mEventTextPaint.color
//                                mEventTextPaint.color = mHeaderColumnTextColor
//                                val rect = Rect()
//                                val text = context.getString(R.string.AP4001_AP4002_more_event, mAllDayEventNumArray[columnIndex] - getFilledRowNum(columnIndex))
//                                mEventTextPaint.getTextBounds(text, 0, text.length, rect)
//                                val x = startFromPixel + mWidthPerDay - rect.width().toFloat() - mHeaderColumnPadding.toFloat()
//                                val y = top + mEventTextSize + mEventPadding / 2
//                                canvas.drawText(text, x, y, mEventTextPaint)
//                                mEventTextPaint.color = oldColor
//                                mToggleList.add(mEventRects[i].rectF!!)
//                                stopped = true
//                            } else {
//                                if (mEventRects[i].originalEvent.endCalendar.before(TimeUtils.today())) {
//                                    mEventBackgroundPaint.alpha = PAST_ALPHA
////                                    mEventTextPaint.alpha = PAST_ALPHA
//                                }
//                                canvas.drawRoundRect(mEventRects[i].rectF, mEventCornerRadius.toFloat(), mEventCornerRadius.toFloat(), mEventBackgroundPaint)
//                                drawEventTitle(mEventRects[i].event, mEventRects[i].rectF!!, canvas, top, startFromPixel)
//                                mEventBackgroundPaint.alpha = NORMAL_ALPHA
////                                mEventTextPaint.alpha = NORMAL_ALPHA
//                            }
//                        } else {
//                            mEventRects[i].rectF = null
//                        }
//                    } else if (mLimitedAllDayEvents && !canDrawInEndRow(mAllDayEventNumArray[columnIndex])) {
//                        val p: Int = getMoreAllDayEventIndex(columnIndex)
//                        if (p > -1) {
//                            moreAllDayEventIndex = p
//                        }
//                    }
//                }
//            }
//            if (mLimitedAllDayEvents && !stopped && moreAllDayEventIndex > -1) {
//                drawMoreAllDayEvents(startFromPixel, moreAllDayEventIndex, columnIndex, canvas)
//            }
//        }
//    }

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
//    private fun drawMoreAllDayEvents(startFromPixel: Float, eventIndex: Int, columnIndex: Int, canvas: Canvas): Boolean {
//        var top = (mHeaderRowPadding * 2).toFloat() + mNameTextHeight / 2 + mEventMarginVertical.toFloat() + mHeaderRowPadding.toFloat()
//        top += ((mMaxVisibleAllDayEventNum - 1) * mAllDayEventItemHeight)
//        // Calculate bottom.
//        val bottom = top + mAllDayEventItemHeight - 4
//        // Calculate left and right.
//        val right = startFromPixel + mWidthPerDay
//
//        // Draw the event and the event name on top of it.
//        if (startFromPixel < right && top < height && bottom > 0) {
//            mEventBackgroundPaint.color = Color.parseColor(mEventRects[eventIndex].event.color)
//            val oldColor = mEventTextPaint.color
//            mEventTextPaint.color = mHeaderColumnTextColor
//            val rect = Rect()
//            val text = context.getString(R.string.AP4001_AP4002_more_event, mAllDayEventNumArray[columnIndex] - getFilledRowNum(columnIndex))
//            mEventTextPaint.getTextBounds(text, 0, text.length, rect)
//            val x = startFromPixel + mWidthPerDay - rect.width().toFloat() - mHeaderColumnPadding.toFloat()
//            val y = top + mHeaderDayTextHeight - mEventPadding.toFloat()
//            canvas.drawText(text, x, y, mEventTextPaint)
//            mEventTextPaint.color = oldColor
//            mEventRects[eventIndex].rectF = RectF(startFromPixel, top, right, bottom)
//            mToggleList.add(mEventRects[eventIndex].rectF!!)
//            return true
//        } else {
//            mEventRects[eventIndex].rectF = null
//        }
//        return false
//    }

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
            else -> title.append("asdasdasda")
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
            var currentPeriodEvents: ShiftData? = null

            if (currentPeriodEvents == null) {
                currentPeriodEvents = mWeekViewLoader?.onLoad(mFirstVisibleDay!!.time,mLastVisibleDay!!.time)
            }

            // Clear events.
            mEventRects.clear()
            mMemos.clear()
            mStaffs.clear()
            sortAndCacheEvents(currentPeriodEvents!!)
            //determineHolidays()
            calculateHeaderHeightAndColumnWidth()

            mCurrentPeriodEvents = currentPeriodEvents
            mFetchedPeriod = periodToFetch
        }

        // Prepare to calculate positions of each events.
//        val tempEvents = mEventRects
//        mEventRects = ArrayList()

        // Iterate through each day with events to calculate the position of the events.
//        while (tempEvents.size > 0) {
//            val eventRects = ArrayList<EventRect>(tempEvents.size)
//
//            // Get first event for a day.
//            val eventRect1 = tempEvents.removeAt(0)
//            eventRects.add(eventRect1)
//
//            var i = 0
//            while (i < tempEvents.size) {
//                // Collect all other events for same day.
//                val eventRect2 = tempEvents[i]
//                if (eventRect1.event.date!!.isTheSameDay(eventRect2.event.date!!)) {
//                    tempEvents.removeAt(i)
//                    eventRects.add(eventRect2)
//                } else {
//                    i++
//                }
//            }
        //computePositionOfEvents(eventRects)
        //}
    }

    /**
     * Cache the event for smooth scrolling functionality.
     *
     * @param event The event to cache.
     */
    private fun cacheEventCell(event: ShiftItem) {
        mEventRects.add(EventRect(event, null))
    }

    /**
     * Sort and cache events.
     *
     * @param events The events to be sorted and cached.
     */
    private fun sortAndCacheEvents(events: ShiftData?) {
        val sortedEvents = sortEvents(events)
        mCurentDepartment = events?.departmentId
        sortedEvents.first.forEach {
            cacheMemo(it)
        }
        sortedEvents.second.forEach {
            cacheStaff(it)
        }
        sortedEvents.third.forEach {
            cacheEventCell(it)
        }
    }

    private fun cacheMemo(it: Memo) {
        mMemos.add(it)
    }

    private fun cacheStaff(it: Staff) {
        mStaffs.add(it)
    }

    /**
     * Sorts the events in ascending order.
     *
     * @param events The events to be sorted.
     */
    private fun sortEvents(shiftData: ShiftData?): Triple<List<Memo>, List<Staff>, List<ShiftItem>> {
        val memos = ArrayList<Memo>()
        val staffs = ArrayList<Staff>()
        val eventCells = ArrayList<ShiftItem>()
        shiftData?.memos?.forEach {
            memos.add(it)
        }
        shiftData?.shifts?.forEach {
            staffs.add(Staff.convertFormEntity(it))
            it.shiftList?.forEach {
                eventCells.add(it)
            }
        }
        eventCells.sortWith(Comparator { event1, event2 ->
            val start1 = event1.date?.time ?: 0
            val start2 = event2.date?.time ?: 0
            var comparator = if (start1 > start2) 1 else if (start1 < start2) -1 else 0
            if (comparator == 0) {
                val end1 = event1.date?.time ?: 0
                val end2 = event2.date?.time ?: 0
                comparator = if (end1 > end2) 1 else if (end1 < end2) -1 else 0
            }
            return@Comparator comparator
        })
        return Triple(memos, staffs, eventCells)
    }

    /**
     * Calculates the left and right positions of each events. This comes handy specially if events
     * are overlapping.
     *
     * @param eventRects The events along with their wrapper class.
     */
//    private fun computePositionOfEvents(eventRects: List<EventRect>) {
//        // Make "collision groups" for all events that collide with others.
//        val collisionGroups = ArrayList<ArrayList<EventRect>>()
//        for (eventRect in eventRects) {
//            var isPlaced = false
//
//            outerLoop@ for (collisionGroup in collisionGroups) {
//                for (groupEvent in collisionGroup) {
//                    if (isEventsCollide(groupEvent.event, eventRect.event) && groupEvent.event.allDay == eventRect.event.allDay) {
//                        collisionGroup.add(eventRect)
//                        isPlaced = true
//                        break@outerLoop
//                    }
//                }
//            }
//
//            if (!isPlaced) {
//                val newGroup = ArrayList<EventRect>()
//                newGroup.add(eventRect)
//                collisionGroups.add(newGroup)
//            }
//        }
//
//        for (collisionGroup in collisionGroups) {
//            expandEventsToMaxWidth(collisionGroup)
//        }
//    }

    /**
     * Expands all the events to maximum possible width. The events will try to occupy maximum
     * space available horizontally.
     *
     * @param collisionGroup The group of events which overlap with each other.
     */
//    private fun expandEventsToMaxWidth(collisionGroup: List<EventRect>) {
//        // Expand the events to maximum possible width.
//        val columns = ArrayList<ArrayList<EventRect>>()
//        columns.add(ArrayList())
//        for (eventRect in collisionGroup) {
//            var isPlaced = false
//            for (column in columns) {
//                if (column.isEmpty()) {
//                    column.add(eventRect)
//                    isPlaced = true
//                } else if (!isEventsCollide(eventRect.event, column.last().event)) {
//                    column.add(eventRect)
//                    isPlaced = true
//                    break
//                }
//            }
//            if (!isPlaced) {
//                val newColumn = ArrayList<EventRect>()
//                newColumn.add(eventRect)
//                columns.add(newColumn)
//            }
//        }
//
//        // Calculate left and right position for all the events.
//        // Get the maxRowCount by looking in all columns.
//        val maxRowCount = columns
//                .map { it.size }
//                .max()
//                ?: 0
//        for (i in 0 until maxRowCount) {
//            // Set the left and right values of the event.
//            var j = 0f
//            for (column in columns) {
//                if (column.size >= i + 1) {
//                    val eventRect = column[i]
//                    eventRect.width = 1f / columns.size
//                    eventRect.left = j / columns.size
//                    if (!eventRect.event.allDay) {
//                        //#13750
//                        val hourStart = eventRect.event.startCalendar.get(Calendar.HOUR_OF_DAY)
//                        var minuteStart = eventRect.event.startCalendar.get(Calendar.MINUTE)
//                        if (hourStart == 23 && minuteStart >= 45) {
//                            minuteStart = 45
//                        }
//                        eventRect.top = (hourStart * 60 + minuteStart).toFloat()
//                        var hourEnd = eventRect.event.endCalendar.get(Calendar.HOUR_OF_DAY)
//                        var minuteEnd = eventRect.event.endCalendar.get(Calendar.MINUTE)
//                        if (hourEnd == 0) {
//                            if (minuteEnd == 0) {
//                                hourEnd = 23
//                                minuteEnd = 59
//                            }
//                            if (minuteEnd <= 15) {
//                                minuteEnd = 15
//                            }
//                        }
//
//                        if (hourStart == hourEnd && minuteEnd - minuteStart < BLOCK) {
//                            minuteEnd = minuteStart + 15
//                        }
//
//                        eventRect.bottom = (hourEnd * 60 + minuteEnd).toFloat()
//                        if (eventRect.top == eventRect.bottom) {
//                            eventRect.top--
//                        }
//                    } else {
//                        eventRect.top = 0f
//                        eventRect.bottom = mAllDayEventHeight.toFloat()
//                    }
//                    mEventRects.add(eventRect)
//                }
//                j++
//            }
//        }
//    }

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

//    private fun determineHolidays() {
//        mHolidays.clear()
//
//        var i = 0
//        while (i < mEventRects.size) {
//            val event = mEventRects[i].event
//            val day = event.date.clone() as Calendar
//            day.set(Calendar.HOUR_OF_DAY, 0)
//            day.set(Calendar.MINUTE, 0)
//            day.set(Calendar.SECOND, 0)
//            day.set(Calendar.MILLISECOND, 0)
//            if (mHolidays[day] == null || mHolidays[day] == false) {
//                mHolidays.put(day, event.calendarType == DJCalendarEnum.Type.HOLIDAY)
//            }
//            if (!event.visible) {
//                mEventRects.removeAt(i)
//                i--
//            }
//            i++
//        }
//    }

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
    (var event: ShiftItem, var rectF: RectF?) {
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
    fun goToStaff(position: Double) {
        if (mAreDimensionsInvalid) {
            mScrollToHour = position
            return
        }

        var verticalOffset = 0
        if (position > getLastPositionStaff())
            verticalOffset = mHourHeight * getStaffSize()
        else if (position > 0)
            verticalOffset = (mHourHeight * position).toInt()

        if (verticalOffset > (mHourHeight * getStaffSize() - height).toFloat() + mHeaderHeight + mHeaderMarginBottom)
            verticalOffset = ((mHourHeight * getStaffSize() - height).toFloat() + mHeaderHeight + mHeaderMarginBottom).toInt()

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
        fun onEventClick(event: ShiftItem, eventRect: RectF)
    }

    interface EventLongPressListener {
        /**
         * Similar to [EventClickListener] but with a long press.
         *
         * @param event:     event clicked.
         * @param eventRect: view containing the clicked event.
         */
        fun onEventLongPress(event: ShiftItem, eventRect: RectF)
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