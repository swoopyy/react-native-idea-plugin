package ru.hse.plugin.core;

import com.intellij.ui.components.JBLabel;
import ru.hse.plugin.ui.ComponentListItem;

import javax.swing.*;
import javax.swing.text.Document;

public class ComponentCellRenderer  implements ListCellRenderer<Component> {
    @Override
    public java.awt.Component getListCellRendererComponent(JList<? extends Component> list, Component value, int index, boolean isSelected, boolean cellHasFocus) {
        ComponentListItem item =  new ComponentListItem(value);
        if (isSelected) {
            item.getPanel2().setBackground(list.getSelectionBackground());
            item.getPanel2().setForeground(list.getSelectionForeground());
        } else {
            item.getPanel2().setForeground(list.getForeground());
            item.getPanel2().setBackground(list.getBackground());
        }
        return item.$$$getRootComponent$$$();
    }
}
