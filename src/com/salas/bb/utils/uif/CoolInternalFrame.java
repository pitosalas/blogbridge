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
// $Id: CoolInternalFrame.java,v 1.25 2006/01/08 05:07:31 kyank Exp $
// 

package com.salas.bb.utils.uif;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.util.SystemUtils;

import javax.swing.*;
import java.awt.*;

/**
 * A header and a body in a nice shaded frame.
 * 
 * The body is any component.
 * The header is constructed from a title, subtitle and header control component, which are
 * laid out in two rows. The Title is in the top left of the header, and has a bigger font. The
 * control component is in the top right. And the subtitle spans the whole bottom of the header,
 * in a smaller font.
 */
public class CoolInternalFrame extends JPanel
{
    private static final int TITLE_FONT_SIZE            = 12;
    private static final int TITLE_FONT_STYLE           = Font.BOLD;
    private static final String TITLE_FONT_FACE_MAC     = "Franklin Gothic Medium";
    private static final String TITLE_FONT_FACE         = "Sans Serif";

    private static final Color COLOR_BORDER             = Color.decode("#797979");
    private static final Color COLOR_GRADIENT_TOP       = Color.decode("#0c98c2");
    private static final Color COLOR_GRADIENT_BOTTOM    = Color.decode("#086091");
    private static final Color COLOR_TITLE_SHADOW       = Color.decode("#000040");

    private ShadowLabel     titleLabel;
    private JLabel          subtitleLabel;
    private JPanel          headerPanel;
    private JComponent      headerCtrlComp;
    private boolean         isSelected;

    // Instance Creation ****************************************************

    /**
     * Constructs a <code>CoolInternalPane</code> with the specified title.
     *
     * @param title the initial title
     */
    public CoolInternalFrame(String title)
    {
        this(title, null, null, null);
    }

    /**
     * Constructs a <code>CoolInternalPane</code> with the specified title, tool bar, and content
     * panel.
     *
     * @param title       initial title.
     * @param subtitle    initial subtitle.
     * @param hdrControls initial header controls component.
     * @param content     initial content pane.
     */
    public CoolInternalFrame(String title, String subtitle, JComponent hdrControls,
        JComponent content)
    {
        this(null, title, subtitle, hdrControls, content);
    }

    /**
     * Constructs a <code>CoolInternalPane</code> with the specified parameters.
     *
     * @param icon        initial icon.
     * @param title       initial title.
     * @param subtitle    initial subtitle.
     * @param hdrControls initial header controls component.
     * @param content     initial content pane.
     */
    public CoolInternalFrame(Icon icon, String title, String subtitle, JComponent hdrControls,
        JComponent content)
    {
        super(new BorderLayout());
        isSelected = false;

        // Build the labels
        titleLabel = new ShadowLabel(title, icon, SwingConstants.LEADING, COLOR_TITLE_SHADOW);
        subtitleLabel = new JLabel(subtitle);
        setupLabels();

        // Build the header and add it to the whole frame
        headerCtrlComp = hdrControls;
        JPanel header = buildHeader();
        add(header, BorderLayout.NORTH);
        
        // If there is a content Component, then add it
        if (content != null) setContent(content);

        // Final steps
        setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        setSelected(true);
        updateHeader();
    }

    // Public API ***********************************************************

    /**
     * Returns the frame's icon.
     *
     * @return the frame's icon.
     */
    public Icon getFrameIcon()
    {
        return titleLabel.getIcon();
    }

    /**
     * Sets a new frame icon.
     *
     * @param newIcon the icon to be set.
     */
    public void setFrameIcon(Icon newIcon)
    {
        Icon oldIcon = getFrameIcon();
        titleLabel.setIcon(newIcon);
        firePropertyChange("frameIcon", oldIcon, newIcon);
    }

    /**
     * Returns the frame's title text.
     *
     * @return String the current title text.
     */
    public String getTitle()
    {
        return titleLabel.getText();
    }

    /**
     * Sets a new title text.
     *
     * @param newText the title text to be set.
     */
    public void setTitle(String newText)
    {
        String oldText = getTitle();
        titleLabel.setText(newText);
        firePropertyChange("title", oldText, newText);
    }

    /**
     * Sets the subtitle text.
     *
     * @param newText title text to be set.
     */
    public void setSubtitle(String newText)
    {
        String oldText = getSubTitle();
        subtitleLabel.setText(newText);
        firePropertyChange("title", oldText, newText);
    }

    /**
     * Returns subtitle text.
     *
     * @return text.
     */
    private String getSubTitle()
    {
        return subtitleLabel.getText();
    }

    /**
     * Returns the current header controls component, null if none has been set before.
     *
     * @return the current header control component (if any).
     */
    public JComponent getHeaderCtrlComp()
    {
        return headerCtrlComp;
    }

    /**
     * Sets a new header control component.
     *
     * @param newhdrContrlComp the header control component to be set in the header.
     */
    public void setHeaderControl(JComponent newhdrContrlComp)
    {
        JComponent oldhdrContrlComp = getHeaderCtrlComp();
        if (oldhdrContrlComp == newhdrContrlComp) return;

        if (oldhdrContrlComp != null)
        {
            headerPanel.remove(oldhdrContrlComp);
        }

        if (newhdrContrlComp != null)
        {
            newhdrContrlComp.setBorder(null);
            CellConstraints cc = new CellConstraints();
                headerPanel.add(newhdrContrlComp, cc.xywh(5, 2, 1, 3, "r, c"));
        }
        headerCtrlComp = newhdrContrlComp;
        updateHeader();
        firePropertyChange("toolBar", oldhdrContrlComp, newhdrContrlComp);
    }

