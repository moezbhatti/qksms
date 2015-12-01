package com.moez.QKSMS.ui.dialog.mms;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.utils.Units;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.view.QKTextView;

/**
 * @author Moez Bhatti
 * @author Shane Creighton-Young
 * @since 2015-02-08
 *
 * BaseDialogFragment is a backwards-compatible, Material-styled Dialog fragment.
 *
 * To listen on results from BaseDialogFragment, launch it from a Fragment that implements
 * DialogFragmentListener. Then, the "onDialogFragmentResult" methods will getConversation called much like
 * "onActivityResult".
 */
public class QKDialogFragment extends DialogFragment {
    private final String TAG = "QKDialogFragment";

    private Context mContext;
    private Resources mResources;

    protected DialogFragmentListener mListener;

    // Result codes for this
    public static final int POSITIVE_BUTTON_RESULT = 0;
    public static final int NEUTRAL_BUTTON_RESULT = 1;
    public static final int NEGATIVE_BUTTON_RESULT = 2;
    public static final int LIST_ITEM_CLICK_RESULT = 3;
    public static final int DISMISS_RESULT = 4;

    // Views
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
    private QKTextView mPositiveButtonView;

    private boolean mNeutralButtonEnabled;
    private String mNeutralButtonText;
    private QKTextView mNeutralButtonView;

    private boolean mNegativeButtonEnabled;
    private String mNegativeButtonText;
    private QKTextView mNegativeButtonView;

    public interface DialogFragmentListener {
        // Called when the DialogFragment button is pressed, the DialogFragment is dismissed, etc.
        public void onDialogFragmentResult(int resultCode, DialogFragment fragment);

        // Called when a list item within the dialog is pressed.
        public void onDialogFragmentListResult(int resultCode, DialogFragment fragment, int index);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Restore the target fragment; this is our listener.
        mListener = (DialogFragmentListener)getTargetFragment();
    }

    /**
     * Sets the listener of the the DialogFragment.
     *
     * The generic stuff is just to ensure that the listener is both a Fragment and a
     * DialogFragmentListener.
     */
    public <T extends Fragment & DialogFragmentListener>
    QKDialogFragment setListener(T l) {
        mListener = l;
        setTargetFragment(l, 0);
        return this;
    }

    /**
     * Notify the listener of a result.
     */
    protected void onResult(int resultCode) {
        if (mListener != null) {
            mListener.onDialogFragmentResult(resultCode, this);
        }
    }

    /**
     * Notify the listener of a result relating to a list item.
     */
    protected void onListResult(int resultCode, int index) {
        if (mListener != null) {
            mListener.onDialogFragmentListResult(resultCode, this, index);
        }
    }

    // Make setTargetFragment final so that nobody subclasses BaseDialogFragment and breaks it.
    @Override
    final public void setTargetFragment(Fragment fragment, int requestCode) {
        super.setTargetFragment(fragment, requestCode);
    }

    /**
     * Builds the dialog using all the View parameters.
     */
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

        if (mPositiveButtonEnabled || mNeutralButtonEnabled || mNegativeButtonEnabled) {
            mButtonBar = (LinearLayout) view.findViewById(R.id.buttonPanel);
            mButtonBar.setVisibility(View.VISIBLE);
            mButtonBar.setOrientation(mButtonBarOrientation);
        }

