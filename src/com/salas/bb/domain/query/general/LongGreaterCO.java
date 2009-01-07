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
// $Id: LongGreaterCO.java,v 1.4 2007/02/20 15:15:55 spyromus Exp $
//

package com.salas.bb.domain.query.general;

import com.salas.bb.domain.query.AbstractComparisonOperation;
import com.salas.bb.utils.i18n.Strings;

/**
 * Operation which is comparing two long numbers. The target should be greater than
 * value to get match.
 */
public class LongGreaterCO extends AbstractComparisonOperation
{
    /** Instance of this comparison operation. */
    public static final LongGreaterCO INSTANCE = new LongGreaterCO();

    /**
     * Creates operation.
     */
    public LongGreaterCO()
    {
        super(Strings.message("query.operation.is.greater.than"), "greater");
    }

    /**
     * Compares some target value against value for comparison.
     *
     * @param targetValue     target value.
     * @param comparisonValue comparison value.
     *
     * @return TRUE if the target value matches the condition presented by this comparison
     *         operation in conjunction with comparison value.
     */
    public boolean match(String targetValue, String comparisonValue)
    {
        return Long.parseLong(targetValue) > Long.parseLong(comparisonValue);
    }
}
