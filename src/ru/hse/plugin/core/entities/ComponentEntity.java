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



    public boolean hasProperty(Property property) {
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

    public String getPropertyValue(Property property) {
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

    public PsiElement getPropertyContainingPsiElement(Property property) {
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
                    propertyEntities.add(
                            new PropertyEntity(
                                    property,
                                    this,
                                    value.replaceAll("'", ""),
                                    true
                            )
                    );
                } else if (value.contains("\'")) {
                    propertyEntities.add(
                            new PropertyEntity(
                                    property,
                                    this,
                                    value.replaceAll("\"", ""),
                                    true
                            )
                    );
                } else if (value.equals("true") || value.equals("false")) {
                    propertyEntities.add(
                            new PropertyEntity(
                                    property,
                                    this,
                                    Boolean.parseBoolean(value),
                                    true
                            )
                    );
                } else if (value.matches("-?\\d+(\\.\\d+)?")) {
                    propertyEntities.add(
                            new PropertyEntity(
                                    property,
                                    this,
                                    Double.parseDouble(value),
                                    true
                            )
                    );
                } else {
                    propertyEntities.add(
                            new PropertyEntity(
                                    property,
                                    this,
                                    null,
                                    true
                            )
                    );
                }
            } else {
                propertyEntities.add(
                        new PropertyEntity(
                                property,
                                this,
                                null,
                                false
                        )
                );
            }
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
