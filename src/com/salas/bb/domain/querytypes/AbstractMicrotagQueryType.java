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
// $Id: AbstractMicrotagQueryType.java,v 1.2 2007/02/09 10:18:26 spyromus Exp $
//

package com.salas.bb.domain.querytypes;

import com.salas.bb.domain.FeedType;
import com.salas.bb.utils.StringUtils;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;

/**
 * Abstract microtags format query type. The basis for types using
 * "[tag]" microformat.
 */
abstract class AbstractMicrotagQueryType extends DefaultQueryType
{
    /**
     * Creates name query type with associated icon and URL pattern. The pattern can use the
     * formatting rules of <code>MessageFormat</code> class and keep in mind that:
     * <ul>
     * <li><b>{0]</b> is a list of parameters, separated with "+" and properly escaped for
     * use in URL.</li>
     * <li><b>{1}</b> maximum number of articles to fetch.</li>
     * </ul>
     *
     * @param aType              type descriptor.
     * @param aFeedType          type of the feed.
     * @param aName              name of this query type.
     * @param anIconKey          key of the icon mentioned in resources.
     * @param anURLPattern       this pattern will be used to create and URL from the list of
     *                           parameters and max articles limit number.
     * @param aParametersName    name of the parameters to be displayed in the GUI.
     * @param aParameterDefault  the default value of parameter.
     * @param aQueryDescription  description of this query type to be displayed in the GUI.
     * @param aPreferredViewMode preferred view mode.
     *
     * @throws NullPointerException if <code>aName</code> or <code>anURLPattern</code>
     *                              is not specified.
     */
    protected AbstractMicrotagQueryType(int aType, FeedType aFeedType, String aName, String anIconKey,
        String anURLPattern, String aParametersName, String aParameterDefault, String aQueryDescription,
        int aPreferredViewMode)
    {
        super(aType, aFeedType, aName, anIconKey, anURLPattern, aParametersName, aParameterDefault, aQueryDescription,
            aPreferredViewMode);
    }

    /**
     * Creates name query type with associated icon and URL pattern. The pattern can use the
     * formatting rules of <code>MessageFormat</code> class and keep in mind that:
     * <ul>
     *  <li><b>{0]</b> is a list of parameters, separated with "+" and properly escaped for
     *      use in URL.</li>
     *  <li><b>{1}</b> maximum number of articles to fetch.</li>
     * </ul>
     *
     * @param aType                 type descriptor.
     * @param aFeedType             type of the feed.
     * @param aName                 name of this query type.
     * @param anIconKey             key of the icon mentioned in resources.
     * @param anURLPattern          this pattern will be used to create and URL from the list of
     *                              parameters and max articles limit number.
     * @param aParametersName       name of the parameters to be displayed in the GUI.
     * @param aQueryDescription     description of this query type to be displayed in the GUI.
     * @param aPreferredViewMode    preferred view mode.
     *
     * @throws NullPointerException if <code>aName</code> or <code>anURLPattern</code>
     *                      is not specified.
     */
    public AbstractMicrotagQueryType(int aType, FeedType aFeedType, String aName,
        String anIconKey, String anURLPattern, String aParametersName,
        String aQueryDescription, int aPreferredViewMode)
    {
        super(aType, aFeedType, aName, anIconKey, anURLPattern, aParametersName, aQueryDescription,
            aPreferredViewMode);
    }

    /**
     * Forms the URL string from the parameter and limit. Both arguments are checked by {@link
     * #convertToURL(String,int)} method.
     *
     * @param param parameter.
     * @param limit maximum articles limit.
     *
     * @return url string.
     */
    protected String formURLString(String param, int limit)
    {
        String url = null;

        if (!StringUtils.isEmpty(param))
        {
            String tags = stripMicroTags(param);
            String[] users = getMicroTags(param);
            url = formURLString(tags, users, limit);
        }

        return url;
    }

    /**
     * Converts clean parameter, microtags and limit into the URL.
     *
     * @param param     parameter.
     * @param microtags microtags list (never NULL).
     * @param limit     limit.
     *
     * @return URL.
     */
    abstract String formURLString(String param, String[] microtags, int limit);

    /**
     * Removes all microtags from the parameter and collapses spaces.
     *
     * @param param parameter.
     *
     * @return clean string.
     */
    protected static String stripMicroTags(String param)
    {
        Pattern pat = Pattern.compile("\\[\\s*([^\\s\\]]+)\\s*\\]");
        Matcher mat = pat.matcher(param);

        return mat.replaceAll("").trim().replaceAll("\\s+", " ");
    }

    /**
     * Fetches microtags from the parameter string.
     *
     * @param param parameter.
     *
     * @return microtags.
     */
    protected static String[] getMicroTags(String param)
    {
        Pattern pat = Pattern.compile("\\[\\s*([^\\s\\]]+)\\s*\\]");
        Matcher mat = pat.matcher(param);

        String[] tags;
        if (mat.find())
        {
            ArrayList<String> list = new ArrayList<String>();

            do {
                list.add(mat.group(1));
            } while (mat.find());

            tags = list.toArray(new String[list.size()]);
        } else tags = new String[0];

        return tags;
    }
}
