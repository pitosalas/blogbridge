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
// $Id: LazyArticle.java,v 1.4 2006/11/08 13:26:56 spyromus Exp $
//

package com.salas.bb.domain;

/**
 * Lazy article loads its text from the database when required and
 * releases it when there's not enough memory. It happens only when
 * an article is given text provider object, otherwise it acts like
 * standard article.
 */
public class LazyArticle extends StandardArticle
{
    /** Provider of the article text. */
    private IArticleTextProvider provider;

    /**
     * Creates lazy article. By default it acts as normal article, but
     * when it's given a provider, it becomes dynamic and resets the text.
     *
     * @param aText text.
     */
    public LazyArticle(String aText)
    {
        super(aText);
    }

    /**
     * Sets the provider and resets the text so it becomes dynamic.
     *
     * @param provider provider to use for text fetching.
     */
    public void setProvider(IArticleTextProvider provider)
    {
        if (provider != null)
        {
            text = null;
            this.provider = provider;
        }
    }

    /**
     * Returns original article text.
     *
     * @return article text either from the property or directly from database.
     */
    public String getText()
    {
        IArticleTextProvider prov = getProvider();
        return prov != null ? prov.getArticleText(getID()) : super.getText();
    }


    /**
     * Returns plain version of article text.
     *
     * @return plain version of text.
     */
    public synchronized String getPlainText()
    {
        String plain = null;

        IArticleTextProvider prov = getProvider();
        if (prov != null) plain = prov.getArticlePlainText(getID());

        return plain != null ? plain : super.getPlainText();
    }

    /**
     * Returns text provider.
     *
     * @return provider.
     */
    private IArticleTextProvider getProvider()
    {
        return provider != null && getID() != -1L ? provider : null;
    }
}