        if (mPositiveButtonEnabled) {
            mPositiveButtonView = (QKTextView) view.findViewById(R.id.buttonPositive);
            mPositiveButtonView.setVisibility(View.VISIBLE);
            mPositiveButtonView.setText(mPositiveButtonText);
            mPositiveButtonView.setTextColor(ThemeManager.getColor());
            mPositiveButtonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onResult(POSITIVE_BUTTON_RESULT);
                }
            });
        }

        if (mNeutralButtonEnabled) {
            mNeutralButtonView = (QKTextView) view.findViewById(R.id.buttonNeutral);
            mNeutralButtonView.setVisibility(View.VISIBLE);
            mNeutralButtonView.setText(mNeutralButtonText);
            mNeutralButtonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onResult(NEUTRAL_BUTTON_RESULT);
                }
            });
        }

        if (mNegativeButtonEnabled) {
            mNegativeButtonView = (QKTextView) view.findViewById(R.id.buttonNegative);
            mNegativeButtonView.setVisibility(View.VISIBLE);
            mNegativeButtonView.setText(mNegativeButtonText);
            mNegativeButtonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onResult(NEGATIVE_BUTTON_RESULT);
                }
            });
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

    /**
     * Called when the dialog is cancelled by the user (i.e. the user clicks outside of it)
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        onResult(DISMISS_RESULT);
    }

    public QKDialogFragment setContext(Context context) {
        mContext = context;
        mResources = context.getResources();
        return this;
    }

    public QKDialogFragment setTitle(int resource) {
        return setTitle(mResources.getString(resource));
    }

    public QKDialogFragment setTitle(String title) {
        mTitleEnabled = true;
        mTitleText = title;
        return this;
    }

    public QKDialogFragment setMessage(int resource) {
        return setMessage(mResources.getString(resource));
    }

    public QKDialogFragment setMessage(String message) {
        mMessageEnabled = true;
        mMessageText = message;
        return this;
    }

    public QKDialogFragment setCancelOnTouchOutside(boolean cancelable) {
        setCancelable(cancelable);
        return this;
    }

    // TODO fix stack from bottom issue
    public QKDialogFragment setButtonBarOrientation(int orientation) {
        mButtonBarOrientation = orientation;
        return this;
    }

    public QKDialogFragment setCustomView(View view) {
        mCustomViewEnabled = true;
        mCustomView = view;
        return this;
    }

    public QKDialogFragment setItems(int resource, final int resultCode) {
        return setItems(mResources.getStringArray(resource));
    }

    public QKDialogFragment setItems(String[] items) {
        ArrayAdapter adapter = new ArrayAdapter<>(mContext, R.layout.list_item_simple, items);
        ListView listView = new ListView(mContext);
        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setPadding(0, Units.dpToPx(mContext, 8), 0, Units.dpToPx(mContext, 8));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListResult(LIST_ITEM_CLICK_RESULT, position);
                dismiss();
            }
        });
        return setCustomView(listView);
    }

    public QKDialogFragment setDoubleLineItems(int titles, int bodies) {
        return setDoubleLineItems(
                mResources.getStringArray(titles), mResources.getStringArray(bodies)
        );
    }

    public QKDialogFragment setDoubleLineItems(String[] titles, String[] bodies) {

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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListResult(LIST_ITEM_CLICK_RESULT, position);
                dismiss();
            }
        });
        return setCustomView(listView);
    }

    public QKDialogFragment setTripleLineItems(int titles, int subtitles, int bodies) {
        return setTripleLineItems(
                mResources.getStringArray(titles), mResources.getStringArray(subtitles),
                mResources.getStringArray(bodies)
        );
    }

    public QKDialogFragment setTripleLineItems(String[] titles, String[] subtitles,
                                                 String[] bodies) {

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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListResult(LIST_ITEM_CLICK_RESULT, position);
                dismiss();
            }
        });
        return setCustomView(listView);
    }

    public QKDialogFragment setPositiveButton(int resource) {
        return setPositiveButton(mResources.getString(resource));
    }

    public QKDialogFragment setPositiveButton(String text) {
        mPositiveButtonEnabled = true;
        mPositiveButtonText = text;
        return this;
    }

    public QKDialogFragment setNeutralButton(int resource) {
        return setNeutralButton(mResources.getString(resource));
    }

    public QKDialogFragment setNeutralButton(String text) {
        mNeutralButtonEnabled = true;
        mNeutralButtonText = text;
        return this;
    }

    public QKDialogFragment setNegativeButton(int resource) {
        return setNegativeButton(mResources.getString(resource));
    }

    public QKDialogFragment setNegativeButton(String text) {
        mNegativeButtonEnabled = true;
        mNegativeButtonText = text;
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
}
