// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2006 by R. Pito Salas
//
// This program is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free Software Foundation;
// either version 2 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
// without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with this program;
// if not, write to the Free Software Foundation, Inc., 59 Temple Place,
// Suite 330, Boston, MA 02111-1307 USA
//
// Contact: R. Pito Salas
// mailto:pitosalas@users.sourceforge.net
// More information: about BlogBridge
// http://www.blogbridge.com
// http://sourceforge.net/projects/blogbridge
//
// $Id: SendFeedbackDialog.java,v 1.17 2006/11/14 13:19:47 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.util.Resizer;
import com.salas.bb.service.ServerService;
import com.salas.bb.service.ServerServiceException;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.utils.uif.HeaderPanelExt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;
import java.util.List;

/**
 * Dialog for entering a feedback.
 */
public class SendFeedbackDialog extends AbstractDialog
{
    private static final String THREAD_NAME = "Forums Loader";

    private JTextArea   wording;
    private JComboBox   cbForum;
    private JTextField  tfSubject;
    private JTextArea   taMessage;
    private JTextField  tfName;
    private JTextField  tfEmail;

    private JButton     btnReload;
    private JButton     btnSend;

    private static String   oldUserName;
    private static String   oldEmail;
    private static int      oldForumId;
    private static String   oldSubject;
    private static String   oldMessage;

    private static ForumEntry[] forumsList;

