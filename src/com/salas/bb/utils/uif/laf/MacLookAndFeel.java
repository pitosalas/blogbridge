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
// $Id: MacLookAndFeel.java,v 1.5 2007/05/14 15:16:32 spyromus Exp $
//

package com.salas.bb.utils.uif.laf;

import ch.randelshofer.quaqua.QuaquaComboBoxUI;
import ch.randelshofer.quaqua.QuaquaManager;
import ch.randelshofer.quaqua.VisualMargin;
import ch.randelshofer.quaqua.util.Images;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.IconUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.net.URL;

/**
 * Tweaked mac look and feel.
 */
public class MacLookAndFeel extends LAFProxy
{
    private UIDefaults lafDefaults;

    /**
     * Creates look and feel.
     */
    public MacLookAndFeel()
    {
        String lafClassName = "apple.laf.AquaLookAndFeel";

        try
        {
            Class lafClass = Class.forName(lafClassName);
            setLookAndFeel((LookAndFeel)lafClass.newInstance());
        } catch (ClassNotFoundException e)
        {
            throw new IllegalStateException(MessageFormat.format(
                Strings.error("failed.to.find.standard.aqua.laf"),
                new Object[] { lafClassName }));
        } catch (InstantiationException e)
        {
            throw new IllegalStateException(MessageFormat.format(
                Strings.error("failed.to.create.instance.of.aqua.laf"),
                new Object[] { lafClassName }));
        } catch (IllegalAccessException e)
        {
            throw new IllegalStateException(MessageFormat.format(
                Strings.error("failed.to.access.aqua.laf.class"),
                new Object[] { lafClassName }));
        }
    }

    /**
     * UIManager.setLookAndFeel calls this method before the first call (and typically the only
     * call) to getDefaults().  Subclasses should do any one-time setup they need here, rather than
     * in a static initializer, because look and feel class objects may be loaded just to discover
     * that isSupportedLookAndFeel() returns false.
     *
     * @see #uninitialize
     * @see javax.swing.UIManager#setLookAndFeel
     */
    public void initialize()
    {
        AccessController.doPrivileged(new PrivilegedAction()
        {
            public Object run()
            {
                laf.initialize();
                lafDefaults = laf.getDefaults();
                initResourceBundle(lafDefaults);
                initClassDefaults(lafDefaults);
                initComponentDefaults(lafDefaults);
                return null;
            }
        });
    }

    /**
     * Initializes component UI default class names.
     *
     * @param table defaults table.
     */
    protected void initClassDefaults(UIDefaults table)
    {
        String quaquaPrefix = "ch.randelshofer.quaqua.Quaqua";
        String quaquaPantherPrefix = "ch.randelshofer.quaqua.panther.QuaquaPanther";

        Object[] objects = new Object[]
        {
            "PopupMenuSeparatorUI", MacPopupMenuSeparatorUI.class.getName(),
            "ComboBoxUI", QuaquaComboBoxUI.class.getName(),
                // for JFileChooser
            "BrowserUI", quaquaPrefix + "BrowserUI",
            "FileChooserUI", quaquaPantherPrefix + "FileChooserUI",
        };
        table.putDefaults(objects);
    }

