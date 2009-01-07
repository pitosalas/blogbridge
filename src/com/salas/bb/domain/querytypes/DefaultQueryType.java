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
// $Id: DefaultQueryType.java,v 1.5 2007/03/23 13:59:11 spyromus Exp $
//

package com.salas.bb.domain.querytypes;

import com.salas.bb.domain.FeedType;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;

import javax.swing.*;
import java.text.MessageFormat;

/**
 * Query type which is capable of building URL using URL template.
 */
public class DefaultQueryType extends QueryType
{
    private final String        name;
    private final String        iconKey;
    private final MessageFormat urlFormat;
    private final String        parametersName;
    private final String        queryDescription;
    private final String        parameterDefault;
    private final int           preferredViewMode;

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
    protected DefaultQueryType(int aType, FeedType aFeedType, String aName, String anIconKey,
        String anURLPattern, String aParametersName, String aQueryDescription,
        int aPreferredViewMode)
    {
        this(aType, aFeedType, aName, anIconKey, anURLPattern, aParametersName, "", aQueryDescription, aPreferredViewMode);
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
     * @param aParameterDefault     the default value of parameter.
     * @param aQueryDescription     description of this query type to be displayed in the GUI.
     * @param aPreferredViewMode    preferred view mode.
     *
     * @throws NullPointerException if <code>aName</code> or <code>anURLPattern</code>
     *                      is not specified.
     */
    protected DefaultQueryType(int aType, FeedType aFeedType, String aName, String anIconKey,
        String anURLPattern, String aParametersName, String aParameterDefault, String aQueryDescription,
        int aPreferredViewMode)
    {
        super(aType, aFeedType);

        if (aName == null) throw new NullPointerException(Strings.error("unspecified.name"));
        if (anURLPattern == null) throw new NullPointerException(Strings.error("unspecified.pattern"));

        name = aName;
        iconKey = anIconKey;
        urlFormat = new MessageFormat(anURLPattern);
        parametersName = aParametersName;
        parameterDefault = aParameterDefault;
        queryDescription = aQueryDescription;
        preferredViewMode = aPreferredViewMode;
    }

    /**
     * Returns type name.
     *
     * @return type name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns associated icon.
     *
     * @return icon key or NULL if no icon associated.
     */
    public String getIconKey()
    {
        return iconKey;
    }

    /**
     * Forms the URL string from the parameter and limit. Both arguments are checked by {@link
     * #convertToURL(String, int)} method.
     *
     * @param param parameter.
     * @param limit maximum articles limit.
     *
     * @return url string.
     */
    protected String formURLString(String param, int limit)
    {
        return urlFormat == null ? null
            : urlFormat.format(new String[] {
                parameterToURLPart(param), Integer.toString(limit) });
    }

    /**
     * Converts parameter into part of URL for a given type. By default, it returns simple
     * escaped version of parameter. For example, "a b" becomes "a+b".
     *
     * @param parameter parameter.
     *
     * @return URL-ready part.
     */
    protected static String parameterToURLPart(String parameter)
    {
        return parameter == null ? null : StringUtils.encodeForURL(parameter.trim());
    }

    /**
     * Validates the entry and returns the error message or NULL in case of success.
     *
     * @param param parameters.
     * @param limit max articles.
     * @param maxDupWords number of first duplicate words to use as a filter.
     * @param removeDuplicates <code>TRUE</code> to remove duplicate articles.
     * @return error message or NULL.
     */
    public String validateEntry(String param, int limit, boolean removeDuplicates, int maxDupWords)
    {
        String message = basicValidation(limit, removeDuplicates, maxDupWords);
        if (message == null && StringUtils.isEmpty(param))
        {
            message = MessageFormat.format(Strings.message("queryfeed.type.validation.unspecified.parameter"),
                getParamterName());
        }

        return message;
    }

    /**
     * Basic validation of main parameters.
     *
     * @param limit             limit.
     * @param removeDuplicates  remove-duplicates flag.
     * @param maxDuplicates     maximum number of duplicates.
     *
     * @return error message or <code>NULL</code>.
     */
    protected String basicValidation(int limit, boolean removeDuplicates, int maxDuplicates)
    {
        String message = null;

        if (limit < 1)
        {
            message = Strings.message("queryfeed.type.validation.negative.limit");
        } else if (removeDuplicates && maxDuplicates < 1)
        {
            message = Strings.message("smartfeed.type.validation.low.maxdupwords");
        }

        return message;
    }

    /**
     * Returns the name of parameter. It can be "Keywords", "Tag", "Blog name" and so on.
     *
     * @return name of parameter.
     */
    public String getParamterName()
    {
        return parametersName;
    }

    /**
     * Returns the default value of the parameter.
     *
     * @return default.
     */
    public String getParameterDefault()
    {
        return parameterDefault;
    }

    /**
     * Returns the description of this query type which will be displayed to the user.
     *
     * @return description of this query type.
     */
    public String getQueryDescription()
    {
        return queryDescription;
    }

    /**
     * Returns preferred view mode.
     *
     * @return view mode.
     */
    public int getPreferredViewMode()
    {
        return preferredViewMode;
    }

    /**
     * Creates and returns the panel with the controls to edit the properties.
     *
     * @param labelColWidth the width in 'dlu' of the label column.
     *
     * @return panel.
     */
    public QueryEditorPanel getEditorPanel(int labelColWidth)
    {
        return new QueryEditor(getParamterName(), labelColWidth);
    }

    /**
     * Simple query editor with the single labeled field.
     */
    private static class QueryEditor extends QueryEditorPanel
    {
        private final JTextField tfParameter = new JTextField();

        /**
         * Creates the editor with the labeled field.
         *
         * @param paramLabel the label of the field.
         * @param labelColWidth the width in 'dlu' of the label column.
         */
        private QueryEditor(String paramLabel, int labelColWidth)
        {
            BBFormBuilder b = new BBFormBuilder(labelColWidth + "dlu, 4dlu, p:grow", this);
            b.append(paramLabel + ":", tfParameter);
        }

        /**
         * Sets the value of the parameter. Initializes the internal controls with
         * the values deciphered from the parameter given.
         *
         * @param text the text.
         */
        public void setParameter(String text)
        {
            tfParameter.setText(text);
        }

        /**
         * Returns the value of the parameter.
         *
         * @return the text.
         */
        public String getParameter()
        {
            return tfParameter.getText();
        }
    }
}
