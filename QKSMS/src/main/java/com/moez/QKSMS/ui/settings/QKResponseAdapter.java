package com.moez.QKSMS.ui.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.moez.QKSMS.R;
import com.moez.QKSMS.ui.view.QKEditText;
import com.moez.QKSMS.ui.view.QKTextView;

import java.util.ArrayList;
import java.util.List;

public  class QKResponseAdapter extends ArrayAdapter<String> {
    private List<String> mResponses = new ArrayList<>();

    public QKResponseAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
        mResponses.addAll(objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder vh;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_qk_response, null);

            vh = new ViewHolder();
            vh.mResponseIndex = (QKTextView) convertView.findViewById(R.id.response_index);
            vh.mResponse = (QKEditText) convertView.findViewById(R.id.response);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        vh.position = position;
        vh.mResponseIndex.setText(Integer.toString(position + 1));
        vh.mResponse.setText(mResponses.get(position));
        vh.mResponse.setTextChangedListener(new QKEditText.TextChangedListener() {
            @Override
            public void onTextChanged(CharSequence s) {
                mResponses.remove(vh.position);
                mResponses.add(vh.position, s.toString());
            }
        });

        return convertView;
    }

    public List<String> getResponses() {
        return mResponses;
    }

    @Override
    public int getCount() {
        return mResponses.size();
    }
}

class ViewHolder {
    int position;
    QKTextView mResponseIndex;
    QKEditText mResponse;
}
