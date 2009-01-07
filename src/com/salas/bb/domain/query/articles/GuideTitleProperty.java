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
// $Id: GuideTitleProperty.java,v 1.3 2007/02/20 15:15:55 spyromus Exp $
//

package com.salas.bb.domain.query.articles;

import com.salas.bb.domain.query.AbstractProperty;
import com.salas.bb.domain.query.PropertyType;
import com.salas.bb.domain.query.IComparisonOperation;
import com.salas.bb.domain.query.general.StringEqualsCO;
import com.salas.bb.domain.query.general.StringNotEqualsCO;
import com.salas.bb.domain.query.general.StringContainsCO;
import com.salas.bb.domain.query.general.StringNotContainsCO;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.IGuide;
import com.salas.bb.utils.i18n.Strings;

/**
 * Guide title property of <code>IArticle</code> objects.
 */
public class GuideTitleProperty extends AbstractProperty
{
    /** Instance of this property. */
    public static final GuideTitleProperty INSTANCE = new GuideTitleProperty();

    /**
     * Creates property.
     */
    protected GuideTitleProperty()
    {
        super(Strings.message("query.property.guide.title"), "guide-title", PropertyType.STRING, "",
            new IComparisonOperation[]
            {
                StringEqualsCO.INSTANCE, StringNotEqualsCO.INSTANCE,
                StringContainsCO.INSTANCE, StringNotContainsCO.INSTANCE
            });
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
        boolean match = false;
        value = value.toUpperCase();

        IArticle article = (IArticle)target;
        IFeed feed = article.getFeed();

        if (feed != null)
        {
            IGuide[] parentGuides = feed.getParentGuides();
            for (int i = 0; !match && i < parentGuides.length; i++)
            {
                IGuide guide = parentGuides[i];
                String guideTitle = guide.getTitle();
                match = operation.match(guideTitle.toUpperCase(), value);
            }
        } else match = operation.match(null, value);

        return match;
    }
}
