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
// $Id: AbstractTagsDialog.java,v 1.25 2008/04/01 10:55:55 spyromus Exp $
//

package com.salas.bb.tags;

import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.util.ScreenUtils;
import com.salas.bb.domain.ITaggable;
import com.salas.bb.tags.net.ITagsStorage;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.LinkLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Basic tagging dialog.
 */
abstract class AbstractTagsDialog extends AbstractDialog
{
    private static final Logger LOG = Logger.getLogger(AbstractTagsDialog.class.getName());

    protected static final String ERR_DESCRIPTION_EMPTY = Strings.message("tags.error.empty.description");
    protected static final String ERR_TAGS_EMPTY = Strings.message("tags.error.empty.tags");
    protected static final String ERR_MULTIWORD_TAGS = Strings.message("tags.error.multi.word.targs");

    private ITagsStorage            tagsStorage;
    private ITaggable[]             taggables;

    private LinkLabel               tfTitle;
    private JTextField              tfDescription;
    private JTextField              tfExtended;
    private JTextField              tfTags;
    private JLabel                  lbSharedTags;

    private FetchSharedTagsAction   fetchTagsAction;
    private JButton                 btnFetch;

    private boolean                 untagging;
    private JButton                 btnUntag;

    /**
     * Creates dialog.
     *
     * @param frame     parent frame.
     * @param aStorage  storage to use for service communications.
     */
    public AbstractTagsDialog(Frame frame, ITagsStorage aStorage)
    {
        super(frame, Strings.message("tags.dialog.title"));
        tagsStorage = aStorage;

        setModal(true);
    }

