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
// $Id: AbstractPostEditor.java,v 1.7 2007/02/21 16:58:29 spyromus Exp $
//

package com.salas.bb.remixfeeds.editor;

import com.jgoodies.binding.list.ArrayListModel;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.util.Resizer;
import com.jgoodies.uif.util.SystemUtils;
import com.jgoodies.uifextras.util.PopupAdapter;
import com.salas.bb.domain.DirectFeed;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.remixfeeds.api.IWeblogAPI;
import com.salas.bb.remixfeeds.api.WeblogAPIException;
import com.salas.bb.remixfeeds.api.WeblogPost;
import com.salas.bb.remixfeeds.prefs.TargetBlog;
import com.salas.bb.utils.Constants;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.IconSource;
import com.salas.bb.utils.uif.ProgressSpinner;
import com.salas.bb.utils.uif.UifUtilities;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract post editor with basic functionality.
 */
public abstract class AbstractPostEditor extends AbstractDialog
{
    private static final Logger LOG = Logger.getLogger(PostEditor.class.getName());

    private static final Insets BTN_INSETS_MAC = new Insets(2, 2, 2, 2);
    private static final Insets BTN_INSETS = new Insets(0, 1, 0, 1);

    protected final Icon iconCut                  = IconSource.getIcon("editor.cut.icon");
    protected final Icon iconCopy                 = IconSource.getIcon("editor.copy.icon");
    protected final Icon iconPaste                = IconSource.getIcon("editor.paste.icon");
    protected final Icon iconAlignLeft            = IconSource.getIcon("editor.align.left.icon");
    protected final Icon iconAlignCenter          = IconSource.getIcon("editor.align.center.icon");
    protected final Icon iconAlignRight           = IconSource.getIcon("editor.align.right.icon");
    protected final Icon iconStyleBold            = IconSource.getIcon("editor.bold.icon");
    protected final Icon iconStyleItalic          = IconSource.getIcon("editor.italic.icon");
    protected final Icon iconStyleUnderline       = IconSource.getIcon("editor.underline.icon");
    protected final Icon iconStyleStrikethrough   = IconSource.getIcon("editor.strikethrough.icon");

    protected final PostToBlogAction actPostToBlog;

    protected JButton btnPostToBlog;
    protected JButton btnCancel;

    protected JTextField  tfTitle;
    protected JEditorPane tfText;
    protected JCheckBox   chDraft;
    protected JComboBox   cbInsertLink;

    /** Listener for the edits on the current document. */
    protected UndoableEditListener undoHandler = new UndoHandler();

    /** UndoManager that we add edits to. */
    protected UndoManager undo              = new UndoManager();
    protected UndoAction undoAction         = new UndoAction();
    protected RedoAction redoAction         = new RedoAction();
    protected Action cutAction              = new DefaultEditorKit.CutAction();
    protected Action copyAction             = new DefaultEditorKit.CopyAction();
    protected Action pasteAction            = new DefaultEditorKit.PasteAction();
    protected Action alignLeftAction        = new StyledEditorKit.AlignmentAction(null, StyleConstants.ALIGN_LEFT);
    protected Action alignCenterAction      = new StyledEditorKit.AlignmentAction(null, StyleConstants.ALIGN_CENTER);
    protected Action alignRightAction       = new StyledEditorKit.AlignmentAction(null, StyleConstants.ALIGN_RIGHT);
    protected Action boldAction;
    protected Action italicAction;
    protected Action strikethroughAction;
    protected Action underlineAction;

    // Source data
    private URL sourceURL;
    private String sourceTitle;
    protected ProgressSpinner spnProgress;

    public AbstractPostEditor(Frame frame, boolean richText)
    {
        super(frame, Strings.message("ptb.editor.post.to.blog"));

        actPostToBlog = new PostToBlogAction();

        btnPostToBlog = new JButton(actPostToBlog);
        btnCancel = createCancelButton();

        tfTitle = new JTextField();
        tfText = new JEditorPane();
        chDraft = new JCheckBox(Strings.message("ptb.editor.post.as.draft"));

        configureTextBox(richText);
        tfText.addMouseListener(createRightClickMenuAdapter());

        cbInsertLink = new JComboBox(new Object[] {
            Strings.message("ptb.editor.insert"),
            new InsertUserLinkAction(AbstractPostEditor.this, tfText, Strings.message("ptb.editor.insert.link.user")) });
        cbInsertLink.addItemListener(new InsertLinkBoxItemListener(tfText));

        spnProgress = new ProgressSpinner();
        spnProgress.setText(Strings.message("ptb.editor.posting"));

        setModal(false);
    }

