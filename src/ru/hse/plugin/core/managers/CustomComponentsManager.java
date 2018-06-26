package ru.hse.plugin.core.managers;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import ru.hse.plugin.core.callbacks.ProjectScanned;
import ru.hse.plugin.core.entities.Component;
import ru.hse.plugin.core.entities.ProjectFile;
import ru.hse.plugin.core.entities.Property;
import ru.hse.plugin.core.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomComponentsManager  {
    private static EditorManager editorManager = new EditorManager();
    private static CustomComponentsManager instance = null;
    private List<ProjectFile> projectFiles;

    private CustomComponentsManager() {
        projectFiles = new ArrayList<>();
    }

    public synchronized static CustomComponentsManager getInstance() {
        if (instance == null) {
            instance = new CustomComponentsManager();
        }
        return instance;
    }

    public List<ProjectFile> getProjectFiles() {
        return projectFiles;
    }

    public void scanProject(ProjectScanned projectScanned) {
        projectFiles = new ArrayList<ProjectFile>();
        ApplicationManager.getApplication().runReadAction((new Runnable() {
            @Override
            public void run()  {
                    List<String> paths;
                    try {
                        paths = Utils.getAllExportingFiles(editorManager.getProject().getBasePath());
                    } catch (Exception ex) {
                        return;
                    }
                    for (String path : paths) {
                        VirtualFile file = LocalFileSystem.getInstance().findFileByIoFile(new File(path));
                        PsiFile psiFile = PsiManager.getInstance(editorManager.getProject()).findFile(file);
                        List<String> components = new ArrayList<>();
                        List<String> exportedComponents = new ArrayList<>();
                        List<String> defaultlyExportedComponents = new ArrayList<>();
                        try {
                            psiFile.accept(new PsiRecursiveElementWalkingVisitor() {
                                @Override
                                public void visitElement(PsiElement element) {
                                    if (element.toString().contains("ES6Class")) {
                                        for (PsiElement child : element.getChildren()) {
                                            if (child.toString().equals("JSReferenceExpression")) {
                                                components.add(child.getText());
                                                if (element.getParent().toString().equals("ES6ExportDefaultAssignment")) {
                                                    if (defaultlyExportedComponents.size() == 1) {
                                                        defaultlyExportedComponents.clear();
                                                    }
                                                    defaultlyExportedComponents.add(child.getText());
                                                } else if (element.getParent().getText().indexOf("export") == 0 ||
                                                            element.getText().indexOf("export") == 0) {
                                                    exportedComponents.add(child.getText());
                                                }

                                            }
                                        }
                                    }
                                    if (element.toString().equals("ES6ExportDefaultAssignment")) {
                                        if (defaultlyExportedComponents.size() == 0) {
                                            String fileName = psiFile.getName().substring(0, psiFile.getName().indexOf('.'));
                                            defaultlyExportedComponents.add(fileName);
                                        }
                                    }
                                    if (element.toString().equals("ES6ExportSpecifier")) {
                                        String component = element.getText();
                                        if (components.contains(component) && !exportedComponents.contains(component)) {
                                            exportedComponents.add(component);
                                        }
                                    }
                                    super.visitElement(element);
                                }
                            });
                        } catch(NullPointerException ex) {
                            continue;
                        }
                        if (components.size() == 0) {
                            defaultlyExportedComponents.clear();
                        }
                        projectFiles.add(new ProjectFile(
                                exportedComponents,
                                defaultlyExportedComponents,
                                path
                        ));
                    }
                    projectScanned.perform();
                }
        }));
    }

    public Component[] components() {
        List<Component> list = new ArrayList<>();
        for(ProjectFile file: projectFiles) {
            for(String component: file.getExportedComponents()) {
                Component component1 = new Component(component, file.getPath(), new Property[0], false);
                component1.setCustom(true);
                list.add(component1);
            }
            for(String component: file.getDefaultlyExportedComponents()) {
                Component component1 = new Component(component, file.getPath(), new Property[0], true);
                component1.setCustom(true);
                list.add(component1);
            }
        }
        return list.toArray(new Component[0]);
    }
}
