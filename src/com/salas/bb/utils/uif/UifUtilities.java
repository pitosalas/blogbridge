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
// $Id: UifUtilities.java,v 1.35 2007/09/12 11:27:17 spyromus Exp $
//

package com.salas.bb.utils.uif;

import com.jgoodies.uif.application.Application;
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collection of UIF utilities.
 */
public final class UifUtilities
{
    private static final Logger LOG = Logger.getLogger(UifUtilities.class.getName());

    private static final ThreadLocal<Boolean> EDT_FLAG = new ThreadLocal<Boolean>();
    private static final JLabel FONT_LABEL = new JLabel();

    /**
     * Hidden utility class constructor.
     */
    private UifUtilities()
    {
    }

    /**
     * Goes to the root of component hierarchy until finds Frame or null.
     *
     * @param obj object to start searching.
     *
     * @return owner frame.
     */
    public static Frame findOwnerFrame(Object obj)
    {
        return (obj instanceof Component)
            ? findComponentOwnerFrame((Component)obj)
            : findComponentOwnerFrame(null);
    }

    /**
     * Goes to the root of component hierarchy until finds Frame or null.
     *
     * @param component start of search.
     *
     * @return owner frame.
     */
    public static Frame findComponentOwnerFrame(Component component)
    {
        Frame owner;

        if (component == null)
        {
            owner = Application.getDefaultParentFrame();
        } else
        {
            if (component instanceof Frame)
            {
                owner = (Frame)component;
            } else
            {
                owner = findComponentOwnerFrame(component.getParent());
            }
        }

        return owner;
    }

    /**
     * Returns TRUE if current thread is EDT.
     *
     * @return TRUE if current thread is EDT.
     */
    public static boolean isEDT()
    {
        Boolean bool;

        synchronized (EDT_FLAG)
        {
            bool = EDT_FLAG.get();
            if (bool == null)
            {
                bool = SwingUtilities.isEventDispatchThread();
                EDT_FLAG.set(bool);
            }
        }

        return bool;
    }

    /**
     * Sets the width of column to fixed value.
     *
     * @param table     table.
     * @param column    index of column.
     * @param width     width value.
     *
     * @return column object.
     */
    public static TableColumn setTableColWidth(JTable table, int column, int width)
    {
        TableColumn col = table.getColumnModel().getColumn(column);
        col.setMinWidth(width);
        col.setMaxWidth(width);

        return col;
    }

    /**
     * Takes current font of the component and makes it smaller.
     *
     * @param component component to adjust.
     *
     * @return smaller font.
     */
    public static Font smallerFont(Component component)
    {
        Font font = component.getFont();
        component.setFont(applyFontBias(font, -1));
        return font;
    }

    /**
     * Apply a bias to grow or shrink a given font and return a new one.
     *
     * @param font  font to apply bias to.
     * @param bias  bias value in size points.
     *
     * @return returns the biased font.
     * 
     * @throws NullPointerException if font is not specified.
     */
    public static Font applyFontBias(Font font, int bias)
    {
        if (font == null) throw new NullPointerException(Strings.error("unspecified.font"));

        if (bias != 0)
        {
            float oldSize = font.getSize2D();
            font = font.deriveFont(oldSize + bias);
        }

        return font;
    }

    /**
     * Sends event to parent in it's coordinate space.
     *
     * @param component component to get parent from.
     * @param e         event to send.
     */
    public static void delegateEventToParent(Component component, MouseEvent e)
    {
        delegateEventToParent(component, e, false);
    }

    /**
     * Sends event to parent in it's coordinate space (if <code>direct</code> isn't set).
     *
     * @param component component to get parent from.
     * @param e         event to send.
     * @param direct    <code>TRUE</code> for direct delegation (no conversion to parent coord. space).
     */
    public static void delegateEventToParent(Component component, MouseEvent e, boolean direct)
    {
        Component parent = component.getParent();
        delegateEventToParent(component, parent, e, direct);
    }

