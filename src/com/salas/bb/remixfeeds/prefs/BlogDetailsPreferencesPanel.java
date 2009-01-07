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
// $Id: BlogDetailsPreferencesPanel.java,v 1.28 2008/06/26 13:41:58 spyromus Exp $
//

package com.salas.bb.remixfeeds.prefs;

import com.jgoodies.binding.adapter.ComboBoxAdapter;
import com.jgoodies.binding.adapter.DocumentAdapter;
import com.jgoodies.binding.adapter.RadioButtonAdapter;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.list.ArrayListModel;
import com.jgoodies.binding.value.AbstractConverter;
import com.jgoodies.binding.value.BufferedValueModel;
import com.jgoodies.binding.value.Trigger;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.AbstractDialog;
import com.salas.bb.remixfeeds.api.IWeblogAPI;
import com.salas.bb.remixfeeds.api.WeblogAPIs;
import com.salas.bb.remixfeeds.templates.Editor;
import com.salas.bb.remixfeeds.templates.Templates;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

/**
 * Single blog details panel.
 */
class BlogDetailsPreferencesPanel extends JPanel implements PropertyChangeListener
{
    private JDialog parent;

    private final TargetBlogProxy proxy = new TargetBlogProxy(null);

    public static final String PROP_BLOG_PREFERENCES = "blogPreferences";

    private Trigger trigger             = new Trigger();
    private JTextField tfTitle          = new JTextField();
    private JTextField tfURLSummary     = new JTextField();
    private JTextField tfURL            = new JTextField();
    private JTextField tfUser           = new JTextField();
    private JPasswordField tfPassword   = new JPasswordField();
    private JTextArea  taDescription    = new JTextArea();
    private JComboBox cbDefaultCategory = new JComboBox();
    private JComboBox cbBlog            = new JComboBox();
    private JComboBox cbWeblogAPIType;

    private FetchBlogsAction actFetchBlogs;
    private FetchCategoriesAction actFetchCategories;

    private JButton btnSetup;
    private JButton btnFetchCategories;
    private JButton btnFetchBlogs;

    private JRadioButton rbPublic               = new JRadioButton(Strings.message("ptb.prefs.public"));
    private JRadioButton rbDraft                = new JRadioButton(Strings.message("ptb.prefs.draft"));

    private final JLabel lbTitle                = new JLabel(Strings.message("ptb.prefs.details.name"));
    private final JLabel lbURL                  = new JLabel(Strings.message("ptb.prefs.details.url"));
    private final JLabel lbDefaultCategory      = new JLabel(Strings.message("ptb.category"));
    private final JLabel lbBlog                 = new JLabel(Strings.message("ptb.prefs.details.blog"));
    private final JLabel lbPostAs               = new JLabel(Strings.message("ptb.prefs.details.post.as"));

    private final boolean hasTemplatesFeature;

    private JComboBox cbTemplate;
    private final JLabel lbTemplate             = new JLabel(Strings.message("te.template"));
    private JButton btnTemplateEditor;

    private final ArrayListModel lmdlCategories = new ArrayListModel();
    private final ArrayListModel lmdlBlogs = new ArrayListModel();
    private ArrayListModel lmTemplateNames;

    private BufferedValueModel vmURL;
    private BufferedValueModel vmUser;
    private BufferedValueModel vmPass;
    private BufferedValueModel vmAPI;

    /**
     * Creates a panel with layout.
     *
     * @param parent parent dialog.
     */
    public BlogDetailsPreferencesPanel(JDialog parent)
    {
        this.parent = parent;
        hasTemplatesFeature = Templates.hasCustomTemplatesFeature();

        initComponents(parent);
        btnTemplateEditor.setEnabled(hasTemplatesFeature);

        // Synthetic panels
        BBFormBuilder builder = new BBFormBuilder("p, 2dlu, p");
        builder.append(rbPublic, rbDraft);
        JPanel pnlPostAs = builder.getPanel();

        // Layout components
        builder = new BBFormBuilder("max(p;60dlu), 4dlu, 125dlu, 2dlu, p, 2dlu, 0:grow", this);

        builder.append(lbTitle, tfTitle);
        builder.nextLine();
        builder.append(lbURL, tfURLSummary);
        builder.append(btnSetup);
        builder.appendUnrelatedComponentsGapRow(2);
        builder.append(lbBlog, cbBlog);
        builder.append(btnFetchBlogs);
        builder.nextLine();
        builder.append(lbDefaultCategory, cbDefaultCategory);
        builder.append(btnFetchCategories);
        builder.nextLine();
        builder.append(lbPostAs, pnlPostAs);
        builder.nextLine();
        builder.append(lbTemplate, cbTemplate);
        builder.append(btnTemplateEditor, UifUtilities.makePublisherPlanIcon(!hasTemplatesFeature));
    }

