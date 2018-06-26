package ru.hse.plugin.ui;

import com.intellij.ui.components.JBLabel;
import ru.hse.plugin.core.entities.Component;
import ru.hse.plugin.core.entities.Platform;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ComponentCellRenderer  implements ListCellRenderer<Component> {
    private FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    private ImageIcon reactIcon = new ImageIcon(ComponentCellRenderer.class.getResource("/icons/react.png"));
    private ImageIcon androidIcon = new ImageIcon(ComponentCellRenderer.class.getResource("/icons/android.png"));
    private ImageIcon appleIcon = new ImageIcon(ComponentCellRenderer.class.getResource("/icons/apple.png"));
    private ImageIcon androidIosIcon = new ImageIcon(ComponentCellRenderer.class.getResource("/icons/android-ios.png"));

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
            label.setBackground(list.getSelectionBackground());
            label.setForeground(list.getSelectionForeground());
        } else {
            label.setForeground(list.getForeground());
            label.setBackground(list.getBackground());
        }
        return label;
    }
}