    /**
     * Sends event to parent in it's coordinate space (if <code>direct</code> isn't set).
     *
     * @param component component to get parent from.
     * @param parent    parent component.
     * @param e         event to send.
     * @param direct    <code>TRUE</code> for direct delegation (no conversion to parent coord. space).
     */
    public static void delegateEventToParent(Component component, Component parent, MouseEvent e, boolean direct)
    {
        if (parent != null)
        {
            if (direct)
            {
                e = new MouseEvent(component, e.getID(), e.getWhen(), e.getModifiers(),
                    e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton());
            } else
            {
                Point point = e.getPoint();
                SwingUtilities.convertPointToScreen(point, component);
                SwingUtilities.convertPointFromScreen(point, parent);
                e = new MouseEvent(parent, e.getID(), e.getWhen(), e.getModifiers(),
                    (int)point.getX(), (int)point.getY(), e.getClickCount(), e.isPopupTrigger(),
                    e.getButton());
            }

            parent.dispatchEvent(e);
        }
    }

    /**
     * Sends event to parent.
     *
     * @param component component to get parent from.
     * @param e         event to send.
     */
    public static void delegateEventToParent(Component component, AWTEvent e)
    {
        Component parent = component.getParent();
        if (parent != null) parent.dispatchEvent(e);
    }

    /**
     * Sets preferred width of the component.
     *
     * @param comp  component.
     * @param width width.
     */
    public static void setPreferredWidth(JComponent comp, int width)
    {
        Dimension size = comp.getPreferredSize();
        size.width = width;
        comp.setPreferredSize(size);
    }

    /**
     * Estimates the width of the message being printed using the given font.
     *
     * @param font  font used.
     * @param msg   the message.
     *
     * @return width.
     */
    public static int estimateWidth(Font font, String msg)
    {
        return FONT_LABEL.getFontMetrics(font).stringWidth(msg);
    }

    /**
     * Paints the antialised text only if "swing.aatext" property is defined and the graphics
     * context is subclass of {@link Graphics2D}.
     *
     * @param g     context.
     * @param text  text.
     * @param x     x.
     * @param y     y.
     */
    public static void drawAAString(Graphics g, String text, int x, int y)
    {
        Graphics2D g2 = (System.getProperty("swing.aatext") != null && g instanceof Graphics2D)
            ? (Graphics2D)g : null;

        Object oldAAValue = null;
        if (g2 != null)
        {
            oldAAValue = g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        g.drawString(text, x, y);

        if (g2 != null)
        {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, oldAAValue);
        }
    }

    /**
     * Returns string signature of the font.
     *
     * @param fnt font.
     *
     * @return signature.
     */
    public static String fontToString(Font fnt)
    {
        String str = null;

        if (fnt != null)
        {
            String strStyle;

            if (fnt.isBold())
            {
                strStyle = fnt.isItalic() ? "bolditalic" : "bold";
            } else
            {
                strStyle = fnt.isItalic() ? "italic" : "plain";
            }

            str = fnt.getFamily() + "-" + strStyle + "-" + fnt.getSize();
        }

        return str;
    }

    /**
     * Converts color to HEX representation, like "#0012a5".
     *
     * @param color color.
     *
     * @return HEX or empty string if color is <code>NULL</code>.
     */
    public static String colorToHex(Color color)
    {
        if (color == null) return "";

        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        StringBuffer str = new StringBuffer("#");
        if (red < 16) str.append("0");
        str.append(Integer.toHexString(red));
        if (green < 16) str.append("0");
        str.append(Integer.toHexString(green));
        if (blue < 16) str.append("0");
        str.append(Integer.toHexString(blue));

        return str.toString();
    }

    public static void setFontAttributes(HTMLDocument doc, String styleName, Font font)
    {
        setFontAttributes(doc.getStyle(styleName), font);

        String css = "ul, ol { font: normal " + font.getSize() + "pt " + font.getFamily() + " }";
        try
        {
            doc.getStyleSheet().loadRules(new StringReader(css), null);
        } catch (IOException e)
        {
            LOG.log(Level.SEVERE, "Couldn't load new stylesheet", e);
        }
    }

    /**
     * Assigns given font to the style.
     *
     * @param style style.
     * @param font  font.
     */
    public static void setFontAttributes(Style style, Font font)
    {
        StyleConstants.setFontFamily(style, font.getFontName());
        style.addAttribute(StyleConstants.CharacterConstants.Size,
            Integer.toString(font.getSize()));
    }

