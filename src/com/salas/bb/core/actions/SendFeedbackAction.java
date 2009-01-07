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
// $Id: SendFeedbackAction.java,v 1.12 2008/08/06 14:44:46 spyromus Exp $
//

package com.salas.bb.core.actions;

import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.application.Application;
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.core.ApplicationLauncher;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.dialogs.SendFeedbackDialog;
import com.salas.bb.service.ServerService;
import com.salas.bb.service.ServicePreferences;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.LinkLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * The action is called when someone wishes to send us a feedback.
 */
public final class SendFeedbackAction extends AbstractAction
{
    private static SendFeedbackAction instance;

    private SendFeedbackDialog dialog;

    /**
     * Hidden singleton constructor.
     */
    private SendFeedbackAction()
    {
        setEnabled(ApplicationLauncher.getConnectionState().isServiceAccessible());
    }

    /**
     * Returns action instance.
     *
     * @return instance.
     */
    public static synchronized SendFeedbackAction getInstance()
    {
        if (instance == null) instance = new SendFeedbackAction();

        return instance;
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e event object.
     */
    public void actionPerformed(ActionEvent e)
    {
        final SendFeedbackDialog dlg = getDialog();

        ServicePreferences servicePreferences = GlobalModel.SINGLETON.getServicePreferences();

        dlg.open(servicePreferences.getFullName(), servicePreferences.getEmail());
        if (!dlg.hasBeenCanceled())
        {
            String name = dlg.getFullName();
            String email = dlg.getEmail();
            int forumId = dlg.getForumId();
            String subject = dlg.getSubject();
            String message = dlg.getMessage();

            Thread thread = new SendForumMessage(name, email, forumId, subject, message);
            thread.setDaemon(true);
            thread.start();
        }
    }

    // Returns dialog.
    private synchronized SendFeedbackDialog getDialog()
    {
        if (dialog == null) dialog = new SendFeedbackDialog(Application.getDefaultParentFrame());

        return dialog;
    }

    /**
     * Sends feedback message to the service.
     */
    private static class SendForumMessage extends Thread
    {
        private static final String THREAD_NAME_SENDING_FEEDBACK = "Sending Feedback";

        private final String name;
        private final String email;
        private final int forumId;
        private final String subject;
        private final String message;

        /**
         * Creates sending action.
         *
         * @param aName     name of the author.
         * @param aEmail    email address of the author.
         * @param aForumId  ID of selected forum.
         * @param aSubject  subject of the message.
         * @param aMessage  message text.
         */
        public SendForumMessage(String aName, String aEmail, int aForumId, String aSubject,
                                String aMessage)
        {
            super(THREAD_NAME_SENDING_FEEDBACK);

            name = aName;
            email = aEmail;
            forumId = aForumId;
            subject = aSubject;
            message = aMessage;
        }

        /** Invoked on execution. */
        public void run()
        {
            boolean sent = ServerService.forumPost(name, email, forumId, subject, message);

            // Show status message
            FeedbackDialog dialog = new FeedbackDialog(sent, forumId);
            dialog.open();
        }

        /**
         * Feedback results dialog.
         */
        private static class FeedbackDialog extends AbstractDialog
        {
            private final boolean sent;
            private final int     forumId;

            /**
             * Creates the dialog.
             *
             * @param sent      TRUE if the message was sent.
             * @param forumId   ID of the target forum.
             */
            public FeedbackDialog(boolean sent, int forumId)
            {
                super(Application.getDefaultParentFrame(), Strings.message("feedback.dialog.title"));
                this.sent = sent;
                this.forumId = forumId;
            }

            protected JComponent buildContent()
            {
                BBFormBuilder builder = new BBFormBuilder("p, 8dlu, p:grow");
                builder.setDefaultDialogBorder();
                
                builder.append(new JLabel(ResourceUtils.getIcon("application.64.icon")),
                    1, CellConstraints.DEFAULT, CellConstraints.TOP);
                builder.append(buildMainPanel());

                builder.appendUnrelatedComponentsGapRow(2);
                builder.append(ButtonBarFactory.buildCenteredBar(createOKButton(true)), 3);

                return builder.getPanel();
            }

            private Component buildMainPanel()
            {
                BBFormBuilder builder = new BBFormBuilder("p");

                builder.append(sent
                    ? Strings.message("feedback.success")
                    : Strings.message("feedback.failure"));

                if (sent)
                {
                    LinkLabel linkLabel = new LinkLabel(Strings.message("feedback.forum"),
                        "http://forum.blogbridge.com/viewforum.php?id=" + forumId);
                    linkLabel.setForeground(LinkLabel.HIGHLIGHT_COLOR);

                    builder.appendUnrelatedComponentsGapRow(2);
                    builder.append(Strings.message("feedback.please.visit"));
                    builder.append(linkLabel, 1, CellConstraints.CENTER, CellConstraints.DEFAULT);
                }

                return builder.getPanel();
            }
        }
    }
}
