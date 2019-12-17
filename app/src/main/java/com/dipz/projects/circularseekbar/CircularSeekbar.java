package com.dipz.projects.circularseekbar;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CircularSeekbar extends View {

    private final float DENSITY = getContext().getResources().getDisplayMetrics().density;

    int circleRadius;
    private int cy;
    private int cx;

    RectF outerCircleRect = new RectF();
    RectF middleCircleArcRect = new RectF();
    RectF middleCircleRect = new RectF();
    RectF innerCircleRect = new RectF();

    private Paint mOuterCirclePaint;
    private Paint mMiddleArcCirclePaint;
    private Paint mMiddleArcProgressCirclePaint;
    private Paint mMiddleCirclePaint;
    private Paint mInnerCirclePaint;
    private Paint gradientMiddleCirclePaint;

    int outerCircleColor, innerCircleColor, circleArcColor, circleTickColor;

    private static int INVALID_PROGRESS_VALUE = -1;
    private OnSemiCircularSeekbarArcChangeListener mOnSemiCircularSeekbarArcChangeListener;
    private int mMax = 100;//The Maximum value that this SeekbarArc can be set to
    private int mProgress = 0;//The Current value that the SeekbarArc is set to
    private int mStartAngle = 0;//The Angle to start drawing this Arc from
    private int mSweepAngle = 180;//The Angle through which to draw the arc (Max is 360)
    private float mProgressSweep = 0;
    private static final int MAX = 180;
    private final int mAngleOffset = -90;    // The initial rotational offset -90 means we start at 12 o'clock
    private int mRotation = 270;//The rotation of the SeekbarArc- 0 is twelve o'clock
    private int mThumbXPos;
    private int mThumbYPos;
    private int mTranslateX;
    private int mTranslateY;
    private double mTouchAngle;
    private float mTouchIgnoreRadius;
    private boolean mClockwise = true;//Will the progress increase clockwise or anti-clockwise
    private Drawable mThumb;//The Drawable for the seek arc thumbnail

    private boolean mArcEnabled = true;//is the arc enabled/touchable
    private int mArcWidth = 5;//The Width of the background arc for the SeekbarArc

    private int mTickOffset = 5;//distance of tick from middle circle
    private int mTickLength = 6;//length of tick
    private int mTickWidth = 2;
    private int mTickProgressWidth = 2;
    private int mAngleTextSize = 12;
    private int mTickIntervals = 5;//space between two ticks
    private int mAngle = 0;
    private TicksBetweenLabel mTicksBetweenLabel = TicksBetweenLabel.TWO;
    private boolean mProgressRoundEdges = true;
    private boolean mIsBrightnessColor = true;

    private boolean mTouchInside = true;//Enable touch inside the circular seekbar
//    private boolean mTouchOutside = true;//Enable outside touch of circular seekbar
    private float mTouchIgnoreOuterRadius;
    float middleCircleRadius;

    public enum TicksBetweenLabel {
        ZERO, ONE, TWO, THREE, FOUR
    }

    public CircularSeekbar(Context context) {
        super(context);
        init(context, null, 0);
    }

    public CircularSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public CircularSeekbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    /**
     * Sets a listener to receive notifications of changes to the SeekbarArc's
     * progress level. Also provides notifications of when the user starts and
     * stops a touch gesture within the SeekbarArc.
     *
     * @param l The seek bar notification listener
     */
    public void setOnSemiCircularSeekbarArcChangeListener(OnSemiCircularSeekbarArcChangeListener l) {
        mOnSemiCircularSeekbarArcChangeListener = l;
    }

    public interface OnSemiCircularSeekbarArcChangeListener {

        /**
         * Notification that the progress level has changed. Clients can use the
         * fromUser parameter to distinguish user-initiated changes from those
         * that occurred programmatically.
         *
         * @param circularSeekbar The SeekbarArc whose progress has changed
         * @param progress        The current progress level. This will be in the range
         *                        0..max where max was set by
         *                        (The default value for
         *                        max is 100.)
         * @param fromUser        True if the progress change was initiated by the user.
         */
        void onProgressChanged(CircularSeekbar circularSeekbar, int progress, boolean fromUser);

        /**
         * Notification that the user has started a touch gesture. Clients may
         * want to use this to disable advancing the seekbar.
         *
         * @param circularSeekbar The SeekbarArc in which the touch gesture began
         */
        void onStartTrackingTouch(CircularSeekbar circularSeekbar);

        /**
         * Notification that the user has finished a touch gesture. Clients may
         * want to use this to re-enable advancing the SeekbarArc.
         *
         * @param circularSeekbar The SeekbarArc in which the touch gesture began
         */
        void onStopTrackingTouch(CircularSeekbar circularSeekbar);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        final Resources res = getResources();


        int thumbHalfheight = 0;
        int thumbHalfWidth = 0;

        mTickOffset = (int) (mTickOffset * DENSITY);
        mTickLength = (int) (mTickLength * DENSITY);
        mTickWidth = (int) (mTickWidth * DENSITY);
        mTickProgressWidth = (int) (mTickProgressWidth * DENSITY);

        mThumb = res.getDrawable(R.drawable.seek_arc_control_selector);
        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularSeekbar, defStyleAttr, 0);

            Drawable thumb = a.getDrawable(R.styleable.CircularSeekbar_thumb);
            if (thumb != null) {
                mThumb = thumb;
            }

            thumbHalfheight = (int) mThumb.getIntrinsicHeight() / 2;
            thumbHalfWidth = (int) mThumb.getIntrinsicWidth() / 2;
            mThumb.setBounds(-thumbHalfWidth, -thumbHalfheight, thumbHalfWidth, thumbHalfheight);

            outerCircleColor = res.getColor(R.color.outer_color);
            innerCircleColor = res.getColor(R.color.inner_color);
            circleArcColor = res.getColor(R.color.black);
            circleTickColor = res.getColor(R.color.white);

            mIsBrightnessColor = a.getBoolean(R.styleable.CircularSeekbar_isBrightnessColor, mIsBrightnessColor);
            mArcEnabled = a.getBoolean(R.styleable.CircularSeekbar_arcEnabled, mArcEnabled);
            mClockwise = a.getBoolean(R.styleable.CircularSeekbar_clockwise, mClockwise);
            mArcWidth = (int) a.getDimension(R.styleable.CircularSeekbar_arcWidth, mArcWidth);
            outerCircleColor = a.getColor(R.styleable.CircularSeekbar_outerCircleColor, outerCircleColor);
            innerCircleColor = a.getColor(R.styleable.CircularSeekbar_innerCircleColor, innerCircleColor);
            circleArcColor = a.getColor(R.styleable.CircularSeekbar_arcColor, circleArcColor);
            circleTickColor = a.getColor(R.styleable.CircularSeekbar_tickColor, circleTickColor);
            mTickOffset = (int) a.getDimension(R.styleable.CircularSeekbar_tickOffset, mTickOffset);
            mTickLength = (int) a.getDimension(R.styleable.CircularSeekbar_tickLength, mTickLength);
            mTickWidth = (int) a.getDimension(R.styleable.CircularSeekbar_tickWidth, mTickWidth);
            mTickIntervals = (int) a.getDimension(R.styleable.CircularSeekbar_tickIntervals, mTickIntervals);
            mProgressRoundEdges = a.getBoolean(R.styleable.CircularSeekbar_roundEdges, mProgressRoundEdges);
            mProgress = a.getInteger(R.styleable.CircularSeekbar_progress, mProgress);
            mTouchInside = a.getBoolean(R.styleable.CircularSeekbar_touchInside, mTouchInside);
//            mTouchOutside = a.getBoolean(R.styleable.CircularSeekbar_touchOutside, mTouchOutside);

            a.recycle();
        }

        mTickOffset = (int) (mTickOffset * DENSITY);
        mTickLength = (int) (mTickLength * DENSITY);

        mProgress = (mProgress > mMax) ? mMax : mProgress;
        mProgress = (mProgress < 0) ? 0 : mProgress;

        mSweepAngle = (mSweepAngle > 360) ? 360 : mSweepAngle;
        mSweepAngle = (mSweepAngle < 0) ? 0 : mSweepAngle;

        mProgressSweep = (float) mProgress / mMax * mSweepAngle;

        mStartAngle = (mStartAngle > 360) ? 0 : mStartAngle;
        mStartAngle = (mStartAngle < 0) ? 0 : mStartAngle;

        mAngle = (mAngle > MAX) ? MAX : ((mAngle < 0) ? 0 : mAngle);

        mOuterCirclePaint = new Paint();
        mOuterCirclePaint.setColor(outerCircleColor);

        mMiddleArcCirclePaint = new Paint();
        mMiddleArcCirclePaint.setAntiAlias(true);
        mMiddleArcCirclePaint.setColor(circleTickColor);
        mMiddleArcCirclePaint.setStrokeWidth(mTickWidth);

        mMiddleCirclePaint = new Paint();
        mMiddleCirclePaint.setAntiAlias(true);
        mMiddleCirclePaint.setColor(getResources().getColor(R.color.middle_start_color));

        mInnerCirclePaint = new Paint();
        mInnerCirclePaint.setAntiAlias(true);
        mInnerCirclePaint.setColor(innerCircleColor);

        mMiddleArcProgressCirclePaint = new Paint();
        mMiddleArcProgressCirclePaint.setColor(circleArcColor);
        mMiddleArcProgressCirclePaint.setAntiAlias(true);
        mMiddleArcProgressCirclePaint.setStyle(Paint.Style.STROKE);
        mMiddleArcProgressCirclePaint.setStrokeWidth(mArcWidth);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!mClockwise) {
            canvas.scale(-1, 1, middleCircleRect.centerX(), middleCircleRect.centerY());
        }

        gradientMiddleCirclePaint = new Paint();
        gradientMiddleCirclePaint.setAntiAlias(true);
        if (mIsBrightnessColor) {
            gradientMiddleCirclePaint.setShader(new LinearGradient(0, 360, getHeight(), getWidth(), getResources().getColor(R.color.middle_end_color), getResources().getColor(R.color.middle_start_color), Shader.TileMode.MIRROR));
        } else {
            gradientMiddleCirclePaint.setShader(new LinearGradient(0, 360, getHeight(), getWidth(), getResources().getColor(R.color.white), getResources().getColor(R.color.progress), Shader.TileMode.REPEAT));
        }

        // Draw the arcs
        final int arcStart = mStartAngle + mAngleOffset + mRotation;

        canvas.drawArc(outerCircleRect, -0F, -180F, true, mOuterCirclePaint);//outer circle
        canvas.drawArc(middleCircleRect, -0F, -180F, true, gradientMiddleCirclePaint);//middle circle
        canvas.drawArc(middleCircleRect, arcStart, mProgressSweep, false, mMiddleArcProgressCirclePaint); // semicircle inside tick which is shown when progress change
        canvas.drawArc(innerCircleRect, -0F, -180F, true, mInnerCirclePaint);//inner circle

        //TicksBetweenLabel
        /**
         * Mechanism to draw the tick.
         * Tan(theta) gives the slope.
         * Formula for a straight line is y = mx + c. y is calculated for varying values of x and the ticks are drawn.
         */
        double slope, startTickX, startTickY, endTickX, endTickY, thetaInRadians;
        double radiusOffset = circleRadius / 1.5f + mTickOffset;
        int count = mTicksBetweenLabel.ordinal();
        for (int i = 360; i >= 180; i -= mTickIntervals) {
            canvas.save();
            //for tick
            canvas.scale(-1, 1, middleCircleRect.centerX(), middleCircleRect.centerY());
            canvas.translate(middleCircleRect.centerX(), middleCircleRect.centerY());
            canvas.rotate(180);
            thetaInRadians = Math.toRadians(360 - i);
            slope = Math.tan(thetaInRadians);
            startTickX = (radiusOffset * Math.cos(thetaInRadians));
            startTickY = slope * startTickX;
            endTickX = startTickX + ((mTickLength) * Math.cos(thetaInRadians));
            endTickY = slope * endTickX;
            canvas.drawLine((float) startTickX, (float) startTickY, (float) endTickX, (float) endTickY, (mAngle <= 359 - i) ? mMiddleArcCirclePaint : mInnerCirclePaint);//drawing ticks
            count++;
            canvas.restore();
        }

        if (mArcEnabled) {
            // Draw the thumb nail
            canvas.translate(mTranslateX - mThumbXPos, mTranslateY - mThumbYPos);
            mThumb.draw(canvas);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        cy = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        cx = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec) / 2;

        circleRadius = cx;

        outerCircleRect.left = cx - circleRadius;
        outerCircleRect.top = cy - circleRadius;
        outerCircleRect.right = cx + circleRadius;
        outerCircleRect.bottom = cy + circleRadius;

        middleCircleRadius = circleRadius / 1.5f;
        middleCircleArcRect.left = cx - middleCircleRadius;
        middleCircleArcRect.top = cy - middleCircleRadius;
        middleCircleArcRect.right = cx + middleCircleRadius;
        middleCircleArcRect.bottom = cy + middleCircleRadius;

        mTranslateX = (cx);
        mTranslateY = (cy);

        middleCircleRect.left = cx - middleCircleRadius;
        middleCircleRect.top = cy - middleCircleRadius;
        middleCircleRect.right = cx + middleCircleRadius;
        middleCircleRect.bottom = cy + middleCircleRadius;

        int innerCircleRadius = circleRadius / 3;
        innerCircleRect.left = cx - innerCircleRadius;
        innerCircleRect.top = cy - innerCircleRadius;
        innerCircleRect.right = cx + innerCircleRadius;
        innerCircleRect.bottom = cy + innerCircleRadius;

        int arcStart = (int) mProgressSweep + mStartAngle + mRotation + 90;
        mThumbXPos = (int) ((circleRadius / 1.5f) * Math.cos(Math.toRadians(arcStart)));
        mThumbYPos = (int) ((circleRadius / 1.5f) * Math.sin(Math.toRadians(arcStart)));

        setmIsBrightnessColor(mIsBrightnessColor);

        if (mProgressRoundEdges) {
            mMiddleArcProgressCirclePaint.setStrokeCap(Paint.Cap.ROUND);
        }

        setTouchInSide(mTouchInside);
