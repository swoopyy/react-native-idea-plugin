package ru.hse.plugin.core;

import com.intellij.ui.components.JBLabel;
import ru.hse.plugin.ui.ComponentListItem;

import javax.swing.*;
import javax.swing.text.Document;

public class ComponentCellRenderer  implements ListCellRenderer<Component> {
    @Override
    public java.awt.Component getListCellRendererComponent(JList<? extends Component> list, Component value, int index, boolean isSelected, boolean cellHasFocus) {
        return new ComponentListItem(value).$$$getRootComponent$$$();
    }
}
