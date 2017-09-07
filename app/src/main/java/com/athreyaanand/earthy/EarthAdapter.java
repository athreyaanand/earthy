package com.athreyaanand.earthy;

/**
 * Created by athreya on 3/20/2017.
 */

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

public class EarthAdapter extends RecyclerView.Adapter<EarthAdapter.MyViewHolder> {

    private List<EarthPackage> earthList;
    private Context mContext;
    private CardView cv;
    private int cellCount = 0;

    final private int AD_TYPE = 127;
    final private int NORMAL_TYPE = 203;
    final private int FEATURE_TYPE = 347;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, domain, time;
        public PhotoView thumbnail;

        public MyViewHolder(View view) {
            super(view);

            cv = (CardView) view.findViewById(R.id.cv);
            title = (TextView) view.findViewById(R.id.title);
            domain = (TextView) view.findViewById(R.id.domain);
            time = (TextView) view.findViewById(R.id.relativeTime);
            thumbnail = (PhotoView) view.findViewById(R.id.featureImage);
        }
    }

    public EarthAdapter(List<EarthPackage> earthList, Context context) {
        this.earthList = earthList;
        this.mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView;

        switch (viewType){
            case FEATURE_TYPE: {
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.first_list_item, parent, false);
                System.out.println("NEWSADAPTER: Returned FEATURE @index("+cellCount+")");
                break;
            }
            case NORMAL_TYPE: {
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.first_list_item, parent, false);
                System.out.println("NEWSADAPTER: Returned NORMAL @index("+cellCount+")");
                break;
            }
            default: {
                itemView = null;
                System.out.println("NEWSADAPTER: Could not return any type @index("+cellCount+")");
            }
        }

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        int viewType = getItemViewType(position);

        switch (viewType){
            case FEATURE_TYPE: {
                System.out.println("NEWSADAPTER: Loading FEATURE");
                //DOES NOT BREAK INTENTINLY
            }
            case NORMAL_TYPE: {
                System.out.println("NEWSADAPTER: Loading NORMAL");
                EarthPackage earthy = (EarthPackage) earthList.get(position);
                    holder.title.setText(earthy.getTitle());
                        holder.title.setEllipsize(TextUtils.TruncateAt.END);
                        holder.title.setMaxLines(2);
                    holder.domain.setText(earthy.getDomain());
                    holder.time.setText(earthy.getPrettyUtc());
                if (earthy.getThumbnail() != null)
                    Picasso.with(mContext).load(earthy.getThumbnail()).fit().centerCrop().into(holder.thumbnail);

                break;
            }
            default: {
            }
        }
    }

    @Override
    public int getItemCount() {
        return earthList.size(); // + (newsList.size() / 5);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return FEATURE_TYPE;
        else
            return NORMAL_TYPE;
    }
}
