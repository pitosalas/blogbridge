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
// $Id: OSXSupport.java,v 1.21 2007/10/01 17:03:27 spyromus Exp $
//

package com.salas.bb.utils.osx;

import com.jgoodies.uif.osx.OSXApplicationMenu;
import com.jgoodies.uif.util.SystemUtils;
import com.salas.bb.utils.apple.AppleApplication;
import com.salas.bb.utils.apple.IAppleApplicationListener;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.ipc.IPC;
import com.salas.bb.utils.uif.laf.MacLookAndFeel;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.ProtectionDomain;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Interface layer to Mac OSX. Contains both local functionality and interfaces to JGoodies OSX
 * Support. Utility.
 */
public final class OSXSupport
{
    private static final Logger LOG = Logger.getLogger(OSXSupport.class.getName());

    /**
     * Width of splitter pane.
     */
    public static final int SPLITPANE_WIDTH = 8;
    private static ClassLoader cocoaClassLoader;

    /**
     * Hidden constructor of utility class.
     */
    private OSXSupport()
    {
    }

    /**
     * Set the name of the Application Menu. 
     *
     * @param aString - Application menu
     */
    public static void setAboutMenuName(String aString)
    {
        if (SystemUtils.IS_OS_MAC) OSXApplicationMenu.setAboutName(aString);
    }

    /**
     * Tell OSX that you want the main window to be Metal. N.B. It is crucial that this method is
     * called before any Swing related code is run. This includes even 'new'ing a class derived from
     * JFrame. If it looks like this method isn't doing anything, it's probably because it has been
     * called too late.
     */
    public static void setMetalOSXLook()
    {
        System.setProperty("apple.awt.brushMetalLook", "true");
    }

    /**
     * Declare the ActionListeners to be invoked when the indicated commands picked off the
     * Application menu of OSX. Note: noop if we are not on a mac.
     *
     * @param about About action
     * @param prefs Show Preferences action
     * @param exit  Quit Application action
     */
    public static void setApplicationMenu(ActionListener about, ActionListener prefs,
                                          ActionListener exit)
    {
        if (SystemUtils.IS_OS_MAC) OSXApplicationMenu.register(about, prefs, exit);
    }

    /**
     * Initializes Mac-specific LAF.
     */
    public static void setupLAF()
    {
        if (!SystemUtils.IS_OS_MAC) return;

        try
        {
            UIManager.setLookAndFeel(new MacLookAndFeel());
        } catch (Exception e)
        {
            LOG.log(Level.SEVERE, Strings.error("unhandled.exception"), e);
        }
    }

    /**
     * Returns Cocoa class by name.
     *
     * @param name name of the class (i.e. <code>com.apple.cocoa.application.NSImage</code>).
     *
     * @return class.
     *
     * @throws ClassNotFoundException if class not found.
     */
    public static Class getCocoaClass(String name)
        throws ClassNotFoundException
    {
        if (!SystemUtils.IS_OS_MAC) return null;
        return getCocoaClassLoader().loadClass(name);
    }

    /**
     * Returns classloader for Cocoa classes.
     *
     * @return class loader.
     */
    public static synchronized ClassLoader getCocoaClassLoader()
    {
        if (!SystemUtils.IS_OS_MAC) return null;
        if (cocoaClassLoader == null)
        {
            // Setup Cocoa Class Loader plus Growl delegate which should be on the
            // same class loader with the rest of Cocoa tools
            ClassLoader currentCL = OSXSupport.class.getClassLoader();
            URL urlCocoa = null;
            try
            {
                urlCocoa = new URL("file:///System/Library/Java/");
            } catch (MalformedURLException e)
            {
                // Impossible
            }
            ClassLoader cl = new AllPermissionsURLClassLoader(new URL[] { urlCocoa }, currentCL);
            cocoaClassLoader = new ResourceClassLoader("resources/growl", cl);
        }

        return cocoaClassLoader;
    }

    /**
     * Loads resource into the array of bytes.
     *
     * @param resource resource to load.
     *
     * @return array of bytes or <code>NULL</code> if not found.
     */
    public static byte[] loadResourceBytes(String resource)
    {
        byte[] buffer = null;

        InputStream stream = OSXSupport.class.getClassLoader().getResourceAsStream(resource);
        if (stream != null)
        {
            BufferedInputStream bis = new BufferedInputStream(stream);
            try
            {
                buffer = new byte[bis.available()];
                bis.read(buffer);
            } catch (IOException e)
            {
                buffer = null;
                LOG.log(Level.SEVERE, Strings.error("notify.failed.to.load.resource"), e);
            } finally
            {
                try { bis.close(); } catch (IOException e) { }
            }
        } else LOG.log(Level.SEVERE, MessageFormat.format(Strings.error("resource.not.found"), new Object[] { resource }));

        return buffer;
    }

    /**
     * Sets the application icon.
     */
    public static void setApplicationIcon()
    {
        DockIcon.setApplicationIcon("blogbridge.icns");
    }

