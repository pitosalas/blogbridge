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
// $Id: FeatureManager.java,v 1.17 2008/03/26 13:48:49 spyromus Exp $
//

package com.salas.bb.core;

import com.salas.bb.service.ServerService;
import com.salas.bb.service.ServicePreferences;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.DateUtils;
import com.salas.bb.utils.StringUtils;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Features manager holds information about all available features and fetches
 * the updates from the service.
 */
public final class FeatureManager implements Runnable
{
    private static final Logger LOG = Logger.getLogger(FeatureManager.class.getName());

    protected static final long UPDATE_PERIOD_SEC = System.getProperty("fm.debug") != null ? 10 : 3600;

    static final String KEY_SYNCHRONIZATIONS                = "fm.synchronizations";
    static final String KEY_LAST_SYNC_DATE                  = "fm.lastSyncDate";

    // --- Default values for features
    private static final String DEFAULT_PLAN_NAME           = "Free";
    private static final Date DEFAULT_PLAN_EXP_DATE         = null;
    private static final int DEFAULT_PLAN_PERIOD_MONTHS     = 0;
    private static final int DEFAULT_PLAN_PRICE             = 0;
    private static final boolean DEFAULT_PLAN_TRIAL         = false;

    private static final int DEFAULT_PUB_LIMIT              = 2;
    private static final int DEFAULT_SUB_LIMIT              = 300;
    private static final int DEFAULT_SYN_LIMIT              = 2;
    private static final boolean DEFAULT_PTB_ENABLED        = false;
    private static final boolean DEFAULT_PTB_ADVANCED       = false;
    private static final boolean DEFAULT_SF_DEDUPLICATION   = false;
    private static final boolean DEFAULT_AUTO_SAVING        = false;
    private static final boolean DEFAULT_SENTIMENTS_ENABLED = false;

    // --- Feature keys
    private static final String FEAT_NAME                   = "_name";
    private static final String FEAT_HASH                   = "_hash";
    private static final String FEAT_EXP_DATE               = "_exp_date";
    private static final String FEAT_PRICE                  = "_price";
    private static final String FEAT_PERIOD_MONTHS          = "_period_months";
    private static final String FEAT_IS_TRIAL               = "_is_trial";
    private static final String FEAT_PTB_ENABLED            = "ptb-enabled";
    private static final String FEAT_PTB_ADVANCED           = "ptb-advanced";
    private static final String FEAT_PUB_LIMIT              = "pub-limit";
    private static final String FEAT_SUB_LIMIT              = "sub-limit";
    private static final String FEAT_SYN_LIMIT              = "syn-limit";
    private static final String FEAT_SF_DEDUPLICATION       = "sf-deduplication";
    private static final String FEAT_AUTO_SAVING            = "auto-saving";

    private static final String FEAT_SA_ENABLED             = "sa-enabled"; // Sentiment analysis

    // --- Feature property names
    public static final String PROP_PUBLICATION_LIMIT       = "publicationLimit";
    public static final String PROP_SUBSCRIPTION_LIMIT      = "subscriptionLimit";
    public static final String PROP_SYNCHRONIZATION_LIMIT   = "synchronizationLimit";
    public static final String PROP_PTB_ENABLED             = "ptbEnabled";
    public static final String PROP_PTB_ADVANCED            = "ptbAdvanced";
    public static final String PROP_SF_DEDUPLICATION        = "sfDeduplication";
    public static final String PROP_AUTO_SAVING             = "autoSaving";
    public static final String PROP_SENTIMENTS_ENABLED      = "sentimentsEnabled";

    // --- Preferences keys
    private static final String PREFS_KEY                   = "plan-features";
    
    // --- Current plan details
    private String planHash             = "";
    private String planName             = DEFAULT_PLAN_NAME;
    private Date planExpirationDate     = DEFAULT_PLAN_EXP_DATE;
    private int planPeriodMonths        = DEFAULT_PLAN_PERIOD_MONTHS;
    private float planPrice             = DEFAULT_PLAN_PRICE;
    private boolean planTrial           = DEFAULT_PLAN_TRIAL;

