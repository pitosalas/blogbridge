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
// $Id: FeedStarzProperty.java,v 1.10 2007/02/20 15:15:55 spyromus Exp $
//

package com.salas.bb.domain.query.articles;

import com.salas.bb.domain.query.AbstractProperty;
import com.salas.bb.domain.query.PropertyType;
import com.salas.bb.domain.query.IComparisonOperation;
import com.salas.bb.domain.query.general.StringEqualsCO;
import com.salas.bb.domain.query.general.LongLessCO;
import com.salas.bb.domain.query.general.LongGreaterCO;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.DataFeed;
import com.salas.bb.utils.i18n.Strings;

/**
 * Feed starz property of <code>IArticle</code> objects.
 */
public class FeedStarzProperty extends AbstractProperty
{
    /** Instance of this property. */
    public static final FeedStarzProperty INSTANCE = new FeedStarzProperty();

    /** Less than 1 star can't happen to feed. */
    public static final String ERR_LESS_1_STAR = Strings.message("query.property.feed.starz.error.less.1.star");
    /** More than 5 starz can't happen to feed. */
    public static final String ERR_MORE_5_STARZ = Strings.message("query.property.feed.starz.error.more.5.starz");

    /** Creates property. */
    public FeedStarzProperty()
    {
        super(Strings.message("query.property.feed.starz"), "feed-starz", PropertyType.STARZ, "3",
            new IComparisonOperation[]
            {
                StringEqualsCO.INSTANCE, LongLessCO.INSTANCE, LongGreaterCO.INSTANCE
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
        IArticle article = (IArticle)target;
        IFeed feed = article.getFeed();

        int rating = -1;

        if (feed != null || !(feed instanceof DataFeed))
        {
            DataFeed dataFeed = (DataFeed)feed;
            rating = dataFeed.getRating() + 1;
        }

        return operation.match(Integer.toString(rating), value);
    }

    /**
     * Validates the value and tells if the value has incorrect format.
     *
     * @param operation operation which is going to happen.
     * @param value     value.
     *
     * @return error message or <code>NULL</code> in successful case.
     */
    public String validateValue(IComparisonOperation operation, String value)
    {
        String errorMessage = super.validateValue(operation, value);

        if (errorMessage == null)
        {
            try
            {
                int starz = Integer.parseInt(value.trim());

                if (operation.equals(LongLessCO.INSTANCE) && starz == 1)
                {
                    errorMessage = ERR_LESS_1_STAR;
                } else if (operation.equals(LongGreaterCO.INSTANCE) && starz == 5)
                {
                    errorMessage = ERR_MORE_5_STARZ;
                }
            } catch (NumberFormatException e)
            {
                errorMessage = Strings.message("query.property.feed.starz.error.wrong.integer.property.format");
            }
        };

        return errorMessage;
    }
}
