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
// $Id: Manager.java,v 1.21 2007/08/24 09:04:04 spyromus Exp $
//

package com.salas.bb.plugins;

import com.salas.bb.plugins.domain.*;
import com.salas.bb.plugins.domain.Package;
import com.salas.bb.utils.CommonUtils;
import com.salas.bb.utils.FileUtils;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.xml.XmlReaderFactory;
import com.salas.bbutilities.opml.utils.EmptyEntityResolver;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Central manager component. It's responsible for loading plug-in packages when
 * asked and reporting what's loaded.
 */
public class Manager
{
    private static final Logger LOG = Logger.getLogger(Manager.class.getName());

    static final String KEY_PLUGINS_PACKAGES                = "plugins.packages";
    static final String KEY_PLUGINS_PACKAGES_TS             = "plugins.packages.ts";

    private static final String KEY_PLUGINS_UNINSTALL       = "plugins.uninstall";
    private static final String PACKAGE_NAME_SEPARATOR      = ";";

    private static final String PACKAGE_XML                 = "package.xml";
    private static final String PACKAGE_FILENAME_PATTERN    = ".*\\.(jar|zip)\\s*$";

    private static final String NODE_PACKAGE                = "package";
    private static final String ATTR_PACKAGE_NAME           = "name";
    private static final String ATTR_PACKAGE_DESCRIPTION    = "description";
    private static final String ATTR_PACKAGE_VERSION        = "version";
    private static final String ATTR_PACKAGE_AUTHOR         = "author";
    private static final String ATTR_PACKAGE_EMAIL          = "email";

    private static final String NODE_THEME                  = "theme";
    private static final String NODE_ACTIONS                = "actions";
    private static final String NODE_RESOURCES              = "resources";
    private static final String NODE_STRINGS                = "strings";
    private static final String NODE_PREFERENCES            = "preferences";
    private static final String NODE_SMARTFEED              = "smartfeed";
    private static final String NODE_CODE                   = "code";
    private static final String NODE_TOOLBAR                = "toolbar";

    private static final List<Package> INSTALLED_PACKAGES = new ArrayList<Package>();

    private static boolean installedPackagesLoaded;

    private static File pluginDirectory;
    private static Preferences prefs;

    private static List<String> uninstallFilenames;
    private static List<Package> enabledPackages;

    /**
     * Initializes the manager.
     *
     * @param pluginDirectory   plug-in directory (created if missing).
     * @param appPrefs          application preferences.
     */
    public static void initialize(File pluginDirectory, Preferences appPrefs)
    {
        Manager.pluginDirectory = pluginDirectory;
        Manager.prefs = appPrefs;
        
        if (!pluginDirectory.exists()) pluginDirectory.mkdirs();

        doAutoDeployment();
        doUninstall();
        uninstallFilenames = new ArrayList<String>();
    }

    /**
     * Loads packages.
     */
    public static void loadPackages()
    {
        enabledPackages = new ArrayList<Package>();

        // Load, initialize and populate the list
        List<File> packages = getPackages();
        for (File pckg : packages)
        {
            Package p = load(pckg);
            if (p != null)
            {
                try
                {
                    p.initialize();
                } catch (Throwable e)
                {
                    LOG.log(Level.SEVERE, "Failed to initailize the action.", e);
                }
                enabledPackages.add(p);
            }
        }
    }

    /**
     * Returns the list of enabled packages.
     *
     * @return enabled.
     */
    public static List<Package> getEnabledPackages()
    {
        return Collections.unmodifiableList(enabledPackages);
    }

    /**
     * Sets the list of enabled packages. Re-loading doesn't happen.
     *
     * @param enabled list of enabled.
     */
    public static void setEnabledPackages(List<Package> enabled)
    {
        enabledPackages = enabled;

        List<String> names = new ArrayList<String>();
        if (enabled != null) for (Package pkg : enabled) names.add(pkg.getFileName());

        prefs.put(KEY_PLUGINS_PACKAGES, StringUtils.join(names.iterator(), PACKAGE_NAME_SEPARATOR));
        prefs.putLong(KEY_PLUGINS_PACKAGES_TS, System.currentTimeMillis());
    }

