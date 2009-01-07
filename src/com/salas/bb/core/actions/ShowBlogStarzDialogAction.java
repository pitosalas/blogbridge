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
// $Id: ShowBlogStarzDialogAction.java,v 1.3 2006/01/08 04:42:25 kyank Exp $
//

package com.salas.bb.core.actions;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.utils.ThreadedAction;
import com.salas.bb.dialogs.BlogStarzDialog;
import com.salas.bb.views.mainframe.MainFrame;

import java.awt.event.ActionEvent;

/**
 * Moves to next Article with matching keywords, if any.
 * <p/>
 * Action is enabled/disabled by <code>ActionsManager</code>.
 */
public final class ShowBlogStarzDialogAction extends ThreadedAction
{
    private static ShowBlogStarzDialogAction instance;

    private ShowBlogStarzDialogAction()
    {
        // Private constructor to ensure singleton.
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized ShowBlogStarzDialogAction getInstance()
    {
        if (instance == null) instance = new ShowBlogStarzDialogAction();
        return instance;
    }

    /**
     * Actual action.
     *
     * @param event original event object.
     */
    protected void doAction(ActionEvent event)
    {
        GlobalController controller = GlobalController.SINGLETON;
        final MainFrame mainFrame = controller.getMainFrame();
        final GlobalModel model = controller.getModel();
        new BlogStarzDialog(mainFrame, model).open();
    }
}
