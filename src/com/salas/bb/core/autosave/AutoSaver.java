// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2007 by R. Pito Salas
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
// $Id: AutoSaver.java,v 1.1 2007/05/02 10:27:06 spyromus Exp $
//

package com.salas.bb.core.autosave;

import com.salas.bb.core.GlobalController;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.SearchFeed;
import com.salas.bb.domain.utils.DomainAdapter;

/**
 * Auto-saver monitors new articles and invokes saving routines.
 */
public class AutoSaver extends DomainAdapter
{
    private ArticleAutoSaver articleAutoSaver;
    private EnclosureAutoSaver enclosureAutoSaver;

    @Override
    public void articleAdded(IFeed feed, IArticle article)
    {
        if (article.isNew() && GlobalController.SINGLETON.getFeatureManager().isAutoSaving())
        {
            try
            {
                getArticleAutoSaver().onNewArticle(article, feed);
            } catch (Throwable e)
            {
                // Fall through
            }

            try
            {
                getEnclosureAutoSaver().onNewArticle(article, feed);
            } catch (Throwable e)
            {
                // Fall through
            }
        }
    }

    @Override
    public void articleAddedToSearchFeed(SearchFeed feed, IArticle article)
    {
        articleAdded(feed, article);
    }

    // ------------------------------------------------------------------------
    // Lazy instantiation
    // ------------------------------------------------------------------------

    /**
     * Returns the AAS.
     *
     * @return AAS.
     */
    private synchronized ArticleAutoSaver getArticleAutoSaver()
    {
        if (articleAutoSaver == null) articleAutoSaver = new ArticleAutoSaver();
        return articleAutoSaver;
    }

    /**
     * Returns the EAS.
     *
     * @return EAS.
     */
    private synchronized EnclosureAutoSaver getEnclosureAutoSaver()
    {
        if (enclosureAutoSaver == null) enclosureAutoSaver = new EnclosureAutoSaver();
        return enclosureAutoSaver;
    }
}
