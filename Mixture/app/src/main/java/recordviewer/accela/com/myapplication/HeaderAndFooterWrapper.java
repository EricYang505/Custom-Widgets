

package recordviewer.accela.com.myapplication;

import android.content.Context;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by eyang on 11/28/16.
 */
public class HeaderAndFooterWrapper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 1000;
    private static final int TYPE_FOOTER = 2000;

    private SparseArrayCompat<View> mHeaderViews = new SparseArrayCompat<>(); //key is viewType
    private SparseArrayCompat<View> mFootViews = new SparseArrayCompat<>(); //key is viewType

    private Context mContext;
    private final RecyclerView.Adapter mAdapter; //decorate pattern

    ListView d;

    public HeaderAndFooterWrapper(Context context, RecyclerView.Adapter adapter){
        this.mContext = context;
        this.mAdapter = adapter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderViews.get(viewType)!=null){
            View viewItem = mHeaderViews.get(viewType);
            Log.d("onCreateViewHolder", "ViewType_HeaderOrFooter:" + viewType);
            return ViewHolder.createHeaderOrFooter(viewItem);
        }
        if (mFootViews.get(viewType)!=null){
            View viewItem = mFootViews.get(viewType);
            Log.d("onCreateViewHolder", "ViewType_HeaderOrFooter:" + viewType);
            return ViewHolder.createHeaderOrFooter(viewItem);
        }
        return mAdapter.onCreateViewHolder(parent, viewType);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (isHeaderPosition(position) || isFooterPosition(position))
            return;
        this.mAdapter.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return this.mHeaderViews.size() + this.mFootViews.size() + this.mAdapter.getItemCount();
    }

    @Override
    public int getItemViewType(int position){
        if (isHeaderPosition(position)){
            return mHeaderViews.keyAt(position);//key is viewType
        }else if (isFooterPosition(position)){
            return mFootViews.keyAt(position-mHeaderViews.size()-mAdapter.getItemCount());
        }else {
            return mAdapter.getItemViewType(position);
        }
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder)
    {
        mAdapter.onViewAttachedToWindow(holder);
        int position = holder.getLayoutPosition();
        if (isHeaderPosition(position) || isFooterPosition(position))
        {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();

            if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams)
            {

                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;

                p.setFullSpan(true);
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        mAdapter.onAttachedToRecyclerView(recyclerView);

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager)
        {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();

            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup()
            {
                @Override
                public int getSpanSize(int position)
                {
                    if (isHeaderPosition(position) || isFooterPosition(position)){
                        return gridLayoutManager.getSpanCount(); //header footer occupy entire row
                    }else if(spanSizeLookup!=null){
                        return spanSizeLookup.getSpanSize(position);
                    }
                    return 1;
                }
            });
            gridLayoutManager.setSpanCount(gridLayoutManager.getSpanCount());
        }
    }



    private boolean isHeaderPosition(int position){
        return position < this.mHeaderViews.size();
    }

    private boolean isFooterPosition(int position){
        return position >= this.mHeaderViews.size() + this.mAdapter.getItemCount();
    }

    public void addHeader(View view){
        this.mHeaderViews.put(TYPE_HEADER+this.mHeaderViews.size(), view);
    }

    public void addFooter(View view){
        this.mFootViews.put(TYPE_FOOTER+this.mFootViews.size(), view);
    }
}