    /**
     * Binds components to the properties of the preferences proxy.
     *
     * @param parent parent dialog.
     */
    private void initComponents(JDialog parent)
    {
        vmURL = new BufferedValueModel(new PropertyAdapter(proxy, TargetBlog.PROP_API_URL, true), trigger);
        vmUser = new BufferedValueModel(new PropertyAdapter(proxy, TargetBlog.PROP_USER, true), trigger);
        vmPass = new BufferedValueModel(new PropertyAdapter(proxy, TargetBlog.PROP_PASSWORD, true), trigger);

        tfTitle.setDocument(new DocumentAdapter(new PropertyAdapter(proxy, TargetBlog.PROP_TITLE, true)));
        tfURL.setDocument(new DocumentAdapter(vmURL));
        tfURLSummary.setDocument(new DocumentAdapter(new URLSummaryConverter(
            new PropertyAdapter(proxy, TargetBlog.PROP_API_URL, true))));
        tfURLSummary.setEditable(false);
        tfUser.setDocument(new DocumentAdapter(vmUser));
        tfPassword.setDocument(new DocumentAdapter(vmPass));

        taDescription = ComponentsFactory.createInstructionsArea("");

        loadCategoriesLookup();

        ValueModel mdlDefaultCategory = new PropertyAdapter(proxy, TargetBlog.PROP_DEFAULT_CATEGORY, true);
        ComboBoxAdapter adapter = new ComboBoxAdapter((ListModel)lmdlCategories, mdlDefaultCategory);
        cbDefaultCategory = new JComboBox(adapter);

        ValueModel mdlBlog = new PropertyAdapter(proxy, TargetBlog.PROP_BLOG, true);
        adapter = new ComboBoxAdapter((ListModel)lmdlBlogs, mdlBlog);
        cbBlog = new JComboBox(adapter);

        ValueModel mdlDraft = new PropertyAdapter(proxy, TargetBlog.PROP_DRAFT, true);
        rbPublic.setModel(new RadioButtonAdapter(mdlDraft, Boolean.FALSE));
        rbDraft.setModel(new RadioButtonAdapter(mdlDraft, Boolean.TRUE));

        Collection apis = WeblogAPIs.getWeblogAPIs();
        ValueModel mdlAPIs = new PropertyAdapter(proxy, TargetBlog.PROP_API_TYPE, true);
        vmAPI = new BufferedValueModel(mdlAPIs, trigger);
        adapter = new ComboBoxAdapter(apis.toArray(), vmAPI);
        cbWeblogAPIType = new JComboBox(adapter);
        vmAPI.addPropertyChangeListener(new WeblogTypeChangeListener());

        btnSetup = new JButton(new SetupAction(parent));
        btnFetchCategories = new JButton();
        actFetchCategories = new FetchCategoriesAction(); // It uses btnFetchCategories (hence the order)
        btnFetchCategories.setAction(actFetchCategories);
        btnFetchBlogs = new JButton();
        actFetchBlogs = new FetchBlogsAction(); // It uses btnFetchBlogs (hence the order)
        btnFetchBlogs.setAction(actFetchBlogs);

        // Templates
        Set<String> templateNames = Templates.getUserTemplates().keySet();
        lmTemplateNames = new ArrayListModel(templateNames);

        ValueModel mdlTemplates = new PropertyAdapter(proxy, TargetBlog.PROP_TEMPLATE_NAME, true);
        cbTemplate = new JComboBox(new ComboBoxAdapter((ListModel)lmTemplateNames, mdlTemplates));
        btnTemplateEditor = new JButton(new TemplateEditorAction());

        proxy.addPropertyChangeListener(TargetBlogProxy.PROP_BLOG_PREFERENCES, this);
        updateViewState();
    }

    private void loadCategoriesLookup()
    {
        loadLookup(lmdlCategories, proxy.getCategories());
    }

