package app.revanced.extension.tiktok.settings.preference;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.settings.BooleanSetting;
import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.shared.settings.StringSetting;
import app.revanced.extension.tiktok.settings.Settings;
import app.revanced.extension.tiktok.settings.SettingsStatus;

/**
 * Native, resource-free UltraMod settings shell.
 *
 * All values are persisted through the existing Setting classes. Existing preference keys,
 * defaults and patch-side consumers are intentionally left unchanged.
 */
public class UltraModSettingsFragment extends Fragment {
    private static final int BG = Color.rgb(14, 12, 20);
    private static final int SURFACE = Color.rgb(27, 23, 37);
    private static final int SURFACE_ALT = Color.rgb(37, 30, 49);
    private static final int TEXT = Color.rgb(248, 245, 252);
    private static final int MUTED = Color.rgb(171, 160, 188);
    private static final int ACCENT = Color.rgb(190, 132, 255);
    private static final int ACCENT_DARK = Color.rgb(112, 57, 170);
    private static final String PAGE_HOME = "home";
    private String currentPage = PAGE_HOME;
    private LinearLayout root;
    private LinearLayout content;
    private ScrollView scrollView;
    private int restoredScrollY;

    @Override
    public View onCreateView(android.view.LayoutInflater inflater, ViewGroup container, Bundle state) {
        if (state != null) {
            currentPage = state.getString("ultramod_page", PAGE_HOME);
            restoredScrollY = state.getInt("ultramod_scroll_y", 0);
        }
        if (!isKnownPage(currentPage)) currentPage = PAGE_HOME;
        buildScreen();
        return root;
    }

    @Override
    public void onDestroyView() {
        root = null;
        content = null;
        scrollView = null;
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("ultramod_page", currentPage);
        outState.putInt("ultramod_scroll_y", scrollView != null ? scrollView.getScrollY() : 0);
        super.onSaveInstanceState(outState);
    }

    private void buildScreen() {
        Context context = getActivity();
        if (root == null) root = new LinearLayout(context);
        else root.removeAllViews();
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setFitsSystemWindows(true);

        LinearLayout toolbar = new LinearLayout(context);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(dp(20), dp(12), dp(20), dp(10));
        toolbar.setBackgroundColor(BG);

        TextView back = text("‹", 34, TEXT, Typeface.NORMAL);
        back.setGravity(Gravity.CENTER);
        back.setVisibility(PAGE_HOME.equals(currentPage) ? View.INVISIBLE : View.VISIBLE);
        toolbar.addView(back, new LinearLayout.LayoutParams(dp(38), dp(44)));
        back.setOnClickListener(v -> {
            if ("country-picker".equals(currentPage) || "operator-picker".equals(currentPage)) {
                showPage("region");
            } else {
                showPage(PAGE_HOME);
            }
        });

        LinearLayout titleBox = new LinearLayout(context);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        titleBox.setPadding(dp(8), 0, 0, 0);
        TextView title = text(PAGE_HOME.equals(currentPage) ? "UltraMod" : pageName(currentPage), 20, TEXT, Typeface.BOLD);
        titleBox.addView(title);
        TextView subtitle = text("ЦЕНТР УПРАВЛЕНИЯ", 9, MUTED, Typeface.BOLD);
        subtitle.setLetterSpacing(0.12f);
        titleBox.addView(subtitle);
        toolbar.addView(titleBox, new LinearLayout.LayoutParams(0, -2, 1));

        TextView mark = text("UM", 13, ACCENT, Typeface.BOLD);
        mark.setGravity(Gravity.CENTER);
        mark.setBackground(round(SURFACE_ALT, dp(14)));
        toolbar.addView(mark, new LinearLayout.LayoutParams(dp(42), dp(42)));
        root.addView(toolbar);

        scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        scrollView.setClipToPadding(false);
        content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(8), dp(18), dp(28));
        scrollView.addView(content, new ScrollView.LayoutParams(-1, -2));
        root.addView(scrollView, new LinearLayout.LayoutParams(-1, 0, 1));
        if (restoredScrollY > 0) {
            final ScrollView restoredScroll = scrollView;
            final int targetScrollY = restoredScrollY;
            restoredScroll.post(() -> restoredScroll.scrollTo(0, targetScrollY));
            restoredScrollY = 0;
        }