    // --- Features
    private int publicationLimit        = DEFAULT_PUB_LIMIT;
    private int subscriptionLimit       = DEFAULT_SUB_LIMIT;
    private int synchronizationLimit    = DEFAULT_SYN_LIMIT;
    private boolean ptbEnabled          = DEFAULT_PTB_ENABLED;
    private boolean ptbAdvanced         = DEFAULT_PTB_ADVANCED;
    private boolean sfDeduplication     = DEFAULT_SF_DEDUPLICATION;
    private boolean autoSaving          = DEFAULT_AUTO_SAVING;
    private boolean sentimentsEnabled   = DEFAULT_SENTIMENTS_ENABLED;

    // --- Misc
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private static final Object SYNCS_LOCK = new Object();

    private final Preferences appPrefs;
    private ServicePreferences servicePrefs;

    /**
     * Creates a feataure manager and loads features from the preferences.
     *
     * @param appPrefs preferences.
     */
    public FeatureManager(Preferences appPrefs)
    {
        this.appPrefs = appPrefs;
        loadFeatures();
    }

    /**
     * Sets service preferences.
     *
     * @param prefs preferences.
     */
    public void setServicePreferences(ServicePreferences prefs)
    {
        this.servicePrefs = prefs;
    }

    /**
     * Invoked to update the features.
     */
    public void run()
    {
        update();
    }

    /**
     * Checks if there's something changed on the service with the feature set
     * and update it if there's.
     */
    void update()
    {
        if (servicePrefs != null)
        {
            String email = servicePrefs.getEmail();
            String password = servicePrefs.getPassword();

            if (StringUtils.isNotEmpty(email) && StringUtils.isNotEmpty(password))
            {
                // Ping the service for hash
                String newHash = ServerService.getPlanHash(email, password);

                // Check if the hash matches our current version
                // If it isn't, update the features map and save to the preferences
                if (newHash != null && !newHash.equals(planHash))
                {
                    Map<String, String> features = ServerService.getPlanFeatures(email, password);
                    if (features != null)
                    {
                        if (isValid(features))
                        {
                            // Store the new hash among features
                            features.put(FEAT_HASH, newHash);

                            parseNewFeatures(features);
                            storeFeatures(features);
                        }
                    }
                }
            } else if (!DEFAULT_PLAN_NAME.equals(getPlanName()))
            {
                revertToDefault();
            }
        }

        if (isExpired()) revertToDefault();
    }

    /**
     * Checks if current plan is expired.
     *
     * @return <code>TRUE</code> if the plan is expired.
     */
    boolean isExpired()
    {
        long thisTimeLastWeek = System.currentTimeMillis() - Constants.MILLIS_IN_WEEK;
        return planExpirationDate != null && planExpirationDate.before(new Date(thisTimeLastWeek));
    }

    /**
     * Reverts the features to default.
     */
    private void revertToDefault()
    {
        // We intentionally do not revert the hash to avoid continuous updating
        // When the plan will be update on the server side so it's no longer expired
        // the hash will become different and the updates will be taken.

        setPlanName(DEFAULT_PLAN_NAME);
        setPlanExpirationDate(DEFAULT_PLAN_EXP_DATE);
        setPlanPeriodMonths(DEFAULT_PLAN_PERIOD_MONTHS);
        setPlanPrice(DEFAULT_PLAN_PRICE);
        setPlanTrial(DEFAULT_PLAN_TRIAL);

        setPtbEnabled(DEFAULT_PTB_ENABLED);
        setPtbAdvanced(DEFAULT_PTB_ADVANCED);
        setPublicationLimit(DEFAULT_PUB_LIMIT);
        setSubscriptionLimit(DEFAULT_SUB_LIMIT);
        setSynchronizationLimit(DEFAULT_SYN_LIMIT);

        setSfDeduplication(DEFAULT_SF_DEDUPLICATION);

        setSentimentsEnabled(DEFAULT_SENTIMENTS_ENABLED);
    }

