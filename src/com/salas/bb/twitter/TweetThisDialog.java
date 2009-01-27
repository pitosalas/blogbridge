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
import com.salas.bb.utils.Constants;
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
    private static final int WIDTH_AVAILABLE = 500;
    private static final int WIDTH_UNAVAILABLE = 450;
    private static final String THREAD_SHORTEN_LINK = "Shorten Link";

    private JTextArea taMessage;
    private JTextField tfLink;
    private JButton btnShorten;
    private JLabel lbCharsLeft;
    private JButton btnSend;
    private JScrollPane spMessage;

    private String initialText;
    private String initialLink;

    /**
     * Creates the dialog.
     *
     * @param frame dialog.
     */
    public TweetThisDialog(Frame frame)
    {
        super(frame, Strings.message("tweetthis.dialog.title"));
    }

    @Override
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("tweetthis.dialog.title"),
            Strings.message("tweetthis.dialog.header"),
            IconSource.getIcon(ResourceID.ICON_TWITTER));
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
            bar = ButtonBarFactory.buildOKCancelBar(btnSend, createCancelButton());
        } else
        {
            bar = buildButtonBarWithClose();
        }

        bar.setBorder(Constants.DIALOG_BUTTON_BAR_BORDER);
        
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

        BBFormBuilder builder = new BBFormBuilder("right:pref, 4dlu, 100dlu:grow, 2dlu, pref");
        builder.setDefaultDialogBorder();

        // Build shifted label
        BBFormBuilder pb = new BBFormBuilder("right:pref");
        pb.appendRow("3px");
        pb.nextLine();
        pb.append(Strings.message("tweetthis.your.message"));
        pb.append(lbCharsLeft);

        builder.append(pb.getPanel(), 1, CellConstraints.LEFT, CellConstraints.TOP);
        builder.append(spMessage, 3);
        builder.append(Strings.message("tweetthis.shorten.link"), tfLink, btnShorten);

        return builder;
    }

    /**
     * Initializes components.
     */
    private void initComponents()
    {
        tfLink      = new JTextField(initialLink);
        taMessage   = new JTextArea(5, 70);
        btnShorten  = new JButton(new ShortenAction());
        lbCharsLeft = new JLabel();
        spMessage   = new JScrollPane(taMessage);

        Border spacing = BorderFactory.createLineBorder(new JLabel().getBackground(), 3);
        spMessage.setBorder(BorderFactory.createCompoundBorder(spacing, spMessage.getBorder()));

        taMessage.setText(initialText);
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

        lbCharsLeft.setForeground(Color.DARK_GRAY);
        updateCharsCount();

        if (StringUtils.isNotEmpty(initialLink))
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    onShorten();
                }
            });
        }

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
        initialLink = link;
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

        taMessage.setBackground(over ? Color.RED : tfLink.getBackground());
        taMessage.setForeground(over ? Color.WHITE : tfLink.getForeground());
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
    private void onShorten()
    {
        componentsEnabled(false);

        new Thread(THREAD_SHORTEN_LINK)
        {
            public void run()
            {
                String aLink = null;
                String anErrorMessage = null;

                try
                {
                    aLink = LinkShortener.process(tfLink.getText());
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
                                onShortenSuccess(link);
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
        btnShorten.setEnabled(enabled);
        tfLink.setEnabled(enabled);
    }

    /**
     * Invoked when completed successfully.
     *
     * @param link link.
     */
    private void onShortenSuccess(String link)
    {
        tfLink.setText(link);
    }

    /**
     * Invoked when completed with an error.
     *
     * @param error error.
     */
    private void onShortenFailure(String error)
    {
        JOptionPane.showMessageDialog(this, error,
            Strings.message("tweetthis.dialog.title") + " - " + Strings.message("tweetthis.shorten.btn"),
            JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Shorten link action.
     */
    private class ShortenAction extends AbstractAction
    {
        public ShortenAction()
        {
            super(Strings.message("tweetthis.shorten.btn"));
        }

        public void actionPerformed(ActionEvent e)
        {
            onShorten();
        }
    }
}
