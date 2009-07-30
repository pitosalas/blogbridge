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
// $Id: ApplicationLauncher.java,v 1.240 2008/12/25 07:49:29 spyromus Exp $
//

package com.salas.bb.core;

import com.jgoodies.looks.*;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.uif.AbstractFrame;
import com.jgoodies.uif.application.Application;
import com.jgoodies.uif.application.ApplicationConfiguration;
import com.jgoodies.uif.application.ApplicationDescription;
import com.jgoodies.uif.application.ResourceIDs;
import com.jgoodies.uif.splash.ImageSplash;
import com.jgoodies.uif.splash.Splash;
import com.jgoodies.uif.splash.SplashProvider;
import com.jgoodies.uif.util.ResourceUtils;
import com.jgoodies.uif.util.SystemUtils;
import com.jgoodies.uifextras.convenience.DefaultApplicationStarter;
import com.jgoodies.uifextras.convenience.SetupManager;
import com.limegroup.gnutella.gui.GURLHandler;
import com.salas.bb.core.actions.ActionsTable;
import com.salas.bb.core.actions.EDTLockupHandler;
import com.salas.bb.core.actions.EDTOverloadReporter;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.installation.Installer;
import com.salas.bb.installation.wizard.WelcomePage;
import com.salas.bb.persistence.IPersistenceManager;
import com.salas.bb.persistence.PersistenceException;
import com.salas.bb.persistence.PersistenceManagerConfig;
import com.salas.bb.plugins.Manager;
import com.salas.bb.service.ServicePreferences;
import com.salas.bb.utils.*;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.ipc.IPC;
import com.salas.bb.utils.locker.Locker;
import com.salas.bb.utils.net.auth.CachingAuthenticator;
import com.salas.bb.utils.net.auth.IPasswordsRepository;
import com.salas.bb.utils.osx.OSXSupport;
import com.salas.bb.utils.uif.NoFlickerSplashWrapper;
import com.salas.bb.utils.uif.TipOfTheDay;
import com.salas.bb.utils.uif.UifUtilities;
import com.salas.bb.utils.uif.images.Cache;
import com.salas.bb.utils.uif.images.ImageFetcher;
import com.salas.bb.utils.watchdogs.EventQueueWithWD;
import com.salas.bb.views.mainframe.MainFrame;
import com.salas.bb.views.stylesheets.StylesheetManager;
import com.salas.bbutilities.VersionUtils;
import org.apache.xmlrpc.XmlRpc;
import sun.net.NetworkClient;

import javax.net.ssl.*;
import javax.swing.*;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.URL;
import java.nio.channels.FileLock;
import java.security.Permission;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * This is the class that starts and runs everything in Blog Bridge.
 */
public class ApplicationLauncher extends DefaultApplicationStarter 
{
    public static boolean proVersion = false;

    private static final Logger LOG = Logger.getLogger(ApplicationLauncher.class.getName());

    private static final String MINIMAL_COMPATIBLE_VERSION  = "1.9";

    /** Current version. */
    private static final String CURRENT_VERSION             = "6.7";

    private static final int DEFAULT_ICON_WIDTH             = 18;
    private static final int DEFAULT_ICON_HEIGHT            = 18;
    private static final int TOOLBAR_SEP_WIDTH              = 6;
    private static final int TOOLBAR_SEP_HEIGHT             = 18;

    private static final int LOGGING_LIMIT                  = 50000;
    private static final int LOGGING_LOOP_COUNT             = 5;

    /**
     * Properties key for installation ID.
     * @noinspection HardCodedStringLiteral
     */
    public static final String KEY_INSTALLATION_ID = "InstallationID";

    /**
     * Properties key for installation version.
     * @noinspection HardCodedStringLiteral
     */
    public static final String KEY_INSTALLATION_VERSION = "InstallationVersion";

    /**
     * Properties key for number of runs.
     * @noinspection HardCodedStringLiteral
     */
    private static final String KEY_RUNS = "Runs";

    /**
     * Report errors flag. System property key.
     * @noinspection HardCodedStringLiteral
     */
    private static final String KEY_REPORT_ERRORS = "report.errors";

