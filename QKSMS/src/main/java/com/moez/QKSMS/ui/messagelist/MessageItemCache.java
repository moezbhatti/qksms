package com.moez.QKSMS.ui.messagelist;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.util.LruCache;

import com.google.android.mms.MmsException;
import com.moez.QKSMS.common.utils.CursorUtils;

import java.util.regex.Pattern;

public class MessageItemCache extends LruCache<Long, MessageItem> {
    private final String TAG = "MessageItemCache";

    private Context mContext;
    private MessageColumns.ColumnsMap mColumnsMap;
    private Pattern mSearchHighlighter;

    public MessageItemCache(Context context, MessageColumns.ColumnsMap columnsMap, Pattern searchHighlighter, int maxSize) {
        super(maxSize);

        mContext = context;
        mColumnsMap = columnsMap;
        mSearchHighlighter = searchHighlighter;
    }

    @Override
    protected void entryRemoved(boolean evicted, Long key, MessageItem oldValue,
                                MessageItem newValue) {
        oldValue.cancelPduLoading();
    }

    /**
     * Generates a unique key for this message item given its type and message ID.
     *
     * @param type
     * @param msgId
     */
    public long getKey(String type, long msgId) {
        if (type.equals("mms")) {
            return -msgId;
        } else {
            return msgId;
        }
    }


    public MessageItem get(String type, long msgId, Cursor c) {
        long key = getKey(type, msgId);
        MessageItem item = get(key);

        if (item == null && CursorUtils.isValid(c)) {
            try {
                item = new MessageItem(mContext, type, c, mColumnsMap, mSearchHighlighter, false);
                key = getKey(item.mType, item.mMsgId);
                put(key, item);
            } catch (MmsException e) {
                Log.e(TAG, "getCachedMessageItem: ", e);
            }
        }
        return item;
    }
}
