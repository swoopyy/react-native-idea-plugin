package ru.hse.plugin.core.utils;

import com.intellij.lang.Language;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import ru.hse.plugin.core.managers.EditorManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    private static EditorManager manager = new EditorManager();

    public static int indentCount(String text, int start) {
        int i = start;
        int count = 0;
        boolean lFound = false;
        while (i > 0) {
            char ch = text.charAt(i);
            if (ch == ' ' && text.charAt(i + 1) != ' ') {
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

    public static String indentText(String text, String firstLineIndent) {
        String out = "";
        String indent = Utils.getIndent(Utils.getTabCount());
        for(String line: text.split("\n")) {
            out += indent + line + "\n";
        }
        return firstLineIndent + out;
    }

    public static void reformatText(int start, int end) {
        WriteCommandAction.runWriteCommandAction(manager.getProject(), () -> {
            try {
                PsiFile psiFile = PsiDocumentManager.getInstance(manager.getProject()).getPsiFile(manager.getEditor().getDocument());
                CodeStyleManager.getInstance(manager.getProject()).reformatText(psiFile, start, end);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        });
    }


    public static List<String> getAllExportingFiles(String path) throws IOException{
        List<String> files = new ArrayList<String>();
        for(File file: new File(path).listFiles()) {
            if (file.isFile()) {
                files.add(file.getAbsolutePath());
            }
            if (isJsContainingDirectory(file)) {
                files.addAll(getAllExportingFiles(file.getPath()));
            }
        }
        return files;
    }



    private static boolean isJsContainingDirectory(File file) {
        String fileName = file.getName();
        return file.isDirectory()
                && !fileName.equals("ios")
                && !fileName.equals("android")
                && !fileName.equals("node_modules");
    }
}
