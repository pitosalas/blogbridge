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
// $Id: StringUtils.java,v 1.42 2007/11/07 17:16:48 spyromus Exp $
//

package com.salas.bb.utils;

import com.salas.bb.utils.i18n.Strings;
import sun.io.Converters;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collection of string utilities.
 */
public final class StringUtils extends org.apache.commons.lang.StringUtils
{
    private static final Logger LOG = Logger.getLogger(StringUtils.class.getName());

    private static final String[] SIZE_UNIT = { "Bytes", "Kb", "Mb", "Gb" };

    private static final DecimalFormat FORMAT = new DecimalFormat();

    private static final Pattern PATTERN_KEYWORDS =
        Pattern.compile("\\s*((\\\"([^\\\"]*)\\\"|([^\\s\\\"]+))\\s*)");
    private static final Pattern PATTERN_URL_WITH_PROTOCOL =
        Pattern.compile("^[a-zA-Z]+:/");
    private static final Pattern PATTERN_PUNCTUATION =
        Pattern.compile("([\\-\\+#\\$%^&\\_*\\s,.\\(\\)\\[\\]<>!\\?\"':;/\\\\])+");

    private static final Map<String, Character> ENTITIES = new HashMap<String, Character>();

    static
    {
        FORMAT.setMaximumFractionDigits(1);
        FORMAT.setMinimumFractionDigits(1);

        String[] fromA0 = {
            "nbsp",   "iexcl",  "cent",   "pound",  "curren", "yen",    "brvbar", "sect",
            "uml",    "copy",   "ordf",   "laquo",  "not",    "shy",    "reg",    "macr",
            "deg",    "plusmn", "sup2",   "sup3",   "acute",  "micro",  "para",   "middot",
            "cedil",  "sup1",   "ordm",   "raquo",  "frac14", "frac12", "frac34", "iquest",
            "Agrave", "Aacute", "Acirc",  "Atilde", "Auml",   "Aring",  "AElig",  "Ccedil",
            "Egrave", "Eacute", "Ecirc",  "Euml",   "Igrave", "Iacute", "Icirc",  "Iuml",
            "ETH",    "Ntilde", "Ograve", "Oacute", "Ocirc",  "Otilde", "Ouml",   "times",
            "Oslash", "Ugrave", "Uacute", "Ucirc",  "Uuml",   "Yacute", "THORN",  "szlig",
            "agrave", "aacute", "acirc",  "atilde", "auml",   "aring",  "aelig",  "ccedil",
            "egrave", "eacute", "ecirc",  "euml",   "igrave", "iacute", "icirc",  "iuml",
            "eth",    "ntilde", "ograve", "oacute", "ocirc",  "otilde", "ouml",   "divide",
            "oslash", "ugrave", "uacute", "ucirc",  "uuml",   "yacute", "thorn",  "yuml"
        };
        for (int i = 0; i < fromA0.length; i++) ENTITIES.put(fromA0[i], (char)(0xa0 + i));
        ENTITIES.put("trade", (char)8482);
        ENTITIES.put("OElig", (char)338);
        ENTITIES.put("oelig", (char)339);
        ENTITIES.put("Scaron", (char)352);
        ENTITIES.put("scaron", (char)353);
        ENTITIES.put("Yuml", (char)376);
        ENTITIES.put("circ", (char)710);
        ENTITIES.put("tilde", (char)732);
        ENTITIES.put("ensp", (char)8194);
        ENTITIES.put("emsp", (char)8195);
        ENTITIES.put("thinsp", (char)8201);
        ENTITIES.put("zwnj", (char)8204);
        ENTITIES.put("zwj", (char)8205);
        ENTITIES.put("lrm", (char)8206);
        ENTITIES.put("rrm", (char)8207);
        ENTITIES.put("ndash", (char)8211);
        ENTITIES.put("mdash", (char)8212);
        ENTITIES.put("lsquo", (char)8216);
        ENTITIES.put("rsquo", (char)8217);
        ENTITIES.put("sbquo", (char)8218);
        ENTITIES.put("ldquo", (char)8220);
        ENTITIES.put("rdquo", (char)8221);
        ENTITIES.put("bdquo", (char)8222);
        ENTITIES.put("dagger", (char)8224);
        ENTITIES.put("Dagger", (char)8225);
        ENTITIES.put("hellip", (char)8230);
        ENTITIES.put("permil", (char)8240);
        ENTITIES.put("lsaquo", (char)8249);
        ENTITIES.put("rsaquo", (char)8250);
        ENTITIES.put("euro", (char)8364);
        ENTITIES.put("amp", '&');
        ENTITIES.put("lt", '<');
        ENTITIES.put("gt", '>');
        ENTITIES.put("apos", '\'');
        ENTITIES.put("quot", '"');
    }

