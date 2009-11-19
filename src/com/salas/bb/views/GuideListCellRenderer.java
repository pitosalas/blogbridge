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
// $Id: GuideListCellRenderer.java,v 1.46 2007/04/17 13:43:26 spyromus Exp $
//

package com.salas.bb.views;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.core.GuideDisplayModeManager;
import com.salas.bb.domain.IGuide;
import com.salas.bb.utils.ResourceID;
import com.salas.bb.utils.uif.IconSource;
import com.salas.bb.views.mainframe.UnreadButton;
import com.salas.bb.views.settings.RenderingManager;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.text.MessageFormat;

/**
 * Renderer for cells of guides list.
 */
public class GuideListCellRenderer extends JPanel
        implements ListCellRenderer
{
    /** Guide cell top margin, in pixels. */
    public static final int CELL_MARGIN_TOP = 4;
    /** Guide cell bottom margin, in pixels. */
    public static final int CELL_MARGIN_BOTTOM = 4;
    /** Guide cell left margin, in pixels. */
    public static final int CELL_MARGIN_LEFT = 0;
    /** Guide cell right margin, in pixels. */
    public static final int CELL_MARGIN_RIGHT = 1;

    private static final float GUIDE_LABEL_FONT_SIZE = 10f;
    private static final Border BORDER_NO_FOCUS = BorderFactory.createEmptyBorder(1, 1, 1, 1);

    private GuidesPanel.UnreadController unreadController;
    private IGuideCellRenderer renderer;

    /**
     * Constructs new renderer.
     *
     * @param anUnreadController unread controller.
     */
    public GuideListCellRenderer(GuidesPanel.UnreadController anUnreadController)
    {
        unreadController = anUnreadController;
        updateRendererAndLayout();
    }

    /**
     * Invoked when the size of an icon changes.
     */
    public void onIconSizeChange()
    {
        updateRendererAndLayout();
    }

    /** Initialzes sub-renderer. */
    public void updateRendererAndLayout()
    {
        boolean largeIcon = RenderingManager.isBigIconInGuides() &&
            RenderingManager.isShowIconInGuides();

        if (renderer == null || (largeIcon && renderer instanceof SmallIconGuideCellRenderer) ||
            (!largeIcon && renderer instanceof LargeIconGuideCellRenderer))
        {
            renderer = largeIcon
                ? new LargeIconGuideCellRenderer()
                : new SmallIconGuideCellRenderer();
        }

        updateLayout();
    }

    /**
     * Set up the cell layout based on current settings. 
     */
    private void updateLayout()
    {
        renderer.showUnreadButton(RenderingManager.isShowUnreadInGuides());
        renderer.showIcon(RenderingManager.isShowIconInGuides());
        renderer.showText(RenderingManager.isShowTextInGuides());
    }

    /**
     * Return a component that has been configured to display the specified
     * value. That component's <code>paint</code> method is then called to
     * "render" the cell.  If it is necessary to compute the dimensions
     * of a list because the list cells do not have a fixed size, this method
     * is called to generate a component on which <code>getPreferredSize</code>
     * can be invoked.
     *
     * @param list         The JList we're painting.
     * @param value        The value returned by list.getModel().getElementAt(index).
     * @param index        The cells index.
     * @param isSelected   True if the specified cell was selected.
     * @param cellHasFocus True if the specified cell has the focus.
     *
     * @return A component whose paint() method will render the specified value.
     *
     * @see javax.swing.JList
     * @see javax.swing.ListSelectionModel
     * @see javax.swing.ListModel
     */
    public Component getListCellRendererComponent(final JList list, final Object value,
                                                  final int index, final boolean isSelected,
                                                  final boolean cellHasFocus)
    {
        IGuide cg = (IGuide)value;

        // Text
        String textName = cg.getTitle();
        renderer.setText(textName);

        // Icon
        final String iconKey = cg.getIconKey() == null
            ? ResourceID.ICON_GUIDE_DEFAULT_KEY : cg.getIconKey();

        ImageIcon icon = IconSource.getIcon(iconKey);
        if (icon == null) icon = IconSource.getIcon(ResourceID.ICON_GUIDE_DEFAULT_KEY);
        renderer.setIcon(icon);

        // Unread counter
        renderer.setUnreadCount(unreadController.deferCalcUnreadStats(cg));

        // Colors
        Color background = isSelected
            ? RenderingManager.getFeedsListSelectedBackground()
            : RenderingManager.getFeedsListBackground(index % 2 == 0);

        Color foreground = GuideDisplayModeManager.getInstance().getColor(cg, isSelected);
        if (foreground == null || isSelected)
        {
            foreground = RenderingManager.getFeedsListForeground(isSelected);
        }
        renderer.setColors(background, foreground);

        // Border
        Border border = GlobalModel.SINGLETON.getSelectedGuide() == value && list.isFocusOwner()
            ? UIManager.getBorder("List.focusCellHighlightBorder")
            : BORDER_NO_FOCUS;
        renderer.setBorder(border);

        return renderer.getComponent();
    }

    /**
     * Returns the height necessary to display a cell renderer by current renderer.
     *
     * @return the height of a cell.
     */
    public int getRequiredHeight()
    {
        renderer.setText("ty");
        renderer.setIcon(IconSource.getIcon(ResourceID.ICON_GUIDE_DEFAULT_KEY));
        return renderer.getComponent().getPreferredSize().height;
    }

    /**
     * Number of pixels the unread button is away from the top.
     *
     * @return pixels.
     */
    public int getUnreadButtonYOffset()
    {
        return renderer.getUnreadButtonYOffset();
    }

    /**
     * The interface to be implemented
     */
    private static interface IGuideCellRenderer
    {
        /**
         * Sets the text to be shown.
         *
         * @param text text.
         */
        void setText(String text);

        /**
         * Sets the icon to be shown.
         *
         * @param icon icon.
         */
        void setIcon(ImageIcon icon);

        /**
         * Sets under of unread elements to be shown.
         *
         * @param unread unread count.
         */
        void setUnreadCount(int unread);

        /**
         * Sets background and foreground colors.
         *
         * @param background background.
         * @param foreground foreground.
         */
        void setColors(Color background, Color foreground);

        /**
         * Sets the border around the cell.
         *
         * @param border border.
         */
        void setBorder(Border border);

        /**
         * Shows / hides unread button.
         *
         * @param show unread button state.
         */
        void showUnreadButton(boolean show);

        /**
         * Shows / hides icon.
         *
         * @param show icon state.
         */
        void showIcon(boolean show);

        /**
         * Shows / hides text.
         *
         * @param show text state.
         */
        void showText(boolean show);

        /**
         * Returns the component to display.
         *
         * @return component.
         */
        Component getComponent();

        /**
         * Number of pixels the unread button is away from the top.
         *
         * @return pixels.
         */
        int getUnreadButtonYOffset();
    }

    /**
     * Guide cell renderer with big icon, label and optional unread button.
     */
    private static class LargeIconGuideCellRenderer extends JPanel
        implements IGuideCellRenderer
    {
        private static final String COLS =
            MessageFormat.format("{0}px, center:0:grow, right:p, {1}px", CELL_MARGIN_LEFT, CELL_MARGIN_RIGHT);
        private static final String ROWS =
            MessageFormat.format("{0}px, p, {1}px", CELL_MARGIN_TOP, CELL_MARGIN_BOTTOM);

        private JLabel          iconLabel;
        private UnreadButton    unreadButton;

        private boolean         iconVisible;
        private boolean         textVisible;

        /**
         * Creates the renderer.
         */
        public LargeIconGuideCellRenderer()
        {
            iconLabel = new JLabel(ResourceUtils.getIcon(ResourceID.ICON_GUIDE_DEFAULT_KEY));
            iconLabel.setFont(iconLabel.getFont().deriveFont(GUIDE_LABEL_FONT_SIZE));
            iconLabel.setHorizontalTextPosition(SwingConstants.CENTER);
            iconLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
            iconLabel.setAlignmentY(CENTER_ALIGNMENT);
            iconLabel.setIconTextGap(1);

            unreadButton = new UnreadButton();

            init();
        }

        /** Initializes the view. */
        private void init()
        {
            // The unread button is in the upper-right corner.
            // The iconLabel is centered over both cells.  This means that
            // it overlaps with the unreadButton, but because the icons tend
            // to be narrow the images don't collide, and this ensures that the
            // guide title can be laid out over the full width.

            setLayout(new FormLayout(COLS, ROWS));
            CellConstraints cc = new CellConstraints();

            add(iconLabel, cc.xyw(2, 2, 2));
            add(unreadButton, cc.xy(3, 2, "center,top"));
        }

        /**
         * Number of pixels the unread button is away from the top.
         *
         * @return pixels.
         */
        public int getUnreadButtonYOffset()
        {
            return 0;
        }

        /**
         * Returns the component to display.
         *
         * @return component.
         */
        public Component getComponent()
        {
            return this;
        }

        /**
         * Sets background and foreground colors.
         *
         * @param background background.
         * @param foreground foreground.
         */
        public void setColors(Color background, Color foreground)
        {
            setBackground(background);
            setForeground(foreground);
            iconLabel.setForeground(foreground);
        }

        /**
         * Sets the icon to be shown.
         *
         * @param icon icon.
         */
        public void setIcon(ImageIcon icon)
        {
            iconLabel.setIcon(iconVisible ? icon : null);
        }

        /**
         * Sets the text to be shown.
         *
         * @param text text.
         */
        public void setText(String text)
        {
            iconLabel.setText(textVisible ? text : null);
        }

        /**
         * Sets under of unread elements to be shown.
         *
         * @param unread unread count.
         */
        public void setUnreadCount(int unread)
        {
            unreadButton.init(unread);
        }

        /**
         * Shows / hides unread button.
         *
         * @param show unread button state.
         */
        public void showUnreadButton(boolean show)
        {
            unreadButton.setVisible(show);
        }

        /**
         * Shows / hides icon.
         *
         * @param show icon state.
         */
        public void showIcon(boolean show)
        {
            iconVisible = show;
        }

        /**
         * Shows / hides text.
         *
         * @param show text state.
         */
        public void showText(boolean show)
        {
            textVisible = show;
        }
    }

    /**
     * Small icon, label and optional button. This renderer is special in a
     * way it renders a big icon as small.
     */
    private static class SmallIconGuideCellRenderer extends JPanel
        implements IGuideCellRenderer
    {
        private static final String COLS =
            MessageFormat.format("{0}px, 4px, p, 4px, left:0:grow, 4px, p, {1}px", CELL_MARGIN_LEFT, CELL_MARGIN_RIGHT);
        private static final String ROWS =
            MessageFormat.format("{0}px, p, {1}px", CELL_MARGIN_TOP, CELL_MARGIN_BOTTOM);

        private final ScalingIcon   icon;
        private final JLabel        text;
        private final UnreadButton  unreadButton;
        private final int           unreadButtonYOffset;

        /** Creates a new <code>JPanel</code> with a double buffer and a flow layout. */
        public SmallIconGuideCellRenderer()
        {
            icon = new ScalingIcon(new Dimension(16, 16));
            text = new JLabel();
            text.setFont(text.getFont().deriveFont(GUIDE_LABEL_FONT_SIZE));
            unreadButton = new UnreadButton();
            unreadButtonYOffset = (16 - unreadButton.getSize().height) / 2;

            setLayout(new FormLayout(COLS, ROWS));
            CellConstraints cc = new CellConstraints();
            add(icon, cc.xy(3, 2));
            add(text, cc.xy(5, 2));
            add(unreadButton, cc.xy(7, 2));
        }

        /**
         * Number of pixels the unread button is away from the top.
         *
         * @return pixels.
         */
        public int getUnreadButtonYOffset()
        {
            return icon.isVisible() ? unreadButtonYOffset : 0;
        }

        /**
         * Returns the component to display.
         *
         * @return component.
         */
        public Component getComponent()
        {
            return this;
        }

        /**
         * Sets background and foreground colors.
         *
         * @param background background.
         * @param foreground foreground.
         */
        public void setColors(Color background, Color foreground)
        {
            setBackground(background);
            setForeground(foreground);
            text.setForeground(foreground);
        }

        /**
         * Sets the icon to be shown.
         *
         * @param anIcon icon.
         */
        public void setIcon(ImageIcon anIcon)
        {
            icon.setIcon(anIcon);
        }

        /**
         * Sets the text to be shown.
         *
         * @param aText text.
         */
        public void setText(String aText)
        {
            text.setText(aText);
        }

        /**
         * Sets under of unread elements to be shown.
         *
         * @param unread unread count.
         */
        public void setUnreadCount(int unread)
        {
            unreadButton.init(unread);
        }

        /**
         * Shows / hides unread button.
         *
         * @param show unread button state.
         */
        public void showUnreadButton(boolean show)
        {
            unreadButton.setVisible(show);
        }

        /**
         * Shows / hides icon.
         *
         * @param show icon state.
         */
        public void showIcon(boolean show)
        {
            icon.setVisible(show);
        }

        /**
         * Shows / hides text.
         *
         * @param show text state.
         */
        public void showText(boolean show)
        {
            text.setVisible(show);
        }
    }

    /**
     * Scales icon to given dimensions.
     */
    private static class ScalingIcon extends JComponent
    {
        private final Dimension dimensions;

        private ImageIcon icon;

        /**
         * Creates icon displayer which shrinks or enlarges icon to the known dimensions.
         *
         * @param aDimensions dimensions.
         */
        public ScalingIcon(Dimension aDimensions)
        {
            dimensions = aDimensions;
            icon = null;
        }

        /**
         * Returns the preferred size.
         *
         * @return the value of the <code>preferredSize</code> property
         */
        public Dimension getPreferredSize()
        {
            return dimensions;
        }

        /**
         * Sets the icon to paint.
         *
         * @param anIcon icon.
         */
        public void setIcon(ImageIcon anIcon)
        {
            icon = anIcon;
            repaint();
        }

        /**
         * Invoked by Swing to draw components.
         *
         * @param g the <code>Graphics</code> context in which to paint
         */
        public void paint(Graphics g)
        {
            if (icon != null)
            {
                Image image = icon.getImage();
                image = image.getScaledInstance(dimensions.width, dimensions.height, Image.SCALE_SMOOTH);

                g.drawImage(image, 0, 0, null);
            }
        }
    }
}
