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
// $Id: AbstractTagsProperty.java,v 1.6 2007/03/29 15:14:54 spyromus Exp $
//

package com.salas.bb.domain.query.articles;

import com.salas.bb.domain.ITaggable;
import com.salas.bb.domain.query.AbstractProperty;
import com.salas.bb.domain.query.IComparisonOperation;
import com.salas.bb.domain.query.PropertyType;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;

/**
 * Abstract definition of tags property.
 */
abstract class AbstractTagsProperty extends AbstractProperty
{
    /**
     * Creates propery with given descriptor, type, default value and list of supported
     * comparison operations.
     *
     * @param aName                 name.
     * @param aDescriptor           descriptor.
     * @param aType                 type.
     * @param aDefaultValue         default value.
     * @param aComparisonOperations list of supported comparison operations.
     */
    public AbstractTagsProperty(String aName, String aDescriptor, PropertyType aType, String aDefaultValue,
                                IComparisonOperation[] aComparisonOperations)
    {
        super(aName, aDescriptor, aType, aDefaultValue, aComparisonOperations);
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
        ITaggable taggable = getTaggableObject(target);

        boolean matching = false;

        if (taggable != null && StringUtils.isNotEmpty(value))
        {
            String[] tags = taggable.getUserTags();
            String tagsString = tags == null ? "" : StringUtils.join(tags, " ");

            matching = operation.match(tagsString, value);
        }

        return matching;
    }

    /**
     * Returns taggable object corresponding to the target.
     *
     * @param target target.
     *
     * @return taggable object or <code>NULL</code>.
     */
    protected ITaggable getTaggableObject(Object target)
    {
        return target instanceof ITaggable ? (ITaggable)target : null;
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
        String error = super.validateValue(operation, value);

        if (error == null)
        {
            if (value.indexOf('"') != -1) error = Strings.message("multi.word.tags.not.allowed");
        }

        return error;
    }
}
