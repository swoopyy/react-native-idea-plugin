package ru.hse.plugin.core.callbacks;

import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.psi.PsiElement;
import ru.hse.plugin.core.entities.Component;

public interface ComponentClicked {
    void perform(PsiElement element, Component component);
}
