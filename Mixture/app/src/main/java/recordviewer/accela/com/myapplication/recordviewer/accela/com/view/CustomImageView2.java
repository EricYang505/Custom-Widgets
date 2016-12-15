

package recordviewer.accela.com.myapplication.recordviewer.accela.com.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import recordviewer.accela.com.myapplication.R;

/**
 * Created by eyang on 12/6/16.
 */
public class CustomImageView2 extends LinearLayout {
    Context mContext;
    private String mImageText;
    private float mImageTextSize;
    private int mImageTextColor;
    private Bitmap mImage;
    private int mImageScaleType;
    private static final int IMAGE_SCALE_FITXY = 0;
    private float mDensity;

    public CustomImageView2(Context context) {
        this(context, null);
    }

    public CustomImageView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomImageView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = this.mContext.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomImageView2, defStyleAttr, 0);
        mImageText = typedArray.getString(R.styleable.CustomImageView2_imageText2);
        mImageTextSize = typedArray.getDimension(R.styleable.CustomImageView2_imageTextSize2, 0);
        mImageTextColor = typedArray.getColor(R.styleable.CustomImageView2_imageTextColor2, getResources().getColor(R.color.colorPrimary));
        mImage = BitmapFactory.decodeResource(getResources(), typedArray.getResourceId(R.styleable.CustomImageView2_image2, 0));
        mImageScaleType = typedArray.getInt(R.styleable.CustomImageView2_imageScaleType2, 0);
        typedArray.recycle();

        this.setOrientation(VERTICAL);
        mDensity = getResources().getDisplayMetrics().density;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        ImageView imageView = new ImageView(mContext);
        if (mImageScaleType == IMAGE_SCALE_FITXY) {
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        imageView.setImageBitmap(mImage);
        this.addView(imageView, lp);

        TextView textView = new TextView(mContext);
        textView.setText(mImageText);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mImageTextSize);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setTextColor(mImageTextColor);
        this.addView(textView, lp);
    }



}
