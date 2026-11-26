package app.revanced.extension.tiktok.settings.preference.categories;

import android.content.Context;
import android.preference.PreferenceScreen;
import app.revanced.extension.tiktok.settings.Settings;
import app.revanced.extension.tiktok.settings.SettingsStatus;
import app.revanced.extension.tiktok.settings.preference.DownloadPathPreference;
import app.revanced.extension.tiktok.settings.preference.TogglePreference;

@SuppressWarnings("deprecation")
public class DownloadsPreferenceCategory extends ConditionalPreferenceCategory {
    public DownloadsPreferenceCategory(Context context, PreferenceScreen screen) {
        super(context, screen);
        setTitle("Загрузки");
    }

    @Override
    public boolean getSettingsStatus() {
        return SettingsStatus.downloadEnabled;
    }

    @Override
    public void addPreferences(Context context) {
        addPreference(new DownloadPathPreference(
                context,
                "Папка загрузок",
                Settings.DOWNLOAD_PATH
        ));
        addPreference(new TogglePreference(
                context,
                "Удалять водяной знак", "Убирать водяной знак при сохранении видео.",
                Settings.DOWNLOAD_WATERMARK
        ));
    }
}
