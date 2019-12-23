package org.binatrix.arduino;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
 * Write a description of class ArduinoBoardsManager here.
 * 
 * @author (Binatrix) 
 * @version (ALFA-1)
 */
public class ArduinoBoardSwitch implements Tool {
    Editor editor;
    List<String> lines = new ArrayList<String>();
    List<BoardInfo> boards = new ArrayList<BoardInfo>();
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
        JFrame frame = new JFrame("Arduino Board Switch");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        jlist = new JScrollPane();

        JButton button = new JButton("Save changes");
        button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    saveBoard(boardFile);
                    MessageBox("Changes saved");
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

    void MessageBox (String message) {
        JOptionPane.showMessageDialog(editor, message);
    }

    BoardInfo findBoardByTag(List<BoardInfo> boards, String tag) {
        for (BoardInfo b : boards) {
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
                        BoardInfo board = findBoardByTag(boards, tag);
                        if (board == null)
                        {
                            boards.add(new BoardInfo(tag, true));
                            board = findBoardByTag(boards, tag);
                        }
                        if (line.startsWith(board.getTag() + ".name"))
                        {
                            String value = line.substring(line.indexOf("=") + 1);
                            board.setName(value);
                        }
                        if (line.startsWith(board.getTag() + ".hide"))
                        {
                            board.setVisible(false);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            MessageBox(e.getMessage());
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
        }
        catch (Exception e)
        {
            MessageBox(e.getMessage());
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
        return files.toArray(new String[files.size()]);
    }

    @SuppressWarnings("unchecked")
    void loadList (String file) {
        getBoards(file);
        List<CheckboxListItem> items = new ArrayList<CheckboxListItem>();
        for(BoardInfo b: boards) {
            items.add(new CheckboxListItem(b.getName(), b.getTag(), b.getVisible()));
        }

        list = new JList<CheckboxListItem>(items.toArray(new CheckboxListItem[items.size()]));
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

    public static void main(String args[]) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ArduinoBoardSwitch s = new ArduinoBoardSwitch();
                    s.run();
                }
            });
    }
}

class CheckboxListItem {
    private String label;
    private String tag;
    private boolean isSelected = false;

    public CheckboxListItem(String label, String tag, boolean isSelected) {
        this.label = label;
        this.tag = tag;
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public String getTag() {
        return tag;
    }

    public String toString() {
        return label;
    }
}

class CheckboxListRenderer extends JCheckBox implements ListCellRenderer<CheckboxListItem> {

    @Override
    public Component getListCellRendererComponent(
    JList<? extends CheckboxListItem> list, CheckboxListItem value,
    int index, boolean isSelected, boolean cellHasFocus) {
        setEnabled(list.isEnabled());
        setSelected(value.isSelected());
        setFont(list.getFont());
        setBackground(list.getBackground());
        setForeground(list.getForeground());
        setText(value.toString());
        return this;
    }
}

class BoardInfo {
    private String tag;
    private String name;
    private boolean visible = false;

    public BoardInfo(String tag, boolean visible) {
        this.tag = tag;
        this.visible = visible;
    }

    public void setTag (String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return this.tag;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getName () {
        return this.name;
    }

    public void setVisible (boolean visible) {
        this.visible = visible;
    }

    public boolean getVisible () {
        return this.visible;
    }
}