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
// $Id: AbstractArticle.java,v 1.36 2008/03/17 17:16:17 spyromus Exp $
//

package com.salas.bb.domain;

import com.salas.bb.sentiments.Calculator;
import com.salas.bb.utils.CommonUtils;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.swinghtml.TextProcessor;

import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract article implementation.
 */
public abstract class AbstractArticle implements IArticle
{
    /** ID property. */
    public static final String PROP_ID = "id";

    /** Pattern for looking for HTML links. */
    private static final Pattern PAT_LINKS =
        Pattern.compile("<a [^>]*href\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>", Pattern.CASE_INSENSITIVE);

    private final List<IArticleListener> listeners;

    private long    id;
    private String  simpleMatchKey;
    private IFeed   feed;
    private boolean read;
    private boolean pinned;
    private String  title;
    private String  author;
    private String  subject;
    private Date    publicationDate;
    private URL     link;

    /** This flag is <code>TRUE</code> only once a life-cycle during the initial articleAdded even processing. */
    private boolean isnew;

    /** Brief mode settings. */

    public static final int DEFAULT_BRIEF_SENTENCES = 3;
    public static final int DEFAULT_BRIEF_MIN_LENGTH = 50;
    public static final int DEFAULT_BRIEF_MAX_LENGTH = 1000;

    private static int briefSentences = DEFAULT_BRIEF_SENTENCES;
    private static int briefMinLength = DEFAULT_BRIEF_MIN_LENGTH;
    private static int briefMaxLength = DEFAULT_BRIEF_MAX_LENGTH;

    /**
     * Links collected from the article text.
     */
    private Collection<String> links;

    /** Soft storage for plain text. */
    private SoftReference<String> softPlainText;

    /** The list of words the title is composed of. */
    private String[] titleWords;
    private final Object titleWordsLock = new Object();

    /** Positive sentiments count. */
    private int positiveSentimentsCount;
    /** Negative sentiments count. */
    private int negativeSentimentsCount;
    private boolean positive;
    private boolean negative;

    private final ReadWriteLock sentimentsLock = new ReentrantReadWriteLock();
    private final Lock readLock = sentimentsLock.readLock();
    private final Lock writeLock = sentimentsLock.writeLock();

    /**
     * Creates abstract article.
     */
    protected AbstractArticle()
    {
        listeners = new CopyOnWriteArrayList<IArticleListener>();

        id = -1;
        simpleMatchKey = null;

        pinned = false;
    }

    /**
     * Returns ID of the article in database. This ID is used by persistence layer.
     *
     * @return ID of the article in database.
     */
    public long getID()
    {
        return id;
    }

    /**
     * Sets ID of the article in database.
     *
     * @param aId ID of the article.
     */
    public void setID(long aId)
    {
        long oldId = id;
        id = aId;
        firePropertyChanged(PROP_ID, oldId, id);
    }

    /**
     * Adds listener to receive article's events.
     *
     * @param listener listener.
     *
     * @throws NullPointerException if listener isn't specified.
     */
    public void addListener(IArticleListener listener)
    {
        if (listener == null) throw new NullPointerException(Strings.error("unspecified.listener"));

        if (!listeners.contains(listener)) listeners.add(listener);
    }

    /**
     * Removes listener.
     *
     * @param listener listener.
     *
     * @throws NullPointerException if listener isn't specified.
     */
    public void removeListener(IArticleListener listener)
    {
        if (listener == null) throw new NullPointerException(Strings.error("unspecified.listener"));

        listeners.remove(listener);
    }

    /**
     * Fires <code>propertyChanged</code> event.
     *
     * @param property  property name.
     * @param oldValue  old value.
     * @param newValue  new value.
     *
     * @throws NullPointerException if property name isn't specified.
     */
    protected void firePropertyChanged(String property, Object oldValue, Object newValue)
    {
        if (property == null) throw new NullPointerException(Strings.error("unspecified.property"));

        if (!CommonUtils.areDifferent(oldValue, newValue)) return;

        for (IArticleListener listener : listeners)
        {
            listener.propertyChanged(this, property, oldValue, newValue);
        }
    }

