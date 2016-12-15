

package recordviewer.accela.com.myapplication.recordviewer.accela.com.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Editable;
import android.text.Layout;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

/**
 * Created by eyang on 12/1/16.
 */
public class AreaEditText extends EditText{
    private int mCurStart = 0;
    private int xStart = 0;
    private int yStart = 0;

    private int left;
    private int top;
    private int lines;
    private double lineHeight;

    private float xx;
    private int yy;


    public AreaEditText(Context context) {
        super(context);
        init(context);
    }

    public AreaEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.e("onLayout", "bottom: " + b);
        left = l;
        top = t;
    }

    private void init(Context context){
        this.addTextChangedListener(new InputValidator());
        lineHeight = getPaint().getFontMetrics().bottom - getPaint().getFontMetrics().top;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw the shadow
        if (mCurStart != 0) {
            Paint myPaint = new Paint();
            myPaint.setStyle(Paint.Style.STROKE);
            myPaint.setColor(Color.rgb(255, 0, 0));
            myPaint.setStrokeWidth(10);
            int lp = left + 8;
            int tp = (int) Math.round(top + lines * lineHeight + .5);
            canvas.drawRect(xx, yy, xx + 600, (float) (yy + lineHeight), myPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        // Try for a width based on our minimum
//        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
//
//        int w = Math.max(minw, MeasureSpec.getSize(widthMeasureSpec));
//
//        // Whatever the width ends up being, ask for a height that would let the pie
//        // get as big as it can
//        int minh = (w - (int) mTextWidth) + getPaddingBottom() + getPaddingTop();
//        int h = Math.min(MeasureSpec.getSize(heightMeasureSpec), minh);
//
//        setMeasuredDimension(w, h);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public int getCurrentCursorLine(EditText editText)
    {
        int selectionStart = Selection.getSelectionStart(editText.getText());
        Layout layout = editText.getLayout();

        if (!(selectionStart == -1)) {
            return layout.getLineForOffset(selectionStart);
        }

        return -1;
    }

    private class InputValidator implements TextWatcher {



        public void afterTextChanged(Editable s) {
            mCurStart = getSelectionStart();
            int selectionStart = Selection.getSelectionStart(AreaEditText.this.getText());
            Layout layout = AreaEditText.this.getLayout();
            if (selectionStart != -1 && layout!=null) {
                int line = layout.getLineForOffset(selectionStart);
                int baseline = layout.getLineBaseline(line);
                int ascent = layout.getLineAscent(line);
                 xx = layout.getPrimaryHorizontal(selectionStart);
                 yy = baseline - ascent;

//                lines = Math.min(10, layout.getLineForOffset(selectionStart));
            }
        }
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {

        }
    }
}
