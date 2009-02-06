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
// $Id$
//

package com.salas.bb.twitter;

import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.utils.ResourceID;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.net.LinkShortener;
import com.salas.bb.utils.net.LinkShorteningException;
import com.salas.bb.utils.uif.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * Tweet This dialog box.
 */
public class TweetThisDialog extends AbstractDialog
{
    private static final int WIDTH_AVAILABLE = 400;
    private static final int WIDTH_UNAVAILABLE = 450;
    private static final String THREAD_SHORTEN_LINK = "Shorten Link";

    private JTextArea   taMessage;
    private JButton     btnPasteLink;
    private JLabel      lbCharsLeft;
    private JButton     btnSend;
    private JScrollPane spMessage;

    private String initialText;
    private String link;

    /**
     * Creates the dialog.
     *
     * @param frame dialog.
     */
    public TweetThisDialog(Frame frame)
    {
        super(frame, Strings.message("tweetthis.dialog.title"));
    }

    /**
     * Builds the dialog content pane.
     *
     * @return dialog.
     */
    protected JComponent buildContent()
    {
        final Component buttonBar = buildButtonBar();
        JPanel content = new JPanel(new BorderLayout());
        content.add(buildBody(), BorderLayout.CENTER);
        content.add(buttonBar, BorderLayout.SOUTH);
        return content;
    }

    /**
     * Creates buttons bar.
     *
     * @return bar.
     */
    private Component buildButtonBar()
    {
        JComponent bar;

        if (TwitterFeature.isAvaiable())
        {
            btnSend = createOKButton(true);
            btnSend.setText(Strings.message("tweetthis.send"));
            btnPasteLink = new JButton(new PasteLinkAction());
            btnPasteLink.setEnabled(StringUtils.isNotEmpty(link));

            BBFormBuilder b = new BBFormBuilder("p, 4dlu:grow, p, 2dlu, p");
            b.append(btnPasteLink);
            b.append(btnSend);
            b.append(createCancelButton());
            bar = b.getPanel();
        } else
        {
            bar = buildButtonBarWithClose();
        }

        return bar;
    }

    /**
     * Creates the main area.
     *
     * @return component.
     */
    private Component buildBody()
    {
        BBFormBuilder builder = TwitterFeature.isAvaiable() ? buildAvailableBody() : buildUnavailableBody();
        builder.appendUnrelatedComponentsGapRow();
        return builder.getPanel();
    }

    /**
     * Body to show when feature is unavailable.
     *
     * @return panel.
     */
    private BBFormBuilder buildUnavailableBody()
    {
        BBFormBuilder builder = new BBFormBuilder("7dlu, pref:grow");
        builder.setDefaultDialogBorder();

        // Service link
        LinkLabel lnkService = new LinkLabel(
            Strings.message("spw.learn.more"),
            ResourceUtils.getString("server.plans.url"));
        lnkService.setForeground(LinkLabel.HIGHLIGHT_COLOR);

        String message = Strings.message("tweetthis.unavailable.1");
        builder.append(ComponentsFactory.createWrappedMultilineLabel(message), 2);

        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(new JLabel(Strings.message("tweetthis.unavailable.2")), 2);

        builder.setLeadingColumnOffset(1);
        builder.append(lnkService);

        return builder;
    }

    /**
     * Body to show when feature is available.
     *
     * @return panel.
     */
    private BBFormBuilder buildAvailableBody()
    {
        initComponents();

        BBFormBuilder builder = new BBFormBuilder("pref, 10dlu, 20dlu:grow, 2dlu, pref");

        // Build shifted label
        builder.append(Strings.message("tweetthis.your.message"), 1, CellConstraints.LEFT, CellConstraints.BOTTOM);
        builder.append(lbCharsLeft, 1, CellConstraints.LEFT, CellConstraints.BOTTOM);
        builder.append(new JLabel(IconSource.getIcon(ResourceID.ICON_TWITTER)));
        builder.append(spMessage, 5);

        return builder;
    }

    /**
     * Initializes components.
     */
    private void initComponents()
    {
        taMessage   = new JTextArea(5, 70);
        lbCharsLeft = new JLabel();
        spMessage   = new JScrollPane(taMessage);

        Border spacing = BorderFactory.createLineBorder(new JLabel().getBackground(), 3);
        spMessage.setBorder(BorderFactory.createCompoundBorder(spacing, spMessage.getBorder()));

        taMessage.setWrapStyleWord(false);
        taMessage.setLineWrap(true);
        taMessage.setDocument(new TwitterMessage());
        taMessage.getDocument().addDocumentListener(new DocumentListener()
        {
            public void insertUpdate(DocumentEvent e)
            {
                updateCharsCount();
            }

            public void removeUpdate(DocumentEvent e)
            {
                updateCharsCount();
            }

            public void changedUpdate(DocumentEvent e)
            {
                updateCharsCount();
            }
        });
        taMessage.setText(initialText);

        lbCharsLeft.setForeground(Color.DARK_GRAY);
        updateCharsCount();

        taMessage.requestFocusInWindow();
    }

