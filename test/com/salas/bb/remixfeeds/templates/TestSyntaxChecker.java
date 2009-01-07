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
// $Id: TestSyntaxChecker.java,v 1.2 2008/04/03 08:53:25 spyromus Exp $
//

package com.salas.bb.remixfeeds.templates;

import com.salas.bb.utils.StringUtils;
import junit.framework.TestCase;

import java.util.List;

/**
 * Tests template rendering.
 */
public class TestSyntaxChecker extends TestCase
{
    // ------------------------------------------------------------------------
    // Basic cases
    // ------------------------------------------------------------------------

    /** No markup. */
    public void testCorrect_NoMarkup()
    {
        assertTrue(SyntaxChecker.validate("abc").isEmpty());
    }

    /** Handling when multiple articles are given for a single-article template. */
    public void testCorrect_MultipleArticlesForSingleTemplate() {}

    // ------------------------------------------------------------------------
    // IF
    // ------------------------------------------------------------------------

    public void testCorrect_If()
    {
        assertValid(
            "text\n" +
            "# if single article\n" +
            "   text\n" +
            "# endif");
    }

    public void testIncorrect_If()
    {
        assertInvalid("Unknown operation: 'if'",
            "text\n" +
            "# if\n" +
            "# endif");

        assertInvalid("IF block isn't closed",
            "# if single article\n");
    }

    public void testCorrect_Else()
    {
        assertValid(
            "# if single article\n" +
            "   text\n" +
            "# else\n" +
            "   some other text\n" +
            "# endif");
    }

    public void testIncorrect_Else()
    {
        assertInvalid("ELSE can be only inside IF-ENDIF block",
            "# else\n");

        assertInvalid("ELSE can be only inside IF-ENDIF block",
            "# for each article\n" +
            "# else\n" +
            "# endfor");
    }

    // ------------------------------------------------------------------------
    // FOR EACH ARTICLE
    // ------------------------------------------------------------------------

    public void testCorrect_ForEach()
    {
        assertValid(
            "# for each article\n" +
            "   hello\n" +
            "# endfor");
    }

    public void testIncorrect_ForEach_InvalidStart()
    {
        assertInvalid("Unknown operation: 'for'",
            "# for\n" +
            "# endfor");
    }

    public void testIncorrect_ForEach_NoEnd()
    {
        assertInvalid("FOR block isn't closed",
            "# for each article\n" +
            "   hi");
    }

    // ------------------------------------------------------------------------
    // --- Helpers
    // ------------------------------------------------------------------------

    /**
     * Asserts the text is valid pattern.
     *
     * @param text text.
     */
    private void assertValid(String text)
    {
        List<SyntaxError> errors = SyntaxChecker.validate(text);
        String errorText = StringUtils.join(errors.iterator(), "\n");
        assertEquals("", errorText);
    }

    /**
     * Makes sure the text is invalid pattern.
     *
     * @param message   error message.
     * @param text      text.
     */
    private void assertInvalid(String message, String text)
    {
        List<SyntaxError> errors = SyntaxChecker.validate(text);
        assertFalse(errors.isEmpty());
        assertEquals(message, errors.get(0).getMessage());
    }
}