package com.codetroopers.betterpickers.mspicker;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.codetroopers.betterpickers.R;
import com.codetroopers.betterpickers.widget.ZeroTopPaddingTextView;

public class MsView extends LinearLayout {

    private ZeroTopPaddingTextView mMinutesOnes, mMinutesTens;
    private ZeroTopPaddingTextView mSecondsOnes, mSecondsTens;
    private final Typeface mAndroidClockMonoThin;
    private ZeroTopPaddingTextView mMinusLabel;

    private ColorStateList mTextColor;

    /**
     * Instantiate an MsView
     *
     * @param context the Context in which to inflate the View
     */
    public MsView(Context context) {
        this(context, null);
    }

    /**
     * Instantiate an MsView
     *
     * @param context the Context in which to inflate the View
     * @param attrs attributes that define the title color
     */
    public MsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mAndroidClockMonoThin =
                Typeface.createFromAsset(context.getAssets(), "fonts/AndroidClockMono-Thin.ttf");

        // Init defaults
        mTextColor = getResources().getColorStateList(R.color.dialog_text_color_holo_dark);
    }

    /**
     * Set a theme and restyle the views. This View will change its text color.
     *
     * @param themeResId the resource ID for theming
     */
    public void setTheme(int themeResId) {
        if (themeResId != -1) {
            TypedArray a = getContext().obtainStyledAttributes(themeResId, R.styleable.BetterPickersDialogFragment);

            mTextColor = a.getColorStateList(R.styleable.BetterPickersDialogFragment_bpTextColor);
        }

        restyleViews();
    }

    private void restyleViews() {
        if (mMinutesOnes != null) {
            mMinutesOnes.setTextColor(mTextColor);
        }
        if (mMinutesTens != null) {
            mMinutesTens.setTextColor(mTextColor);
        }
        if (mSecondsOnes != null) {
            mSecondsOnes.setTextColor(mTextColor);
        }
        if (mSecondsTens != null) {
            mSecondsTens.setTextColor(mTextColor);
        }
        if (mMinusLabel != null) {
            mMinusLabel.setTextColor(mTextColor);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mMinutesTens = (ZeroTopPaddingTextView) findViewById(R.id.minutes_tens);
        mMinutesOnes = (ZeroTopPaddingTextView) findViewById(R.id.minutes_ones);
        mSecondsTens = (ZeroTopPaddingTextView) findViewById(R.id.seconds_tens);
        mSecondsOnes = (ZeroTopPaddingTextView) findViewById(R.id.seconds_ones);
        mMinusLabel = (ZeroTopPaddingTextView) findViewById(R.id.minus_label);

        if (mMinutesTens != null) {
            mMinutesTens.updatePaddingForBoldDate();
        }
        if (mMinutesOnes != null) {
            mMinutesOnes.updatePaddingForBoldDate();
        }
        // Set the lowest time unit with thin font (excluding hundredths)
        if (mSecondsTens != null) {
            mSecondsTens.setTypeface(mAndroidClockMonoThin);
            mSecondsTens.updatePadding();
        }
        if (mSecondsOnes != null) {
            mSecondsOnes.setTypeface(mAndroidClockMonoThin);
            mSecondsOnes.updatePadding();
        }
    }

    /**
     * Set the time shown
     *
     * @param minutesTensDigit the tens digit of the minutes TextView
     * @param minutesOnesDigit the ones digit of the minutes TextView
     * @param secondsTensDigit the tens digit of the seconds TextView
     * @param secondsOnesDigit the ones digit of the seconds TextView
     */
    public void setTime(int minutesTensDigit, int minutesOnesDigit, int secondsTensDigit,
                        int secondsOnesDigit) {
        setTime(false, minutesTensDigit, minutesOnesDigit, secondsTensDigit, secondsOnesDigit);
    }

    public void setTime(boolean isNegative, int minutesTensDigit, int minutesOnesDigit, int secondsTensDigit,
            int secondsOnesDigit) {

        mMinusLabel.setVisibility(isNegative ? View.VISIBLE : View.GONE);

        if (mMinutesTens != null) {
            mMinutesTens.setText(String.format("%d", minutesTensDigit));
        }
        if (mMinutesOnes != null) {
            mMinutesOnes.setText(String.format("%d", minutesOnesDigit));
        }
        if (mSecondsTens != null) {
            mSecondsTens.setText(String.format("%d", secondsTensDigit));
        }
        if (mSecondsOnes != null) {
            mSecondsOnes.setText(String.format("%d", secondsOnesDigit));
        }
    }
}