    /**
     * Logging config file.
     * @noinspection HardCodedStringLiteral
     */
    private static final String KEY_LOGGING_CONFIG_FILE = "java.util.logging.config.file";

    /**
     * Bundled logging properties resource.
     * @noinspection HardCodedStringLiteral
     */
    private static final String RESOURCE_LOGGING_PROPERTIES = "logging.properties";

    /**
     * Weekly release type name.
     * @noinspection HardCodedStringLiteral
     */
    private static final String RELEASE_TYPE_WEEKLY = "weekly";

    /** Name of backups directory within user's BB directory. */
    private static final String BACKUPS_DIR_NAME = "backups";

    private static final String MSG_SPLASH_ENSURE_ERROR = "Wasn't able to manipulate splash screen visiblity.";
    private static final String MSG_PARENT_APP_LAUNCHER_ERROR = "Failed to call parent app launcher.";

    private static ApplicationLauncher globalAppLauncher;

    private static String releaseType;
    private static String prefix;
    private static boolean usingFinalData;

    private static ApplicationDescription description;
    private static ApplicationConfiguration configuration;

    private static ConnectionState connectionState;
    private static String contextPath;

    private static FileLock instanceLock;
    private static IPC ipc;
    private static File socketFile;
    private static String urlToOpen;

    static
    {
        connectionState = new ConnectionState();
    }

    /**
     * Returns connection state object.
     *
     * @return connection state object.
     */
    public static ConnectionState getConnectionState()
    {
        return connectionState;
    }

    /**
     * Returns the URL to open when the initialization is finished.
     *
     * @return URL specified in the command-line or <code>NULL</code>.
     */
    public static String getURLToOpen()
    {
        return urlToOpen;
    }

    /**
     * Configure LOG handling to send all Log messages of Warning or worse to the default LOG file.
     * 1. if -D java.util.logging.config.file is set, use that as the log configuration
     * 2. if not then locate logging.properties in classpath otherwise error.
     * <p/>
     * Keep LOGGING_LOOP_COUNT console.* files around
     */
    protected void configureLogging()
    {
        // Read configuration from specified file or default resource
        LogManager lm = LogManager.getLogManager();
        String fname = System.getProperty(KEY_LOGGING_CONFIG_FILE);

        boolean inited = false;
        if (fname != null && new File(fname).exists())
        {
            try
            {
                // default configuration reader will examine the property and load configuration
                lm.readConfiguration();
                inited = true;
            } catch (IOException e)
            {
                System.err.println(Strings.error("logging.using.overriden.configuration.errored"));
                e.printStackTrace();
            }
        }

        // If configuration still not read use internal production configuration
        if (!inited)
        {
            final InputStream url =
                this.getClass().getClassLoader().getResourceAsStream(RESOURCE_LOGGING_PROPERTIES);
            try
            {
                lm.readConfiguration(url);
            } catch (IOException e)
            {
                System.err.println(Strings.error("logging.unable.to.use.production.configuration"));
                e.printStackTrace();
            }
        }

        Logger aLogger = Logger.getLogger("");

        // Setup file logger
        try
        {
            String pattern = getDefaultLogFilePattern();
            ensureParentDirectoryExists(pattern);
            FileHandler handler = new FileHandler(pattern, LOGGING_LIMIT, LOGGING_LOOP_COUNT);
            handler.setFormatter(new TinyFormatter());
            aLogger.addHandler(handler);
        } catch (IOException e)
        {
            System.err.println(MessageFormat.format(Strings.error("logging.failed.to.configure"),
                e.getLocalizedMessage()));
        }

        // Setup reporting logger (can't use logging.properties as this handler is not
        // on the system path under JWS) :(
        if (System.getProperty(KEY_REPORT_ERRORS) != null)
        {
            ReportingLogHandler reportingHandler = new ReportingLogHandler();
            reportingHandler.setLevel(Level.SEVERE);
            aLogger.addHandler(reportingHandler);
        }
    }

