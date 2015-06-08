package com.pageviewer.eyang.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.pageviewer.eyang.Test.R;

/**
 * Created by eyang on 5/25/15.
 */
public class DotsView extends LinearLayout {

    private float density;
    private ImageView[] imageDots;
    private int currentIndex = 0;
    private Context mContext;

    public DotsView(Context context) {
        super(context);
        mContext = context;
        initView(context);
    }

    public DotsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView(context);
    }

    public DotsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView(context);
    }

    public void updateActiveDot(int index){
        this.currentIndex = index;
        for(int i=0; i<imageDots.length; i++){
            if(i==currentIndex){
                imageDots[i].setImageResource(R.drawable.active);
            }else{
                imageDots[i].setImageResource(R.drawable.inactive);
            }
        }
    }

    public void setDotsNumber(int number){
        if(number>0){
            if(imageDots==null || number!=imageDots.length){
                setupDotsView(number);
            }
        }
        if(currentIndex<0 || currentIndex>= imageDots.length)
            currentIndex = 0;
    }

    private void setupDotsView(int number) {
        this.removeAllViews();
        imageDots = new ImageView[number];
        int margin = (int) (8*density);
        for(int i=0; i<number; i++){
            imageDots[i] = new ImageView(mContext);
            imageDots[i].setScaleType(ImageView.ScaleType.FIT_XY);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int)(16*density), (int)(16*density));
            layoutParams.setMargins(margin, margin, margin, margin);
            this.addView(imageDots[i], layoutParams);
        }
        updateActiveDot(currentIndex);
    }

    private void initView(Context context) {
        density = context.getResources().getDisplayMetrics().density;
    }
}