    protected void configureTextBox(boolean html)
    {
        // Configure the kit and the document
        StyledEditorKit kit = html ? new HTMLEditorKit() : new StyledEditorKit();
        Document document = kit.createDefaultDocument();
        document.addUndoableEditListener(undoHandler);
        resetUndoManager();

        // Apply the kit and the document
        tfText.setEditorKit(kit);
        tfText.setDocument(document);

        // Configure actions
        if (html)
        {
            boldAction          = new StyledEditorKit.BoldAction();
            italicAction        = new StyledEditorKit.ItalicAction();
            strikethroughAction = new StrikeThroughAction();
            underlineAction     = new StyledEditorKit.UnderlineAction();
        } else
        {
            boldAction          = new InsertTextAction("plain-bold", "<strong>{0}</strong>");
            italicAction        = new InsertTextAction("plain-italic", "<i>{0}</i>");
            strikethroughAction = new InsertTextAction("plain-strike", "<del>{0}</del>");
            underlineAction     = new InsertTextAction("plain-under", "<u>{0}</u>");

            alignCenterAction.setEnabled(false);
            alignLeftAction.setEnabled(false);
            alignRightAction.setEnabled(false);
        }

    }

    /** Action for insert commands. */
    private class InsertTextAction extends StyledEditorKit.StyledTextAction
    {
        private final String fmt;

        /**
         * Creates the action.
         *
         * @param nm    action name.
         * @param fmt   format string.
         */
        public InsertTextAction(String nm, String fmt)
        {
            super(nm);
            this.fmt = fmt;
        }

        /** Invoked when an action occurs. */
        public void actionPerformed(ActionEvent e)
        {
            JEditorPane ed = getEditor(e);
            String sel = ed.getSelectedText();
            if (sel == null) sel = "";
            ed.replaceSelection(MessageFormat.format(fmt, sel));
        }
    }