    /**
     * Returns the list of all detected installed packages.
     *
     * @return packages.
     */
    public static List<Package> getInstalledPackages()
    {
        synchronized (INSTALLED_PACKAGES)
        {
            if (!installedPackagesLoaded)
            {
                installedPackagesLoaded = true;

                INSTALLED_PACKAGES.clear();
                File[] files = pluginDirectory.listFiles();
                for (File file : files)
                {
                    if ((file.isDirectory() || file.getName().matches(PACKAGE_FILENAME_PATTERN)) &&
                        !uninstallFilenames.contains(file.getName()))
                    {
                        Package p = load(file);
                        if (p != null) INSTALLED_PACKAGES.add(p);
                    }
                }
            }
        }

        return INSTALLED_PACKAGES;
    }

    /**
     * Reloads the list of installed packages.
     *
     * @return installed packages.
     */
    public static List<Package> reloadInstalledPackages()
    {
        synchronized (INSTALLED_PACKAGES)
        {
            installedPackagesLoaded = false;
            return getInstalledPackages();
        }
    }

    /**
     * Installs the package file into the plug-ins directory.
     *
     * @param packageFile package file or directory.
     *
     * @return error message or <code>NULL</code> if succeeded.
     */
    public static String install(File packageFile)
    {
        String error = null;

        if (isPackage(packageFile))
        {
            String name = packageFile.getName();
            File dest = new File(pluginDirectory, name);

            if (dest.exists())
            {
                if (uninstallFilenames.contains(name))
                {
                    error = Strings.message("plugin.manager.install.uninstall");
                } else
                {
                    error = Strings.message("plugin.manager.install.exists");
                }
            } else
            {
                try
                {
                    FileUtils.copyRec(packageFile, pluginDirectory);
                } catch (IOException e)
                {
                    error = Strings.message("plugin.manager.install.failed");

                    LOG.log(Level.WARNING, error, e);
                }
            }
        } else error = Strings.message("plugin.manager.install.invalid");

        return error;
    }

    /**
     * Uninstalls given packages or schedules it on the next restart.
     *
     * @param packages  packages to uninstall.
     */
    public static void uninstall(Package ... packages)
    {
        for (Package pkg : packages)
        {
            String fn = pkg.getFileName();
            if (!uninstallFilenames.contains(fn)) uninstallFilenames.add(fn);
        }

        updateUninstallFilenamesProperty();
    }

    /**
     * Updates the uninstall filenames property.
     */
    private static void updateUninstallFilenamesProperty()
    {
        prefs.put(KEY_PLUGINS_UNINSTALL,
            StringUtils.join(uninstallFilenames.iterator(),
            PACKAGE_NAME_SEPARATOR));
    }

    /**
     * Removes every package mentioned in the uninstall list.
     */
    private static void doUninstall()
    {
        String[] names = getPackageNames(KEY_PLUGINS_UNINSTALL);
        prefs.remove(KEY_PLUGINS_UNINSTALL);

        for (String name : names)
        {
            File file = new File(pluginDirectory, name);
            if (!file.exists()) continue;
            if (file.isFile()) file.delete(); else FileUtils.rmdir(file);
        }
    }

    private static void doAutoDeployment()
    {
        ClassLoader loader = Manager.class.getClassLoader();
        try
        {
            FilenameFilter pluginFilter = new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return name != null && name.endsWith(".zip");
                }
            };

            String[] pluginNames = { "bb-connect.zip" };

            // Find all existing plug-ins
            File[] deployedFiles = pluginDirectory.listFiles(pluginFilter);

            // Convert the list of files into the map on names to sizes
            Map<String, Long> ntsExisting = new HashMap<String, Long>();
            for (File file : deployedFiles) ntsExisting.put(file.getName(), file.length());