    /**
     * Returns the content or null, if none has been set.
     *
     * @return the current content.
     */
    public Component getContent()
    {
        return hasContent() ? getComponent(1) : null;
    }

    /**
     * Sets a new panel content; replaces any existing content, if existing.
     *
     * @param newContent the panel's new content.
     */
    public void setContent(Component newContent)
    {
        Component oldContent = getContent();
        if (hasContent()) remove(oldContent);
        add(newContent, BorderLayout.CENTER);
        firePropertyChange("content", oldContent, newContent);
    }

    /**
     * Answers if the panel is currently selected (or in other words active) or not. In the selected
     * state, the header background will be rendered differently.
     *
     * @return boolean a boolean, where true means the frame is selected (currently active) and
     *         false means it is not.
     */
    public boolean isSelected()
    {
        return isSelected;
    }

    /**
     * This panel draws its title bar differently if it is selected, which may be used to indicate
     * to the user that this panel has the focus, or should get more attention than other simple
     * internal frames.
     *
     * @param newValue a boolean, where true means the frame is selected (currently active) and
     *                 false means it is not.
     */
    public void setSelected(boolean newValue)
    {
        boolean oldValue = isSelected();
        isSelected = newValue;
        updateHeader();
        firePropertyChange("selected", oldValue, newValue);
    }

    // Building *************************************************************

    /**
     * Creates the appropriately laied out Header for the CoolInternalFrame.
     *
     * @return the header.
     */
    private JPanel buildHeader()
    {
        FormLayout layout = new FormLayout(
            "11px, pref, min:grow, 2px, pref, 11px",
            "6px, pref, 1px, pref, 8px");
        headerPanel = new GradientPanel(layout);

        CellConstraints cc = new CellConstraints();
        headerPanel.add(titleLabel, cc.xy(2, 2));
        headerPanel.add(subtitleLabel, cc.xyw(2, 4, 3));
        if (headerCtrlComp != null) headerPanel.add(headerCtrlComp, cc.xywh(5, 2, 1, 3, "r, c"));

        headerPanel.setOpaque(SystemUtils.IS_OS_MAC);
        return headerPanel;
    }

    /**
     * Set up the desired forns and opacities for the two labels.
     */
    private void setupLabels()
    {
        titleLabel.setOpaque(false);
        titleLabel.setFont(new Font(TITLE_FONT_FACE, TITLE_FONT_STYLE, TITLE_FONT_SIZE));

        subtitleLabel.setOpaque(false);
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(9.0f));
    }

    /**
     * Updates the header.
     */
    private void updateHeader()
    {
        headerPanel.setOpaque(isSelected());
        titleLabel.setForeground(getTextForeground(isSelected()));
        subtitleLabel.setForeground(getSubTitleForeground(isSelected()));

        if (headerCtrlComp != null)
        {
            headerCtrlComp.setOpaque(false);
            headerCtrlComp.setBackground(Color.RED);
        }

        headerPanel.repaint();
    }

    /**
     * Returns sub-title foreground color.
     *
     * @param selected if the sub-title is selected.
     *
     * @return color.
     */
    protected Color getSubTitleForeground(boolean selected)
    {
        return getTextForeground(selected);
    }

    /**
     * Updates the UI. In addition to the superclass behavior, we need to update the header
     * component.
     */
    public void updateUI()
    {
        super.updateUI();
        if (titleLabel != null) updateHeader();
    }

    // Helper Code **********************************************************

    /**
     * Checks and answers if the panel has a content component set.
     *
     * @return true if the panel has a content, false if it's empty.
     */
    private boolean hasContent()
    {
        return getComponentCount() > 1;
    }

    /**
     * Determines and answers the header's text foreground color. Tries to lookup a special color
     * from the L&amp;F. In case it is absent, it uses the standard internal frame forground.
     *
     * @param selected true to lookup the active color, false for the inactive.
     *
     * @return the color of the foreground text.
     */
    protected Color getTextForeground(boolean selected)
    {
        return Color.WHITE;
    }

    /**
     * A panel with a horizontal gradient background.
     */
    private static final class GradientPanel extends JPanel
    {
        /**
         * Creates panel.
         *
         * @param lm         layout manager.
         */
        private GradientPanel(LayoutManager lm)
        {
            super(lm);
        }

        /**
         * Paints component on a given graphics context.
         *
         * @param g graphics context.
         */
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            if (isOpaque())
            {
                Color color1 = COLOR_GRADIENT_TOP;
                Color color2 = COLOR_GRADIENT_BOTTOM;

                int width = getWidth();
                int height = getHeight();

                Graphics2D g2 = (Graphics2D)g;
                Paint storedPaint = g2.getPaint();
                g2.setPaint(new GradientPaint(0, 0, color1, 0, height, color2));
                g2.fillRect(0, 0, width, height);
                g2.setPaint(storedPaint);

                Color lighter = COLOR_GRADIENT_TOP;
                g2.setColor(lighter);
                g2.drawLine(0, height - 2, width, height - 2);
            }
        }
    }
}