    /**
     * Returns <code>TRUE</code> if release type is weekly.
     *
     * @return <code>TRUE</code> if release type is weekly.
     */
    public static boolean isWeekly()
    {
        return RELEASE_TYPE_WEEKLY.equalsIgnoreCase(releaseType);
    }

    /**
     * Returns <code>TRUE</code> if this build uses final data directory.
     *
     * @return <code>TRUE</code> if this build uses final data directory.
     */
    public static boolean isUsingFinalData()
    {
        return usingFinalData;
    }

    /**
     * Returns <code>TRUE</code> if auto-updates features should be enabled.
     *
     * @return <code>TRUE</code> if auto-updates features should be enabled.
     */
    public static boolean isAutoUpdatesEnabled()
    {
        return usingFinalData && !BrowserLauncher.isRunningUnderJWS();
    }

    /**
     * Returns the type of release (weekly, stable, or final).
     *
     * @return the type of release.
     */
    public static String getReleaseType()
    {
        return releaseType;
    }

    /**
     * Returns prefix (working folder path relative to user home).
     *
     * @return prefix.
     */
    public static String getPrefix()
    {
        return prefix;
    }

    /**
     * Configure Log Handling so that we never display a Message Box via logger calls.
     */
    protected void addMessageHandler()
    {
    }

    /**
     * UIF Framework call to create Main window frame during initialization.
     *
     * @return Main Window Frame.
     */
    protected AbstractFrame createMainFrame()
    {
        MainFrame mainFrame = new MainFrame(connectionState);

        Application.setDefaultParentFrame(mainFrame);
        GlobalController.SINGLETON.setMainFrame(mainFrame);

        return mainFrame;
    }

    /**
     * Once only initialization for the whole application.
     */
    protected void initializeActions()
    {
        Splash.setNote(Strings.message("startup.registering.actions"), 60);
        ActionsTable.getInstance().registerActions(connectionState);
    }

    /**
     * Configuring general networking.
     */
    private void configureNetwork()
    {
        // Configure XML-RPC package to use Unicode
        XmlRpc.setEncoding("UTF-8");

        ApplicationDescription description = Application.getDescription();
        System.setProperty("http.agent", description.getProductText() +
            " (" + description.getVendorURL() + ") " + System.getProperty("java.version"));
        System.setProperty("http.agent.discoverer", description.getProductText() + " Discoverer" +
            " (" + description.getVendorURL() + ") " + System.getProperty("java.version"));

        // Create and initialize caching authenticator
        IPersistenceManager persistenceManager = PersistenceManagerConfig.getManager();
        IPasswordsRepository passwordsRepository = persistenceManager.getPasswordsRepository();
        Authenticator.setDefault(new CachingAuthenticator(passwordsRepository));

        // This is a very kludgy way to propagate default reading and connection timeouts.
        // It's the only way for now (JRE 1.4.x) to let this values be used when running under JWS.
        // NOTE: NetworkClient is not part of public API and can be removed/changed in a future.
        new NetworkClient()
        {
            {
                Integer readTimeoutInt = Integer.getInteger("sun.net.client.defaultReadTimeout");
                defaultSoTimeout = (readTimeoutInt == null) ? -1 : readTimeoutInt;

                Integer connectTimeoutInt = Integer.getInteger("sun.net.client.defaultConnectTimeout");
                defaultConnectTimeout = (connectTimeoutInt == null) ? -1 : connectTimeoutInt;
            }
        };

        // Sets proxy selector to use for communication
        ProxySelector.setDefault(CustomProxySelector.INSTANCE);
    }

