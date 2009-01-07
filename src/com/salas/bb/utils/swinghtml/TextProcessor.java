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
// $Id: TextProcessor.java,v 1.15 2008/04/04 14:03:27 spyromus Exp $
//

package com.salas.bb.utils.swinghtml;

import com.salas.bb.utils.Constants;
import com.salas.bb.utils.htmlparser.HtmlParser;
import com.salas.bb.utils.htmlparser.IHtmlParserListener;
import com.salas.bb.utils.htmlparser.utils.StringBuilderListener;

import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple text processor utility which is using <code>HtmlParser</code> and company to prepare
 * any HTML source for displaing.
 * <p>
 * There's Swing and non-Swing modes available. When in Swing mode the processor will prepare
 * text to be correctly displayed in Swing components, while when in non-Swing mode it will
 * simply clean and straighten things up.
 */
public final class TextProcessor
{
    private static final Pattern PAT_ALL_TAGS = Pattern.compile("<[^>]+>");

    private static final Pattern PAT_ENTITIES =
        Pattern.compile("&[a-zA-Z]{2,6};", Pattern.CASE_INSENSITIVE);
    private static final Pattern PAT_NUMERIC_ENTITIES =
        Pattern.compile("&#(x?[0-9a-fA-F]{1,4})(;|(\\s)|$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PAT_QUOT =
        Pattern.compile("&quot(;|(\\s)|$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PAT_NBSP =
        Pattern.compile("&nbsp(;|(\\s)|$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PAT_APOS =
        Pattern.compile("&apos(;|(\\s)|$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PAT_AMP =
        Pattern.compile("&amp(;|(\\s)|$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PAT_LT =
        Pattern.compile("&lt(;|(\\s)|$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PAT_GT =
        Pattern.compile("&gt(;|(\\s)|$)", Pattern.CASE_INSENSITIVE);
    public static final Pattern PAT_BACKGROUND_ATTR =
        Pattern.compile("(<[^>]+)background\\s*=\\s*('[\\s]*'|\"[\\s]*\")([^>]*>)",
        Pattern.CASE_INSENSITIVE);

    private static final Pattern PATTERN = Pattern.compile("&((nbsp)|(lt)|(gt)|(apos)|(quot)|(amp));");
    private static final String[] REPLACEMENT = { " ", "<", ">", "'", "\"", "&" };

    /**
     * Hidden utility class constructor.
     */
    private TextProcessor()
    {
    }

    /**
     * Process text to return clean HTML text no longer than limit.
     *
     * @param text      text.
     * @param sizeLimit limit in characters.
     *
     * @return HTML text.
     */
    public static String processHTML(String text, int sizeLimit)
    {
        return process(text, sizeLimit, true);
    }

    /**
     * Process text to return clean plain text no longer than limit.
     *
     * @param text      text.
     * @param sizeLimit limit in characters.
     *
     * @return plain text.
     */
    public static String processPlain(String text, int sizeLimit)
    {
        return process(text, sizeLimit, false);
    }

    /**
     * Process source text with several flags.
     *
     * @param aText     original text.
     * @param sizeLimit size limitation in chars.
     * @param html      FALSE to remove styling markup, but leave structural (p, blockquotes...).
     *
     * @return result.
     */
    static String process(String aText, int sizeLimit, boolean html)
    {
        if (aText == null) return null;

        IHtmlParserListener listener;
        HtmlParser parser = new HtmlParser(true);

        StringBuilderListener bufListener = new StringBuilderListener(aText.length(), sizeLimit);
        listener = html ? new SwingHtmlFilter(bufListener) : new SwingPlainFilter(bufListener);

        try
        {
            parser.parse(new StringReader(aText), listener);
        } catch (IOException e)
        {
            // OK. Buffer will be empty.
        }

        String result = bufListener.toString();

        result = PAT_BACKGROUND_ATTR.matcher(result).replaceAll("$1$3".intern());
        result = convertEntity(result, "&mdash(;|(\\s)|$)".intern(), "\u2014".intern());
        // of course we have to put here more entities, but it isn't clear which of them

        return result;
    }

    /**
     * Converts numeric entities (&amp;#YYYY, &amp;#YY, &amp;#xYYYY or others) into Unicode chars.
     *
     * @param text original text.
     *
     * @return output.
     */
    public static String convertNumericHTMLEntities(String text)
    {
        if (text == null) return null;

        Matcher m = PAT_NUMERIC_ENTITIES.matcher(text);
        StringBuffer buf = new StringBuffer();
        while (m.find())
        {
            String value = m.group(1);
            if (value.length() > 0)
            {
                char c;
                String replacement;
                try
                {
                    if (value.toLowerCase().charAt(0) == 'x')
                    {
                        c = (char)Integer.parseInt(value.substring(1), 16);
                    } else
                    {
                        c = (char)Integer.parseInt(value);
                    }

                    char[] chars;
                    if (c == 92 || c == 36)
                    {
                        chars = new char[] { '\\', c };
                    } else
                    {
                        chars = new char[] { c };
                    }

                    replacement = new String(chars);
                } catch (NumberFormatException e)
                {
                    replacement = Constants.EMPTY_STRING;
                }

                String tail = m.group(3);
                if (tail != null) replacement += tail;
                m.appendReplacement(buf, replacement);
            }
        }
        m.appendTail(buf);

        return buf.toString();
    }

    /**
     * Converts &amp;amp;, &amp;apos;, &amp;nbsp; and &amp;quot; entities in quivalents.
     *
     * @param text  source text.
     *
     * @return result.
     */
    public static String convertHTMLEntities(String text)
    {
        if (text == null) return null;

        text = convertEntity(text, PAT_AMP, "&");
        text = convertEntity(text, PAT_APOS, "'");
        text = convertEntity(text, PAT_NBSP, " ");
        text = convertEntity(text, PAT_QUOT, "\"");
        text = convertEntity(text, PAT_LT, "<");
        text = convertEntity(text, PAT_GT, ">");

        return text;
    }

    /**
     * Replaces entity with a corresponding replacement. The tail is taken from second group.
     *
     * @param text          source text.
     * @param pattern       pattern.
     * @param replacement   replacement.
     *
     * @return result.
     */
    private static String convertEntity(String text, String pattern, String replacement)
    {
        return convertEntity(text, Pattern.compile(pattern), replacement);
    }

    /**
     * Replaces entity with a corresponding replacement. The tail is taken from second group.
     *
     * @param text          source text.
     * @param pattern       pattern.
     * @param replacement   replacement.
     *
     * @return result.
     */
    private static String convertEntity(String text, Pattern pattern, String replacement)
    {
        if (text == null) return null;

        Matcher m = pattern.matcher(text);
        StringBuffer buf = new StringBuffer();
        while (m.find())
        {
            String rep = replacement;
            String tail = m.group(2);
            if (tail != null) rep += tail;
            m.appendReplacement(buf, rep);
        }
        m.appendTail(buf);

        return buf.toString();
    }

    /**
     * Replaces all detected entities with space.
     *
     * @param text source text.
     *
     * @return result.
     */
    public static String removeHTMLEntities(String text)
    {
        return text == null ? null : PAT_ENTITIES.matcher(text).replaceAll(" ".intern());
    }

    /**
     * Removes all found tags from text.
     *
     * @param text source text.
     *
     * @return result.
     */
    public static String removeTags(String text)
    {
        return text == null ? null : PAT_ALL_TAGS.matcher(text).replaceAll(Constants.EMPTY_STRING);
    }

    /**
     * Returns the excerpt from text. The excerpt is a text, having no more than defined number
     * of distinct words and doesn't spanning across multiple lines. The exceprpt may be terminated
     * with new line or natural sentense terminator: '.', '!' or '?'.
     *
     * @param text  text to get excerpt from.
     * @param words maximum number of words.
     *
     * @return result.
     */
    public static String getExcerpt(String text, int words)
    {
        if (text == null) return null;

        int i = 0;
        int length = text.length();

        // Find letter or digit
        boolean found = false;
        while (i < length && !(found = !Character.isWhitespace(text.charAt(i)))) i++;

        String excerpt = null;
        if (found)
        {
            int start = i;
            int end = -1;
            int count = 0;
            boolean isInWord = false;

            // Spin until we encounter new line, sentense terminator, the end of text or
            // number of words will match the limit.
            while (excerpt == null && count <= words && i < length)
            {
                char ch = text.charAt(i);
                if (ch == 0x0a || ch == 0x0d)
                {
                    // New line found -- get the excerpt
                    excerpt = text.substring(start, i) + "...";
                } else if (ch == '.' || ch == '!' || ch == '?')
                {
                    // Sentence terminator found -- get the excerpt
                    excerpt = text.substring(start, i + 1);
                } else
                {
                    // Some other character
                    if (Character.isLetterOrDigit(ch))
                    {
                        if (!isInWord)
                        {
                            count++;
                            isInWord = true;
                        }
                    } else
                    {
                        isInWord = false;
                        end = i;
                    }
                }
                i++;
            }

            // If we still don't have the excerpt it means that we have reached the end
            // of text. Take everything from it and finish.
            if (excerpt == null) excerpt = text.substring(start, i == length ? i : end);
            if (count > words) excerpt += "...";
        } else excerpt = Constants.EMPTY_STRING;

        return excerpt;
    }

    /**
     * Cleans the title of the article. Converts known HTML entities into strings,
     * removes unknown HTML entities and tags. If title isn't present, some words
     * from the head of the text will be returned.
     *
     * @param title title to process.
     * @param text article text to get title from if title isn't defined.
     *
     * @return title of the article.
     */
    public static String filterTitle(String title, String text)
    {
        boolean excerptRequired = false;
        if (title == null && text != null)
        {
            title = text;
            excerptRequired = true;
        }

        if (title != null)
        {
            title = removeTags(title);
            title = convertNumericHTMLEntities(title);
            title = convertHTMLEntities(title);
            title = removeHTMLEntities(title);

            if (excerptRequired)
            {
                title = getExcerpt(title, Constants.WORDS_IN_EXCERPT);
            }
        }

        return title;
    }

    /**
     * Cleans the text of the article. Removes all garbage and constructs which
     * aren't understood by Swing and its renderers. If text isn't present null
     * will be returned.
     *
     * @param text original text.
     *
     * @return filtered text.
     */
    public static String filterText(String text)
    {
        if (text == null) return null;

        text = text.replaceAll("<style[^>]*>[^<]*</style>", "");
        text = processHTML(text, Constants.ARTICLE_SIZE_LIMIT);
        text = removeLeadingParagraphs(text);

        return text;
    }

    /**
     * Removes all leading paragraph signs to avoid unnecessary spacing.
     *
     * @param text text to process.
     *
     * @return results.
     */
    static String removeLeadingParagraphs(String text)
    {
        return text.replaceFirst("^(\\s*<[pP]>)+", "").trim();
    }

    /**
     * Converts raw text to plain.
     *
     * @param html raw HTML.
     *
     * @return plain text version.
     */
    public static String toPlainText(String html)
    {
        String result;

        Matcher m = PATTERN.matcher(html);
        if (m.find())
        {
            StringBuffer sb = new StringBuffer();
            do {
                int matchIndex = findMatchIndex(m);
                m.appendReplacement(sb, REPLACEMENT[matchIndex]);
            } while (m.find());
            m.appendTail(sb);

            result = sb.toString();
        } else
        {
            result = html;
        }

        return result;
    }

    /**
     * Finds the matching group number.
     *
     * @param m matcher.
     *
     * @return group index.
     */
    private static int findMatchIndex(Matcher m)
    {
        for (int i = 2; i <= m.groupCount(); i++)
        {
            String val = m.group(i);
            if (val != null) return i - 2;
        }

        return 0;
    }
}
