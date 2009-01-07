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
// $Id: PostData.java,v 1.1 2007/08/22 12:24:40 spyromus Exp $
//

package com.salas.bb.remixfeeds.type;

import com.salas.bb.domain.IArticle;

/**
 * Post data block.
 */
public class PostData
{
    public final String title;
    public final String text;
    public final IArticle sourceArticle;

    /**
     * Creates a block.
     *
     * @param title         title.
     * @param text          text.
     * @param sourceArticle article.
     */
    PostData(String title, String text, IArticle sourceArticle)
    {
        this.title = title;
        this.text = text;
        this.sourceArticle = sourceArticle;
    }
}