    private void loadBlogsLookup()
    {
        loadLookup(lmdlBlogs, proxy.getBlogs());
    }

    private static void loadLookup(ArrayListModel lm, Object[] items)
    {
        Arrays.sort(items);
        lm.clear();
        lm.addAll(Arrays.asList(items));
    }

    /**
     * Returns the blog preferences object.
     *
     * @return preferences.
     */
    public TargetBlog getBlogPreferences()
    {
        return proxy.getBlogPreferences();
    }

    /**
     * Sets the preferences object to edit. If object is <code>NULL</code>,
     * the view is disabled.
     *
     * @param prefs preferences object.
     */
    public void setBlogPreferences(TargetBlog prefs)
    {
        proxy.setBlogPreferences(prefs);
        loadBlogsLookup();
        loadCategoriesLookup();
    }

    /**
     * Invoked when the proxy preferences object gets loaded / unloaded.
     *
     * @param evt event.
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        updateViewState();
    }

    /**
     * Updates the state of view components.
     */
    private void updateViewState()
    {
        boolean en = proxy.isLoaded();

        lbTitle.setEnabled(en);
        tfTitle.setEnabled(en);
        lbURL.setEnabled(en);
        tfURLSummary.setEnabled(en);
        btnSetup.setEnabled(en);
        lbDefaultCategory.setEnabled(en);
        cbDefaultCategory.setEnabled(en);
        btnFetchCategories.setEnabled(en);
        lbBlog.setEnabled(en);
        cbBlog.setEnabled(en);
        btnFetchBlogs.setEnabled(en);
        lbPostAs.setEnabled(en);
        rbPublic.setEnabled(en);
        rbDraft.setEnabled(en);
        lbTemplate.setEnabled(en);
        cbTemplate.setEnabled(en);
    }

    /**
     * Opens setup dialog.
     */
    private class SetupAction extends AbstractAction
    {
        private final JDialog parentDialog;
        private BlogSetupDialog dialog;

        /**
         * Creates action.
         *
         * @param parent parent dialog.
         */
        public SetupAction(JDialog parent)
        {
            super(Strings.message("ptb.prefs.details.setup"));
            parentDialog = parent;
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e event.
         */
        public void actionPerformed(ActionEvent e)
        {
            if (dialog == null) dialog = new BlogSetupDialog(parentDialog);
            String oldURL = tfURL.getText();
            dialog.open();
            String newURL = tfURL.getText();

            if (!oldURL.equalsIgnoreCase(newURL))
            {
                proxy.setCategories(null);
                proxy.setDefaultCategory(null);
                proxy.setBlogs(null);
                proxy.setBlog(null);
                actFetchBlogs.actionPerformed(null);
                actFetchCategories.actionPerformed(null);
            }
        }
    }

    /**
     * The dialog for setup and probing blogs.
     */
    private class BlogSetupDialog extends AbstractDialog
    {
        private final JLabel lbStatus;
        private JButton btnOK;
        private JButton btnCancel;
        private ProgressSpinner spnProgress;

        /**
         * Creates setup dialog.
         *
         * @param parent parent dialog. 
         */
        public BlogSetupDialog(JDialog parent)
        {
            super(parent, Strings.message("ptb.prefs.details.setup.title"), true);
            lbStatus = new JLabel(Strings.message("ptb.prefs.details.setup.status.idle"));

            spnProgress = new ProgressSpinner();
            spnProgress.setText(Strings.message("ptb.prefs.details.setup.status.connecting"));
        }

        /**
         * Creates main page component.
         *
         * @return component.
         */
        protected JComponent buildContent()
        {
            JPanel content = new JPanel(new BorderLayout());
            content.add(buildMainPane(), BorderLayout.CENTER);
            content.add(buildButtonsBar(), BorderLayout.SOUTH);
            return content;
        }

