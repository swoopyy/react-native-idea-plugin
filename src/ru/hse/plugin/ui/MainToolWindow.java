package ru.hse.plugin.ui;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jetbrains.annotations.NotNull;
import ru.hse.plugin.core.callbacks.ComponentClicked;
import ru.hse.plugin.core.callbacks.ProjectScanned;
import ru.hse.plugin.core.entities.*;
import ru.hse.plugin.core.entities.Component;
import ru.hse.plugin.core.managers.CustomComponentsManager;
import ru.hse.plugin.core.managers.Inserter;
import ru.hse.plugin.core.managers.InsertionManager;
import ru.hse.plugin.core.callbacks.SnippetInserted;
import ru.hse.plugin.core.managers.PropertiesInspectorManager;
import ru.hse.plugin.core.utils.Utils;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;

public class MainToolWindow implements ToolWindowFactory {
    private ToolWindow mainToolWindow;
    private JPanel contentPanel;
    private JTabbedPane tabbedPane1;
    private JPanel builtInPanel;
    private JTextField searchField;
    private JLabel searchLabel;
    private JPanel propertiesPanel;
    private JPanel customPanel;
    private ComponentEntity componentEntity;
    private DefaultListModel[] models = new DefaultListModel[]{
            new DefaultListModel<Component>(),
            new DefaultListModel<Component>(),
    };
    private Component[] builtinComponents;
    private Component[] customComponents; // initialized asynchronously
    private InsertionManager insertionManager;
    private CustomComponentsManager customComponentsManager;
    private PropertiesInspector propertiesInspector;

    public MainToolWindow() {
        builtinComponents = ComponentCollection.getBuiltinComponents();
        customComponents = new Component[0];
        insertionManager = InsertionManager.getInstance();
        customComponentsManager = CustomComponentsManager.getInstance();
        propertiesInspector = new PropertiesInspector();
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        mainToolWindow = toolWindow;
        propertiesPanel.setLayout(new BoxLayout(propertiesPanel, BoxLayout.Y_AXIS));
        this.createUIComponents();
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(contentPanel, "", false);
        toolWindow.getContentManager().addContent(content);
        propertiesPanel.add(propertiesInspector.$$$getRootComponent$$$());
        initPropertiesInspectorHandler();
        rescanForCustomComponents();
    }

    private void initPropertiesInspectorHandler() {
        PropertiesInspectorManager.setHandler(new ComponentClicked() {
            @Override
            public void perform(PsiElement element, Component component) {
                if (component != null && (componentEntity == null || !componentEntity.getPsiElement().equals(element))) {
                    componentEntity = new ComponentEntity(component, element);
                    propertiesInspector.setComponentEntity(componentEntity);
                } else if (component == null) {
                    propertiesInspector.clear();
                }
            }
        });
    }

    private void rescanForCustomComponents() {
        customComponentsManager.scanProject(new ProjectScanned() {
            @Override
            public void perform() {
                customComponents = customComponentsManager.components(); //customComponentsManager.components();
                refillModel(1, "");
            }
        });
    }

    private void refillModel(int index, String searchTerm) {
        DefaultListModel<Component> model = models[index];
        System.out.println("Index " + index);
        System.out.println("Custom components length " + customComponents.length);
        model.clear();
        if (index == 0) {
            for (Component component : builtinComponents) {
                model.addElement(component);
            }
        }
        if (index == 1) {
            for (Component component : customComponents) {
                if (component.meets(searchTerm)) {
                    model.addElement(component);
                }
            }
        }
    }

    private void updateTabPanel(JBList jbList, JPanel jPanel) {
        if (jPanel.getComponentCount() > 0) {
            jPanel.remove(0);
        }
        jPanel.add(new JBScrollPane(jbList), BorderLayout.CENTER);
    }

    private void updateTabView() {
        DefaultListModel model = models[tabbedPane1.getSelectedIndex()];
        JBList jbList = new JBList(model);
        jbList.setCellRenderer(new ComponentCellRenderer());
        jbList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (jbList.getSelectedIndex() != -1) {
                    Component component = (Component) model.getElementAt(jbList.getSelectedIndex());
                    SelectionModel selectionModel = insertionManager.getEditor().getSelectionModel();
                    if (selectionModel.hasSelection()) {
                        int start = selectionModel.getSelectionStart();
                        int end = selectionModel.getSelectionEnd();
                        WriteCommandAction.runWriteCommandAction(insertionManager.getProject(), () -> {
                            Document document = insertionManager.getEditor().getDocument();
                            String text = document.getText().substring(start, end);
                            String toInsert = component.wrapSnippet(text);
                            document.replaceString(start, end, toInsert);
                            PsiDocumentManager.getInstance(insertionManager.getProject()).commitDocument(insertionManager.getEditor().getDocument());
                            Utils.reformatText(start, start + toInsert.length());
                            new Inserter(insertionManager.getProject(), insertionManager.getEditor(), component).insertImportStatement();
                            selectionModel.removeSelection();
                        });
                        jbList.clearSelection();
                        insertionManager.clear();
                        return;
                    }
                    insertionManager.setComponent(component, new SnippetInserted() {
                        @Override
                        public void perform() {
                            jbList.clearSelection();
                            insertionManager.clear();
                        }
                    });
                }
            }
        });
        switch (tabbedPane1.getSelectedIndex()) {
            case 0:
                updateTabPanel(jbList, builtInPanel);
                break;
            case 1:
                updateTabPanel(jbList, customPanel);
                break;

        }
    }

    private void createUIComponents() {
        refillModel(0, "");
        refillModel(1, "");
        updateTabView();
        searchField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent documentEvent) {
                refillModel(tabbedPane1.getSelectedIndex(), searchField.getText());
                updateTabView();
            }
        });
        tabbedPane1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateTabView();
            }
        });
    }


    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayoutManager(2, 1, new Insets(4, 4, 0, 4), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 2, new Insets(0, 5, 0, 0), -1, -1));
        panel1.setBackground(new Color(-1250068));
        contentPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 250), null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        tabbedPane1 = new JTabbedPane();
        panel1.add(tabbedPane1, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        builtInPanel = new JPanel();
        builtInPanel.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab("Built in", builtInPanel);
        customPanel = new JPanel();
        customPanel.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab("Custom", customPanel);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 10, 0, 5), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        searchLabel = new JLabel();
        searchLabel.setBackground(new Color(-1));
        searchLabel.setText("Search");
        panel2.add(searchLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        searchField = new JTextField();
        panel2.add(searchField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        propertiesPanel = new JPanel();
        propertiesPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        contentPanel.add(propertiesPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 200), null, new Dimension(-1, 250), 1, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }
}