    /**
     * Opens with some initial text.
     *
     * @param text text.
     */
    public void open(String text)
    {
        initialText = text;
        open();
    }

    public void open(String text, String link)
    {
        this.link = link;
        open(text);
    }

    @Override
    public void doAccept()
    {
        btnSend.setEnabled(false);
        btnSend.setText("sending ...");

        new Thread("Sending To Twitter")
        {
            public void run()
            {
                try
                {
                    TwitterGateway.update(taMessage.getText());
                    onSent();
                } catch (IOException e)
                {
                    onFailedToSend(e.getMessage());
                }
            }
        }.start();
    }

    /**
     * Invoked when the message is sent.
     */
    private void onSent()
    {
        super.doAccept();
    }

    /**
     * Invoked when sending has failed.
     *
     * @param error error message.
     */
    private void onFailedToSend(String error)
    {
        btnSend.setText(Strings.message("tweetthis.send"));
        btnSend.setEnabled(true);

        JOptionPane.showMessageDialog(this, error,
            Strings.message("tweetthis.dialog.title") + " - " + Strings.message("tweetthis.send"),
            JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Updates the count of chars left.
     */
    private void updateCharsCount()
    {
        int current = taMessage.getText().length();
        lbCharsLeft.setText(Integer.toString(TwitterMessage.MAX_LENGTH - current));
        final boolean over = current > TwitterMessage.MAX_LENGTH;

        taMessage.setBackground(over ? Color.RED : Color.WHITE);
        taMessage.setForeground(over ? Color.WHITE : Color.BLACK);
        btnSend.setEnabled(!over && current > 0);
    }

    /**
     * Resizing hook.
     *
     * @param component component.
     */
    protected void resizeHook(JComponent component)
    {
        final int width = TwitterFeature.isAvaiable() ? WIDTH_AVAILABLE : WIDTH_UNAVAILABLE;
        component.setPreferredSize(new Dimension(width, (int)component.getPreferredSize().getHeight()));
    }

    /**
     * Invoked when shortening should be performed.
     */
    private void onPasteLink()
    {
        // If empty, do nothing
        if (StringUtils.isEmpty(link)) return;

        // If already shortened, do pasting
        if (link.matches("^http://(www\\.)?is\\.gd\\/.*"))
        {
            doPasteLink(link);
            return;
        }

        // If not compressed, compress and do pasting
        componentsEnabled(false);

        new Thread(THREAD_SHORTEN_LINK)
        {
            public void run()
            {
                String aLink = null;
                String anErrorMessage = null;

                try
                {
                    aLink = LinkShortener.process(link);
                } catch (LinkShorteningException e)
                {
                    anErrorMessage = e.getMessage();
                }

                // Invoke in the GUI thread
                final String link = aLink;
                final String errorMessage = anErrorMessage;
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            if (link != null)
                            {
                                TweetThisDialog.this.link = link;
                                doPasteLink(link);
                            } else
                            {
                                onShortenFailure(errorMessage);
                            }
                        } finally
                        {
                            componentsEnabled(true);
                        }
                    }
                });
            }
        }.start();
    }

    /**
     * Enables / disables related components.
     *
     * @param enabled TRUE to enable.
     */
    private void componentsEnabled(boolean enabled)
    {
        btnPasteLink.setEnabled(enabled);
    }

    /**
     * Invoked when completed successfully.
     *
     * @param link link.
     */
    private void doPasteLink(String link)
    {
        taMessage.append(link);
        taMessage.requestFocusInWindow();
    }

    /**
     * Invoked when completed with an error.
     *
     * @param error error.
     */
    private void onShortenFailure(String error)
    {
        JOptionPane.showMessageDialog(this, error,
            Strings.message("tweetthis.dialog.title") + " - " + Strings.message("tweetthis.paste.link.btn"),
            JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Shorten link action.
     */
    private class PasteLinkAction extends AbstractAction
    {
        public PasteLinkAction()
        {
            super(Strings.message("tweetthis.paste.link.btn"));
        }

        public void actionPerformed(ActionEvent e)
        {
            onPasteLink();
        }
    }
}
