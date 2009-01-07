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
// $Id: AbstractShowTagsAction.java,v 1.9 2007/05/14 15:50:33 spyromus Exp $
//

package com.salas.bb.tags;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.ITaggable;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.tags.net.ITagsStorage;
import com.salas.bb.views.mainframe.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Abstract action, incorporating basic "show tags window" functionality.
 */
abstract class AbstractShowTagsAction extends AbstractAction
{

    /**
     * Invoke when action occurs.
     *
     * @param e action object.
     */
    public void actionPerformed(ActionEvent e)
    {
        final ITaggable[] taggables = getSelectedTaggables();

        if (taggables != null)
        {
            GlobalController controller = GlobalController.SINGLETON;
            MainFrame mainFrame = controller.getMainFrame();
            ITagsStorage tagsNetworker = controller.getTagsStorage();
            final AbstractTagsDialog dialog = getTagsDialog(mainFrame, tagsNetworker);

            GlobalModel model = GlobalModel.SINGLETON;
            final UserPreferences userPreferences = model.getUserPreferences();

            // We call the code in the next EDT event to consume keyboard shortcut ('T')
            // if the code is called with it, or otherwise it will appear as a letter 'T'
            // in the text field of the dialog once it's opened.
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    dialog.open(taggables, userPreferences.isTagsAutoFetch());

                    if (!dialog.hasBeenCanceled())
                    {
                        for (ITaggable taggable : taggables)
                        {
                            TagsRepository.getInstance().addMissingTags(taggable.getUserTags());
                        }
                    }
                }
            });
        }
    }

    /**
     * Returns currently selected taggable objects.
     *
     * @return taggable objects or <code>NULL</code> to skip further processing.
     */
    protected abstract ITaggable[] getSelectedTaggables();

    /**
     * Returns initialized dialog window.
     *
     * @param aMainFrame    main frame.
     * @param aNetHandler   tags networker object.
     *
     * @return dialog.
     */
    protected abstract AbstractTagsDialog getTagsDialog(MainFrame aMainFrame,
        ITagsStorage aNetHandler);
}
