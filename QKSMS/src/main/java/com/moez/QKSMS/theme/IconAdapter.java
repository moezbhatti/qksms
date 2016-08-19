package com.moez.QKSMS.theme;

import android.content.Context;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.moez.QKSMS.R;

public class IconAdapter extends RecyclerView.Adapter<IconViewHolder> {

    private Context mContext;
    private AdapterViewCompat.OnItemClickListener mOnItemClickListener;
    private int[] mIconIds = new int[]{
            R.mipmap.ic_launcher, R.mipmap.ic_launcher_dark, R.mipmap.ic_launcher_red, R.mipmap.ic_launcher_pink,
            R.mipmap.ic_launcher_purple, R.mipmap.ic_launcher_deep_purple, R.mipmap.ic_launcher_indigo,
            R.mipmap.ic_launcher_blue, R.mipmap.ic_launcher_light_blue, R.mipmap.ic_launcher_cyan,
            R.mipmap.ic_launcher_teal, R.mipmap.ic_launcher_green, R.mipmap.ic_launcher_light_green,
            R.mipmap.ic_launcher_lime, R.mipmap.ic_launcher_yellow, R.mipmap.ic_launcher_amber,
            R.mipmap.ic_launcher_orange, R.mipmap.ic_launcher_deep_orange, R.mipmap.ic_launcher_brown,
            R.mipmap.ic_launcher_grey, R.mipmap.ic_launcher_blue_grey
    };

    public IconAdapter(Context context, AdapterViewCompat.OnItemClickListener onItemClickListener) {
        mContext = context;
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public IconViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_icon, parent, false);
        return new IconViewHolder(this, view);
    }

    @Override
    public void onBindViewHolder(IconViewHolder holder, int position) {
        holder.bind(position);
        holder.itemView.setOnClickListener(v -> mOnItemClickListener.onItemClick(null, holder.itemView, position, 0));
    }

    public Integer getItem(int position) {
        return mIconIds[position];
    }

    @Override
    public int getItemCount() {
        return mIconIds.length;
    }
}
