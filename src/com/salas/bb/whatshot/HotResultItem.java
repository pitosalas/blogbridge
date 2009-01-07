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
// $Id: HotResultItem.java,v 1.3 2007/07/11 16:31:33 spyromus Exp $
//

package com.salas.bb.whatshot;

import com.salas.bb.domain.IArticle;
import com.salas.bb.search.ResultItem;

/**
 * What's Hot results item.
 */
class HotResultItem extends ResultItem
{
    private final IArticle article;
    private final String hotlink;

    /**
     * Creates an item for an article and a link.
     *
     * @param article   article.
     * @param hotlink   link.
     */
    public HotResultItem(IArticle article, String hotlink)
    {
        super(article);
        this.hotlink = hotlink;
        this.article = article;
    }

    /**
     * Returns a hotlink.
     *
     * @return hotlink.
     */
    public String getHotlink()
    {
        return hotlink;
    }

    /**
     * Gets associated article.
     *
     * @return article.
     */
    public IArticle getArticle()
    {
        return article;
    }
}
