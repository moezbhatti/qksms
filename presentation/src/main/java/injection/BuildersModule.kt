package injection

import dagger.Module
import dagger.android.ContributesAndroidInjector
import feature.blocked.BlockedActivity
import feature.blocked.BlockedActivityModule
import feature.compose.ComposeActivity
import feature.compose.ComposeActivityModule
import feature.conversationinfo.ConversationInfoActivity
import feature.conversationinfo.ConversationInfoActivityModule
import feature.gallery.GalleryActivity
import feature.gallery.GalleryActivityModule
import feature.main.MainActivity
import feature.main.MainActivityModule
import feature.notificationprefs.NotificationPrefsActivity
import feature.notificationprefs.NotificationPrefsActivityModule
import feature.plus.PlusActivity
import feature.plus.PlusActivityModule
import feature.qkreply.QkReplyActivity
import feature.qkreply.QkReplyActivityModule
import feature.settings.SettingsActivity
import feature.settings.SettingsActivityModule
import feature.settings.about.AboutActivity
import feature.settings.about.AboutActivityModule
import feature.themepicker.ThemePickerActivity
import feature.themepicker.ThemePickerActivityModule

@Module
abstract class BuildersModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun bindMainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [PlusActivityModule::class])
    abstract fun bindPlusActivity(): PlusActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [AboutActivityModule::class])
    abstract fun bindAboutActivity(): AboutActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ComposeActivityModule::class])
    abstract fun bindComposeActivity(): ComposeActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ConversationInfoActivityModule::class])
    abstract fun bindConversationInfoActivity(): ConversationInfoActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [GalleryActivityModule::class])
    abstract fun bindGalleryActivity(): GalleryActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [NotificationPrefsActivityModule::class])
    abstract fun bindNotificationPrefsActivity(): NotificationPrefsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [QkReplyActivityModule::class])
    abstract fun bindQkReplyActivity(): QkReplyActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [SettingsActivityModule::class])
    abstract fun bindSettingsActivity(): SettingsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [BlockedActivityModule::class])
    abstract fun bindBlockedActivity(): BlockedActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ThemePickerActivityModule::class])
    abstract fun bindThemePickerActivity(): ThemePickerActivity

}