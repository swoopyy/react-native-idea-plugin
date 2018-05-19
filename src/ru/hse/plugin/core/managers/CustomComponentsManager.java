package ru.hse.plugin.core.managers;

import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import ru.hse.plugin.core.entities.ProjectFile;
import ru.hse.plugin.core.utils.Utils;

import java.io.File;
import java.io.IOException;
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

    private String getComponentFrom(String psiElementText, List<String> components) {
        for (String component: components) {
            if (psiElementText.contains(component)) {
                return component;
            }
        }
        return null;
    }

    public void scanProject() {
        new Thread(new Runnable() {
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
                        psiFile.accept(new PsiRecursiveElementWalkingVisitor() {
                            @Override
                            public void visitElement(PsiElement element) {
                                String elementText = element.getText();
                                if (elementText.contains("class")
                                        && elementText.contains("Component")
                                        && !elementText.equals(psiFile.getText())) {
                                    PsiElement[] children = element.getChildren();
                                    for (int i = 0; i < children.length; ++i) {
                                        if (children[i].getText().contains("extends") && i != 0) {
                                            if (children[i - 1].getText().split(" ").length == 1) {
                                                components.add(children[i - 1].getText());
                                            }
                                        }
                                    }
                                }
                                if (elementText.contains("export")
                                        && !elementText.equals(psiFile.getText())) {
                                    String componentName = getComponentFrom(elementText, components);
                                    if (componentName != null) {
                                        if (elementText.contains("default")) {
                                            defaultlyExportedComponents.add(componentName);
                                        } else {
                                            exportedComponents.add(componentName);
                                        }
                                    }
                                }
                                super.visitElement(element);
                            }
                        });
                        projectFiles.add(new ProjectFile(
                                exportedComponents,
                                defaultlyExportedComponents,
                                path
                        ));
                    }
                    for (ProjectFile projectFile: projectFiles) {
                        System.out.println(projectFile.toString());
                    }
                }
        }).start();

    }
}