    /**
     * Constructs dialog box.
     *
     * @param owner the dialog's parent frame
     */
    public SendFeedbackDialog(Frame owner)
    {
        super(owner);

        setTitle(Strings.message("sendfeedback.dialog.title"));
        initComponents();
    }

    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("sendfeedback.dialog.title"),
            Strings.message("sendfeedback.dialog.header"));
    }

    /**
     * Builds contents.
     *
     * @return the dialog's main content without header and border
     */
    protected JComponent buildContent()
    {
        JPanel content = new JPanel(new BorderLayout());

        content.add(createMainPanel(), BorderLayout.CENTER);
        content.add(buildButtonsBar(),  BorderLayout.SOUTH);

        return content;
    }

    private JComponent buildButtonsBar()
    {
        return ButtonBarFactory.buildRightAlignedBar(btnSend, createCancelButton());
    }

    // Creates main panel.
    private Component createMainPanel()
    {
        JScrollPane pane = new JScrollPane(taMessage);
        pane.setPreferredSize(new Dimension(500, 20));

        BBFormBuilder builder = new BBFormBuilder("p, 2dlu, max(p;100dlu), 4dlu, p, 2dlu, max(p;150dlu), 0:grow");

        builder.append(wording, 8);
        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(Strings.message("sendfeedback.forum"), cbForum, btnReload);
        builder.nextLine();
        builder.append(Strings.message("sendfeedback.name"), tfName);
        builder.append(Strings.message("sendfeedback.email"), tfEmail, 2);
        builder.append(Strings.message("sendfeedback.subject"), tfSubject, 6);
        builder.append(Strings.message("sendfeedback.message"), 1).setLabelFor(taMessage);
        builder.appendRelatedComponentsGapRow(2);
        builder.appendRow("pref:grow");
        builder.append(pane, 8, CellConstraints.FILL, CellConstraints.FILL);
        builder.append(Strings.message("sendfeedback.notice"), 7);

        return builder.getPanel();
    }

    /** Initializes components. */
    private void initComponents()
    {
        btnSend = createAcceptButton(Strings.message("sendfeedback.send"), true);

        cbForum = new JComboBox();
        cbForum.setEnabled(false);
        cbForum.addItemListener(new ForumSelectionListener());

        taMessage = new JTextArea();
        taMessage.setWrapStyleWord(true);
        taMessage.setLineWrap(true);

        tfSubject = new JTextField();

        wording = ComponentsFactory.createWrappedMultilineLabel(Strings.message("sendfeedback.wording"));

        tfName = new JTextField();
        tfEmail = new JTextField();

        btnReload = new JButton(new ReloadForumsListAction());
    }

    /**
     * Clears text and opens dialog.
     *
     * @param aName     name of the user according to the preferences.
     * @param aEmail    email of the user according to the preferences.
     */
    public void open(String aName, String aEmail)
    {
        if (taMessage != null)
        {
            tfSubject.setText(oldSubject);
            taMessage.setText(oldMessage);
            tfName.setText(oldUserName != null ? oldUserName : aName);
            tfEmail.setText(oldEmail != null ? oldEmail : aEmail);

            setupForums();
        }

        super.open();

        if (!hasBeenCanceled())
        {
            oldUserName = getFullName();
            oldEmail = getEmail();
            oldForumId = getForumId();
            oldSubject = getSubject();
            oldMessage = getMessage();
        }
    }

    /**
     * Setups the list of forums.
     */
    private void setupForums()
    {
        if (forumsList != null)
        {
            popuplateForumsBox();
            selectForum(oldForumId);
        } else
        {
            loadForumsList();
        }
    }

    /**
     * Selects given forum if the last is in the list.
     *
     * @param aForumId forum to select.
     */
    private void selectForum(int aForumId)
    {
        if (aForumId == -1 || forumsList == null) return;

        ForumEntry entry = null;
        for (int i = 0; entry == null && i < forumsList.length; i++)
        {
            ForumEntry forumEntry = forumsList[i];
            if (forumEntry.getId() == aForumId) entry = forumEntry;
        }

        if (entry != null) cbForum.setSelectedItem(entry);
    }

    /** Loads the list of forums available. */
    private void loadForumsList()
    {
        btnReload.setEnabled(false);

        setStatusItem(new ForumEntry(-1, Strings.message("sendfeedback.forums.loading")));

        Thread thread = new Thread(THREAD_NAME)
        {
            public void run()
            {
                ForumEntry error = null;
                try
                {
                    Map forumsTable = ServerService.forumGetForums();
                    if (forumsTable != null)
                    {
                        Set entries = forumsTable.entrySet();
                        final List forums = new ArrayList(entries.size());

                        Iterator it = entries.iterator();
                        while (it.hasNext())
                        {
                            Map.Entry entry = (Map.Entry)it.next();
                            int forumId = Integer.parseInt(entry.getKey().toString());
                            String forumName = entry.getValue().toString();

                            forums.add(new ForumEntry(forumId, forumName));
                        }

                        // Store obtained forums list and populate combo-box
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                forumsList = (ForumEntry[])forums.toArray(
                                    new ForumEntry[forums.size()]);
                                popuplateForumsBox();
                            }
                        });
                    } else error = new ForumEntry(-1, Strings.message("sendfeedback.forums.no.forums.found"));
                } catch (ServerServiceException e)
                {
                    error = new ForumEntry(-1, Strings.message("sendfeedback.forums.failed.loading"));
                }

                // Set error if necessary
                if (error != null)
                {
                    final ForumEntry errorEntry = error;
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            setStatusItem(errorEntry);
                        }
                    });
                }

                btnReload.setEnabled(true);
            }
        };

        thread.start();
    }

    /** Invoked when selected forum changes to update the state of Send button. */
    private void onForumSelectionChange()
    {
        Object item = cbForum.getSelectedItem();

        btnSend.setEnabled(item != null &&
            item instanceof ForumEntry &&
            ((ForumEntry)item).getId() != -1);
    }

    /**
     * Puts single entry.
     *
     * @param entry entry.
     */
    private void setStatusItem(ForumEntry entry)
    {
        cbForum.setEnabled(false);
        cbForum.removeAllItems();
        cbForum.addItem(entry);
    }

    /**
     * Populates the forums box.
     */
    private void popuplateForumsBox()
    {
        if (forumsList.length > 0)
        {
            cbForum.setEnabled(true);
            cbForum.removeAllItems();
            for (int i = 0; i < forumsList.length; i++) cbForum.addItem(forumsList[i]);
        } else
        {
            setStatusItem(new ForumEntry(-1, Strings.message("sendfeedback.forums.no.forums.available")));
        }
    }

    /** Accpets the entry only if the data is valid. */
    public void doAccept()
    {
        ForumEntry selectedForum = (ForumEntry)cbForum.getSelectedItem();
        String name = tfName.getText();
        String email = tfEmail.getText();
        String subject = tfSubject.getText();
        String message = taMessage.getText();

        String error = hasValidData(selectedForum, name, email, subject, message);

        if (error != null)
        {
            JOptionPane.showMessageDialog(this, error,
                Strings.message("sendfeedback.dialog.title"), JOptionPane.INFORMATION_MESSAGE);
        } else super.doAccept();
    }

    /**
     * Validates the data and returns <code>TRUE</code> if the data is ready for sending.
     *
     * @param selectedForum selected forum entry.
     * @param aName         the name of author.
     * @param aEmail        the email address.
     * @param aSubject      the subject of the post.
     * @param aMessage      the message.
     *
     * @return <code>TRUE</code> if the data is ready for sending.
     */
    static String hasValidData(ForumEntry selectedForum, String aName, String aEmail,
        String aSubject, String aMessage)
    {
        String error = null;

        if (selectedForum == null || selectedForum.getId() == -1)
        {
            error = Strings.message("sendfeedback.validation.unselected.forum");
        } else if (StringUtils.isEmpty(aName))
        {
            error = Strings.message("sendfeedback.validation.empty.name");
        } else if (!StringUtils.isEmpty(aEmail) && !StringUtils.isValidEmail(aEmail))
        {
            error = Strings.message("sendfeedback.validation.invalid.email");
        } else if (StringUtils.isEmpty(aSubject))
        {
            error = Strings.message("sendfeedback.validation.unspecified.subject");
        } else if (StringUtils.isEmpty(aMessage))
        {
            error = Strings.message("sendfeedback.validation.empty.message");
        }

        return error;
    }

    /**
     * Returns text entered by user.
     *
     * @return the text.
     */
    public String getMessage()
    {
        return taMessage.getText();
    }

    /**
     * Resizes the specified component. This method is called during the build process and enables
     * subclasses to achieve a better aspect ratio, by applying a resizer, e.g. the
     * <code>Resizer</code>.
     *
     * @param component the component to be resized
     */
    protected void resizeHook(JComponent component)
    {
        Resizer.DEFAULT.resize(component);
    }

    /**
     * Returns email entered by user.
     *
     * @return email.
     */
    public String getEmail()
    {
        return tfEmail.getText().trim();
    }

    /**
     * Returns ID of selected forum.
     *
     * @return ID of selected forum.
     */
    public int getForumId()
    {
        return ((ForumEntry)cbForum.getSelectedItem()).getId();
    }

    /**
     * Returns the subject line.
     *
     * @return subject.
     */
    public String getSubject()
    {
        return tfSubject.getText();
    }

    /**
     * Returns user name.
     *
     * @return name.
     */
    public String getFullName()
    {
        return tfName.getText();
    }

    /**
     * Clears old text, forum ID and subject of the message.
     */
    public static void clearBackupData()
    {
        oldForumId = -1;
        oldSubject = null;
        oldMessage = null;
    }

    /**
     * Single forum entry.
     */
    static class ForumEntry
    {
        private int     id;
        private String  name;

        /**
         * Creates entry.
         *
         * @param aId   forum ID.
         * @param aName forum name.
         */
        public ForumEntry(int aId, String aName)
        {
            id = aId;
            name = aName;
        }

        /**
         * Returns forum id.
         *
         * @return forum id.
         */
        public int getId()
        {
            return id;
        }

        /**
         * Returns a string representation of the object.
         *
         * @return a string representation of the object.
         */
        public String toString()
        {
            return name;
        }
    }

    /**
     * Reloads the list of forums.
     */
    private class ReloadForumsListAction extends AbstractAction
    {
        /** Defines an <code>Action</code> object with a default description string and default icon. */
        public ReloadForumsListAction()
        {
            super(Strings.message("sendfeedback.reload.forums"));
        }

        /** Invoked when an action occurs. */
        public void actionPerformed(ActionEvent e)
        {
            loadForumsList();
        }
    }

    private class ForumSelectionListener implements ItemListener
    {
        /**
         * Invoked when an item has been selected or deselected by the user. The code written for this
         * method performs the operations that need to occur when an item is selected (or deselected).
         */
        public void itemStateChanged(ItemEvent e)
        {
            onForumSelectionChange();
        }
    }
}
