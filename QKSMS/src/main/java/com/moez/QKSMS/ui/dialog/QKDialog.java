package com.moez.QKSMS.ui.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.utils.Units;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.view.QKTextView;

import java.util.ArrayList;

public class QKDialog extends DialogFragment {
    private final String TAG = "QKDialog";

    protected QKActivity mContext;
    protected Resources mResources;

    private boolean mTitleEnabled;
    private String mTitleText;
    private QKTextView mTitleView;

    private LinearLayout mContentPanel;

    private boolean mMessageEnabled;
    private String mMessageText;
    private QKTextView mMessageView;

    private LinearLayout mCustomPanel;

    private boolean mCustomViewEnabled;
    private View mCustomView;

    private LinearLayout mButtonBar;
    private int mButtonBarOrientation = LinearLayout.HORIZONTAL;

    private boolean mPositiveButtonEnabled;
    private String mPositiveButtonText;
    private OnClickListener mPositiveButtonClickListener;
    private QKTextView mPositiveButtonView;

    private boolean mNeutralButtonEnabled;
    private String mNeutralButtonText;
    private OnClickListener mNeutralButtonClickListener;
    private QKTextView mNeutralButtonView;

    private boolean mNegativeButtonEnabled;
    private String mNegativeButtonText;
    private OnClickListener mNegativeButtonClickListener;
    private QKTextView mNegativeButtonView;

    private ArrayList<String> mMenuItems = new ArrayList<>();
    private ArrayList<Long> mMenuItemIds = new ArrayList<>();

    public QKDialog() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new Dialog(mContext);

