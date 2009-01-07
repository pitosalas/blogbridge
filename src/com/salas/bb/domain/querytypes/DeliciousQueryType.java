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
// $Id: DeliciousQueryType.java,v 1.3 2007/03/06 15:47:28 spyromus Exp $
//

package com.salas.bb.domain.querytypes;

import com.salas.bb.domain.FeedType;
import com.salas.bb.utils.ResourceID;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.views.feeds.IFeedDisplayConstants;

import javax.swing.*;
import java.text.MessageFormat;

/**
 * Query type which is capable of building URL using URL template.
 */
class DeliciousQueryType extends AbstractMicrotagQueryType
{
    private static final String PAT_TAG         = "http://del.icio.us/rss/tag/{0}";
    private static final String PAT_USER        = "http://del.icio.us/rss/{0}";
    private static final String PAT_USER_TAG    = "http://del.icio.us/rss/{0}/{1}";

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
    protected DeliciousQueryType()
    {
        super(TYPE_DELICIOUS, FeedType.TEXT,
            Strings.message("queryfeed.type.delicious.name"),
            ResourceID.ICON_QUERYFEED_DELICIOUS,
            "",
            Strings.message("queryfeed.type.delicious.parameter"),
            Strings.message("queryfeed.type.delicious.description"),
            IFeedDisplayConstants.MODE_MINIMAL);
    }

    /**
     * Validates the entry and returns the error message or NULL in case of success.
     *
     * @param param            parameters.
     * @param limit            max articles.
     * @param maxDupWords      number of first duplicate words to use as a filter.
     * @param removeDuplicates <code>TRUE</code> to remove duplicate articles.
     *
     * @return error message or NULL.
     */
    @Override
    public String validateEntry(String param, int limit, boolean removeDuplicates, int maxDupWords)
    {
        String msg = basicValidation(limit, removeDuplicates, maxDupWords);
        if (msg == null && StringUtils.isEmpty(param))
        {
            msg = Strings.message("queryfeed.type.delicious.notags");
        }

        return msg;
    }

    /**
     * Converts clean parameter, microtags and limit into the URL.
     *
     * @param tags      parameter.
     * @param microtags microtags list (never NULL).
     * @param limit     limit.
     *
     * @return URL.
     */
    String formURLString(String tags, String[] microtags, int limit)
    {
        String user = (microtags.length > 0) ? microtags[0] : null;

        user = parameterToURLPart(user);
        tags = parameterToURLPart(tags);

        boolean userEmpty = StringUtils.isEmpty(user);
        boolean tagsEmpty = StringUtils.isEmpty(tags);

        String pattern = null;
        Object[] params = null;

        if (userEmpty)
        {
            if (!tagsEmpty)
            {
                pattern = PAT_TAG;
                params = new Object[] { tags };
            }
        } else
        {
            if (tagsEmpty)
            {
                pattern = PAT_USER;
                params = new Object[] { user };
            } else
            {
                pattern = PAT_USER_TAG;
                params = new Object[] { user, tags };
            }
        }

        return pattern == null ? null : MessageFormat.format(pattern, params);
    }

    /**
     * Creates and returns the panel with the controls to edit the properties.
     *
     * @param labelColWidth the width in 'dlu' of the label column.
     *
     * @return panel.
     */
    @Override
    public QueryEditorPanel getEditorPanel(int labelColWidth)
    {
        return new QueryEditor(labelColWidth);
    }

    /**
     * Microtag query editor.
     */
    private static class QueryEditor extends QueryEditorPanel
    {
        private final JTextField tfUser = new JTextField();
        private final JTextField tfTags = new JTextField();

        /**
         * Creates the editor.
         *
         * @param labelColWidth the width in 'dlu' of the label column.
         */
        private QueryEditor(int labelColWidth)
        {
            BBFormBuilder b = new BBFormBuilder(labelColWidth + "dlu, 4dlu, p:grow", this);
            b.append("Tags:", tfTags);
            b.append("User:", tfUser);
        }


        /**
         * Sets the value of the parameter. Initializes the internal controls with
         * the values deciphered from the parameter given.
         *
         * @param text the text.
         */
        public void setParameter(String text)
        {
            String[] users = getMicroTags(text);
            tfUser.setText(users.length > 0 ? users[0] : "");
            tfTags.setText(stripMicroTags(text));
        }


        /**
         * Returns the value of the parameter.
         *
         * @return the text.
         */
        public String getParameter()
        {
            String res = tfTags.getText();

            String user = tfUser.getText();
            if (StringUtils.isNotEmpty(user)) res += " [" + user + "]";

            return res;
        }
    }
}
