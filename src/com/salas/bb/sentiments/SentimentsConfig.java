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
// $Id: SentimentsConfig.java,v 1.7 2008/04/08 08:06:19 spyromus Exp $
//

package com.salas.bb.sentiments;

import com.salas.bb.service.sync.SyncIn;
import com.salas.bb.service.sync.SyncOut;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.UifUtilities;

import java.awt.*;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

/**
 * Configuration holder.
 */
public class SentimentsConfig
{
    /** Key for <code>keywords</code> preference. */
    public static final String KEY_KEYWORDS                     = "state.keywords";

    private static final String PREFIX                          = "sentiments.";

    public static final String PROP_ENABLED                     = "enabled";
    public static final String PROP_POSITIVE_EXPRESSIONS        = "positiveExpressions";
    public static final String PROP_NEGATIVE_EXPRESSIONS        = "negativeExpressions";
    public static final String PROP_POSITIVE_THRESHOLD          = "positiveThreshold";
    public static final String PROP_NEGATIVE_THRESHOLD          = "negativeThreshold";
    public static final String PROP_POSITIVE_COLOR              = "positiveColor";
    public static final String PROP_NEGATIVE_COLOR              = "negativeColor";

    private static final int MIN_THRESHOLD                      = -100;
    private static final int MAX_THRESHOLD                      = 100;

    private static final boolean DEFAULT_ENABLED                = false;
    public static final String DEFAULT_POSITIVE_EXPRESSIONS     = Strings.message("sentiments.default.positive.expressions");
    public static final String DEFAULT_NEGATIVE_EXPRESSIONS     = Strings.message("sentiments.default.negative.expressions");
    public static final int DEFAULT_POSITIVE_THRESHOLD          = Integer.parseInt(Strings.message("sentiments.default.positive.threshold"));
    public static final int DEFAULT_NEGATIVE_THRESHOLD          = Integer.parseInt(Strings.message("sentiments.default.negative.threshold"));
    public static final Color DEFAULT_POSITIVE_COLOR            = Color.decode(Strings.message("sentiments.default.positive.color"));
    public static final Color DEFAULT_NEGATIVE_COLOR            = Color.decode(Strings.message("sentiments.default.negative.color"));

    private boolean enabled;
    private String  positiveExpressions;
    private String  negativeExpressions;
    private Pattern positivePattern;
    private Pattern negativePattern;
    private int     positiveThreshold;
    private int     negativeThreshold;
    private Color   positiveColor;
    private Color   negativeColor;

    /**
     * Default initialization.
     */
    public SentimentsConfig()
    {
        setEnabled(DEFAULT_ENABLED);
        setPositiveExpressions(DEFAULT_POSITIVE_EXPRESSIONS);
        setNegativeExpressions(DEFAULT_NEGATIVE_EXPRESSIONS);
        setPositiveThreshold(DEFAULT_POSITIVE_THRESHOLD);
        setNegativeThreshold(DEFAULT_NEGATIVE_THRESHOLD);
    }

    /**
     * Returns TRUE if enabled.
     *
     * @return TRUE if enabled.
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Enables / disables the analysis.
     *
     * @param enabled TRUE to enable.
     */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;

