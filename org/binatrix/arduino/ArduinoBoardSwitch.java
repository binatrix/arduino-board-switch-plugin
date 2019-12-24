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

/**
 * Main arduino class implementing a new menu tool.
 * 
 * @author (Binatrix) 
 * @version (1.1)
 */
public class ArduinoBoardSwitch implements Tool {
    Editor editor;
    List<String> lines = new ArrayList<String>();
    List<CheckboxListItem> boards = new ArrayList<CheckboxListItem>();
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

        String[] platforms = listPlatforms();
        JComboBox combo = new JComboBox();
        for(String p: platforms) {
            combo.addItem(p);
        }
        combo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loadList(combo.getSelectedItem().toString());
                }
            });
        loadList(combo.getSelectedItem().toString());

        Container pane = frame.getContentPane();
        pane.add(combo, BorderLayout.PAGE_START);
        pane.add(jlist, BorderLayout.CENTER);
        pane.add(button, BorderLayout.PAGE_END);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    CheckboxListItem findBoardByTag(List<CheckboxListItem> boards, String tag) {
        for (CheckboxListItem b : boards) {
            if (b.getTag().equals(tag)) {
                return b;
            }
        }
        return null;
    }

    void getBoards(String file) {
        boards.clear();
        lines.clear();
        boardFile = file;
        editor.statusNotice("File " + file + " loaded");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.contains(".hide="))
                {
                    lines.add(line);
                }
                if (!line.startsWith("#") && !line.startsWith("menu")) {
                    int pos = line.indexOf(".");
                    if (pos > 0) {
                        String tag = line.substring(0, pos);
                        CheckboxListItem board = findBoardByTag(boards, tag);
                        if (board == null)
                        {
                            boards.add(new CheckboxListItem(tag, true));
                            board = findBoardByTag(boards, tag);
                        }
                        if (line.startsWith(board.getTag() + ".name"))
                        {
                            String value = line.substring(line.indexOf("=") + 1);
                            board.setLabel(value);
                        }
                        if (line.startsWith(board.getTag() + ".hide"))
                        {
                            board.setSelected(false);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            editor.statusError(e.getMessage());
        }
    }

    String[] getDirectories(String path) {
        File file = new File(path);
        String[] dirs = file.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File current, String name) {
                        return new File(current, name).isDirectory();
                    }
                });
        return dirs;
    }

    void saveBoard(String file) {
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
            editor.statusNotice("Changes saved!");
        }
        catch (Exception e)
        {
            editor.statusError(e.getMessage());
        }
    }

    String[] listPlatforms() {
        List<String> files = new ArrayList<String>();
        for (TargetPackage targetPackage : BaseNoGui.packages.values()) {
            for (TargetPlatform targetPlatform : targetPackage.platforms()) {
                String platformFolder = targetPlatform.getFolder().getAbsolutePath();
                File f = new File(platformFolder, "boards.txt");
                files.add(f.getAbsolutePath());

            }
        }  
        editor.statusNotice("Found " + files.size() + " platforms");
        return files.toArray(new String[files.size()]);
    }

    @SuppressWarnings("unchecked")
    void loadList (String file) {
        getBoards(file);

        list = new JList<CheckboxListItem>(boards.toArray(new CheckboxListItem[boards.size()]));
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

    public static void main() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ArduinoBoardSwitch s = new ArduinoBoardSwitch();
                    s.run();
                }
            });
    }
}