    /**
     * Parses the map of new features, validates it and updates current features if everything seems OK.
     *
     * @param features  new features.
     */
    private void parseNewFeatures(Map<String, String> features)
    {
        // Read the plan features
        setPlanHash(features.get(FEAT_HASH));
        setPlanName(features.get(FEAT_NAME));
        setPlanExpirationDate(getDate(features, FEAT_EXP_DATE));
        setPlanPrice(getFloat(features, FEAT_PRICE));
        setPlanPeriodMonths(getInt(features, FEAT_PERIOD_MONTHS));
        setPlanTrial(getBoolean(features, FEAT_IS_TRIAL));

        // Read features we understand
        setPtbEnabled(getBoolean(features, FEAT_PTB_ENABLED, DEFAULT_PTB_ENABLED));
        setPtbAdvanced(getBoolean(features, FEAT_PTB_ADVANCED, DEFAULT_PTB_ADVANCED));
        setPublicationLimit(getInt(features, FEAT_PUB_LIMIT, DEFAULT_PUB_LIMIT));
        setSubscriptionLimit(getInt(features, FEAT_SUB_LIMIT, DEFAULT_SUB_LIMIT));
        setSynchronizationLimit(getInt(features, FEAT_SYN_LIMIT, DEFAULT_SYN_LIMIT));
        setSfDeduplication(getBoolean(features, FEAT_SF_DEDUPLICATION, DEFAULT_SF_DEDUPLICATION));
        setAutoSaving(getBoolean(features, FEAT_AUTO_SAVING, DEFAULT_AUTO_SAVING));

        setSentimentsEnabled(getBoolean(features, FEAT_SA_ENABLED, DEFAULT_SENTIMENTS_ENABLED));
    }

    /**
     * Checks if the given features collection is valid before using it.
     *
     * @param features features.
     *
     * @return <code>TRUE</code> if valid and ready for consumption.
     */
    private static boolean isValid(Map<String, String> features)
    {
        return StringUtils.isNotEmpty(features.get(FEAT_NAME)) &&
            isDate(features, FEAT_EXP_DATE) &&
            isFloat(features, FEAT_PRICE) &&
            isInt(features, FEAT_PERIOD_MONTHS, false) &&
            isBoolean(features, FEAT_IS_TRIAL);
    }

    /**
     * Checks if the given feature exists and is a valid integer.
     *
     * @param features          features collection.
     * @param name              feature name.
     * @param unlimIsPossible   <code>TRUE</code> if unlimited value ('-') is possible.
     *
     * @return <code>TRUE</code> if a valid integer value.
     */
    static boolean isInt(Map<String, String> features, String name, boolean unlimIsPossible)
    {
        String v = features.get(name);
        return StringUtils.isNotEmpty(v) && (v.matches("[0-9]+") || (unlimIsPossible && "-".equals(v)));
    }

    /**
     * Checks if the given feature exists and is a valid date.
     *
     * @param features  features collection.
     * @param name      feature name.
     *
     * @return <code>TRUE</code> if a valid date value.
     */
    static boolean isDate(Map<String, String> features, String name)
    {
        return isInt(features, name, false);
    }

    /**
     * Checks if the given feature exists and is a valid boolean.
     *
     * @param features  features collection.
     * @param name      feature name.
     *
     * @return <code>TRUE</code> if a valid boolean value.
     */
    static boolean isBoolean(Map<String, String> features, String name)
    {
        String v = features.get(name);
        return StringUtils.isNotEmpty(v) && v.matches("(0|1)");
    }

    /**
     * Checks if the given feature exists and is a valid float.
     *
     * @param features  features collection.
     * @param name      feature name.
     *
     * @return <code>TRUE</code> if a valid float value.
     */
    static boolean isFloat(Map<String, String> features, String name)
    {
        String v = features.get(name);
        return StringUtils.isNotEmpty(v) && v.matches("[0-9]+(\\.[0-9]+)?");
    }

    // ------------------------------------------------------------------------
    // Storage procedures
    // ------------------------------------------------------------------------

    /**
     * Loads features from the preferences.
     */
    private void loadFeatures()
    {
        if (appPrefs == null)
        {
            revertToDefault();
            return;
        }
        
        String f = decode(appPrefs.get(PREFS_KEY, null));
        if (StringUtils.isNotEmpty(f))
        {
            Map<String, String> features = new HashMap<String, String>();

            String[] featurePairs = f.split("~");
            for (String pair : featurePairs)
            {
                String[] p = pair.split("\\|");
                features.put(p[0], p[1]);
            }

            if (isValid(features) && StringUtils.isNotEmpty(features.get(FEAT_HASH)))
            {
                parseNewFeatures(features);
                planHash = features.get(FEAT_HASH);
            } else
            {
                LOG.severe("Loaded features have incorrect format: " + f);
            }
        }
    }

