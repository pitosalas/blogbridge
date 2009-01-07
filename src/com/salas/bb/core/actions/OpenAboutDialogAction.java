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
// $Id: OpenAboutDialogAction.java,v 1.23 2007/03/29 10:21:02 spyromus Exp $
//

package com.salas.bb.core.actions;

import com.jgoodies.uif.application.Application;
import com.jgoodies.uif.util.ResourceUtils;
import com.jgoodies.uifextras.convenience.DefaultAboutDialog;
import com.salas.bb.core.ApplicationLauncher;
import com.salas.bb.core.FeatureManager;
import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.prefs.UserPreferences;
import com.salas.bb.service.ServicePreferences;
import com.salas.bb.utils.BrowserLauncher;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.ResourceID;
import com.salas.bb.utils.i18n.Strings;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Action which opens About dialog.
 */
public final class OpenAboutDialogAction extends AbstractAction
{
    private static final Pattern PATTERN_VERSION =
        Pattern.compile("<version/?>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_REG_STATUS =
        Pattern.compile("<regstatus/?>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_REG_NAME =
        Pattern.compile("<regname/?>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_REG_EMAIL =
        Pattern.compile("<regemail/?>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_PLAN_INFO =
        Pattern.compile("<plan_info/?>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_WORKING_FOLDER =
        Pattern.compile("<working_folder/?>", Pattern.CASE_INSENSITIVE);

    private static OpenAboutDialogAction instance;

    private boolean     initialized;
    private JPanel      panel;
    private JEditorPane text;
    private String      html;

    /**
     * Hidden singleton constructor.
     */
    private OpenAboutDialogAction()
    {
        initialized = false;
    }

    private void init()
    {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        text = new JEditorPane();
        text.setEditorKit(new HTMLEditorKit());
        text.setEditable(false);
        Dimension size = new Dimension(400, 300);
        text.setMinimumSize(size);
        text.setMaximumSize(size);
        text.setPreferredSize(size);

        final UserPreferences preferences = GlobalModel.SINGLETON.getUserPreferences();
        text.addHyperlinkListener(new BrowserLauncher.LinkListener(preferences));

        panel.add(new JScrollPane(text), BorderLayout.CENTER);

        String tutS = ResourceUtils.getString(ResourceID.URL_ABOUT);
        URL aboutTextURL = ResourceUtils.getURL(tutS);

        // Prepare HTML. Substitute version number here because it cannot change during run.
        html = readHtml(aboutTextURL);
        String releaseType = ApplicationLauncher.getReleaseType();
        String version = Application.getDescription().getVersion() +
            (releaseType == null ? "" : " " + releaseType);
        html = PATTERN_VERSION.matcher(html).replaceAll(version);

        HTMLDocument doc = (HTMLDocument)text.getDocument();
        doc.setBase(aboutTextURL);
    }

    private String readHtml(URL url)
    {
        String result;

        try
        {
            Reader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            int ch;
            StringBuffer buf = new StringBuffer();
            while ((ch = reader.read()) != -1)
            {
                buf.append((char)ch);
            }
            result = buf.toString();
            reader.close();
        } catch (IOException e)
        {
            result = Strings.message("aboutdialog.data.is.unavailable");
        }

        return result;
    }

    /**
     * Returns initialized instance.
     *
     * @return instance of action.
     */
    public static synchronized OpenAboutDialogAction getInstance()
    {
        if (instance == null) instance = new OpenAboutDialogAction();
        return instance;
    }

    /**
     * Invoked when action occurs.
     *
     * @param e action event details object.
     */
    public void actionPerformed(ActionEvent e)
    {
        // Once only initialization
        if (!initialized) init();

        // Fill in the current registration state info, since that can change from About command
        // to About command.
        String message = prepareText();
        text.setText(message);
        text.setCaretPosition(0);

        new DefaultAboutDialog(GlobalController.SINGLETON.getMainFrame(), panel).open();
    }

    /**
     * Substitutes registration status, name and email.
     *
     * @return resulting HTML.
     */
    private String prepareText()
    {
        String result;

        ServicePreferences servicePreferences = GlobalModel.SINGLETON.getServicePreferences();
        String regStatus;
        String regName = Constants.EMPTY_STRING;
        String regEmail = Constants.EMPTY_STRING;

        boolean registered = servicePreferences.isAccountInformationEntered();
        if (registered)
        {
            regStatus = Strings.message("aboutdialog.registered");
            regName = servicePreferences.getFullName();
            regEmail = servicePreferences.getEmail();
        } else
        {
            regStatus = Strings.message("aboutdialog.unregistered");
        }

        result = PATTERN_REG_STATUS.matcher(html).replaceAll(regStatus);
        result = PATTERN_REG_NAME.matcher(result).replaceAll(regName);
        result = PATTERN_REG_EMAIL.matcher(result).replaceAll(regEmail);
        result = PATTERN_PLAN_INFO.matcher(result).replaceAll(getPlanInfo());
        result = PATTERN_WORKING_FOLDER.matcher(result).replaceAll(getWorkingFolder());

        return result;
    }

    /**
     * Returns the working folder.
     *
     * @return folder.
     */
    private String getWorkingFolder()
    {
        String p = ApplicationLauncher.getContextPath();
        p = p.replaceAll("\\\\", "\\\\\\\\");
        p = p.replaceAll("\\$", "\\\\$");
        return p;
    }

    /**
     * Prepares plan information for insertion. 
     * @return detailed plan info.
     */
    private String getPlanInfo()
    {
        FeatureManager fm = GlobalController.SINGLETON.getFeatureManager();

        String name = fm.getPlanName();
        Date date = fm.getPlanExpirationDate();
        float price = fm.getPlanPrice();
        int period = fm.getPlanPeriodMonths();
        boolean trial = fm.isPlanTrial();

        String info = name;
        if (date != null)
        {
            String dates = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT).format(date);

            if (trial)
            {
                info += " Trial (Expires: " + dates + ")";
            } else if (price == 0)
            {
                info += " (Expires: " + dates + ")";
            } else if (period != 0)
            {
                info += MessageFormat.format(" (Next Installment: {0}, \\${1} for the next {2} months)", dates,
                    new DecimalFormat("#.00", new DecimalFormatSymbols(Locale.US)).format(price), period);
            }
        }

        return info;
    }
}
