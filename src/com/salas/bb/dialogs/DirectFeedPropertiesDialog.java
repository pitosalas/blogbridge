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
// $Id: DirectFeedPropertiesDialog.java,v 1.40 2008/03/05 16:34:17 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.util.Resizer;
import com.salas.bb.core.FeedFormatter;
import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.core.ScoresCalculator;
import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.FeedHandlingType;
import com.salas.bb.domain.prefs.StarzPreferences;
import com.salas.bb.utils.BrowserLauncher;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.DateUtils;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.ComponentsFactory;
import com.salas.bb.utils.uif.HeaderPanelExt;
import com.salas.bb.utils.uif.LinkLabel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Displays properties of channel.
 */
public class DirectFeedPropertiesDialog extends AbstractDialog
{
    private static final Logger LOG = Logger.getLogger(DirectFeedPropertiesDialog.class.getName());

    private static final Pattern PAT_EMAIL = Pattern.compile(
        "(^|[\\s\\[\\(\\{\\<])([a-zA-Z]\\w*(([_-]+|\\.)\\w+)*@[\\w-_]{2,}(\\.[\\w-_]{2,})+)($|[\\s\\]\\)\\}\\>])");

    private static final int MAX_HEADER_TITLE_LENGTH = 40;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.00");

    private DirectFeed          feed;

    private JTextField          tfXmlUrl;
    private JTextField          tfTitle;
    private JTextField          tfAuthor;
    private JTextArea           taDescription;
    private JTextField          tfPurgeLimit;

    private JLabel              lbFinalScore;
    private int                 blogStarzScore;
    private int                 rating;

    private String              initialTitle;
    private String              initialAuthor;
    private String              initialDescription;
    private int                 initialPurgeLimit;
    private long                initialUpdatePeriod;

    private JButton                     btnCancel;
    private LinkLabel                   tfSiteUrl;
    private JButton                     btnSendEmail;
    private DisplayPropertiesTabPanel   displayTab;
    private FeedUpdatePeriodPanel       pnlFeedUpdatePeriod;
    private FeedAutoSavePanel           pnlFeedAutoSave;
    private JComboBox                   cbHandlingType;

    /**
     * Creates new properties dialog.
     *
     * @param owner owning frame for this Dialog.
     * @param aFeed feed for which we are showing properties.
     */
    public DirectFeedPropertiesDialog(Frame owner, DirectFeed aFeed)
    {
        super(owner, Strings.message("show.feed.properties.dialog.title.directfeed"));
        setResizable();

        feed = aFeed;
    }

    /**
     * Builds a pretty XP-style white header.
     *
     * @return header object.
     */
    protected JComponent buildHeader()
    {
        String title = feed.getTitle();
        if (title != null && title.length() > MAX_HEADER_TITLE_LENGTH)
            title = title.substring(0, MAX_HEADER_TITLE_LENGTH) + ("\u2026");

        return new HeaderPanelExt(
            Strings.message("show.feed.properties.dialog.title.directfeed"),
            MessageFormat.format(Strings.message("show.feed.properties.dialog.header.direct"), title));
    }

    /**
     * Returns the content of the dialog.
     *
     * @return contents.
     */
    protected JComponent buildContent()
    {
        JPanel content = new JPanel(new BorderLayout());

        content.add(buildBody(), BorderLayout.CENTER);
        content.add(buildButtonBar(), BorderLayout.SOUTH);

        return content;
    }

    /**
     * Builds buttons bar.
     *
     * @return bar.
     */
    private Component buildButtonBar()
    {
        JButton btnOk = createOKButton(true);
        btnCancel = createCancelButton();

        JPanel buttonPanel = ButtonBarFactory.buildOKCancelBar(btnOk, btnCancel);
        buttonPanel.setBorder(Borders.BUTTON_BAR_GAP_BORDER);

        return buttonPanel;
    }

