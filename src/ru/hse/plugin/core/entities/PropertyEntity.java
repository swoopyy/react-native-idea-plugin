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
                String psiText = propertyPsi.getText();
                int offset = propertyPsi.getTextOffset();
                String text = document.getText().substring(offset, offset + psiText.length());
                int startBr = text.indexOf("{");
                int startQ = text.indexOf("\"");
                int startQuo = text.indexOf("\'");
                int start = startQ;
                int end = startBr + 1;
                if (startBr != -1) {
                    start = startBr;
                    end = text.indexOf("}");
                } else if (startQ != -1 && startQ < startQuo) {
                    start = startQ;
                    end = text.lastIndexOf('"');
                } else if(startQuo != -1 && startQuo < startQ) {
                    start = startQuo;
                    end = text.lastIndexOf("'");
                }
                start += propertyPsi.getTextOffset();
                end += propertyPsi.getTextOffset();
                String val = value.toString();
                if (super.getType() != Types.bool && super.getType() != Types.number) {
                    val = '"' + val + '"';
                }
                if (end > start) {
                    document.replaceString(start + 1, end, val);
                }
            }
        });
    }

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
                document.insertString(insertionStart, String.format(" %s={%s}", getName(), getDefaultValue()));
                setSelected(true);
            }
            Utils.reformatText(psiElement.getTextOffset(), psiElement.getTextOffset() + psiElement.getTextLength());
        });
    }

}