    /**
     * Saves features to the preferences.
     *
     * @param features  features collection.
     */
    private void storeFeatures(Map<String, String> features)
    {
        if (appPrefs == null) return;

        List<String> featureList = new ArrayList<String>();
        Set<Map.Entry<String,String>> fs = features.entrySet();
        for (Map.Entry<String, String> feature : fs)
        {
            featureList.add(feature.getKey() + '|' + feature.getValue());
        }

        appPrefs.put(PREFS_KEY, encode(StringUtils.join(featureList.iterator(), "~")));
    }

    /**
     * Simple encoder.
     *
     * @param s string to encode.
     *
     * @return encoded version.
     */
    static String encode(String s)
    {
        String v = s;

        if (StringUtils.isNotEmpty(s))
        {
            byte[] bs = s.getBytes();
            StringBuffer sb = new StringBuffer(bs.length);
            for (byte b : bs)
            {
                if (b < 16) sb.append(0);
                sb.append(Integer.toHexString(b));
            }

            v = sb.toString();
        }

        return v;
    }

    /**
     * Simple decoder.
     *
     * @param s string to decode.
     *
     * @return decoded version.
     */
    static String decode(String s)
    {
        String v = s;

        if (StringUtils.isNotEmpty(s))
        {
            byte[] bb = new byte[s.length() / 2];
            for (int i = 0; i < s.length(); i += 2)
            {
                String b = "" + s.charAt(i) + s.charAt(i+1);
                bb[i / 2] = (byte)Integer.parseInt(b, 16);
            }

            v = new String(bb);
        }

        return v;
    }
    
    // ------------------------------------------------------------------------
    // Helper functions to parse features
    // ------------------------------------------------------------------------

    private Date getDate(Map<String, String> features, String name)
    {
        long date = Long.parseLong(features.get(name));
        return date == 0 ? null : new Date(date);
    }

    private static int getInt(Map<String, String> features, String name)
    {
        return getInt(features, name, Integer.MIN_VALUE);
    }

    private static int getInt(Map<String, String> features, String name, int def)
    {
        String v = features.get(name);
        return v == null ? def : "-".equals(v) ? -1 : Integer.parseInt(v);
    }

    private static boolean getBoolean(Map<String, String> features, String name)
    {
        return getBoolean(features, name, false);
    }

    private static boolean getBoolean(Map<String, String> features, String name, boolean def)
    {
        int v = getInt(features, name);
        return v == Integer.MIN_VALUE ? def : v == 1;
    }

    private static float getFloat(Map<String, String> features, String name)
    {
        return Float.parseFloat(features.get(name));
    }

    // ------------------------------------------------------------------------
    // Property change support
    // ------------------------------------------------------------------------

    /**
     * Adds property change listener.
     *
     * @param l pcl.
     */
    public void addPropertyChangeListener(PropertyChangeListener l)
    {
        pcs.addPropertyChangeListener(l);
    }

    /**
     * Removes property change listener.
     *
     * @param l pcl.
     */
    public void removePropertyChangeListener(PropertyChangeListener l)
    {
        pcs.removePropertyChangeListener(l);
    }

    /**
     * Adds property change listener.
     *
     * @param prop  property name.
     * @param l     pcl.
     */
    public void addPropertyChangeListener(String prop, PropertyChangeListener l)
    {
        pcs.addPropertyChangeListener(prop, l);
    }

    /**
     * Removes property change listener.
     *
     * @param prop  property name.
     * @param l     pcl.
     */
    public void removePropertyChangeListener(String prop, PropertyChangeListener l)
    {
        pcs.removePropertyChangeListener(prop, l);
    }

    // ------------------------------------------------------------------------
    // Plan properties
    // ------------------------------------------------------------------------

    /**
     * Returns the name of the current plan.
     *
     * @return name.
     */
    public String getPlanName()
    {
        return planName;
    }

    /**
     * Sets the name of the current plan.
     *
     * @param name name.
     */
    public void setPlanName(String name)
    {
        this.planName = name;
    }

