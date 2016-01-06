package com.moez.QKSMS.common.utils;

import android.text.Html;
import android.text.SpannedString;

import java.util.regex.Pattern;

public class TextUtils {
    public static CharSequence styleText(CharSequence text) {
        if (text == null || text.toString().isEmpty() || (!text.toString().contains("*") && !text.toString().contains("_")))
            return text; // Do nothing if there's nothing to be styled

        boolean bool;

        text = Html.toHtml(new SpannedString(text));

        // bold text
        if (text.toString().contains("*")) {
            int doubleStars = 0;
            bool = true;
            for (int i = 0; i < text.length() - 1; i++) {
                if (text.subSequence(i, i + 2).equals("**")) {
                    doubleStars++;
                }
            }
            if (doubleStars >= 2) {
                if (doubleStars % 2 != 0) {
                    doubleStars--;
                }
                for (int i = 0; i < doubleStars; i++) {
                    text = text.toString().replaceFirst(Pattern.quote("**"), bool ? "<b>" : "</b>");
                    bool = !bool;
                }
            }
        }

        // italic text
        if (text.toString().contains("*")) {
            int singleStars = 0;
            bool = true;
            for (int i = 0; i < text.length(); i++) {
                if (text.subSequence(i, i + 1).equals("*")) {
                    singleStars++;
                }
            }
            if (singleStars >= 2) {
                if (singleStars % 2 != 0) {
                    singleStars--;
                }
                for (int i = 0; i < singleStars; i++) {
                    text = text.toString().replaceFirst(Pattern.quote("*"), bool ? "<i>" : "</i>");
                    bool = !bool;
                }
            }
        }

        // underlined text
        if (text.toString().contains("_")) {
            int underscores = 0;
            bool = true;
            for (int i = 0; i < text.length(); i++) {
                if (text.subSequence(i, i + 1).equals("_")) {
                    underscores++;
                }
            }
            if (underscores >= 2) {
                if (underscores % 2 != 0) {
                    underscores--;
                }
                for (int i = 0; i < underscores; i++) {
                    text = text.toString().replaceFirst(Pattern.quote("_"), bool ? "<u>" : "</u>");
                    bool = !bool;
                }
            }
        }

        text = text.toString().replaceAll(Pattern.quote("<p dir=\"ltr\">"), "").replaceAll(Pattern.quote("</p>"), "");

        return Html.fromHtml(text.toString());
    }
}
