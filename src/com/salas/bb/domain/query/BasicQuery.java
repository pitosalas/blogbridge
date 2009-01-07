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
// $Id: BasicQuery.java,v 1.11 2008/02/28 09:58:32 spyromus Exp $
//

package com.salas.bb.domain.query;

import com.salas.bb.domain.query.articles.ArticleSentimentsProperty;
import com.salas.bb.utils.i18n.Strings;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Base implementation of a query. It takes care of storing the criteria and type of
 * join. It knows nothing about the actual object types we will be matching.
 */
public abstract class BasicQuery implements IQuery
{
    private ICriteria[] criteriaList;
    private boolean andQuery;

    private final List<IProperty> availableProperties;

    /**
     * Creates basic query.
     *
     * @param availableProperties   properties, which can be used in this type of query.
     */
    public BasicQuery(IProperty[] availableProperties)
    {
        this.availableProperties = Arrays.asList(availableProperties);
        andQuery = false;
        criteriaList = new ICriteria[0];
    }

    // ---------------------------------------------------------------------------------------------
    // Working with criteria
    // ---------------------------------------------------------------------------------------------

    /**
     * Creates new criteria record and adds it to the tail of the criteria list.
     *
     * @return criteria record.
     */
    public synchronized ICriteria addCriteria()
    {
        Criteria criteria = new Criteria();

        ICriteria[] newCriteriaList = copyWithEmptySlot(1);

        int lastIndex = newCriteriaList.length - 1;
        newCriteriaList[lastIndex] = criteria;

        criteriaList = newCriteriaList;

        return criteria;
    }

    /**
     * Copies current criteria list and adds the slot in the tail for new criteria.
     *
     * @param slots number of slots to add to the tail.
     *
     * @return copy of list with a slot.
     */
    private synchronized ICriteria[] copyWithEmptySlot(int slots)
    {
        int currentSize = criteriaList.length;

        ICriteria[] newCriteriaList = new ICriteria[currentSize + slots];
        System.arraycopy(criteriaList, 0, newCriteriaList, 0, currentSize);

        return newCriteriaList;
    }

    /**
     * Removes criteria from the list.
     *
     * @param index index of criteria.
     *
     * @throws IndexOutOfBoundsException if index &lt; 0 or index &gt;= getCriteriaCount().
     */
    public synchronized void removeCriteria(int index)
    {
        if (index < 0 || index >= getCriteriaCount())
        {
            throw new IndexOutOfBoundsException(MessageFormat.format(
                Strings.error("no.criteria.at.0"), index, getCriteriaCount()));
        }

        int currentSize = criteriaList.length;

        ICriteria[] newCriteriaList = new ICriteria[currentSize - 1];
        System.arraycopy(criteriaList, 0, newCriteriaList, 0, index);
        System.arraycopy(criteriaList, index + 1, newCriteriaList, index,
            currentSize - index - 1);

        criteriaList = newCriteriaList;
    }

    /**
     * Returns the number of criteria records in this query.
     *
     * @return number of criteria records.
     */
    public synchronized int getCriteriaCount()
    {
        return criteriaList.length;
    }

    /**
     * Returns criteria at the given index.
     *
     * @param index index.
     *
     * @return criteria.
     *
     * @throws IndexOutOfBoundsException if index &lt; 0 or index &gt;= getCriteriaCount().
     */
    public synchronized ICriteria getCriteriaAt(int index)
    {
        return criteriaList[index];
    }

    /**
     * Returns TRUE if the query is in AND-mode.
     *
     * @return TRUE for AND-mode.
     */
    public boolean isAndQuery()
    {
        return andQuery;
    }

    /**
     * Sets/resets the And/Or query flag. When set the query is following AND-logic when joining the
     * results.
     *
     * @param and TRUE to turn AND-mode on.
     */
    public void setAndQuery(boolean and)
    {
        andQuery = and;
    }