    /**
     * Hidden utility class constructor.
     */
    private StringUtils()
    {
    }

    /**
     * Converts array of bytes in UTF-8 encoding to appropriate string. If encoding
     * isn't supported then the array will be converted into string using default
     * encoding and record will be put in log with severe priority.
     *
     * @param string    bytes forming string.
     *
     * @return resulting string.
     */
    public static String fromUTF8(byte[] string)
    {
        if (string == null) return null;

        String str;

        try
        {
            str = new String(string, "UTF-8");
        } catch (UnsupportedEncodingException e)
        {
            LOG.severe(Strings.error("utf8.not.supported"));
            str = new String(string);
        }

        return str;
    }

    /**
     * Converts array of byte arrays in UTF-8 encoding to array of strings. The notes are
     * the same as for <code>fromUTF8(String)</code> method.
     *
     * @param strings   array of byte arrays to decode.
     *
     * @return resulting array of strings.
     */
    public static String[] fromUTF8(byte[][] strings)
    {
        if (strings == null) return null;

        String[] strs = new String[strings.length];
        for (int i = 0; i < strings.length; i++)
        {
            byte[] string = strings[i];
            strs[i] = fromUTF8(string);
        }

        return strs;
    }

    /**
     * Converts string into array of bytes in UTF-8 encoding. If UTF-8 encoding isn't supported
     * then the tring is converted into bytes in default encoding and record is put in log
     * with severe priority.
     *
     * @param string    string to convert.
     *
     * @return resulting array of bytes.
     */
    public static byte[] toUTF8(String string)
    {
        if (string == null) return null;

        byte[] result;

        try
        {
            result = string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e)
        {
            LOG.severe(Strings.error("utf8.not.supported"));
            result = string.getBytes();
        }

        return result;
    }

    /**
     * Converts multi-line text into the array of strings.
     *
     * @param text      text.
     *
     * @return array of strings.
     */
    public static String[] multilineToArray(String text)
    {
        return text == null ? null : split(text, "\n");
    }

    /**
     * Converts any value to multi-line text. Arrays of strings are converted that each string
     * appears on its own line.
     *
     * @param value arbitrary value.
     *
     * @return multi-line text.
     */
    public static String anyToMultiline(Object value)
    {
        String result = Constants.EMPTY_STRING;

        if (value instanceof String[])
        {
            result = arrayToMultiline((String[])value);
        } else if (value != null)
        {
            result = value.toString();
        }

        return result;
    }

    /**
     * Converts array of strings to multi-line text where each string appears on it's own line.
     *
     * @param aStrings  array of strings.
     *
     * @return multi-line.
     */
    public static String arrayToMultiline(String[] aStrings)
    {
        return aStrings == null ? null : join(aStrings, "\n");
    }

    /**
     * Converts text from source encoding into Unicode. If encoding isn't supported you will get
     * the original text.
     *
     * @param text              text.
     * @param sourceEncoding    source encoding.
     *
     * @return converted text.
     */
    public static String decodeForced(String text, String sourceEncoding)
    {
        if (text == null) return null;
        if (sourceEncoding == null || sourceEncoding.equals(Converters.getDefaultEncodingName()))
            return text;

        try
        {
            text = new String(text.getBytes("ISO8859-1"), sourceEncoding);
        } catch (UnsupportedEncodingException e)
        {
            // We don't cate about it.
        }

        return text;
    }

    /**
     * Returns the first line of article.
     *
     * @param aText text to scan.
     * 
     * @return first text line.
     */
    public static String getFirstSentense(String aText)
    {
        if (aText == null) return null;

        int size = aText.length();

        int start;
        int length;

        for (start = 0; start < size && Character.isWhitespace(aText.charAt(start)); start++);
        for (length = 0; start + length < size &&
            (!isSentenseTerminator(aText.charAt(start + length))); length++);

        return length > 0 ? aText.substring(start, start + length).trim() : Constants.EMPTY_STRING;
    }

