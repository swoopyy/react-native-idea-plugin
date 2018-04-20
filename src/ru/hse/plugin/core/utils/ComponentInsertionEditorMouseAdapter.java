package ru.hse.plugin.core.utils;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorMouseAdapter;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.IElementTypePattern;
import com.intellij.psi.*;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.RefactoringFactory;
import org.jetbrains.annotations.NotNull;
import org.mozilla.javascript.ast.AstNode;

public class ComponentInsertionEditorMouseAdapter extends EditorMouseAdapter {
    private String componentCode;
    private Editor editor;
    private Project project;
    private SnippetInserted snippetInserted;

    public ComponentInsertionEditorMouseAdapter(
            Project project,
            Editor editor,
            SnippetInserted snippetInserted,
            String componentCode
    ) {
        this.editor = editor;
        this.componentCode = componentCode;
        this.project = project;
        this.snippetInserted = snippetInserted;
    }

    @Override
    public void mouseClicked(EditorMouseEvent e) {
        int offset = editor.getCaretModel().getOffset();
        WriteCommandAction.runWriteCommandAction(project, () -> {
            editor.getDocument().insertString(offset, componentCode);
            PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
            String documentText = psiFile.getText();
            psiFile.getNode().getPsi().accept(new PsiRecursiveElementWalkingVisitor() {
                @Override
                public void visitElement(PsiElement element) {
                    String text = element.getText();
                    if (text.contains("import")
                            && text.contains("react-native")
                            && !text.equals(documentText)) {
                        int rBracketOffset = text.indexOf('{');
                        int importOffset = documentText.indexOf(text);
                        editor.getDocument().insertString(importOffset + rBracketOffset + 1, " View,");
                    } else {
                        super.visitElement(element);
                    }
                }
            });
            if (snippetInserted != null) {
                snippetInserted.perform();
            }
        });
    }
}