    /**
     * Returns plan hash.
     *
     * @return hash.
     */
    public String getPlanHash()
    {
        return planHash;
    }

    /**
     * Sets plan hash.
     *
     * @param planHash hash.
     */
    void setPlanHash(String planHash)
    {
        this.planHash = planHash;
    }

    /**
     * Returns plan expiration date.
     *
     * @return exp date.
     */
    public Date getPlanExpirationDate()
    {
        return planExpirationDate;
    }

    /**
     * Sets plan expiration date.
     *
     * @param date  date.
     */
    void setPlanExpirationDate(Date date)
    {
        planExpirationDate = date;
    }

    /**
     * Returns plan period in months.
     *
     * @return months.
     */
    public int getPlanPeriodMonths()
    {
        return planPeriodMonths;
    }

    /**
     * Sets plan period in months.
     *
     * @param period period.
     */
    void setPlanPeriodMonths(int period)
    {
        this.planPeriodMonths = period;
    }

    /**
     * Returns plan price.
     *
     * @return price.
     */
    public float getPlanPrice()
    {
        return planPrice;
    }

    /**
     * Returns TRUE if the plain is the paid one.
     *
     * @return TRUE if the plain is the paid one. 
     */
    public boolean isPaidPlan()
    {
        return planName != null && !"free".equalsIgnoreCase(planName);
    }

    /**
     * Sets plan price.
     *
     * @param price price.
     */
    void setPlanPrice(float price)
    {
        this.planPrice = price;
    }

    /**
     * Returns <code>TRUE</code> if plan is trial.
     *
     * @return <code>TRUE</code> if plan is trial.
     */
    public boolean isPlanTrial()
    {
        return planTrial;
    }

    /**
     * Sets the trial plan flag.
     *
     * @param trial trial.
     */
    void setPlanTrial(boolean trial)
    {
        this.planTrial = trial;
    }

    // ------------------------------------------------------------------------
    // Features
    // ------------------------------------------------------------------------

    /**
     * Returns the number of lists allowed for publishing.
     *
     * @return the limit or '-1' for unlimited.
     */
    public int getPublicationLimit()
    {
        return publicationLimit;
    }

    /**
     * Sets new published list limit.
     *
     * @param limit limit.
     */
    private void setPublicationLimit(int limit)
    {
        int old = publicationLimit;
        publicationLimit = limit;

        pcs.firePropertyChange(PROP_PUBLICATION_LIMIT, old, limit);
    }

    /**
     * Returns the maximum number of subscriptions a user can have.
     *
     * @return the limit or '-1' for unlimited.
     */
    public int getSubscriptionLimit()
    {
        return subscriptionLimit;
    }

    /**
     * Sets new subscriptions limit.
     *
     * @param limit limit.
     */
    private void setSubscriptionLimit(int limit)
    {
        int old = subscriptionLimit;
        subscriptionLimit = limit;

        pcs.firePropertyChange(PROP_SUBSCRIPTION_LIMIT, old, limit);
    }

    /**
     * Returns the maximum number of syncs a user can have during the day.
     *
     * @return the limit.
     */
    public int getSynchronizationLimit()
    {
        return synchronizationLimit;
    }

    /**
     * Sets new sync limit.
     *
     * @param limit limit.
     */
    void setSynchronizationLimit(int limit)
    {
        int old = synchronizationLimit;
        synchronizationLimit = limit;

        pcs.firePropertyChange(PROP_SYNCHRONIZATION_LIMIT, old, limit);
    }

    /**
     * Returns <code>TRUE</code> when post-to-blog feature is enabled.
     *
     * @return <code>TRUE</code> when post-to-blog feature is enabled.
     */
    public boolean isPtbEnabled()
    {
        return ptbEnabled;
    }

    /**
     * Sets new PTB enabled flag.
     *
     * @param enabled flag.
     */
    private void setPtbEnabled(boolean enabled)
    {
        boolean old = ptbEnabled;
        ptbEnabled = enabled;

        pcs.firePropertyChange(PROP_PTB_ENABLED, old, enabled);
    }

    /**
     * Returns <code>TRUE</code> if the post-to-blog feature should be advanced
     * (multiple blogs / advanced dialog).
     *
     * @return <code>TRUE</code> if the post-to-blog feature is advanced.
     */
    public boolean isPtbAdvanced()
    {
        return ptbAdvanced;
    }