    /**
     * Returns TRUE if char is a sentense terminator.
     *
     * @param ch    char to test.
     *
     * @return TRUE if char is a sentense terminator.
     */
    public static boolean isSentenseTerminator(char ch)
    {
        return ch == '.' || ch == '?' || ch == '!';
    }

    /**
     * Returns the stringified size.
     *
     * @param size size in bytes.
     *
     * @return string representation.
     */
    public static String sizeToString(double size)
    {
        return sizeToString(size, 0);
    }

    /**
     * Returns the stringified size.
     *
     * @param size      size in units.
     * @param unitIndex unit index.
     *
     * @return string represenation.
     */
    private static String sizeToString(double size, int unitIndex)
    {
        String value;

        if (size < 512 || unitIndex == SIZE_UNIT.length - 1)
        {
            value = FORMAT.format(size) + " "  + SIZE_UNIT[unitIndex];
        } else {
            value = sizeToString(size / 1024, unitIndex + 1);
        }

        return value;
    }

    /**
     * Encodes string to be put in URL.
     *
     * <p>Example:</p>
     * <pre>
     *  input string: 'a &?b'
     *  output string: 'a+%26%3Fb'
     * </pre>
     *
     * @param str string to encode.
     *
     * @return encoded string or NULL if source was NULL.
     */
    public static String encodeForURL(String str)
    {
        if (str == null) return null;

        try
        {
            str = URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(Strings.error("utf8.not.supported"), e);
        }

        return str;
    }

    /**
     * Converts list of keywords separated by whitespace. Each keyword can actually
     * contain several words if enclosed in double quotes.
     *
     * @param keywords keywords string.
     *
     * @return list of keywords or <code>NULL</code> if <code>keywords</code>
     *         were <code>NULL</code>.
     */
    public static String[] keywordsToArray(String keywords)
    {
        List<String> matches = keywordsToList(keywords);
        return matches == null ? null : matches.toArray(new String[matches.size()]);
    }

    /**
     * Converts list of keywords separated by whitespace. Each keyword can actually
     * contain several words if enclosed in double quotes.
     *
     * @param keywords keywords string.
     *
     * @return list of keywords or <code>NULL</code> if <code>keywords</code>
     *         were <code>NULL</code>.
     */
    public static List<String> keywordsToList(String keywords)
    {
        if (keywords == null) return null;

        Matcher mat = PATTERN_KEYWORDS.matcher(keywords);

        List<String> matches = new ArrayList<String>();
        while (mat.find())
        {
            String keyword = mat.group(3);
            if (keyword == null) keyword = mat.group(2);
            if (keyword != null &&
                !"*".equals(keyword) &&
                !"+".equals(keyword) &&
                !matches.contains(keyword)) matches.add(keyword);
        }

        return matches;
    }

    /**
     * Places multi-word keyword in quotes only if it isn't in quotes already.
     *
     * @param keyword param to quote if necessary.
     *
     * @return updated keyword.
     */
    public static String quoteKeywordIfNecessary(String keyword)
    {
        keyword = keyword.trim();
        if (keyword.indexOf(' ') != -1 &&
            keyword.charAt(0) != '"' && keyword.charAt(keyword.length() - 1) != '"')
        {
            keyword = "\"" + keyword + "\"";
        }

        return keyword;
    }

    /**
     * Converts keywords from <code>a|b|c d|e</code> or <code>a, b, c d, e</code>
     * looks to <code>a b "c d" e</code>.
     *
     * @param aKeywords keywords to convert.
     *
     * @return new-look keywords.
     */
    public static String convertKeywordsToNewFormat(String aKeywords)
    {
        String result;

        if (aKeywords.indexOf('|') != -1)
        {
            result = breakAndRejoinKeywords(aKeywords, "|");
        } else if (aKeywords.indexOf(',') != -1)
        {
            result = breakAndRejoinKeywords(aKeywords, ",");
        } else result = aKeywords;

        return result;
    }

