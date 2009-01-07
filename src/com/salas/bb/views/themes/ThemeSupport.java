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
// $Id: ThemeSupport.java,v 1.12 2007/03/19 15:49:45 spyromus Exp $
//

package com.salas.bb.views.themes;

import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Loader of themes available for the current OS.
 */
public final class ThemeSupport
{
    private static final Logger LOG = Logger.getLogger(ThemeSupport.class.getName());

    private static final char ZIP_SEPARATOR     = '/';
    private static final String THEMES_INDEX    = "resources/themes/index.txt";

    private static List<ITheme> themes;
    private static ITheme       defaultTheme;
    private static String       currentOS;
    private static final String KEY_NAME = "name";
    private static final String KEY_TITLE = "title";
    private static final String KEY_HIDDEN = "hidden";

    /**
     * Hidden constructor of utility class.
     */
    private ThemeSupport()
    {
    }

    /**
     * Returns list of available themes.
     *
     * @return themes list.
     */
    public static synchronized List<ITheme> getThemes()
    {
        if (themes == null) themes = loadThemes();

        return themes;
    }

    /**
     * Returns default theme.
     *
     * @return default theme.
     */
    public static synchronized ITheme getDefaultTheme()
    {
        if (defaultTheme == null) defaultTheme = loadDefaultTheme();

        return defaultTheme;
    }

    /**
     * Looks up theme by its name.
     *
     * @param name  name.
     *
     * @return theme or NULL if it wasn't found.
     */
    public static ITheme getThemeByName(String name)
    {
        for (ITheme theme : getThemes()) if (theme.getName().equals(name)) return theme;
        return null;
    }

    /**
     * Loads list of themes available for this platform.
     *
     * @return list of themes.
     */
    private static List<ITheme> loadThemes()
    {
        ITheme aDefaultTheme = getDefaultTheme();

        return loadThemes(aDefaultTheme);
    }

    /**
     * Loads list of themes available for this platform.
     *
     * @param aDefaultTheme default theme to use.
     *
     * @return list of themes.
     */
    private static List<ITheme> loadThemes(ITheme aDefaultTheme)
    {
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine("Calling loadThemes (" + aDefaultTheme + ")");
        }

        URL[] themesUrls = getThemesUrls();
        List<ITheme> someThemes = new ArrayList<ITheme>(themesUrls.length);
        for (URL themeUrl : themesUrls)
        {
            ITheme theme = loadTheme(themeUrl, aDefaultTheme);
            if (theme != null) someThemes.add(theme);
        }

