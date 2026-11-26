package app.revanced.extension.tiktok.settings.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.Preference;
import android.view.View;

import app.revanced.extension.tiktok.Utils;

/**
 * About entry for the TikTok UltraMOD settings menu.
 */
@SuppressWarnings("deprecation")
public class ReVancedTikTokAboutPreference extends Preference {

    public static final String ULTRAMOD_VERSION = "1.0";

    public ReVancedTikTokAboutPreference(Context context) {
        super(context);

        setTitle("О TikTok UltraMod");
        setSummary("TikTok UltraMod · версия " + ULTRAMOD_VERSION);
    }

    @Override
    protected void onClick() {
        new AlertDialog.Builder(getContext())
                .setTitle("TikTok UltraMOD")
                .setMessage("TikTok UltraMod — модифицированная версия TikTok с дополнительными возможностями и настройками."
                        + "\n\nВерсия UltraMod: " + ULTRAMOD_VERSION
                        + "\n\nTelegram: @ultratiktok")
                .setPositiveButton("Закрыть", null)
                .show();
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        Utils.setTitleAndSummaryColor(view);
    }
}
