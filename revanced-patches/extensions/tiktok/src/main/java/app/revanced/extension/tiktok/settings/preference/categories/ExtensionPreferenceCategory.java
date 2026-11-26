package app.revanced.extension.tiktok.settings.preference.categories;

import android.content.Context;
import android.preference.PreferenceScreen;

import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.tiktok.settings.preference.ReVancedTikTokAboutPreference;
import app.revanced.extension.tiktok.settings.preference.UltraMODTelegramPreference;
import app.revanced.extension.tiktok.settings.preference.TogglePreference;

@SuppressWarnings("deprecation")
public class ExtensionPreferenceCategory extends ConditionalPreferenceCategory {
    public ExtensionPreferenceCategory(Context context, PreferenceScreen screen) {
        super(context, screen);
        setTitle("Дополнительно");
    }

    @Override
    public boolean getSettingsStatus() {
        return true;
    }

    @Override
    public void addPreferences(Context context) {
        addPreference(new ReVancedTikTokAboutPreference(context));
        addPreference(new UltraMODTelegramPreference(context));

        addPreference(new TogglePreference(context,
                "Очищать ссылки при отправке",
                "Удалять параметры отслеживания из ссылок, которыми вы делитесь.",
                BaseSettings.SANITIZE_SHARING_LINKS
        ));

        addPreference(new TogglePreference(context,
                "Включить журнал отладки",
                "Показывать технический журнал расширения.",
                BaseSettings.DEBUG
        ));
    }
}
