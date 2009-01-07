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
// $Id: GuideType.java,v 1.2 2008/03/31 15:29:12 spyromus Exp $
//

package com.salas.bb.remixfeeds.type;

import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;
import com.salas.bb.remixfeeds.templates.Template;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Post to blog from a guide.
 */
public class GuideType extends MultipleArticlesType
{
    @Override
    public boolean isAvailable()
    {
        return super.isAvailable() && GlobalModel.SINGLETON.getSelectedGuide() != null;
    }

    /**
     * Returns the post data.
     *
     * @param template template.
     *
     * @return data.
     */
    public PostData getPostData(Template template)
    {
        String title = null;
        String text = null;

        IGuide[] guides = GlobalController.SINGLETON.getSelectedGuides();
        if (guides.length > 0)
        {
            title = "";
            text = template.render(chooseArticles(flatten(guides)));
        }

        return new PostData(title, text, null);
    }

    /**
     * Flattens guides to articles.
     *
     * @param guides guides.
     *
     * @return articles.
     */
    private Set<IArticle> flatten(IGuide[] guides)
    {
        Set<IArticle> articles = new LinkedHashSet<IArticle>();

        for (IGuide guide : guides)
        {
            IFeed[] feeds = guide.getFeeds();
            articles.addAll(flatten(feeds));
        }

        return articles;
    }
}
