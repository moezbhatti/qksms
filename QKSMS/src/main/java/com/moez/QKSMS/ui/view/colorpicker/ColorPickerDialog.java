package com.moez.QKSMS.ui.view.colorpicker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import com.moez.QKSMS.R;

public class ColorPickerDialog extends DialogFragment implements ColorPickerSwatch.OnColorSelectedListener {
    protected AlertDialog mAlertDialog;
    protected int[] mColors = null;
    protected int mColumns;
    protected ColorPickerSwatch.OnColorSelectedListener mListener;
    private ColorPickerPalette mPalette;
    private ProgressBar mProgress;
    protected int mSelectedColor;
    protected int mSize;
    protected int mTitleResId = R.string.color_picker_default_title;

    private void refreshPalette() {
        if (mPalette != null && mColors != null) {
            mPalette.drawPalette(mColors, mSelectedColor);
        }
    }

    public void initialize(int titleId, int[] colors, int selectedColor, int columns, int size) {
        setArguments(titleId, columns, size);
        setColors(colors, selectedColor);
    }

    public void onColorSelected(int selectedColor) {
        if (mListener != null) {
            mListener.onColorSelected(selectedColor);
        }

        if ((getTargetFragment() instanceof ColorPickerSwatch.OnColorSelectedListener)) {
            ((ColorPickerSwatch.OnColorSelectedListener) getTargetFragment()).onColorSelected(selectedColor);
        }

        if (selectedColor != mSelectedColor) {
            mSelectedColor = selectedColor;
            mPalette.drawPalette(mColors, mSelectedColor);
        }

        dismiss();
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getArguments() != null) {
            mTitleResId = getArguments().getInt("title_id");
            mColumns = getArguments().getInt("columns");
            mSize = getArguments().getInt("size");
        }

        if (bundle != null) {
            mColors = bundle.getIntArray("colors");
            mSelectedColor = ((Integer) bundle.getSerializable("selected_color")).intValue();
        }
    }

    public Dialog onCreateDialog(Bundle bundle) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_color_picker, null);
        mProgress = ((ProgressBar) view.findViewById(android.R.id.progress));
        mPalette = ((ColorPickerPalette) view.findViewById(R.id.color_picker));
        mPalette.init(mSize, mColumns, this);

        if (mColors != null) {
            showPaletteView();
        }

        mAlertDialog = new AlertDialog.Builder(getActivity()).setTitle(mTitleResId).setView(view).create();
        return mAlertDialog;
    }

    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putIntArray("colors", mColors);
        bundle.putSerializable("selected_color", Integer.valueOf(mSelectedColor));
    }

    public void setArguments(int titleId, int columns, int size) {
        Bundle bundle = new Bundle();
        bundle.putInt("title_id", titleId);
        bundle.putInt("columns", columns);
        bundle.putInt("size", size);
        setArguments(bundle);
    }

    public void setColors(int[] colors, int selected) {
        if (mColors != colors || mSelectedColor != selected) {
            mColors = colors;
            mSelectedColor = selected;
            refreshPalette();
        }
    }

    public void setOnColorSelectedListener(ColorPickerSwatch.OnColorSelectedListener onColorSelectedListener) {
        mListener = onColorSelectedListener;
    }

    public void showPaletteView() {
        if (mProgress != null && mPalette != null) {
            mProgress.setVisibility(View.GONE);
            refreshPalette();
            mPalette.setVisibility(View.VISIBLE);
        }
    }

    public void showProgressBarView() {
        if (mProgress != null && mPalette != null) {
            mProgress.setVisibility(View.VISIBLE);
            mPalette.setVisibility(View.GONE);
        }
    }
}
