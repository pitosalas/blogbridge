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
// $Id: TestStringUtils.java,v 1.25 2008/02/28 15:59:52 spyromus Exp $
//

package com.salas.bb.utils;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @see StringUtils
 */
public class TestStringUtils extends TestCase
{
    /**
     * @see StringUtils#decodeForced
     *
     * @throws Exception in case of any exceptions.
     */
    public void testEncodeForced() throws Exception
    {
        assertNull(StringUtils.decodeForced(null, null));
        assertNull(StringUtils.decodeForced(null, "A"));
        assertEquals("a", StringUtils.decodeForced("a", "A"));
        assertEquals((char)1056, StringUtils.decodeForced("\u00f2", "KOI8-R").toCharArray()[0]);
    }

    /**
     * Tests conversion from utf-8 byte arrays into Unicode.
     */
    public void testFromUTF8()
    {
        assertNull(StringUtils.fromUTF8((byte[])null));
        assertNull(StringUtils.fromUTF8((byte[][])null));

        assertEquals("abc", StringUtils.fromUTF8(new byte[] { 'a', 'b', 'c' }));
        assertEquals("\u07ff", StringUtils.fromUTF8(new byte[] { (byte)0xdf, (byte)0xbf }));

        String[] strs = StringUtils.fromUTF8(new byte[][]
            { { 'a' }, { 'b', 'c' } });
        assertEquals("a", strs[0]);
        assertEquals("bc", strs[1]);

        String[] str2 = StringUtils.fromUTF8(new byte[][]
            { { (byte)0xdf, (byte)0xbf }, { (byte)0xc2, (byte)0xa9 } });
        assertEquals("\u07ff", str2[0]);
        assertEquals("\u00a9", str2[1]);
    }

    /**
     * Tests simple conversion into UTF-8 bytes array.
     */
    public void testToUTF8()
    {
        assertNull(StringUtils.toUTF8(null));

        byte[] bytes;

        bytes = StringUtils.toUTF8("abc");
        assertEquals('a', bytes[0]);
        assertEquals('b', bytes[1]);
        assertEquals('c', bytes[2]);

        bytes = StringUtils.toUTF8("\u07ff");
        assertEquals((byte)0xdf, bytes[0]);
        assertEquals((byte)0xbf, bytes[1]);
    }

    /**
     * @see StringUtils#multilineToArray
     */
    public void testMultilineToArray()
    {
        String[] arr;

        assertNull(StringUtils.multilineToArray(null));

        arr = StringUtils.multilineToArray("a\n b\n\nc ");
        assertNotNull(arr);
        assertEquals(3, arr.length);
        assertEquals("a", arr[0]);
        assertEquals(" b", arr[1]);
        assertEquals("c ", arr[2]);
    }

    /**
     * @see StringUtils#anyToMultiline
     */
    public void testAnyToMultiline()
    {
        // TODO ???
        assertEquals("", StringUtils.anyToMultiline(null));

        assertEquals("a\nb", StringUtils.anyToMultiline(new String[] { "a", "b" }));
        assertEquals("1", StringUtils.anyToMultiline(new Integer(1)));
    }

    /**
     * @see StringUtils#arrayToMultiline
     */
    public void testArrayToMultiline()
    {
        assertNull(StringUtils.arrayToMultiline(null));

        assertEquals("", StringUtils.arrayToMultiline(new String[0]));
        assertEquals("", StringUtils.arrayToMultiline(new String[] { "" }));
        assertEquals("a", StringUtils.arrayToMultiline(new String[] { "a" }));
        assertEquals("a\nb", StringUtils.arrayToMultiline(new String[] { "a", "b" }));
    }

    /**
     * Tests the sentense termination detection.
     */
    public void testIsSentenseTerminator()
    {
        assertTrue(StringUtils.isSentenseTerminator('.'));
        assertTrue(StringUtils.isSentenseTerminator('!'));
        assertTrue(StringUtils.isSentenseTerminator('?'));
        assertFalse(StringUtils.isSentenseTerminator(':'));
        assertFalse(StringUtils.isSentenseTerminator(';'));
    }

