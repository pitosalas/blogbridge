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
// $Id: DiggQueryType.java,v 1.2 2006/12/11 13:54:37 spyromus Exp $
//

package com.salas.bb.domain.querytypes;

import com.salas.bb.domain.FeedType;
import com.salas.bb.utils.ResourceID;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.views.feeds.IFeedDisplayConstants;

import java.text.MessageFormat;

/**
 * Twitter query type.
 */
class TwitterQueryType extends DefaultQueryType
{
    private static final String PATTERN = "http://search.twitter.com/search.atom?q={0}&rpp={1}";

    /**
     * Creates name query type with associated icon and URL pattern. The pattern can use the
     * formatting rules of <code>MessageFormat</code> class and keep in mind that:
     * <ul>
     * <li><b>{0]</b> is a list of parameters, separated with "+" and properly escaped for
     * use in URL.</li>
     * <li><b>{1}</b> maximum number of articles to fetch.</li>
     * </ul>
     *
     * @throws NullPointerException if <code>aName</code> or <code>anURLPattern</code>
     *                              is not specified.
     */
    protected TwitterQueryType()
    {
        super(TYPE_TWITTER, FeedType.TWITTER,
            Strings.message("queryfeed.type.twitter.name"),
            ResourceID.ICON_QUERY_FEED_TWITTER,
            "",
            Strings.message("queryfeed.type.twitter.parameter"),
            "",
            Strings.message("queryfeed.type.twitter.description"),
            IFeedDisplayConstants.MODE_FULL);
    }

    @Override
    protected String formURLString(String param, int limit)
    {
        param = parameterToURLPart(param);
        if (StringUtils.isEmpty(param)) return null;

        return MessageFormat.format(PATTERN, param, limit);
    }
}