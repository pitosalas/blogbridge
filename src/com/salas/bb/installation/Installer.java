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
// $Id: Installer.java,v 1.20 2008/02/28 15:59:50 spyromus Exp $
//

package com.salas.bb.installation;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.uif.application.Application;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.core.actions.guide.ImportGuidesAction;
import com.salas.bb.installation.wizard.InstallationSettings;
import com.salas.bb.installation.wizard.InstallationWizard;
import com.salas.bb.service.ServicePreferences;
import com.salas.bb.service.sync.SyncIn;
import com.salas.bb.utils.CommonUtils;
import com.salas.bb.utils.ResourceID;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.feedscollections.CollectionItem;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.UifUtilities;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.prefs.Preferences;

/**
 * Installer of new version. When application detects that this is the first run it
 * calls installer to perform user-friendly installation. Installer in turn uses
 * Wizard-like dialog box to gather required information about user's preferences and
 * then runs the installation.
 */
public class Installer extends Thread
{
    private static final String MSG_INST_PROGRESS_DIALOG_ERROR = "Failed to create installation progress dialog.";

    /** Path to fresh database script. */
    private static final String FRESH_SCRIPT_RESOURCE = "resources/blogbridge.script";
    private static final String FRESH_PROPERTIES_RESOURCE = "resources/blogbridge.properties";

    private InstallationProgressDialog progressDialog;
    private InstallationSettings installationSettings;

    private GlobalModel model;
    private String workingPath;

    private final Object lock = new Object();

    /**
     * Performs installation by displaying wizard dialog and then running main process.
     *
     * @param aWorkingPath path to working directory.
     *
     * @return prepared global model. Null if installation cancelled for some reason.
     */
    public GlobalModel perform(String aWorkingPath)
    {
        workingPath = aWorkingPath;

        InstallationSettings settings = getInstallationSettings();

        // user entered all necessary information
        if (settings != null) install(settings);

        return model;
    }

