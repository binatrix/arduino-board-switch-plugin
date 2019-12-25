package org.binatrix.arduino;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import processing.app.Editor;
import processing.app.tools.Tool;
import processing.app.BaseNoGui;
import processing.app.debug.TargetPackage;
import processing.app.debug.TargetPlatform;
import processing.app.debug.TargetBoard;
import processing.app.helpers.PreferencesMap;

/**
 * Main arduino class implementing a new menu tool.
 * 
 * @author (Binatrix) 
 * @version (1.2)
 */
public class ArduinoBoardSwitch implements Tool {
    Editor editor;
    JScrollPane jlist;
    JList<CheckboxListItem> list;
    String boardFile;

    public void init(Editor editor) {
        this.editor = editor;
    }

    public String getMenuTitle() {
        return "Arduino Board Switch";
    }

    @SuppressWarnings("unchecked")
    public void run() {
        JDialog frame = new JDialog(editor, "Arduino Board Switch", true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    editor.statusNotice("Arduino Board Switch plugin closed");
                }
            });

        jlist = new JScrollPane();

        JButton button = new JButton("Save changes");
        button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    saveBoard(boardFile);
                }
            });

        Platform[] platforms = listPlatforms();
        JComboBox combo = new JComboBox();
        for(Platform p: platforms) {
            combo.addItem(p);
        }
        combo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loadList((Platform)combo.getSelectedItem());
                }
            });
        loadList((Platform)combo.getSelectedItem());

        Container pane = frame.getContentPane();
        pane.add(combo, BorderLayout.PAGE_START);
        pane.add(jlist, BorderLayout.CENTER);
        pane.add(button, BorderLayout.PAGE_END);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    CheckboxListItem[] getBoards(Platform platform) {
        List<CheckboxListItem> boards = new ArrayList<CheckboxListItem>();
        boardFile = platform.getPath();
        editor.statusNotice("File " + boardFile + " loaded");

        try {
            TargetPackage targetPackage = BaseNoGui.getTargetPackage(platform.getPackageId());
            if (targetPackage == null) 
                throw new Exception("Package " + targetPackage.getId() + " not found");
            TargetPlatform targetPlatform = targetPackage.get(platform.getId());
            if (targetPlatform == null) 
                throw new Exception("Platform " + platform.getId() + " not found");
            for (TargetBoard board : targetPlatform.getBoards().values()) {
                String tag = board.getId();
                String name = board.getName();
                PreferencesMap pref = board.getPreferences();
                boolean visible = new Boolean(pref.get("hide", "false"));
                boards.add(new CheckboxListItem(tag, name, !visible));
            }
        }
        catch (Exception e)
        {
            editor.statusError(e.getMessage());
        }
        return boards.toArray(new CheckboxListItem[boards.size()]);
    }

    void saveBoard(String file) {
        List<String> lines = new ArrayList<String>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.contains(".hide="))
                {
                    lines.add(line);
                }
            }
        }
        catch (Exception e)
        {
            editor.statusError(e.getMessage());
        }

        try(FileWriter w = new FileWriter(file)) {
            for (String line :lines)
            {
                w.write(line + "\n");
            }

            for(int i = 0; i < list.getModel().getSize(); i++) {
                CheckboxListItem item = list.getModel().getElementAt(i);
                if (item.isSelected() == false) {
                    w.write(item.getTag() + ".hide=true\n");
                }
            }

            w.close();
            editor.statusNotice("Changes saved! You must restart Arduino to changes take effect");
        }
        catch (Exception e)
        {
            editor.statusError(e.getMessage());
        }
    }

    Platform[] listPlatforms() {
        List<Platform> files = new ArrayList<Platform>();
        for (TargetPackage targetPackage : BaseNoGui.packages.values()) {
            for (TargetPlatform targetPlatform : targetPackage.platforms()) {
                File f = new File(targetPlatform.getFolder(), "boards.txt");
                files.add(new Platform (targetPackage.getId(), targetPlatform.getId(), f.getAbsolutePath()));
            }
        }  
        editor.statusNotice("Found " + files.size() + " platforms");
        return files.toArray(new Platform[files.size()]);
    }

    @SuppressWarnings("unchecked")
    void loadList (Platform platform) {
        CheckboxListItem[] boards = getBoards(platform);
        list = new JList<CheckboxListItem>(boards);
        list.setCellRenderer(new CheckboxListRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent event) {
                    JList<CheckboxListItem> list = (JList<CheckboxListItem>) event.getSource();
                    int index = list.locationToIndex(event.getPoint());
                    CheckboxListItem item = (CheckboxListItem) list.getModel().getElementAt(index);
                    item.setSelected(!item.isSelected());
                    list.repaint(list.getCellBounds(index, index));
                }
            });
        jlist.setViewportView(list);
    }
}
