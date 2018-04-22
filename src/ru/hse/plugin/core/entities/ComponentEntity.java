package ru.hse.plugin.core.entities;

import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import ru.hse.plugin.core.managers.EditorManager;
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
                if (element.equals(property.getName())) {
                    hasProperty[0] = true;
                }
                super.visitElement(element);
            }
        });
        return hasProperty[0];
    }

    private String getPropertyValue(Property property) {
        final String[] propertyValue = {""};
        psiElement.accept(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                String text = element.getText();
                if (text.contains(property.getName()) && !text.equals(property.getName())) {
                    if (text.split("\"").length == 3) {
                        propertyValue[0] =  text.split("\"")[1];
                    } else if (text.split("'").length == 3) {
                        propertyValue[0] = text.split("'")[1];
                    } else if (text.indexOf("{") != -1 && text.indexOf("}") != -1) {
                        int start = text.indexOf("{");
                        int end = text.indexOf("}");
                        propertyValue[0] = text.substring(start + 1, end);
                    }
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
        for(PropertyEntity propertyEntity: propertyEntities) {
            System.out.println(propertyEntity.getName());
        }
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
