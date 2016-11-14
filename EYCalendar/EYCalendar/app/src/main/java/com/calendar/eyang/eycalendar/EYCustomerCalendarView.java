package com.calendar.eyang.eycalendar;

import android.app.Service;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by eyang on 6/14/15.
 */
public class EYCustomerCalendarView extends FrameLayout {
    private int MAX_MONTH_COUNT = 12; //for one year
    private Context mContext;
    private float mDensity;
    private ViewPager monthPager;
    private ArrayAdapter pagerAdapter;
    private Calendar todayDate;
    private Calendar mTempDate;
    // The current locale
    protected Locale mCurrentLocale;
    /**
     * The current select date.
     * If month was changed by flip the pageview, it will be set to first day of current month
     * If user tap the date in calendar, it will be set to the date.
     */
    private Calendar selectDate;

    /**
     * The selected available date
     */
    private Calendar selectAvailableDate;
    /**
     * The array used to save if the date is available, second index is same as Calendar.DAY_OF_MONTH (from 1 to 31)
     */
    private byte availableDates[][] = new byte[MAX_MONTH_COUNT+1][32];

    public EYCustomerCalendarView(Context context) {
        this(context, null);
    }

    public EYCustomerCalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EYCustomerCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mDensity = mContext.getResources().getDisplayMetrics().density;
        //initialize the current date
        setCurrentLocale(Locale.getDefault());

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.ey_calendar, null, false);
        ImageView leftButton = (ImageView) view.findViewById(R.id.leftButtonId);
        ImageView rightButton = (ImageView) view.findViewById(R.id.leftButtonId);
        this.addView(view);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (monthPager.getCurrentItem() > 0) {
                    monthPager.setCurrentItem(monthPager.getCurrentItem() - 1);
                }
            }
        });
        rightButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (monthPager.getCurrentItem()-1 < pagerAdapter.getCount()) {
                    monthPager.setCurrentItem(monthPager.getCurrentItem() + 1);
                }
            }
        });
    }

    private class MonthPagerAdapter extends PagerAdapter {
        Map<Integer, MonthView> monthViewMap = new HashMap<Integer, MonthView>();

        public void updateAllViews() {
            for(Integer key: monthViewMap.keySet()) {
                MonthView monthView = monthViewMap.get(key);
                monthView.refreshAllViews();
            }
        }

        @Override
        public int getItemPosition(Object object) {
            // must add this. force refresh when call notifyDataSetChanged
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return MAX_MONTH_COUNT;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            MonthView monthView;
            if(monthViewMap.get(position)!=null){
                monthView = monthViewMap.get(position);
            }else{
                monthView = new MonthView(container.getContext());
            }
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            container.addView(monthView, lp);
            mTempDate.setTimeInMillis(todayDate.getTimeInMillis());
            mTempDate.set(Calendar.DAY_OF_MONTH, 1);
            mTempDate.add(Calendar.MONTH, position);
            monthView.setMonth(mTempDate);
            monthView.setMonthDisplayed(mTempDate);
            monthViewMap.put(position, monthView);
            return monthView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            monthViewMap.remove(position);
            container.removeView((View) object);
        }
    }

    /**
     * Sets the current locale.
     *
     * @param locale The current locale.
     */
    protected void setCurrentLocale(Locale locale) {
        if (locale.equals(mCurrentLocale)) {
            return;
        }
        mCurrentLocale = locale;
        selectAvailableDate =  getCalendarForLocale(todayDate, locale);
        selectAvailableDate.setTimeInMillis(0);

        todayDate = getCalendarForLocale(todayDate, locale);
        mTempDate = getCalendarForLocale(mTempDate, locale);
        selectDate = getCalendarForLocale(selectDate, locale);
    }

    /**
     * Gets a calendar for locale bootstrapped with the value of a given calendar.
     *
     * @param oldCalendar The old calendar.
     * @param locale The locale.
     */
    private static Calendar getCalendarForLocale(Calendar oldCalendar, Locale locale) {
        if (oldCalendar == null) {
            return Calendar.getInstance(locale);
        } else {
            final long currentTimeMillis = oldCalendar.getTimeInMillis();
            Calendar newCalendar = Calendar.getInstance(locale);
            newCalendar.setTimeInMillis(currentTimeMillis);
            return newCalendar;
        }
    }

    /**
     * @return True if the <code>firstDate</code> is the same as the <code>
     * secondDate</code>.
     */
    private static boolean isSameDate(Calendar firstDate, Calendar secondDate) {
        return (firstDate.get(Calendar.DAY_OF_YEAR) == secondDate.get(Calendar.DAY_OF_YEAR)
                && firstDate.get(Calendar.YEAR) == secondDate.get(Calendar.YEAR));
    }

    private  class MonthView extends FrameLayout {

        /**
         * The number of milliseconds in a day.e
         */
        private static final long MILLIS_IN_DAY = 86400000L;

        /**
         * The number of day in a week.
         */
        private static final int DAYS_PER_WEEK = 7;

        /**
         * The number of milliseconds in a week.
         */
        private static final long MILLIS_IN_WEEK = DAYS_PER_WEEK * MILLIS_IN_DAY;

        private static final int DEFAULT_SHOWN_WEEK_COUNT = 6;

        private static final int DEFAULT_DATE_TEXT_SIZE = 15;


        private int mDateTextSize;

        private int mFocusedMonthDateColor;

        private int mAvaialbleDateColor;

        private int mUnfocusedMonthDateColor;

        /**
         * The number of shown weeks.
         */
        private int mShownWeekCount;


        /**
         * The number of day per week to be shown.
         */
        private int mDaysPerWeek = 7;

        /**
         * The adapter for the weeks list.
         */
        private WeeksAdapter mAdapter;

        /**
         * The weeks list.
         */
        private ListView mListView;

        /**
         * The name of the month to display.
         */
        private TextView mMonthName;

        /**
         * The first day of the week.
         */
        private int mFirstDayOfWeek;

        /**
         * Which month should be displayed/highlighted [0-11].
         */
        private int mFocusedMonth = -1;

        /**
         * The first day of the focused month.
         */
        private Calendar mFirstDayOfMonth = Calendar.getInstance();

        public MonthView(Context context) {
            super(context);
            init(context);
        }

        void init(Context context) {
            mContext = context;

            updateDateTextSize();
            mFirstDayOfWeek = Calendar.SUNDAY;

            mShownWeekCount = DEFAULT_SHOWN_WEEK_COUNT;
            mUnfocusedMonthDateColor = mContext.getResources().getColor(R.color.mid_gray);
            mFocusedMonthDateColor = mContext.getResources().getColor(R.color.mid_gray);
            mAvaialbleDateColor = mContext.getResources().getColor(R.color.black);

            LayoutInflater layoutInflater = (LayoutInflater) mContext
                    .getSystemService(Service.LAYOUT_INFLATER_SERVICE);
            View content = layoutInflater.inflate(R.layout.widget_month, null, false);
            addView(content);

            mListView = (ListView) findViewById(R.id.listView);

            mMonthName = (TextView) content.findViewById(R.id.month_name);


            setUpListView();
            setUpAdapter();

        }

        public void refreshAllViews() {
    	/*	final int childCount = mListView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                WeekView weekView = (WeekView) mListView.getChildAt(i);
                if (weekView!=null) {
                    weekView.invalidate();
                }
            }*/
            mAdapter.notifyDataSetChanged();

        }

        //set the calendar month
        protected void setMonth(Calendar month) {
            if (mFirstDayOfMonth == null) {
                mFirstDayOfMonth = getCalendarForLocale(null, mCurrentLocale);
            }

            mFirstDayOfMonth.setTimeInMillis(month.getTimeInMillis());
            mFirstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);

            mFocusedMonth = mFirstDayOfMonth.get(Calendar.MONTH);
            invalidate();
        }


        private void updateDateTextSize() {
            mDateTextSize = (int) (DEFAULT_DATE_TEXT_SIZE * mDensity);
        }

        /**
         * Creates a new adapter if necessary and sets up its parameters.
         */
        private void setUpAdapter() {
            if (mAdapter == null) {
                mAdapter = new WeeksAdapter(mContext);
                /*mAdapter.registerDataSetObserver(new DataSetObserver() {
                    @Override
                    public void onChanged() {
                        if (mOnSelectDayListener != null) {
                            //Calendar selectedDay = mAdapter.getSelectedDay();
                            mOnSelectDayListener.onSelectedDayChange(
                                    selectDate);
                        }
                    }
                });*/
                mListView.setAdapter(mAdapter);
            }

            // refresh the view with the new parameters
            mAdapter.notifyDataSetChanged();
        }


        /**
         * Sets all the required fields for the list view.
         */
        private void setUpListView() {
            // Configure the listview
            mListView.setDivider(null);
            mListView.setItemsCanFocus(true);
            mListView.setVerticalScrollBarEnabled(false);

        }


        /**
         * Sets the month displayed at the top of this view based on time. Override
         * to add custom events when the title is changed.
         *
         * @param calendar A day in the new focus month.
         */
        private void setMonthDisplayed(Calendar calendar) {

            final int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_MONTH_DAY
                    | DateUtils.FORMAT_SHOW_YEAR;
            final long millis = calendar.getTimeInMillis();
            String newMonthName = DateUtils.formatDateRange(mContext, millis, millis, flags);
            mMonthName.setText(newMonthName);
            mMonthName.invalidate();
        }

        /**
         * @return Returns the number of weeks between the current <code>date</code>
         * and the <code>mMinDate</code>.
         */
        private int getWeekIndexByDate(Calendar date) {

            long endTimeMillis = date.getTimeInMillis()
                    + date.getTimeZone().getOffset(date.getTimeInMillis());
            long startTimeMillis = this.mFirstDayOfMonth.getTimeInMillis()
                    + mFirstDayOfMonth.getTimeZone().getOffset(mFirstDayOfMonth.getTimeInMillis());
            long dayOffsetMillis = (mFirstDayOfMonth.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek)
                    * MILLIS_IN_DAY;
            return (int) ((endTimeMillis - startTimeMillis + dayOffsetMillis) / MILLIS_IN_WEEK);
        }

        /**
         * <p>
         * This is a specialized adapter for creating a list of weeks with
         * selectable days. It can be configured to display the week number, start
         * the week on a given day, show a reduced number of days, or display an
         * arbitrary number of weeks at a time.
         * </p>
         */
        private class WeeksAdapter extends BaseAdapter implements OnTouchListener {

            private int mSelectedWeek;

            private GestureDetector mGestureDetector;

            //private final Calendar mSelectedDate = Calendar.getInstance();

            private int mTotalWeekCount;

            public WeeksAdapter(Context context) {
                mContext = context;
                mGestureDetector = new GestureDetector(mContext, new CalendarGestureListener());
                init();
            }

            /**
             * Set up the gesture detector and selected time
             */
            private void init() {
                mSelectedWeek = getWeekIndexByDate(selectAvailableDate);
                mTotalWeekCount = 6;// always display 6 weeks on Calendar. //getWeeksSinceMinDate(mMaxDate);

                notifyDataSetChanged();
            }


            @Override
            public int getCount() {
                return mTotalWeekCount;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                WeekView weekView = null;
                if (convertView != null) {
                    weekView = (WeekView) convertView;
                } else {
                    weekView = new WeekView(mContext);
                    android.widget.AbsListView.LayoutParams params =
                            new android.widget.AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT,
                                    LayoutParams.WRAP_CONTENT);
                    weekView.setLayoutParams(params);
                    weekView.setClickable(true);
                    weekView.setOnTouchListener(this);
                }

                int selectedWeekDay = -1;

                mSelectedWeek = getWeekIndexByDate(selectAvailableDate);
                if (selectAvailableDate.get(Calendar.MONTH) != mFocusedMonth) {
                    // the select date is not in this month view
                    selectedWeekDay = -1;
                } else {
                    selectedWeekDay = (mSelectedWeek == position) ? selectAvailableDate.get(
                            Calendar.DAY_OF_WEEK) : -1;
                }
                weekView.init(position, selectedWeekDay, mFocusedMonth);

                return weekView;
            }


            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mListView.isEnabled() && mGestureDetector.onTouchEvent(event)) {
                    WeekView weekView = (WeekView) v;
                    // if we cannot find a day for the given location we are done
                    if (!weekView.getDayFromLocation(event.getX(), mTempDate)) {
                        return true;
                    }
                    // it is possible that the touched day is outside the valid range
                    // we draw whole weeks but range end can fall not on the week end
                /*    if (mTempDate.before(mMinDate) || mTempDate.after(mMaxDate)) {
                        return true;
                    } */
                    Calendar newDate = Calendar.getInstance();
                    newDate.setTime(mTempDate.getTime());
                    onDateTapped(newDate);
                    return true;
                }
                return false;
            }

            /**
             * Maintains the same hour/min/sec but moves the day to the tapped day.
             *
             * @param day The day that was tapped
             */
            private void onDateTapped(Calendar day) {
                if (mOnSelectDayListener != null) {
                    mOnSelectDayListener.onSelectedDayChange(day);
                }
                //user only can pick the day on current month.
                if (day.get(Calendar.MONTH) == mFirstDayOfMonth.get(Calendar.MONTH)) {
                    //setMonthDisplayed(day);
                }
            }

            /**
             * This is here so we can identify single tap events and set the
             * selected day correctly
             */
            class CalendarGestureListener extends GestureDetector.SimpleOnGestureListener {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            }
        }

        /**
         * <p>
         * This is a dynamic view for drawing a single week. It can be configured to
         * display the week number, start the week on a given day, or show a reduced
         * number of days. It is intended for use as a single view within a
         * ListView. See {@link WeeksAdapter} for usage.
         * </p>
         */
        private class WeekView extends View {

            private final Rect mTempRect = new Rect();

            private final Paint mDrawPaint = new Paint();

            private final Paint mMonthNumDrawPaint = new Paint();

            // Cache the number strings so we don't have to recompute them each time
            private String[] mDayNumbers;

            // Quick lookup for checking which days are in the focus month
            private boolean[] mFocusDay;

            // The first day displayed by this item
            private Calendar mFirstDay;

            // The position of this week, equivalent to weeks since the week of Jan
            // 1st, 1900
            private int mWeek = -1;

            // Quick reference to the width of this view, matches parent
            private int mWidth;

            // The height this view should draw at in pixels, set by height param
            private int mHeight;

            // If this view contains the selected day
            private boolean mHasSelectedDay = false;

            // Which day is selected [0-6] or -1 if no day is selected
            private int mSelectedDay = -1;

            // The number of days + a spot for week number if it is displayed
            private int mNumCells;

            // The left edge of the selected day
            private int mSelectedLeft = -1;

            // The right edge of the selected day
            private int mSelectedRight = -1;

            private long weekDates[] = new long[7];
            private byte availableWeekDays[] = new byte[7];

            public WeekView(Context context) {
                super(context);

                // Sets up any standard paints that will be used
                initilaizePaints();
            }

            /**
             * Initializes this week view.
             *
             * @param weekNumber The number of the week this view represents. The
             *            week number is a zero based index of the weeks since
             *            {@link AMCalendarView#getMinDate()}.
             * @param selectedWeekDay The selected day of the week from 0 to 6, -1 if no
             *            selected day.
             * @param focusedMonth The month that is currently in focus i.e.
             *            highlighted.
             */
            public void init( int weekNumber, int selectedWeekDay, int focusedMonth) {
                mSelectedDay = selectedWeekDay;
                mHasSelectedDay = mSelectedDay != -1;
                mNumCells =  mDaysPerWeek;
                mWeek = weekNumber;
                mTempDate.setTimeInMillis(mFirstDayOfMonth.getTimeInMillis());

                mTempDate.add(Calendar.WEEK_OF_YEAR, mWeek);
                mTempDate.setFirstDayOfWeek(mFirstDayOfWeek);

                // Allocate space for caching the day numbers and focus values
                mDayNumbers = new String[mNumCells];
                mFocusDay = new boolean[mNumCells];

                int i = 0;

                // Now adjust our starting day based on the start day of the week
                int diff = mFirstDayOfWeek - mTempDate.get(Calendar.DAY_OF_WEEK);
                mTempDate.add(Calendar.DAY_OF_MONTH, diff);

                mFirstDay = (Calendar) mTempDate.clone();



                for (; i < mNumCells; i++) {
                    final boolean isFocusedDay = (mTempDate.get(Calendar.MONTH) == focusedMonth);
                    mFocusDay[i] = isFocusedDay;
                    // do not draw dates outside the valid range to avoid user confusion
                    if (mTempDate.before(mFirstDayOfMonth)) {
                        mDayNumbers[i] = String.format(Locale.getDefault(), "%d",
                                mTempDate.get(Calendar.DAY_OF_MONTH));
                        //mDayNumbers[i] = "";
                        weekDates[i] = 0;
                    } else {
                        mDayNumbers[i] = String.format(Locale.getDefault(), "%d",
                                mTempDate.get(Calendar.DAY_OF_MONTH));
                        weekDates[i] = mTempDate.getTimeInMillis();
                    }
                    mTempDate.add(Calendar.DAY_OF_MONTH, 1);
                }
                // We do one extra add at the end of the loop, if that pushed us to
                // new month undo it
                if (mTempDate.get(Calendar.DAY_OF_MONTH) == 1) {
                    mTempDate.add(Calendar.DAY_OF_MONTH, -1);
                }

                updateSelectionPositions();
            }

            /**
             * Initialize the paint instances.
             */
            private void initilaizePaints() {
                mDrawPaint.setFakeBoldText(false);
                mDrawPaint.setAntiAlias(true);
                mDrawPaint.setStyle(Paint.Style.FILL);

                //mMonthNumDrawPaint.setFakeBoldText(true);
                mMonthNumDrawPaint.setAntiAlias(true);
                mMonthNumDrawPaint.setStyle(Paint.Style.FILL);
                mMonthNumDrawPaint.setTextAlign(Paint.Align.CENTER);
                mMonthNumDrawPaint.setTextSize(mDateTextSize);
            }

            protected boolean isLayoutRtl() {
                return false;
            }

            /**
             * Calculates the day that the given x position is in, accounting for
             * week number.
             *
             * @param x The x position of the touch event.
             * @return True if a day was found for the given location.
             */
            public boolean getDayFromLocation(float x, Calendar outCalendar) {
                final boolean isLayoutRtl = isLayoutRtl();

                int start;
                int end;

                if (isLayoutRtl) {
                    start = 0;
                    end =  mWidth;
                } else {
                    start =  0;
                    end = mWidth;
                }

                if (x < start || x > end) {
                    outCalendar.clear();
                    return false;
                }

                // Selection is (x - start) / (pixels/day) which is (x - start) * day / pixels
                int dayPosition = (int) ((x - start) * mDaysPerWeek / (end - start));

                if (isLayoutRtl) {
                    dayPosition = mDaysPerWeek - 1 - dayPosition;
                }

                outCalendar.setTimeInMillis(mFirstDay.getTimeInMillis());
                outCalendar.add(Calendar.DAY_OF_MONTH, dayPosition);

                return true;
            }

            @Override
            protected void onDraw(Canvas canvas) {
                updateavailableWeekDays();
                drawBackground(canvas);
                drawDates(canvas);
            }

            private void updateavailableWeekDays() {
                for(int i=0; i< 7; i++) {
                    if(weekDates[i]==0) {
                        availableWeekDays[i] = 0;
                    } else {
                        mTempDate.setTimeInMillis(weekDates[i]);
                        int yearDiff = mTempDate.get(Calendar.YEAR) - todayDate.get(Calendar.YEAR);
                        int monthDiff = mTempDate.get(Calendar.MONTH) - todayDate.get(Calendar.MONTH);
                        int monthIndex = yearDiff * 12 + monthDiff;

                        int dayIndex = mTempDate.get(Calendar.DAY_OF_MONTH);
                        //AMLogger.logInfo("monthIndex : %d / %d", monthIndex, dayIndex);
                        if(monthIndex >= availableDates.length || monthIndex < 0) {
                            return;
                        } else {
                            availableWeekDays[i] = availableDates[monthIndex][dayIndex];
                        }

                    }
                }
            }

            /**
             * This draws the selection highlight if a day is selected in this week.
             *
             * @param canvas The canvas to draw on
             */
            private void drawBackground(Canvas canvas) {
                if (!mHasSelectedDay) {
                    return;
                }
                //draw selected day background
                mDrawPaint.setColor(mContext.getResources().getColor(R.color.orange));
                mTempRect.top = 0;
                mTempRect.bottom = mHeight;
                mTempRect.left = mSelectedLeft;
                mTempRect.right = mSelectedRight+2;
                //canvas.drawRect(mTempRect, mDrawPaint);
                float radius = 0;
                if(mTempRect.width() > mTempRect.height()) {
                    radius = mTempRect.height()/2.0f;
                } else {
                    radius = mTempRect.width()/2.0f;
                }
                canvas.drawCircle(mTempRect.exactCenterX(), mTempRect.exactCenterY(), radius, mDrawPaint);
                //canvas.drawRect(mTempRect, mDrawPaint);
            }

            /**
             * Draws the week and month day numbers for this week.
             *
             * @param canvas The canvas to draw on
             */
            private void drawDates(Canvas canvas) {
                mDrawPaint.setTextAlign(Paint.Align.CENTER);
                mDrawPaint.setTextSize(mDateTextSize);

                final float textHeight = mDrawPaint.getTextSize();
                final int y = (int) ((mHeight + mDateTextSize) / 2 - 2* mDensity) ;
                final int nDays = mNumCells;
                final int divisor = 2 * nDays;



                int i = 0;

                if (isLayoutRtl()) {
                    for (; i < nDays - 1; i++) {

                        if(mFocusDay[i] ) {
                            mMonthNumDrawPaint.setColor(availableWeekDays[i] != 0 ? mAvaialbleDateColor: mFocusedMonthDateColor);
                            mMonthNumDrawPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                        } else {
                            mMonthNumDrawPaint.setColor(mUnfocusedMonthDateColor);
                            mMonthNumDrawPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        }

                        int x = (2 * i + 1) * mWidth / divisor;
                        canvas.drawText(mDayNumbers[nDays - 1 - i], x, y, mMonthNumDrawPaint);

                    }

                } else {
                    for (; i < nDays; i++) {
                        int x = (2 * i + 1) * mWidth / divisor;
                        if(mFocusDay[i] ) {
                            //AMLogger.logInfo(" draw days : %d ", i);
                            mMonthNumDrawPaint.setColor(availableWeekDays[i] != 0? mAvaialbleDateColor: mFocusedMonthDateColor);
                            mMonthNumDrawPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                        } else {
                            mMonthNumDrawPaint.setColor(mUnfocusedMonthDateColor);
                            mMonthNumDrawPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
                        }
                        canvas.drawText(mDayNumbers[i], x, y, mMonthNumDrawPaint);
                    }
                }
            }


            @Override
            protected void onSizeChanged(int w, int h, int oldw, int oldh) {
                mWidth = w;
                updateSelectionPositions();
            }

            /**
             * This calculates the positions for the selected day lines.
             */
            private void updateSelectionPositions() {
                if (mHasSelectedDay) {
                    final boolean isLayoutRtl = isLayoutRtl();
                    int selectedPosition = mSelectedDay - mFirstDayOfWeek;
                    if (selectedPosition < 0) {
                        selectedPosition += 7;
                    }
                    if (isLayoutRtl) {
                        mSelectedLeft = (mDaysPerWeek - 1 - selectedPosition) * mWidth / mNumCells;

                    } else {
                        mSelectedLeft = selectedPosition * mWidth / mNumCells;
                    }
                    mSelectedRight = mSelectedLeft + mWidth / mNumCells;
                }
            }

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                mHeight = (mListView.getHeight() - mListView.getPaddingTop() - mListView
                        .getPaddingBottom()) / mShownWeekCount;
                setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mHeight);
            }
        }
    }
}
