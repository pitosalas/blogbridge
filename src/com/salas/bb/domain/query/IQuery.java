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
// $Id: IQuery.java,v 1.3 2006/12/13 07:40:17 spyromus Exp $
//

package com.salas.bb.domain.query;

import java.util.Collection;

/**
 * Query object. Consists of several criteria records. Query can join the criteria with
 * "AND" or "OR" to get the final match answer.
 *
 * Each particular implementation of query knows how to handle specific type of objects.
 * It provides the list of all available properties it supports. When building the query
 * this information is enough to create criteria objects on the fly using the provided
 * meta-information.
 *
 * When rebuilding the query during deserialization it's possible to get the properties
 * by their descriptors.
 */
public interface IQuery
{
    /**
     * Returns the number of criteria records in this query.
     *
     * @return number of criteria records.
     */
    int getCriteriaCount();

    /**
     * Returns criteria at the given index.
     *
     * @param index index.
     *
     * @return criteria.
     *
     * @throws IndexOutOfBoundsException if index &lt; 0 or index &gt;= getCriteriaCount().
     */
    ICriteria getCriteriaAt(int index);

    /**
     * Creates new criteria record and adds it to the tail of the criteria list.
     *
     * @return criteria record.
     */
    ICriteria addCriteria();

    /**
     * Removes criteria from the list.
     *
     * @param index index of criteria.
     *
     * @throws IndexOutOfBoundsException if index &lt; 0 or index &gt;= getCriteriaCount().
     */
    void removeCriteria(int index);

    /**
     * Validates the query. The query is valid when all criteria records are valid.
     *
     * @param removeDuplicates <code>TRUE</code> to remove duplicate articles.
     * @param maxDupWords maximum first duplicate words threshold.
     *
     * @return error message or NULL if valid.
     */
    String validate(boolean removeDuplicates, int maxDupWords);

    /**
     * Returns the list of all properties this query can operate.
     *
     * @return available properties.
     */
    Collection getAvailableProperties();

    /**
     * Returns the property object by its descriptor. When you recreate query from
     * some saved state you need to use this method to get properties for stored
     * criteria objects.
     *
     * @param descriptor descriptor which is previously returned by IProperty objects.
     *
     * @return property object or NULL if unknown.
     *
     * @throws NullPointerException if the object is not specified.
     */
    IProperty getPropertyByDescriptor(String descriptor);

    /**
     * Sets/resets the And/Or query flag. When set the query is following AND-logic when
     * joining the results.
     *
     * @param and TRUE to turn AND-mode on.
     */
    void setAndQuery(boolean and);

    /**
     * Returns TRUE if the query is in AND-mode.
     *
     * @return TRUE for AND-mode.
     */
    boolean isAndQuery();

    /**
     * Returns TRUE if some object matches the query criteria.
     *
     * @param target target object.
     *
     * @return TRUE if matches.
     *
     * @throws ClassCastException if the object is off unacceptable type.
     * @throws NullPointerException if the object isn't sepcified.
     */
    boolean match(Object target);
}