    /**
     * Creates content.
     *
     * @return content.
     */
    protected JComponent buildContent()
    {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(buildMainPanel(), BorderLayout.CENTER);
        panel.add(buildButtonsBar(), BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates the buttons bar.
     *
     * @return bar component.
     */
    private JComponent buildButtonsBar()
    {
        btnUntag = createOKButton(false);
        btnUntag.setAction(new AbstractAction(Strings.message("tags.untag"))
        {
            public void actionPerformed(ActionEvent e)
            {
                untagging = true;
                doAccept();
            }
        });

        return ButtonBarFactory.buildHelpOKCancelBar(btnUntag, createOKButton(true), createCancelButton());
    }

    /**
     * Creates main panel.
     *
     * @return main panel.
     */
    private Component buildMainPanel()
    {
        BBFormBuilder builder = new BBFormBuilder("pref, 4dlu, 150dlu, 2dlu, pref");

        builder.append(getTitleLabel(), tfTitle, 3);
        builder.append(Strings.message("tags.description"), tfDescription, 3);
        builder.append(Strings.message("tags.extended"), tfExtended, 3);
        builder.append(Strings.message("tags.tags"), tfTags, new JButton(new SuggestTagsAction()));

        builder.nextLine();
        builder.appendUnrelatedComponentsGapRow();
        builder.nextLine();

        builder.append(Strings.message("tags.other.tags"), 1, lbSharedTags, btnFetch);

        builder.appendUnrelatedComponentsGapRow();
        
        return builder.getPanel();
    }

    /**
     * Initializes components.
     */
    private void initComponents()
    {
        fetchTagsAction = new FetchSharedTagsAction();

        tfTitle = new LinkLabel();
        tfDescription = new JTextField();
        tfExtended = new JTextField();
        tfTags = new JTextField();
        lbSharedTags = new JLabel();

        btnFetch = new JButton(fetchTagsAction);
    }

    /**
     * Returns the label for the title field.
     *
     * @return label.
     */
    protected abstract String getTitleLabel();

    /**
     * Sets all fields at once. Apply reasonable defaults for the labels where possible.
     *
     * @param title         title of the object.
     * @param url           URL of the object.
     * @param description   textual description.
     * @param extended      extended description.
     * @param tags          user tags assigned.
     */
    protected void setTaggableData(String title, String url, String description,
                                   String extended, String[] tags)
    {
        URL link = null;
        try
        {
            link = new URL(url);
        } catch (MalformedURLException e)
        {
            // Bad of course, but it's just presentation layer
            LOG.log(Level.WARNING, MessageFormat.format(
                Strings.error("invalid.url"), url), e);
        }

        if (StringUtils.isEmpty(title)) title = url;

        tfTitle.setText(title);
        tfTitle.setForeground(Color.BLUE);
        tfTitle.setLink(link);

        if (StringUtils.isEmpty(description)) description = title;

        tfDescription.setText(description);
        tfExtended.setText(extended);

        tfTags.setText(tags == null ? null : StringUtils.join(tags, " "));
    }

    /**
     * This operation is not supported.
     */
    public void open()
    {
        throw new UnsupportedOperationException(Strings.error("use.open.itaggable.method"));
    }

    /**
     * Opens the dialog for given taggble objects.
     *
     * @param aTaggables    taggable objects.
     * @param autoLoad      <code>TRUE</code> to load shared tags automatically. Works only
     *                      when there's one taggable object.
     *
     * It is important that this method is final. We need to drop the untagged flag every time
     * new taggable objects are opened in this dialog.
     */
    public final void open(ITaggable[] aTaggables, boolean autoLoad)
    {
        taggables = filterLinkless(aTaggables);

        int taggablesCount = taggables.length;
        if (taggablesCount > 0)
        {
            // setting the flag that we aren't untagging anything
            untagging = false;

            initComponents();

            if (aTaggables.length == 1 && taggablesCount == 1)
            {
                ITaggable taggable = taggables[0];

                String title = taggable.getTitle();
                String description = taggable.getTagsDescription();
                String extended = taggable.getTagsExtended();
                String[] tags = taggable.getUserTags();
                String[] sharedTags = taggable.getSharedTags();
                String[] authorTags = taggable.getAuthorTags();

                setTaggableData(title, taggable.getTaggableLink().toString(),
                    description, extended, tags);

                final String flattenedTagString = createSummary(sharedTags, authorTags, false);
                final int fontStyle = isTagLabelItalics(sharedTags) ? Font.ITALIC : Font.PLAIN;
                lbSharedTags.setText(flattenedTagString);
                lbSharedTags.setToolTipText(flattenedTagString);
                lbSharedTags.setFont(lbSharedTags.getFont().deriveFont(fontStyle));

                if (autoLoad && sharedTags == null) fetchTagsAction.actionPerformed(null);

                super.open();

                if (!hasBeenCanceled()) propagateChanges(taggable, description, extended, tags);
            } else
            {
                // Multi-taggable management
                String[] descriptions = new String[taggablesCount];
                String[] extendeds = new String[taggablesCount];
                String[][] tags = new String[taggablesCount][];

                // Save original values
                for (int i = 0; i < taggables.length; i++)
                {
                    ITaggable taggable = taggables[i];
                    descriptions[i] = taggable.getTagsDescription();
                    extendeds[i] = taggable.getTagsExtended();
                    tags[i] = taggable.getUserTags();
                }

                setTaggableData(MessageFormat.format(Strings.message("tags.multitagging.title"),
                    Integer.toString(taggablesCount)),
                    null,
                    Strings.message("tags.multitagging.description"),
                    "", new String[0]);

                tfDescription.setEditable(false);
                btnFetch.setEnabled(false);

                super.open();

                if (!hasBeenCanceled())
                {
                    for (int i = 0; i < taggables.length; i++)
                    {
                        ITaggable taggable = taggables[i];
                        propagateChanges(taggable, descriptions[i], extendeds[i], tags[i]);
                    }
                }
            }
        }
    }

    /**
     * Filter out all taggables without links which can be tagged.
     *
     * @param aTaggables taggables.
     *
     * @return only these taggables with links.
     */
    private ITaggable[] filterLinkless(ITaggable[] aTaggables)
    {
        List<ITaggable> havingLinks = new ArrayList<ITaggable>(aTaggables.length);

        for (ITaggable taggable : aTaggables)
        {
            if (taggable.getTaggableLink() != null) havingLinks.add(taggable);
        }

        return havingLinks.toArray(new ITaggable[havingLinks.size()]);
    }

    /**
     * Accepts entry if it's valid and otherwise displays the error and moves back to editing.
     */
    public void doAccept()
    {
        // If we do untagging, everything else doesn't matter
        if (untagging || validateAndWarn()) super.doAccept();
    }

    /**
     * Move changes from dialog controls to the dialog.
     *
     * @param taggable          taggable object.
     * @param oldDescription    old description text.
     * @param oldExtended       old extended description text.
     * @param oldUserTags       old user tags.
     */
    private void propagateChanges(ITaggable taggable, String oldDescription,
                                  String oldExtended, String[] oldUserTags)
    {
        boolean changed = false;

        String newDescription = "";
        String newExtended = "";
        String[] newUserTags =  new String[0];

        // if not untagging, read what user has entered
        if (!untagging)
        {
            newDescription = getNewDescription(taggable);
            newExtended = tfExtended.getText();
            newUserTags = getEnteredTags();
        }

        if (!newDescription.equals(oldDescription))
        {
            taggable.setTagsDescription(newDescription);
            changed = true;
        }

        if (!newExtended.equals(oldExtended))
        {
            taggable.setTagsExtended(newExtended);
            changed = true;
        }

        if (!Arrays.equals(newUserTags, oldUserTags))
        {
            taggable.setUserTags(newUserTags);
            changed = true;
        }

        if (changed) taggable.setUnsavedUserTags(true);
    }

    /**
     * Returns the entered description if entry was allowed (single object mode) and
     * title of the object if description field wasn't editable (multi object mode).
     *
     * @param taggable  taggable.
     *
     * @return new description.
     */
    private String getNewDescription(ITaggable taggable)
    {
        return tfDescription.isEditable() ? tfDescription.getText() : taggable.getTitle();
    }

    /**
     * Returns the list of currently entered tags.
     *
     * @return entered tags.
     */
    private String[] getEnteredTags()
    {
        return StringUtils.split(tfTags.getText(), " ");
    }

    /**
     * Puts the list of tags in form controls.
     *
     * @param aNewTags new tags to put.
     */
    private void enterTags(String[] aNewTags)
    {
        String tagsText = aNewTags == null ? null : StringUtils.join(aNewTags, " ");
        tfTags.setText(tagsText);
    }

    /**
     * Creates tags summary from list of tags. Empty list of tags may mean
     * two things: error during fetching or not attempted to fetch yet.
     *
     * @param aSharedTags   tags to convert to summary.
     * @param anAuthorTags  tags, set by an author.
     * @param emptyIsError  <code>TRUE</code> to treat <code>NULL</code> as
     *                      a fetching failure, otherwise as non-fetched yet.
     *
     * @return summary string.
     */
    private static String createSummary(String[] aSharedTags, String[] anAuthorTags, boolean emptyIsError)
    {
        String summary;
        if (aSharedTags == null)
        {
            if (emptyIsError)
            {
                summary = Strings.message("tags.fetching.failed");
            } else
            {
                summary = Strings.message("tags.fetching.not.fetched");
            }
        } else
        {
            String[] aTags = new String[aSharedTags.length + anAuthorTags.length];
            for (int i = 0; i < aSharedTags.length; i++) aTags[i] = aSharedTags[i];
            for (int i = 0; i < anAuthorTags.length; i++)
            {
                aTags[aSharedTags.length + i] = anAuthorTags[i];
            }

            if (aTags.length == 0)
            {
                summary = Strings.message("tags.fetching.no.other.tags");
            } else
            {
                summary = TagsUtils.createTagsSummary(aTags);
            }
        }

        return summary;
    }

    /**
     * Returns TRUE if 'other tags' field should be italics because the text there is just a
     * message to the user, and FALSE if they are actual tags fetched.
     *
     * @param aTags - tags to analyze
     * @return true: if label should be italics, false otherwise
     */
    private boolean isTagLabelItalics(String[] aTags)
    {
        return (aTags == null || aTags.length == 0);
    }

    /**
     * Returns <code>TRUE</code> if entry is valid. If entry is invalid the dialog
     * box with error will be shown.
     *
     * @return <code>TRUE</code> if entry is valid.
     */
    private boolean validateAndWarn()
    {
        String error = validate(tfDescription.getText(), tfTags.getText());
        boolean valid = error == null;

        if (!valid)
        {
            JOptionPane.showMessageDialog(this, error, Strings.message("tags.validation.dialog.title"),
                JOptionPane.WARNING_MESSAGE);
        }

        return valid;
    }

    /**
     * Validates data entered and returns the error messages.
     *
     * @param aDescription  description entered.
     * @param aTags         targs list entered.
     *
     * @return error message or <code>NULL</code> if everything is fine.
     */
    static String validate(String aDescription, String aTags)
    {
        String error = null;

        if (aDescription == null || aDescription.trim().length() == 0)
        {
            error = ERR_DESCRIPTION_EMPTY;
        } else if (aTags == null || aTags.trim().length() == 0)
        {
            error = ERR_TAGS_EMPTY;
        } else if (aTags.indexOf('"') != -1)
        {
            error = ERR_MULTIWORD_TAGS;
        }

        return error;
    }

    /**
     * Calls the suggestion window with list of available tags.
     */
    private class SuggestTagsAction extends AbstractAction
    {
        /**
         * Creates action.
         */
        public SuggestTagsAction()
        {
            super(Strings.message("tags.suggest.action"));
            putValue(Action.SHORT_DESCRIPTION, Strings.message("tags.suggest.description"));

        }

        /**
         * Invoked when an action occurs.
         *
         * @param e action object.
         */
        public void actionPerformed(ActionEvent e)
        {
            SuggestedTagsDialog dialog = new SuggestedTagsDialog(AbstractTagsDialog.this);
            Set<String> usedTags = TagsRepository.getInstance().getUsedTags();
            String[] enteredTags = getEnteredTags();

            String[] newTags = dialog.open(usedTags, enteredTags);
            if (!Arrays.equals(newTags, enteredTags)) enterTags(newTags);
        }
    }

    /**
     * Suggested tags list dialog.
     */
    private static class SuggestedTagsDialog extends JDialog
    {
        private SelectionTable tagsTable;

        /**
         * Creates the dialog.
         *
         * @param owner owner dialog.
         */
        public SuggestedTagsDialog(Dialog owner)
        {
            super(owner, Strings.message("tags.suggest.dialog.title"));
            setModal(true);

            tagsTable = new SelectionTable();
            JScrollPane scrollPane = new JScrollPane(tagsTable);
            scrollPane.setPreferredSize(new Dimension(150, 150));

            Container container = getContentPane();
            container.setLayout(new BorderLayout());
            container.add(scrollPane, BorderLayout.CENTER);

            pack();
            ScreenUtils.locateOnScreenCenter(this);
        }

        /**
         * Opens the dialog and displays sorted list of all available tags plus
         * selected tags checked in it.
         *
         * @param availableTags set of all available tags.
         * @param selectedTags  list of selected tags.
         *
         * @return updated list of selected tags.
         */
        public String[] open(Set<String> availableTags, String[] selectedTags)
        {
            String[] availTags = availableTags.toArray(new String[availableTags.size()]);
            tagsTable.setTags(availTags, selectedTags);

            super.setVisible(true);

            return tagsTable.getSelectedTags();
        }
    }

    /**
     * Action which is fetching the tags using networker object.
     */
    private class FetchSharedTagsAction extends AbstractAction
    {
        private static final String THREAD_NAME = "Load Shared Tags";

        /**
         * Creates action.
         */
        public FetchSharedTagsAction()
        {
            super(Strings.message("tags.fetching.action"));
            putValue(Action.SHORT_DESCRIPTION, Strings.message("tags.fetching.description"));
        }

        /**
         * Invoked when action performed.
         *
         * @param e action object.
         */
        public void actionPerformed(ActionEvent e)
        {
            setEnabled(false);
            lbSharedTags.setText(Strings.message("tags.fetching.status.fetching"));

            Thread thread = new Thread(THREAD_NAME)
            {
                public void run()
                {
                    loadSharedTagsAndDisplay();
                }
            };
            thread.start();
        }

        /**
         * Loads shared tags, converts them into summary line and displays.
         */
        private void loadSharedTagsAndDisplay()
        {
            final ITaggable taggable = taggables[0];

            tagsStorage.loadSharedTags(taggable);
            String[] sharedTags = taggable.getSharedTags();
            String[] authorTags = taggable.getAuthorTags();

            // Get desired text and style for the label, return to EDT and update the label
            final String sharedTagsSummary = createSummary(sharedTags, authorTags, true);
            final int  fontStyle = isTagLabelItalics(sharedTags) ? Font.ITALIC : Font.PLAIN;
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    lbSharedTags.setText(sharedTagsSummary);
                    lbSharedTags.setFont(lbSharedTags.getFont().deriveFont(fontStyle));
                    lbSharedTags.setToolTipText(sharedTagsSummary);

                    // Update user tags if necessary
                    if (StringUtils.isEmpty(tfTags.getText()))
                    {
                        String[] tags = taggable.getUserTags();
                        if (tags != null && tags.length > 0)
                        {
                            tfTags.setText(StringUtils.join(tags, " "));
                        }
                    }

                    setEnabled(true);
                }
            });
        }
    }
}
