package recordviewer.accela.com.myapplication;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by eyang on 11/28/16.
 */
public class ViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView textView;

    public ViewHolder(View itemView) {
        super(itemView);
        imageView = (ImageView) itemView.findViewById(R.id.imageId);
        textView = (TextView) itemView.findViewById(R.id.textId);
    }

    public ViewHolder(View itemView, boolean isHeaderOrFooter) {
        super(itemView);
        if (isHeaderOrFooter){
            return;
        }
        imageView = (ImageView) itemView.findViewById(R.id.imageId);
        textView = (TextView) itemView.findViewById(R.id.textId);
    }

    public static ViewHolder createHeaderOrFooter(View itemView){
        return new ViewHolder(itemView, true);
    }
}