    /**
     * Returns simple match key which is close to unique.
     *
     * @return simple match key.
     */
    public synchronized String getSimpleMatchKey()
    {
        if (simpleMatchKey == null) computeSimpleMatchKey();
        return simpleMatchKey;
    }

    /**
     * Sets new simple match key to be used for matching. This is usually done during the loading from
     * the database.
     *
     * @param key key.
     */
    public void setSimpleMatchKey(String key)
    {
        simpleMatchKey = StringUtils.intern(key);
    }

    /**
     * Resets the value of simple match key indicating that the key is no longer valid.
     */
    protected synchronized void resetSimpleMatchKey()
    {
        simpleMatchKey = null;
    }

    /**
     * Returns plain version of article text.
     *
     * @return plain version of text.
     */
    public synchronized String getPlainText()
    {
        String plainText = getPlainFromCache();

        if (plainText == null)
        {
            plainText = convertTextToPlain(getHtmlText());

            putPlainInCache(plainText);
        }

        return plainText;
    }

    /**
     * Returns the name of article author.
     *
     * @return author.
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * Sets the name of article author.
     *
     * @param aAuthor author.
     */
    public void setAuthor(String aAuthor)
    {
        author = StringUtils.intern(aAuthor);
    }

    /**
     * Returns the date of publication.
     *
     * @return publication date.
     */
    public Date getPublicationDate()
    {
        return publicationDate;
    }

    /**
     * Sets the date of article publication.
     *
     * @param aPublicationDate publication date.
     */
    public void setPublicationDate(Date aPublicationDate)
    {
        publicationDate = aPublicationDate;
    }

    /**
     * Returns TRUE if the article is read.
     *
     * @return TRUE if the article is read.
     */
    public boolean isRead()
    {
        return read;
    }

    /**
     * Sets the value of read flag for the article.
     *
     * @param aRead TRUE if the article is read.
     */
    public void setRead(boolean aRead)
    {
        if (aRead != read)
        {
            read = aRead;
            firePropertyChanged(PROP_READ, !read, read);
        }
    }

    /**
     * Returns the pin flag state.
     *
     * @return pin flag.
     */
    public boolean isPinned()
    {
        return pinned;
    }

    /**
     * Sets the pin flag state.
     *
     * @param pinned TRUE to pin.
     */
    public void setPinned(boolean pinned)
    {
        this.pinned = pinned;
        firePropertyChanged(PROP_PINNED, !pinned, pinned);
    }

    /**
     * Returns the subject of article.
     *
     * @return subject.
     */
    public String getSubject()
    {
        return subject;
    }

    /**
     * Sets the subject of article.
     *
     * @param aSubject new subject.
     */
    public void setSubject(String aSubject)
    {
        subject = StringUtils.intern(aSubject);
    }

    /**
     * Returns title of the article.
     *
     * @return title.
     */
    public synchronized String getTitle()
    {
        return title;
    }

    /**
     * Sets the title of the article.
     *
     * @param aTitle title of the article.
     */
    public synchronized void setTitle(String aTitle)
    {
        title = StringUtils.intern(aTitle);

        // Simple match key depends on the title -- reset the key when text changes.
        resetSimpleMatchKey();
    }

    /**
     * Returns the words of the title in the order of appearance.
     *
     * @return words of the title.
     */
    public String[] getTitleWords()
    {
        synchronized (titleWordsLock)
        {
            if (titleWords == null)
            {
                String[] wrds = StringUtils.split(title, "-+#$%^&_*,.()[]<>!?\"':;/\\ ");

                // Count words with 3 or more chars
                int s = 0;
                for (String wrd : wrds) if (wrd.length() > 2) s++;

                // Compose the resulting array
                titleWords = new String[s];
                s = 0;
                for (String wrd : wrds) if (wrd.length() > 2) titleWords[s++] = wrd.intern();
            }
        }

        return titleWords;
    }

    /**
     * Returns URL of associated article page.
     *
     * @return URL of article page.
     */
    public URL getLink()
    {
        return link;
    }

    /**
     * Sets the link to associated HTML page.
     *
     * @param aLink link to HTML page.
     */
    public void setLink(URL aLink)
    {
        // Recreate a link in a way that reuses strings
        link = CommonUtils.intern(aLink);
        resetSimpleMatchKey();
    }

