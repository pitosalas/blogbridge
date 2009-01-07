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
// $Id: TestSendFeedbackDialog.java,v 1.3 2006/01/08 05:28:16 kyank Exp $
//

package com.salas.bb.dialogs;

import junit.framework.TestCase;

/**
 * This suite contains tests for <code>SendFeedbackDialog</code> unit.
 */
public class TestSendFeedbackDialog extends TestCase
{
    private SendFeedbackDialog.ForumEntry invalidForum;
    private SendFeedbackDialog.ForumEntry validForum;

    /**
     * Creates test.
     */
    public TestSendFeedbackDialog()
    {
        invalidForum = new SendFeedbackDialog.ForumEntry(-1, "A");
        validForum = new SendFeedbackDialog.ForumEntry(1, "A");
    }

    /**
     * Tests validation of incorrect form data.
     */
    public void testValidateIncorrectForum()
    {
        assertNotNull(SendFeedbackDialog.hasValidData(null, "A", "a@b.c", "A", "A"));
        assertNotNull(SendFeedbackDialog.hasValidData(invalidForum, "A", "a@b.c", "A", "A"));
    }

    /**
     * Tests validation of incorrect form data.
     */
    public void testValidateNoName()
    {
        assertNotNull(SendFeedbackDialog.hasValidData(validForum, null, "a@b.c", "A", "A"));
        assertNotNull(SendFeedbackDialog.hasValidData(validForum, "", "a@b.c", "A", "A"));
        assertNotNull(SendFeedbackDialog.hasValidData(validForum, " ", "a@b.c", "A", "A"));
    }

    /**
     * Tests validation of incorrect form data.
     */
    public void testValidateBadEmail()
    {
        assertNotNull(SendFeedbackDialog.hasValidData(validForum, "A", "a", "A", "A"));
        assertNotNull(SendFeedbackDialog.hasValidData(validForum, "A", "a@a", "A", "A"));
    }

    /**
     * Tests validation of incorrect form data.
     */
    public void testValidateNoSubject()
    {
        assertNotNull(SendFeedbackDialog.hasValidData(validForum, "A", "a@b.c", null, "A"));
        assertNotNull(SendFeedbackDialog.hasValidData(validForum, "A", "a@b.c", "", "A"));
    }

    /**
     * Tests validation of incorrect form data.
     */
    public void testValidateNoMessage()
    {
        assertNotNull(SendFeedbackDialog.hasValidData(validForum, "A", "a@b.c", "A", null));
        assertNotNull(SendFeedbackDialog.hasValidData(validForum, "A", "a@b.c", "A", ""));
    }

    /**
     * Tests accepting empty email address.
     */
    public void testValidateNoEmail()
    {
        assertNull(SendFeedbackDialog.hasValidData(validForum, "A", "", "A", "A"));
        assertNull(SendFeedbackDialog.hasValidData(validForum, "A", " ", "A", "A"));
    }
}
