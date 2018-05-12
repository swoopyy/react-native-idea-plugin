package ru.hse.plugin.core.entities;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import ru.hse.plugin.core.managers.EditorManager;
import ru.hse.plugin.core.utils.Utils;

public class PropertyEntity extends Property {
    private static EditorManager editorManager = new EditorManager();

    private ComponentEntity componentEntity;
    private Object value;
    private boolean isSelected;

    public PropertyEntity(Property property, ComponentEntity componentEntity, Object value, boolean isSelected) {
        super(property.getName(), property.getTypeStringRepr(), property.isRequired());
        this.value = value;
        this.isSelected = isSelected;
        this.componentEntity = componentEntity;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void toggle() {
        WriteCommandAction.runWriteCommandAction(editorManager.getProject(), () -> {
            Document document = editorManager.getEditor().getDocument();
            PsiElement psiElement = componentEntity.getPsiElement();
            if (isSelected()) {
                PsiElement propertyPsi = componentEntity.getPropertyContainingPsiElement(this);
                String propertyText = propertyPsi.getText();
                int start = propertyPsi.getTextOffset();
                int end = start + propertyPsi.getTextLength();
                setSelected(false);
                document.deleteString(start, end);
            } else {
                int closingTag;
                int closingTag1 = psiElement.getText().indexOf('>');
                int closingTag2 = psiElement.getText().indexOf("/>");
                if (closingTag1 == closingTag2 + 1) {
                    closingTag = closingTag2;
                } else {
                    closingTag = closingTag1;
                }
                int insertionStart = psiElement.getTextOffset() + closingTag;
                document.insertString(insertionStart, " " + getName() + "={}");
                setSelected(true);
            }
            Utils.reformatText(psiElement.getTextOffset(), psiElement.getTextOffset() + psiElement.getTextLength());
        });
    }
}