    /**
     * Returns parent feed.
     *
     * @return parent feed.
     */
    public IFeed getFeed()
    {
        return feed;
    }

    /**
     * Sets the parent feed.
     *
     * @param aFeed parent feed.
     */
    public void setFeed(IFeed aFeed)
    {
        feed = aFeed;
    }

    /**
     * Returns all links (absolute and relative) found in the article text.
     *
     * @return links.
     */
    public synchronized Collection<String> getLinks()
    {
        if (links == null) links = collectLinks(getHtmlText());

        return links;
    }

   /**
    * Looks through the article body and records all found links (&lt;a href="link"&gt;).
    *
    * @param aText the text of the article.
    *
    * @return the list of links in the article.
    */
   static Collection<String> collectLinks(String aText)
   {
       Set<String> linksSet = new HashSet<String>();

       if (aText != null)
       {
           Matcher m = PAT_LINKS.matcher(aText);

           while (m.find()) linksSet.add(m.group(1).intern());
       }

       return linksSet;
   }

    /**
     * Returns hash code for the article.
     *
     * @return hash code.
     */
    public int hashCode()
    {
        return getSimpleMatchKey().hashCode();
    }

    /**
     * Compares this article to some different article. Uses simple match key for comparison.
     *
     * @param obj article object.
     *
     * @return TRUE if equal.
     */
    public boolean equals(Object obj)
    {
        IArticle article = (IArticle)obj;

        return getSimpleMatchKey().equals(article.getSimpleMatchKey());
    }

    // ---------------------------------------------------------------------------------------------
    // Brief Mode
    // ---------------------------------------------------------------------------------------------

    /**
     * Returns brief version of plain text.
     *
     * @return brief text.
     */
    public String getBriefText()
    {
        return StringUtils.excerpt(getPlainText(), briefSentences, briefMinLength, briefMaxLength);
    }

    /**
     * Returns the number of sentences in a brief mode.
     *
     * @return sentences.
     */
    public static int getBriefSentences()
    {
        return briefSentences;
    }

    /**
     * Sets new sentence limit for brief mode.
     *
     * @param limit limit.
     */
    public static void setBriefSentences(int limit)
    {
        briefSentences = limit;
    }

    /**
     * Returns the minimum number of characters to show in a brief mode (if available).
     *
     * @return minimum.
     */
    public static int getBriefMinLength()
    {
        return briefMinLength;
    }

    /**
     * Sets new minimum length for brief mode.
     *
     * @param min length.
     */
    public static void setBriefMinLength(int min)
    {
        briefMinLength = min;
    }

    /**
     * Returns the maximum number of characters to show in a brief mode (if available).
     *
     * @return max length.
     */
    public static int getBriefMaxLength()
    {
        return briefMaxLength;
    }

    /**
     * Sets new maximum length for brief mode.
     *
     * @param max length.
     */
    public static void setBriefMaxLength(int max)
    {
        briefMaxLength = max;
    }

    // ---------------------------------------------------------------------------------------------
    // Caching
    // ---------------------------------------------------------------------------------------------

    private synchronized void putPlainInCache(String plainText)
    {
        softPlainText = new SoftReference<String>(plainText);
    }

    private synchronized String getPlainFromCache()
    {
        return softPlainText == null ? null : softPlainText.get();
    }

    /**
     * Computes the key.
     */
    public void computeSimpleMatchKey()
    {
        setSimpleMatchKey(computeSimpleMatchKey(getLink(), getTitle()));
    }

    /**
     * Computes simple match key.
     *
     * @param anUrl     URL to use for computations.
     * @param aTitle    title of an article
     *
     * @return key value.
     */
    public static String computeSimpleMatchKey(URL anUrl, String aTitle)
    {
        long code = (anUrl == null) ? 0 : Math.abs(anUrl.toString().hashCode());
        code = code * 29L + (aTitle == null ? 0 : Math.abs(aTitle.hashCode()));

        return Long.toHexString(code);
    }

    /**
     * Converts text to plain version.
     *
     * @param text      text to convert.
     *
     * @return plain text version.
     */
    public static String convertTextToPlain(String text)
    {
        return text == null ? null : TextProcessor.processPlain(text, Constants.ARTICLE_SIZE_LIMIT);
    }

