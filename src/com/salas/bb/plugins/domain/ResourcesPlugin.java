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
// $Id: ResourcesPlugin.java,v 1.3 2007/07/07 06:59:17 spyromus Exp $
//

package com.salas.bb.plugins.domain;

import com.jgoodies.uif.application.Application;
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.utils.StringUtils;
import org.jdom.Element;

import java.util.*;

/**
 * Reseources plug-in re-assigns resources.
 */
public class ResourcesPlugin extends AbstractPlugin
{
    private final String bundle;

    /**
     * Creates new plug-in.
     *
     * @param element   XML element to parse.
     * @param loader    loader to use for resources.
     */
    public ResourcesPlugin(Element element, ClassLoader loader)
    {
        this(getType(element, "Resources", "Resources", "Icons", "Sounds"), element, loader);
    }

    /**
     * Creates new plug-in.
     *
     * @param type      type of the plug-in.
     * @param element   XML element to parse.
     * @param loader    loader to use for resources.
     */
    protected ResourcesPlugin(String type, Element element, ClassLoader loader)
    {
        super(type, loader);

        String bundle = element.getAttributeValue("bundle");
        if (StringUtils.isEmpty(bundle)) throw new IllegalArgumentException("Bundle can't be empty");

        this.bundle = bundle;
    }

    /** Initializes plug-in. */
    public void initialize()
    {
        ResourceBundle bundle = ResourceBundle.getBundle(this.bundle, Locale.getDefault(), getLoader());
        if (bundle != null)
        {
            ResourceBundle parent = getParentBundle();
            overrideBundle(new OverridingResourceBundle(parent, bundle));
        }
    }

    /**
     * Hook to get current bundle to be the parent to override.
     *
     * @return to-be-parent bundle.
     */
    protected ResourceBundle getParentBundle()
    {
        String path = Application.getConfiguration().getResourcesBundlePath();
        return ResourceBundle.getBundle(path, Locale.getDefault(),
            ResourceUtils.class.getClassLoader());
    }

    /**
     * Hook to install new bundle in place of the present. Don't forget to install the
     * class loader if you need any resources being loaded from the plug-in package.
     *
     * @param bundle bundle.
     */
    protected void overrideBundle(ResourceBundle bundle)
    {
        ResourceUtils.setBundle(bundle);
        ResourceUtils.setDefaultClassLoader(getLoader());
    }

    /**
     * Returns the type of the actions plug-in.
     *
     * @param element   element.
     * @param def       default type value.
     * @param allowed   optional list of allowed types.
     *
     * @return type.
     */
    protected static String getType(Element element, String def, String ... allowed)
    {
        String type = element.getAttributeValue("type");

        if (StringUtils.isEmpty(type) ||
            (allowed.length > 0 && !Arrays.asList(allowed).contains(type)))
        {
            type = def;
        }

        return type;
    }

    /**
     * Overriding bundle takes the keys from override first, then from parent.
     */
    static class OverridingResourceBundle extends ResourceBundle
    {
        private final ResourceBundle override;

        /**
         * Creates the bundle.
         *
         * @param parent    parent.
         * @param override  override.
         */
        public OverridingResourceBundle(ResourceBundle parent, ResourceBundle override)
        {
            this.override = override;

            setParent(parent);
        }

        /**
         * Gets an object for the given key from this resource bundle.
         * Returns null if this resource bundle does not contain an
         * object for the given key.
         *
         * @param key the key for the desired object
         *
         * @return the object for the given key, or null
         *
         * @throws NullPointerException if <code>key</code> is <code>null</code>
         */
        protected Object handleGetObject(String key)
        {
            Object o;

            try
            {
                o = override.getObject(key);
            } catch (MissingResourceException e)
            {
                o = null;
            }

            return o;
        }

        /** Returns an enumeration of the keys. */
        public Enumeration<String> getKeys()
        {
            Map<String, Object> kmap = new HashMap<String, Object>();
            Vector<String> keys = new Vector<String>();

            if (parent != null) addAllMissingKeys(keys, parent.getKeys(), kmap);
            addAllMissingKeys(keys, override.getKeys(), kmap);

            return keys.elements();
        }

        private static void addAllMissingKeys(Vector<String> keys, Enumeration<String> newKeys,
                                              Map<String, Object> presentKeys)
        {
            while (newKeys.hasMoreElements())
            {
                String k = newKeys.nextElement();
                if (!presentKeys.containsKey(k))
                {
                    keys.add(k);
                    presentKeys.put(k, k);
                }
            }
        }
    }
}