    /**
     * Tests returning of the first sentense in the article.
     */
    public void testGetFirstSentense()
    {
        assertNull(StringUtils.getFirstSentense(null));

        assertEquals("Test", StringUtils.getFirstSentense("Test"));
        assertEquals("Test", StringUtils.getFirstSentense("  Test"));
        assertEquals("Test", StringUtils.getFirstSentense("\n\t Test"));
        assertEquals("Test", StringUtils.getFirstSentense("\n\t Test."));
        assertEquals("Test", StringUtils.getFirstSentense("Test.Test"));
        assertEquals("Test", StringUtils.getFirstSentense("\n\t Test. "));
        assertEquals("Test test", StringUtils.getFirstSentense("\n\t Test test "));
        assertEquals("Test \n\ttest", StringUtils.getFirstSentense("\n\t Test \n\ttest "));
        assertEquals("Test: here we go",
            StringUtils.getFirstSentense("\n\t Test: here we go "));
    }

    /**
     * Tests encoding for URL.
     */
    public void testEncodeForURL()
    {
        assertNull("Should return null.", StringUtils.encodeForURL(null));

        assertEquals("Haven't encoded the string.", "a+%26%3F", StringUtils.encodeForURL("a &?"));
    }

    /**
     * Tests converting the keywords string into array of keywords.
     */
    public void testKeywordsToArrayClear()
    {
        String[] keywords = StringUtils.keywordsToArray(" aa bb\nc ");
        assertTrue(dump(keywords), Arrays.equals(new String[] { "aa", "bb", "c" }, keywords));
    }

    /**
     * Tests handling broken quotes.
     */
    public void testKeywordsToArrayBrokenQuotes()
    {
        String[] keywords = StringUtils.keywordsToArray("\"a b c ");
        assertTrue(dump(keywords), Arrays.equals(new String[] { "a", "b", "c" }, keywords));
    }

    /**
     * Tests handling normal quotes.
     */
    public void testKeywordsToArrayQuotes()
    {
        String[] keywords = StringUtils.keywordsToArray("\" a b \" c");
        assertTrue(dump(keywords), Arrays.equals(new String[] { " a b ", "c" }, keywords));
    }

    /**
     * Tests conditional quoting of keywords.
     */
    public void testQuoteKeywordIfNecessary()
    {
        assertEquals("No quotes, trimmed spaces.", "test", StringUtils.quoteKeywordIfNecessary(" test "));
        assertEquals("Quotes, trimmed spaces.", "\"test a\"", StringUtils.quoteKeywordIfNecessary(" test a "));
        assertEquals("No extra quotes, trimmed spaces.", "\" test a\"", StringUtils.quoteKeywordIfNecessary("\" test a\" "));
    }

    /**
     * Tests conversion of old format keywords into new look.
     */
    public void testConvertKeywordsToNewFormat()
    {
        assertEquals("abc cba \"a b c\"", StringUtils.convertKeywordsToNewFormat("abc|cba|a b c"));
        assertEquals("abc cba \"a b c\"", StringUtils.convertKeywordsToNewFormat("abc, cba, a b c"));
    }

    /**
     * Test cleaning the URL which was dragged into application.
     */
    public void testCleanDraggedURL()
    {
        assertNull(StringUtils.cleanDraggedURL(null));

        assertEquals("http://a", StringUtils.cleanDraggedURL(" http://a "));
        assertEquals("http://a", StringUtils.cleanDraggedURL("feed://a"));
        assertEquals("http://a", StringUtils.cleanDraggedURL("feed://a\nthis is my test"));
    }

    private static String dump(String[] array)
    {
        return StringUtils.join(array, "~");
    }


    /**
     * Testing basic keywrods transformations.
     */
    public void testKeywordToPatterns()
    {
        assertPattern("a|b", new String[] { "a", "b" });
        assertPattern("a\\s+b|c", new String[] { "a b", "c" });

        assertPattern("a\\w*|\\w*b|c\\w+|\\w+d", new String[] { "a*", "*b", "c+", "+d" });
    }

    /**
     * Testing basic pattern transformations.
     */
    public void testKeywordToPattern()
    {
        assertPattern("a", "a");

        assertPattern("\\w*a", "*a");
        assertPattern("a\\w*", "a*");
        assertPattern("\\w+a", "+a");
        assertPattern("a\\w+", "a+");

        assertPattern("a\\s+(\\w*\\s+)?b", "\"a * b\"");
        assertPattern("a\\s+\\w+\\s+b", "\"a + b\"");

        assertPattern("a\\(\\)\\[\\]\\\\", "a()[]\\");

        assertRawPattern("(^|\\s)(abc)($|\\s)", "\" abc \"");
        assertRawPattern("(^|\\W)(abc)($|\\s)", "\"abc \"");
        assertRawPattern("(^|\\s)(abc)($|\\W)", "\" abc\"");
    }