    /**
     * Disables verification of host name.
     */
    private void disableSSLHostNameVerification()
    {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier()
        {
            public boolean verify(String string, SSLSession sslSession)
            {
                return true;
            }
        });
    }

    /**
     * Disables verification of SSL certificate.
     */
    private void disableSSLCertificates()
    {
        // Create a trust manager that does not validate certificate chains
       TrustManager[] trustAllCerts = new TrustManager[]
       {
           new X509TrustManager()
           {
               public java.security.cert.X509Certificate[] getAcceptedIssuers()
               {
                   return null;
               }

               public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
                                              String authType)
               {
               }

               public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
                                              String authType)
               {
               }
           }
       };

       // Install the all-trusting trust manager
       try
       {
           SSLContext sc = SSLContext.getInstance("SSL");
           sc.init(null, trustAllCerts, new java.security.SecureRandom());
           HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
       } catch (Exception e)
       {
           if (LOG.isLoggable(Level.WARNING))
           {
               LOG.log(Level.WARNING, Strings.error("ssl.probably.certificates.validation.was.not.disabled"), e);
           }
       }
    }

    // Install alternative event queue and necessary performance timers
    private void installWatchdogs()
    {
        final String watchdogsDisable = System.getProperty("watchdogs.disable");
        if (watchdogsDisable == null || !watchdogsDisable.equalsIgnoreCase("true"))
        {
            EventQueueWithWD queue = EventQueueWithWD.install();

            queue.addWatchdog(2000, new EDTOverloadReporter(Level.WARNING), false);
            queue.addWatchdog(3 * 60 * 1000, new EDTLockupHandler(), true);
        }
    }

    /** Install our custom security manager. */
    private void configureSecurityManager()
    {
        try
        {
            System.setSecurityManager(new SecurityManager()
            {
                /**
                 * Throws a <code>SecurityException</code> if the requested
                 * access, specified by the given permission, is not permitted based
                 * on the security policy currently in effect.
                 *
                 * @param perm the requested permission.
                 */
                public void checkPermission(Permission perm)
                {
                    // This implementation allows all connections to any hosts.
                    // It's necessary to fix the unknown problem with SecurityException
                    // raising when trying to fetch some images (see BT #247 GUI: Gizmodo images)
                }

                /**
                 * Throws a <code>SecurityException</code> if the
                 * specified security context is denied access to the resource
                 * specified by the given permission.
                 *
                 * @param perm    the specified permission
                 * @param context a system-dependent security context.
                 */
                public void checkPermission(Permission perm, Object context)
                {
                    // This implementation allows all connections to any hosts.
                    // It's necessary to fix the unknown problem with SecurityException
                    // raising when trying to fetch some images (see BT #247 GUI: Gizmodo images)
                }
            });
        } catch (SecurityException e)
        {
            // If we are unable to reinstall the SecurityManager, then it's better to report it
            LOG.log(Level.SEVERE, "Unable to install the security manager.", e);
        }
    }

    
    /** Configures GUI part. */
    protected void configureUI()
    {
        if (LOG.isLoggable(Level.FINE)) LOG.fine("configureUI starting...");

        // Configure displaying anti-aliased text (SHOULD BE RUN BEFORE SWING INIT)
        configureAntiAliasing();

        // Configure Mac OS X dock icon (we had problems with JWS dock icon: ugly and cannot be badged)
        OSXSupport.setApplicationIcon();

        // Images cache
        Cache imagesCache = new Cache(new File(getContextPath() + "cache"), 20000000);
        ImageFetcher.setCache(imagesCache);

        // Stylesheets
        try
        {
            URL baseStylesheetURL = new URL("http://www.blogbridge.com/bbstyles/");
            StylesheetManager.init(new File(getContextPath() + "stylesheets"), baseStylesheetURL);
        } catch (MalformedURLException e)
        {
            // Impossible to have
        }

        Options.setDefaultIconSize(new Dimension(DEFAULT_ICON_WIDTH, DEFAULT_ICON_HEIGHT));
        UIManager.put("ToolBar.separatorSize",
            new DimensionUIResource(TOOLBAR_SEP_WIDTH, TOOLBAR_SEP_HEIGHT));
        Options.setPopupDropShadowEnabled(true);

        // In Java Web Start, indicate where to find the l&f classes. Set the Swing class loader.
        UIManager.put("ClassLoader", LookUtils.class.getClassLoader());

        // Make Swing update everything dynamically during resize operations.
        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        super.configureUI();
        configureFonts();

        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine("configureUI done...");
        }

        OSXSupport.setupLAF();

        // Custom icons
        UIManager.getLookAndFeelDefaults().put("html.missingImage",
            new UIDefaults.LazyValue() {
                /**
                 * Creates the actual value retrieved from the <code>UIDefaults</code> table. When an object that implements
                 * this interface is retrieved from the table, this method is used to create the real value, which is then
                 * stored in the table and returned to the calling method.
                 *
                 * @param table a <code>UIDefaults</code> table
                 *
                 * @return the created <code>Object</code>
                 */
                public Object createValue(UIDefaults table)
                {
                    return ResourceUtils.getIcon("application.icon");
                }
            });
    }

    /**
     * Configures fonts.
     */
    private void configureFonts()
    {
        Options.setUseSystemFonts(true);

        if (SystemUtils.IS_OS_LINUX)
            PlasticLookAndFeel.setFontPolicy(new SmallPlasticFontPolicy());
    }

    /**
     * Configures the splash component: reads the splash image, then opens an ImageSplash.
     */
    protected void configureSplash()
    {
        // Create image splash
        Image image = ResourceUtils.getIcon(ResourceIDs.SPLASH_IMAGE).getImage();
        ImageSplash imageSplash = new ImageSplash(image, true);
        imageSplash.setNoteEnabled(true);

        // Wrap with de-flicker "filter"
        SplashProvider splashWrapper = new NoFlickerSplashWrapper(imageSplash);
        Splash.setProvider(splashWrapper);
    }

    private static boolean isMac()
    {
        String osName = System.getProperty("os.name");
        return osName != null && osName.startsWith("Mac OS");
    }

    /**
     * Use the lovely JGoodies booting mechanism to get the thing launched.
     *
     * @param arguments standard main parameters.
     */
    public static void main(String[] arguments)
    {
        if (isMac()) GURLHandler.getInstance().register();

        initReleaseTypeAndPrefix(arguments, System.getProperty("release.type"),
            System.getProperty("working.folder"));

        overrideLocale();

        String urlToOpen = checkCommandLineForURL(arguments);

        socketFile = new File(getContextPath() + "/application.sock");

        File lockerFile = new File(getContextPath() + "/application.lock");
        instanceLock = Locker.tryLocking(lockerFile);

        if (instanceLock != null)
        {
            proVersion = System.getProperty("pro") != null;
            globalAppLauncher = new ApplicationLauncher();
            globalAppLauncher.boot(getDescription(), getConfiguration());

            // A few Mac Specific things that have to be done really early
            OSXSupport.setAboutMenuName("BlogBridge");
            ApplicationLauncher.urlToOpen = urlToOpen;
        } else if (urlToOpen != null)
        {
            IPC.sendCommand(socketFile, "subscribe", new String[] { urlToOpen });
            System.exit(0);
        } else
        {
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(),
                Strings.message("startup.only.one.instance.allowed"),
                "BlogBridge", JOptionPane.WARNING_MESSAGE);
            System.exit(1);
        }
    }

    /**
     * Overrides system locale with English if the flag is set to use English.
     */
    private static void overrideLocale()
    {
        boolean alwaysUseEnglish = Preferences.userRoot().node(prefix).getBoolean(UserPreferences.PROP_ALWAYS_USE_ENGLISH,
            UserPreferences.DEFAULT_ALWAYS_USE_ENGLISH);

        if (alwaysUseEnglish) Locale.setDefault(new Locale("en"));
    }

    /**
     * Gets URL from the command line.
     *
     * @param arguments arguments.
     *
     * @return URL or <code>NULL</code>.
     */
    static String checkCommandLineForURL(String[] arguments)
    {
        String urlStr = null;

        for (int i = 0; urlStr == null && i < arguments.length; i++)
        {
            String arg = arguments[i];
            if ("-open".equals(arg) && arguments.length > i)
            {
                arg = arguments[++i];
            }

            URL url = IPC.argToURL(StringUtils.cleanDraggedURL(arg));
            if (url != null) urlStr = url.toString();
        }

        return urlStr;
    }

    static void initReleaseTypeAndPrefix(String[] arguments, String propReleaseType,
                                         String propWorkingFolder)
    {
        String relType = propReleaseType;
        String pref = propWorkingFolder;

        if (pref != null)
        {
            pref = "bb/" + pref;
            if (relType == null) relType = pref;
        } else
        {
            if (arguments.length > 0)
            {
                pref = arguments[0];

                int i = pref.indexOf('/');
                if (i > -1) relType = pref.substring(i + 1).trim();

                if (relType == null || relType.length() == 0) relType = "Final";
            } else
            {
                pref = "bb/final";
                relType = "Final";
            }
        }

        releaseType = StringUtils.capitalise(relType);
        prefix = pref;
        usingFinalData = "bb/final".equalsIgnoreCase(prefix);
    }

    /**
     * Transfers anti-aliasing property from user preferences to the Swing-known place.
     */
    private static void configureAntiAliasing()
    {
        Preferences prefs = Application.getUserPreferences();
        boolean aaText = prefs.getBoolean(UserPreferences.PROP_AA_TEXT, false);
        System.getProperties().put("swing.aatext", Boolean.toString(aaText));
    }

    /**
     * Creates application description object.
     *
     * @return application description.
     */
    private static synchronized ApplicationDescription getDescription()
    {
        if (description == null)
        {
            description = new ApplicationDescription(
                "BlogBridge",                    // Application short name
                "BlogBridge",                    // Application long name
                CURRENT_VERSION,                 // Version
                CURRENT_VERSION + " - " + getMonthYear(), // Full version
                Strings.message("appdescriptor.description"),     // Description
                "\u00a9 " + getYear() + " Salas Associates, All Rights Reserved.", // Copyright
                "Salas Associates, Inc.",        // Vendor
                "http://www.blogbridge.com/",    // Vendor URL
                "info@blogbridge.com");          // Vendor email
        }

        return description;
    }

    /**
     * Returns current month and year. For example, "Aug 2005".
     *
     * @return month and year.
     */
    private static String getMonthYear()
    {
        return new SimpleDateFormat("MMM yyyy").format(new Date());
    }

    /**
     * Returns current year.
     *
     * @return year.
     */
    private static String getYear()
    {
        return new SimpleDateFormat("yyyy").format(new Date());
    }

    /**
     * Return JGoodies UIF ApplicationConfiguration for specified prefix. The prefix is used as a
     * subdirectory under the .bb directory to have separate persistent areas, and is also used as
     * a prefix node in the Preferences (registery) to keep those separate as well.
     *
     * @return application configuration object.
     */
    public static synchronized ApplicationConfiguration getConfiguration()
    {
        if (configuration == null)
        {
            configuration = new ApplicationConfiguration(
                prefix,                  // Root node for prefs and logs
                "",                      // resource.properties URL
                "docs/Help.hs",          // Helpset URL
                "docs/tips/index.txt");  // Tips index path
        }

        return configuration;
    }

    /**
     * Configures the <code>SetupManager</code>. The default does nothing. Sublcasses can, for
     * example modify the welcome panel.
     */
    protected void configureSetupManager()
    {
        SetupManager.setWelcomePanel(new WelcomePage(null, null));
    }

    /**
     * Works that needs to be done *after* MainFrame has been displayed.
     */
    protected void launchApplication()
    {
        incrementInstallationRuns();

        com.salas.bbutilities.opml.export.Exporter.setGenerator("OPML generated by BlogBridge " + CURRENT_VERSION +
            " (http://www.blogbridge.com/)");

        configureSecurityManager();

        // Start plug-ins
        Splash.setNote(Strings.message("startup.loading.plugins"), 2);
        File f = new File(getContextPath(), "plugins");
        Manager.initialize(f, Application.getUserPreferences());
        Manager.loadPackages();

        disableSSLHostNameVerification();
        disableSSLCertificates();

        Splash.setNote(Strings.message("startup.starting.ipc"), 10);
        configureIPC();

        Splash.setNote(Strings.message("startup.configuring.network"), 12);
        configureNetwork();

        Splash.setNote(Strings.message("startup.installing.performance.timers"), 15);
        installWatchdogs();

        Splash.setNote(Strings.message("startup.checking.model"), 20);
        GlobalModel newVersionModel = checkForNewVersion();

        GlobalController.SINGLETON.restorePreferences();

        UifUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                ApplicationLauncher.super.launchApplication();
            }
        }, MSG_PARENT_APP_LAUNCHER_ERROR, Level.SEVERE);

        Splash.setNote(Strings.message("startup.opening.database"), 40);
        IPersistenceManager manager = PersistenceManagerConfig.getManager();
        try
        {
            manager.init();
        } catch (PersistenceException e)
        {
            LOG.log(Level.SEVERE, Strings.error("failed.to.initialize.database"), e);

            // It's not possible to continue working with database failing.
            System.exit(1);
        }

        configureShutdownHook();

        Splash.setNote(Strings.message("startup.restoring.state.from.database"), 80);
        // Restore last saved persistent state. If none, then gen fake data.
        GlobalController.SINGLETON.restorePersistentState(newVersionModel);

        Splash.setNote(Strings.message("startup.starting.connection.checking"), 85);
        startConnectionChecker();

        Splash.setNote(Strings.message("startup.loading.tip.of.the.day"), 90);
        checkForOpenTipOfTheDayDailog();

        splashEnsureOpenClosed(false);
        Splash.setProvider(null);
    }

    /**
     * Enabled IPC by subscribing the model to the handler and enabling GURL handler on Mac.
     * The handler is registered during the very start and may be already
     * reported some URL. Now, when it's enabled, it will pass it through to
     * the controller.
     */
    public static void enableIPC()
    {
        ipc.addListener(GlobalController.SINGLETON);
        
        // Enable GURL listener on Mac
        if (isMac()) GURLHandler.getInstance().enable();
    }

    /**
     * Configures IPC.
     */
    private void configureIPC()
    {
        try
        {
            ipc = new IPC(socketFile);
        } catch (IOException e)
        {
            LOG.log(Level.WARNING, "Failed to initialize IPC", e);
        }

        if (SystemUtils.IS_OS_MAC) OSXSupport.setupIPC(ipc);
    }

    /**
     * Configures hook for application shutdown.
     */
    private void configureShutdownHook()
    {
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            /**
             * Invoked when shutdown of application is in progress.
             */
            public void run()
            {
                // Closing database before termination
                PersistenceManagerConfig.getManager().shutdown();

                try
                {
                    instanceLock.release();
                } catch (IOException e)
                {
                    // Not a big deal
                }

                // Close IPC channel
                ipc.close();
            }
        });
    }

    private void checkForOpenTipOfTheDayDailog()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                TipOfTheDay.showRandomTip();
            }
        });
    }

    /**
     * Initializes and starts connection checker.
     */
    private void startConnectionChecker()
    {
        GlobalModel model = GlobalController.SINGLETON.getModel();
        ServicePreferences prefs = model.getServicePreferences();

        String version = globalAppLauncher.getApplicationDescription().getVersion();
        long installationId = getInstallationId();
        int runs = getInstallationRuns();
        String accountEmail = prefs.getEmail();
        String accountPassword = prefs.getPassword();

        ConnectionChecker connectionChecker = new ConnectionChecker(
            version, installationId, runs, accountEmail, accountPassword, connectionState);
        connectionChecker.start();
    }

    /**
     * Returns number of runs for current installation.
     *
     * @return runs count.
     */
    public static int getInstallationRuns()
    {
        return Application.getUserPreferences().getInt(KEY_RUNS, 0);
    }

    /**
     * Clears number of runs for installation.
     */
    static void clearInstallationRuns()
    {
        Application.getUserPreferences().putInt(KEY_RUNS, 0);
    }

    /**
     * Increments number of runs for installation.
     */
    static void incrementInstallationRuns()
    {
        Application.getUserPreferences().putInt(KEY_RUNS, getInstallationRuns() + 1);
    }

    /**
     * Returns installation ID. If ID is not currently defined defines it and stores.
     *
     * @return installation ID.
     */
    public static long getInstallationId()
    {
        return Application.getUserPreferences().getLong(KEY_INSTALLATION_ID, -1);
    }

    /**
     * Checks if migration procedures are necessary.
     *
     * @return newly created model in case of new installation or <code>null</code>.
     */
    private GlobalModel checkForNewVersion()
    {
        long installationId = getInstallationId();

        String forceInstallationVal = System.getProperty("force.installation");
        String installationVersion = forceInstallationVal != null
            ? Constants.EMPTY_STRING
            : Application.getUserPreferences().get(KEY_INSTALLATION_VERSION,
                Constants.EMPTY_STRING);

        GlobalModel model = null;
        if (installationId == -1 ||
            !globalAppLauncher.getApplicationDescription().getVersion().equals(installationVersion))
        {
            // If previous version is less than minimal version compatible with current
            // then continue with full installation wizard.
            if (VersionUtils.versionCompare(installationVersion, MINIMAL_COMPATIBLE_VERSION) < 0)
            {
                model = resetIncompatibleDatabase();
                if (model == null) System.exit(0);
            }

            installationId = generateInstallationId();
            Application.getUserPreferences().putLong(KEY_INSTALLATION_ID, installationId);
            Application.getUserPreferences().put(KEY_INSTALLATION_VERSION,
                globalAppLauncher.getApplicationDescription().getVersion());

            clearInstallationRuns();
        }

        return model;
    }

    /**
     * Calls installer in EDT and waits for it to finish.
     *
     * @return new model or <code>NULL</code> in case of failure.
     */
    private GlobalModel resetIncompatibleDatabase()
    {
        splashEnsureOpenClosed(false);

        Installer installer = new Installer();
        GlobalModel model = installer.perform(getContextPath());

        splashEnsureOpenClosed(true);

        return model;
    }

    /**
     * Hides / shows splash screen.
     *
     * @param open <code>TRUE</code> to ensure that the splash screen is shown.
     */
    private static void splashEnsureOpenClosed(final boolean open)
    {
        UifUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                if (open) Splash.ensureOpen(); else Splash.ensureClosed();
            }
        }, MSG_SPLASH_ENSURE_ERROR, Level.WARNING);
    }

    /**
     * Generates installation ID with high level of uniqueness.
     *
     * @return installation ID.
     */
    private long generateInstallationId()
    {
        // Using current time in millis here as it has high level enough.
        return System.currentTimeMillis();
    }

    /**
     * Computes where on the user's machine we will place various state files.
     *
     * @return context path.
     */
    public synchronized static String getContextPath()
    {
        if (contextPath == null)
        {
            String userHomePath = System.getProperty("user.home");
            String nodePath = '.' + getConfiguration().getPreferencesRootName();
            contextPath = userHomePath + File.separatorChar + nodePath + File.separatorChar;
        }

        return contextPath;
    }

    /**
     * Returns path to backups directory.
     *
     * @return backups directory path.
     */
    public static String getBackupsPath()
    {
        return getContextPath() + BACKUPS_DIR_NAME;
    }

    /**
     * Returns current application version.
     * 
     * @return current version.
     */
    public static String getCurrentVersion()
    {
        return CURRENT_VERSION;
    }

    /**
     * Small font plastic policy.
     */
    private static class SmallPlasticFontPolicy implements FontPolicy
    {
        /**
         * Returns fonts for a given LAF with given defaults.
         *
         * @param laf           LAF name.
         * @param uiDefaults    defaults.
         *
         * @return fonts set.
         */
        public FontSet getFontSet(String laf, UIDefaults uiDefaults)
        {
            FontPolicy policy = FontPolicies.getDefaultPlasticPolicy();
            int size = LookUtils.IS_LOW_RESOLUTION ? 11 : 12;

            FontSet set = policy.getFontSet(laf, uiDefaults);
            set = FontSets.createDefaultFontSet(
                ensureSize(set.getControlFont(), size),
                ensureSize(set.getMenuFont(), size),
                ensureSize(set.getTitleFont(), size));

            return set;
        }

        /**
         * Ensures that the size of the font equals to the given or derives the font.
         *
         * @param font  font to check.
         * @param size  desired size.
         *
         * @return font.
         */
        private static Font ensureSize(FontUIResource font, int size)
        {
            return font.getSize() == size ? font : font.deriveFont((float)size);
        }
    }
}
