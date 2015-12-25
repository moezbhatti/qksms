package com.moez.QKSMS.common.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.text.util.Linkify.TransformFilter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.regex.Pattern;

/**
 * Shamelessly stolen from
 * <a href="https://raw.githubusercontent.com/guardianproject/ChatSecureAndroid/master/src/info/guardianproject/util/LinkifyHelper.java">ChatSecure's implementation</a>
 */
public class LinkifyUtils {

    private static Pattern bitcoin = Pattern.compile("bitcoin:[1-9a-km-zA-HJ-NP-Z]{27,34}(\\?[a-zA-Z0-9$\\-_.+!*'(),%:;@&=]*)?");
    private static Pattern geo = Pattern.compile("geo:[-0-9.]+,[-0-9.]+[^ \t\n\"\':]*");
    private static Pattern market = Pattern.compile("market://[^ \t\n\"\':,<>]+");
    private static Pattern openpgp4fpr = Pattern.compile("openpgp4fpr:[A-Za-z0-9]{8,40}");
    private static Pattern xmpp = Pattern.compile("xmpp:[^ \t\n\"\':,<>]+");
    private static Pattern twitterHandle = Pattern.compile("@([A-Za-z0-9_-]+)");
    private static Pattern hashtag = Pattern.compile("#([A-Za-z0-9_-]+)");

    static TransformFilter returnMatchFilter = (match, url) -> match.group(1);

    /* Right now, if there is no app to handle */
    public static void addLinks(TextView text) {
        Linkify.addLinks(text, Linkify.ALL);
        Linkify.addLinks(text, geo, null);
        Linkify.addLinks(text, market, null);
        Linkify.addLinks(text, openpgp4fpr, null);
        Linkify.addLinks(text, bitcoin, null);
        Linkify.addLinks(text, xmpp, null);
        Linkify.addLinks(text, twitterHandle, "https://twitter.com/", null, returnMatchFilter);
        Linkify.addLinks(text, hashtag, "https://twitter.com/hashtag/", null, returnMatchFilter);
        text.setText(replaceAll(text.getText(), URLSpan.class, new URLSpanConverter()));
    }

    /**
     * Do not create this static utility class.
     */
    private LinkifyUtils() {
    }

    // thanks to @commonsware https://stackoverflow.com/a/11417498
    public static <A extends CharacterStyle, B extends CharacterStyle> Spannable replaceAll(
            CharSequence original, Class<A> sourceType, SpanConverter<A, B> converter) {
        SpannableString result = new SpannableString(original);
        A[] spans = result.getSpans(0, result.length(), sourceType);

        for (A span : spans) {
            int start = result.getSpanStart(span);
            int end = result.getSpanEnd(span);
            int flags = result.getSpanFlags(span);

            result.removeSpan(span);
            result.setSpan(converter.convert(span), start, end, flags);
        }

        return (result);
    }

    private interface SpanConverter<A extends CharacterStyle, B extends CharacterStyle> {
        B convert(A span);
    }

    /**
     * This trickery is needed in order to have clickable links that open things
     * in a new {@code Task} rather than in ChatSecure's {@code Task.} Thanks to @commonsware
     * https://stackoverflow.com/a/11417498
     */
    private static class NewTaskUrlSpan extends ClickableSpan {
        private String urlString;

        NewTaskUrlSpan(String urlString) {
            this.urlString = urlString;
        }

        @Override
        public void onClick(View widget) {
            try {
                Uri uri = Uri.parse(urlString);
                Context context = widget.getContext();
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (ActivityNotFoundException ignored) {
                Log.e("LinkifyUtils", "", ignored); // We should (or shouldn't) explain that no app can handle this intent ?
            }
        }
    }

    private static class URLSpanConverter implements LinkifyUtils.SpanConverter<URLSpan, ClickableSpan> {
        @Override
        public NewTaskUrlSpan convert(URLSpan span) {
            return (new NewTaskUrlSpan(span.getURL()));
        }
    }
}