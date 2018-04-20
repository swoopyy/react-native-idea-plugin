package ru.hse.plugin.core.utils;

import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;

public class Utils {
    public static int indentCount(String text, int start) {
        int i = start;
        int count = 0;
        while (i > 0) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                return count;
            }
            if (ch == ' ' || ch == '\t') {
                ++count;
            }
            --i;
        }
        return 0;
    }

    public static char indentType(String text) {
        if (text.indexOf('\t') != -1) {
            return '\t';
        } else {
            return ' ';
        }
    }

    public static String getIndent(char type, int count) {

        String out = "";
        for (int i = 0; i < count; ++i) {
            out += type;
        }
        return out;
    }
}
