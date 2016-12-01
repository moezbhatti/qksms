package com.moez.QKSMS.ui.attachmentlist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.moez.QKSMS.R;
import com.moez.QKSMS.ui.view.ComposeView;

import java.util.List;

/**
 * Created by barrus on 11/29/16.
 */

public class AttachmentListAdapter extends RecyclerView.Adapter<AttachmentListAdapter.AttachmentListViewHolder> {
    private List<AttachmentItem> attachmentItemList;
    private Context mContext;
    private ComposeView.OnItemClickListener onItemClickListener;

    public AttachmentListAdapter(Context context, List<AttachmentItem> attachmentItemList) {
        this.attachmentItemList = attachmentItemList;
        this.mContext = context;
    }

    @Override
    public AttachmentListViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_image, null);
        AttachmentListViewHolder viewHolder = new AttachmentListViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(AttachmentListViewHolder attachmentListViewHolder, int i) {
        AttachmentItem feedItem = attachmentItemList.get(i);
        attachmentListViewHolder.imageView.setImageBitmap(feedItem.getBitmap());

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(feedItem);
            }
        };
        attachmentListViewHolder.cancelView.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return (null != attachmentItemList ? attachmentItemList.size() : 0);
    }

    class AttachmentListViewHolder extends RecyclerView.ViewHolder {
        protected ImageView imageView;
        protected ImageView cancelView;

        public AttachmentListViewHolder(View view) {
            super(view);
            this.imageView = (ImageView) view.findViewById(R.id.list_item_image);
            this.cancelView = (ImageView) view.findViewById(R.id.cancel_attach);
        }
    }

    public ComposeView.OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(ComposeView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
