package ru.hse.plugin.ui;

import javax.swing.*;

public class JMultilineLabel extends JTextArea {
    private static final long serialVersionUID = 1L;
    public JMultilineLabel(String text){
        super(text);
        setEditable(false);
        setCursor(null);
     //   setOpaque(false);
        setFocusable(false);
        setWrapStyleWord(true);
        setLineWrap(true);
    }
}