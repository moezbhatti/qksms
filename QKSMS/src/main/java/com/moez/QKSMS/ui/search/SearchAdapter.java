package com.moez.QKSMS.ui.search;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.utils.DateFormatter;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.base.RecyclerCursorAdapter;

import java.util.ArrayList;

public class SearchAdapter extends RecyclerCursorAdapter<SearchViewHolder, SearchData> {

    private String mQuery;

    public SearchAdapter(QKActivity context) {
        super(context);
    }

    public void setQuery(String query) {
        mQuery = query;
    }

    @Override
    protected SearchData getItem(int position) {
        mCursor.moveToPosition(position);
        return new SearchData(mCursor);
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.list_item_search, parent, false);
        return new SearchViewHolder(mContext, view);
    }

    @Override
    public void onBindViewHolder(SearchViewHolder holder, int position) {
        SearchData data = getItem(position);

        holder.mData = data;
        holder.mClickListener = mItemClickListener;
        holder.root.setOnClickListener(holder);
        holder.root.setOnLongClickListener(holder);

        if (data.contact != null) {
            Drawable avatarDrawable = data.contact.getAvatar(mContext, null);
            holder.avatar.setImageDrawable(avatarDrawable);
            holder.avatar.setContactName(data.contact.getName());
            holder.name.setText(data.contact.getName());
        } else {
            holder.avatar.setImageDrawable(null);
            holder.avatar.setContactName(null);
            holder.name.setText(null);
        }

        holder.date.setText(DateFormatter.getConversationTimestamp(mContext, data.date));

        if (mQuery != null) {

            // We need to make the search string bold within the full message
            // Get all of the start positions of the query within the messages
            ArrayList<Integer> indices = new ArrayList<>();
            int index = data.body.toLowerCase().indexOf(mQuery.toLowerCase());
            while (index >= 0) {
                indices.add(index);
                index = data.body.toLowerCase().indexOf(mQuery.toLowerCase(), index + 1);
            }

            // Make all instances of the search query bold
            SpannableStringBuilder sb = new SpannableStringBuilder(data.body);
            for (int i : indices) {
                StyleSpan span = new StyleSpan(Typeface.BOLD);
                sb.setSpan(span, i, i + mQuery.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }

            holder.snippet.setText(sb);
        }
    }
}
