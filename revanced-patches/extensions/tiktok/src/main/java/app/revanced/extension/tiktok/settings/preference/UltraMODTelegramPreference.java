package app.revanced.extension.tiktok.settings.preference;

import android.content.Context;
import android.preference.Preference;
import android.view.View;

import app.revanced.extension.tiktok.Utils;

/**
 * Opens the official TikTok UltraMOD Telegram channel.
 */
@SuppressWarnings("deprecation")
public class UltraMODTelegramPreference extends Preference {

    private static final String TELEGRAM_URL = "https://t.me/ultratiktok";

    public UltraMODTelegramPreference(Context context) {
        super(context);
        setTitle("Telegram UltraMOD");
        setSummary("@ultratiktok · Новости и обновления");
    }

    @Override
    protected void onClick() {
        app.revanced.extension.shared.Utils.openLink(TELEGRAM_URL);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        Utils.setTitleAndSummaryColor(view);
    }
}
