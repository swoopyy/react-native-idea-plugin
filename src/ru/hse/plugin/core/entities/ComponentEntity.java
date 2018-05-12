package ru.hse.plugin.core.entities;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import ru.hse.plugin.core.managers.EditorManager;
import ru.hse.plugin.core.utils.Utils;
import sun.jvm.hotspot.ui.Editor;

import java.util.ArrayList;
import java.util.List;

public class ComponentEntity extends Component {
    private static EditorManager editorManager = new EditorManager();

    private PsiElement psiElement;
    private List<PropertyEntity> propertyEntities;



    private boolean hasProperty(Property property) {
        final boolean[] hasProperty = {false};
        psiElement.accept(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if (element.getText().equals(property.getName())) {
                    hasProperty[0] = true;
                }
                super.visitElement(element);
            }
        });
        return hasProperty[0];
    }

    private String getPropertyValue(Property property) {
        PsiElement psiElement = getPropertyContainingPsiElement(property);
        if (psiElement == null) {
            return null;
        }
        String text = psiElement.getText();
        if (text.split("\"").length == 3) {
            return text.split("\"")[1];
        } else if (text.split("'").length == 3) {
            return text.split("'")[1];
        } else if (text.indexOf("{") != -1 && text.indexOf("}") != -1) {
            int start = text.indexOf("{");
            int end = text.indexOf("}");
            return text.substring(start + 1, end);
        }
        return null;
    }

    private PsiElement getPropertyContainingPsiElement(Property property) {
        final PsiElement[] propertyValue = {null};
        psiElement.accept(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                String text = element.getText();
                if (text.contains(property.getName()) && !text.equals(property.getName())) {
                    propertyValue[0] = element;
                }
                super.visitElement(element);
            }
        });
        return propertyValue[0];
    }

    public ComponentEntity(Component component, PsiElement psiElement) {
        super(component.getName(),
                component.getDescription(),
                component.getPlatform(),
                component.getUrl(),
                component.getIconPath(),
                component.getProperties(),
                component.isContainer());
        this.psiElement = psiElement;
        propertyEntities = new ArrayList<>();
        for(Property property: component.getProperties()) {
            if (hasProperty(property)) {
                String value = getPropertyValue(property);
                if (value.contains("'")) {
                    propertyEntities.add(new PropertyEntity(property, value.replaceAll("'", ""), true));
                } else if (value.contains("\'")) {
                    propertyEntities.add(new PropertyEntity(property, value.replaceAll("\"", ""), true));
                } else if (value.equals("true") || value.equals("false")) {
                    propertyEntities.add(new PropertyEntity(property, Boolean.parseBoolean(value), true));
                } else if (value.matches("-?\\d+(\\.\\d+)?")) {
                    propertyEntities.add(new PropertyEntity(property, Double.parseDouble(value),true));
                } else {
                    propertyEntities.add(new PropertyEntity(property, null, true));
                }
            } else {
                propertyEntities.add(new PropertyEntity(property, null, false));
            }
        }
    }

    public void toggleProperty(PropertyEntity propertyEntity) {
        WriteCommandAction.runWriteCommandAction(editorManager.getProject(), () -> {
            Document document = editorManager.getEditor().getDocument();
            if (propertyEntity.isSelected()) {
                PsiElement propertyPsi = getPropertyContainingPsiElement(propertyEntity);
                String propertyText = propertyPsi.getText();
                int start = propertyPsi.getTextOffset();
                int end = start + propertyPsi.getTextLength();
                propertyEntity.setSelected(false);
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
                document.insertString(insertionStart, " " + propertyEntity.getName() + "={}");
                propertyEntity.setSelected(true);
            }
            Utils.reformatText(psiElement.getTextOffset(), psiElement.getTextOffset() + psiElement.getTextLength());
        });
    }

  

    public List<PropertyEntity> getRequiredPropertyEntities() {
        List<PropertyEntity> entities = new ArrayList<>();
        for(PropertyEntity propertyEntity: propertyEntities) {
            if (propertyEntity.isRequired()) {
                entities.add(propertyEntity);
            }
        }
        return entities;
    }

    public List<PropertyEntity> getOptionalEntities() {
        List<PropertyEntity> entities = new ArrayList<>();
        for(PropertyEntity propertyEntity: propertyEntities) {
            if (!propertyEntity.isRequired()) {
                entities.add(propertyEntity);
            }
        }
        return entities;
    }

    public List<PropertyEntity> getPropertyEntities() {
        return new ArrayList<>(propertyEntities);
    }

    public PsiElement getPsiElement() {
        return psiElement;
    }
}