    /**
     * Initializes component defaults.
     *
     * @param table defaults table.
     */
    protected void initComponentDefaults(UIDefaults table)
    {
        // Set visual margin.
        int[] values = QuaquaManager.getProperty("Quaqua.visualMargin", new int[] { 3, 3, 3, 3});
        InsetsUIResource visualMargin = new InsetsUIResource(values[0], values[1], values[2], values[3]);

        // Opaqueness
        boolean opaque = QuaquaManager.getProperty("Quaqua.opaque", "false").equals("true");

        String commonDir = "/ch/randelshofer/quaqua/images/";

        Icon[] browserIcons = makeIcons(commonDir+"Browser.disclosureIcons.png", 4, true);
        Font smallSystemFont = SMALL_SYSTEM_FONT;

        Object[] objects = new Object[]
        {
            "PopupMenu.border", new UIDefaults.ProxyLazyValue(MacMenuBorder.class.getName()),
            "Separator.background", Color.decode("#f4f4f4"),
            "Separator.foreground", Color.decode("#dadada"),
            "ComboBox.border", new VisualMargin(2,2,2,2),
            "ComboBox.dropDownIcon", makeButtonStateIcon(commonDir + "ComboBox.dropDownIcons.png", 6),
            "ComboBox.opaque", opaque,
            "ComboBox.popupIcon", makeButtonStateIcon(commonDir + "ComboBox.popupIcons.png", 6),
            "ComboBox.smallPopupIcon", makeButtonStateIcon(commonDir + "ComboBox.small.popupIcons.png", 6),
            "ComboBox.smallDropDownIcon", makeButtonStateIcon(commonDir + "ComboBox.small.dropDownIcons.png", 6),

            // The values for this margin are ignored. We dynamically compute a margin
            // for the various button styles that we support, if we encounter a
            // a margin that is an instanceof a UIResource.
            "ComboBoxButton.margin", new InsetsUIResource(0,0,0,0),

            // The visual margin is used to allow each component having room
            // for a cast shadow and a focus ring, and still supporting a
            // consistent visual arrangement of all components aligned to their
            // visualy perceived lines.
            "Component.visualMargin", visualMargin,

            // for JFileChooser
                "Browser.expandedIcon", browserIcons[0],
                "Browser.expandingIcon", browserIcons[1],
                "Browser.selectedExpandedIcon", browserIcons[2],
                "Browser.selectedExpandingIcon", browserIcons[3],
                "Browser.selectionBackground", new ColorUIResource(56,117,215),
                "Browser.selectionForeground", new ColorUIResource(255,255,255),
                "Browser.inactiveSelectionBackground", new ColorUIResource(208,208,208),
                "Browser.inactiveSelectionForeground", new ColorUIResource(0,0,0),

                "FileChooser.homeFolderIcon", LookAndFeel.makeIcon(getClass(), commonDir+"FileChooser.homeFolderIcon.png"),
                "FileView.computerIcon", LookAndFeel.makeIcon(getClass(), commonDir+"FileView.computerIcon.png"),
                "FileChooser.previewLabelForeground", new ColorUIResource(0x000000),
                "FileChooser.previewValueForeground", new ColorUIResource(0x000000),
                "FileChooser.previewLabelFont", smallSystemFont,
                "FileChooser.previewValueFont", smallSystemFont,
                "FileChooser.splitPaneDividerSize", new Integer(6),
                "FileChooser.previewLabelInsets",new InsetsUIResource(0,0,0,4),
                "FileChooser.cellTipOrigin", new Point(18, 1),

        };

        table.putDefaults(objects);
    }

    protected static Object makeButtonStateIcon(String location, int states)
    {
         return new UIDefaults.ProxyLazyValue(
             "ch.randelshofer.quaqua.QuaquaIconFactory",
             "createButtonStateIcon",
             new Object[] { location, states }
         );
     }

    /**
     * Returns our tweaked defaults.
     *
     * @return defaults.
     */
    public UIDefaults getDefaults()
    {
        return lafDefaults;
    }

    // New stuff for JFileChooser

    protected void initResourceBundle(UIDefaults table)
    {
        table.addResourceBundle("ch.randelshofer.quaqua.Labels");
    }

    protected URL getResource(String location) {
        URL url = getClass().getResource(location);
        if (url == null) {
            throw new InternalError("image resource missing: "+location);
        }
        return url;
    }

    protected Image createImage(String location) {
        return Toolkit.getDefaultToolkit().createImage(getResource(location));
    }
    protected Icon[] makeIcons(String location, int count, boolean horizontal) {
        Icon[] icons = new Icon[count];

        BufferedImage[] images = Images.split(
        createImage(location),
        count, horizontal
        );

        for (int i=0; i < count; i++) {
            icons[i] = new IconUIResource(new ImageIcon(images[i]));
        }
        return icons;
    }

    /**
     * The small system font (Lucida Grande Regular 11 pt) is used for
     * informative text in alerts. It is also the default font for column
     * headings in lists, for help tags, and for small controls. You can also
     * use it to provide additional information about settings in various
     * windows, such as the QuickTime pane in System Preferences.
     */
    protected static final FontUIResource SMALL_SYSTEM_FONT =
    new FontUIResource("Lucida Grande", Font.PLAIN, 11);
}
