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

import com.jgoodies.binding.adapter.DocumentAdapter;
import com.jgoodies.binding.adapter.ToggleButtonAdapter;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.value.BufferedValueModel;
import com.jgoodies.binding.value.ValueModel;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.swingworker.AbstractSwingWorkerAction;
import com.salas.bb.utils.swingworker.SwingWorker;
import com.salas.bb.utils.uif.*;
import oauth.signpost.exception.OAuthException;
import oauth.signpost.exception.OAuthExpectationFailedException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Twitter preferences.
 */
public class TwitterPreferencesPanel extends JPanel
{
    private final TwitterPreferences prefs;
    private ProgressSpinner spinner;
    private JTextField tfPIN;

    private JPanel setupPanel;
    private JPanel authPanel;

    /**
     * Creates the panel.
     *
     * @param parent        parent dialog.
     * @param trigger       trigger.
     * @param preferences   preferences object to manipulate.
     */
    public TwitterPreferencesPanel(JDialog parent, ValueModel trigger, TwitterPreferences preferences)
    {
        prefs = preferences;

        configureSetupPanel(trigger);
        configureAuthPanel();

        this.setLayout(new BorderLayout());

        setCorrectPanel();
    }

    private void setCorrectPanel()
    {
        JPanel setPanel, removePanel;

        if (prefs.isAuthorized())
        {
            setPanel    = setupPanel;
            removePanel = authPanel;
        } else {
            setPanel    = authPanel;
            removePanel = setupPanel;
            tfPIN.setText("");
        }

        remove(removePanel);
        add(setPanel, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private void configureAuthPanel()
    {
        BBFormBuilder builder = new BBFormBuilder("p, 4dlu, 50dlu, 4dlu, p, p:grow");
        builder.setDefaultDialogBorder();

        Component tfWording     = ComponentsFactory.createWrappedMultilineLabel("You need to authorize BlogBridge to use your account if you want to tweet and access your timeline.");
        // TODO i18n Strings.message("userprefs.tab.twitter.unauthorized"));

        JLabel lbStep1          = new JLabel("1. Visit:"); // TODO i18n
        LinkLabel llLink        = new TwitterAuthLinkLabel("Twitter application authorization page");
        spinner                 = new ProgressSpinner();

        JLabel lbStep2          = new JLabel("2. Enter PIN:"); // TODO i18n
        tfPIN                   = new JTextField();
        JButton btnAuthorize    = new JButton(new AuthorizeAction());

        JPanel linkAndSpinner = new JPanel(new FlowLayout(FlowLayout.LEFT));
        linkAndSpinner.add(llLink);
        linkAndSpinner.add(spinner);

        builder.append(tfWording, 6);
        builder.appendUnrelatedComponentsGapRow(2);

        builder.append(lbStep1);
        builder.append(linkAndSpinner, 4);
        builder.appendRelatedComponentsGapRow(2);
        builder.append(lbStep2, tfPIN);
        builder.append(btnAuthorize);

        authPanel = builder.getPanel();
    }

    private void configureSetupPanel(ValueModel trigger)
    {
        // Layout
        BBFormBuilder builder = new BBFormBuilder("7dlu, p, 4dlu, p:grow, 4dlu, p");
        builder.setDefaultDialogBorder();

        Component tfWording = ComponentsFactory.createWrappedMultilineLabel(
            Strings.message("userprefs.tab.twitter.wording"));

        JCheckBox chEnabled = ComponentsFactory.createCheckBox(
            Strings.message("userprefs.tab.twitter.enable"),
            new ToggleButtonAdapter(new BufferedValueModel(
                new PropertyAdapter(prefs, TwitterPreferences.PROP_ENABLED),
                trigger)));

        JCheckBox chProfilePics = ComponentsFactory.createCheckBox(
            Strings.message("userprefs.tab.twitter.profile.pics"),
            new ToggleButtonAdapter(new BufferedValueModel(
                new PropertyAdapter(prefs, TwitterPreferences.PROP_PROFILE_PICS),
                trigger)));

        JCheckBox chPasteLink = ComponentsFactory.createCheckBox(
            Strings.message("userprefs.tab.twitter.paste.link"),
            new ToggleButtonAdapter(new BufferedValueModel(
                new PropertyAdapter(prefs, TwitterPreferences.PROP_PASTE_LINK),
                trigger)));

        JLabel lbScreenName = new JLabel(Strings.message("userprefs.tab.twitter.screenname"));
        JLabel lbPassword   = new JLabel(Strings.message("userprefs.tab.twitter.password"));

        JTextField tfScreenName = new JTextField();
        tfScreenName.setEditable(false);
        tfScreenName.setDocument(new DocumentAdapter(new BufferedValueModel(
            new PropertyAdapter(prefs, TwitterPreferences.PROP_SCREEN_NAME), trigger)));

        JButton btnChange = new JButton(new UnauthorizeAction());

        StateUpdatingToggleListener.install(chEnabled, lbScreenName, tfScreenName, lbPassword, chProfilePics, chPasteLink);

        builder.append(tfWording, 6);
        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(chEnabled, 6);

        builder.setLeadingColumnOffset(1);
        builder.nextLine();
        builder.append(lbScreenName, tfScreenName);
        builder.append(btnChange);

        builder.setLeadingColumnOffset(3);
        builder.nextLine();
        builder.append(chProfilePics, 3);
        builder.append(chPasteLink, 3);

        setupPanel = builder.getPanel();
    }

    // Custom Twitter AUTH link label
    class TwitterAuthLinkLabel extends LinkLabel
    {
        public TwitterAuthLinkLabel(String text)
        {
            super(text, "http://twitter.com/oauth/authorize");
        }

        protected void doAction()
        {
            spinner.start();

            SwingWorker<Object, Integer> worker = new SwingWorker<Object, Integer>()
            {
                protected Object doInBackground()
                    throws Exception
                {
                    String link = prefs.getAuthURL();
                    if (link != null) setLink(new URL(link));
                    return null;
                }

                protected void done()
                {
                    spinner.stop();
                    TwitterAuthLinkLabel.super.doAction();
                }
            };
            worker.execute();
        }
    }

    class UnauthorizeAction extends AbstractAction
    {
        public UnauthorizeAction()
        {
            super("Change Account"); // TODO i18n
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            prefs.unauthorize();
            setCorrectPanel();
        }
    }

    class AuthorizeAction extends AbstractSwingWorkerAction<Object, Object>
    {
        public AuthorizeAction()
        {
            super("Authorize"); // TODO i18n
            setDisableWhenWorking(true);
        }

        protected Object performInBackground() throws OAuthException
        {
            prefs.acquireAccessTokens(tfPIN.getText());
            return null;
        }

        protected void done()
        {
            try
            {
                get();
                setCorrectPanel();
            } catch (ExecutionException e)
            {
                if (e.getCause() instanceof OAuthExpectationFailedException)
                {
                    JOptionPane.showMessageDialog(TwitterPreferencesPanel.this, "Please visit");
                }
            } catch (InterruptedException ignore)
            {
            }
        }
    }

}
