package com.moez.QKSMS.common.emoji;

import android.text.TextUtils;

import com.vdurmont.emoji.EmojiParser;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class EmojiRegistry {
    public static Map<String, String> EMOJIS_MAP = new HashMap<>();

    static {
        EMOJIS_MAP.put(":)", Emojis.SMILEY);
        EMOJIS_MAP.put(":(", Emojis.FROWNING);
        EMOJIS_MAP.put(":'(", Emojis.CRY);
        EMOJIS_MAP.put(":D", Emojis.SMILE);
        EMOJIS_MAP.put(":P", Emojis.STUCK_OUT_TONGUE_WINKING_EYE);
        EMOJIS_MAP.put(":O", Emojis.OPEN_MOUTH);
        EMOJIS_MAP.put(";)", Emojis.WINK);
        EMOJIS_MAP.put("<3", Emojis.HEART);
        EMOJIS_MAP.put(":/", Emojis.CONFUSED);
        EMOJIS_MAP.put(":*", Emojis.KISSING_SMILING_EYES);
        EMOJIS_MAP.put(">:(", Emojis.ANGRY);
        EMOJIS_MAP.put(":poop:", Emojis.HANKEY);
    }

    /**
     * Replaces emoji codes within the text with unicode emojis.
     *
     * Note that an emoji code must be surrounded on both sides by one of:
     *
     * - whitespace (i.e. ' ' or '\t')
     * - punctuation (one of [.,;:"'?!])
     * - the beginning or end of the string
     */
    public static String parseEmojis(String body) {
        if (TextUtils.isEmpty(body)) {
            return body;
        }

        // whitespace and punctuation characters
        String ACCEPTED_CHARS = "[\\s.,?:;'\"!]";

        // explanation: we want to match:
        // 1) either an accepted char or the beginning of the text (i.e. ^)
        // 2) the actual emoji code (to be inserted in the loop)
        // 3) either an accepted char or the end of the text (i.e. $)
        String REGEX_TEMPLATE = String.format("(^|%s)%s(%s|$)", ACCEPTED_CHARS, "%s", ACCEPTED_CHARS);

        // iterate over all the entries
        for (Map.Entry<String, String> entry : EmojiRegistry.EMOJIS_MAP.entrySet()) {
            // quote the emoji code because some characters like ) are protected in regex land
            String quoted = Pattern.quote(entry.getKey());
            String regex = String.format(REGEX_TEMPLATE, quoted);

            // the $1 and $2 represent the character* that came before the emoji and the
            // character* that came after the emoji
            // * - or beginning / end of text
            body = body.replaceAll(regex, "$1:" + entry.getValue() + ":$2");
        }

        return EmojiParser.parseToUnicode(body);
    }
}
