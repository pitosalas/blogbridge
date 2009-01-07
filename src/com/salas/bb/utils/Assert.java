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
// $Id: Assert.java,v 1.4 2006/05/30 08:25:28 spyromus Exp $
//

package com.salas.bb.utils;

import com.salas.bb.utils.i18n.Strings;

import java.text.MessageFormat;

/**
 * Collection of common assertions.
 */
public final class Assert
{
    /** Hidden utility class constructor. */
    private Assert()
    {
    }

    /**
     * Verifies argument and fires exceptions if argument is empty.
     *
     * @param argName   name of argument for messages.
     * @param argument  argument.
     *
     * @throws NullPointerException if argument is <code>NULL</code>.
     * @throws IllegalArgumentException if argument is empty.
     */
    public static void notEmpty(String argName, String[] argument)
    {
        notNull(argName, argument);
        if (argument.length == 0)
        {
            throw new IllegalArgumentException(MessageFormat.format(
                Strings.error("0.cannot.be.empty"), new Object[] { argName }));
        }
    }

    /**
     * Verifies argument and fires exceptions if argument is empty.
     *
     * @param argName   name of argument for messages.
     * @param argument  argument.
     *
     * @throws NullPointerException if argument is <code>NULL</code>.
     * @throws IllegalArgumentException if argument is empty.
     */
    public static void notEmpty(String argName, String argument)
    {
        notNull(argName, argument);
        if (argument.trim().length() == 0)
        {
            throw new IllegalArgumentException(MessageFormat.format(
                Strings.error("0.cannot.be.empty"), new Object[] { argName }));
        }
    }

    /**
     * Verifies argument and fires exceptions if argument is <code>NULL</code>.
     *
     * @param argName   name of argument for messages.
     * @param argument  argument.
     *
     * @throws NullPointerException if argument is <code>NULL</code>.
     */
    public static void notNull(String argName, Object argument)
    {
        if (argument == null) throw new NullPointerException(MessageFormat.format(
            Strings.error("unspecified.0"), new Object[] { argName }));
    }
}
