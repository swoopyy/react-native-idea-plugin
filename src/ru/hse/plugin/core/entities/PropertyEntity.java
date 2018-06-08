package ru.hse.plugin.core.entities;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import ru.hse.plugin.core.managers.EditorManager;
import ru.hse.plugin.core.utils.Utils;

public class PropertyEntity extends Property {
    private static EditorManager editorManager = new EditorManager();

    private ComponentEntity componentEntity;
    private Object value;
    private boolean isSelected;

    private int closingTagOffset(PsiElement psiElement) {
        for (PsiElement child: psiElement.getChildren()) {
            if (child.toString().equals("XmlToken:XML_EMPTY_ELEMENT_END")) {
                return child.getTextOffset();
            }
            if (child.toString().equals("XmlToken:XML_TAG_END")) {
                return child.getTextOffset();
            }
        }
        return psiElement.getTextOffset();
    }

    private String embededValue() {
        if (getType() == Types.color
                || getType() == Types.string
                || getType() == Types.enumeration) {
            if (!value.toString().contains("\"")) {
                return "\"" + value + "\"";
            } else {
                return value.toString();
            }
        } else {
            return "{" + value + "}";
        }
    }

    public PropertyEntity(Property property, ComponentEntity componentEntity, Object value, boolean isSelected) {
        super(property.getName(), property.getTypeStringRepr(), property.isRequired());
        if (value != null) {
            this.value = value;
        } else {
            this.value = getDefaultValue();
        }
        this.isSelected = isSelected;
        this.componentEntity = componentEntity;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getValue() {
        return this.value.toString();
    }


    public void setValue(Object value) {
        this.value = value;
        WriteCommandAction.runWriteCommandAction(editorManager.getProject(), () -> {
            Document document = editorManager.getEditor().getDocument();
            if (isSelected()) {
                PsiElement propertyPsi = componentEntity.getPropertyContainingPsiElement(this);
                if (propertyPsi == null) {
                    toggle();
                } else {
                    propertyPsi.accept(new PsiRecursiveElementWalkingVisitor() {
                        @Override
                        public void visitElement(PsiElement element) {
                            if (element.toString().equals("PsiElement(XML_ATTRIBUTE_VALUE)")) {
                                int start = element.getTextOffset();
                                int end = element.getTextOffset() + element.getText().length() - 1;
                                char endChar = document.getText().charAt(end);
                                if (endChar == '}' || endChar == '"') {
                                    end += 1;
                                }
                                document.replaceString(start - 1, end, embededValue());
                                return;
                            }
                            super.visitElement(element);
                        }
                    });
                }
            }
        });
    }


    public void toggle() {
        WriteCommandAction.runWriteCommandAction(editorManager.getProject(), () -> {
            Document document = editorManager.getEditor().getDocument();
            PsiElement psiElement = componentEntity.getPsiElement();
            if (isSelected()) {
                PsiElement propertyPsi = componentEntity.getPropertyContainingPsiElement(this);
                int start = propertyPsi.getTextOffset();
                int end = start + propertyPsi.getTextLength();
                setSelected(false);
                document.deleteString(start, end);
            } else {
                int insertionStart = closingTagOffset(psiElement);
                document.insertString(insertionStart, String.format(" %s=%s", getName(), embededValue()));
                setSelected(true);
            }
            Utils.reformatText(psiElement.getTextOffset(), psiElement.getTextOffset() + psiElement.getTextLength());
        });
    }

}
