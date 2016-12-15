

package recordviewer.accela.com.myapplication.recordviewer.accela.com.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by eyang on 12/14/16.
 */
public class LargeImageView extends View{
    private Context mContext;
    private BitmapRegionDecoder mDecoder;
    private BitmapFactory.Options options = new BitmapFactory.Options();
    private int mImageWidth;
    private int mImageHeight;
    private Rect mRect = new Rect();
    private boolean isTouchActive;
    private MotionEvent mPreMotionEvent;
    private PointF mPrePointer;
    private PointF mCurrentPointer;
    private PointF mExtenalPointer = new PointF();
    private int preX;
    private int preY;

    public LargeImageView(Context context) {
        this(context, null);
    }

    public LargeImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LargeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        options.inPreferredConfig = Bitmap.Config.RGB_565;
    }

    public void setInputStream(InputStream is)
    {
        try
        {
            mDecoder = BitmapRegionDecoder.newInstance(is, false);
            BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
            // Grab the bounds for the scene dimensions
            tmpOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, tmpOptions);
            mImageWidth = tmpOptions.outWidth;
            mImageHeight = tmpOptions.outHeight;

            requestLayout();
            invalidate();
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {

            try
            {
                if (is != null) is.close();
            } catch (Exception e)
            {
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas){
        Bitmap bm = mDecoder.decodeRegion(mRect, options);
        canvas.drawBitmap(bm, 0, 0, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        //display
        if (mImageHeight<0 || mImageWidth<0){
            mRect.top = getPaddingTop();
            mRect.left = getPaddingLeft();
            mRect.bottom = getPaddingTop() + height;
            mRect.right = getPaddingLeft() + width;
        } else {
            mRect.top = mImageHeight/2 - height/2;
            mRect.left = mImageWidth/2 - width/2;
            mRect.bottom = mImageHeight/2 + height/2;
            mRect.right = mImageWidth/2 + width/2;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        int x = (int)event.getRawX();
        int y = (int)event.getRawY();
        int dealtX = 0, dealtY = 0;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                preX = x;
                preY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                dealtX += Math.abs(x - preX);
                dealtY += Math.abs(y - preY);
                Log.d("dispatchTouchEvent", "dealtX:" + dealtX + " dealtY:" + dealtY);
                if (dealtX >= dealtY){
                    getParent().requestDisallowInterceptTouchEvent(true);
                }else{
//                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                preX = x;
                preY = y;
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int action = MotionEventCompat.getActionMasked(event);

        switch(action) {
            case (MotionEvent.ACTION_DOWN) :
                resetState();//防止没有接收到CANCEL or UP ,保险起见
                mPreMotionEvent = MotionEvent.obtain(event);
                this.isTouchActive = true;
                return true;
            case (MotionEvent.ACTION_MOVE) :
                if (isTouchActive){
                    updateStateByEvent(event);
                    updateView();
                    mPreMotionEvent.recycle();
                    mPreMotionEvent = MotionEvent.obtain(event); //update preMotionEvent
                }
                return true;
            case (MotionEvent.ACTION_UP) :
                isTouchActive = false;
                return true;
            case (MotionEvent.ACTION_CANCEL) :
                isTouchActive = false;
                return true;
            case (MotionEvent.ACTION_OUTSIDE) :

                return true;
            default :
                return super.onTouchEvent(event);
        }
    }

    private void updateView(){
        int moveX = (int) mExtenalPointer.x;
        int moveY = (int) mExtenalPointer.y;
        Log.d("updateView", "X: " + moveX + "Y: " + moveY);
        if (mImageWidth > getWidth() || mImageWidth==-1)
        {
            mRect.offset(-moveX, 0);
            checkWidth();
            invalidate();
        }
        if (mImageHeight > getHeight() || mImageHeight==-1)
        {
            mRect.offset(0, -moveY);
            checkHeight();
            invalidate();
        }
    }

    protected void updateStateByEvent(MotionEvent event)
    {
        final MotionEvent prev = mPreMotionEvent;

        mPrePointer = caculateFocalPointer(prev);
        mCurrentPointer = caculateFocalPointer(event);

        boolean mSkipThisMoveEvent = prev.getPointerCount() != event.getPointerCount();

        mExtenalPointer.x = mSkipThisMoveEvent ? 0 : mCurrentPointer.x - mPrePointer.x;
        mExtenalPointer.y = mSkipThisMoveEvent ? 0 : mCurrentPointer.y - mPrePointer.y;

    }

    protected void resetState()
    {
        if (mPreMotionEvent != null)
        {
            mPreMotionEvent.recycle();
            mPreMotionEvent = null;
        }

        isTouchActive = false;
    }

    /**
     * 根据event计算多指中心点
     *
     * @param event
     * @return
     */
    private PointF caculateFocalPointer(MotionEvent event)
    {
        final int count = event.getPointerCount();
        float x = 0, y = 0;
        for (int i = 0; i < count; i++)
        {
            x += event.getX(i);
            y += event.getY(i);
        }

        x /= count;
        y /= count;

        return new PointF(x, y);
    }

    private void checkWidth()
    {


        Rect rect = mRect;
        int imageWidth = mImageWidth;
        int imageHeight = mImageHeight;

        if (rect.right > imageWidth)
        {
            rect.right = imageWidth;
            rect.left = imageWidth - getWidth();
        }

        if (rect.left < 0)
        {
            rect.left = 0;
            rect.right = getWidth();
        }
    }


    private void checkHeight()
    {

        Rect rect = mRect;
        int imageWidth = mImageWidth;
        int imageHeight = mImageHeight;

        if (rect.bottom > imageHeight)
        {
            rect.bottom = imageHeight;
            rect.top = imageHeight - getHeight();
        }

        if (rect.top < 0)
        {
            rect.top = 0;
            rect.bottom = getHeight();
        }
    }

}