    /**
     * Tests pattern matching.
     */
    public void testPattern()
    {
        assertMatch("at", "\" at \"");
        assertMatch("a at", "\" at \"");
        assertMatch("a at b", "\" at \"");
        assertNotMatch("a attack b", "\" ta \"");
    }

    private void assertNotMatch(String str, String keywords)
    {
        assertFalse(Pattern.compile(StringUtils.keywordsToPattern(keywords)).matcher(str).find());
    }

    private void assertMatch(String str, String keywords)
    {
        assertTrue(Pattern.compile(StringUtils.keywordsToPattern(keywords)).matcher(str).find());
    }

    private void assertPattern(String target, String[] keywords)
    {
        String pattern = StringUtils.keywordsToPattern(keywords);
        assertEquals("(^|\\W)(" + target + ")($|\\W)", pattern);
    }

    private void assertPattern(String aTarget, String aKeywords)
    {
        String pattern = StringUtils.keywordsToPattern(aKeywords);
        assertEquals("(^|\\W)(" + aTarget + ")($|\\W)", pattern);
    }

    private void assertRawPattern(String aTarget, String aKeywords)
    {
        String pattern = StringUtils.keywordsToPattern(aKeywords);
        assertEquals(aTarget, pattern);
    }

    /**
     * Testing that the rules of regex matching and group returning are unchanged.
     */
    public void testMatchingRules()
    {
        Pattern pattern = Pattern.compile("(^|\\W)(a)($|\\W)");

        Matcher mat = pattern.matcher("b a b");
        assertTrue(mat.find());
        assertEquals(2, mat.start(2));
        assertEquals(3, mat.end(2));

        mat = pattern.matcher("a");
        assertTrue(mat.find());
        assertEquals(0, mat.start(2));
        assertEquals(1, mat.end(2));
    }

    /**
     * Tests fixing of URL's.
     */
    public void testFixURL()
    {
        assertNull("NULL should be returned for NULL.", StringUtils.fixURL(null));

        assertNull("NULL should be returned for empty string.", StringUtils.fixURL(""));
        assertNull("NULL should be returned for empty string.", StringUtils.fixURL(" "));

        assertEquals("http:// should be prepended for URL without protocol and spaces removed.",
            "http://a", StringUtils.fixURL(" a "));

        assertEquals("Protocol should be preserved.",
            "https://a", StringUtils.fixURL(" https://a "));

        assertEquals("There's FEED: without HTTP.",
            "http://a", StringUtils.fixURL("feed://a"));

        assertEquals("There's HTTP with FEED.",
            "http://a", StringUtils.fixURL("feed:http://a"));

        assertEquals("There's HTTP with FEED.",
            "http://a", StringUtils.fixURL("feed://http://a"));
    }

    /**
     * Simple unescaping test.
     */
    public void testQuickUnescape()
    {
        assertEquals("http://test?a=1&b=2", StringUtils.quickUnescape("http://test?a=1&amp;b=2"));
    }

    /**
     * Tests e-mail check.
     */
    public void testValidEmail()
    {
        assertTrue(StringUtils.isValidEmail("a@a.com"));
        assertTrue(StringUtils.isValidEmail("gaetan@nixonmcinnes.co.uk"));

        assertFalse(StringUtils.isValidEmail("test@nixonmcinnes"));
        assertFalse(StringUtils.isValidEmail("gaetan_nixonmcinnes"));
    }

    /**
     * Tests get up to n words function.
     */
    public void testGetUpToNWords()
    {
        assertEquals("a b", StringUtils.getUpToNWords("a b c", 2));
        assertEquals("a", StringUtils.getUpToNWords("a b c", 1));
    }

    /** Tests returning empty result on empty request. */
    public void testExcerpt_Empty()
    {
        assertEquals("", StringUtils.excerpt("", 1, 2, 2));
        assertEquals(" ", StringUtils.excerpt(" ", 1, 2, 2));
        assertNull(StringUtils.excerpt(null, 1, 2, 2));
    }