    /**
     * Returns <code>TRUE</code> if article has just been added.
     * This flag remains <code>TRUE</code> during the initial article
     * added even processing. Right after that it's no longer new.
     *
     * @return <code>TRUE</code> if it's the first time the article is added to a feed.
     */
    public boolean isNew()
    {
        return isnew;
    }

    /**
     * Sets the new state.
     *
     * @param n new state.
     */
    public void setNew(boolean n)
    {
        isnew = n;
    }
    /**
     * Recalculates sentiment counts.
     */
    public void recalculateSentimentCounts()
    {
        writeLock.lock();
        try
        {
            String text = getPlainText();
            setSentimentsCounts(Calculator.countPositiveOccurances(text), Calculator.countNegativeOccurances(text));
        } finally
        {
            writeLock.unlock();
        }
    }

    /**
     * Sets sentiments counts.
     *
     * @param positive positive.
     * @param negative negative.
     */
    public void setSentimentsCounts(int positive, int negative)
    {
        writeLock.lock();

        int oldPositive;
        int oldNegative;
        try
        {
            oldPositive = positiveSentimentsCount;
            oldNegative = negativeSentimentsCount;

            positiveSentimentsCount = positive;
            negativeSentimentsCount = negative;

            recalculateConnotation();

            readLock.lock();
        } finally
        {
            writeLock.unlock();
        }

        try
        {
            if (oldPositive != positive || oldNegative != negative)
            {
                firePropertyChanged(PROP_SENTIMENT_COUNTS, 0, 1);
            }
        } finally
        {
            readLock.unlock();
        }
    }

    /**
     * Returns positive sentiments count.
     *
     * @return count.
     */
    public int getPositiveSentimentsCount()
    {
        int cnt;

        readLock.lock();
        try { cnt = positiveSentimentsCount; } finally { readLock.unlock(); }

        return cnt;
    }

    /**
     * Returns negative sentiments count.
     *
     * @return count.
     */
    public int getNegativeSentimentsCount()
    {
        int cnt;

        readLock.lock();
        try { cnt = negativeSentimentsCount; } finally { readLock.unlock(); }

        return cnt;
    }

    /**
     * Returns TRUE if article is positive based on the sentiments count.
     *
     * @return TRUE if positive.
     */
    public boolean isPositive()
    {
        boolean is;

        readLock.lock();
        try { is = positive; } finally { readLock.unlock(); }

        return is;
    }

    /**
     * Returns TRUE if article is negative based on the sentiments count.
     *
     * @return TRUE if negative.
     */
    public boolean isNegative()
    {
        boolean is;

        readLock.lock();
        try { is = negative; } finally { readLock.unlock(); }

        return is;
    }

    /**
     * Recalculates connotation.
     */
    public void recalculateConnotation()
    {
        boolean oldPositive;
        boolean oldNegative;

        writeLock.lock();
        try
        {
            int vn = negativeSentimentsCount;
            int vp = positiveSentimentsCount;
            int tp = Calculator.getConfig().getPositiveThreshold();
            int tn = Calculator.getConfig().getNegativeThreshold();

            oldPositive = positive;
            oldNegative = negative;

//          positive = isDominating(vp, vn, tp);
//          negative = isDominating(vn, vp, tn);

            positive = negative = false;

            // alternate formula
            int excess_positives = vp - vn;

            if (excess_positives > 0 && excess_positives > tp)
                positive = true;
            else if (excess_positives < 0 && excess_positives < tn )
                negative = true;

            readLock.lock();
        } finally
        {
            writeLock.unlock();
        }

        try
        {
            firePropertyChanged(PROP_POSITIVE, oldPositive, positive);
            firePropertyChanged(PROP_NEGATIVE, oldNegative, negative);
        } finally
        {
            readLock.unlock();
        }
    }

    /**
     * Calculates the attitude of an article taking a threshold in account.
     *
     * @param v1    leading value of sentiments.
     * @param v2    following value of sentiments.
     * @param th    threshold.
     *
     * @return TRUE if the domination of v1 over v2 is over th percent.
     */
    private static boolean isDominating(int v1, int v2, int th)
    {
        if (v2 == 0) return v1 > 0;
        return ((v1 - v2) * 100 / v2) > th;
    }
}
