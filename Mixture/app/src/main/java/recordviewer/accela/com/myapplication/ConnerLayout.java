
package recordviewer.accela.com.myapplication;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by eyang on 12/7/16.
 */
public class ConnerLayout extends ViewGroup {
    private Context mContext;


    public ConnerLayout(Context context) {
        this(context, null);
    }

    public ConnerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConnerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {

    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int curWidth, curHeight;
        MarginLayoutParams layoutParams;
        int lh = 0, rh = 0, tw = 0, bw = 0;

        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                curWidth = child.getMeasuredWidth();
                curHeight = child.getMeasuredHeight();
                layoutParams = (MarginLayoutParams) child.getLayoutParams();

                if (i == 0 || i == 1) {
                    tw += curWidth + layoutParams.leftMargin + layoutParams.rightMargin;
                }
                if (i == 2 || i == 3) {
                    bw += curWidth + layoutParams.leftMargin + layoutParams.rightMargin;
                }
                if (i == 0 || i == 3) {
                    lh += curHeight + layoutParams.topMargin + layoutParams.bottomMargin;
                }
                if (i == 2 || i == 4) {
                    rh += curHeight + layoutParams.topMargin + layoutParams.bottomMargin;
                }
            }
        }
        int width = Math.max(tw, bw) + getPaddingLeft() + getPaddingRight(); //add padding
        int height = Math.max(lh, rh) + getPaddingTop() + getPaddingBottom(); //add padding
        setMeasuredDimension(widthMode == MeasureSpec.EXACTLY ? widthSize : width, heightMode == MeasureSpec.EXACTLY ? heightSize : height);
    }

    //    View的根据ViewGroup传人的测量值和模式，对自己宽高进行确定（onMeasure中完成），然后在onDraw中完成对自己的绘制。
//    ViewGroup需要给View传入view的测量值和模式（onMeasure中完成），而且对于此ViewGroup的父布局，自己也需要在onMeasure中完成对自己宽和高的确定。
//    此外，需要在onLayout中完成对其childView的位置的指定.

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        //存储基准的left top (子类.layout(),里的坐标是基于父控件的坐标，所以 x应该是从0+父控件左内边距开始，y从0+父控件上内边距开始)
        int baseLeft = 0 + getPaddingLeft();
        int baseTop = 0 + getPaddingTop();
        int baseRight = right - left - getPaddingRight(); // width - rightPadding
        int baseBottom = bottom - top - getPaddingBottom();

        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                MarginLayoutParams layoutParams = (MarginLayoutParams) child.getLayoutParams();
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();

                int l=0, t=0, r=0, b=0;
                if (i==0){
                    l = baseLeft+layoutParams.leftMargin;
                    t = baseTop+layoutParams.topMargin;
                    r = baseLeft+layoutParams.leftMargin+childWidth;
                    b = baseTop+layoutParams.topMargin+childHeight;
                }else if (i==1){
                    l = baseRight-layoutParams.rightMargin-childWidth;
                    t = baseTop+layoutParams.topMargin;
                    r = baseRight-layoutParams.rightMargin;
                    b = baseTop+layoutParams.topMargin+childHeight;
                }else if(i==2){
                    l = baseLeft+layoutParams.leftMargin;
                    t = baseBottom-layoutParams.bottomMargin-childHeight;
                    r = baseLeft+layoutParams.leftMargin+childWidth;
                    b = baseBottom-layoutParams.bottomMargin;
                }else if (i==3){
                    l = baseRight-layoutParams.rightMargin-childWidth;
                    t = baseBottom-layoutParams.bottomMargin-childHeight;
                    r = baseRight-layoutParams.rightMargin;
                    b = baseBottom-layoutParams.bottomMargin;
                }
                child.layout(l, t, r, b);
            }
        }
    }
}
