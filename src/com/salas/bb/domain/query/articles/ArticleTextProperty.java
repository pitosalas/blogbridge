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
// $Id: ArticleTextProperty.java,v 1.9 2007/04/03 13:10:25 spyromus Exp $
//

package com.salas.bb.domain.query.articles;

import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.query.AbstractProperty;
import com.salas.bb.domain.query.IComparisonOperation;
import com.salas.bb.domain.query.PropertyType;
import com.salas.bb.domain.query.general.StringContainsCO;
import com.salas.bb.domain.query.general.StringNotContainsCO;
import com.salas.bb.utils.i18n.Strings;

/**
 * Property, representing the text of an article.
 */
public class ArticleTextProperty extends AbstractProperty
{
    /** Instance of this property. */
    public static final ArticleTextProperty INSTANCE = new ArticleTextProperty();

    /**
     * Creates property.
     */
    protected ArticleTextProperty()
    {
        this(Strings.message("query.property.article.text"), "article-text");
    }

    /**
     * Constructor for sub-classes.
     *
     * @param aName         the name.
     * @param aDescriptor   the descriptor.
     */
    protected ArticleTextProperty(String aName, String aDescriptor)
    {
        super(aName, aDescriptor, PropertyType.STRING, "",
            new IComparisonOperation[] { StringContainsCO.INSTANCE, StringNotContainsCO.INSTANCE });
    }

    /**
     * Compares the corresponding property of the target object to the value using specific
     * comparison operation.
     *
     * @param target    target object.
     * @param operation comparison operation, supported by this property.
     * @param value     value to compare against.
     *
     * @return TRUE if the object matches the criteria.
     *
     * @throws NullPointerException     if target, operation or value are NULL's.
     * @throws ClassCastException       if target is not supported.
     * @throws IllegalArgumentException if operation is not supported.
     */
    public boolean match(Object target, IComparisonOperation operation, String value)
    {
        if (target == null) throw new NullPointerException("Article can't be null");
        if (value == null) throw new NullPointerException("Value can't be null");

        IArticle article = (IArticle)target;
        String text = article.getPlainText();
        return operation.match(text, value);
    }
}