    /**
     * Configures Mac-specific IPC.
     *
     * @param ipc IPC module.
     */
    public static void setupIPC(final IPC ipc)
    {
        AppleApplication.addApplicationListener(new IAppleApplicationListener()
        {
            /**
             * Invoked when Finder gives the command to open some file.
             *
             * @param filename file name.
             *
             * @return <code>TRUE</code> if the action was performed.
             */
            public boolean handleOpenFile(String filename)
            {
                if (filename == null) return false;

                URL u = null;

                // Check if it's URL
                try
                {
                    u = new URL(filename);
                } catch (MalformedURLException e)
                {
                    // Not URL
                }

                // Check if it's filename
                if (u == null)
                {
                    File f = new File(filename);
                    try
                    {
                        u = f.toURL();
                    } catch (MalformedURLException e)
                    {
                        // Not filename
                    }
                }

                if (u != null) ipc.fireSubscribe(u);

                return u == null;
            }
        });
    }

    /**
     * The class loader which is taking classes from the resources.
     */
    private static class ResourceClassLoader extends ClassLoader
    {
        private final Map classes = new HashMap();
        private final String root;

        /**
         * Creates a new class loader.
         *
         * @param root offset from the resources root to start loading classes from.
         * @param parent parent class loader.
         */
        public ResourceClassLoader(String root, ClassLoader parent)
        {
            super(parent);
            this.root = root;
        }

        /**
         * Loads the class with the specified name.  This method searches for
         * classes in the same manner as the {@link #loadClass(String, boolean)}
         * method.  It is invoked by the Java virtual machine to resolveURI class
         * references.  Invoking this method is equivalent to invoking {@link
         * #loadClass(String, boolean) <tt>loadClass(name, false)</tt>}.  </p>
         *
         * @param name The name of the class
         * @return The resulting <tt>Class</tt> object
         * @throws ClassNotFoundException If the class was not found
         */
        public Class loadClass(String name) throws ClassNotFoundException
        {
            return loadClass(name, true);
        }

        /**
         * Loads the class with the specified name.  The default implementation
         * of this method searches for classes in the following order:
         * <p/>
         * <p><ol>
         * <p/>
         * <li><p> Invoke {@link #findLoadedClass(String)} to check if the class
         * has already been loaded.  </p></li>
         * <p/>
         * <li><p> Invoke the {@link #loadClass(String) <tt>loadClass</tt>} method
         * on the parent class loader.  If the parent is <tt>null</tt> the class
         * loader built-in to the virtual machine is used, instead.  </p></li>
         * <p/>
         * <li><p> Invoke the {@link #findClass(String)} method to find the
         * class.  </p></li>
         * <p/>
         * </ol>
         * <p/>
         * <p> If the class was found using the above steps, and the
         * <tt>resolveURI</tt> flag is true, this method will then invoke the {@link
         * #resolveClass(Class)} method on the resulting <tt>Class</tt> object.
         * <p/>
         * <p> Subclasses of <tt>ClassLoader</tt> are encouraged to override {@link
         * #findClass(String)}, rather than this method.  </p>
         *
         * @param name    The name of the class
         * @param resolve If <tt>true</tt> then resolveURI the class
         * @return The resulting <tt>Class</tt> object
         * @throws ClassNotFoundException If the class could not be found
         */
        protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException
        {
            Class result;

            result = (Class)classes.get(name);
            if (result == null)
            {
                try
                {
                    result = super.loadClass(name, resolve);
                } catch (ClassNotFoundException e)
                {
                    // Not a parent class-loader class
                }

                if (result == null)
                {
                    byte[] classBytes = loadClassBytes(name);
                    if (classBytes != null)
                    {
                        ProtectionDomain protectionDomain = ResourceClassLoader.class.getProtectionDomain();
                        result = defineClass(name, classBytes, 0, classBytes.length, protectionDomain);
                        if (result == null) throw new ClassFormatError(name);

                        // Put class in cache
                        classes.put(name, result);
                    }
                }
            }

            return result;
        }

        /**
         * Loads class bytes.
         *
         * @param name  name of the class.
         *
         * @return bytes.
         *
         * @throws ClassNotFoundException if class couldn't be found.
         */
        private byte[] loadClassBytes(String name) throws ClassNotFoundException
        {
            String resource = root + "/" + name.replace('.', '/') + ".class";

            byte[] buffer = loadResourceBytes(resource);
            if (buffer == null || buffer.length == 0) throw new ClassNotFoundException(name);

            return buffer;
        }
    }

    /**
     * URL class loader setting all permissions to the classes it loads.
     */
    private static class AllPermissionsURLClassLoader extends URLClassLoader
    {
        private PermissionCollection pc;

        /**
         * Creates loader.
         *
         * @param urls the list of URLs to treat as roots.
         * @param parent parent class loader.
         */
        public AllPermissionsURLClassLoader(URL[] urls, ClassLoader parent)
        {
            super(urls, parent);

            AllPermission ap = new AllPermission();
            pc = ap.newPermissionCollection();
            pc.add(ap);
        }

        /**
         * Returns the permissions for the code source.
         *
         * @param codesource code source.
         *
         * @return permissions.
         */
        protected PermissionCollection getPermissions(CodeSource codesource)
        {
            return pc;
        }
    }
}