//        setTouchOutSide(mTouchOutside);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mArcEnabled) {
            this.getParent().requestDisallowInterceptTouchEvent(true);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    onStartTrackingTouch();
                    updateOnTouch(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    updateOnTouch(event);
                    break;
                case MotionEvent.ACTION_UP:
                    onStopTrackingTouch();
                    setPressed(false);
                    this.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    onStopTrackingTouch();
                    setPressed(false);
                    this.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return true;
        }
        return false;
    }

    private void updateThumbPosition() {
        int thumbAngle = (int) (mStartAngle + mProgressSweep + mRotation + 90);
        mThumbXPos = (int) ((circleRadius / 1.5f) * Math.cos(Math.toRadians(thumbAngle)));
        mThumbYPos = (int) ((circleRadius / 1.5f) * Math.sin(Math.toRadians(thumbAngle)));
    }

    private void updateProgress(int progress, boolean fromUser) {

        if (progress == INVALID_PROGRESS_VALUE) {
            return;
        }

        progress = (progress > mMax) ? mMax : progress;
        progress = (progress < 0) ? 0 : progress;
        mProgress = progress;

        if (mOnSemiCircularSeekbarArcChangeListener != null) {
            mOnSemiCircularSeekbarArcChangeListener.onProgressChanged(this, progress, fromUser);
        }

        mProgressSweep = (float) progress / mMax * mSweepAngle;

        updateThumbPosition();

        invalidate();
    }

    private float valuePerDegree() {
        return (float) mMax / mSweepAngle;
    }

    private int getProgressForAngle(double angle) {
        int touchProgress = (int) Math.round(valuePerDegree() * angle);

        touchProgress = (touchProgress < 0) ? INVALID_PROGRESS_VALUE : touchProgress;
        touchProgress = (touchProgress > mMax) ? INVALID_PROGRESS_VALUE : touchProgress;
        return touchProgress;
    }

    private void onStartTrackingTouch() {
        if (mOnSemiCircularSeekbarArcChangeListener != null) {
            mOnSemiCircularSeekbarArcChangeListener.onStartTrackingTouch(this);
        }
    }

    private void onStopTrackingTouch() {
        if (mOnSemiCircularSeekbarArcChangeListener != null) {
            mOnSemiCircularSeekbarArcChangeListener.onStopTrackingTouch(this);
        }
    }

    private void updateOnTouch(MotionEvent event) {
        boolean ignoreTouch = ignoreTouch(event.getX(), event.getY());
        if (ignoreTouch) {
            return;
        }
        setPressed(true);
        mTouchAngle = getTouchDegrees(event.getX(), event.getY());
        int progress = getProgressForAngle(mTouchAngle);
        onProgressRefresh(progress, true);
    }

    private boolean ignoreTouch(float xPos, float yPos) {
        boolean ignore = false;
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;

        float touchRadius = (float) Math.sqrt(((x * x) + (y * y)));
        if (touchRadius < mTouchIgnoreRadius) {
            ignore = true;
        }

        return ignore;
    }

    private double getTouchDegrees(float xPos, float yPos) {
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;

        //invert the x-coord if we are rotating anti-clockwise
        x = (mClockwise) ? x : -x;
        // convert to arc Angle
        double angle = Math.toDegrees(Math.atan2(y, x) + (Math.PI / 2)
                - Math.toRadians(mRotation));
        if (angle < 0) {
            angle = 360 + angle;
        }
        angle -= mStartAngle;
        return angle;
    }

    private void onProgressRefresh(int progress, boolean fromUser) {
        updateProgress(progress, fromUser);
    }

    public void setTouchInSide(boolean isEnabled) {
        int thumbHalfheight = (int) mThumb.getIntrinsicHeight() / 2;
        int thumbHalfWidth = (int) mThumb.getIntrinsicWidth() / 2;
        mTouchInside = isEnabled;
        if (mTouchInside) {
            mTouchIgnoreRadius = (float) middleCircleRadius / 4;
        } else {
            // Don't use the exact radius makes interaction too tricky
            mTouchIgnoreRadius = middleCircleRadius - Math.min(thumbHalfWidth, thumbHalfheight);
        }
    }

    public boolean ismIsBrightnessColor() {
        return mIsBrightnessColor;
    }

    public void setmIsBrightnessColor(boolean mIsBrightnessColor) {
        this.mIsBrightnessColor = mIsBrightnessColor;
    }

    public boolean isEnabled() {
        return mArcEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.mArcEnabled = enabled;
    }

    public void setClockwise(boolean isClockwise) {
        mClockwise = isClockwise;
    }

    public boolean isClockwise() {
        return mClockwise;
    }

    public int getArcWidth() {
        return mArcWidth;
    }

    public void setArcWidth(int mArcWidth) {
        this.mArcWidth = mArcWidth;
        mMiddleArcProgressCirclePaint.setStrokeWidth(mArcWidth);
    }

    public int getOuterCircleColor() {
        return mOuterCirclePaint.getColor();
    }

    public void setOuterCircleColor(int color) {
        mOuterCirclePaint.setColor(color);
        invalidate();
    }

    public int getInnerCircleColor() {
        return mInnerCirclePaint.getColor();
    }

    public void setInnerCircleColor(int color) {
        mInnerCirclePaint.setColor(color);
        invalidate();
    }

    public int getArcColor() {
        return mMiddleArcProgressCirclePaint.getColor();
    }

    public void setArcColor(int color) {
        mMiddleArcProgressCirclePaint.setColor(color);
        invalidate();
    }

    public int getTickLength() {
        return mTickLength;
    }

    public void setTickLength(int tickLength) {
        this.mTickLength = tickLength;
    }

    public TicksBetweenLabel getTicksBetweenLabel() {
        return mTicksBetweenLabel;
    }

    public void setTicksBetweenLabel(TicksBetweenLabel ticksBetweenLabel) {
        this.mTicksBetweenLabel = mTicksBetweenLabel;
        invalidate();
    }

    public int getTickIntervals() {
        return mTickIntervals;
    }

    public void setTickIntervals(int tickIntervals) {
        this.mTickIntervals = tickIntervals;
        invalidate();
    }

    public int getTickOffset() {
        return mTickOffset;
    }

    public void setTickOffset(int tickOffset) {
        this.mTickOffset = tickOffset;
    }

    public int getTickWidth() {
        return mTickWidth;
    }

    public void setTickWidth(int tickWidth) {
        this.mTickWidth = tickWidth;
        invalidate();
    }

    public boolean isRoundedEdges() {
        return mProgressRoundEdges;
    }

    public void setRoundedEdges(boolean roundedEdges) {
        this.mProgressRoundEdges = roundedEdges;
        if (roundedEdges) {
            mMiddleArcProgressCirclePaint.setStrokeCap(Paint.Cap.ROUND);
            gradientMiddleCirclePaint.setStrokeCap(Paint.Cap.ROUND);
        } else {
            mMiddleArcProgressCirclePaint.setStrokeCap(Paint.Cap.SQUARE);
        }
        invalidate();
    }

    public void setProgress(int progress) {
        updateProgress(progress, false);
    }

    public int getProgress() {
        return mProgress;
    }
}
