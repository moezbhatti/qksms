package com.moez.QKSMS.enums;

import android.os.Build;
import android.support.annotation.NonNull;
import com.moez.QKSMS.common.ThemeManager;

import java.util.HashSet;

public enum QKPreference {
    // Appearance
    THEME("pref_key_theme", String.valueOf(ThemeManager.DEFAULT_COLOR)),
    ICON("pref_key_icon"),
    BACKGROUND("pref_key_background", "offwhite"),

    BUBBLES("pref_key_bubbles"),
    BUBBLES_NEW("pref_key_new_bubbles", true),
    BUBBLES_COLOR_SENT("pref_key_colour_sent", true),
    BUBBLES_COLOR_RECEIVED("pref_key_colour_received", false),

    AUTO_NIGHT("pref_key_night_auto", false),
    AUTO_NIGHT_DAY_START("pref_key_day_start", "6:00"),
    AUTO_NIGHT_NIGHT_START("pref_key_night_start", "21:00"),

    TINTED_STATUS("pref_key_status_tint", true),
    TINTED_NAV("pref_key_navigation_tint", false),

    FONT_FAMILY("pref_key_font_family", "0"),
    FONT_SIZE("pref_key_font_size", "1"),
    FONT_WEIGHT("pref_key_font_weight", "0"),

    HIDE_AVATAR_CONVERSATIONS("pref_key_hide_avatar_conversations", false),
    HIDE_AVATAR_SENT("pref_key_hide_avatar_sent", true),
    HIDE_AVATAR_RECEIVED("pref_key_hide_avatar_received", false),

    MESSAGE_COUNT("pref_key_message_count", false),

    // General
    DELAYED_MESSAGING("pref_key_delayed", false),
    DELAYED_DURATION("pref_key_delay_duration", 3),

    DELIVERY_CONFIRMATIONS("pref_key_delivery", false),
    DELIVERY_TOAST("pref_key_delivery_toast", true),
    DELIVERY_VIBRATE("pref_key_delivery_vibrate", true),

    AUTO_DELETE("pref_key_delete_old_messages", false),
    AUTO_DELETE_UNREAD("pref_key_delete_old_unread_messages", "7"), // This type of preference only accepts strings
    AUTO_DELETE_READ("pref_key_delete_old_read_messages", "7"),

    AUTO_EMOJI("pref_key_auto_emoji", false),
    TEXT_FORMATTING("pref_key_markdown_enabled", false),
    STRIP_UNICODE("pref_key_strip_unicode", false),
    SPLIT_SMS("pref_key_split", false),
    SPLIT_COUNTER("pref_key_split_counter", true),
    SIGNATURE("pref_key_signature", ""),

    BLOCKED_CONVERSATIONS("pref_key_blocked_enabled", false),
    BLOCKED_SENDERS("pref_key_blocked_senders", new HashSet<String>()),
    BLOCKED_FUTURE("pref_key_block_future", new HashSet<String>()),
    SHOULD_I_ANSWER("pref_key_should_i_answer", false),
    MOBILE_ONLY("pref_key_mobile_only", false),
    ENTER_BUTTON("pref_key_enter_button", "0"),
    SENT_TIMESTAMPS("pref_key_sent_timestamps", false),
    NEW_TIMESTAMP_DELAY("pref_key_timestamp_delay", "5"),
    FORCE_TIMESTAMPS("pref_key_force_timestamps", false),
    STARRED_CONTACTS("pref_key_compose_favorites", true),
    PROXIMITY_SENSOR("pref_key_prox_sensor_calling", false),
    YAPPY_INTEGRATION("pref_key_endlessjabber", false),
    QK_RESPONSES("pref_key_qk_responses", new HashSet<>()),

    // Notifications
    NOTIFICATIONS("pref_key_notifications", true),
    NOTIFICATIONS_LED("pref_key_led", true),
    NOTIFICATIONS_LED_COLOR("pref_key_theme_led", "-48060"),
    NOTIFICATIONS_WAKE("pref_key_wake", false),
    NOTIFICATIONS_TICKER("pref_key_ticker", true),
    NOTIFICATIONS_PRIVATE("pref_key_notification_private", "0"),
    NOTIFICATIONS_VIBRATION("pref_key_vibration", true),
    NOTIFICATIONS_SOUND("pref_key_ringtone", "content://settings/system/notification_sound"),
    NOTIFICATIONS_CALL_BUTTON("pref_key_notification_call", false),
    NOTIFICATIONS_MARK_READ("pref_key_dismiss_read", false),

    // MMS
    GROUP_MESSAGING("pref_key_compose_group", true),
    LONG_AS_MMS("pref_key_long_as_mms", false),
    LONG_AS_MMS_AFTER("pref_key_long_as_mms_after", "3"),
    MAX_MMS_SIZE("pref_mms_max_attachment_size", "300kb"),
    AUTO_CONFIGURE_MMS("pref_key_automatically_configure_mms", true),
    MMSC("mmsc_url", ""),
    MMS_PORT("mms_port", ""),
    MMS_PROXY("mms_proxy", ""),
    MMS_AGENT("mms_agent", ""),
    MMS_AGENT_PROFILE("mms_user_agent_profile_url", ""),
    MMS_AGENT_NAME("mms_user_agent_tag_name", ""),

    // QK Reply
    QK_REPLY("pref_key_quickreply_enabled", Build.VERSION.SDK_INT < 24),
    TAP_DISMISS("pref_key_quickreply_dismiss", true),

    // QK Compose
    QK_COMPOSE("pref_key_quickcompose", false),

    // LiveViews
    CONVERSATION_THEME("conversation_theme"),

    // Setting views
    MMS_CONTACT_SUPPORT("pref_key_mms_contact_support"),
    CHANGELOG("pref_key_changelog"),
    VERSION("pref_key_version"),
    THANKS("pref_key_thanks"),
    DONATE("pref_key_donate"),
    GOOGLE_PLUS("pref_key_google_plus"),
    GITHUB("pref_key_github"),
    CROWDIN("pref_key_crowdin"),

    // Storage
    LAST_AUTO_DELETE_CHECK("last_auto_delete_check", 0),
    WELCOME_SEEN("pref_key_welcome_seen", false),

    // Used if `get(String)` can't find an enum, instead of passing null, which would break switches
    NULL("");

    private String mKey;
    private Object mDefaultValue;

    QKPreference(String key) {
        mKey = key;
    }

    QKPreference(String key, Object defaultValue) {
        mKey = key;
        mDefaultValue = defaultValue;
    }

    @NonNull
    public static QKPreference get(String key) {
        for (QKPreference preference : QKPreference.values()) {
            if (preference.mKey.equals(key)) {
                return preference;
            }
        }
        return NULL;
    }

    public String getKey() {
        return mKey;
    }

    public Object getDefaultValue() {
        return mDefaultValue;
    }
}
