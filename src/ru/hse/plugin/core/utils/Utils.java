package ru.hse.plugin.core.utils;

import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import ru.hse.plugin.core.managers.EditorManager;

public class Utils {
    private static EditorManager manager = new EditorManager();

    public static int indentCount(String text, int start) {
        int i = start;
        int count = 0;
        boolean lFound = false;
        while (i > 0) {
            char ch = text.charAt(i);
            if (ch == '<') {
                lFound = true;
            }
            if (ch == '\n' && count != 0) {
                return count;
            }
            if (lFound && (ch == ' ' || ch == '\t')) {
                ++count;
            }
            --i;
        }
        return 0;
    }

    public static char indentType() {
        if (manager.getEditor().getSettings().isUseTabCharacter(manager.getProject())) {
            return '\t';
        } else {
            return ' ';
        }
    }

    public static int getTabCount() {
        if (Utils.indentType() == '\t') {
            return 1;
        }
        return manager.getEditor().getSettings().getTabSize(manager.getProject());
    }

    public static String getIndent(int count) {
        char type = Utils.indentType();
        String out = "";
        for (int i = 0; i < count; ++i) {
            out += type;
        }
        return out;
    }
}
