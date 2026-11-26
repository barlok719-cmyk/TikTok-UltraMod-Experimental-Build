package app.revanced.extension.tiktok.settings.preference.categories;

import android.content.Context;
import android.preference.PreferenceScreen;
import app.revanced.extension.tiktok.settings.Settings;
import app.revanced.extension.tiktok.settings.SettingsStatus;
import app.revanced.extension.tiktok.settings.preference.InputTextPreference;
import app.revanced.extension.tiktok.settings.preference.TogglePreference;

@SuppressWarnings("deprecation")
public class SimSpoofPreferenceCategory extends ConditionalPreferenceCategory {
    public SimSpoofPreferenceCategory(Context context, PreferenceScreen screen) {
        super(context, screen);
        setTitle("Регион и страна");
    }


    @Override
    public boolean getSettingsStatus() {
        return SettingsStatus.simSpoofEnabled;
    }

    @Override
    public void addPreferences(Context context) {
        addPreference(new TogglePreference(
                context,
                "Подменять данные SIM-карты",
                "Использовать заданные данные SIM-карты для обхода региональных ограничений.",
                Settings.SIM_SPOOF
        ));
        addPreference(new InputTextPreference(
                context,
                "Код страны (ISO)", "Например: us, gb, jp.",
                Settings.SIM_SPOOF_ISO
        ));
        addPreference(new InputTextPreference(
                context,
                "Код мобильной сети (MCC+MNC)", "Идентификатор страны и оператора мобильной сети.",
                Settings.SIMSPOOF_MCCMNC
        ));
        addPreference(new InputTextPreference(
                context,
                "Название оператора", "Название мобильного оператора.",
                Settings.SIMSPOOF_OP_NAME
        ));
    }
}
