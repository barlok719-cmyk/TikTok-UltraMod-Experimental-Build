package app.revanced.extension.tiktok.settings.preference.categories;

import android.content.Context;
import android.preference.PreferenceScreen;
import app.revanced.extension.tiktok.settings.Settings;
import app.revanced.extension.tiktok.settings.SettingsStatus;
import app.revanced.extension.tiktok.settings.preference.TogglePreference;

@SuppressWarnings("deprecation")
public class FeedFilterPreferenceCategory extends ConditionalPreferenceCategory {
    public FeedFilterPreferenceCategory(Context context, PreferenceScreen screen) {
        super(context, screen);
        setTitle("Лента и фильтрация");
    }

    @Override
    public boolean getSettingsStatus() {
        return SettingsStatus.feedFilterEnabled;
    }

    @Override
    public void addPreferences(Context context) {
        addPreference(new TogglePreference(
                context,
                "Убирать рекламу из ленты", "Скрывать рекламные публикации в ленте.",
                Settings.REMOVE_ADS
        ));
        addPreference(new TogglePreference(
                context,
                "Автоматически пропускать рекламу", "Если реклама всё же появилась, автоматически пролистывать её. При отключении лента не будет сама переключаться на следующий ролик.",
                Settings.SKIP_ADS_AT_RENDER
        ));
        addPreference(new TogglePreference(
                context,
                "Скрывать TikTok Shop", "Убирать публикации и элементы TikTok Shop из ленты.",
                Settings.HIDE_SHOP
        ));
        addPreference(new TogglePreference(
                context,
                "Скрывать прямые эфиры", "Не показывать прямые эфиры в ленте.",
                Settings.HIDE_LIVE
        ));
        addPreference(new TogglePreference(
                context,
                "Скрывать истории", "Не показывать истории в ленте.",
                Settings.HIDE_STORY
        ));
        addPreference(new TogglePreference(
                context,
                "Скрывать публикации с фото", "Не показывать публикации с изображениями в ленте.",
                Settings.HIDE_IMAGE
        ));
    }
}
