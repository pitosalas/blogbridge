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
// $Id: ShowArticleTagsAction.java,v 1.10 2007/05/30 10:09:12 spyromus Exp $
//

package com.salas.bb.tags;

import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.ITaggable;
import com.salas.bb.tags.net.ITagsStorage;
import com.salas.bb.views.mainframe.MainFrame;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows tags of selected article.
 */
public class ShowArticleTagsAction extends AbstractShowTagsAction
{
    private static ShowArticleTagsAction instance;

    /**
     * Hidden singleton constructor.
     */
    private ShowArticleTagsAction()
    {
    }

    /**
     * Returns instance of the action.
     *
     * @return instance.
     */
    public static synchronized ShowArticleTagsAction getInstance()
    {
        if (instance == null) instance = new ShowArticleTagsAction();
        return instance;
    }

    /**
     * Returns currently selected taggable objects.
     *
     * @return taggable objects or <code>NULL</code> to skip further processing.
     */
    protected ITaggable[] getSelectedTaggables()
    {
        IArticle[] articles = GlobalModel.SINGLETON.getSelectedArticles();
        if (articles == null || articles.length == 0) return null;
        
        List<ITaggable> ta = new ArrayList<ITaggable>();
        for (IArticle article : articles) if (article instanceof ITaggable) ta.add((ITaggable)article);

        return ta.toArray(new ITaggable[ta.size()]);
    }

    /**
     * Returns initialized dialog window.
     *
     * @param aMainFrame  main frame.
     * @param aNetHandler tags networker object.
     *
     * @return dialog.
     */
    protected AbstractTagsDialog getTagsDialog(MainFrame aMainFrame,
        ITagsStorage aNetHandler)
    {
        return new ArticleTagsDialog(aMainFrame, aNetHandler);
    }
}
