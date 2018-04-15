package ru.hse.plugin.ui;

import javax.swing.*;
import java.awt.*;

public class JMultilineLabel extends JTextArea {
    private static final long serialVersionUID = 1L;
    public JMultilineLabel(String text){
        super(text);
        setEditable(false);
        setCursor(null);
       // setRows(2);
        setOpaque(false);
        setFocusable(false);
     //   setWrapStyleWord(true);
      //  setLineWrap(true);
    }
}