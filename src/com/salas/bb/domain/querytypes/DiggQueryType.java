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
 * Query type which is capable of building URL using URL template.
 */
class DiggQueryType extends AbstractMicrotagQueryType
{
    private static final String PAT_TAG = "http://digg.com/rss_search?search={0}&area=all&type=both&age=all";
    private static final String PAT_ALL = "http://digg.com/rss/index.xml";
    private static final String TAG_ALL = "all";

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
    protected DiggQueryType()
    {
        super(TYPE_DIGG, FeedType.TEXT,
            Strings.message("queryfeed.type.digg.name"),
            ResourceID.ICON_QUERYFEED_DIGG,
            "",
            Strings.message("queryfeed.type.digg.parameter"),
            "[all]",
            Strings.message("queryfeed.type.digg.description"),
            IFeedDisplayConstants.MODE_FULL);
    }

    /**
     * Converts clean parameter, microtags and limit into the URL.
     *
     * @param keywords  parameter.
     * @param microtags microtags list (never NULL).
     * @param limit     limit.
     *
     * @return URL.
     */
    protected String formURLString(String keywords, String[] microtags, int limit)
    {
        String pattern = null;
        Object[] params = null;

        String tag = (microtags.length > 0) ? microtags[0] : null;
        if (tag != null && TAG_ALL.equalsIgnoreCase(tag))
        {
            pattern = PAT_ALL;
            params = new Object[0];
        } else
        {
            keywords = parameterToURLPart(keywords);
            if (!StringUtils.isEmpty(keywords))
            {
                pattern = PAT_TAG;
                params = new Object[] { keywords };
            }
        }

        return pattern == null ? null : MessageFormat.format(pattern, params);
    }
}