        Window window = dialog.getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_material, null);

        if (mTitleEnabled || mMessageEnabled) {
            mContentPanel = (LinearLayout) view.findViewById(R.id.contentPanel);
            mContentPanel.setVisibility(View.VISIBLE);
        }

        if (mTitleEnabled) {
            mTitleView = (QKTextView) view.findViewById(R.id.alertTitle);
            mTitleView.setVisibility(View.VISIBLE);
            mTitleView.setText(mTitleText);
            Log.d(TAG, "title enabled");
        }

        if (mMessageEnabled) {
            mMessageView = (QKTextView) view.findViewById(R.id.message);
            mMessageView.setVisibility(View.VISIBLE);
            mMessageView.setText(mMessageText);
        }

        if (mCustomViewEnabled) {
            mCustomPanel = (LinearLayout) view.findViewById(R.id.customPanel);
            mCustomPanel.setVisibility(View.VISIBLE);

            if (mCustomView instanceof ListView) {
                mCustomPanel.addView(mCustomView);
            } else {
                ScrollView scrollView = new ScrollView(mContext);
                scrollView.addView(mCustomView);
                mCustomPanel.addView(scrollView);
            }
        }

        if (mPositiveButtonEnabled || mNegativeButtonEnabled) {
            mButtonBar = (LinearLayout) view.findViewById(R.id.buttonPanel);
            mButtonBar.setVisibility(View.VISIBLE);
            mButtonBar.setOrientation(mButtonBarOrientation);
        }

        if (mPositiveButtonEnabled) {
            mPositiveButtonView = (QKTextView) view.findViewById(R.id.buttonPositive);
            mPositiveButtonView.setVisibility(View.VISIBLE);
            mPositiveButtonView.setText(mPositiveButtonText);
            mPositiveButtonView.setTextColor(ThemeManager.getColor());
            mPositiveButtonView.setOnClickListener(mPositiveButtonClickListener);
        }

        if (mNeutralButtonEnabled) {
            mNeutralButtonView = (QKTextView) view.findViewById(R.id.buttonNeutral);
            mNeutralButtonView.setVisibility(View.VISIBLE);
            mNeutralButtonView.setText(mNeutralButtonText);
            mNeutralButtonView.setOnClickListener(mNeutralButtonClickListener);
        }

        if (mNegativeButtonEnabled) {
            mNegativeButtonView = (QKTextView) view.findViewById(R.id.buttonNegative);
            mNegativeButtonView.setVisibility(View.VISIBLE);
            mNegativeButtonView.setText(mNegativeButtonText);
            mNegativeButtonView.setOnClickListener(mNegativeButtonClickListener);
        }

        dialog.setContentView(view);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        Window window = getDialog().getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0.33f;
        windowParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(windowParams);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = (int) (metrics.widthPixels * 0.9);

        window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public QKDialog setContext(QKActivity context) {
        mContext = context;
        mResources = context.getResources();
        return this;
    }

    public QKDialog setTitle(int resource) {
        return setTitle(mResources.getString(resource));
    }

    public QKDialog setTitle(String title) {
        mTitleEnabled = true;
        mTitleText = title;
        return this;
    }

    public QKDialog setMessage(int resource) {
        return setMessage(mResources.getString(resource));
    }

    public QKDialog setMessage(String message) {
        mMessageEnabled = true;
        mMessageText = message;
        return this;
    }

    public QKDialog setCancelOnTouchOutside(boolean cancelable) {
        setCancelable(cancelable);
        return this;
    }

    // TODO fix stack from bottom issue
    public QKDialog setButtonBarOrientation(int orientation) {
        mButtonBarOrientation = orientation;
        return this;
    }

    public QKDialog setCustomView(View view) {
        mCustomViewEnabled = true;
        mCustomView = view;
        return this;
    }

    public QKDialog addMenuItem(@StringRes int titleId, long id) {
        return addMenuItem(mContext.getString(titleId), id);
    }

    /**
     * Adds a menu style item, allowing for dynamic ids for different items. This is useful when the item order
     * is set dynamically, like in the MessageListItem
     *
     * If you use this method, always make sure to use #buildMenu(OnItemClickListener) to compile the items and add the
     * click listener
     */
    public QKDialog addMenuItem(String title, long id) {
        mMenuItems.add(title);
        mMenuItemIds.add(id);
        return this;
    }

    public QKDialog buildMenu(final OnItemClickListener onItemClickListener) {
        ArrayAdapter adapter = new ArrayAdapter<>(mContext, R.layout.list_item_simple, mMenuItems);
        ListView listView = new ListView(mContext);
        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setPadding(0, Units.dpToPx(mContext, 8), 0, Units.dpToPx(mContext, 8));
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Ignore the given ID and use the one that we set in #addMenuItem()
                onItemClickListener.onItemClick(parent, view, position, mMenuItemIds.get(position));
                dismiss();
            }
        });
        return setCustomView(listView);
    }

    public QKDialog setItems(int resource, final OnItemClickListener onClickListener) {
        return setItems(mResources.getStringArray(resource), onClickListener);
    }

    public QKDialog setItems(String[] items, final OnItemClickListener onClickListener) {
        ArrayAdapter adapter = new ArrayAdapter<>(mContext, R.layout.list_item_simple, items);
        ListView listView = new ListView(mContext);
        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setPadding(0, Units.dpToPx(mContext, 8), 0, Units.dpToPx(mContext, 8));
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (onClickListener != null) {
                    onClickListener.onItemClick(parent, view, position, id);
                    dismiss();
                }
            }
        });
        return setCustomView(listView);
    }

    public QKDialog setDoubleLineItems(int titles, int bodies, final OnItemClickListener onClickListener) {
        return setDoubleLineItems(mResources.getStringArray(titles), mResources.getStringArray(bodies), onClickListener);
    }

    public QKDialog setDoubleLineItems(String[] titles, String[] bodies, final OnItemClickListener onClickListener) {

        int size = Math.min(titles.length, bodies.length);
        DoubleLineListItem[] doubleLineListItems = new DoubleLineListItem[size];
        for (int i = 0; i < size; i++) {
            doubleLineListItems[i] = new DoubleLineListItem();
            doubleLineListItems[i].title = titles[i];
            doubleLineListItems[i].body = bodies[i];
        }

        ArrayAdapter adapter = new DoubleLineArrayAdapter(mContext, doubleLineListItems);
        ListView listView = new ListView(mContext);
        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setPadding(0, Units.dpToPx(mContext, 8), 0, Units.dpToPx(mContext, 8));
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (onClickListener != null) {
                    onClickListener.onItemClick(parent, view, position, id);
                    dismiss();
                }
            }
        });
        return setCustomView(listView);
    }

    public QKDialog setTripleLineItems(int titles, int subtitles, int bodies, final OnItemClickListener onClickListener) {
        return setTripleLineItems(mResources.getStringArray(titles), mResources.getStringArray(subtitles), mResources.getStringArray(bodies), onClickListener);
    }

    public QKDialog setTripleLineItems(String[] titles, String[] subtitles, String[] bodies, final OnItemClickListener onClickListener) {

        int size = Math.min(titles.length, Math.min(subtitles.length, bodies.length));
        TripleLineListItem[] tripleLineListItems = new TripleLineListItem[size];
        for (int i = 0; i < size; i++) {
            tripleLineListItems[i] = new TripleLineListItem();
            tripleLineListItems[i].title = titles[i];
            tripleLineListItems[i].subtitle = subtitles[i];
            tripleLineListItems[i].body = bodies[i];
        }

        ArrayAdapter adapter = new TripleLineArrayAdapter(mContext, tripleLineListItems);
        ListView listView = new ListView(mContext);
        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setPadding(0, Units.dpToPx(mContext, 8), 0, Units.dpToPx(mContext, 8));
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (onClickListener != null) {
                    onClickListener.onItemClick(parent, view, position, id);
                    dismiss();
                }
            }
        });
        return setCustomView(listView);
    }

    public QKDialog setPositiveButton(int resource, OnClickListener onClickListener) {
        return setPositiveButton(mResources.getString(resource), onClickListener);
    }

    public QKDialog setPositiveButton(String text, final OnClickListener onClickListener) {
        mPositiveButtonEnabled = true;
        mPositiveButtonText = text;
        mPositiveButtonClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onClick(v);
                }
                dismiss();
            }
        };
        return this;
    }

    public QKDialog setNeutralButton(int resource, OnClickListener onClickListener) {
        return setNeutralButton(mResources.getString(resource), onClickListener);
    }

    public QKDialog setNeutralButton(String text, final OnClickListener onClickListener) {
        mNeutralButtonEnabled = true;
        mNeutralButtonText = text;
        mNeutralButtonClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onClick(v);
                }
                dismiss();
            }
        };
        return this;
    }

    public QKDialog setNegativeButton(int resource, OnClickListener onClickListener) {
        return setNegativeButton(mResources.getString(resource), onClickListener);
    }

    public QKDialog setNegativeButton(String text, final OnClickListener onClickListener) {
        mNegativeButtonEnabled = true;
        mNegativeButtonText = text;
        mNegativeButtonClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onClick(v);
                }
                dismiss();
            }
        };
        return this;
    }

    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }

    private class DoubleLineListItem {
        String title;
        String body;
    }

    private class DoubleLineArrayAdapter extends ArrayAdapter<DoubleLineListItem> {

        public DoubleLineArrayAdapter(Context context, DoubleLineListItem[] items) {
            super(context, R.layout.list_item_dual, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_dual, parent, false);
            }

            ((QKTextView) convertView.findViewById(R.id.list_item_title))
                    .setText(getItem(position).title);

            ((QKTextView) convertView.findViewById(R.id.list_item_body))
                    .setText(getItem(position).body);

            return convertView;
        }
    }

    private class TripleLineListItem {
        String title;
        String subtitle;
        String body;
    }

    private class TripleLineArrayAdapter extends ArrayAdapter<TripleLineListItem> {

        public TripleLineArrayAdapter(Context context, TripleLineListItem[] items) {
            super(context, R.layout.list_item_triple, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_triple, parent, false);
            }

            QKTextView title = (QKTextView) convertView.findViewById(R.id.list_item_title);
            title.setTextColor(ThemeManager.getColor());
            title.setText(getItem(position).title);

            QKTextView subtitle = (QKTextView) convertView.findViewById(R.id.list_item_subtitle);
            subtitle.setTextColor(ThemeManager.getTextOnBackgroundPrimary());
            subtitle.setText(getItem(position).subtitle);

            ((QKTextView) convertView.findViewById(R.id.list_item_body))
                    .setText(getItem(position).body);

            return convertView;
        }
    }

    public void show() {
        try {
            super.show(mContext.getFragmentManager(), null);
        } catch (IllegalStateException ignored) {
            // Sometimes the context is destroyed, but the check for that is API 17+
        }
    }

    @Deprecated
    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
    }
}