    /**
     * Breaks current keywords list appart and rejoins it using curren keywords
     * rules.
     *
     * @param aKeywords         list of keywords.
     * @param currentSeparator  the separator to be used for breaking.
     *
     * @return newly formed keywords list.
     */
    private static String breakAndRejoinKeywords(String aKeywords, String currentSeparator)
    {
        String[] keywordsList = split(aKeywords, currentSeparator);

        return arrayToQuotedKeywords(keywordsList);
    }

    /**
     * Converts the array of keywords into the space-delimited list with quoted multi-word
     * items.
     *
     * @param aKeywordsList list.
     *
     * @return space-delimete and quoted list of keywords.
     */
    public static String arrayToQuotedKeywords(String[] aKeywordsList)
    {
        String result = null;

        if (aKeywordsList != null)
        {
            if (aKeywordsList.length > 0)
            {
                StringBuffer buf = new StringBuffer();
                buf.append(quoteKeywordIfNecessary(aKeywordsList[0]));
                for (int i = 1; i < aKeywordsList.length; i++)
                {
                    buf.append(" ").append(quoteKeywordIfNecessary(aKeywordsList[i]));
                }
                result = buf.toString();
            } else result = "";
        }

        return result;
    }

    /**
     * Digests the buffer with key using MD5 algorithm.
     *
     * @param buffer    buffer.
     * @param key       key is secret key (password or something else which isn't
     *                  going to be passed over network).
     *
     * @return digested buffer.
     *
     * @throws NoSuchAlgorithmException if there's no MD5 algorithm implemetation.
     */
    public static byte[] digestMD5(String buffer, String key)
        throws NoSuchAlgorithmException
    {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(buffer.getBytes());
        return md5.digest(key.getBytes());
    }

    /**
     * Creates basic authentication token given user name and password.
     *
     * @param user      user name.
     * @param password  password.
     *
     * @return token.
     */
    public static String createBasicAuthToken(String user, String password)
    {
        String token = user + ":" + password;
        String base64Token = new BASE64Encoder().encode(token.getBytes());

        return "Basic " + base64Token;
    }

    /**
     * Creates pattern from the keywords list.
     *
     * @param keywords keywords list.
     *
     * @return keywords regex pattern.
     */
    public static String keywordsToPattern(String keywords)
    {
        return keywordsToPattern(keywordsToArray(keywords));
    }

    /**
     * Creates pattern from the keywords list.
     *
     * @param aKeywords keywords list.
     *
     * @return keywords regex pattern.
     */
    public static String keywordsToPattern(String[] aKeywords)
    {
        String pattern;

        if (aKeywords != null && aKeywords.length > 0)
        {
            pattern = join(aKeywords, "|");
            pattern = pattern.replaceAll("\\\\", "\\\\\\\\");
            pattern = pattern.replaceAll("\\.", "\\\\.");
            pattern = pattern.replaceAll("\\n+", "|");
            pattern = pattern.replaceAll("\\?", "\\\\?");
            pattern = pattern.replaceAll("\\(", "\\\\(");
            pattern = pattern.replaceAll("\\)", "\\\\)");
            pattern = pattern.replaceAll("\\[", "\\\\[");
            pattern = pattern.replaceAll("\\]", "\\\\]");

            pattern = pattern.replaceAll("\\++", "\\\\w+");
            pattern = pattern.replaceAll("\\s+", "\\\\s+");
            pattern = pattern.replaceAll("\\*+", "\\\\w*");
            pattern = pattern.replaceAll("\\\\s\\+\\\\w\\*\\\\s\\+", "\\\\s+(\\\\w*\\\\s+)?");

            pattern = pattern.replaceAll("\\|\\|+", "\\|");
            pattern = pattern.replaceAll("(^\\||\\|$)", "");

            String start = "\\W";
            String end = start;

            if (pattern.startsWith("\\s+"))
            {
                start = "\\s";
                pattern = pattern.substring(3);
            }

            if (pattern.endsWith("\\s+"))
            {
                end = "\\s";
                pattern = pattern.substring(0, pattern.length() - 3);
            }

            pattern = "(^|" + start + ")(" + pattern.trim() + ")($|" + end + ")";
        } else pattern = null;

        return pattern;
    }

