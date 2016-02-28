package com.moez.QKSMS.enums;

import com.moez.QKSMS.ui.ThemeManager;

import java.util.Arrays;
import java.util.HashSet;

public enum QKPreference {
    // Appearance
    THEME("pref_key_theme", ThemeManager.DEFAULT_COLOR),
    ICON("pref_key_icon"),
    BACKGROUND("pref_key_background", "offwhite"),

    BUBBLES("pref_key_bubbles"),
    BUBBLES_NEW("pref_key_new_bubbles", true),
    BUBBLES_COLOR_SENT("pref_key_colour_sent", false),
    BUBBLES_COLOR_RECEIVED("pref_key_colour_received", true),

    AUTO_NIGHT("pref_key_night_auto", false),
    AUTO_NIGHT_DAY_START("pref_key_day_start", "6:00"),
    AUTO_NIGHT_NIGHT_START("pref_key_night_start", "21:00"),

    TINTED_STATUS("pref_key_status_tint", true),
    TINTED_NAV("pref_key_navigation_tint", false),

    FONT_FAMILY("pref_key_font_family", "0"),
    FONT_SIZE("pref_key_font_size", "1"),
    FONT_WEIGHT("pref_key_font_weight", "0"),

    AVATAR_CONVERSATIONS("pref_key_avatar_conversations", true),
    AVATAR_SENT("pref_key_avatar_sent", false),
    AVATAR_RECEIVED("pref_key_avatar_received", true),

    MESSAGE_COUNT("pref_key_message_count", false),
    SLIDING_TAB("pref_key_sliding_tab", false),
    TIMESTAMPS_24H("pref_key_24h", false),

    // General
    DELAYED_MESSAGING("pref_key_delayed", false),
    DELAYED_DURATION("pref_key_delay_duration", 3),

    DELIVERY_CONFIRMATIONS("pref_key_delivery", false),
    DELIVERY_TOAST("pref_key_delivery_toast", true),
    DELIVERY_VIBRATE("pref_key_delivery_vibrate", true),

    AUTO_EMOJI("pref_key_auto_emoji", false),
    TEXT_FORMATTING("pref_key_markdown_enabled", false),
    STRIP_UNICODE("pref_key_strip_unicode", false),
    SPLIT_SMS("pref_key_split", false),
    SPLIT_COUNTER("pref_key_split_counter", true),

    BLOCKED_CONVERSATIONS("pref_key_blocked_enabled", false),
    BLOCKED_SENDERS("pref_key_blocked_senders", new HashSet<String>()),
    BLOCKED_FUTURE("pref_key_block_future", new HashSet<String>()),
    MOBILE_ONLY("pref_key_mobile_only", false),
    ENTER_BUTTON("pref_key_enter_button", "0"),
    SENT_TIMESTAMPS("pref_key_sent_timestamps", false),
    STARRED_CONTACTS("pref_key_compose_favorites", true),
    PROXIMITY_SENSOR("pref_key_prox_sensor_calling", false),
    YAPPY_INTEGRATION("pref_key_endlessjabber", false),
    QK_RESPONSES("pref_key_qk_responses", new HashSet<>(Arrays.asList(new String[]{
            "Okay", "Give me a moment", "On my way", "Thanks", "Sounds good", "What's up?", "Agreed", "No",
            "Love you", "Sorry", "LOL", "That's okay"}))),

    // Notifications
    NOTIFICATIONS("pref_key_notifications", true),
    NOTIFICATIONS_LED("pref_key_led", true),
    NOTIFICATIONS_LED_COLOR("pref_key_theme_led", "-48060"),
    NOTIFICATIONS_WAKE("pref_key_wake", false),
    NOTIFICATIONS_TICKER("pref_key_ticker", false),
    NOTIFICATIONS_PRIVATE("pref_key_notification_private", false),
    NOTIFICATIONS_VIBRATION("pref_key_vibration", true),
    NOTIFICATIONS_SOUND("pref_key_ringtone", "content://settings/system/notification_sound"),
    NOTIFICATIONS_CALL_BUTTON("pref_key_notification_call", false),
    NOTIFICATIONS_MARK_READ("pref_key_dismiss_read", false),

    // MMS
    GROUP_MESSAGING("pref_key_compose_group", true),
    AUTOMATIC_DATA("pref_key_auto_data", true),
    LONG_AS_MMS("", true),
    LONG_AS_MMS_AFTER("", true),
    MAX_MMS_SIZE("", true),
    AUTO_CONFIGURE_MMS("", true),
    MMSC("mmsc_url", true),
    MMS_PORT("mms_port", true),
    MMS_PROXY("mms_proxy", true),

    // QK Reply
    QK_REPLY("pref_key_quickreply_enabled", true),
    TAP_DISMISS("pref_key_quickreply_dismiss", true),

    // QK Compose
    QK_COMPOSE("pref_key_quickcompose", false),

    // LiveViews
    CONVERSATION_THEME("conversation_theme"),

    // Storage
    COMPOSE_DRAFT("compose_draft", "");

    private String mKey;
    private Object mDefaultValue;

    QKPreference(String key) {
        mKey = key;
    }

    QKPreference(String key, Object defaultValue) {
        mKey = key;
        mDefaultValue = defaultValue;
    }

    public String getKey() {
        return mKey;
    }

    public Object getDefaultValue() {
        return mDefaultValue;
    }
}
