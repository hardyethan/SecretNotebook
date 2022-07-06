package git.hardyethan.secretnotebook.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import git.hardyethan.secretnotebook.encryption.AES256;
import git.hardyethan.secretnotebook.encryption.EncryptionResponse;
import git.hardyethan.secretnotebook.storage.Database;

//Adapted from https://gist.github.com/6footGeek/e990d3f6177625012124
public class GUI extends JFrame {

    Database database;
    ArrayList<JButton> viewButtons, editButtons, deleteButtons;
    ArrayList<JLabel> titles;

    public GUI(String pathString) {

        database = new Database(pathString);

        this.init(null); // init all our things!

        // set window object size
        this.setSize(800, 450);
        this.setTitle("Secret Notebook by Ethan Hardy");
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private String getPassword() {
        return getPassword("Enter Document Password");
    }

    private String getPassword(String prompt) {
        // https://stackoverflow.com/a/8881370
        JFrame passwordFrame = new JFrame();
        JPasswordField pf = new JPasswordField();
        int okOrCancelled = JOptionPane.showConfirmDialog(passwordFrame, pf, prompt,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (okOrCancelled != JOptionPane.OK_OPTION)
            System.exit(0);

        return new String(pf.getPassword());
    }

    public void init(String password) {

        // create container to hold GUI in window
        Container pane = this.getContentPane();
        pane.removeAll();
        pane.revalidate();
        pane.repaint();
        titles = new ArrayList<JLabel>();
        viewButtons = new ArrayList<JButton>();
        editButtons = new ArrayList<JButton>();
        deleteButtons = new ArrayList<JButton>();
        pane.setLayout(null);

        String globPassword;
        if (password == null)
            globPassword = getPassword();
        else
            globPassword = password;

        HashMap<String, String> entryTitles = database.getEntryTitles(globPassword);

        JButton newEntryButton = new JButton("New");
        newEntryButton.setBounds(10, 10, 80, 20);
        newEntryButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                JFrame jFrame = new JFrame();
                String newTitle = JOptionPane.showInputDialog(jFrame, "Title");
                if (newTitle == null)
                    return;
                if (entryTitles.values().contains(newTitle)) {
                    JOptionPane.showMessageDialog(new JFrame(),
                            "That title has already been used, if you would like to use it, delete the old one.");
                    actionPerformed(e);
                    return;
                }
                JTextArea textArea = new JTextArea();
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);

                JScrollPane scrollPane = new JScrollPane(textArea);

                scrollPane.setPreferredSize(new Dimension(700, 350));

                JOptionPane.showMessageDialog(new JFrame(), scrollPane,
                        "Message", JOptionPane.INFORMATION_MESSAGE);

                String newMessage = textArea.getText();

                EncryptionResponse titleResponse = AES256.encrypt(newTitle, globPassword);
                EncryptionResponse messageResponse = AES256.encrypt(newMessage, globPassword);
                database.addEntry(titleResponse, messageResponse);

                init(globPassword);
            }
        });
        pane.add(newEntryButton);

        JButton reloadButton = new JButton("Change password in use");
        reloadButton.setBounds(10 + 20 + 80, 10, 280, 20);
        reloadButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int okOrCancelled = JOptionPane.showConfirmDialog(new JFrame(),
                        "This will not change the password used for past documents, only the password of future documents. Would you like to continue?",
                        "Confirm",
                        JOptionPane.YES_NO_OPTION);
                if (okOrCancelled == JOptionPane.NO_OPTION)
                    return;

                init(null);
            }
        });
        pane.add(reloadButton);

        for (String entryID : entryTitles.keySet()) {
            JLabel titleLabel = new JLabel(entryTitles.get(entryID));
            titles.add(titleLabel);
        }

        // https://stackoverflow.com/a/258499
        for (int i = 0; i < titles.size(); i++) {
            JLabel titleLabel = titles.get(i);
            titleLabel.setBounds(10, 40 + (30 * i), 300,
                    20);
            pane.add(titleLabel);
        }

        // View Button
        for (String entryID : entryTitles.keySet()) {
            JButton viewButton = new JButton("View");
            viewButton.setName(entryID);
            // https://stackoverflow.com/a/891397
            viewButton.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    JTextArea textArea = new JTextArea(database.getEntry(entryID, globPassword));
                    textArea.setLineWrap(true);
                    textArea.setWrapStyleWord(true);

                    JScrollPane scrollPane = new JScrollPane(textArea);

                    scrollPane.setPreferredSize(new Dimension(700, 350));

                    JOptionPane.showMessageDialog(new JFrame(), scrollPane,
                            entryTitles.get(entryID), JOptionPane.INFORMATION_MESSAGE);
                }
            });
            viewButtons.add(viewButton);
        }

        for (int i = 0; i < viewButtons.size(); i++) {
            JButton entryButton = viewButtons.get(i);
            entryButton.setBounds(300 + 20, 40 + (30 * i),
                    80, 20);
            pane.add(entryButton);
        }

        // Edit Button
        for (String entryID : entryTitles.keySet()) {
            JButton editButton = new JButton("Edit");
            editButton.setName(entryID);
            // https://stackoverflow.com/a/891397
            editButton.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    String currEntryMessage = database.getEntry(entryID, globPassword);
                    JTextArea textArea = new JTextArea(currEntryMessage);
                    textArea.setLineWrap(true);
                    textArea.setWrapStyleWord(true);

                    JScrollPane scrollPane = new JScrollPane(textArea);

                    scrollPane.setPreferredSize(new Dimension(700, 350));

                    JOptionPane.showMessageDialog(new JFrame(), scrollPane,
                            entryTitles.get(entryID), JOptionPane.INFORMATION_MESSAGE);

                    String newMessage = textArea.getText();
                    if (!newMessage.equals(currEntryMessage)) {
                        database.setEntry(entryID, newMessage, globPassword);
                    }
                }
            });
            editButtons.add(editButton);
        }

        for (int i = 0; i < editButtons.size(); i++) {
            JButton editButton = editButtons.get(i);
            editButton.setBounds(300 + 20 + 80 + 20, 40 + (30 * i),
                    80, 20);
            pane.add(editButton);
        }

        // Delete Button
        for (String entryID : entryTitles.keySet()) {
            JButton deleteButton = new JButton("Delete");
            deleteButton.setName(entryID);
            // https://stackoverflow.com/a/891397
            deleteButton.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    database.deleteEntry(entryID);
                    init(globPassword);
                }
            });
            deleteButtons.add(deleteButton);
        }

        for (int i = 0; i < deleteButtons.size(); i++) {
            JButton deleteButton = deleteButtons.get(i);
            deleteButton.setBounds(300 + 20 + 80 + 20 + 80 + 20, 40 + (30 * i),
                    120, 20);
            pane.add(deleteButton);
        }

        pane.revalidate();
        pane.repaint();
    }
}
