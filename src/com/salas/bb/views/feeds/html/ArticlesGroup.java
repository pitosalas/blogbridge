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
// $Id: ArticlesGroup.java,v 1.21 2007/09/19 15:55:01 spyromus Exp $
//

package com.salas.bb.views.feeds.html;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.IArticle;
import com.salas.bb.utils.uif.ColExIconLabel;
import com.salas.bb.utils.uif.ShadowLabel;
import com.salas.bb.utils.uif.UpDownBorder;
import com.salas.bb.views.feeds.IArticleDisplay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Special component to represent the header of articles group.
 */
public class ArticlesGroup extends JPanel
{
    private static final Color COLOR_GRADIENT_TOP       = Color.WHITE;
    private static final Color COLOR_GRADIENT_BOTTOM    = Color.decode("#c5c5c5");

    private final ColExIconLabel    lbSign;
    private final ShadowLabel       lbTitle;
    private final MouseListener     popup;

    private boolean                 expanded;
    private List<IArticleDisplay>   displays;
    private boolean                 canBeVisible;
    private boolean                 visibleIfEmpty;

    /**
     * Creates article group.
     *
     * @param aName name of the group.
     * @param popup configures popup.
     */
    public ArticlesGroup(String aName, MouseListener popup)
    {
        this.popup = popup;
        canBeVisible = true;
        visibleIfEmpty = true;

        displays = new ArrayList<IArticleDisplay>();

        lbSign = new ColExIconLabel();
        lbSign.addMouseListener(new SignClickListener());
        lbTitle = new ShadowLabel(Color.WHITE);

        initGUI();

        setName(aName);
        setExpanded(true);

        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    }

    /**
     * Changes a font of the label.
     *
     * @param font new font.
     */
    public void setFont(Font font)
    {
        if (lbTitle != null) lbTitle.setFont(font);
    }

    /**
     * Returns <code>TRUE</code> if is currently expanded.
     *
     * @return <code>TRUE</code> if is currently expanded.
     */
    boolean isExpanded()
    {
        return expanded;
    }

    /**
     * Expands / collapses the views.
     *
     * @param exp <code>TRUE</code> to expand.
     */
    public void setExpanded(boolean exp)
    {
        if (expanded != exp)
        {
            expanded = exp;

            // Show / hide views
            for (IArticleDisplay display : displays)
            {
                display.getComponent().setVisible(exp);
            }

            // Change icon
            lbSign.setCollapsed(!exp);
        }
    }

    /**
     * Changes the name of the group.
     *
     * @param name name of the group.
     */
    public void setName(String name)
    {
        super.setName(name);

        lbTitle.setText(name);
    }

    /**
     * Registers view this group should manage.
     *
     * @param display view.
     */
    public void register(IArticleDisplay display)
    {
        if (!displays.contains(display))
        {
            displays.add(display);
            display.getComponent().setVisible(expanded);
            reviewVisibility();
        }
    }

    /**
     * Unregisters article display.
     *
     * @param display article display.
     */
    public void unregister(IArticleDisplay display)
    {
        displays.remove(display);
        reviewVisibility();
    }

    /**
     * Unregisters all views.
     */
    public void unregisterAll()
    {
        displays.clear();
        reviewVisibility();
    }

    /** GUI initialization. */
    private void initGUI()
    {
        setLayout(new FormLayout("5dlu, center:min, 5dlu, left:min:grow, 5dlu",
            "1dlu, pref, 1dlu"));

        CellConstraints cc = new CellConstraints();
        add(lbSign, cc.xy(2, 2));
        add(lbTitle, cc.xy(4, 2));

        setBorder(new UpDownBorder(Color.decode("#7a7a7a")));
    }

    /**
     * Sets flat to show groups or no.
     *
     * @param vis <code>TRUE</code> to make groups visible.
     */
    public void setCanBeVisible(boolean vis)
    {
        canBeVisible = vis;
        reviewVisibility();
    }

    /**
     * Sets flag to show group if it's empty or no.
     *
     * @param vis <code>TRUE</code> to show empty groups
     *            (if <code>canBeVisible</code> is <code>TRUE</code>).
     */
    public void setVisibleIfEmpty(boolean vis)
    {
        visibleIfEmpty = vis;
        reviewVisibility();
    }

    /**
     * Reviews current visibility mode.
     */
    private void reviewVisibility()
    {
        setVisible(canBeVisible &&
            (visibleIfEmpty || displays.size() > 0));
    }

    /**
     * Paints component.
     *
     * @param g the <code>Graphics</code> object to protect
     */
    protected void paintComponent(Graphics g)
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

//            g2.setColor(COLOR_BOTTOM_STROKE);
//            g2.drawLine(0, height - 1, width - 1, height - 1);

            paintBorder(g);
        }
    }

    @Override
    protected void processMouseEvent(MouseEvent e)
    {
        super.processMouseEvent(e);

        switch (e.getID())
        {
            case MouseEvent.MOUSE_PRESSED:
                if (popup != null) popup.mousePressed(e);
                break;

            case MouseEvent.MOUSE_RELEASED:
                if (popup != null) popup.mousePressed(e);
                break;

            default:
                break;
        }
    }

    /**
     * Marks articles of all associated displays as (un)read.
     *
     * @param markAsRead <code>TRUE</code> to mark as read.
     */
    public void markDisplays(boolean markAsRead)
    {
        List<IArticle> articles = new LinkedList<IArticle>();
        for (IArticleDisplay display : displays)
        {
            IArticle article = display.getArticle();
            if (article != null) articles.add(article);
        }

        // Mark articles as read and update statistics
        GlobalModel model = GlobalModel.SINGLETON;
        GlobalController.readArticles(markAsRead,
            model.getSelectedGuide(),
            model.getSelectedFeed(),
            articles.toArray(new IArticle[articles.size()]));
    }

    /**
     * Listener for clicks on expand / collaps sign.
     */
    private final class SignClickListener extends MouseAdapter
    {
        /**
         * Invoked when a mouse button has been pressed on a component.
         *
         * @param e mouse event object.
         */
        public void mousePressed(final MouseEvent e)
        {
            setExpanded(!expanded);
        }
    }
}
