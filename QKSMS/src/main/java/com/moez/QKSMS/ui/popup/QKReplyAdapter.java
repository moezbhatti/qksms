package com.moez.QKSMS.ui.popup;

import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.messagelist.MessageListAdapter;
import com.moez.QKSMS.ui.messagelist.MessageListViewHolder;

/**
 * ArrayAdapter wrapper of MessageListAdapter to allow regular ListView usage in QKReplyActivity
 * RecyclerView in variable-height windows causes a lot of issues
 */
public class QKReplyAdapter extends ArrayAdapter {

    private MessageListAdapter mMessageListAdapter;

    public QKReplyAdapter(QKActivity context) {
        super(context, 0);
        mMessageListAdapter = new MessageListAdapter(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageListViewHolder holder;

        if (convertView == null) {
            holder = mMessageListAdapter.onCreateViewHolder(parent, getItemViewType(position));
            convertView = holder.itemView;
            convertView.setTag(holder);
        } else {
            holder = (MessageListViewHolder) convertView.getTag();
        }

        mMessageListAdapter.bindViewHolder(holder, position);
        return holder.itemView;
    }

    @Override
    public int getCount() {
        return mMessageListAdapter != null ? mMessageListAdapter.getCount() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return mMessageListAdapter.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    public void changeCursor(Cursor cursor) {
        mMessageListAdapter.changeCursor(cursor);
        notifyDataSetChanged();
    }
}