        return someThemes;
    }

    /**
     * Loads default theme for this platform.
     *
     * @return default theme.
     */
    private static ITheme loadDefaultTheme()
    {
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine("Calling LoadDefaultTheme()");
        }

        return loadTheme(getThemeUrl("default.theme"), new LAFTheme());
    }

    /**
     * Loads theme from definition and assigns specified default theme.
     *
     * @param themeUrl      URL of the theme definition.
     * @param aDefaultTheme default theme to assign.
     *
     * @return theme or NULL if not defined for this OS.
     *
     * @throws IllegalArgumentException if defaultTheme is NULL or URL is NULL or invalid.
     */
    private static ITheme loadTheme(URL themeUrl, ITheme aDefaultTheme)
    {
        if (themeUrl == null)
            throw new IllegalArgumentException(Strings.error("unspecified.theme.url"));

        String os = getCurrentOS();
        ITheme theme;

        try
        {
            Properties properties = new Properties();
            properties.load(themeUrl.openStream());

            theme = createTheme(properties, aDefaultTheme, os);
        } catch (IOException e)
        {
            throw new IllegalArgumentException(Strings.error("invalid.theme.url"));
        }

        return theme;
    }

    /**
     * Creates theme using properties if it isn't set that this theme should be hidden for the
     * given OS.
     *
     * @param aProperties   properties to use for theme.
     * @param aDefaultTheme default theme.
     * @param aOs           OS to create theme for.
     *
     * @return theme or NULL if theme cannot be created for the OS.
     */
    static ITheme createTheme(Properties aProperties, ITheme aDefaultTheme, String aOs)
    {
        if (aDefaultTheme == null)
            throw new IllegalArgumentException(Strings.error("unspecified.default.theme"));
        if (aProperties == null)
            throw new IllegalArgumentException(Strings.error("unspecified.properties"));
        if (aOs == null)
            throw new IllegalArgumentException(Strings.error("unspecified.os"));

        String name = aProperties.getProperty(KEY_NAME);
        String title = aProperties.getProperty(KEY_TITLE);
        String hidden = aProperties.getProperty(KEY_HIDDEN);

        ITheme theme = null;
        boolean isHidden = name == null || title == null || isInList(hidden, aOs);
        if (!isHidden)
        {
            aProperties = preProcessProperties(aProperties, aOs);
            theme = new Theme(name, title, aProperties, aDefaultTheme);
        }

        return theme;
    }

    /**
     * Takes values, specific to the OS, and puts them over the simple key versions.
     *
     * @param aProperties   properties.
     * @param aOs           OS.
     *
     * @return processed properties version.
     */
    static Properties preProcessProperties(Properties aProperties, String aOs)
    {
        final Set keySet = aProperties.keySet();
        final String[] keys = (String[])keySet.toArray(new String[keySet.size()]);

        aOs = "." + aOs;
        int suffixLength = aOs.length();

        for (String key : keys)
        {
            if (key.endsWith(aOs))
            {
                String baseKey = key.substring(0, key.length() - suffixLength);
                aProperties.setProperty(baseKey, aProperties.getProperty(key));
            }
        }

        return aProperties;
    }

    /**
     * Accepts comma-delimetered list and string and tells if the string in this list.
     *
     * @param list  comma-delimetered list.
     * @param str   string to look for.
     *
     * @return TRUE if string is found in list.
     */
    private static boolean isInList(String list, String str)
    {
        if (list == null) return false;

        str = str.toLowerCase();
        StringTokenizer tokenizer = new StringTokenizer(list, ",");
        boolean found = false;
        while (!found && tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken().trim().toLowerCase();
            found = token.equals(str);
        }

        return found;
    }

    /**
     * Returns the URL to the theme file definition.
     *
     * @param themeFileName file name.
     *
     * @return URL.
     */
    private static URL getThemeUrl(String themeFileName)
    {
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine("Calling getThemeUrl(" + themeFileName + ")");
        }

        return ResourceUtils.getURL("resources/themes/" + themeFileName);
    }

    /**
     * Returns current OS.
     *
     * @return 'windows', 'linux' or 'mac'.
     */
    private static synchronized String getCurrentOS()
    {
        if (currentOS == null) currentOS = detectCurrentOS();

        return currentOS;
    }

    /**
     * Detects current OS.
     *
     * @return current OS.
     */
    private static String detectCurrentOS()
    {
        String os = "mac";
        String propOsName = System.getProperty("os.name");

        if (Pattern.compile("linux", Pattern.CASE_INSENSITIVE).matcher(propOsName).find())
        {
            os = "linux";
        } else if (Pattern.compile("windows", Pattern.CASE_INSENSITIVE).matcher(propOsName).find())
        {
            os = "windows";
        }

        return os;
    }

    /**
     * Finds all available themes and returns their URL's.
     *
     * @return URL's of found themes.
     */
    private static URL[] getThemesUrls()
    {
        List<URL> themesUrlsList = new ArrayList<URL>();
        String directoryName = getDirectoryName(THEMES_INDEX);
        InputStream in = null;
        try
        {
            in = ResourceUtils.getInputStream(THEMES_INDEX);
            if (null == in) return new URL[0];

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            try
            {
                String line;
                do
                {
                    line = reader.readLine();
                    if (line != null)
                    {
                        line = line.trim();
                        if (line.length() > 0)
                            themesUrlsList.add(ResourceUtils.getURL(directoryName + line));
                    }
                } while (line != null);
                reader.close();
            } catch (IOException e)
            {
                LOG.log(Level.WARNING, MessageFormat.format(
                    Strings.error("failed.to.read.tips.index"),
                    THEMES_INDEX), e);
            }
        } finally
        {
            try
            {
                if (in != null) in.close();
            } catch (IOException e1)
            {
                // That's OK.
            }
        }

        return themesUrlsList.toArray(new URL[themesUrlsList.size()]);
    }

    /**
     * Answers the directory name for the given file name.
     *
     * @param filename file name.
     *
     * @return the directory name.
     */
    private static String getDirectoryName(String filename)
    {
        int lastSeparatorIndex = filename.lastIndexOf(ZIP_SEPARATOR);
        return (lastSeparatorIndex == -1) ? "" : filename.substring(0, lastSeparatorIndex + 1);
    }

    /**
     * Loads and adds a theme by its URL.
     *
     * @param themeURL theme URL.
     */
    public static void addTheme(URL themeURL)
    {
        getThemes().add(loadTheme(themeURL, getDefaultTheme()));
    }

    public static class FontsComboBoxModel extends DefaultComboBoxModel
    {
        private static final java.util.List fontFamilies;

        static
        {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            fontFamilies = Arrays.asList(ge.getAvailableFontFamilyNames());
        }

        /**
         * Returns size of the list.
         *
         * @return size.
         */
        public int getSize()
        {
            return fontFamilies.size();
        }

        /**
         * Returns element at specified index.
         *
         * @param index index.
         *
         * @return element.
         */
        public Object getElementAt(int index)
        {
            return fontFamilies.get(index);
        }

        /**
         * Returns the index-position of the specified object in the list.
         *
         * @param anObject object.
         *
         * @return an int representing the index position, where 0 is the first position
         */
        public int getIndexOf(Object anObject)
        {
            return fontFamilies.indexOf(anObject);
        }
    }

    /**
     * Immutable list of themes for any combo-box.
     */
    public static class ThemesComboBoxModel extends DefaultComboBoxModel
    {
        /**
         * Returns size of the list.
         *
         * @return size.
         */
        public int getSize()
        {
            return getThemes().size();
        }

        /**
         * Returns element at specified index.
         *
         * @param index index.
         *
         * @return element.
         */
        public Object getElementAt(int index)
        {
            return getThemes().get(index);
        }

        /**
         * Returns the index-position of the specified object in the list.
         *
         * @param anObject object.
         *
         * @return an int representing the index position, where 0 is the first position
         */
        public int getIndexOf(Object anObject)
        {
            return getThemes().indexOf(anObject);
        }
    }
}
