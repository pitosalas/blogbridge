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
// $Id: IStylesheet.java,v 1.2 2006/10/18 08:00:05 spyromus Exp $
//

package com.salas.bb.views.stylesheets;

import com.salas.bb.views.stylesheets.domain.IRule;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Stylesheet interface.
 */
public interface IStylesheet
{
    /**
     * Returns the rule for the element with the given set of classes.
     *
     * @param el        element.
     * @param classes   classes.
     *
     * @return the rule.
     */
    IRule getRule(String el, String[] classes);

    /**
     * Returns the font for the element with the given set of classes.
     *
     * @param el        element.
     * @param classes   classes.
     *
     * @return the font object or <code>NULL</code> if the default should be used.
     */
    Font getFont(String el, String[] classes);

    /**
     * Returns the color for the element with the given set of classes.
     *
     * @param el        element.
     * @param classes   classes.
     *
     * @return the color or <code>NULL</code> if the default should be used.
     */
    Color getColor(String el, String[] classes);

    /**
     * Returns the icon for the element with the given set of classes.
     *
     * @param el        element.
     * @param classes   classes.
     *
     * @return the icon or <code>NULL</code> if the default should be used.
     *
     * @throws IOException if loading of icon failed.
     */
    Icon getIcon(String el, String[] classes) throws IOException;
}