    /** Simple return and cut test. */
    public void testExcerpt_Simple()
    {
        assertEquals("abc", StringUtils.excerpt("abc", 1, 1, 100));
        assertEquals("ab...", StringUtils.excerpt("abcd", 1, 1, 2));
    }

    /** Complex test with sentences. */
    public void testExcerpt_Complex()
    {
        assertEquals("ho!", StringUtils.excerpt("ho! ho!? ho!!! ho.", 1, 1, 100));
        assertEquals("ho! ho!?", StringUtils.excerpt("ho! ho!? ho!!! ho.", 2, 1, 100));
        assertEquals("ho! ho!? ho!!!", StringUtils.excerpt("ho! ho!? ho!!! ho.", 3, 1, 100));
        assertEquals("ho! ho!? ho!!! ho.", StringUtils.excerpt("ho! ho!? ho!!! ho.", 4, 1, 100));

        assertEquals("ho! h...", StringUtils.excerpt("ho! ho!? ho!!! ho.", 4, 1, 5));

        String question =
            "In brief mode we show some 300 characters from the beginning, " +
            "but what if we could configure what the brief mode shows? It " +
            "can show either some number of characters or sentences. I'm " +
            "studying writing these days and found that the paragraph usually " +
            "starts with some topic sentence followed by one or two explanatory " +
            "sentences, followed by examples. For the brief mode, top 3 sentences " +
            "may hold enough value to be useful. So the config might look like this:";
        String answer =
            "In brief mode we show some 300 characters from the beginning, " +
            "but what if we could configure what the brief mode shows? It " +
            "can show either some number of characters or sentences. I'm " +
            "studying writing these days and found that the paragraph usually " +
            "starts with some topic sentence followed by one or two explanatory " +
            "sentences, followed by examples.";
        assertEquals(answer, StringUtils.excerpt(question, 3, 50, 1000));
    }

    /** Testing the acceptable limit: 10000 texts per second (on mobile Athlon 2400+). */
    public void testExcerpt_Timing()
    {
        String question =
            "In brief mode we show some 300 characters from the beginning, " +
            "but what if we could configure what the brief mode shows? It " +
            "can show either some number of characters or sentences. I'm " +
            "studying writing these days and found that the paragraph usually " +
            "starts with some topic sentence followed by one or two explanatory " +
            "sentences, followed by examples. For the brief mode, top 3 sentences " +
            "may hold enough value to be useful. So the config might look like this:";

        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) StringUtils.excerpt(question, 3, 50, 1000);
        long finish = System.currentTimeMillis();

        assertTrue("Took: " + (finish - start), finish - start < 2000);
    }

    public void testGetLowercaseWords_Empty()
    {
        assertNull(StringUtils.getWordsInRange(null, 0, 1));
        assertEquals("", StringUtils.getWordsInRange("", 0, 1));
        assertEquals(" ", StringUtils.getWordsInRange(" ", 0, 1));
    }

    public void testGetLowercaseWords_Normal()
    {
        assertEquals("b c d", StringUtils.getWordsInRange(" a b,  C:/D e", 1, 3));
    }

    public void testGetLowercaseWords_NotEnoughWords()
    {
        assertEquals("b c d", StringUtils.getWordsInRange(" a b,  C:/D", 1, 5));
    }

    public void testUnescape()
    {
        assertEquals("abc", StringUtils.unescape("abc"));
        assertEquals("abc\u2026", StringUtils.unescape("abc&hellip;"));
        assertEquals("abc\u2026", StringUtils.unescape("abc&#x2026;"));
        assertEquals("abc\u2026", StringUtils.unescape("abc&#8230;"));
    }

    public void testCollectLinks()
    {
        assertLinks(new String[0], "");
        assertLinks(new String[] { "http://a.bc/123" }, "http://a.bc/123");
        assertLinks(new String[] { "http://a.bc/123", "http://bc.d/123a" }, "Check: http://a.bc/123, http://bc.d/123a.");
    }

    private void assertLinks(String[] links, String text)
    {
        List<String> res = StringUtils.collectLinks(text);
        assertEquals(links.length, res.size());
        for (int i = 0; i < links.length; i++)
        {
            assertEquals(links[i], res.get(i));
        }
    }
}
