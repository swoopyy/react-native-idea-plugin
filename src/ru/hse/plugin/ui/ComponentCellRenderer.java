package ru.hse.plugin.ui;

import com.intellij.ui.Colors;
import com.intellij.ui.components.JBLabel;
import ru.hse.plugin.core.entities.Component;
import ru.hse.plugin.core.entities.Platform;

import javax.swing.*;
import java.awt.*;

public class ComponentCellRenderer  implements ListCellRenderer<Component> {
    private ImageIcon reactIcon = new ImageIcon(ComponentCellRenderer.class.getResource("/icons/react.png"));
    private ImageIcon androidIcon = new ImageIcon(ComponentCellRenderer.class.getResource("/icons/android.png"));
    private ImageIcon appleIcon = new ImageIcon(ComponentCellRenderer.class.getResource("/icons/apple.png"));

    @Override
    public java.awt.Component getListCellRendererComponent(JList<? extends Component> list, Component value, int index, boolean isSelected, boolean cellHasFocus) {
        JBLabel label = new JBLabel();
        label.setText(value.getName());
        label.setToolTipText(value.getDescription());
        if (value.getPlatform() == Platform.IOS) {
            label.setIcon(appleIcon);
        }
        if (value.getPlatform() == Platform.ANDROID) {
            label.setIcon(androidIcon);
        }
        if (value.getPlatform() == Platform.BOTH) {
            label.setIcon(reactIcon);
        }
        if (isSelected) {
            label.setForeground(Colors.DARK_RED);
            label.setBackground(Colors.DARK_RED);
        } else {
            label.setForeground(list.getForeground());
            label.setBackground(list.getBackground());
        }
        return label;
    }
}