        if (PAGE_HOME.equals(currentPage)) buildHome();
        else buildPage(currentPage);
    }

    private boolean isKnownPage(String page) {
        return PAGE_HOME.equals(page) || "feed".equals(page) || "downloads".equals(page)
                || "region".equals(page) || "more".equals(page)
                || "country-picker".equals(page) || "operator-picker".equals(page);
    }

    private void showPage(String page) {
        currentPage = page;
        restoredScrollY = 0;
        buildScreen();
    }

    private void buildHome() {
        addHero();
        addSectionLabel("ВОЗМОЖНОСТИ");
        LinearLayout firstRow = horizontalRow();
        if (SettingsStatus.feedFilterEnabled) addCategoryCard(firstRow, "◈", "Лента и фильтрация", "Реклама и контент", "feed");
        if (SettingsStatus.downloadEnabled) addCategoryCard(firstRow, "↓", "Загрузки", "Папка и водяной знак", "downloads");
        if (firstRow.getChildCount() > 0) {
            normalizeCategoryRow(firstRow);
            content.addView(firstRow);
        }

        LinearLayout secondRow = horizontalRow();
        if (SettingsStatus.simSpoofEnabled) addCategoryCard(secondRow, "◎", "Регион и страна", "Страна и оператор", "region");
        addCategoryCard(secondRow, "✦", "Дополнительно", "Информация и параметры", "more");
        normalizeCategoryRow(secondRow);
        content.addView(secondRow);

        addSectionLabel("ОБ ULTRAMOD");
        LinearLayout about = panel();
        about.setOrientation(LinearLayout.VERTICAL);
        TextView aboutTitle = text("Сделано для удобной настройки TikTok", 14, TEXT, Typeface.BOLD);
        aboutTitle.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        TextView aboutSummary = text("Все основные функции мода собраны в одном месте.", 12, MUTED, Typeface.NORMAL);
        LinearLayout.LayoutParams aboutSummaryParams = new LinearLayout.LayoutParams(-1, -2);
        aboutSummaryParams.topMargin = dp(6);
        TextView aboutVersion = text("Tik-Tok Ultra Mod · версия 1.1", 11, MUTED, Typeface.NORMAL);
        LinearLayout.LayoutParams aboutVersionParams = new LinearLayout.LayoutParams(-1, -2);
        aboutVersionParams.topMargin = dp(8);
        about.addView(aboutTitle);
        about.addView(aboutSummary, aboutSummaryParams);
        about.addView(aboutVersion, aboutVersionParams);
        content.addView(about, new LinearLayout.LayoutParams(-1, -2));
        TextView version = text("TIKTOK ULTRAMOD  ·  ВЕРСИЯ 1.0", 10, MUTED, Typeface.BOLD);
        version.setGravity(Gravity.CENTER);
        version.setPadding(0, dp(20), 0, dp(4));
        content.addView(version);
    }

    private void addHero() {
        LinearLayout hero = new LinearLayout(getActivity());
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(20), dp(20), dp(20), dp(20));
        hero.setBackground(gradient(new int[]{0xFF34204A, 0xFF1C1728, 0xFF171321}, dp(22)));
        TextView eyebrow = text("РАСШИРЕНИЕ TIKTOK", 10, 0xFFD8B9FF, Typeface.BOLD);
        eyebrow.setLetterSpacing(0.14f);
        hero.addView(eyebrow);
        TextView heading = text("Твой TikTok.\nТвои правила.", 27, Color.WHITE, Typeface.BOLD);
        heading.setPadding(0, dp(10), 0, dp(6));
        hero.addView(heading);
        TextView summary = text("Управляй лентой, загрузками и регионом в одном месте.", 13, 0xFFD0C3DE, Typeface.NORMAL);
        hero.addView(summary);
        TextView badge = text("●  ULTRAMOD АКТИВЕН", 10, 0xFFE2C8FF, Typeface.BOLD);
        badge.setPadding(dp(11), dp(8), dp(11), dp(8));
        badge.setBackground(round(0x443F2B58, dp(30)));
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(-2, -2);
        badgeParams.topMargin = dp(18);
        hero.addView(badge, badgeParams);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(22);
        content.addView(hero, params);
    }

    private void normalizeCategoryRow(LinearLayout row) {
        if (row.getChildCount() != 1) return;
        View card = row.getChildAt(0);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) card.getLayoutParams();
        params.width = -1;
        params.weight = 0;
        card.setLayoutParams(params);
    }

    private LinearLayout horizontalRow() {
        LinearLayout row = new LinearLayout(getActivity());
        row.setOrientation(LinearLayout.HORIZONTAL);
        return row;
    }

    private void addCategoryCard(LinearLayout row, String icon, String title, String summary, String page) {
        LinearLayout card = new LinearLayout(getActivity());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(15), dp(15), dp(13), dp(15));
        card.setBackground(round(SURFACE, dp(18)));
        card.setClickable(true);
        card.setFocusable(true);
        TextView symbol = text(icon, 23, ACCENT, Typeface.BOLD);
        symbol.setGravity(Gravity.CENTER);
        symbol.setBackground(round(SURFACE_ALT, dp(12)));
        card.addView(symbol, new LinearLayout.LayoutParams(dp(42), dp(42)));
        TextView heading = text(title, 14, TEXT, Typeface.BOLD);
        LinearLayout.LayoutParams headingParams = new LinearLayout.LayoutParams(-1, -2);
        headingParams.topMargin = dp(13);
        card.addView(heading, headingParams);
        TextView detail = text(summary, 11, MUTED, Typeface.NORMAL);
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(-1, -2);
        detailParams.topMargin = dp(4);
        card.addView(detail, detailParams);
        card.setOnClickListener(v -> showPage(page));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -2, 1);
        params.rightMargin = dp(6);
        params.leftMargin = dp(6);
        params.bottomMargin = dp(12);
        row.addView(card, params);
    }

    private void buildPage(String page) {
        TextView intro = text(pageDescription(page), 13, MUTED, Typeface.NORMAL);
        intro.setPadding(dp(2), 0, dp(2), dp(18));
        content.addView(intro);

        if ("country-picker".equals(page)) {
            buildCountryPickerPage();
        } else if ("operator-picker".equals(page)) {
            buildOperatorPickerPage();
        } else if ("feed".equals(page)) {
            addSectionLabel("ФИЛЬТРАЦИЯ ЛЕНТЫ");
            addToggle("Убирать рекламу из ленты", "Скрывать рекламные публикации.", Settings.REMOVE_ADS);
            addToggle("Автоматически пропускать рекламу", "Автоматически пролистывать рекламу, если она появилась.", Settings.SKIP_ADS_AT_RENDER);
            addToggle("Скрывать TikTok Shop", "Убирать публикации и элементы магазина.", Settings.HIDE_SHOP);
            addToggle("Скрывать прямые эфиры", "Не показывать прямые эфиры в ленте.", Settings.HIDE_LIVE);
            addToggle("Скрывать истории", "Не показывать истории в ленте.", Settings.HIDE_STORY);
            addToggle("Скрывать публикации с фото", "Не показывать публикации с изображениями.", Settings.HIDE_IMAGE);
        } else if ("downloads".equals(page)) {
            addSectionLabel("СОХРАНЕНИЕ");
            addActionRow("Папка загрузок", currentDownloadPath(), "Выбрать расположение и имя подпапки", this::showDownloadPathDialog);
            addToggle("Удалять водяной знак", "Убирать водяной знак при сохранении видео.", Settings.DOWNLOAD_WATERMARK);
        } else if ("region".equals(page)) {
            addSectionLabel("ПОДМЕНА РЕГИОНА");
            addToggle("Подменять данные SIM-карты", "Использовать заданные данные SIM-карты.", Settings.SIM_SPOOF);
            addActionRow("Страна", countryFlag(Settings.SIM_SPOOF_ISO.get()) + "  " + countryName(Settings.SIM_SPOOF_ISO.get()), "Открыть каталог стран", this::showCountryPicker);
            String selectedOperator = Settings.SIMSPOOF_OP_NAME.get();
            addActionRow("Оператор", selectedOperator == null || selectedOperator.trim().isEmpty()
                    ? "Не выбран" : selectedOperator, "Открыть каталог операторов", this::showOperatorPicker);
            addActionRow("Код мобильной сети (MCC+MNC)", Settings.SIMSPOOF_MCCMNC.get(), "Идентификатор страны и оператора", () -> editString("Код мобильной сети", Settings.SIMSPOOF_MCCMNC, InputType.TYPE_CLASS_NUMBER));
            addActionRow("Название оператора", Settings.SIMSPOOF_OP_NAME.get(), "Название мобильного оператора", () -> editString("Название оператора", Settings.SIMSPOOF_OP_NAME, InputType.TYPE_CLASS_TEXT));
            addInfo("Справочник операторов работает офлайн. При выборе оператора страна, MCC+MNC и название сохраняются вместе.");
            addInfo("Данные: MCC-MNC.dev (https://mcc-mnc.dev) — лицензия CC BY-SA 4.0.");
        } else if ("more".equals(page)) {
            addSectionLabel("ДОПОЛНИТЕЛЬНО");
            addToggle("Очищать ссылки при отправке", "Удалять параметры отслеживания из ссылок, которыми вы делитесь.", BaseSettings.SANITIZE_SHARING_LINKS);
            addToggle("Журнал отладки", "Включить технический журнал расширения.", BaseSettings.DEBUG);
            addActionRow("О TikTok UltraMod", "Версия 1.0", "Информация о модификации", this::showAbout);
            addActionRow("Telegram UltraMod", "@ultratiktok", "Новости и обновления", () -> Utils.openLink("https://t.me/ultratiktok"));
        }
    }

    private void addToggle(String title, String summary, BooleanSetting setting) {
        LinearLayout row = panel();
        LinearLayout texts = new LinearLayout(getActivity());
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.addView(text(title, 14, TEXT, Typeface.BOLD));
        TextView detail = text(summary, 11, MUTED, Typeface.NORMAL);
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(-1, -2);
        detailParams.topMargin = dp(5);
        texts.addView(detail, detailParams);
        row.addView(texts, new LinearLayout.LayoutParams(0, -2, 1));

        Switch toggle = new Switch(getActivity());
        toggle.setChecked(setting.get());
        row.setClickable(true);
        row.setFocusable(true);
        row.setOnClickListener(v -> toggle.setChecked(!toggle.isChecked()));
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            toggle.setThumbTintList(new ColorStateList(
                    new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                    new int[]{ACCENT, 0xFF81788B}));
            toggle.setTrackTintList(new ColorStateList(
                    new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                    new int[]{0x887D4DB0, 0xFF494250}));
        }
        LinearLayout.LayoutParams switchParams = new LinearLayout.LayoutParams(-2, -2);
        switchParams.leftMargin = dp(10);
        row.addView(toggle, switchParams);
        toggle.setOnCheckedChangeListener((button, checked) -> {
            setting.save(checked);
            if (setting.rebootApp) {
                showRestartPrompt();
            }
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(9);
        content.addView(row, params);
    }

    private void showRestartPrompt() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Требуется перезапуск")
                .setMessage("Перезапустите приложение, чтобы изменения вступили в силу.")
                .setPositiveButton("Перезапустить", (dialog, which) -> Utils.restartApp(getActivity()))
                .setNegativeButton("Позже", null)
                .show();
    }

    private void addActionRow(String title, String value, String summary, Runnable action) {
        LinearLayout row = panel();
        row.setOrientation(LinearLayout.VERTICAL);
        LinearLayout top = new LinearLayout(getActivity());
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView heading = text(title, 14, TEXT, Typeface.BOLD);
        top.addView(heading, new LinearLayout.LayoutParams(0, -2, 1));
        top.addView(text("›", 25, ACCENT, Typeface.NORMAL));
        row.addView(top);
        TextView current = text(value, 12, ACCENT, Typeface.BOLD);
        LinearLayout.LayoutParams currentParams = new LinearLayout.LayoutParams(-1, -2);
        currentParams.topMargin = dp(6);
        row.addView(current, currentParams);
        TextView detail = text(summary, 11, MUTED, Typeface.NORMAL);
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(-1, -2);
        detailParams.topMargin = dp(3);
        row.addView(detail, detailParams);
        row.setClickable(true);
        row.setFocusable(true);
        row.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(9);
        content.addView(row, params);
    }

    private void addInfo(String message) {
        TextView info = text(message, 11, MUTED, Typeface.NORMAL);
        info.setPadding(dp(14), dp(12), dp(14), dp(12));
        info.setBackground(round(SURFACE_ALT, dp(14)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(4);
        content.addView(info, params);
    }

    private void addSectionLabel(String label) {
        TextView view = text(label, 10, MUTED, Typeface.BOLD);
        view.setLetterSpacing(0.12f);
        view.setPadding(dp(3), dp(2), dp(3), dp(10));
        content.addView(view);
    }

    private LinearLayout panel() {
        LinearLayout panel = new LinearLayout(getActivity());
        panel.setGravity(Gravity.CENTER_VERTICAL);
        panel.setPadding(dp(15), dp(14), dp(15), dp(14));
        panel.setBackground(round(SURFACE, dp(16)));
        return panel;
    }

    private void showCountryPicker() {
        showPage("country-picker");
    }

    private void showOperatorPicker() {
        showPage("operator-picker");
    }

    private void buildCountryPickerPage() {
        addSectionLabel("ВЫБОР СТРАНЫ");
        addInfo("Найди страну по названию или двухбуквенному коду. Флаг отображается средствами Android.");
        EditText search = pickerSearch("Поиск страны");
        LinearLayout rows = new LinearLayout(getActivity());
        rows.setOrientation(LinearLayout.VERTICAL);
        content.addView(search, pickerSearchParams());
        content.addView(rows);

        final List<Country> countries = new ArrayList<>();
        Locale russian = new Locale("ru");
        for (String code : Locale.getISOCountries()) {
            String name = new Locale("", code).getDisplayCountry(russian);
            if (name != null && !name.trim().isEmpty()) countries.add(new Country(code, name));
        }
        Collections.sort(countries, (left, right) -> left.name.compareToIgnoreCase(right.name));

        Runnable refresh = () -> renderCountryRows(rows, countries, search.getText().toString());
        refresh.run();
        search.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) { refresh.run(); }
            @Override public void afterTextChanged(android.text.Editable value) { }
        });
    }

    private void renderCountryRows(LinearLayout rows, List<Country> countries, String rawQuery) {
        rows.removeAllViews();
        String query = rawQuery == null ? "" : rawQuery.trim().toLowerCase(new Locale("ru"));
        String selectedIso = Settings.SIM_SPOOF_ISO.get();
        int matches = 0;
        for (Country country : countries) {
            String code = country.code.toUpperCase(Locale.US);
            if (!query.isEmpty() && !country.name.toLowerCase(new Locale("ru")).contains(query)
                    && !code.toLowerCase(Locale.US).contains(query)) continue;
            matches++;
            LinearLayout row = panel();
            row.setOrientation(LinearLayout.HORIZONTAL);
            TextView flag = text(countryFlag(country.code), 27, TEXT, Typeface.NORMAL);
            flag.setGravity(Gravity.CENTER);
            row.addView(flag, new LinearLayout.LayoutParams(dp(46), dp(42)));

            LinearLayout labels = new LinearLayout(getActivity());
            labels.setOrientation(LinearLayout.VERTICAL);
            labels.addView(text(country.name, 14, TEXT, Typeface.BOLD));
            TextView codeLabel = text(code, 10, MUTED, Typeface.BOLD);
            codeLabel.setLetterSpacing(0.10f);
            LinearLayout.LayoutParams codeParams = new LinearLayout.LayoutParams(-1, -2);
            codeParams.topMargin = dp(3);
            labels.addView(codeLabel, codeParams);
            row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));

            if (country.code.equalsIgnoreCase(selectedIso)) {
                TextView selected = text("ВЫБРАНА", 9, ACCENT, Typeface.BOLD);
                selected.setLetterSpacing(0.05f);
                row.addView(selected);
            } else {
                row.addView(text("›", 24, ACCENT, Typeface.NORMAL));
            }

            row.setClickable(true);
            row.setFocusable(true);
            row.setOnClickListener(v -> {
                String newIso = country.code.toLowerCase(Locale.US);
                String oldIso = Settings.SIM_SPOOF_ISO.get();
                if (oldIso == null || !newIso.equalsIgnoreCase(oldIso)) {
                    Settings.SIM_SPOOF_ISO.save(newIso);
                    // Do not leave an operator from the previous country displayed as selected.
                    Settings.SIMSPOOF_MCCMNC.save("");
                    Settings.SIMSPOOF_OP_NAME.save("");
                }
                showPage("operator-picker");
            });
            rows.addView(row, pickerRowParams());
        }
        if (matches == 0) addInfo("По вашему запросу ничего не найдено.");
    }

    private void buildOperatorPickerPage() {
        String iso = Settings.SIM_SPOOF_ISO.get();
        addSectionLabel("ВЫБОР ОПЕРАТОРА");
        LinearLayout countryHeader = panel();
        countryHeader.setOrientation(LinearLayout.HORIZONTAL);
        TextView flag = text(countryFlag(iso), 28, TEXT, Typeface.NORMAL);
        flag.setGravity(Gravity.CENTER);
        countryHeader.addView(flag, new LinearLayout.LayoutParams(dp(48), dp(44)));
        LinearLayout countryLabels = new LinearLayout(getActivity());
        countryLabels.setOrientation(LinearLayout.VERTICAL);
        countryLabels.addView(text(countryName(iso), 14, TEXT, Typeface.BOLD));
        countryLabels.addView(text("Страна · " + (iso == null ? "--" : iso.toUpperCase(Locale.US)), 10, MUTED, Typeface.BOLD));
        LinearLayout.LayoutParams countryLabelParams = new LinearLayout.LayoutParams(0, -2, 1);
        countryHeader.addView(countryLabels, countryLabelParams);
        content.addView(countryHeader, pickerRowParams());

        final List<SimOperator> operators;
        try {
            operators = loadOperators(iso);
        } catch (Exception error) {
            addInfo("Не удалось прочитать локальный справочник операторов. Проверьте, что файл networks.min.json включён в assets сборки. Детали: "
                    + error.getClass().getSimpleName());
            return;
        }
        if (operators.isEmpty()) {
            addInfo("В справочнике нет записей для этой страны. Можно вернуться назад и выбрать другую страну.");
            return;
        }

        EditText search = pickerSearch("Поиск оператора или MCC+MNC");
        LinearLayout rows = new LinearLayout(getActivity());
        rows.setOrientation(LinearLayout.VERTICAL);
        content.addView(search, pickerSearchParams());
        content.addView(rows);
        Runnable refresh = () -> renderOperatorRows(rows, operators, search.getText().toString());
        refresh.run();
        search.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) { refresh.run(); }
            @Override public void afterTextChanged(android.text.Editable value) { }
        });
    }

    private void renderOperatorRows(LinearLayout rows, List<SimOperator> operators, String rawQuery) {
        rows.removeAllViews();
        String query = rawQuery == null ? "" : rawQuery.trim().toLowerCase(new Locale("ru"));
        int matches = 0;
        for (SimOperator operator : operators) {
            String status = operatorStatusLabel(operator.status);
            String haystack = (operator.name + " " + operator.mccMnc + " " + operator.status + " " + status)
                    .toLowerCase(new Locale("ru"));
            if (!query.isEmpty() && !haystack.contains(query)) continue;
            matches++;

            LinearLayout row = panel();
            row.setOrientation(LinearLayout.HORIZONTAL);
            TextView mark = text(operator.name.substring(0, 1).toUpperCase(new Locale("ru")), 17, ACCENT, Typeface.BOLD);
            mark.setGravity(Gravity.CENTER);
            mark.setBackground(round(SURFACE_ALT, dp(13)));
            row.addView(mark, new LinearLayout.LayoutParams(dp(42), dp(42)));

            LinearLayout labels = new LinearLayout(getActivity());
            labels.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams labelsParams = new LinearLayout.LayoutParams(0, -2, 1);
            labelsParams.leftMargin = dp(12);
            labels.addView(text(operator.name, 14, TEXT, Typeface.BOLD));
            TextView details = text("MCC+MNC  " + operator.mccMnc, 10, MUTED, Typeface.NORMAL);
            LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(-1, -2);
            detailsParams.topMargin = dp(4);
            labels.addView(details, detailsParams);
            TextView state = text(status, 10, MUTED, Typeface.NORMAL);
            LinearLayout.LayoutParams stateParams = new LinearLayout.LayoutParams(-1, -2);
            stateParams.topMargin = dp(2);
            labels.addView(state, stateParams);
            row.addView(labels, labelsParams);
            row.addView(text("›", 24, ACCENT, Typeface.NORMAL));

            row.setClickable(true);
            row.setFocusable(true);
            row.setOnClickListener(v -> {
                Settings.SIMSPOOF_MCCMNC.save(operator.mccMnc);
                Settings.SIMSPOOF_OP_NAME.save(operator.name);
                showPage("region");
            });
            rows.addView(row, pickerRowParams());
        }
        if (matches == 0) addInfo("По вашему запросу ничего не найдено.");
    }

    private EditText pickerSearch(String hint) {
        EditText search = new EditText(getActivity());
        search.setSingleLine(true);
        search.setTextSize(14);
        search.setTextColor(TEXT);
        search.setHintTextColor(MUTED);
        search.setHint(hint);
        search.setPadding(dp(14), dp(11), dp(14), dp(11));
        search.setBackground(round(SURFACE_ALT, dp(13)));
        search.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        return search;
    }

    private LinearLayout.LayoutParams pickerSearchParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(12);
        return params;
    }

    private LinearLayout.LayoutParams pickerRowParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(9);
        return params;
    }

    private void addPickerRow(View row) {
        LinearLayout.LayoutParams params = pickerRowParams();
        content.addView(row, params);
    }

    private String countryFlag(String iso) {
        if (iso == null || !iso.matches("(?i)[a-z]{2}")) return "🌐";
        String upper = iso.toUpperCase(Locale.US);
        int first = 0x1F1E6 + upper.charAt(0) - 'A';
        int second = 0x1F1E6 + upper.charAt(1) - 'A';
        return new String(Character.toChars(first)) + new String(Character.toChars(second));
    }

    private String operatorStatusLabel(String status) {
        if (status == null) return "Неизвестно";
        switch (status.toLowerCase(Locale.US)) {
            case "operational": return "Работает";
            case "temporary operational": return "Временно работает";
            case "not operational": return "Не работает";
            case "planned": return "Планируется";
            case "upcoming": return "Ожидается";
            case "ongoing": return "В процессе";
            case "implement / design": return "Проектирование";
            case "allocated": return "Выделен";
            case "reserved": return "Зарезервирован";
            case "returned spare": return "Возвращён в резерв";
            case "test network": return "Тестовая сеть";
            case "testing": return "Тестирование";
            case "unknown": return "Неизвестно";
            default: return status;
        }
    }

    private int pickerListHeight() {
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        return Math.max(dp(180), Math.min(dp(360), (int) (screenHeight * 0.42f)));
    }

    private List<SimOperator> loadOperators(String iso) throws Exception {
        List<SimOperator> result = new ArrayList<>();
        if (iso == null || !iso.matches("(?i)[a-z]{2}")) return result;
        try (InputStream input = NetworksData.open();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
            JSONArray records = new JSONArray(new String(output.toByteArray(), StandardCharsets.UTF_8));
            String wantedIso = iso.toLowerCase(Locale.US);
            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.optJSONObject(i);
                if (record == null) continue;
                JSONObject country = record.optJSONObject("country");
                if (country == null) continue;
                String recordIso = country.optString("iso2", "");
                if (!recordIso.matches("(?i)[a-z]{2}") || !wantedIso.equals(recordIso.toLowerCase(Locale.US))) continue;
                String mcc = record.optString("mcc", "");
                String mnc = record.optString("mnc", "");
                if (mcc.isEmpty() || mnc.isEmpty()) continue;
                String brand = record.optString("brand", "").trim();
                String operator = record.optString("operator", "").trim();
                String name = brand.isEmpty() ? operator : brand;
                if (name.isEmpty()) continue;
                result.add(new SimOperator(name, mcc + mnc, record.optString("status", "Unknown")));
            }
        }
        Collections.sort(result, (left, right) -> {
            boolean leftOperational = "Operational".equalsIgnoreCase(left.status);
            boolean rightOperational = "Operational".equalsIgnoreCase(right.status);
            if (leftOperational != rightOperational) return leftOperational ? -1 : 1;
            int byName = left.name.compareToIgnoreCase(right.name);
            return byName != 0 ? byName : left.mccMnc.compareTo(right.mccMnc);
        });
        return result;
    }

    private static final class SimOperator {
        final String name;
        final String mccMnc;
        final String status;
        SimOperator(String name, String mccMnc, String status) {
            this.name = name;
            this.mccMnc = mccMnc;
            this.status = status;
        }
    }

    private void editString(String title, StringSetting setting, int inputType) {
        EditText input = new EditText(getActivity());
        input.setSingleLine(true);
        input.setText(setting.get());
        input.setSelection(input.getText().length());
        input.setInputType(inputType);
        if (setting == Settings.SIMSPOOF_MCCMNC) {
            input.setFilters(new android.text.InputFilter[]{
                    new android.text.InputFilter.LengthFilter(6)
            });
        } else if (setting == Settings.SIMSPOOF_OP_NAME) {
            input.setFilters(new android.text.InputFilter[]{
                    new android.text.InputFilter.LengthFilter(64)
            });
        }

        int pad = dp(20);
        LinearLayout wrapper = new LinearLayout(getActivity());
        wrapper.setPadding(pad, dp(8), pad, 0);
        wrapper.addView(input, new LinearLayout.LayoutParams(-1, -2));

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(wrapper)
                .setPositiveButton("Сохранить", null)
                .setNegativeButton("Отмена", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String value = input.getText().toString().trim();
            if (setting == Settings.SIMSPOOF_MCCMNC && !value.matches("[0-9]{5,6}")) {
                input.setError("Введите MCC+MNC: 5 или 6 цифр");
                input.requestFocus();
                return;
            }
            if (setting == Settings.SIMSPOOF_OP_NAME && (value.isEmpty() || value.length() > 64)) {
                input.setError("Введите название оператора (до 64 символов)");
                input.requestFocus();
                return;
            }
            setting.save(value);
            dialog.dismiss();
            showPage("region");
        }));
        dialog.show();
    }

    private void showDownloadPathDialog() {
        String[] roots = new String[]{"DCIM", "Movies", "Pictures"};
        String[] labels = new String[]{"Камера (DCIM)", "Фильмы", "Изображения"};
        String saved = Settings.DOWNLOAD_PATH.get();
        String rootName = saved.contains("/") ? saved.substring(0, saved.indexOf('/')) : "Pictures";
        String childName = saved.contains("/") ? saved.substring(saved.indexOf('/') + 1) : "TikTok";
        int selected = 2;
        for (int i = 0; i < roots.length; i++) if (roots[i].equals(rootName)) selected = i;

        final int[] chosen = new int[]{selected};
        LinearLayout wrapper = new LinearLayout(getActivity());
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setPadding(dp(20), dp(8), dp(20), 0);
        android.widget.RadioGroup group = new android.widget.RadioGroup(getActivity());
        for (int i = 0; i < labels.length; i++) {
            android.widget.RadioButton radio = new android.widget.RadioButton(getActivity());
            radio.setText(labels[i]);
            radio.setId(1000 + i);
            group.addView(radio);
        }
        group.check(1000 + selected);
        group.setOnCheckedChangeListener((g, id) -> {
            int index = id - 1000;
            if (index >= 0 && index < roots.length) chosen[0] = index;
        });
        wrapper.addView(group);

        EditText folder = new EditText(getActivity());
        folder.setSingleLine(true);
        folder.setHint("Имя подпапки");
        folder.setText(childName);
        folder.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        wrapper.addView(folder, new LinearLayout.LayoutParams(-1, -2));

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("Папка загрузок")
                .setView(wrapper)
                .setPositiveButton("Сохранить", null)
                .setNegativeButton("Отмена", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String subfolder = folder.getText().toString().trim();
            if (subfolder.isEmpty()) subfolder = "TikTok";

            // The setting accepts only one child folder below a known media directory.
            // Reject separators, traversal names and filesystem-reserved characters.
            if (".".equals(subfolder) || "..".equals(subfolder)
                    || !subfolder.matches("[\\p{L}\\p{N} _().-]{1,64}")) {
                folder.setError("Используйте до 64 букв, цифр, пробелов, дефиса или скобок");
                folder.requestFocus();
                return;
            }

            Settings.DOWNLOAD_PATH.save(roots[chosen[0]] + "/" + subfolder);
            dialog.dismiss();
            showPage("downloads");
        }));
        dialog.show();
    }

    private void showAbout() {
        new AlertDialog.Builder(getActivity())
                .setTitle("TikTok UltraMod")
                .setMessage("TikTok UltraMod — модифицированная версия TikTok с дополнительными возможностями и настройками.\n\nВерсия: 1.0\n\nTelegram: @ultratiktok")
                .setPositiveButton("Закрыть", null)
                .show();
    }

    private String currentDownloadPath() {
        String saved = Settings.DOWNLOAD_PATH.get();
        int separator = saved.indexOf('/');
        if (separator < 0) return saved;

        String root = saved.substring(0, separator);
        String folder = saved.substring(separator + 1);
        String rootLabel;
        switch (root) {
            case "DCIM": rootLabel = "Камера (DCIM)"; break;
            case "Movies": rootLabel = "Фильмы"; break;
            case "Pictures": rootLabel = "Изображения"; break;
            default: rootLabel = root;
        }
        return rootLabel + " / " + (folder.isEmpty() ? "TikTok" : folder);
    }

    private String countryName(String iso) {
        if (iso == null || iso.length() != 2) return "Не выбрана";
        String name = new Locale("", iso.toUpperCase(Locale.US)).getDisplayCountry(new Locale("ru"));
        return name == null || name.isEmpty() ? iso.toUpperCase(Locale.US) : name;
    }

    private String pageName(String page) {
        if ("feed".equals(page)) return "Лента и фильтрация";
        if ("downloads".equals(page)) return "Загрузки";
        if ("region".equals(page)) return "Регион и страна";
        if ("country-picker".equals(page)) return "Выбор страны";
        if ("operator-picker".equals(page)) return "Выбор оператора";
        return "Дополнительно";
    }

    private String pageDescription(String page) {
        if ("feed".equals(page)) return "Настрой, какие публикации и элементы будут отображаться в ленте.";
        if ("downloads".equals(page)) return "Управляй расположением сохранённых видео и водяным знаком.";
        if ("region".equals(page)) return "Настройки региона и данных мобильного оператора.";
        if ("country-picker".equals(page)) return "Выбери страну из каталога.";
        if ("operator-picker".equals(page)) return "Выбери оператора из локального справочника.";
        return "Полезные ссылки и дополнительные параметры расширения.";
    }

    private TextView text(String value, int sp, int color, int style) {
        TextView view = new TextView(getActivity());
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setTypeface(Typeface.DEFAULT, style);
        view.setIncludeFontPadding(true);
        return view;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private GradientDrawable gradient(int[] colors, int radius) {
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR, colors);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static final class Country {
        final String code;
        final String name;
        Country(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
}
