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
// $Id: QueryEditorPanel.java,v 1.1 2007/03/06 11:48:56 spyromus Exp $
//

package com.salas.bb.domain.querytypes;

import javax.swing.*;

/**
 * Query editor panel.
 */
public abstract class QueryEditorPanel extends JPanel
{
    /**
     * Sets the value of the parameter. Initializes the internal controls with
     * the values deciphered from the parameter given.
     *
     * @param text the text.
     */
    public abstract void setParameter(String text);

    /**
     * Returns the value of the parameter.
     *
     * @return the text.
     */
    public abstract String getParameter();
}