    /**
     * Creates content pane.
     *
     * @return content pane.
     */
    protected JComponent buildContent()
    {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(buildMainPanel(), BorderLayout.CENTER);
        panel.add(buildButtonsBar(), BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates main content panel.
     *
     * @return main panel.
     */
    protected Component buildMainPanel()
    {
        Component controlPanel = buildControlPanel();

        BBFormBuilder builder = new BBFormBuilder("max(p;150dlu):grow, 0");
        builder.setDefaultDialogBorder();

        builder.append(tfTitle, 2);
        builder.appendUnrelatedComponentsGapRow(2);

        builder.append(controlPanel, 2);
        builder.appendRelatedComponentsGapRow(2);
        builder.appendRow("50dlu:grow");
        builder.append(new JScrollPane(tfText), 2, CellConstraints.FILL, CellConstraints.FILL);

        addCustomPanels(builder);

        return builder.getPanel();
    }

    /**
     * The hook to add more custom panels after the main part.
     *
     * @param builder builder.
     */
    protected void addCustomPanels(BBFormBuilder builder)
    {
    }

    /**
     * Creates buttons bar.
     *
     * @return bar.
     */
    protected Component buildButtonsBar()
    {
        ButtonBarBuilder builder = new ButtonBarBuilder();

        builder.getPanel().setBorder(Constants.DIALOG_BUTTON_BAR_BORDER);
        builder.addFixed(spnProgress);
        builder.addUnrelatedGap();
        builder.addGlue();
        addCustomButtons(builder);
        builder.addGriddedButtons(new JButton[] { btnPostToBlog, btnCancel });

        return builder.getPanel();
    }

    /**
     * The hook to add more buttons.
     *
     * @param builder builder.
     */
    protected void addCustomButtons(ButtonBarBuilder builder)
    {
    }

    /**
     * Builds RTF control panel.
     *
     * @return panel.
     */
    private Component buildControlPanel()
    {
        int spcInGroup = SystemUtils.IS_OS_MAC ? 0 : 4;
        int spcBetweenGroups = 8;

        String cols = MessageFormat.format(
            "p, {0}px, p, {0}px, p, {1}px, " +
            "p, {0}px, p, {0}px, p, {0}px, p, {1}px, " +
            "p, {1}px:grow, " +
            "p", spcInGroup, spcBetweenGroups);

        BBFormBuilder builder = new BBFormBuilder(cols);

        registerActionShortcut(undoAction, KeyEvent.VK_Z, Event.CTRL_MASK);
        registerActionShortcut(redoAction, KeyEvent.VK_Z, Event.CTRL_MASK | Event.SHIFT_MASK);

        builder.append(createButton(alignLeftAction, iconAlignLeft, -1, -1));
        builder.append(createButton(alignCenterAction, iconAlignCenter, -1, -1));
        builder.append(createButton(alignRightAction, iconAlignRight, -1, -1));
        builder.append(createButton(boldAction, iconStyleBold, KeyEvent.VK_B, Event.CTRL_MASK));
        builder.append(createButton(italicAction, iconStyleItalic, KeyEvent.VK_I, Event.CTRL_MASK));
        builder.append(createButton(underlineAction, iconStyleUnderline, KeyEvent.VK_U, Event.CTRL_MASK));
        builder.append(createButton(strikethroughAction, iconStyleStrikethrough, KeyEvent.VK_S, Event.CTRL_MASK));
        builder.append(cbInsertLink);

        addCustomControls(builder);

        return builder.getPanel();
    }

    /**
     * The hook to add custom control elements to the toolbar.
     *
     * @param builder builder.
     */
    protected void addCustomControls(BBFormBuilder builder)
    {
    }

    /**
     * Creates menu adapter.
     *
     * @return menu adapter.
     */
    private PopupAdapter createRightClickMenuAdapter()
    {
        return new PopupAdapter()
        {
            protected JPopupMenu buildPopupMenu(MouseEvent mouseEvent)
            {
                JPopupMenu menu = new JPopupMenu();

                menu.add(new JMenuItem(new ActionWrapper(cutAction, "ptb.editor.cut", iconCut)));
                menu.add(new JMenuItem(new ActionWrapper(copyAction, "ptb.editor.copy", iconCopy)));
                menu.add(new JMenuItem(new ActionWrapper(pasteAction, "ptb.editor.paste", iconPaste)));
                menu.addSeparator();
                menu.add(new JMenuItem(new ActionWrapper(alignLeftAction, "ptb.editor.align.left", iconAlignLeft)));
                menu.add(new JMenuItem(new ActionWrapper(alignCenterAction, "ptb.editor.align.center", iconAlignCenter)));
                menu.add(new JMenuItem(new ActionWrapper(alignRightAction, "ptb.editor.align.right", iconAlignRight)));
                menu.addSeparator();
                menu.add(new JMenuItem(new ActionWrapper(boldAction, "ptb.editor.style.bold", iconStyleBold)));
                menu.add(new JMenuItem(new ActionWrapper(italicAction, "ptb.editor.style.italic", iconStyleItalic)));
                menu.add(new JMenuItem(new ActionWrapper(underlineAction, "ptb.editor.style.underline", iconStyleUnderline)));
                menu.add(new JMenuItem(new ActionWrapper(strikethroughAction, "ptb.editor.style.strikethrough",
                    iconStyleStrikethrough)));

                return menu;
            }
        };
    }

    /**
     * Resets undo manager.
     */
    protected void resetUndoManager()
    {
        undo.discardAllEdits();
        undoAction.update();
        redoAction.update();
    }

    /**
     * Invoked when a user closes window
     */
    protected void doCloseWindow()
    {
        String[] options = {
            Strings.message("ptb.editor.warning.post"),
            Strings.message("ptb.editor.warning.discard"),
            Strings.message("ptb.editor.warning.cancel")};

        int res = JOptionPane.showOptionDialog(getParent(),
            Strings.message("ptb.editor.warning.wording"),
            getTitle(),
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            options,
            options[0]);

        switch (res)
        {
            case 0: // Post
                actPostToBlog.actionPerformed(null);
                break;
            case 1: // Discard
                doCancel();
                break;
            default:
                break;
        }
    }

    /**
     * Sets editor font.
     *
     * @param font font.
     */
    public void setEditorFont(Font font)
    {
        UifUtilities.setEditorFont(tfText, font);
    }

    /**
     * Sets the title of the post.
     *
     * @param title title.
     */
    public void setPostTitle(String title)
    {
        tfTitle.setText(title);
    }

    /**
     * Sets the text of the post.
     *
     * @param text text.
     */
    public void setPostText(String text)
    {
        tfText.setText(cleanHTML(text));
        tfText.setCaretPosition(0);
    }

    /**
     * Sets the draft of the post.
     *
     * @param draft draft.
     */
    public void setDraft(boolean draft)
    {
        chDraft.setSelected(draft);
    }

    /**
     * Notifies of a source of the original article.
     *
     * @param feedURL   URL.
     * @param feedTitle title.
     */
    public void setSource(URL feedURL, String feedTitle)
    {
        sourceURL = feedURL;
        sourceTitle = feedTitle;
    }

    /**
     * Updates the title of the dialog depending on the number of blogs selected.
     *
     * @param blogs selected blogs.
     */
    protected void updateTitle(TargetBlog[] blogs)
    {
        // TODO: localize!
        switch (blogs.length)
        {
            case 0:
                setTitle("Post to Blog");
                break;
            case 1:
                setTitle("Post to Blog: " + blogs[0].getTitle());
                break;
            default:
                setTitle("Posting to " + blogs.length + " blogs");
        }
    }

    /**
     * Sets source article and initializes links from it.
     *
     * @param source source article.
     */
    public void setSourceArticle(IArticle source)
    {
        // Remove all but the first item
        int ic = cbInsertLink.getItemCount();
        for (int i = ic - 1; i > 1; i--) cbInsertLink.removeItemAt(i);

        if (source == null) return;

        // Add article link item if the link is present
        if (source.getLink() != null)
        {
            InsertStaticLinkAction item = new InsertStaticLinkAction(
                tfText, source.getLink().toString(), source.getTitle(),
                Strings.message("ptb.editor.insert.link.article"));

            cbInsertLink.addItem(item);
        }

        // Add feed link item if present
        IFeed feed = source.getFeed();
        if (feed instanceof DirectFeed)
        {
            DirectFeed dfeed = (DirectFeed)feed;
            URL siteURL = dfeed.getSiteURL();

            if (siteURL != null)
            {
                InsertStaticLinkAction item = new InsertStaticLinkAction(
                    tfText, siteURL.toString(), feed.getTitle(),
                    Strings.message("ptb.editor.insert.link.feed"));

                cbInsertLink.addItem(item);
            }
        }
    }

    /**
     * Sets resizable flag.
     */
    protected void setResizable()
    {
        setResizable(true);
    }

    /**
     * Shortcut for button creation.
     *
     * @param act       button action.
     * @param icon      button icon.
     * @param key       key for shortcut.
     * @param modifiers key modifiers.
     *
     * @return button.
     */
    protected JButton createButton(Action act, Icon icon, int key, int modifiers)
    {
        JButton btn = new JButton(act);
        btn.setText(null);
        btn.setMargin(SystemUtils.IS_OS_MAC ? BTN_INSETS_MAC : BTN_INSETS);
        btn.setIcon(icon);

        registerActionShortcut(act, key, modifiers);

        return btn;
    }

    /**
     * Registers keyboard shortcut for action.
     *
     * @param act       action.
     * @param key       key for shortcut.
     * @param modifiers key modifiers.
     */
    protected void registerActionShortcut(Action act, int key, int modifiers)
    {
        if (key != -1)
        {
            if (modifiers == Event.CTRL_MASK && SystemUtils.IS_OS_MAC) modifiers = Event.META_MASK;

            KeyStroke ks = KeyStroke.getKeyStroke(key, modifiers);
            tfText.getKeymap().addActionForKeyStroke(ks, act);
        }
    }

    /**
     * Returns the list of selected target blog indexes.
     *
     * @return the list.
     */
    public List<Integer> getSelectedBlogIndices()
    {
        List<Integer> sel = new ArrayList<Integer>();
        sel.add(0);
        return sel;
    }

    /**
     * Enables / disables all controls during posting.
     *
     * @param en <code>TRUE</code> to enable.
     */
    protected void enableControls(boolean en)
    {
        tfTitle.setEnabled(en);
        tfText.setEnabled(en);
        chDraft.setEnabled(en);
        cbInsertLink.setEnabled(en);

        undoAction.setEnabled(en);
        redoAction.setEnabled(en);
        cutAction.setEnabled(en);
        copyAction.setEnabled(en);
        pasteAction.setEnabled(en);
        boldAction.setEnabled(en);
        italicAction.setEnabled(en);
        strikethroughAction.setEnabled(en);
        underlineAction.setEnabled(en);
        alignLeftAction.setEnabled(en);
        alignCenterAction.setEnabled(en);
        alignRightAction.setEnabled(en);

        btnPostToBlog.setEnabled(en);
        btnCancel.setEnabled(en);
    }

    /**
     * Resizing hook.
     *
     * @param component component.
     */
    protected void resizeHook(JComponent component)
    {
        component.setPreferredSize(Resizer.FOUR2THREE.fromHeight(500));
    }

    /**
     * Prepares text for sending to the server. The text contains unnecessary HTML headers and
     * some redundant styles we wish to clean.
     *
     * @param text text.
     *
     * @return the result.
     */
    static String cleanHTML(String text)
    {
        if (StringUtils.isNotEmpty(text))
        {
            // Remove leading and trailing tags, and leading spaces
            text = Pattern.compile("(^.*<body>|</body>.*$)", Pattern.DOTALL).matcher(text).replaceAll("");
            text = Pattern.compile("^\\s+|\\s+$", Pattern.MULTILINE).matcher(text).replaceAll("");

            // Replace carrige returns with spaces
            text = Pattern.compile("\n", Pattern.MULTILINE).matcher(text).replaceAll(" ");

            // De-entitize
            Matcher m = Pattern.compile("&#([0-9]+);").matcher(text);
            StringBuffer b = new StringBuffer(text.length());
            while (m.find())
            {
                String n = m.group(1);
                char ch = (char)Integer.parseInt(n);
                m.appendReplacement(b, "" + ch);
            }
            m.appendTail(b);
            text = b.toString();

            // Beautifying
            text = text.replaceAll("<(p|ol|ul)>", "\n\n$0");
            text = text.replaceAll("<br\\s*/?>", "$0\n");
            text = text.replaceAll("</(u|o)l>", "\n$0");
            text = text.replaceAll("<li>", "\n $0");

            text = text.trim();
        }

        return text;
    }

    /**
     * Scans the list of given categories and returns the one which is like the given.
     *
     * @param categories    categories.
     * @param cat           category to find among these in the list.
     *
     * @return a category or <code>NULL</code>.
     */
    protected static TargetBlog.Category findEqualCategory(ArrayListModel categories, TargetBlog.Category cat)
    {
        TargetBlog.Category found = null;

        if (cat != null)
        {
            for (Object o : categories)
            {
                TargetBlog.Category c = (TargetBlog.Category)o;
                if (c != null && c.equals(cat)) found = c;
            }
        }

        return found;
    }

    /** Strikethrough style action. */
    class StrikeThroughAction extends StyledEditorKit.StyledTextAction
    {
        /** Creates action. */
        public StrikeThroughAction()
        {
            super(StyleConstants.StrikeThrough.toString());
        }

        /**
         * Invoked when a user presses ctrl-s or clicks strikethrough button.
         *
         * @param ae event.
         */
        public void actionPerformed(ActionEvent ae)
        {
            JEditorPane editor = getEditor(ae);
            if (editor != null)
            {
                StyledEditorKit kit = getStyledEditorKit(editor);
                MutableAttributeSet attr = kit.getInputAttributes();
                boolean strikeThrough = !StyleConstants.isStrikeThrough(attr);
                SimpleAttributeSet sas = new SimpleAttributeSet();
                StyleConstants.setStrikeThrough(sas, strikeThrough);
                setCharacterAttributes(editor, sas, false);
            }
        }
    }

    /** Posts current contents to targetBlog. */
    protected class PostToBlogAction extends AbstractAction
    {
        private final boolean cont;

        /** Creates action. */
        public PostToBlogAction()
        {
            this(false);
        }

        /**
         * Defines an <code>Action</code> object with a default
         * description string and default icon.
         *
         * @param cont <code>TRUE</code> to continue on success.
         */
        public PostToBlogAction(boolean cont)
        {
            super(cont
                ? Strings.message("ptb.editor.post.and.continue")
                : Strings.message("ptb.editor.post.to.blog"));
            this.cont = cont;
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e event.
         */
        public void actionPerformed(ActionEvent e)
        {
            enableControls(false);
            spnProgress.start();

            final String title = tfTitle.getText();
            final String text = cleanHTML(tfText.getText());
            final boolean publish = !chDraft.isSelected();

            new Thread("Posting to Blog")
            {
                { setDaemon(true); }

                public void run()
                {
                    WeblogAPIException ex = null;

                    WeblogPost post = new WeblogPost(title, text, sourceURL, sourceTitle, publish);
                    addProperties(post);

                    List<Integer> ind = getSelectedBlogIndices();
                    for (int i : ind)
                    {
                        TargetBlog targetBlog = getBlogAt(i);
                        IWeblogAPI api = targetBlog.getApiType();

                        post.categories = getCategoriesForBlog(i);

                        // Perform a call
                        try
                        {
                            api.newPost(targetBlog, post);
                        } catch (WeblogAPIException e1)
                        {
                            ex = e1;
                            LOG.log(Level.WARNING, e1.getMessage(), e1);
                        }

                        if (ex != null) break;
                    }

                    // Report the results
                    final WeblogAPIException err = ex;
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            enableControls(true);
                            spnProgress.stop();

                            if (err == null) onSuccess(); else onError(err);
                        }
                    });
                }
            }.start();
        }

        /** Invoked when sending the post was successfull. */
        private void onSuccess()
        {
            if (!cont) doClose();
        }

        /**
         * Invoked when sending the post errored out.
         *
         * @param ex exception.
         */
        private void onError(WeblogAPIException ex)
        {
            JOptionPane.showMessageDialog(AbstractPostEditor.this,
                "<html><b>Posting to Blog failed</b>\n\n" + ex.getMessage(),
                AbstractPostEditor.this.getTitle(),
                JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Returns the list of categories selected for the given blog.
     *
     * @param blogIndex index of blog in list.
     *
     * @return categories.
     */
    protected abstract List<TargetBlog.Category> getCategoriesForBlog(int blogIndex);

    /**
     * Adds more specific properties before sending.
     *
     * @param post post.
     */
    protected void addProperties(WeblogPost post)
    {
    }

    /**
     * Returns a blog at a given index.
     *
     * @param i index.
     *
     * @return blog.
     */
    protected abstract TargetBlog getBlogAt(int i);

    /** Action wrapper to alter title and icon of the target action. */
    class ActionWrapper extends AbstractAction
    {
        private final Action target;

        /**
         * Create titled and iconed wrapper.
         *
         * @param target    target action.
         * @param titleID   resource ID of the title.
         * @param icon      icon.
         */
        public ActionWrapper(Action target, String titleID, Icon icon)
        {
            super(Strings.message(titleID), icon);
            this.target = target;
        }

        /**
         * Executes target action.
         *
         * @param e event.
         */
        public void actionPerformed(ActionEvent e)
        {
            target.actionPerformed(e);
        }
    }

    /**
     * Handler of UNDO / REDO operations.
     */
    class UndoHandler implements UndoableEditListener
    {
        /**
         * Messaged when the Document has created an edit, the edit is
         * added to <code>undo</code>, an instance of UndoManager.
         */
        public void undoableEditHappened(UndoableEditEvent e)
        {
            undo.addEdit(e.getEdit());
            undoAction.update();
            redoAction.update();
        }
    }

    /**
     * UNDO action.
     */
    class UndoAction extends AbstractAction
    {
        /** Constructor. */
        public UndoAction()
        {
            super("Undo");
            setEnabled(false);
        }

        /**
         * Invoked when a user presses ctrl-z or clicks undo button.
         *
         * @param e even.
         */
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                undo.undo();
            } catch (CannotUndoException ex)
            {
                System.out.println("Unable to undo: " + ex);
                ex.printStackTrace();
            }

            update();
            redoAction.update();
        }

        /** Updates the state. */
        protected void update()
        {
            setEnabled(undo.canUndo());
        }
    }

    /** Redo action. */
    class RedoAction extends AbstractAction
    {
        /** Creates action. */
        public RedoAction()
        {
            super("Redo");
            setEnabled(false);
        }

        /**
         * Invoked when a user presses ctrl-shift-z or clicks redo button.
         *
         * @param e event.
         */
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                undo.redo();
            } catch (CannotRedoException ex)
            {
                System.err.println("Unable to redo: " + ex);
                ex.printStackTrace();
            }
            update();
            undoAction.update();
        }

        /** Updates the state. */
        protected void update()
        {
            setEnabled(undo.canRedo());
        }
    }
}