    /**
     * Performs different cleanups of URL. Removes extra spaces, converts "feed://" into "http://",
     * takes only first line of draggeed URL which is actual link under FireFox 1.5 (Win).
     *
     * @param link  link being dragged into application.
     *
     * @return final link.
     */
    public static String cleanDraggedURL(String link)
    {
        if (link == null) return null;

        link = link.trim();

        // If URL starts with feed:// we change it to http://
        if (link.startsWith("feed:")) link = "http:" + link.substring(5);

        // FireFox 1.5 under Win has two lines: URL and description taken from page
        // We leave only the first line -- the URL
        int index = link.indexOf(0x0a);
        if (index != -1) link = link.substring(0, index).trim();

        return link;
    }

    /**
     * Scans for tags definitions in micro-format. The text should contain A-links to
     * tag categories with "rel" attribute equal to "tag". The last section of URL is
     * taken as tag.
     *
     * @param aText text to parse.
     *
     * @return list of tags detected.
     */
    public static String[] collectTags(String aText)
    {
        List<String> tagsList = null;

        if (aText != null)
        {
            Pattern pat = Pattern.compile("<a\\s+[^>]*rel\\s*=\\s*['\"]tag['\"][^>]*>",
                Pattern.CASE_INSENSITIVE);
            Matcher matcher = pat.matcher(aText);

            Pattern patTag = null;

            while (matcher.find())
            {
                if (tagsList == null)
                {
                    tagsList = new ArrayList<String>();
                    patTag = Pattern.compile(
                        "href\\s*=\\s*['\"]([^'\"/]+/+)+([\\+a-zA-Z0-9]+)['\"]");
                }

                Matcher m2 = patTag.matcher(matcher.group());

                if (m2.find()) tagsList.add(m2.group(2).replaceAll("\\+", " "));
            }
        }

        return tagsList == null
            ? Constants.EMPTY_STRING_LIST
            : tagsList.toArray(new String[tagsList.size()]);
    }

    /**
     * Adds protocol part to URL (http://) if none is specified and removes spaces around text.
     *
     * @param url source URL.
     *
     * @return modified URL.
     */
    public static String fixURL(String url)
    {
        if (url != null)
        {
            url = url.trim();
            if (url.length() == 0)
            {
                url = null;
            } else if (url.startsWith("feed:"))
            {
                url = url.substring(5).replaceAll("^/+", "");
                url = fixURL(url);
            } else if (!PATTERN_URL_WITH_PROTOCOL.matcher(url).find())
            {
                url = "http://" + url;
            }
        }

        return url;
    }

    /**
     * Unescapes the string.
     *
     * @param str string.
     *
     * @return unescaped version.
     */
    public static String quickUnescape(String str)
    {
        if (str == null) return null;

        str = str.replaceAll("&amp;", "&");
        str = str.replaceAll("&lt;", "<");
        str = str.replaceAll("&gt;", ">");
        str = str.replaceAll("&apos;", "'");
        str = str.replaceAll("&quot;", "\"");

        return str;
    }

    /**
     * Complete recoding of all HTML entities into Unicode symbols.
     *
     * @param str string.
     *
     * @return result.
     */
    public static String unescape(String str)
    {
        if (isEmpty(str)) return str;

        Pattern p = Pattern.compile("&(([^#;\\s]{3,6})|#([0-9]{1,4})|#x([0-9a-fA-F]{1,4}));");
        Matcher m = p.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (m.find())
        {
            Character c;

            String strEntity = m.group(2);
            String decEntity = m.group(3);
            String hexEntity = m.group(4);
            if (strEntity != null)
            {
                // String entity
                c = ENTITIES.get(strEntity);
            } else
            {
                c = decEntity != null ? (char)Integer.parseInt(decEntity) : (char)Integer.parseInt(hexEntity, 16);
            }

            m.appendReplacement(sb, c == null ? m.group() : c.toString());
        }
        m.appendTail(sb);

        return sb.toString();
    }

    /**
     * Checks whether the given strings is a valid e-mail address.
     *
     * @param email address.
     *
     * @return <code>TRUE</code> if valid.
     */
    public static boolean isValidEmail(String email)
    {
        return !isEmpty(email) && email.trim().matches("^[^@]+@[^\\.]+(\\.[^\\.]+)+$");
    }