    /**
     * Sets new PTB advanced flag.
     *
     * @param flag flag.
     */
    private void setPtbAdvanced(boolean flag)
    {
        boolean old = ptbAdvanced;
        ptbAdvanced = flag;

        pcs.firePropertyChange(PROP_PTB_ADVANCED, old, flag);
    }

    /**
     * Returns <code>TRUE</code> for enabled deduplication mode.
     *
     * @return <code>TRUE</code> if enabled.
     */
    public boolean isSfDeduplication()
    {
        return sfDeduplication;
    }

    /**
     * Sets the state of deduplication mode.
     *
     * @param en <code>TRUE</code> to enable.
     */
    private void setSfDeduplication(boolean en)
    {
        boolean old = sfDeduplication;
        this.sfDeduplication = en;

        pcs.firePropertyChange(PROP_SF_DEDUPLICATION, old, en);
    }

    /**
     * Returns <code>TRUE</code> if auto-saving is on.
     *
     * @return <code>TRUE</code> if auto-saving is on.
     */
    public boolean isAutoSaving()
    {
        return autoSaving;
    }

    /**
     * Enabled / disables auto-saving.
     *
     * @param en <code>TRUE</code> to enable.
     */
    private void setAutoSaving(boolean en)
    {
        boolean old = autoSaving;
        autoSaving = en;

        pcs.firePropertyChange(PROP_AUTO_SAVING, old, en);
    }

    /**
     * Returns TRUE if sentiments feature are enabled.
     *
     * @return TRUE if sentiments feature are enabled.
     */
    public boolean isSentimentsEnabled()
    {
        return sentimentsEnabled;
    }

    /**
     * Enables / disables sentiments feature.
     *
     * @param en TRUE to enable.
     */
    private void setSentimentsEnabled(boolean en)
    {
        boolean old = sentimentsEnabled;
        sentimentsEnabled = en;

        pcs.firePropertyChange(PROP_SENTIMENTS_ENABLED, old, en);

    }

    /**
     * Returns the task updating the plan features.
     *
     * @return task.
     */
    public Runnable getUpdater()
    {
        return this;
    }

    /**
     * Registers synchronization attempt.
     */
    public void registerSync()
    {
        synchronized (SYNCS_LOCK)
        {
            getSynchronizationsCount(true);
        }
    }

    /**
     * Returns the number of synchronizations today.
     * NOTE: Must be called with SYNCS_LOCK.
     *
     * @param inc   <code>TRUE</code> to increment counter.
     *
     * @return syncs.
     */
    int getSynchronizationsCount(boolean inc)
    {
        long today = DateUtils.getTodayTime();
        long lastSyncDate = appPrefs.getLong(KEY_LAST_SYNC_DATE, -1);
        int syncs;

        // Get current syncs count and reset if the day has ended
        boolean updated = false;
        if (lastSyncDate < today)
        {
            updated = true;
            syncs = 0;
        } else syncs = appPrefs.getInt(KEY_SYNCHRONIZATIONS, 0);

        // Increment if required
        if (inc) syncs++;

        // Save if updated
        if (updated || inc)
        {
            appPrefs.putLong(KEY_LAST_SYNC_DATE, System.currentTimeMillis());
            appPrefs.putInt(KEY_SYNCHRONIZATIONS, syncs);
        }

        return syncs;
    }

    /**
     * Checks if the user can synchronize today.
     *
     * @return <code>TRUE</code> if the user can synchronize.
     */
    public boolean canSynchronize()
    {
        boolean res = true;

        if (synchronizationLimit > -1)
        {
            synchronized (SYNCS_LOCK)
            {
                res = getSynchronizationsCount(false) < synchronizationLimit;
            }
        }

        return res;
    }

    /**
     * Returns TRUE if the user is registered.
     *
     * NOTE: This checks only if the user has entered their email / password.
     *
     * @return TRUE if registered.
     */
    public boolean isRegistered()
    {
        String email = servicePrefs.getEmail();
        String password = servicePrefs.getPassword();

        return StringUtils.isNotEmpty(email) && StringUtils.isNotEmpty(password);
    }
}
