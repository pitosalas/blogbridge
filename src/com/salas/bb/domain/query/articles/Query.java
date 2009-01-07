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
// $Id: Query.java,v 1.20 2008/02/28 09:58:33 spyromus Exp $
//

package com.salas.bb.domain.query.articles;

import com.salas.bb.domain.query.BasicQuery;
import com.salas.bb.domain.query.IComparisonOperation;
import com.salas.bb.domain.query.ICriteria;
import com.salas.bb.domain.query.IProperty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Query for <code>IArticle</code> objects.
 */
public class Query extends BasicQuery
{
    private static final Pattern PAT_TOP_QUERY =
        Pattern.compile("^(true|false);(.+;)?$");
    private static final Pattern PAT_CRITERIA =
        Pattern.compile("(([^:]+):([^:]+):(([^\\\\;]|\\\\(;|\\\\))*));");

    /**
     * Creates query.
     */
    public Query()
    {
        super(new IProperty[]
            {
                EntireArticleTextProperty.INSTANCE,
                ArticleCategoryProperty.INSTANCE,
                ArticleDateProperty.INSTANCE,
                ArticleFlagProperty.INSTANCE,
                ArticleStatusProperty.INSTANCE,
                ArticleSentimentsProperty.INSTANCE,
                ArticleTagsProperty.INSTANCE,
                ArticleTextProperty.INSTANCE,
                ArticleTitleProperty.INSTANCE,
                FeedTagsProperty.INSTANCE,
                FeedTitleProperty.INSTANCE,
                FeedStarzProperty.INSTANCE,
                GuideTitleProperty.INSTANCE
            });
    }

    /**
     * Creates complete clone which includes And/Or flag and all criteria rows.
     *
     * @return complete clone.
     */
    public Query getClone()
    {
        Query cloneQuery = new Query();

        cloneQuery.setAndQuery(isAndQuery());
        int criteriaCount = getCriteriaCount();
        for (int i = 0; i < criteriaCount; i++)
        {
            ICriteria criteria = getCriteriaAt(i);

            ICriteria cloneCriteria = cloneQuery.addCriteria();
            cloneCriteria.setProperty(criteria.getProperty());
            cloneCriteria.setComparisonOperation(criteria.getComparisonOperation());
            cloneCriteria.setValue(criteria.getValue());
        }

        return cloneQuery;
    }

    // ---------------------------------------------------------------------------------------------
    // Serialization / Deserialization
    // ---------------------------------------------------------------------------------------------

    public String serializeToString()
    {
        StringBuffer buf = new StringBuffer();

        buf.append(isAndQuery()).append(";");

        int criteriaCount = getCriteriaCount();
        for (int i = 0; i < criteriaCount; i++)
        {
            ICriteria criteria = getCriteriaAt(i);
            String serializedCriteria = serializeToString(criteria);
            buf.append(serializedCriteria).append(";");
        }

        return buf.toString();
    }

    private String serializeToString(ICriteria aCriteria)
    {
        IProperty property = aCriteria.getProperty();
        IComparisonOperation operation = aCriteria.getComparisonOperation();
        String value = aCriteria.getValue();

        StringBuffer buf = new StringBuffer();

        buf.append(property.getDescriptor()).append(':');
        buf.append(operation.getDescriptor()).append(':');
        buf.append(escapeValue(value));

        return buf.toString();
    }

    public static Query deserializeFromString(String serializedVersion)
    {
        Matcher matcher = PAT_TOP_QUERY.matcher(serializedVersion);

        Query query = null;
        if (matcher.find())
        {
            String andQueryString = matcher.group(1);
            String criteriaItems = matcher.group(2);

            query = new Query();
            query.setAndQuery(Boolean.valueOf(andQueryString).booleanValue());

            if (criteriaItems != null)
            {
                Matcher criteriaMatcher = PAT_CRITERIA.matcher(criteriaItems);
                while (criteriaMatcher.find())
                {
                    String property = criteriaMatcher.group(2);
                    String operation = criteriaMatcher.group(3);
                    String value = criteriaMatcher.group(4);

                    addCriteria(query, property, operation, value);
                }
            }
        }

        return query;
    }

    static void addCriteria(Query aQuery, String propertyDescriptor,
        String operationDescriptor, String value)
    {
        IProperty property = aQuery.getPropertyByDescriptor(propertyDescriptor);
        if (property != null)
        {
            IComparisonOperation operation =
                property.getComparsonOperationByDescriptor(operationDescriptor);

            if (operation != null)
            {
                ICriteria criteria = aQuery.addCriteria();
                criteria.setProperty(property);
                criteria.setComparisonOperation(operation);
                criteria.setValue(unescapeValue(value));
            }
        }
    }

    public static String escapeValue(String aValue)
    {
        aValue = aValue.replaceAll("\\\\", "\\\\\\\\");
        aValue = aValue.replaceAll("\\;", "\\\\;");

        return aValue;
    }

    public static String unescapeValue(String aValue)
    {
        aValue = aValue.replaceAll("\\\\\\\\", "\\\\");
        aValue = aValue.replaceAll("\\\\;", "\\;");

        return aValue;
    }
}
