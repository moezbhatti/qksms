package com.moez.QKSMS.ui.base;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import butterknife.ButterKnife;

public abstract class ClickyViewHolder<DataType> extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    public RecyclerCursorAdapter.ItemClickListener<DataType> mClickListener;
    public DataType mData;
    public QKActivity mContext;

    public ClickyViewHolder(QKActivity context, View view) {
        super(view);
        ButterKnife.bind(this, view);
        mContext = context;
    }

    @Override
    public void onClick(View v) {
        if (mClickListener != null) {
            mClickListener.onItemClick(mData, v);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mClickListener != null) {
            mClickListener.onItemLongClick(mData, v);
        }

        return true;
    }
}
