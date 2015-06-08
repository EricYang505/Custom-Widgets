package com.pageviewer.eyang.view;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.pageviewer.eyang.Test.R;



/**
 * Created by eyang on 5/26/15.
 */
public class PageViewer extends FrameLayout{
    private static final int sPageNumber = 3;
    private DotsView mDotsView;
    private Context mContext;
    private ViewPager viewPager;
    private PageViewerAdapter mAdapter;
    private float density;

    public PageViewer(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public PageViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    public PageViewer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

    private void init() {
        density = this.mContext.getResources().getDisplayMetrics().density;
        viewPager = new ViewPager(mContext);
        mAdapter = new PageViewerAdapter();
        viewPager.setAdapter(mAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){

            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                mDotsView.updateActiveDot(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        viewPager.setLayoutParams(lp);
        this.addView(viewPager);

        mDotsView = new DotsView(mContext);
        lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, (int)(10*density));
        lp.gravity = Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL;
        mDotsView.setLayoutParams(lp);
        mDotsView.setDotsNumber(sPageNumber);
        this.addView(mDotsView);
    }


    private class PageViewerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return sPageNumber;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.page_view_item, null);
            TextView textView = (TextView) view.findViewById(R.id.textViewId);
            ImageView imageView = (ImageView) view.findViewById(R.id.imageViewId);
            textView.setText("Penguin" + position);
            if (position==1){
                imageView.setImageResource(R.drawable.pic2);
            }else if (position==2){
                imageView.setImageResource(R.drawable.pic3);
            }else {
                imageView.setImageResource(R.drawable.pic1);
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
