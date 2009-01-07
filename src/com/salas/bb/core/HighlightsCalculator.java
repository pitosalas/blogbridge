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
// $Id: HighlightsCalculator.java,v 1.38 2007/11/07 17:16:48 spyromus Exp $
//

package com.salas.bb.core;

import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.utils.TextRange;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.concurrency.CachingCalculator;
import com.salas.bb.utils.swinghtml.TextProcessor;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Calculator of highlighted areas for articles. Responsible for providing always actual
 * information about what should be highlighted in articles.
 */
public class HighlightsCalculator
{

    private Map rangesCache = Collections.synchronizedMap(new WeakHashMap());
    private Map countsCache = Collections.synchronizedMap(new WeakHashMap());

    private String currentKeywords;

    private Pattern pattern = null;
    private final Object patternLock = new Object();

    private final Calculator calculator;

    /**
     * Creates highlights calculator.
     */
    public HighlightsCalculator()
    {
        calculator = new Calculator(1);
    }

    /**
     * Returns list of highlighted ranges in article.
     *
     * @param text text of article.
     *
     * @return list of regions.
     */
    public TextRange[] getHighlights(String text)
    {
        if (text == null) return Constants.EMPTY_TEXT_RANGE_LIST;

        // TODO !!!! read/write locking necessary !!!!

        CustomCacheKey key = CustomCacheKey.key(text);
        TextRange[] tranges = (TextRange[])rangesCache.get(key);

        if (tranges == null)
        {
            tranges = TextRange.findRanges(text, getKeywordsPattern());
            if (tranges != null) rangesCache.put(key, tranges);
        }

        return tranges;
    }

    /**
     * Returns number of highlights in text. Uses simplified scheme.
     *
     * @param text  text to process.
     *
     * @return number of highlights.
     */
    public int getHighlightsCount(String text)
    {
        if (text == null) return 0;
        
        int cnt = 0;

        String plainText = TextProcessor.toPlainText(text);
        Integer count = getCountFromCache(plainText);

        if (count == null)
        {
            Pattern pat = getKeywordsPattern();

            if (pat != null)
            {
                Matcher mat = pat.matcher(plainText);
                int st = 0;
                while (mat.find(st))
                {
                    cnt++;
                    st = mat.end(2);
                }
            }

            countsCache.put(CustomCacheKey.key(plainText), cnt);
        } else
        {
            cnt = count.intValue();
        }

        return cnt;
    }

    private Integer getCountFromCache(String text)
    {
        return (Integer)countsCache.get(CustomCacheKey.key(text));
    }

    /**
     * Returns regex pattern with keywords.
     *
     * @return pattern.
     */
    private Pattern getKeywordsPattern()
    {
        Pattern pat;

        synchronized (patternLock)
        {
            pat = pattern;
        }

        return pat;
    }

    /**
     * Called to notify that keywords has changed.
     *
     * @param newKeywords new keywords.
     */
    public void keywordsChanged(String newKeywords)
    {
        boolean invalidate = false;

        synchronized (patternLock)
        {
            if ((currentKeywords == null && newKeywords != null) ||
                (currentKeywords != null && !currentKeywords.equals(newKeywords)))
            {
                currentKeywords = newKeywords;

                String patternRegex = StringUtils.keywordsToPattern(currentKeywords);
                pattern = patternRegex == null ? null
                    : Pattern.compile(patternRegex, Pattern.CASE_INSENSITIVE);

                invalidate = true;
            }
        }

        if (invalidate) invalidateAll();
    }

    /**
     * Returns number of highlighted regions in articles of this feed.
     *
     * @param feed feed to check.
     *
     * @return number of highlights.
     */
    public int getHighlightsCount(IFeed feed)
    {
        return ((CompositeValue)calculator.getValue(feed)).highlights;
    }

    /**
     * Returns number of highlighted articles.
     *
     * @param feed   feed to examine.
     *
     * @return number of articles with highlights.
     */
    public int getHighlightedArticles(IFeed feed)
    {
        return ((CompositeValue)calculator.getValue(feed)).articlesWithHighlights;
    }

    /**
     * Invalidates all records stored in cache.
     */
    public void invalidateAll()
    {
        synchronized (this)
        {
            rangesCache.clear();
            countsCache.clear();
        }

        calculator.invalidateAll();
    }

    /**
     * Invalidates the feed stored in cache.
     *
     * @param feed feed to invalidate.
     */
    public void invalidateFeed(IFeed feed)
    {
        calculator.invalidateKey(feed);
    }

    /**
     * Removes the feed from cache.
     *
     * @param feed feed.
     */
    public void feedRemoved(IFeed feed)
    {
        calculator.removeKey(feed);
    }

    /**
     * Returns number of highlights for given article.
     *
     * @param aArticle article.
     *
     * @return number of highlights.
     */
    public int getHighlightsCount(IArticle aArticle)
    {
        return getHighlightsCount(aArticle.getPlainText());
    }

    /**
     * Simple composite value holder.
     */
    private static class CompositeValue
    {
        private volatile int highlights;
        private volatile int articlesWithHighlights;
    }

    private static class CustomCacheKey
    {
        private volatile int length;
        private volatile int hash;

        private CustomCacheKey(int length, int hash)
        {
            this.length = length;
            this.hash = hash;
        }

        public static CustomCacheKey key(String s)
        {
            return s == null ? new CustomCacheKey(0, 0) : new CustomCacheKey(s.length(), s.hashCode());
        }

        @Override
        public int hashCode()
        {
            return hash;
        }

        @Override
        public boolean equals(Object o)
        {
            boolean res = false;
            if (o instanceof CustomCacheKey)
            {
                CustomCacheKey other = (CustomCacheKey)o;
                res = length == other.length && hash == other.hash;
            }
            return res;
        }
    }

    /**
     * Calculator of highligts counts.
     */
    private class Calculator extends CachingCalculator
    {
        public Calculator(int threads)
        {
            super(threads);
            startThreads();
        }

        protected String getThreadsBaseName()
        {
            return "HC";
        }

        protected Object calculate(Object key)
        {
            final IFeed feed = (IFeed)key;

            CompositeValue value = new CompositeValue();
            int highlights = 0;
            int articlesWithHighlights = 0;

            IArticle[] articles = feed.getArticles();
            for (int i = 0; i < articles.length; i++)
            {
                IArticle article = articles[i];
                int highlightsInArticle = getHighlightsCount(article.getPlainText());

                highlights += highlightsInArticle;
                if (highlightsInArticle > 0) articlesWithHighlights++;
            }

            value.articlesWithHighlights = articlesWithHighlights;
            value.highlights = highlights;

            return value;
        }
    }
}
