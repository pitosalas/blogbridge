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
// $Id: TestAbstractTagsDialog.java,v 1.2 2006/01/08 05:28:17 kyank Exp $
//

package com.salas.bb.tags;

import junit.framework.TestCase;

/**
 * This suite contains tests for <code>AbstractTagsDialog</code> unit.
 */
public class TestAbstractTagsDialog extends TestCase
{
    /**
     * Tests validation of entered data.
     */
    public void testDataValidationFailure()
    {
        String message;

        String description = "";
        String tags = "";
        message = AbstractTagsDialog.validate(description, tags);
        assertEquals(AbstractTagsDialog.ERR_DESCRIPTION_EMPTY, message);

        description = "Test";
        message = AbstractTagsDialog.validate(description, tags);
        assertEquals(AbstractTagsDialog.ERR_TAGS_EMPTY, message);

        tags = "a b \"c d\"";
        message = AbstractTagsDialog.validate(description, tags);
        assertEquals(AbstractTagsDialog.ERR_MULTIWORD_TAGS, message);
    }

    /**
     * Tests validation of entered data.
     */
    public void testDataValidation()
    {
        String message;
        String description = "Test";
        String tags = "a";

        message = AbstractTagsDialog.validate(description, tags);
        assertNull(message);
    }
}