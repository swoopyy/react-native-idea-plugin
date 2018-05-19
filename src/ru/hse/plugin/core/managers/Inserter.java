package ru.hse.plugin.core.managers;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import ru.hse.plugin.core.entities.Component;
import ru.hse.plugin.core.utils.Utils;

public class Inserter {
    private Component component;
    private Editor editor;
    private Project project;
    private boolean importPathExists = false;
    private int lastImportOffset = -1;

    public Inserter(
            Project project,
            Editor editor,
            Component component
    ) {
        this.editor = editor;
        this.component = component;
        this.project = project;
    }

    private boolean isImportPathNode(String nodeText, String documentText) {
        return nodeText.contains("import")
                && (nodeText.contains(component.getImportPathSingleQuoted()) || nodeText.contains(component.getImportPathDoubleQuoted()))
                && !nodeText.equals(documentText);
    }

    private boolean isImportNode(String nodeText, String documentText) {
        return nodeText.contains("import")
                && nodeText.contains("from")
                && !nodeText.equals(documentText);
    }

    private boolean isComponentImported(String nodeText) {
        return nodeText.contains(" " + component.getImportName()) || nodeText.contains("," + component.getImportName());
    }

    private void insert(int offset, String text) {
        editor.getDocument().insertString(offset, text);
    }

    private void reformatImportPathNode() {
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        String documentText = psiFile.getText();
        psiFile.getNode().getPsi().accept(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                String text = element.getText();
                if (isImportPathNode(text, documentText)) {
                    Utils.reformatText(element.getTextOffset(), element.getTextOffset() + element.getTextLength());
                }
                super.visitElement(element);
            }
        });
    }

    public void perform(int offset) {
        insert(offset, component.getSnippet());
        Utils.reformatText(offset, offset + component.getSnippet().length());
        insertImportStatement();
    }

    public void insertImportStatement() {
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        String documentText = psiFile.getText();
        psiFile.getNode().getPsi().accept(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                String text = element.getText();
                if (isImportPathNode(text, documentText)) {
                    importPathExists = true;
                    if (!isComponentImported(text)) {
                        int rBracketOffset = text.indexOf('{');
                        int importOffset = documentText.indexOf(text);
                        if (component.isDefault()) {
                            insert(importOffset + 1, String.format(" %s ", component.getImportName()));
                        } else {
                            if (rBracketOffset != -1) {
                                insert(importOffset + rBracketOffset + 1, String.format(" %s,", component.getImportName()));
                            } else {
                                insert(importOffset + 1, String.format("{ %s }", component.getImportName()));
                            }
                        }
                    }
                } else {
                    if (isImportNode(text, documentText)) {
                        lastImportOffset = documentText.indexOf(text) + text.length() + 1;
                    }
                    super.visitElement(element);
                }
            }
        });
        if (!importPathExists) {
            String statement;
            if (component.isDefault()) {
                statement = String.format("import %s from '%s';\n", component.getImportName(), component.getImportPathSingleQuoted());
            } else {
                statement = String.format("import { %s } from '%s';\n", component.getImportName(), component.getImportPathSingleQuoted());
            }
            if (lastImportOffset == -1) {
                insert(0, statement);
            } else {
                insert(lastImportOffset, statement);
            }
        }
        reformatImportPathNode();
    }
}