    /**
     * Puts the text style over the text in current document.
     *
     * @param control   text control.
     * @param styleName style name.
     */
    public static void installTextStyle(JEditorPane control, String styleName)
    {
        HTMLDocument doc = (HTMLDocument)control.getDocument();
        doc.setCharacterAttributes(0, control.getDocument().getLength(),
            doc.getStyle(styleName), false);
    }

    /**
     * Invokes the task and waits for the completion. Reports no failure.
     *
     * @param task          task to invoke.
     *
     * @return TRUE if completed without errors.
     */
    public static boolean invokeAndWait(Runnable task)
    {
        return invokeAndWait(task, null, null);
    }

    /**
     * Invokes the task and waits for the completion.
     *
     * @param task          task to invoke.
     * @param failMessage   message to output if failed.
     * @param failureLevel  level of failure.
     *
     * @return TRUE if completed without errors.
     */
    public static boolean invokeAndWait(Runnable task, String failMessage, Level failureLevel)
    {
        boolean errorless = false;

        if (UifUtilities.isEDT())
        {
            task.run();
        } else
        {
            try
            {
                SwingUtilities.invokeAndWait(task);
                errorless = true;
            } catch (Throwable e)
            {
                if (failMessage != null && failureLevel != null) LOG.log(failureLevel, failMessage, e);
            }
        }

        return errorless;
    }

    /**
     * Adds dependancy between two check boxes, so that the slave is enabled only when master is checked.
     *
     * @param master    master.
     * @param slave     slave.
     */
    public static void setDependency(final JCheckBox master, final JCheckBox slave)
    {
        if (master == null || slave == null) return;

        slave.setEnabled(master.isSelected());
        master.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                slave.setEnabled(master.isSelected());
            }
        });
    }

    /**
     * Sets editor font.
     *
     * @param font font.
     * @param editor editor to set font of.
     */
    public static void setEditorFont(JEditorPane editor, Font font)
    {
        Document document = editor.getDocument();
        if (document instanceof HTMLDocument)
        {
            HTMLDocument htmlDocument = (HTMLDocument)document;

            String css = "body { font: normal " + font.getSize() + "pt " + font.getFamily() + " }";
            try
            {
            htmlDocument.getStyleSheet().loadRules(new StringReader(css), null);
            } catch (IOException e)
            {
                LOG.log(Level.SEVERE, "Couldn't load new stylesheet", e);
            }
        }
    }

    /**
     * Creates a basic plan icon component.
     *
     * @param visible <code>TRUE</code> if it should be visible.
     *
     * @return component.
     */
    public static JLabel makeBasicPlanIcon(boolean visible)
    {
        return makePlanIcon(visible, "plan.basic.icon", "ptb.prefs.basic.tooltip");
    }

    /**
     * Creates a publisher plan icon component.
     *
     * @param visible <code>TRUE</code> if it should be visible.
     *
     * @return component.
     */
    public static JLabel makePublisherPlanIcon(boolean visible)
    {
        return makePlanIcon(visible, "plan.publisher.icon", "ptb.prefs.pub.tooltip");
    }

    private static JLabel makePlanIcon(boolean visible, String rsIcon, String rsTooltip)
    {
        JLabel icn;

        if (visible)
        {
            icn = new JLabel(ResourceUtils.getIcon(rsIcon));
            icn.setToolTipText(Strings.message(rsTooltip));
        } else icn = new JLabel("");

        return icn;
    }

    /**
     * Every list component during construction registers a special handler that
     * lets the user type letters and select elements in the list. When you
     * don't need this functionality, there's no way to disable it other than
     * remove the listener in a kluggie way.
     *
     * @param list component to operate.
     */
    public static void removeTypeSelectionListener(JList list)
    {
        KeyListener[] kls = list.getListeners(KeyListener.class);
        for (KeyListener kl : kls)
        {
            String name = kl.getClass().getName();
            if (name.endsWith("BasicListUI$Handler")) list.removeKeyListener(kl);
        }
    }

    /**
     * Updates a font in a component to be bold.
     *
     * @param component component.
     *
     * @return component for chaining
     */
    public static JComponent boldFont(JComponent component)
    {
        if (component == null) return null;

        component.setFont(component.getFont().deriveFont(Font.BOLD));
        return component;
    }
}
