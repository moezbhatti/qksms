package com.moez.QKSMS.ui.view.colorpicker;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.moez.QKSMS.R;

public class ColorPickerSwatch extends FrameLayout implements View.OnClickListener {
    private ImageView mCheckmarkImage;
    private int mColor;
    private OnColorSelectedListener mOnColorSelectedListener;
    private ImageView mSwatchImage;

    public ColorPickerSwatch(Context paramContext, int color, boolean checked, OnColorSelectedListener onColorSelectedListener) {
        super(paramContext);
        mColor = color;
        mOnColorSelectedListener = onColorSelectedListener;
        LayoutInflater.from(paramContext).inflate(R.layout.view_color_picker_swatch, this);
        mSwatchImage = ((ImageView) findViewById(R.id.color_picker_swatch));
        mCheckmarkImage = ((ImageView) findViewById(R.id.color_picker_checkmark));
        setColor(color);
        setChecked(checked);
        setOnClickListener(this);
    }

    private void setChecked(boolean checked) {
        if (checked) {
            mCheckmarkImage.setVisibility(View.VISIBLE);
            return;
        }

        mCheckmarkImage.setVisibility(View.GONE);
    }

    public void onClick(View view) {
        if (mOnColorSelectedListener != null) {
            mOnColorSelectedListener.onColorSelected(mColor);
        }
    }

    protected void setColor(int color) {
        Drawable[] drawables = new Drawable[1];
        drawables[0] = ContextCompat.getDrawable(getContext(), R.drawable.color_picker_swatch);
        mSwatchImage.setImageDrawable(new ColorStateDrawable(drawables, color));
    }

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }
}
