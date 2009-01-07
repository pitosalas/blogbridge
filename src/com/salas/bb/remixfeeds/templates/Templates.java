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
// $Id: Templates.java,v 1.13 2008/04/03 08:53:22 spyromus Exp $
//

package com.salas.bb.remixfeeds.templates;

import com.salas.bb.core.FeatureManager;
import com.salas.bb.core.GlobalController;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * The repository of templates.
 */
public final class Templates
{
    // Old modes

    private static final int MODE_TITLE_AND_LINK = 0;
    private static final int MODE_EXCERPT_AND_LINK = 1;
    private static final int MODE_FULL_TEXT = 2;

    // Preference key holding the number of templates
    private static final String KEY_COUNT = "ptb.templates.count";

    public static Template TITLE_ONLY  = new Template("Title Only", true, Strings.message("ptb.template.title.only"));
    public static Template BRIEF       = new Template("Brief", true, Strings.message("ptb.template.brief"));
    public static Template FULL        = new Template("Full", true, Strings.message("ptb.template.full"));

    public static Template DEFAULT     = FULL;

    /** System templates only. */
    private static Map<String, Template> SYSTEM = new HashMap<String, Template>();

    /** All templates */
    private static Map<String, Template> ALL = new HashMap<String, Template>();

    static
    {
        addTemplate(TITLE_ONLY);
        addTemplate(BRIEF);
        addTemplate(FULL);

        SYSTEM.putAll(ALL);
    }

    /**
     * Hidden singleton constructor.
     */
    private Templates()
    {
    }

    /**
     * Adds a template to the repository.
     *
     * @param template template.
     */
    public static void addTemplate(Template template)
    {
        String key = template.getName();
        if (!ALL.containsKey(key)) ALL.put(key, template);
    }

    /**
     * Deletes a template.
     *
     * @param template template.
     */
    public static void deleteTemplate(Template template)
    {
        ALL.remove(template.getName());
    }

    /**
     * Returns all templates mapped to their titles.
     *
     * @return templates.
     */
    public static Map<String, Template> getAll()
    {
        return Collections.unmodifiableMap(ALL);
    }

    /**
     * Returns only system templates if the user has FREE level and ALL templates otherwise.
     *
     * @return templates
     */
    public static Map<String, Template> getUserTemplates()
    {
        return hasCustomTemplatesFeature() ? getAll() : Collections.unmodifiableMap(SYSTEM);
    }

    /**
     * Returns the names of user-accessible templates.
     *
     * @return user template names.
     */
    public static Collection<String> getUserTemplateNames()
    {
        return getUserTemplates().keySet();
    }

    /**
     * Returns TRUE if a template with the given title is already registered.
     *
     * @param title title.
     *
     * @return TRUE if a template with the given title is already registered.
     */
    public static boolean isExisting(String title)
    {
        return ALL.get(title) != null;
    }

    /**
     * This method is used to convert previously used mode into the template name.
     *
     * @param mode mode.
     *
     * @return name.
     */
    public static String fromOldMode(int mode)
    {
        String name;

        switch (mode)
        {
            case MODE_TITLE_AND_LINK:
                name = TITLE_ONLY.getName();
                break;
            case MODE_EXCERPT_AND_LINK:
                name = BRIEF.getName();
                break;
            case MODE_FULL_TEXT:
                name = FULL.getName();
                break;
            default:
                name = DEFAULT.getName();
        }

        return name;
    }

    /**
     * Finds a template by name or returns the default.
     *
     * @param name name.
     *
     * @return template.
     */
    public static Template getByName(String name)
    {
        Template template = ALL.get(name);
        if (template == null || !hasCustomTemplatesFeature()) template = DEFAULT;

        return template;
    }

    /**
     * Returns TRUE if the user has custom templates feature.
     *
     * @return TRUE if has.
     */
    public static boolean hasCustomTemplatesFeature()
    {
        FeatureManager fm = GlobalController.SINGLETON.getFeatureManager();
        return fm.isPtbAdvanced();
    }

    /**
     * Stores user-defined templates.
     *
     * @param prefs preferences.
     */
    public static void store(Preferences prefs)
    {
        Collection<Template> templates = ALL.values();

        // Store templates one by one
        int seq = 0;
        for (Template template : templates)
        {
            if (!template.isSystem()) template.store(prefs, ++seq);
        }

        // Store the counter
        prefs.putInt(KEY_COUNT, seq);
    }

    /**
     * Restores user-defined templates.
     *
     * @param prefs preferences.
     */
    public static void restore(Preferences prefs)
    {
        int count = prefs.getInt(KEY_COUNT, 0);

        for (int seq = 1; seq <= count; seq++)
        {
            try
            {
                Template template = new Template();
                template.restore(prefs, seq);

                if (StringUtils.isNotEmpty(template.getName())) addTemplate(template);
            } catch (InvalidSyntaxException e)
            {
                // One of the templates has invalid syntax. It will be removed.
            }
        }
    }
}
