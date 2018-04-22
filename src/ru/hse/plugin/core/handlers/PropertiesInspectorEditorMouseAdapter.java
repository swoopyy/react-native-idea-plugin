package ru.hse.plugin.core.handlers;

import com.intellij.openapi.editor.event.EditorMouseAdapter;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import ru.hse.plugin.core.callbacks.ComponentClicked;
import ru.hse.plugin.core.entities.Component;
import ru.hse.plugin.core.entities.ComponentCollection;
import ru.hse.plugin.core.managers.EditorManager;

public class PropertiesInspectorEditorMouseAdapter extends EditorMouseAdapter {
    private static EditorManager manager = new EditorManager();
    ComponentClicked componentClicked;

    public PropertiesInspectorEditorMouseAdapter(ComponentClicked componentClicked) {
        this.componentClicked = componentClicked;
    }
    @Override
    public void mouseClicked(EditorMouseEvent e) {
        PsiFile psiFile = PsiDocumentManager.getInstance(manager.getProject()).getPsiFile(manager.getEditor().getDocument());
        PsiElement element = psiFile.findElementAt(e.getEditor().getCaretModel().getOffset()).getParent();
        PsiElement[] children = element.getChildren();
        if (children.length > 1) {
            Component component = ComponentCollection.getComponent(children[1].getText());
            if (component != null) {
                this.componentClicked.perform(element, component);
            }
        }
        super.mouseClicked(e);
    }
}
