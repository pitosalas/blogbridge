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
// $Id: WelcomePage.java,v 1.5 2007/11/02 12:32:20 spyromus Exp $
//

package com.salas.bb.installation.wizard;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.application.ResourceIDs;
import com.jgoodies.uif.util.ResourceUtils;
import com.jgoodies.uifextras.convenience.SetupManager;
import com.jgoodies.uifextras.panel.GradientBackgroundPanel;
import com.salas.bb.utils.ResourceID;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.ComponentsFactory;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

/**
 * Page with welcome information.
 */
public class WelcomePage extends GradientBackgroundPanel implements IWizardPage,
    SetupManager.WizardPanel
{
    private JLabel logo;
    private JComponent welcomeText;

    /**
     * Creates page.
     *
     * @param next   listener of 'Next' button.
     * @param cancel listener of 'Cancel' button.
     */
    public WelcomePage(ActionListener next, ActionListener cancel)
    {
        if (next != null && cancel != null) build(buildButtonBar(next, cancel));
    }

    /**
     * Creates and configures the UI components.
     */
    private void initComponents()
    {
        logo = new JLabel(ResourceUtils.getIcon(ResourceIDs.LOGO_ICON));

        try
        {
            final String path = ResourceUtils.getString(ResourceID.LICENSE_WELCOME_TEXT_URL);
            final URL url = ResourceUtils.getURL(path);
            JEditorPane welcomeTextView = new JEditorPane(url);
            welcomeTextView.setFocusable(false);
            welcomeTextView.setEditable(false);

            // Set font
            HTMLDocument doc = (HTMLDocument)welcomeTextView.getDocument();
            final Style style = doc.addStyle("normal", null);
            doc.setCharacterAttributes(0, doc.getLength(), style, false);

            welcomeText = welcomeTextView;
        } catch (IOException e)
        {
            String text = ResourceUtils.getString(ResourceIDs.LICENSE_WELCOME_TEXT);
            welcomeText = ComponentsFactory.createWrappedMultilineLabel(text);
            welcomeText.setForeground(Color.black);
        }
    }


    /**
     * Builds the panel using the specified button bar.
     * 
     * @param buttonBar button bar to use.
     */
    public void build(JComponent buttonBar)
    {
        initComponents();

        JScrollPane sp = new JScrollPane(welcomeText);

        BBFormBuilder builder = new BBFormBuilder("0, pref:grow, 0", this);
        builder.setDefaultDialogBorder();

        builder.append(logo, 3, CellConstraints.CENTER, CellConstraints.DEFAULT);
        builder.appendUnrelatedComponentsGapRow(2);
        builder.appendRow("50dlu:grow");
        builder.append(sp, 3, CellConstraints.FILL, CellConstraints.FILL);
        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(buttonBar, 3, CellConstraints.RIGHT, CellConstraints.DEFAULT);
    }

    /**
     * Paints the component's background; uses the <code>GradientBackgroundPanel</code>.
     *
     * @param g graphics context.
     */
    public void paintComponent(Graphics g)
    {
        GradientBackgroundPanel.paintBackground(g, this, getWidth(), getHeight(), false);
    }

    /**
     * Builds bar with buttons.
     */
    private JComponent buildButtonBar(ActionListener aNext, ActionListener aCancel)
    {
        JPanel bar = ButtonBarFactory.buildRightAlignedBar(WizardUIHelper.createNextButton(aNext),
            WizardUIHelper.createCancelButton(aCancel));

        bar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
        bar.setOpaque(false);

        return bar;
    }

    /**
     * Returns component to be put on the screen to represent this page.
     *
     * @return visible component.
     */
    public JComponent getComponent()
    {
        return this;
    }

    /**
     * Validates and returns error message to display or <code>null</code> if information on the
     * page is valid.
     *
     * @return string to display or <code>null</code>.
     */
    public String validatePage()
    {
        return null;
    }
}
