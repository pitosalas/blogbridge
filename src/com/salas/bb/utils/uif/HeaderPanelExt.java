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
// $Id: HeaderPanelExt.java,v 1.10 2006/01/08 05:07:31 kyank Exp $
//

package com.salas.bb.utils.uif;

import com.jgoodies.uifextras.panel.GradientBackgroundPanel;
import com.jgoodies.uifextras.util.UIFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.util.SystemUtils;
import com.salas.bb.utils.osx.OSXSupport;
import com.salas.bb.utils.Constants;

import javax.swing.*;
import java.awt.*;

/**
 * Extended header panel, with the ability to wrap and with platform specific font handling.
 */
public class HeaderPanelExt extends GradientBackgroundPanel
{
    private static final String COLUMNS             = "pref:grow";
    private static final String ROWS                = "pref, pref";
    private static final Font FONT_LUCIDA_GRANDE    = new Font("Lucida Grande", Font.PLAIN, 10);

    /**
     * Automatically computed height. Don't work for dialogs with resizer hooks as they require
     * the pre-set dimensions to do the job.
     */
//    public static final int HEIGHT_AUTO     = -1;

    /** Default height. */
    public static final int HEIGHT_DEFAULT  = 70;

    private final int   height;

    private JLabel      titleLabel;
    private JLabel      iconLabel;
    private JTextArea   descriptionArea;

    // Instance Creation ****************************************************

    /**
     * Constructs the panel. Minimum height will be computed.
     *
     * @param title         title.
     * @param description   description.
     */
    public HeaderPanelExt(String title, String description)
    {
        this(title, description, null);
    }

    /**
     * Constructs the panel. Minimum height will be computed.
     *
     * @param title         title.
     * @param description   description.
     * @param icon          icon.
     */
    public HeaderPanelExt(String title, String description, Icon icon)
    {
        this(title, description, icon, HEIGHT_DEFAULT);
    }

    /**
     * Constructs the panel.
     *
     * @param title         title.
     * @param description   description.
     * @param icon          icon.
     * @param height        minimum height.
     *
     * @see #HEIGHT_DEFAULT
     */
    public HeaderPanelExt(String title, String description, Icon icon, int height)
    {
        super(true);

        this.height = height;

        initComponents();
        build();
        setTitle(title);
        setDescription(description);
        setIcon(icon);
    }

    /**
     * Returns the title text.
     *
     * @return title.
     */
    public String getTitle()
    {
        return titleLabel.getText();
    }

    /**
     * Sets the title text.
     *
     * @param title title.
     */
    public void setTitle(String title)
    {
        titleLabel.setText(title);
    }

    /**
     * Returns the description text.
     *
     * @return description.
     */
    public String getDescription()
    {
        return descriptionArea.getText();
    }

    /**
     * Sets the description text.
     *
     * @param description description.
     */
    public void setDescription(String description)
    {
        descriptionArea.setText(description);
    }

    /**
     * Returns the icon.
     *
     * @return icon.
     */
    public Icon getIcon()
    {
        return iconLabel.getIcon();
    }

    /**
     * Sets the icon.
     *
     * @param icon icon.
     */
    public void setIcon(Icon icon)
    {
        if (icon != null)
        {
            if (icon.getIconWidth() <= 20 && (icon instanceof ImageIcon))
            {
                Image image = ((ImageIcon)icon).getImage();
                int newWidth = 2 * icon.getIconWidth();
                int newHeight = 2 * icon.getIconHeight();
                image = image.getScaledInstance(newWidth, newHeight, 0);
                icon = new ImageIcon(image);
            }
        }

        iconLabel.setIcon(icon);
    }

    /**
     * Creates and configures the UI components.
     */
    private void initComponents()
    {
        titleLabel = UIFactory.createBoldLabel(Constants.EMPTY_STRING, 0, Color.black);

        descriptionArea = ComponentsFactory.createWrappedMultilineLabel(Constants.EMPTY_STRING);
        if (SystemUtils.IS_OS_MAC) descriptionArea.setFont(FONT_LUCIDA_GRANDE);

        iconLabel = new JLabel();
    }

    /**
     * Builds the panel.
     */
    private void build()
    {
        setLayout(new FormLayout(COLUMNS, ROWS));
        CellConstraints cc = new CellConstraints();
        add(buildCenterComponent(), cc.xy(1, 1));
        add(buildBottomComponent(), cc.xy(1, 2));
    }

    /**
     * Builds and answers the panel's center component.
     */
    protected JComponent buildCenterComponent()
    {
        FormLayout fl = new FormLayout(
            "7dlu, 9dlu, pref:grow, 14dlu, pref, 4dlu",
            "7dlu, pref, 2dlu, pref, 7dlu:grow");
        JPanel panel = new JPanel(fl);

//        if (height != HEIGHT_AUTO)
//        {
            Dimension size = new Dimension(300, height);
            panel.setMinimumSize(size);
            panel.setPreferredSize(size);
//        }

        panel.setOpaque(false);

        CellConstraints cc = new CellConstraints();
        panel.add(titleLabel, cc.xyw(2, 2, 2));
        panel.add(descriptionArea, cc.xy(3, 4));
        panel.add(iconLabel, cc.xywh(5, 1, 1, 5));

        return panel;
    }

    /**
     * Builds and answers the panel's bottom component, a separator by default.
     */
    protected JComponent buildBottomComponent()
    {
        return new JSeparator();
    }
}
