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
// $Id: Template.java,v 1.11 2008/04/15 10:29:11 spyromus Exp $
//

package com.salas.bb.remixfeeds.templates;

import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.StandardArticle;
import com.salas.bb.utils.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

/**
 * A template.
 */
public class Template
{
    private static final Pattern PATTERN_IF                 = Pattern.compile("^\\s*#\\s+if\\s+single(\\s+article)?\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_ELSE               = Pattern.compile("^\\s*#\\s+else\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_ENDIF              = Pattern.compile("^\\s*#\\s+endif\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_FOR_EACH           = Pattern.compile("^\\s*#\\s+for\\s+each\\s+article\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_ENDFOR             = Pattern.compile("^\\s*#\\s+endfor\\s*$", Pattern.CASE_INSENSITIVE);

    private static final String PATTERN_FEED_TITLE          = "${feed.title}";
    private static final String PATTERN_FEED_URL            = "${feed.url}";
    private static final String PATTERN_ARTICLE_TITLE       = "${article.title}";
    private static final String PATTERN_ARTICLE_URL         = "${article.url}";
    private static final String PATTERN_ARTICLE_TEXT        = "${article.text}";
    private static final String PATTERN_ARTICLE_BRIEF_TEXT  = "${article.brief-text}";
    private static final String PATTERN_ARTICLE_DATE        = "${article.date}";

    /** Template name. */
    private String name;

    /** System or user-defined template. */
    private boolean system;

    /** Template text. */
    private String text;

    /**
     * Creates an empty template.
     */
    Template()
    {
    }

    /**
     * Creates a template.
     *
     * @param name      name.
     * @param system    system flag.
     * @param text      string text.
     */
    public Template(String name, boolean system, String text)
    {
        this.name = name;
        this.system = system;
        this.text = text;
    }

    /**
     * Returns the name.
     *
     * @return name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the name of the template.
     *
     * @param name name.
     */
    private void setName(String name)
    {
        this.name = name;
    }

    /**
     * Sets the template text.
     *
     * @param text text.
     *
     * @throws InvalidSyntaxException if template syntax is incorrect.
     */
    public void setText(String text)
        throws InvalidSyntaxException
    {
        List<SyntaxError> errors = SyntaxChecker.validate(text);
        if (!errors.isEmpty()) throw new InvalidSyntaxException(errors);
        
        this.text = text;
    }

    /**
     * Returns current template text.
     *
     * @return text.
     */
    public String getText()
    {
        return text;
    }

    /**
     * Renders a single article.
     *
     * @param article       article.
     * @param selectedText  selected text or NULL.
     *
     * @return HTML.
     */
    public String render(IArticle article, String selectedText)
    {
        // Wrap article to override the text
        if (StringUtils.isNotEmpty(selectedText)) article = new ArticleWrapper(article, selectedText);

        ArrayList<IArticle> list = new ArrayList<IArticle>(1);
        list.add(article);
        return render(list);
    }

    /**
     * Renders a template.
     *
     * @param articles articles.
     *
     * @return HTML.
     */
    public String render(Collection<IArticle> articles)
    {
        String result = "";

        boolean single = articles.size() == 1;
        if (StringUtils.isNotEmpty(text))
        {
            try
            {
                BufferedReader reader = new BufferedReader(new StringReader(text));
                result = render(reader, articles, single);
            } catch (IOException e)
            {
                // Never happens as we deal with a string
            }
        }

        return result;
    }

    /**
     * Analyzes the line and renders it if it's a text line or starts the sequence, if it's
     * an operation.
     *
     * @param reader    reader.
     * @param articles  articles.
     * @param single    TRUE if it's a single-article template.
     *
     * @return result.
     *
     * @throws IOException if fails.
     */
    private String render(BufferedReader reader, Collection<IArticle> articles, boolean single)
        throws IOException
    {
        String line;
        String result = "";
        boolean skip = false;

        while ((line = reader.readLine()) != null)
        {
            if (line.matches("\\s*#.+"))
            {
                // Operation
                if (!skip && PATTERN_IF.matcher(line).matches())
                {
                    result += renderIf(reader, articles, single);
                } else if (!skip && PATTERN_FOR_EACH.matcher(line).matches())
                {
                    result += renderForEach(reader, articles);
                } else if (PATTERN_ENDIF.matcher(line).matches() ||
                           PATTERN_ENDFOR.matcher(line).matches())
                {
                    // Break the loop if we encounter the end of the section of any kind
                    break;
                } else if (PATTERN_ELSE.matcher(line).matches())
                {
                    // If else block is reached, we need to skip it
                    skip = true;
                }
            } else if (!skip)
            {
                result += applyPatterns(line, articles.iterator().next());
            }
        }

        return result;
    }

    private String renderForEach(BufferedReader reader, Collection<IArticle> articles)
        throws IOException
    {
        String result = "";
        List<String> lines = new LinkedList<String>();

        // Fetch in all lines
        String line;
        while ((line = reader.readLine()) != null)
        {
            if (PATTERN_ENDFOR.matcher(line).matches())
            {
                break;
            } else
            {
                lines.add(line);
            }
        }

        // For each article, output lines
        for (IArticle article : articles)
        {
            for (String l : lines)
            {
                result += applyPatterns(l, article);
            }
        }

        return result;
    }

    private String renderIf(BufferedReader reader, Collection<IArticle> articles, boolean single)
        throws IOException
    {
        String results = "";

        String line;
        if (single)
        {
            // Single article is available
            results += render(reader, articles, single);
        } else
        {
            // Multi-article mode
            while ((line = reader.readLine()) != null)
            {
                if (PATTERN_ELSE.matcher(line).matches())
                {
                    // Start processing
                    results += render(reader, articles, single);
                    break;
                } else if (PATTERN_ENDIF.matcher(line).matches())
                {
                    break;
                }
            }
        }

        return results;
    }

    /**
     * Applies patterns to the line and returns the result.
     *
     * @param line      line.
     * @param article   article.
     *
     * @return result.
     */
    private String applyPatterns(String line, IArticle article)
    {
        if (StringUtils.isEmpty(line)) return line + "\n";

        IFeed feed = article.getFeed();
        if (feed != null)
        {
            line = line.replace(PATTERN_FEED_TITLE, feed.getTitle());
            if (feed instanceof DirectFeed)
            {
                DirectFeed dFeed = (DirectFeed)feed;
                line = line.replace(PATTERN_FEED_URL, toString(dFeed.getXmlURL()));
            }
        }

        line = line.replace(PATTERN_ARTICLE_TITLE, article.getTitle());
        line = line.replace(PATTERN_ARTICLE_DATE, toString(article.getPublicationDate()));
        line = line.replace(PATTERN_ARTICLE_TEXT, article.getHtmlText());
        line = line.replace(PATTERN_ARTICLE_BRIEF_TEXT, article.getBriefText());
        line = line.replace(PATTERN_ARTICLE_URL, toString(article.getLink()));

        return line + "\n";
    }

    /**
     * Safely converts a date into string.
     *
     * @param date date.
     *
     * @return string.
     */
    private String toString(Date date)
    {
        return date == null ? "" : DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
    }

    /**
     * Safely converts an URL into string.
     *
     * @param url URL.
     *
     * @return string.
     */
    private String toString(URL url)
    {
        return url == null ? "" : url.toString();
    }

    /**
     * Returns string representation.
     *
     * @return string.
     */
    public String toString()
    {
        return name;
    }

    /**
     * Returns TRUE if template is system-template.
     *
     * @return TRUE if template is system-template. 
     */
    public boolean isSystem()
    {
        return system;
    }

    /**
     * Stores a template in the preferences.
     *
     * @param prefs preferences.
     * @param seq   sequence number.
     */
    public void store(Preferences prefs, int seq)
    {
        String n = Integer.toString(seq);

        prefs.put("ptb.template." + n + ".name", getName());
        prefs.put("ptb.template." + n + ".text", getText());
    }

    /**
     * Restores a template from the preferences.
     *
     * @param prefs preferences.
     * @param seq   sequence number.
     */
    public void restore(Preferences prefs, int seq)
    {
        String n = Integer.toString(seq);

        setName(prefs.get("ptb.template." + n + ".name", null));
        setText(prefs.get("ptb.template." + n + ".text", ""));
    }

    /**
     * Article wrapper that is used for article text overriding.
     */
    private static class ArticleWrapper extends StandardArticle
    {
        private IArticle article;

        private ArticleWrapper(IArticle article, String selectedText)
        {
            super(selectedText);
            this.article = article;
        }

        @Override
        public String getHtmlText()
        {
            return getText();
        }

        @Override
        public synchronized String getPlainText()
        {
            return getText();
        }

        @Override
        public String getAuthor()
        {
            return article.getAuthor();
        }

        @Override
        public Date getPublicationDate()
        {
            return article.getPublicationDate();
        }

        @Override
        public synchronized String getTitle()
        {
            return article.getTitle();
        }

        @Override
        public URL getLink()
        {
            return article.getLink();
        }

        @Override
        public IFeed getFeed()
        {
            return article.getFeed();
        }

        @Override
        public String getBriefText()
        {
            return getText();
        }
    }
}