            // Walk through the list of plug-ins to deploy and find new / updated
            List<String> enPackNames = null;
            for (String name : pluginNames)
            {
                String resource = "resources/plug-ins/" + name;
                long size = getResourceSize(loader, resource);

                Long exSize = ntsExisting.get(name);
                if (exSize == null || exSize != size)
                {
                    // New or updated
                    CommonUtils.copyResourceToFile(resource, new File(pluginDirectory, name).getAbsolutePath());

                    // If new -- register as enabled
                    if (exSize == null)
                    {
                        if (enPackNames == null)
                        {
                            List<String> list = Arrays.asList(getPackageNames(KEY_PLUGINS_PACKAGES));
                            enPackNames = new LinkedList<String>(list);
                        }

                        if (!enPackNames.contains(name)) enPackNames.add(name);
                    }
                }
            }

            // If the package names list is initialized, it means there are new plug-ins
            if (enPackNames != null)
            {
                prefs.put(KEY_PLUGINS_PACKAGES, StringUtils.join(enPackNames.iterator(), PACKAGE_NAME_SEPARATOR));
                prefs.putLong(KEY_PLUGINS_PACKAGES_TS, System.currentTimeMillis());
            }
        } catch (Exception e)
        {
            LOG.log(Level.SEVERE, "Couldn't perform auto-deployment.", e);
        }
    }

    /**
     * Returns the length of the resource.
     *
     * @param loader    loader to access the resource.
     * @param resource  resource name.
     *
     * @return length.
     *
     * @throws IOException if data access fails.
     */
    private static long getResourceSize(ClassLoader loader, String resource)
            throws IOException
    {
        URL url = loader.getResource(resource);
        URLConnection con = url.openConnection();
        return (long)con.getContentLength();
    }

    // ------------------------------------------------------------------------
    // Synchronization
    // ------------------------------------------------------------------------

    /**
     * Stores current state into preferences map. Simply transfers the keys.
     *
     * @param preferences preferences.
     */
    public static void storeState(Map<String, Object> preferences)
    {
        String list = StringUtils.join(getPackageNames(KEY_PLUGINS_PACKAGES), PACKAGE_NAME_SEPARATOR);
        if (StringUtils.isNotEmpty(list))
        {
            preferences.put(KEY_PLUGINS_PACKAGES, StringUtils.toUTF8(list));

            // Last change timestamp
            long ts = prefs.getLong(KEY_PLUGINS_PACKAGES_TS, -1);
            if (ts != -1) preferences.put(KEY_PLUGINS_PACKAGES_TS, StringUtils.toUTF8(Long.toString(ts)));
        }
    }

    /**
     * Restores the state from the preferences. Compares the times of key
     * modifications and decides whether to update or not.
     *
     * @param preferences preferences.
     */
    public static void restoreState(Map<String, Object> preferences)
    {
        String list = StringUtils.fromUTF8((byte[])preferences.get(KEY_PLUGINS_PACKAGES));
        if (StringUtils.isNotEmpty(list))
        {
            // Load TS
            long ts = -1;
            String tsS = StringUtils.fromUTF8((byte[])preferences.get(KEY_PLUGINS_PACKAGES_TS));
            if (StringUtils.isNotEmpty(tsS)) ts = Long.parseLong(tsS);

            // Check if local data is more up-to-date
            long localTs = prefs.getLong(KEY_PLUGINS_PACKAGES_TS, -1);
            if (localTs < ts || localTs == -1)
            {
                // It is, update the preference property
                prefs.put(KEY_PLUGINS_PACKAGES, list);

                // Set the last change timestamp to the server-stored
                prefs.putLong(KEY_PLUGINS_PACKAGES_TS, ts);
            }
        }
    }

    // ------------------------------------------------------------------------
    // Private stuff
    // ------------------------------------------------------------------------

    /**
     * Returns the list of all packages mentioned in the preferences and existing
     * in the plug-ins directory.
     *
     * @return packages.
     */
    private static List<File> getPackages()
    {
        List<File> packages = new ArrayList<File>();

        // Get the list of enabled packages and prepend the name
        // of the recovery plug-in
        String[] names = getPackageNames(KEY_PLUGINS_PACKAGES);
        String[] names2 = new String[names.length + 2];
        names2[0] = "bb-recovery";
        names2[1] = "bb-recovery.zip";
        System.arraycopy(names, 0, names2, 2, names.length);

        for (String name : names2)
        {
            File file = new File(pluginDirectory, name);
            if (file.exists()) packages.add(file);
        }

        return packages;
    }

    /**
     * Returns the list of package names read from the preferences by given key.
     *
     * @param key key.
     *
     * @return package names.
     */
    private static String[] getPackageNames(String key)
    {
        String pluginPackageNames = prefs.get(key, "");
        return StringUtils.split(pluginPackageNames, PACKAGE_NAME_SEPARATOR);
    }

    /**
     * Returns <code>TRUE</code> if the file is a valid package (the directory or
     * the archive with package.xml).
     *
     * @param file  file.
     *
     * @return <code>TRUE</code> if the file is a valid package.
     */
    private static boolean isPackage(File file)
    {
        boolean is = false;

        if (file.exists())
        {
            try
            {
                // Evaluate package XML URL
                if (file.isDirectory())
                {
                    is = new File(file, PACKAGE_XML).exists();
                } else if (file.getName().matches(PACKAGE_FILENAME_PATTERN))
                {
                    URL fileURL = file.toURL();
                    ClassLoader loader = new URLClassLoader(new URL[] { fileURL }, Manager.class.getClassLoader());
                    is = loader.getResourceAsStream(PACKAGE_XML) != null;
                }
            } catch (IOException e)
            {
                // Incorrect package
                e.printStackTrace();
            }
        }

        return is;
    }

    /**
     * Makes and attempt to load a package from file.
     *
     * @param packageFile   package file.
     *
     * @return loaded package or <code>NULL</code> if failed.
     */
    private static Package load(File packageFile)
    {
        Package p = null;
        InputStream is = null;

        try
        {
            // Create loader for the package and initialize the stream
            ClassLoader loader = new URLClassLoader(new URL[] { packageFile.toURL() }, Manager.class.getClassLoader());
            is = loader.getResourceAsStream(PACKAGE_XML);

            if (is != null)
            {
                // Parse the descriptor file
                SAXBuilder b = new SAXBuilder(false);
                b.setEntityResolver(EmptyEntityResolver.INSTANCE);
                Document doc = b.build(XmlReaderFactory.create(is));

                // Convert the descriptor into the package
                p = descriptorToPackage(doc, packageFile, loader);
            }
        } catch (Exception e)
        {
            LOG.log(Level.WARNING, "Failed to load plug-in package: " + packageFile, e);
        } finally
        {
            try
            {
                if (is != null) is.close();
            } catch (IOException e)
            {
                // Nothing to do here
            }
        }

        return p;
    }

    /**
     * Converts the descriptor document into package.
     *
     * @param doc           package descriptor document.
     * @param packageFile   package file.
     * @param loader        class loader of the package.
     *
     * @return package.
     *
     * @throws LoaderException if something goes wrong.
     */
    private static Package descriptorToPackage(Document doc, File packageFile, ClassLoader loader)
        throws LoaderException
    {
        Element elPackage = doc.getRootElement();
        if (!NODE_PACKAGE.equals(elPackage.getName())) throw new LoaderException("Wrong root element");

        // Mandatory attributes
        String name = elPackage.getAttributeValue(ATTR_PACKAGE_NAME);
        String desc = elPackage.getAttributeValue(ATTR_PACKAGE_DESCRIPTION);

        if (StringUtils.isEmpty(name)) throw new LoaderException("Package name isn't specified");
        if (StringUtils.isEmpty(desc)) throw new LoaderException("Package description isn't specified");

        Package p = new Package(packageFile.getName(), name, desc,
            elPackage.getAttributeValue(ATTR_PACKAGE_VERSION),
            elPackage.getAttributeValue(ATTR_PACKAGE_AUTHOR),
            elPackage.getAttributeValue(ATTR_PACKAGE_EMAIL));

        List elements = elPackage.getChildren();
        for (Object elementO : elements)
        {
            Element element = (Element)elementO;
            String elName = element.getName();

            IPlugin plugin = null;
            if (NODE_THEME.equals(elName))
            {
                plugin = parseTheme(element, loader);
            } else if (NODE_ACTIONS.equals(elName))
            {
                plugin = parseActions(element, loader);
            } else if (NODE_RESOURCES.equals(elName))
            {
                plugin = parseResources(element, loader);
            } else if (NODE_STRINGS.equals(elName))
            {
                plugin = parseStrings(element, loader);
            } else if (NODE_PREFERENCES.equals(elName))
            {
                plugin = parsePreferences(element);
            } else if (NODE_SMARTFEED.equals(elName))
            {
                plugin = parseSmartFeed(element, loader);
            } else if (NODE_CODE.equals(elName))
            {
                plugin = parseCode(element, loader);
            } else if (NODE_TOOLBAR.equals(elName))
            {
                plugin = parseToolbar(element);
            }

            if (plugin != null) p.add(plugin);
        }

        return p;
    }

    /**
     * Loads theme plug-in.
     *
     * @param element   theme element list.
     * @param loader    the class loader to use for the resource access.
     *
     * @return the plug-in;
     */
    private static IPlugin parseTheme(Element element, ClassLoader loader)
    {
        IPlugin tp = null;

        try
        {
            tp = ThemePlugin.create(element, loader);
        } catch (LoaderException e)
        {
            LOG.log(Level.WARNING, "Failed to load a theme", e);
        }

        return tp;
    }

    /**
     * Parses actions element.
     *
     * @param element   element.
     * @param loader    the class loader to use for the resource access.
     *
     * @return plugin.
     */
    private static IPlugin parseActions(Element element, ClassLoader loader)
    {
        ActionsPlugin pl = null;

        try
        {
            pl = new ActionsPlugin(element, loader);
        } catch (IllegalArgumentException e)
        {
            LOG.log(Level.WARNING, "Failed to create plug-in.", e);
        }

        return pl;
    }

    /**
     * Parses resources element.
     *
     * @param element   element.
     * @param loader    the class loader to use for the resource access.
     *
     * @return plugin.
     */
    private static IPlugin parseResources(Element element, ClassLoader loader)
    {
        ResourcesPlugin pl = null;

        try
        {
            pl = new ResourcesPlugin(element, loader);
        } catch (IllegalArgumentException e)
        {
            LOG.log(Level.WARNING, "Failed to create plug-in.", e);
        }

        return pl;
    }

    /**
     * Parses strings element.
     *
     * @param element   element.
     * @param loader    the class loader to use for the resource access.
     *
     * @return plugin.
     */
    private static IPlugin parseStrings(Element element, ClassLoader loader)
    {
        StringsPlugin pl = null;

        try
        {
            pl = new StringsPlugin(element, loader);
        } catch (IllegalArgumentException e)
        {
            LOG.log(Level.WARNING, "Failed to create plug-in.", e);
        }

        return pl;
    }

    /**
     * Parses smartfeed element.
     *
     * @param element   element.
     * @param loader    the class loader to use for the resource access.
     *
     * @return plugin.
     */
    private static IPlugin parseSmartFeed(Element element, ClassLoader loader)
    {
        IPlugin pl = null;

        try
        {
            pl = SmartFeedPlugin.create(element, loader);
        } catch (LoaderException e)
        {
            LOG.log(Level.WARNING, "Failed to load a smart feed plug-in", e);
        }

        return pl;
    }

    /**
     * Creates and returns advanced preferences plug-in.
     *
     * @param element element.
     *
     * @return plug-in.
     */
    private static IPlugin parsePreferences(Element element)
    {
        return new AdvancedPreferencesPlugin(element);
    }

    /**
     * Parses code element.
     *
     * @param element   element.
     * @param loader    the class loader to use for the resource access.
     *
     * @return plugin.
     */
    private static IPlugin parseCode(Element element, ClassLoader loader)
    {
        IPlugin pl = null;

        try
        {
            pl = CodePlugin.create(element, loader);
        } catch (LoaderException e)
        {
            LOG.log(Level.WARNING, "Failed to load a code plug-in", e);
        }

        return pl;
    }

    /**
     * Parses the plug-in element.
     *
     * @param element element.
     *
     * @return toolbar plug-in.
     */
    private static IPlugin parseToolbar(Element element)
    {
        IPlugin pl = null;

        try
        {
            pl = new ToolbarPlugin(element);
        } catch (Exception e)
        {
            LOG.log(Level.WARNING, "Failed to load a code plug-in", e);
        }

        return pl;
    }
}
