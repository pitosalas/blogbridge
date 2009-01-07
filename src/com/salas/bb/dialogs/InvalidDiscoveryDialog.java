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
// $Id: InvalidDiscoveryDialog.java,v 1.11 2006/11/22 10:05:01 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.util.Resizer;
import com.salas.bb.utils.uif.HeaderPanelExt;
import com.salas.bb.utils.uif.SelectableLabel;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * The dialog telling user when the discovery was not helpful.
 */
public class InvalidDiscoveryDialog extends AbstractDialog
{
    /** Leave invalid feed as is. */
    public static final int OPTION_LEAVE            = 0;
    /** Use entered URL to make another try. */
    public static final int OPTION_NEW_DISCOVERY    = 1;
    /** Use entered URL to test and report to service associated data URL. */
    public static final int OPTION_SUGGEST_URL      = 2;
    /** Cancels the subscription request. */
    public static final int OPTION_CANCEL           = 3;

    private JTextField      lbLink;
    private JRadioButton    rbLeave;
    private JRadioButton    rbNewDiscovery;
    private JRadioButton    rbSuggest;
    private JRadioButton    rbRemove;
    private JTextField      tfSuggestedUrl;
    private JTextField      tfNewDiscoveryUrl;

    private static int      lastSelection = OPTION_LEAVE;

    /**
     * Creates dialog.
     *
     * @param owner owner frame.
     */
    public InvalidDiscoveryDialog(Frame owner)
    {
        super(owner);

        initComponents();
    }

    // Builds content.
    protected JComponent buildContent()
    {
        JPanel content = new JPanel(new BorderLayout());

        content.add(buildMainPanel(), BorderLayout.CENTER);
        content.add(ButtonBarFactory.buildOKBar(createOKButton(true)), BorderLayout.SOUTH);

        return content;
    }

    // Creates header
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(Strings.message("invalid.discovery.dialog.title"),
            Strings.message("invalid.discovery.dialog.header"),
            null);
    }

    // Creates main panel
    private Component buildMainPanel()
    {
        FormLayout layout = new FormLayout(
            "7dlu, pref, 2dlu, min:grow",
            "min:grow, " +
                "pref, 2dlu, pref, 7dlu, " +
                "pref, 2dlu, " +
                "pref, 2dlu, " +
                "pref, 2dlu, " +
                "pref, 2dlu, " +
                "pref, " +
                "min:grow");
        JPanel panel = new JPanel(layout);
        CellConstraints cc = new CellConstraints();

        panel.add(new JLabel(Strings.message("invalid.discovery.wording")), cc.xyw(1, 2, 4));
        panel.add(lbLink, cc.xyw(2, 4, 3));

        panel.add(new JLabel(Strings.message("invalid.discovery.query")), cc.xyw(1, 6, 4));

        panel.add(rbNewDiscovery, cc.xy(2, 8));
        panel.add(tfNewDiscoveryUrl, cc.xy(4, 8));

        panel.add(rbSuggest, cc.xy(2, 10));
        panel.add(tfSuggestedUrl, cc.xy(4, 10));

        panel.add(rbLeave, cc.xy(2, 12));

        panel.add(rbRemove, cc.xy(2, 14));

        return panel;
    }

    // Initialization of components
    private void initComponents()
    {
        lbLink = new SelectableLabel();
        lbLink.setOpaque(false);

        // Init radio-buttons
        rbLeave = new JRadioButton(Strings.message("invalid.discovery.fix.it.later"));
        rbNewDiscovery = new JRadioButton(Strings.message("invalid.discovery.try.again"));
        rbSuggest = new JRadioButton(Strings.message("invalid.discovery.suggest.xmlurl"));
        rbRemove = new JRadioButton(Strings.message("invalid.discovery.cancel"));

        ButtonGroup group = new ButtonGroup();
        group.add(rbNewDiscovery);
        group.add(rbSuggest);
        group.add(rbLeave);
        group.add(rbRemove);

        tfNewDiscoveryUrl = new JTextField();
        tfNewDiscoveryUrl.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                rbNewDiscovery.setSelected(true);
            }
        });
        tfSuggestedUrl = new JTextField();
        tfSuggestedUrl.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                rbSuggest.setSelected(true);
            }
        });
    }

    // Resizing hook
    protected void resizeHook(JComponent component)
    {
        component.setPreferredSize(Resizer.DEFAULT.fromHeight(component.getPreferredSize().height));
    }

    /**
     * Open dialog and show the previous URL.
     *
     * @param discoveryUrl  discovery URL to set.
     * @param suggestedUrl  suggested URL to set.
     * @param canSuggest    TRUE to enable suggestions.
     *
     * @return selected option.
     *
     * @see #OPTION_LEAVE
     * @see #OPTION_NEW_DISCOVERY
     * @see #OPTION_SUGGEST_URL
     */
    public int open(URL discoveryUrl, String suggestedUrl, boolean canSuggest)
    {
        String url = discoveryUrl == null ? null : discoveryUrl.toString();

        lbLink.setText(url);
        tfNewDiscoveryUrl.setText(url);
        tfSuggestedUrl.setText(suggestedUrl);

        JRadioButton selectedButton;

        rbSuggest.setEnabled(canSuggest);
        tfSuggestedUrl.setEnabled(canSuggest);

        if (suggestedUrl != null && canSuggest)
        {
            selectedButton = rbSuggest;
        } else
        {
            if (!canSuggest && lastSelection == OPTION_SUGGEST_URL) lastSelection = OPTION_LEAVE;
            selectedButton = getComponentByOption(lastSelection);
        }

        selectedButton.setSelected(true);
        
        open();

        lastSelection = getSelectedOption();

        return lastSelection;
    }

    // Returns radio-button corresponding to option.
    private JRadioButton getComponentByOption(int option)
    {
        JRadioButton button;

        switch (option)
        {
            case OPTION_CANCEL:
                button = rbRemove;
                break;
            case OPTION_LEAVE:
                button = rbLeave;
                break;
            case OPTION_NEW_DISCOVERY:
                button = rbNewDiscovery;
                break;
            default:
                button = rbSuggest;
                break;
        }

        return button;
    }

    /**
     * Returns the option selected by user with radio-buttons.
     *
     * @return the option.
     *
     * @see #OPTION_LEAVE
     * @see #OPTION_CANCEL
     * @see #OPTION_NEW_DISCOVERY
     * @see #OPTION_SUGGEST_URL
     */
    public int getSelectedOption()
    {
        int option;

        if (rbLeave.isSelected())
        {
            option = OPTION_LEAVE;
        } else if (rbNewDiscovery.isSelected())
        {
            option = OPTION_NEW_DISCOVERY;
        } else if (rbSuggest.isSelected())
        {
            option = OPTION_SUGGEST_URL;
        } else if (rbLeave.isSelected())
        {
            option = OPTION_LEAVE;
        } else
        {
            option = OPTION_CANCEL;
        }

        return option;
    }

    /**
     * Returns URL entereed by user in field for New Discovery URL.
     *
     * @return new discovery URL.
     */
    public URL getNewDiscoveryUrl()
    {
        URL url = null;
        String str = tfNewDiscoveryUrl.getText();

        try
        {
            if (!StringUtils.isEmpty(str)) url = new URL(str);
        } catch (MalformedURLException e)
        {
            url = null;
        }

        return url;
    }

    /**
     * Returns URL entered by user in field for Suggested Feed URL.
     *
     * @return suggested Feed URL.
     */
    public String getSuggestedFeedUrl()
    {
        return tfSuggestedUrl.getText();
    }


}
