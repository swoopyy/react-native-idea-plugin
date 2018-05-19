package ru.hse.plugin.core.handlers;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorMouseAdapter;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import ru.hse.plugin.core.callbacks.SnippetInserted;
import ru.hse.plugin.core.entities.Component;
import ru.hse.plugin.core.managers.Inserter;
import ru.hse.plugin.core.utils.Utils;

public class ComponentInsertionEditorMouseAdapter extends EditorMouseAdapter {
    private Editor editor;
    private Project project;
    private SnippetInserted snippetInserted;
    private Inserter inserter;

    public ComponentInsertionEditorMouseAdapter(
            Project project,
            Editor editor,
            SnippetInserted snippetInserted,
            Component component
    ) {
        this.editor = editor;
        this.project = project;
        this.snippetInserted = snippetInserted;
        inserter = new Inserter(project, editor, component);
    }


    @Override
    public void mouseClicked(EditorMouseEvent e) {
        int offset = editor.getCaretModel().getOffset();
        WriteCommandAction.runWriteCommandAction(project, () -> {
            inserter.perform(offset);
            if (snippetInserted != null) {
                snippetInserted.perform();
            }
        });
        super.mouseClicked(e);
    }
}
