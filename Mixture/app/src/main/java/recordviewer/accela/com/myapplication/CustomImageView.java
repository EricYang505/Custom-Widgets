
package recordviewer.accela.com.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by eyang on 12/6/16.
 */
public class CustomImageView extends ImageView {
    Context mContext;
    private String mImageText;
    private float mImageTextSize;
    private int mImageTextColor;
    private Bitmap mImage;
    private int mImageScaleType;
    private Rect mImageRect;
    private Paint mTextPaint;
    private Rect mTextBound;
    private int mWidth;
    private int mHeight;
    private static final int IMAGE_SCALE_FITXY = 0;

    public CustomImageView(Context context) {
        super(context);
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle){
        TypedArray typedArray = this.mContext.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomImageView, defStyle, 0);
        mImageText = typedArray.getString(R.styleable.CustomImageView_imageText);
        mImageTextSize = typedArray.getDimension(R.styleable.CustomImageView_imageTextSize, 0);
        mImageTextColor = typedArray.getColor(R.styleable.CustomImageView_imageTextColor, getResources().getColor(R.color.colorPrimary));
        mImage = BitmapFactory.decodeResource(getResources(), typedArray.getResourceId(R.styleable.CustomImageView_image, 0));
        mImageScaleType = typedArray.getInt(R.styleable.CustomImageView_imageScaleType, 0);
        typedArray.recycle();
        
        mImageRect = new Rect();
        mTextBound = new Rect();
        mTextPaint = new Paint();
        mTextPaint.setTextSize(mImageTextSize);
        if (mImageText!=null)
            mTextPaint.getTextBounds(mImageText, 0, mImageText.length(), mTextBound);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (widthMode==MeasureSpec.EXACTLY){ //match_parent or explicit set size
            mWidth = widthSize;
        }else{ //AT_MOST(content_wrap) or UNSPECIFIED
            int imageWidth = getPaddingLeft() + getPaddingRight() + mImage.getWidth();
            int textWidth = getPaddingLeft() + getPaddingRight() + mTextBound.width();
            int curWidth = Math.max(imageWidth, textWidth);
            mWidth = Math.min(curWidth, widthSize); //curWidth shouldn't bigger than widthSize
        }

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode==MeasureSpec.EXACTLY){ //match_parent or explicit set size
            mHeight = heightSize;
        }else{ //AT_MOST(content_wrap) or UNSPECIFIED
            int curHeight = getPaddingTop() + getPaddingBottom() + mImage.getHeight() + mTextBound.height();
            mHeight = Math.min(curHeight, heightSize); //curWidth shouldn't bigger than widthSize
        }
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas){
        mTextPaint.setStrokeWidth(6.0f);
        mTextPaint.setStyle(Paint.Style.STROKE);
        mTextPaint.setColor(Color.BLUE);
        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), mTextPaint);

        mTextPaint.setColor(mImageTextColor);
        mTextPaint.setStyle(Paint.Style.FILL);

        mImageRect.left = getPaddingLeft();
        mImageRect.right = mWidth-getPaddingRight();
        mImageRect.top = getPaddingTop();
        mImageRect.bottom = mHeight-getPaddingBottom()-mTextBound.height();
        if (mImageScaleType == IMAGE_SCALE_FITXY){
            canvas.drawBitmap(mImage, null, mImageRect, mTextPaint);
        } else {
            mImageRect.left = mWidth/2-mImage.getWidth()/2;
            mImageRect.right = mWidth/2+mImage.getWidth()/2;
            mImageRect.top = (mHeight-mTextBound.height())/2-mImage.getHeight()/2;
            mImageRect.bottom = (mHeight-mTextBound.height())/2+mImage.getHeight()/2;
            canvas.drawBitmap(mImage, null, mImageRect, mTextPaint);
        }

        //if the text length is bigger than the width of the view, Truncate String with Ellipsis
        if (mTextBound.width()>mWidth){
            String msg = TextUtils.ellipsize(mImageText, new TextPaint(mTextPaint), (float) mWidth - getPaddingLeft() - getPaddingRight(), TextUtils.TruncateAt.END).toString();
            canvas.drawText(msg, getPaddingLeft(), mHeight-getPaddingBottom(), mTextPaint);
        }else{
            canvas.drawText(mImageText, mWidth/2 - mTextBound.width()/2, mHeight-getPaddingBottom(), mTextPaint);
        }


    }

}
