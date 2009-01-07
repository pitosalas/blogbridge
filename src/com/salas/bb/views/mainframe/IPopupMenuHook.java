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
// $Id: IPopupMenuHook.java,v 1.2 2007/05/11 12:54:39 spyromus Exp $
//

package com.salas.bb.views.mainframe;

import javax.swing.*;
import java.util.Collection;

/**
 * Provides a way to add more items to the context popup menus.
 */
public interface IPopupMenuHook
{
    /**
     * Returns the collection of actions that should
     * be appended to the list in the image article context
     * menu.
     *
     * @return the collection of actions or <code>NULL</code>.
     */
    Collection<Action> getImageArticleActions();

    /**
     * Returns the collection of actions that should
     * be appended to the list in the HTML article context
     * menu.
     *
     * @return the collection of actions or <code>NULL</code>.
     */
    Collection<Action> getHTMLArticleActions();

    /**
     * Returns the collection of actions that should
     * be appended to the list in the guides list context
     * menu.
     *
     * @return the collection of actions or <code>NULL</code>.
     */
    Collection<Action> getGuidesListActions();

    /**
     * Returns the collection of actions that should
     * be appended to the list in the feeds list context
     * menu.
     *
     * @return the collection of actions or <code>NULL</code>.
     */
    Collection<Action> getFeedsListActions();

    /**
     * Returns the collection of actions that should
     * be appended to the list in the article hyperlink context
     * menu.
     *  
     * @return the collection of actions or <code>NULL</code>.
     */
    Collection<Action> getArticleHyperlinkActions();

    /**
     * Returns the collection of actions that should
     * be appended to the list in the article group context
     * menu.
     *
     * @return the collection of actions or <code>NULL</code>.
     */
    Collection<Action> getArticleGroupActions();
}