        if (!enabled) ArticleFilterProtector.switchToSafeIfUnsafe();
    }

    /**
     * Returns the list of positive expressions.
     *
     * @return positive expressions.
     */
    public String getPositiveExpressions()
    {
        return positiveExpressions;
    }

    /**
     * Sets the list of positive expressions.
     *
     * @param expr positive expressions.
     */
    public void setPositiveExpressions(String expr)
    {
        String old = positiveExpressions;
        positiveExpressions = expr;

        if (!StringUtils.equals(old, expr))
        {
            positivePattern = compilePattern(expr);
        }
    }

    /**
     * Returns the positive expressions pattern.
     *
     * @return pattern or NULL when uninitialized.
     */
    public Pattern getPositivePattern()
    {
        return positivePattern;
    }

    /**
     * Returns negative expressions.
     *
     * @return expressions.
     */
    public String getNegativeExpressions()
    {
        return negativeExpressions;
    }

    /**
     * Sets negative expressions.
     *
     * @param expr expressions.
     */
    public void setNegativeExpressions(String expr)
    {
        String old = negativeExpressions;
        negativeExpressions = expr;

        if (!StringUtils.equals(old, expr))
        {
            negativePattern = compilePattern(expr);
        }
    }

    /**
     * Returns negative expressions pattern.
     *
     * @return pattern or NULL when uninitialized.
     */
    public Pattern getNegativePattern()
    {
        return negativePattern;
    }

    /**
     * Returns positive threshold in percents.
     *
     * @return threshold.
     */
    public int getPositiveThreshold()
    {
        return positiveThreshold;
    }

    /**
     * Sets positive threshold in percents.
     *
     * @param threshold threshold.
     */
    public void setPositiveThreshold(int threshold)
    {
        checkThreshold(threshold);
        positiveThreshold = threshold;
    }

    /**
     * Returns negative threshold in percents.
     *
     * @return negative threshold.
     */
    public int getNegativeThreshold()
    {
        return negativeThreshold;
    }

    /**
     * Sets negative threshold in percents.
     *
     * @param threshold threshold.
     */
    public void setNegativeThreshold(int threshold)
    {
        checkThreshold(threshold);
        this.negativeThreshold = threshold;
    }

    /**
     * Returns the color to use for positive coding.
     *
     * @return color.
     */
    public Color getPositiveColor()
    {
        return positiveColor;
    }

    /**
     * Sets the color to use for positive coding.
     *
     * @param color color.
     */
    public void setPositiveColor(Color color)
    {
        positiveColor = color;
    }

    /**
     * Returns the color to use for negative coding.
     *
     * @return color.
     */
    public Color getNegativeColor()
    {
        return negativeColor;
    }

    /**
     * Sets the color to use for negative coding.
     *
     * @param color color.
     */
    public void setNegativeColor(Color color)
    {
        negativeColor = color;
    }

    /**
     * Compiles an expression.
     *
     * @param expr expression.
     *
     * @return expression.
     */
    private static Pattern compilePattern(String expr)
    {
        return StringUtils.isEmpty(expr) ? null
            : Pattern.compile(StringUtils.keywordsToPattern(expr), Pattern.CASE_INSENSITIVE);
    }

    /**
     * Ensures the threshold is in the range 0-100.
     *
     * @param threshold threshold.
     *
     * @throws IllegalArgumentException if it's not.
     */
    private static void checkThreshold(int threshold)
    {
        if (threshold > MAX_THRESHOLD || threshold < MIN_THRESHOLD)
        {
            throw new IllegalArgumentException("Threshold percents should be in range -100 - 100.");
        }
    }

    /**
     * Restores preferences.
     *
     * @param prefs prefs.
     */
    public static void restorePreferences(Preferences prefs)
    {
        SentimentsConfig config = Calculator.getConfig();

        config.setEnabled(prefs.getBoolean(PREFIX + PROP_ENABLED, DEFAULT_ENABLED));
        config.setNegativeExpressions(prefs.get(PREFIX + PROP_NEGATIVE_EXPRESSIONS, DEFAULT_NEGATIVE_EXPRESSIONS));
        config.setPositiveExpressions(prefs.get(PREFIX + PROP_POSITIVE_EXPRESSIONS, DEFAULT_POSITIVE_EXPRESSIONS));
        config.setNegativeThreshold(prefs.getInt(PREFIX + PROP_NEGATIVE_THRESHOLD, DEFAULT_NEGATIVE_THRESHOLD));
        config.setPositiveThreshold(prefs.getInt(PREFIX + PROP_POSITIVE_THRESHOLD, DEFAULT_POSITIVE_THRESHOLD));
        config.setPositiveColor(prefToColor(prefs.get(PREFIX + PROP_POSITIVE_COLOR, UifUtilities.colorToHex(DEFAULT_POSITIVE_COLOR))));
        config.setNegativeColor(prefToColor(prefs.get(PREFIX + PROP_NEGATIVE_COLOR, UifUtilities.colorToHex(DEFAULT_NEGATIVE_COLOR))));

        // See if we have keywords legacy
        if (StringUtils.isEmpty(config.positiveExpressions))
        {
            String keywords = prefs.get(KEY_KEYWORDS, null);
            if (StringUtils.isNotEmpty(keywords))
            {
                String k = StringUtils.convertKeywordsToNewFormat(keywords);
                config.positiveExpressions = StringUtils.join(StringUtils.keywordsToArray(k), "\n");
            }
        }
    }

    /**
     * Converts a preference value to color.
     *
     * @param pref value.
     *
     * @return color.
     */
    private static Color prefToColor(String pref)
    {
        return StringUtils.isEmpty(pref) ? null : Color.decode(pref);
    }

    /**
     * Stores preferences.
     *
     * @param prefs prefs.
     */
    public static void storePreferences(Preferences prefs)
    {
        SentimentsConfig config = Calculator.getConfig();

        prefs.putBoolean(PREFIX + PROP_ENABLED,        config.isEnabled());
        prefs.put(PREFIX + PROP_NEGATIVE_EXPRESSIONS,  config.getNegativeExpressions());
        prefs.putInt(PREFIX + PROP_NEGATIVE_THRESHOLD, config.getNegativeThreshold());
        prefs.put(PREFIX + PROP_POSITIVE_EXPRESSIONS,  config.getPositiveExpressions());
        prefs.putInt(PREFIX + PROP_POSITIVE_THRESHOLD, config.getPositiveThreshold());
        prefs.put(PREFIX + PROP_POSITIVE_COLOR,        UifUtilities.colorToHex(config.positiveColor));
        prefs.put(PREFIX + PROP_NEGATIVE_COLOR,        UifUtilities.colorToHex(config.negativeColor));
    }

    /**
     * Synchronizes preferences in.
     *
     * @param prefs preferences.
     */
    public static void syncIn(Map prefs)
    {
        SentimentsConfig config = Calculator.getConfig();

        config.setEnabled(SyncIn.getBoolean(prefs, PREFIX + PROP_ENABLED, DEFAULT_ENABLED));
        config.setPositiveExpressions(SyncIn.getString(prefs, PREFIX + PROP_POSITIVE_EXPRESSIONS, DEFAULT_POSITIVE_EXPRESSIONS));
        config.setNegativeExpressions(SyncIn.getString(prefs, PREFIX + PROP_NEGATIVE_EXPRESSIONS, DEFAULT_NEGATIVE_EXPRESSIONS));
        config.setPositiveThreshold(SyncIn.getInt(prefs, PREFIX + PROP_POSITIVE_THRESHOLD, DEFAULT_POSITIVE_THRESHOLD));
        config.setNegativeThreshold(SyncIn.getInt(prefs, PREFIX + PROP_NEGATIVE_THRESHOLD, DEFAULT_NEGATIVE_THRESHOLD));
        config.setPositiveColor(prefToColor(SyncIn.getString(prefs, PREFIX + PROP_POSITIVE_COLOR, UifUtilities.colorToHex(DEFAULT_POSITIVE_COLOR))));
        config.setNegativeColor(prefToColor(SyncIn.getString(prefs, PREFIX + PROP_NEGATIVE_COLOR, UifUtilities.colorToHex(DEFAULT_NEGATIVE_COLOR))));
    }

    /**
     * Synchronizes preferences out.
     *
     * @param prefs preferences.
     */
    public static void syncOut(Map prefs)
    {
        SentimentsConfig config = Calculator.getConfig();

        SyncOut.setBoolean(prefs, PREFIX + PROP_ENABLED, config.isEnabled());
        SyncOut.setString(prefs, PREFIX + PROP_POSITIVE_EXPRESSIONS, config.getPositiveExpressions());
        SyncOut.setString(prefs, PREFIX + PROP_NEGATIVE_EXPRESSIONS, config.getNegativeExpressions());
        SyncOut.setInt(prefs, PREFIX + PROP_POSITIVE_THRESHOLD, config.getPositiveThreshold());
        SyncOut.setInt(prefs, PREFIX + PROP_NEGATIVE_THRESHOLD, config.getNegativeThreshold());
        SyncOut.setString(prefs, PREFIX + PROP_POSITIVE_COLOR, UifUtilities.colorToHex(config.getPositiveColor()));
        SyncOut.setString(prefs, PREFIX + PROP_NEGATIVE_COLOR, UifUtilities.colorToHex(config.getNegativeColor()));
    }
}