    /**
     * Creates custom revert button.
     *
     * @return button.
     */
    private JButton createRevertButton()
    {
        JButton btnRevert = ComponentsFactory.createButton(
            Strings.message("show.feed.properties.revert.to.default"));

        btnRevert.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                tfTitle.setText(feed.getBaseTitle());
                tfAuthor.setText(feed.getBaseAuthor());
                taDescription.setText(feed.getBaseDescription());
            }
        });

        return btnRevert;
    }

    @Override
    public void setVisible(boolean b)
    {
        // Request focus for the title
        tfTitle.requestFocusInWindow();

        super.setVisible(b);
    }

    /**
     * Applies changes if some.
     */
    public void doApply()
    {
        String msg = pnlFeedAutoSave == null ? null : pnlFeedAutoSave.validateData();
        if (msg != null)
        {
            JOptionPane.showMessageDialog(this, msg, getTitle(), JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newTitle = tfTitle.getText();
        String newAuthor = tfAuthor.getText();
        String newDescription = taDescription.getText();
        int newPurgeLimit = getFormPurgeLimit();
        long newUpdatePeriod = pnlFeedUpdatePeriod.getUpdatePeriod();

        if (!newTitle.equals(initialTitle == null ? Constants.EMPTY_STRING : initialTitle))
        {
            if (newTitle.equals(feed.getBaseTitle())) newTitle = null;
            feed.setCustomTitle(newTitle);
        }

        if (!newAuthor.equals(initialAuthor == null ? Constants.EMPTY_STRING : initialAuthor))
        {
            if (newAuthor.equals(feed.getBaseAuthor())) newAuthor = null;
            feed.setCustomAuthor(newAuthor);
        }

        if (!newDescription.equals(initialDescription == null
            ? Constants.EMPTY_STRING : initialDescription))
        {
            if (newDescription.equals(feed.getBaseDescription())) newDescription = null;
            feed.setCustomDescription(newDescription);
        }

        // Custom purge limit
        if (newPurgeLimit != initialPurgeLimit)
        {
            feed.setPurgeLimit(newPurgeLimit);
        }

        // Custom update period
        if (newUpdatePeriod != initialUpdatePeriod)
        {
            feed.setUpdatePeriod(newUpdatePeriod);
        }

        // Ratings
        if (rating != feed.getRating())
        {
            feed.setRating(rating);
        }

        // Feed type
        displayTab.commitChanges();
        if (pnlFeedAutoSave != null) pnlFeedAutoSave.commitChanges(feed);

        feed.setHandlingType((FeedHandlingType)cbHandlingType.getSelectedItem());

        // Put XML url if the feed is not initialized
        if (!feed.isInitialized())
        {
            String newUrl = tfXmlUrl.getText();
            if (newUrl.trim().length() == 0) newUrl = null;

            try
            {
                feed.setXmlURL(newUrl == null ? null : new URL(newUrl));
            } catch (MalformedURLException e)
            {
                // TODO possibly report an error or do some type validation
                LOG.log(Level.WARNING, MessageFormat.format(Strings.error("invalid.url"), newUrl), e);
            }
        }
    }

    /**
     * Returns purge limit setting from form.
     *
     * @return purge limit.
     */
    private int getFormPurgeLimit()
    {
        int purgeLimit = -1;

        String purgeLimitString = tfPurgeLimit.getText();
        if (purgeLimitString != null && purgeLimitString.trim().length() != 0)
        {
            purgeLimit = Integer.parseInt(purgeLimitString);
        }

        return purgeLimit;
    }

    /**
     * Sets new purge limit value.
     *
     * @param purgeLimit new purge limit.
     */
    private void setFormPurgeLimit(int purgeLimit)
    {
        String text = purgeLimit == -1 ? Constants.EMPTY_STRING : Integer.toString(purgeLimit);
        tfPurgeLimit.setText(text);
    }

    /**
     * Performs cancel operation only if cancel button is enbled.
     */
    public void doCancel()
    {
        if (btnCancel.isEnabled()) super.doCancel();
    }

    /**
     * Performs close operation only if cancel button is enbled.
     */
    public void doClose()
    {
        if (btnCancel.isEnabled()) super.doClose();
    }

    /**
     * Returns the JPanel which will be the body of this little dialog.
     *
     * @return JComponent body of this Dialog Box.
     */
    private JComponent buildBody()
    {
        initComponents();

        JPanel panel = new JPanel(new BorderLayout());
        JTabbedPane pane = new JTabbedPane();
        panel.add(pane, BorderLayout.CENTER);

        displayTab = new DisplayPropertiesTabPanel(feed);

        pane.addTab(Strings.message("show.feed.properties.tab.basic"), createBasicTab());
        pane.addTab(Strings.message("show.feed.properties.tab.display"), displayTab);
        pane.addTab(Strings.message("show.feed.properties.tab.blogstarz"), createBlogStarzTab());
        pane.addTab(Strings.message("show.feed.properties.tab.advanced"), createAdvancedTab());

        adjustReadOnly();

        return panel;
    }

    /**
     * Creates basic tab component.
     *
     * @return basic tab.
     */
    private Component createBasicTab()
    {
        JScrollPane spDescription = new JScrollPane(taDescription);

        BBFormBuilder builder = new BBFormBuilder("pref, 4dlu, 0:grow, 2dlu, p");
        builder.setDefaultDialogBorder();

        builder.append(Strings.message("show.feed.properties.tab.basic.title"), tfTitle, 3);
        builder.appendRelatedComponentsGapRow(2);
        builder.appendRow("50dlu");
        JLabel label = builder.append(Strings.message("show.feed.properties.tab.basic.description"), 1,
            CellConstraints.FILL, CellConstraints.TOP);
        label.setLabelFor(taDescription);
        builder.append(spDescription, 3, CellConstraints.FILL, CellConstraints.FILL);
        builder.append(Strings.message("show.feed.properties.tab.basic.author"), tfAuthor);
        builder.append(btnSendEmail);
        builder.append(Strings.message("show.feed.properties.tab.basic.siteurl"), tfSiteUrl, 3);
        builder.append(Strings.message("show.feed.properties.tab.basic.xmlurl"), tfXmlUrl, 3);
        builder.append(Strings.message("show.feed.properties.tab.basic.language"),
            new JLabel(convertLang2String(feed.getLanguage())), 3);
        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(ButtonBarFactory.buildCenteredBar(createRevertButton()), 5);
        builder.appendRow("14dlu:grow");

        return builder.getPanel();
    }

    /**
     * Initializes components.
     */
    private void initComponents()
    {
        initialUpdatePeriod = feed.getUpdatePeriod();

        // Basic Tab
        initialTitle = feed.getTitle();
        tfTitle = new JTextField(initialTitle);

        initialAuthor = feed.getAuthor();
        tfAuthor = new JTextField(initialAuthor);
        tfAuthor.getDocument().addDocumentListener(new AuthorFieldUpdateListener());

        initialDescription = feed.getDescription();
        taDescription = new JTextArea(initialDescription);
        taDescription.setLineWrap(true);
        taDescription.setWrapStyleWord(true);

        URL siteURL = feed.getSiteURL();
        tfSiteUrl = new LinkLabel();
        tfSiteUrl.setText(siteURL == null ? null : siteURL.toString());
        tfSiteUrl.setLink(siteURL);
        URL xmlURL = feed.getXmlURL();
        tfXmlUrl = new JTextField(xmlURL == null ? null : xmlURL.toString());

        btnSendEmail = ComponentsFactory.createButton(Strings.message("show.feed.properties.tab.basic.send.email"));
        btnSendEmail.addActionListener(new SendEmailToAuthorAction());
        updateSendEmailButton();

        pnlFeedUpdatePeriod = new FeedUpdatePeriodPanel(feed.getUpdatePeriod());
        pnlFeedAutoSave = new FeedAutoSavePanel(feed, GlobalController.SINGLETON.getFeatureManager().isAutoSaving());
    }

    // Updates the state of the send email button
    private void updateSendEmailButton()
    {
        btnSendEmail.setEnabled(getAuthorEmail() != null);
    }

    /**
     * Returns first email mentioned in the author field.
     *
     * @return email or NULL if not found.
     */
    private String getAuthorEmail()
    {
        String email = null;

        String author = tfAuthor.getText();
        if (!StringUtils.isEmpty(author))
        {
            Matcher matcher = PAT_EMAIL.matcher(author);
            if (matcher.find()) email = matcher.group(2);
        }

        return email;
    }

    /**
     * Adjusts the state of components.
     */
    private void adjustReadOnly()
    {
        boolean feedInitialized = feed.isInitialized();

        tfXmlUrl.setEditable(!feedInitialized);
        tfAuthor.setEditable(feedInitialized);
        taDescription.setEditable(feedInitialized);
        tfTitle.setEditable(feedInitialized);
    }

    /**
     * Creates advanced tab.
     *
     * @return advanced tab.
     */
    private Component createAdvancedTab()
    {
        cbHandlingType = new JComboBox(FeedHandlingType.ALL_TYPES);
        cbHandlingType.setSelectedItem(feed.getHandlingType());

        initialPurgeLimit = feed.getPurgeLimitCombined();
        tfPurgeLimit = new JTextField();
        setFormPurgeLimit(initialPurgeLimit);

        JLabel lbArticleCount = new JLabel(Integer.toString(feed.getArticlesCount()));
        JLabel lbRetrievals = new JLabel(String.valueOf(feed.getRetrievals()));
        JLabel lbLastUpdate = new JLabel(DateUtils.dateToString(new Date(feed.getLastPollTime())));
        JLabel lbFormat = new JLabel(feed.getFormat());

        BBFormBuilder builder = new BBFormBuilder("pref, 4dlu, max(pref;40px), 2dlu, pref:grow");
        builder.setDefaultDialogBorder();

        builder.append(Strings.message("show.feed.properties.tab.advanced.articles"), lbArticleCount, 3);
        builder.append(Strings.message("show.feed.properties.tab.advanced.retrievals"), lbRetrievals, 3);
        builder.append(Strings.message("show.feed.properties.tab.advanced.last.update"), lbLastUpdate, 3);
        builder.append(Strings.message("show.feed.properties.tab.advanced.format"), lbFormat, 3);
        builder.append(Strings.message("show.feed.properties.tab.advanced.purge.limit"), tfPurgeLimit);
        builder.nextLine();
        builder.append(Strings.message("show.feed.properties.tab.advanced.handling.type"), cbHandlingType);
        builder.nextLine();
        builder.append(Strings.message("show.feed.properties.tab.advanced.update.period"), 1,
                CellConstraints.LEFT, CellConstraints.TOP);
        builder.append(pnlFeedUpdatePeriod, 3);

        if (pnlFeedAutoSave != null)
        {
            builder.appendUnrelatedComponentsGapRow(2);
            builder.append(pnlFeedAutoSave, 5);
        }

        return builder.getPanel();
    }

    /**
     * Creates blog starz tab.
     *
     * @return tab.
     */
    private Component createBlogStarzTab()
    {
        ScoresCalculator calc = GlobalModel.SINGLETON.getScoreCalculator();
        double activity = calc.calcActivity(feed);
        double inlinks = calc.calcInlinksScore(feed);
        double views = calc.calcFeedViewsScore(feed);
        double clickthroughs = calc.calcClickthroughsScore(feed);
        blogStarzScore = calc.calcBlogStarzScore(feed);
        rating = feed.getRating();

        StarzPreferences prefs = GlobalModel.SINGLETON.getStarzPreferences();
        int activityWeight = prefs.getActivityWeight();
        int inlinksWeight = prefs.getInlinksWeight();
        int viewsWeight = prefs.getFeedViewsWeight();
        int clickthroughsWeight = prefs.getClickthroughsWeight();

        String msg = feed.getTextualInboundLinks();

        lbFinalScore = new JLabel();
        lbFinalScore.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lbFinalScore.addMouseListener(new FinalScoreStarzListener());
        updateFinalScoreIcon();

        JLabel lbTechnoratiInlinks = new JLabel(msg);
        JLabel lbActivity = new JLabel(MessageFormat.format(
            Strings.message("show.feed.properties.tab.blogstarz.0.weight.1"),
            DECIMAL_FORMAT.format(activity), activityWeight));
        JLabel lbInLinks = new JLabel(MessageFormat.format(
            Strings.message("show.feed.properties.tab.blogstarz.0.weight.1"),
            DECIMAL_FORMAT.format(inlinks), inlinksWeight));
        JLabel lbViews = new JLabel(MessageFormat.format(
            Strings.message("show.feed.properties.tab.blogstarz.0.weight.1"),
            DECIMAL_FORMAT.format(views), viewsWeight));
        JLabel lbClickthroughs = new JLabel(MessageFormat.format(
            Strings.message("show.feed.properties.tab.blogstarz.0.weight.1"),
            DECIMAL_FORMAT.format(clickthroughs), clickthroughsWeight));
        JLabel lbRecommendation = new JLabel(FeedFormatter.getStarzIcon(blogStarzScore, true));

        BBFormBuilder builder = new BBFormBuilder("pref, 4dlu, pref, 4dlu, pref:grow");
        builder.setDefaultDialogBorder();

        builder.append(Strings.message("show.feed.properties.tab.blogstarz.technorati.inlinks"), lbTechnoratiInlinks, 3);
        builder.append(Strings.message("show.feed.properties.tab.blogstarz.activity"), lbActivity, 3);
        builder.append(Strings.message("show.feed.properties.tab.blogstarz.inlinks"), lbInLinks, 3);
        builder.append(Strings.message("show.feed.properties.tab.blogstarz.views"), lbViews, 3);
        builder.append(Strings.message("show.feed.properties.tab.blogstarz.clickthroughs"), lbClickthroughs, 3);
        builder.append(Strings.message("show.feed.properties.tab.blogstarz.recommendation"), 1);
        builder.append(lbRecommendation, 1, CellConstraints.LEFT, CellConstraints.CENTER);
        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(Strings.message("show.feed.properties.tab.blogstarz.final.rating"), 1);
        builder.append(lbFinalScore, 1, CellConstraints.LEFT, CellConstraints.CENTER);
        builder.append(new JButton(new RevertAction()), 1,
            CellConstraints.RIGHT, CellConstraints.CENTER);
        builder.appendRow("pref:grow");
        builder.append(ComponentsFactory.createWrappedMultilineLabel(
            Strings.message("show.feed.properties.tab.blogstarz.notes")), 5,
            CellConstraints.FILL, CellConstraints.BOTTOM);

        return builder.getPanel();
    }

    /**
     * Converts compressed language-country code into human-readable form.
     *
     * @param language language.
     *
     * @return string.
     */
    private static String convertLang2String(String language)
    {
        if (language == null) return null;
        language = language.replaceAll("-", "_");
        StringTokenizer st = new StringTokenizer(language, "_");
        String lang;
        String country = "";
        String variant = "";
        String display = language;

        if (language.length() > 0)
        {
            try
            {
                lang = st.nextToken();
                if (st.hasMoreTokens()) country = st.nextToken().toUpperCase();
                if (st.hasMoreTokens()) variant = st.nextToken();

                Locale l = new Locale(lang, country, variant);
                display = l.getDisplayName();
            } catch (Exception e)
            {
                if (LOG.isLoggable(Level.WARNING))
                {
                    LOG.log(Level.WARNING, MessageFormat.format(
                        Strings.error("failed.to.transform.the.language.into.printable.form"),
                        language), e);
                }
            }
        }
        
        return display;
    }

    // Updates the icon according to the rating setting.
    private void updateFinalScoreIcon()
    {
        if (rating == -1)
        {
            lbFinalScore.setIcon(FeedFormatter.getStarzIcon(blogStarzScore, true));
        } else
        {
            lbFinalScore.setIcon(FeedFormatter.getStarzIcon(rating, false));
        }
    }

    /**
     * Resizes the specified component. This method is called during the build process and enables
     * subclasses to achieve a better aspect ratio, by applying a resizer, e.g. the
     * <code>Resizer</code>.
     *
     * @param component the component to be resized
     */
    protected void resizeHook(JComponent component)
    {
        component.setPreferredSize(Resizer.ONE2ONE.fromHeight(component.getPreferredSize().height));
    }

    /**
     * Listens for clicks over the final score starz and updates the view by applying or
     * resetting user supplied rating.
     */
    private class FinalScoreStarzListener extends MouseAdapter
    {
        public void mouseClicked(MouseEvent e)
        {
            if ((e.getModifiers() & InputEvent.SHIFT_MASK) == 0)
            {
                rating = (int)(5.0 * e.getX() / lbFinalScore.getIcon().getIconWidth());
            } else
            {
                rating = -1;
            }

            updateFinalScoreIcon();
        }
    }

    /** Reverts rating to default value. */
    private class RevertAction extends AbstractAction
    {
        public RevertAction()
        {
            super(Strings.message("show.feed.properties.revert.to.recommendation"));
        }

        public void actionPerformed(ActionEvent e)
        {
            rating = -1;
            updateFinalScoreIcon();
        }
    }

    /**
     * Sends email to author mentioned in the author field.
     */
    private class SendEmailToAuthorAction implements ActionListener
    {
        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e)
        {
            String email = getAuthorEmail();
            if (email != null)
            {
                String browser = GlobalModel.SINGLETON.getUserPreferences().getInternetBrowser();
                BrowserLauncher.emailThis(email, tfTitle.getText(), "", browser);
            }
        }
    }

    private class AuthorFieldUpdateListener implements DocumentListener
    {
        /**
         * Gives notification that there was an insert into the document.  The
         * range given by the DocumentEvent bounds the freshly inserted region.
         *
         * @param e the document event
         */
        public void insertUpdate(DocumentEvent e)
        {
            updateSendEmailButton();
        }

        /**
         * Gives notification that a portion of the document has been
         * removed.  The range is given in terms of what the view last
         * saw (that is, before updating sticky positions).
         *
         * @param e the document event
         */
        public void removeUpdate(DocumentEvent e)
        {
            updateSendEmailButton();
        }

        /**
         * Gives notification that an attribute or set of attributes changed.
         *
         * @param e the document event
         */
        public void changedUpdate(DocumentEvent e)
        {
            updateSendEmailButton();
        }
    }

}
