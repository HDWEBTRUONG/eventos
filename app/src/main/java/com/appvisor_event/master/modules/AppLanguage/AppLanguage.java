package com.appvisor_event.master.modules.AppLanguage;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by bsfuji on 16/06/02.
 */
public class AppLanguage
{
    public class Language
    {
        public final static int Japanese = 0;
        public final static int English  = 1;
    }

    public static void setLanguageWithStringValue(Context context, String value)
    {
        SharedPreferences data = context.getSharedPreferences("AppLanguage", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = data.edit();
        editor.putString("language", value);
        editor.apply();
    }

    public static String getLanguageWithStringValue(Context context)
    {
        SharedPreferences data = context.getSharedPreferences("AppLanguage", Context.MODE_PRIVATE);
        String language = data.getString("language", "ja");
        return language;
    }

    public static int language(Context context)
    {
        SharedPreferences data = context.getSharedPreferences("AppLanguage", Context.MODE_PRIVATE);
        String language = data.getString("language", "ja");
        return AppLanguage.languageWithStringValue(language);
    }

    public static Boolean isJapanese(Context context)
    {
        return (Language.Japanese == AppLanguage.language(context));
    }

    private static int languageWithStringValue(String value)
    {
        return value.equals("ja") ? Language.Japanese : Language.English;
    }
}
