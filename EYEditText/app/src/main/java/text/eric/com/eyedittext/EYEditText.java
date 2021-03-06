
package text.eric.com.eyedittext;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;

/**
 * Created by eyang on 11/13/16.
 */
public class EYEditText extends EditText{
    // 画下划线的画笔
    private Paint paint;
    // 下划线的开始y坐标
    private int lineStartY;
    // 下划线的初始颜色
    private int preLineColor;

    // 额外的顶部内边距
    private int extraTopPadding;
    // 额外的底部内边距
    private int extraBottomPadding;

    // 绘制标签的画笔
    private TextPaint textPaint;
    // 标签的透明度
    private float textAlpha=0;
    // 标签的颜色
    private int labelColor;
    // 标签的文字内容
    private String labelText;
    // 超过限制长度时的下划线颜色
    private int overLengthColor;
    // 最大输入的字符数
    private int maxCount;
    // 当前输入的字符数
    private int presentCount;
    // 标签文字的动画
    ValueAnimator labelAnim;
    // 标签是否展现的标志
    private boolean isShow = false;
    // 标签动画中标签移动的比例
    private float yFraction;

    // 用来获取计数结果的字符串
    private StringBuilder countString;
    // 绘制计数结果的画笔
    private TextPaint countPaint;

    public EYEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public EYEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    private void initView(AttributeSet attrs){
        if (attrs==null){
            return;
        }
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.EYEditText);
        preLineColor = array.getColor(R.styleable.EYEditText_preLineColor, getResources().getColor(R.color.colorAccent));
        labelText = array.getString(R.styleable.EYEditText_labelText);
        labelColor = array.getColor(R.styleable.EYEditText_labelColor, getResources().getColor(R.color.colorPrimary));
        maxCount = array.getInteger(R.styleable.EYEditText_maxLength, -1);
        overLengthColor = array.getColor(R.styleable.EYEditText_overlengthColor, getResources().getColor(android.R.color.holo_red_dark));
        array.recycle();

        countString = new StringBuilder();

        // 初始化下划线画笔
        paint = new Paint();
        paint.setColor(Color.LTGRAY);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(dpToPix(0.35f));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(spToPix(14.5f)); // default
        textPaint.setColor(labelColor);

        countPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        countPaint.setTextSize(spToPix(14.5f));
        countPaint.setColor(Color.LTGRAY);

        // 获取额外的顶部内边距
        extraTopPadding = (int) getTextHeight(textPaint) + dpToPix(4);
        // 获取额外的底部内布距
        extraBottomPadding = (int) getTextHeight(textPaint) + dpToPix(6);
        // 矫正edittext的内边距
        correctPaddings();

        // 初始化标签动画
        labelAnim = ValueAnimator.ofFloat(0, 255);
        labelAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // 获取标签的透明度
                textAlpha = (float) animation.getAnimatedValue();
                // 获取文字的高度比例：
                // 当透明度为0时，高度比例为1.5，为1时，高度为1
                // 达到一种从底部浮现的效果
                yFraction = (float) (-(5.0f / 2550.0f) * textAlpha + 1.5);
                // 重绘
                invalidate();
            }
        });

        // 初始化监听器
        initListener();
        // 设置提示字体颜色
        setHintTextColor(Color.LTGRAY);
        // 设置文字方向
        setGravity(SCROLL_INDICATOR_LEFT);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 设置edittext的背景为空，主要为了隐藏自带的下划线
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(null);
        } else {
            setBackgroundDrawable(null);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {

        // 获取下划线的起点高度
        lineStartY = getScrollY()+getHeight()-getPaddingBottom()+dpToPix(5);
        // 设置标签的透明度
        textPaint.setAlpha((int) textAlpha);
        // 根据标签的高度比例绘制标签文字
        canvas.drawText(labelText, getScrollX(),(getScrollY()-dpToPix(4)+extraTopPadding)*yFraction, textPaint);
        // 绘制下划线
        canvas.drawRect(getScrollX(), lineStartY, getScrollX()+getWidth()-getPaddingRight(), lineStartY + dpToPix(0.8f), paint);
        // 根据是否有字符长度规定绘制右下角的计数
        if(maxCount!=-1) {
            canvas.drawText(countString.toString(), getScrollX()+getWidth()-getPaddingRight() - getTextWidth(countString.toString(), countPaint), lineStartY + extraBottomPadding, countPaint);
        }
        // 开始edittext原生的绘制
        super.onDraw(canvas);

    }

    private void initListener() {
        /// 设置edittetx内容改变监听器
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                // 当内容改变后，计数、并判断是否超过规定字符长度
                if(maxCount!=-1) {
                    // 添加计数字符串
                    countString.delete(0, countString.length());
                    presentCount = s.length();
                    countString.append(presentCount);
                    countString.append(" / ");
                    countString.append(maxCount);

                    // 超过规定长度时，绘制的颜色发生变化
                    if (presentCount > maxCount) {
                        paint.setColor(overLengthColor);
                        countPaint.setColor(overLengthColor);
                    } else {
                        paint.setColor(preLineColor);
                        countPaint.setColor(Color.LTGRAY);
                    }
                }

                // 当内容长度为0并获得焦点时：
                if(s.length()==0&&isShow&&isFocused()){
                    // 将标签动画逆序播放，将标签隐藏
                    labelAnim.reverse();
                    isShow = false;

                }else if(s.length()!=0&&!isShow&&isFocused()){
                    // 当内容长度不为0时，播放标签浮出一次
                    labelAnim.start();
                    isShow = true;
                }
            }
        });

        // 添加焦点的获取通知
        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // 获取焦点时
                if(hasFocus) {
                    // 改变下划线的粗细
                    paint.setStrokeWidth(dpToPix(1.3f));
                    // 改变标签的颜色
                    textPaint.setColor(labelColor);

                    if(presentCount>maxCount&&maxCount!=-1){
                        // 超出字符长度时，设置画笔颜色
                        paint.setColor(overLengthColor);
                        countPaint.setColor(overLengthColor);
                    }else {
                        // 不超出字符长度/不设置规定长度时，设置画笔颜色
                        paint.setColor(preLineColor);
                        countPaint.setColor(Color.LTGRAY);
                    }

                }else {
                    // 没有获取焦点时，改变下划线的颜色和粗细和标签颜色
                    textPaint.setColor(Color.LTGRAY);
                    paint.setColor(Color.LTGRAY);
                    paint.setStrokeWidth(dpToPix(0.35f));
                }

            }
        });
    }

    // dp to px
    private int dpToPix(float i) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,i,getContext().getResources().getDisplayMetrics());
    }

    // sp to pix
    private float spToPix(float i) {
        return  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,i,getContext().getResources().getDisplayMetrics());
    }

    // 获取字符串的宽度
    private int getTextWidth(String text,TextPaint paint) {
        return (int) paint.measureText(text);
    }

    // 获取文字的高度
    private float getTextHeight(Paint paint) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        return fontMetrics.bottom-fontMetrics.descent-fontMetrics.ascent;
    }

    // 因为我们需要绘制标签和下划线，因此需要重新设置padding值
    private void correctPaddings() {
        super.setPadding(getPaddingLeft(),getPaddingTop()+extraTopPadding,getPaddingRight(),getPaddingBottom()+extraBottomPadding+dpToPix(5));
    }
}