    /**
     * Converts the list of URLs in string form into the array of URL objects.
     *
     * @param str   URLs string.
     *
     * @return array of URLs.
     */
    public static URL[] strToURLs(String str)
    {
        URL[] newURLs = null;

        if (isNotEmpty(str))
        {
            String[] urls = split(str, Constants.URL_SEPARATOR);
            List<URL> urlsList = new ArrayList<URL>(urls.length);
            for (String url : urls)
            {
                try
                {
                    urlsList.add(new URL(url));
                } catch (MalformedURLException e)
                {
                    // Wrong url specified -- skipping
                }
            }

            newURLs = urlsList.toArray(new URL[urlsList.size()]);
        }
        
        return newURLs;
    }

    /**
     * Returns space-separated list of first <code>N</code> words.
     *
     * @param str   string.
     * @param n     number of words.
     *
     * @return list or <code>NULL</code> if string is <code>NULL</code> or <code>N</code> is less than <code>1</code>.
     */
    public static String getUpToNWords(String str, int n)
    {
        if (str == null || n < 1) return null;

        String[] split = split(str, " ,.()[]<>!?\"':;/\\", n + 1);
        if (split.length > 1) split[split.length - 1] = "";
        
        return join(split, " ").trim();
    }

    private static Pattern lastPattern;
    private static int lastSentences = -1;

    /**
     * Returns the excerpt consisting of given number of sentences unless they are shorter or longer than given
     * limits. In this case minimum or maximum allowed number of characters plus "..." are returned.
     *
     * @param str       string to process.
     * @param sentences the number of sentences.
     * @param min       minimum characters.
     * @param max       maximum characters.
     *
     * @return the result.
     */
    public static String excerpt(String str, int sentences, int min, int max)
    {
        if (str == null) return str;

        String res;

        // Create pattern or reuse
        Pattern pat;
        if (lastSentences != sentences)
        {
            String patS = "^([^\\.!?]+(\\.+|!+|\\?+)+){" + sentences + "}";
            pat = Pattern.compile(patS);

            lastSentences = sentences;
            lastPattern = pat;
        } else pat = lastPattern;

        // Match the string
        Matcher m = pat.matcher(str);
        if (m.find())
        {
            res = m.group().trim();
            int len = res.length();
            if (len < min) res = excerpt(str, min); else
            if (len > max) res = excerpt(str, max);
        } else res = excerpt(str, max);

        return res;
    }

    /**
     * Returns the excerpt with given number of characters plus "..." unless
     * the string is shorter than limit.
     *
     * @param str   string to process.
     * @param len   number of characters.
     *
     * @return the result.
     */
    public static String excerpt(String str, int len)
    {
        return str == null || str.length() <= len ? str : str.substring(0, len) + "...";
    }

    /**
     * Takes the string, removes punctuation, lowercases it and returns the words in a given range glued
     * together with spaces. If there's not enough words, the maximum available number of them is returned.
     *
     * @param str   string.
     * @param from  the first word to return.
     * @param to    the last word to return.
     *
     * @return the result.
     */
    public static String getWordsInRange(String str, int from, int to)
    {
        if (isEmpty(str)) return str;
        if (from > to) throw new IllegalArgumentException("From can't be bigger than To.");

        // Remove all punctuation and collapse spaces plus lowercase
        str = PATTERN_PUNCTUATION.matcher(str).replaceAll(" ").toLowerCase().trim();
        String[] strs = str.split(" ");

        // Figure out what are our limits
        from = Math.min(from, strs.length);
        to = Math.min(to + 1, strs.length);

        // Glue words back together
        String[] arr = new String[to - from];
        System.arraycopy(strs, from,arr, 0, to - from);

        return join(arr, " ");
    }

    /**
     * Safely intern's a string.
     *
     * @param s string.
     *
     * @return intern'ed version.
     */
    public static String intern(String s)
    {
        return s == null ? null : s.intern();
    }


    /**
     * Finds all http://* links.
     *
     * @param text text.
     *
     * @return links list.
     */
    public static List<String> collectLinks(String text)
    {
        ArrayList<String> links = new ArrayList<String>();

        Pattern pattern = Pattern.compile("(https?://[^\\s,]*)(\\s|,|$)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find())
        {
            String link = matcher.group(1);

            // Remove trailing dots and commas
            link = link.replaceAll("[,.]+$", "");

            links.add(link);
        }

        return links;
    }
}
