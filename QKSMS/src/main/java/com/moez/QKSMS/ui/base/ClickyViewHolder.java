package com.moez.QKSMS.ui.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class ClickyViewHolder<DataType> extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    public RecyclerCursorAdapter.ItemClickListener<DataType> clickListener;
    public DataType data;
    public Context context;

    public ClickyViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void onClick(View v) {
        if (clickListener != null) {
            clickListener.onItemClick(data, v);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (clickListener != null) {
            clickListener.onItemLongClick(data, v);
        }

        return true;
    }
}
