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
// $Id: LAFProxy.java,v 1.1 2006/02/01 15:08:40 spyromus Exp $
//

package com.salas.bb.utils.uif.laf;

import javax.swing.plaf.basic.BasicLookAndFeel;
import javax.swing.*;
import java.awt.*;

/**
 * LAF proxy to use for tweaking some parameters of other LAF's.
 */
public class LAFProxy extends BasicLookAndFeel
{
    protected LookAndFeel laf;

    /**
     * Creates a proxy.
     */
    public LAFProxy()
    {
    }

    /**
     * Sets the underlying laf.
     *
     * @param aLaf laf.
     */
    public void setLookAndFeel(LookAndFeel aLaf)
    {
        laf = aLaf;
    }

    /**
     * If the underlying platform has a "native" look and feel, and this is an implementation of it,
     * return true.  For example a CDE/Motif look and implementation would return true when the
     * underlying platform was Solaris.
     */
    public boolean isNativeLookAndFeel()
    {
        return laf.isNativeLookAndFeel();
    }

    /**
     * Return true if the underlying platform supports and or permits this look and feel.  This
     * method returns false if the look and feel depends on special resources or legal agreements
     * that aren't defined for the current platform.
     *
     * @see javax.swing.UIManager#setLookAndFeel
     */
    public boolean isSupportedLookAndFeel()
    {
        return laf.isSupportedLookAndFeel();
    }

    /**
     * Return a one line description of this look and feel implementation, e.g. "The CDE/Motif Look
     * and Feel".   This string is intended for the user, e.g. in the title of a window or in a
     * ToolTip message.
     */
    public String getDescription()
    {
        return laf.getDescription();
    }

    /**
     * Return a string that identifies this look and feel.  This string will be used by
     * applications/services that want to recognize well known look and feel implementations.
     * Presently the well known names are "Motif", "Windows", "Mac", "Metal".  Note that a
     * LookAndFeel derived from a well known superclass that doesn't make any fundamental changes to
     * the look or feel shouldn't override this method.
     */
    public String getID()
    {
        return laf.getID();
    }

    /**
     * Return a short string that identifies this look and feel, e.g. "CDE/Motif".  This string
     * should be appropriate for a menu item. Distinct look and feels should have different names,
     * e.g. a subclass of MotifLookAndFeel that changes the way a few components are rendered should
     * be called "CDE/Motif My Way"; something that would be useful to a user trying to select a L&F
     * from a list of names.
     */
    public String getName()
    {
        return laf.getName();
    }

    /**
     * Invoked when the user attempts an invalid operation, such as pasting into an uneditable
     * <code>JTextField</code> that has focus. The default implementation beeps. Subclasses that
     * wish different behavior should override this and provide the additional feedback.
     *
     * @param component Component the error occured in, may be null indicating the error condition
     *                  is not directly associated with a <code>Component</code>.
     */
    public void provideErrorFeedback(Component component)
    {
        Toolkit.getDefaultToolkit().beep();
    }

    /**
     * Returns true if the <code>LookAndFeel</code> returned <code>RootPaneUI</code> instances
     * support providing Window decorations in a <code>JRootPane</code>. <p> The default
     * implementation returns false, subclasses that support Window decorations should override this
     * and return true.
     *
     * @return True if the RootPaneUI instances created support client side decorations
     *
     * @see javax.swing.JDialog#setDefaultLookAndFeelDecorated
     * @see javax.swing.JFrame#setDefaultLookAndFeelDecorated
     * @see javax.swing.JRootPane#setWindowDecorationStyle
     * @since 1.4
     */
    public boolean getSupportsWindowDecorations()
    {
        return false;
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
        laf.initialize();
    }

    /**
     * UIManager.setLookAndFeel calls this method just before we're replaced by a new default look
     * and feel.   Subclasses may choose to free up some resources here.
     *
     * @see #initialize
     */
    public void uninitialize()
    {
        laf.uninitialize();
    }

    /**
     * Returns a string that displays and identifies this object's properties.
     *
     * @return a String representation of this object
     */
    public String toString()
    {
        return laf.toString();
    }

    // -----------------------------

    public UIDefaults getDefaults()
    {
        return laf.getDefaults();
    }
}
