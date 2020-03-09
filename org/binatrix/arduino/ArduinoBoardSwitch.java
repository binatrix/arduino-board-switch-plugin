package org.binatrix.arduino;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.*;
import java.awt.*;
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
 * @version (1.4)
 */
public class ArduinoBoardSwitch implements Tool {
    Editor editor;
    JScrollPane jlist;
    JList<CheckboxListItem> list;
    JLabel label2;
    JCheckBox check2;
    String boardFile;
    CheckboxListItem[] boards;
    private static final boolean ARDUINO = true;
    private static final String VERSION = "1.4";

    public void init(Editor editor) {
        this.editor = editor;
    }

    public String getMenuTitle() {
        return "Arduino Board Switch";
    }

    @SuppressWarnings("unchecked")
    public void run() {
        JDialog frame = new JDialog(editor, "Arduino Board Switch " + VERSION, true);
        frame.setPreferredSize(new Dimension(600, 400));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (ARDUINO) {
                        editor.statusNotice("Arduino Board Switch plugin closed");
                    }
                }
            });

        jlist = new JScrollPane();
        JList combo = new JList();

        JButton button1 = new JButton("Save changes");
        button1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (ARDUINO) {
                        saveBoard(boardFile);
                    }
                }
            });

        JCheckBox check1 = new JCheckBox("Check all/none");
        check1.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    int est = e.getStateChange();
                    int len = list.getModel().getSize();
                    for(int i=0; i<len; i++) {
                        CheckboxListItem item = (CheckboxListItem) list.getModel().getElementAt(i);
                        item.setSelected(est == 1);
                        list.repaint(list.getCellBounds(i, i));
                        checkBoard(item);
                    }
                }
            });

        check2 = new JCheckBox("Show visible/all");
        check2.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    loadList((Platform)combo.getSelectedValue());
                }
            });

        JLabel label1 = new JLabel("Platforms");
        label1.setFont(new Font(null, Font.BOLD, 18));
        label1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        label2 = new JLabel("Boards");
        label2.setFont(new Font(null, Font.BOLD, 18));
        label2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        Platform[] platforms = listPlatforms();
        DefaultListModel dlm = new DefaultListModel();
        for(Platform p: platforms) {
            dlm.addElement(p);
        }
        combo.setModel(dlm);
        combo.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 1) {
                        loadList((Platform)combo.getSelectedValue());
                    }
                }
            });
        combo.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        loadList((Platform)combo.getSelectedValue());
                    }
                }

                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                        loadList((Platform)combo.getSelectedValue());
                    }
                }
            });
        combo.setSelectedIndex(0);
        loadList((Platform)combo.getSelectedValue());
        /*
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
         */

        JPanel pane1 = new JPanel(new BorderLayout());
        pane1.add(label1, BorderLayout.NORTH);
        pane1.add(combo, BorderLayout.CENTER);

        JPanel pane3 = new JPanel(new BorderLayout());
        pane3.add(label2, BorderLayout.NORTH);
        pane3.add(check1, BorderLayout.WEST);
        pane3.add(check2, BorderLayout.EAST);

        JPanel pane2 = new JPanel(new BorderLayout());
        pane2.add(pane3, BorderLayout.NORTH);
        pane2.add(jlist, BorderLayout.CENTER);
        pane2.add(button1, BorderLayout.SOUTH);

        Container pane = frame.getContentPane();
        pane.add(pane1, BorderLayout.WEST);
        pane.add(pane2, BorderLayout.CENTER);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    CheckboxListItem[] getBoards(Platform platform) {
        java.util.List<CheckboxListItem> boards = new ArrayList<CheckboxListItem>();
        if (ARDUINO) {
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
                    boolean hidden = new Boolean(pref.get("hide", "false"));
                    boards.add(new CheckboxListItem(tag, name, !hidden));
                }
            }
            catch (Exception e)
            {
                editor.statusError(e.getMessage());
            }
        }
        else {
            boards.add(new CheckboxListItem("tag1", "Name 1", false));
            boards.add(new CheckboxListItem("tag2", "Name 2", true));
            boards.add(new CheckboxListItem("tag3", "Name 3", false));
        }
        return boards.toArray(new CheckboxListItem[boards.size()]);
    }

    void saveBoard(String file) {
        java.util.List<String> lines = new ArrayList<String>();

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

            for(int i = 0; i < boards.length; i++) {
                CheckboxListItem item = boards[i];
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
        java.util.List<Platform> files = new ArrayList<Platform>();
        if (ARDUINO) {
            for (TargetPackage targetPackage : BaseNoGui.packages.values()) {
                for (TargetPlatform targetPlatform : targetPackage.platforms()) {
                    File f = new File(targetPlatform.getFolder(), "boards.txt");
                    files.add(new Platform (targetPackage.getId(), targetPlatform.getId(), f.getAbsolutePath()));
                }
            } 
            editor.statusNotice("Found " + files.size() + " platforms");
        }
        else {
            files.add(new Platform ("aaa", "aaa", "c:\\path1\\path2\\path3\\boards.txt"));
            files.add(new Platform ("bbb", "bbb", "c:\\path1\\path2\\path4\\boards.txt"));
        }
        return files.toArray(new Platform[files.size()]);
    }

    @SuppressWarnings("unchecked")
    void loadList (Platform platform) {
        boards = getBoards(platform);
        java.util.List<CheckboxListItem> temp_array = new ArrayList<CheckboxListItem>();
        boolean visible = check2.isSelected();
        for(int i=0; i<boards.length; i++) {
            boolean hidden = !boards[i].isSelected();
            if ((visible && !hidden) || !visible) {
                temp_array.add(boards[i]);
            }
        }
        list = new JList<CheckboxListItem>(temp_array.toArray(new CheckboxListItem[temp_array.size()]));
        list.setCellRenderer(new CheckboxListRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent event) {
                    JList<CheckboxListItem> list = (JList<CheckboxListItem>) event.getSource();
                    int index = list.locationToIndex(event.getPoint());
                    CheckboxListItem item = (CheckboxListItem) list.getModel().getElementAt(index);
                    item.setSelected(!item.isSelected());
                    list.repaint(list.getCellBounds(index, index));
                    checkBoard(item);
                }
            });
        jlist.setViewportView(list);
        label2.setText("Boards for platform \"" + platform.getPackageId() + "\"");
    }

    void checkBoard(CheckboxListItem item) {
        for(int i=0; i<boards.length; i++) {
            if (boards[i].getTag().equals(item.getTag())) {
                boards[i].setSelected(item.isSelected());
            }
        }
    }

    public static void main () {
        new ArduinoBoardSwitch().run();
    }
}