    /**
     * Returns the list of all properties this query can operate.
     *
     * @return available properties.
     */
    public Collection getAvailableProperties()
    {
        return Collections.unmodifiableList(availableProperties);
    }

    /**
     * Returns the property object by its descriptor. When you recreate query from some saved state
     * you need to use this method to get properties for stored criteria objects.
     *
     * @param descriptor descriptor which is previously returned by IProperty objects.
     *
     * @return property object or NULL if unknown.
     *
     * @throws NullPointerException     if the object is not specified.
     */
    public IProperty getPropertyByDescriptor(String descriptor)
    {
        if (descriptor == null) return null;

        for (IProperty property : availableProperties)
        {
            if (descriptor.equals(property.getDescriptor()) ||
                descriptor.equals(property.getName())) return property;
        }

        return null;
    }

    // ---------------------------------------------------------------------------------------------
    // Basic query operations
    // ---------------------------------------------------------------------------------------------

    /**
     * Validates the query. The query is valid when all criteria records are valid.
     *
     * @param removeDuplicates <code>TRUE</code> to remove duplicate articles.
     * @param maxDupWords maximum first duplicate words to treat as a filter.
     *
     * @return error message or NULL if valid. @param removeDuplicates
     */
    public synchronized String validate(boolean removeDuplicates, int maxDupWords)
    {
        String errorMessage = null;

        if (removeDuplicates && maxDupWords < 1)
        {
            errorMessage = Strings.message("smartfeed.type.validation.low.maxdupwords");
        } else if (criteriaList.length > 0)
        {
            for (int i = 0; errorMessage == null && i < criteriaList.length; i++)
            {
                ICriteria criteria = criteriaList[i];
                errorMessage = criteria.validate();
            }
        } else errorMessage = Strings.message("query.is.empty");

        return errorMessage;
    }

    /**
     * Returns TRUE if some object matches all/any query criteria, depending on the
     * <code>andQuery</code> switch.
     *
     * @param target target object.
     *
     * @return TRUE if matches.
     *
     * @throws ClassCastException   if the object is off unacceptable type.
     * @throws NullPointerException if the object isn't sepcified.
     */
    public boolean match(Object target)
    {
        boolean matching = false;

        boolean continuing = true;
        ICriteria[] criteriaCopy = copyWithEmptySlot(0);
        for (int i = 0; continuing && i < criteriaCopy.length; i++)
        {
            ICriteria criteria = criteriaCopy[i];
            matching = isCriteriaMatching(criteria, target);

            continuing = isAndQuery() ? matching : !matching;
        }

        return matching;
    }

    /**
     * Checks if the target is matching given criteria.
     *
     * @param criteria  criteria to check.
     * @param target    target to match against.
     *
     * @return target.
     */
    protected boolean isCriteriaMatching(ICriteria criteria, Object target)
    {
        String value = criteria.getValue();
        if (value != null) value = value.trim().toLowerCase();
        return criteria.getProperty().match(target, criteria.getComparisonOperation(), value);
    }

    /**
     * Returns TRUE if a criteria in this query refers to the sentiments property.
     *
     * @return TRUE if a criteria in this query refers to the sentiments property. 
     */
    public boolean hasSentimentsClause()
    {
        for (ICriteria criteria : criteriaList)
        {
            if (criteria.getProperty().getClass() == ArticleSentimentsProperty.class) return true;
        }

        return false;
    }

    /**
     * Compares this object to another one.
     *
     * @param o another object.
     *
     * @return <code>TRUE</code> if two objects are the same.
     */
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final BasicQuery basicQuery = (BasicQuery)o;

        if (andQuery != basicQuery.andQuery) return false;
        if (!Arrays.equals(criteriaList, basicQuery.criteriaList)) return false;

        return true;
    }

    /**
     * Returns the hash code.
     *
     * @return hash code.
     */
    public int hashCode()
    {
        return getClass().getName().hashCode();
    }
}