    /**
     * Displays wizard-like dialog box with several pages to get information from user.
     *
     * @return installation settings or <code>null</code> if user cancelled.
     */
    private InstallationSettings getInstallationSettings()
    {
        final ValueHolder settingsHolder = new ValueHolder();

        UifUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                InstallationWizard wizard = new InstallationWizard();
                InstallationSettings settings = wizard.openDialog();
                settingsHolder.setValue(settings);
            }
        }, "Failed to get installation settings.", Level.SEVERE);

        return (InstallationSettings)settingsHolder.getValue();
    }

    /**
     * Perform installation using provided settings.
     *
     * @param aSettings settings to use in installation.
     *
     * @return <code>true</code> if installation was successful.
     */
    private boolean install(InstallationSettings aSettings)
    {
        if (aSettings == null) return false;

        // Setup progress dialog
        final String[] steps = enumerateInstallationSteps(aSettings);
        UifUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                progressDialog = new InstallationProgressDialog(steps, lock);
            }
        }, MSG_INST_PROGRESS_DIALOG_ERROR, Level.SEVERE);

        boolean result = true;
        installationSettings = aSettings;

        synchronized (lock)
        {
            start();
            try
            {
                lock.wait();
            } catch (InterruptedException e)
            {
                // Job exited or canceled
            }

            if (this.isAlive()) this.interrupt();
        }

        return result;
    }

    /**
     * Enumerates all steps necessary to finish installation.
     *
     * @param settings settings to analyze.
     *
     * @return list of steps to do.
     */
    private static String[] enumerateInstallationSteps(InstallationSettings settings)
    {
        String serviceAccountEmail = settings.getServiceAccountEmail();
        String serviceAccountPassword = settings.getServiceAccountPassword();

        ArrayList<String> steps = new ArrayList<String>();

        steps.add(Strings.message("installation.step.creating.empty.database"));

        if (StringUtils.isNotEmpty(serviceAccountEmail) &&
            StringUtils.isNotEmpty(serviceAccountPassword) &&
            !settings.isServiceAccountExists())
        {
            steps.add(Strings.message("installation.step.initializing.service.account"));
        }

        if (settings.getDataInitMode() == InstallationSettings.DATA_INIT_POINTS)
        {
            steps.add(Strings.message("installation.step.loading.guides.from.selected.starting.points"));
        } else if (settings.getDataInitMode() == InstallationSettings.DATA_INIT_SERVICE)
        {
            steps.add(Strings.message("installation.step.synchronizing.with.service.account"));
        } else
        {
            steps.add(Strings.message("installation.step.creating.clean.list.of.guides"));
        }

        steps.add(Strings.message("installation.step.applying.settings.locally"));

        return steps.toArray(new String[steps.size()]);
    }

    /**
     * Performs installation.
     */
    public void run()
    {
        synchronized (lock)
        {
            progressDialog.open();
        }

        // Create empty model and read in all preferences we have
        model = new GlobalModel(null, false);
        model.restorePreferences(Application.getUserPreferences());

        // Create empty database

        progressDialog.succeedStep();
        cleanDatabase(workingPath);

        if (installationSettings.isUseAccountSelected())
        {
            // Initialize account
            initializeServiceAccount();
        }
        progressDialog.succeedStep();

        // Import data
        if (importData())
        {
            progressDialog.succeedStep();
        } else
        {
            progressDialog.failedStep();
        }

        // Apply local settings
        applyLocalSettings();
        progressDialog.succeedStep();

        synchronized (lock)
        {
            progressDialog.finish();
        }
    }

    private void applyLocalSettings()
    {
        // Set acccepted license
        Preferences prefs = Application.getUserPreferences();
        String version = Application.getDescription().getVersion();
        prefs.put(ResourceID.VERSION_ACCEPTED_LICENSE, version);
    }

    /**
     * Initializes main properties of the service account.
     */
    private void initializeServiceAccount()
    {
        ServicePreferences sp = model.getServicePreferences();
        sp.setRegDate(new Date());
        sp.setEmail(installationSettings.getServiceAccountEmail());
        sp.setPassword(installationSettings.getServiceAccountPassword());

// Since we copy everything from the installation model to the working model in
// OpenDBInBackground.run(), we don't need this any more
//        Preferences prefs = Application.getUserPreferences();
//        prefs.putLong(ServicePreferences.KEY_REG_DATE, System.currentTimeMillis());
//        prefs.put(ServicePreferences.KEY_EMAIL, installationSettings.getServiceAccountEmail());
//        prefs.put(ServicePreferences.KEY_PASSWORD, installationSettings.getServiceAccountPassword());
    }

    private boolean importData()
    {
        boolean result = true;
        switch (installationSettings.getDataInitMode())
        {
            case InstallationSettings.DATA_INIT_POINTS:
                importDataFromPoints();
                break;
            case InstallationSettings.DATA_INIT_SERVICE:
                result = importDataFromService();
                break;
            default:
                // clean installation
                break;
        }

        return result;
    }

    private boolean importDataFromService()
    {
        String email = installationSettings.getServiceAccountEmail();
        String password = installationSettings.getServiceAccountPassword();
        SyncIn syncIn = new SyncIn(model, false);

        return !syncIn.doSynchronization(null, false, email, password).hasFailed();
    }

    private void importDataFromPoints()
    {
        CollectionItem[] points = installationSettings.getSelectedStartingPoints();
        for (CollectionItem point : points)
        {
            ImportGuidesAction.importAndAppend(model, point.getXmlURL());
        }
    }

    /**
     * Replaces database with a fresh copy from resources.
     *
     * @param workingPath working folder path.
     */
    public static void cleanDatabase(String workingPath)
    {
        CommonUtils.copyResourceToFile(FRESH_SCRIPT_RESOURCE, workingPath + "blogbridge.script");
        CommonUtils.copyResourceToFile(FRESH_PROPERTIES_RESOURCE, workingPath + "blogbridge.properties");
    }
}