        /**
         * Creates main pane.
         *
         * @return main pane.
         */
        private Component buildMainPane()
        {
            BBFormBuilder builder = new BBFormBuilder("p, 2dlu, max(p;50dlu), 75dlu");
            builder.setDefaultDialogBorder();

            builder.append(Strings.message("ptb.prefs.details.setup.type"), cbWeblogAPIType);
            builder.append(Strings.message("ptb.prefs.details.setup.url"), tfURL, 2);
            builder.nextLine();
            builder.append(Strings.message("ptb.prefs.details.setup.user"), tfUser, 2);
            builder.nextLine();
            builder.append(Strings.message("ptb.prefs.details.setup.pass"), tfPassword, 2);
            builder.nextLine();
            builder.appendUnrelatedComponentsGapRow();
            builder.nextLine();
            builder.appendRow("75dlu");
            builder.append(new JScrollPane(taDescription), 4, CellConstraints.FILL, CellConstraints.FILL);
            builder.nextLine();
            builder.appendUnrelatedComponentsGapRow();
            builder.nextLine();
            builder.append(Strings.message("ptb.prefs.details.setup.status"), lbStatus, 2);
            builder.appendRow("0:grow");

            return builder.getPanel();
        }

        /**
         * Creates buttons bar.
         *
         * @return bar.
         */
        private Component buildButtonsBar()
        {
            btnOK = createOKButton(true);
            btnOK.setAction(new TestBlogSetupAction());
            btnCancel = createCancelButton();

            ButtonBarBuilder builder = new ButtonBarBuilder();

            builder.getPanel().setBorder(Constants.DIALOG_BUTTON_BAR_BORDER);
            builder.addFixed(spnProgress);
            builder.addUnrelatedGap();
            builder.addGlue();
            builder.addGriddedButtons(new JButton[] { btnOK, btnCancel });

            return builder.getPanel();
        }

        /**
         * Opens the dialog.
         */
        public void open()
        {
            status(Strings.message("ptb.prefs.details.setup.status.idle"), false);
            super.open();
        }

        /**
         * Commit changes when accepting the dialog.
         */
        public void doAccept()
        {
            trigger.triggerCommit();
            super.doAccept();
        }

        /**
         * Flush all the fields in the setup dialog when cancelled.
         */
        public void doCancel()
        {
            trigger.triggerFlush();
            super.doCancel();
        }

        /**
         * Sets status and font.
         *
         * @param text status text.
         * @param bold <code>TRUE</code> for bold font.
         */
        private void status(String text, boolean bold)
        {
            lbStatus.setText(text);
            lbStatus.setFont(lbStatus.getFont().deriveFont(bold ? Font.BOLD :Font.PLAIN));
        }

        /**
         * Tests connection settings.
         */
        private class TestBlogSetupAction extends AbstractAction
        {
            /**
             * Creates action.
             */
            public TestBlogSetupAction()
            {
                super(Strings.message("ptb.prefs.details.setup.test"));
            }

            /**
             * Invoked when an action occurs.
             *
             * @param e event.
             */
            public void actionPerformed(ActionEvent e)
            {
                spnProgress.start();
                enableButtons(false);

                new Thread("Testing Connection")
                {
                    { setDaemon(true); }

                    /**
                     * Invoked when running a thread.
                     */
                    public void run()
                    {
                        TargetBlog tb = new TargetBlog();
                        tb.setApiType((IWeblogAPI)vmAPI.getValue());
                        tb.setApiURL((String)vmURL.getValue());
                        tb.setUser((String)vmUser.getValue());
                        tb.setPassword((String)vmPass.getValue());
                        final String msg = tb.testConnection();

                        if (msg == null) vmURL.setValue(tb.getApiURL());

                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                spnProgress.stop();
                                onTestComplete(msg);
                            }
                        });
                    }
                }.start();
            }

            /**
             * Invoked when the connection testing is complete.
             *
             * @param msg <code>NULL</code> if fine, or error message
             */
            private void onTestComplete(String msg)
            {
                enableButtons(true);
                if (msg == null)
                {
                    status(Strings.message("ptb.prefs.details.setup.status.detected"), true);
                    doAccept();
                } else
                {
                    status(msg, false);
                }
            }

