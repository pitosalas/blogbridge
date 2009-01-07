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
// $Id: AmazonQueryType.java,v 1.3 2007/07/06 14:47:56 spyromus Exp $
//

package com.salas.bb.domain.querytypes;

import com.jgoodies.uif.util.ResourceUtils;
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
class AmazonQueryType extends AbstractMicrotagQueryType
{
    private static final String PATTERN = "http://www.amazon.com/rss/tag/{0}/{1}?length={2}&tag={3}";

    /**
     * Creates name query type with associated icon and URL pattern.
     *
     * @throws NullPointerException if <code>aName</code> or <code>anURLPattern</code>
     *                              is not specified.
     */
    protected AmazonQueryType()
    {
        super(TYPE_AMAZON_TAGS, FeedType.TEXT,
            Strings.message("queryfeed.type.amazontags.name"),
            ResourceID.ICON_QUERYFEED_AMAZON,
            "",
            Strings.message("queryfeed.type.amazontags.parameter"),
            Strings.message("queryfeed.type.amazontags.description"),
            IFeedDisplayConstants.MODE_FULL);
    }

    @Override
    public String validateEntry(String param, int limit, boolean removeDuplicates, int maxDupWords)
    {
        String msg = basicValidation(limit, removeDuplicates, maxDupWords);
        if (msg == null && StringUtils.isEmpty(param))
        {
            msg = Strings.message("queryfeed.type.amazontags.notags");
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
        String type = (microtags.length > 0) ? microtags[0] : null;

        type = parameterToURLPart(type);
        tags = parameterToURLPart(tags);

        return MessageFormat.format(PATTERN, tags, type.toLowerCase(), limit,
                ResourceUtils.getString("amazon.partner"));
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
        private final JComboBox  cbType = new JComboBox();
        private final JTextField tfTags = new JTextField();

        /**
         * Creates the editor.
         *
         * @param labelColWidth the width in 'dlu' of the label column.
         */
        private QueryEditor(int labelColWidth)
        {
            // Initialize types
            cbType.addItem("Popular");
            cbType.addItem("Recent");
            cbType.addItem("New");

            BBFormBuilder b = new BBFormBuilder(labelColWidth + "dlu, 4dlu, p, p:grow", this);
            b.append("Tag:", tfTags, 2);
            b.append("Type:", cbType);
        }


        /**
         * Sets the value of the parameter. Initializes the internal controls with
         * the values deciphered from the parameter given.
         *
         * @param text the text.
         */
        public void setParameter(String text)
        {
            String[] mtags = getMicroTags(text);
            String type = mtags.length > 0 ? mtags[0] : "popular";
            type = StringUtils.capitalise(type);

            cbType.setSelectedItem(type);
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
            String type = cbType.getSelectedItem().toString();
            res += " [" + type + "]";

            return res;
        }
    }
}