            /**
             * Enables and disables buttons.
             *
             * @param en <code>TRUE</code> to enable.
             */
            private void enableButtons(boolean en)
            {
                btnOK.setEnabled(en);
                btnCancel.setEnabled(en);
            }
        }
    }

    /**
     * Fetches the list of categories.
     */
    private class FetchCategoriesAction extends AbstractFetchLookupAction
    {
        /**
         * Creates action.
         */
        public FetchCategoriesAction()
        {
            super(cbDefaultCategory, btnFetchCategories, Strings.message("ptb.prefs.details.category.update"));
        }

        /**
         * This method is called asynchronously when the action takes place. You need
         * to call loading procedure from it and the callback should be executed from
         * within EDT upon completion.
         *
         * @param edtCallback callback to execute.
         */
        protected void startAsyncLoading(Runnable edtCallback)
        {
            proxy.loadCategories(edtCallback);
        }

        /** This method is called when the lookup combo-box can be reloaded with new data. */
        protected void loadLookup()
        {
            loadCategoriesLookup();
        }

        /**
         * After the async loading the state is checked to learn whether the lookup
         * update is neccessary.
         *
         * @return <code>TRUE</code> if loading was successful and lookup should be updated.
         */
        protected boolean isSuccessfulLoading()
        {
            return proxy.getCategories().length > 0;
        }
    }

    /**
     * Fetches the list of blogs.
     */
    private class FetchBlogsAction extends AbstractFetchLookupAction
    {
        /**
         * Creates action.
         */
        public FetchBlogsAction()
        {
            super(cbBlog, btnFetchBlogs, Strings.message("ptb.prefs.details.blog.update"));
        }

        /**
         * This method is called asynchronously when the action takes place. You need
         * to call loading procedure from it and the callback should be executed from
         * within EDT upon completion.
         *
         * @param edtCallback callback to execute.
         */
        protected void startAsyncLoading(Runnable edtCallback)
        {
            proxy.loadBlogs(edtCallback);
        }

        /** This method is called when the lookup combo-box can be reloaded with new data. */
        protected void loadLookup()
        {
            loadBlogsLookup();
        }

        /**
         * After the async loading the state is checked to learn whether the lookup
         * update is neccessary.
         *
         * @return <code>TRUE</code> if loading was successful and lookup should be updated.
         */
        protected boolean isSuccessfulLoading()
        {
            return proxy.getBlogs().length > 0;
        }
    }

    /** This converter returns <i>Not Set</i> when URL isn't set. */
    private static class URLSummaryConverter extends AbstractConverter
    {
        /**
         * Creates converter.
         *
         * @param valueModel model to convert.
         */
        public URLSummaryConverter(ValueModel valueModel)
        {
            super(valueModel);
        }

        /**
         * Converts the data from the subject.
         *
         * @param object object.
         *
         * @return value.
         */
        public Object convertFromSubject(Object object)
        {
            String url = (String)object;
            return StringUtils.isEmpty(url) ? Strings.message("ptb.prefs.details.not.set") : url;
        }

        /**
         * We don't set anything.
         *
         * @param object value.
         */
        public void setValue(Object object)
        {
        }
    }

    /**
     * Invoked when a template editor is called.
     */
    private class TemplateEditorAction extends AbstractAction
    {
        private TemplateEditorAction()
        {
            super(Strings.message("te.edit"));
        }

        public void actionPerformed(ActionEvent e)
        {
            String templateName = (String)cbTemplate.getSelectedItem();

            Editor editor = new Editor(parent);
            editor.open(Templates.getByName(templateName));

            // Repopulate the templates list
            Collection<String> templateNames = Templates.getUserTemplateNames();
            lmTemplateNames.clear();
            lmTemplateNames.addAll(templateNames);

            if (templateName != null)
            {
                if (templateNames.contains(templateName))
                {
                    cbTemplate.setSelectedItem(templateName);
                } else
                {
                    cbTemplate.setSelectedIndex(0);
                }
            }
        }
    }

    private class WeblogTypeChangeListener implements PropertyChangeListener
    {
        private String lastEnteredURL;

        /**
         * This method gets called when a bound property is changed.
         *
         * @param evt A PropertyChangeEvent object describing the event source and the property that has changed.
         */

        public void propertyChange(PropertyChangeEvent evt)
        {
            IWeblogAPI type = (IWeblogAPI)vmAPI.getValue();
            if (type != null)
            {
                boolean newEnabled = type.isApiUrlApplicable();
                boolean oldEnabled = tfURL.isEnabled();

                if (newEnabled != oldEnabled)
                {
                    tfURL.setEnabled(newEnabled);
                    if (newEnabled)
                    {
                        tfURL.setText(lastEnteredURL);
                    } else
                    {
                        lastEnteredURL = tfURL.getText();
                        tfURL.setText(Strings.message("ptb.prefs.details.setup.not.applicable"));
                    }
                }

                taDescription.setText(type.getDescription());
            }
        }
    }
}
