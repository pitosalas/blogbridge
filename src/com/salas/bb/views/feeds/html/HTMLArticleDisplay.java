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
// $Id: HTMLArticleDisplay.java,v 1.65 2008/02/29 06:17:46 spyromus Exp $
//

package com.salas.bb.views.feeds.html;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.util.SystemUtils;
import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IArticleListener;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.NetworkFeed;
import com.salas.bb.domain.prefs.ViewModePreferences;
import com.salas.bb.domain.utils.TextRange;
import com.salas.bb.sentiments.Calculator;
import com.salas.bb.sentiments.SentimentsConfig;
import com.salas.bb.sentiments.SentimentsFeature;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.swinghtml.TextProcessor;
import com.salas.bb.utils.uif.*;
import com.salas.bb.utils.uif.html.CustomImageView;
import com.salas.bb.views.feeds.ArticlePinControl;
import com.salas.bb.views.feeds.IArticleDisplay;
import com.salas.bb.views.feeds.IFeedDisplayConstants;
import com.salas.bb.views.feeds.IHighlightsAdvisor;
import com.salas.bb.views.mainframe.MainFrame;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A view for article.
 */
public class HTMLArticleDisplay extends JPanel implements IArticleListener, IArticleDisplay
{
    private static final Logger LOG = Logger.getLogger(HTMLArticleDisplay.class.getName());

    private static final String MSG_SIZING_DATE;
    private static final String MSG_SIZING_TIME;

    private static final ExecutorService executor;

    public  static final Color COLOR_BORDER_LINE = Color.decode("#dfdfdf"); //bfbfbf

    /** Name of the style we use to apply customized fonts. */
    private static final String TEXT_STYLE_NAME = "normal";
    private static final CellConstraints CELL_CONSTRAINTS = new CellConstraints();

    // WARNING: we need to have "pref" for title row (1st) height as JTextArea (used for multi-line
    //          titles) reports incorrect minimum dimensions after font change (read/unread)
    private static final String LAYOUT_ROWS = "0, pref, pref, min, min, min, 1px";

    /** URL of an image the mouse was clicked on. */
    public static URL clickImageURL;

    private final ColExIconLabel        lbSign;
    private final LinkExtendedLabel     lbTitle;
    private final JComponent            pnlInfo;
    private final JComponent            pnlFromFeed;
    private final JPanel                pnlContent;
    private final JEditorPane           tpText;

    private final IArticle              article;
    private final IArticleDisplayConfig config;

    private JLabel lbDate;
    private JLabel lbTime;
    private JLabel lbCategories;
    private JLabel lbFrom;
    private LinkLabel lbFeedTitle;
    private LinkLabel lbURL;
    private SentimentColorCode lbColorCode;

    /** Current view mode. */
    private int mode;

    /**
     * Current mode of text. When in title-only mode, text can be both in brief
     * and full state. This property holds the state of text.
     */
    private int textMode;

    /** Selection state of the view. */
    private boolean selected;
    /** Focus state of the view. */
    private boolean focused;

    /**
     * Map of string URL's to text ranges occupied with those links.
     * <code>NULL</code> means that the links were not collected yet.
     */
    private volatile Map<String, List<TextRange>> linksRanges;
    private final Object linksRangesLock = new Object();

    /** <code>TRUE</code> when view is collapsed (title only mode or user). */
    private boolean collapsed;

    /** Pin icon component. */
    private ArticlePinControl lbPin;

    static
    {
        Calendar c = new GregorianCalendar(2007, 11, 31, 23, 59);

        MSG_SIZING_DATE = getDateFormat().format(c.getTime()) + "2";
        MSG_SIZING_TIME = getTimeFormat().format(c.getTime()) + "2";

        executor = Executors.newFixedThreadPool(2, new ThreadFactory()
        {
            public Thread newThread(Runnable r)
            {
                Thread th = new Thread(r, "Article Tasks");
                th.setDaemon(true);
                th.setPriority(Thread.MIN_PRIORITY);
                return th;
            }
        });
    }

    /**
     * Creates view for some article.
     *
     * @param aArticle      article.
     * @param aConfig       configuration.
     * @param aShowFeed     <code>TRUE</code> to show origin feed.
     * @param aCallback     jump link clicks callback.
     * @param aEditorKit    the editor kit to use for rendering document.
     */
    public HTMLArticleDisplay(IArticle aArticle, IArticleDisplayConfig aConfig,
        boolean aShowFeed, IFeedJumpLinkClickCallback aCallback, EditorKit aEditorKit)
    {
        article = aArticle;
        config = aConfig;

        MouseListener ml = new DelegatingMouseListener(this);
        addMouseListener(ml);

        lbSign = new ColExIconLabel();
        lbSign.addMouseListener(new CollapseExpandListener());

        lbTitle = createTitle(ml);
        pnlInfo = createInfoPanel();
        pnlFromFeed = createFromFeedPanel(ml, aCallback, aShowFeed);
        tpText = createTextPane(ml, aEditorKit);
        pnlContent = createContentPanel(tpText, ml);
        lbCategories = createCategoriesLabel();
        lbURL = createURLLabel();

        selected = false;
        focused = false;

        // Create new style for article and init it with default style settings
        HTMLDocument doc = (HTMLDocument)tpText.getDocument();
        doc.setBase(article.getLink());
        Style def = doc.getStyle("default");
        doc.addStyle(TEXT_STYLE_NAME, def);
        UifUtilities.setFontAttributes(doc, TEXT_STYLE_NAME, config.getTextFont());

        // Set base URL to resolve relative links
        final IFeed feed = article.getFeed();
        if (feed instanceof NetworkFeed)
        {
            doc.putProperty(Document.StreamDescriptionProperty, ((NetworkFeed)feed).getXmlURL());
        }

        setupLayout();
        setBorder(new UpDownBorder(COLOR_BORDER_LINE));

        updateForegrounds();
        updateBackgrounds();
        updateBorder();
        updateFonts();

        mode = -1;
        textMode = -1;
        linksRanges = null;

        setViewMode(config.getViewMode());

        updateTitle();
        updateDateStatus();
    }

    /**
     * Returns currently selected text.
     *
     * @return text.
     */
    public String getSelectedText()
    {
        return tpText.getSelectedText();
    }

    /**
     * Creates categories label.
     *
     * @return label.
     */
    private JLabel createCategoriesLabel()
    {
        JLabel label = new JLabel();
        label.setForeground(Color.GRAY);

        String subject = article.getSubject();
        if (StringUtils.isEmpty(subject))
        {
            label.setEnabled(false);
        } else
        {
            label.setText(MessageFormat.format(Strings.message("articledisplay.categories"), subject));
        }


        return label;
    }

    /**
     * Creates URL label.
     *
     * @return label.
     */
    private LinkLabel createURLLabel()
    {
        LinkLabel label = new LinkLabel();
        label.setForeground(Color.GRAY);

        URL url = article.getLink();
        if (url == null)
        {
            label.setEnabled(false);
        } else
        {
            label.setText(url.toString());
            label.setLink(url);
        }


        return label;
    }

    /**
     * Creates a sentiment color code.
     *
     * @return code.
     */
    private SentimentColorCode createColorCode()
    {
        return new SentimentColorCode();
    }

    /**
     * Updates a color code.
     */
    public void updateColorCode()
    {
        if (lbColorCode == null) return;

        // Update color
        SentimentsConfig sconfig = Calculator.getConfig();
        Color color = article.isPositive() ? sconfig.getPositiveColor()
            : article.isNegative() ? sconfig.getNegativeColor() : null;
        lbColorCode.setColor(color);
        lbColorCode.setToolTipText("<html>" +
            "Pos words: " + article.getPositiveSentimentsCount() + "<br>" +
            "Neg words: " + article.getNegativeSentimentsCount());

        // Update visibility
        boolean cColorCode = isCompVisible(lbColorCode);
        boolean colorCode = isColorCodeVisible();
        lbColorCode.setVisible(colorCode);
        if (colorCode != cColorCode) rescaleTitle();
    }

    /**
     * Creates a panel if the showing feed is necessary.
     *
     * @param ml        mouse listener.
     * @param aCallback callback.
     * @param aShowFeed <code>TRUE</code> to show feed info.
     *
     * @return panel or NULL.
     */
    private JComponent createFromFeedPanel(MouseListener ml, IFeedJumpLinkClickCallback aCallback,
                                           boolean aShowFeed)
    {
        if (!aShowFeed) return null;

        IFeed feed = article.getFeed();

        lbFrom = new JLabel("from: ");
        lbFeedTitle = new FeedLabel(feed, aCallback);

        lbFrom.addMouseListener(ml);
// If we enable this listener, the feed menu will disappear
//        lbFeedTitle.addMouseListener(ml);

        JPanel panel = new JPanel(new FormLayout("p, p", "p"));
        panel.add(lbFrom, CELL_CONSTRAINTS.xy(1, 1));
        panel.add(lbFeedTitle, CELL_CONSTRAINTS.xy(2, 1));
        return panel;
    }

    /**
     * Creates info header panel.
     *
     * @return header panel.
     */
    private JComponent createInfoPanel()
    {
        Date date = article.getPublicationDate();

        JPanel panel = new JPanel(new FormLayout("p, p, 2px, p, p", "pref"));

        lbDate = new JLabel(getDateFormat().format(date), SwingConstants.LEFT);
        lbTime = new JLabel(getTimeFormat().format(date), SwingConstants.LEFT);

        GlobalModel model = GlobalModel.SINGLETON;
        lbPin = new ArticlePinControl(model.getSelectedGuide(), model.getSelectedFeed(), article);
        lbColorCode = createColorCode();

        panel.add(lbDate, CELL_CONSTRAINTS.xy(1, 1));
        panel.add(lbTime, CELL_CONSTRAINTS.xy(2, 1));
        panel.add(lbPin, CELL_CONSTRAINTS.xy(4, 1));
        panel.add(lbColorCode, CELL_CONSTRAINTS.xy(5, 1));

        updateColorCode();

        return panel;
    }

    /**
     * Returns date format used for the date output.
     *
     * @return date format.
     */
    private static DateFormat getDateFormat()
    {
        return SimpleDateFormat.getDateInstance();
    }

    /**
     * Returns time format used for the time output.
     *
     * @return time format.
     */
    private static DateFormat getTimeFormat()
    {
        return SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
    }

    /**
     * Updates date visibility status.
     */
    public void updateDateStatus()
    {
//        if (lbDate != null) lbDate.setVisible(config.isShowingDate());
    }

    /**
     * Puts all components together.
     */
    private void setupLayout()
    {
        setLayout(new FormLayout("5dlu, min, 5dlu, min:grow, 2dlu, left:min, 5dlu", LAYOUT_ROWS));

        add(lbSign, CELL_CONSTRAINTS.xy(2, 2, "c, t"));
        add(lbTitle, CELL_CONSTRAINTS.xy(4, 2));
        if (pnlInfo != null) add(pnlInfo, CELL_CONSTRAINTS.xy(6, 2, "c, t"));
        if (pnlFromFeed != null) add(pnlFromFeed, CELL_CONSTRAINTS.xyw(4, 3, 3));
        add(lbURL, CELL_CONSTRAINTS.xyw(4, 4, 3));
        add(lbCategories, CELL_CONSTRAINTS.xyw(4, 5, 3));
        add(pnlContent, CELL_CONSTRAINTS.xyw(4, 6, 3));
    }

    /**
     * Returns wrapped article.
     *
     * @return article.
     */
    public IArticle getArticle()
    {
        return article;
    }

    /**
     * Returns current article display view mode.
     *
     * @return mode.
     */
    public int getViewMode()
    {
        return mode;
    }

    /**
     * Sets a view model of this view.
     *
     * @param aMode new mode.
     */
    public void setViewMode(int aMode)
    {
        if (mode == aMode) return;

        mode = aMode;
        updateComponentsState();

        boolean isTitleOnlyMode = aMode == IFeedDisplayConstants.MODE_MINIMAL;

        // Hide content when in title-only mode
        boolean fo = tpText.isFocusOwner();
        pnlContent.setVisible(!isTitleOnlyMode);
        if (isTitleOnlyMode && fo) getParent().requestFocusInWindow();

        // If switching to non-title-only mode we may wish to
        // set text if it is currently in different mode.
        if (!isTitleOnlyMode && aMode != textMode)
        {
            setText(aMode == IFeedDisplayConstants.MODE_BRIEF);
            textMode = aMode;
        }

        // If it's the first time we switched to the FULL mode,
        // collect links from the text.
        synchronized (linksRangesLock)
        {
            if (aMode == IFeedDisplayConstants.MODE_FULL && linksRanges == null)
            {
                linksRanges = collectLinks((HTMLDocument)tpText.getDocument());
            }
        }

        // Collapse icons when in title-only mode
        lbSign.setCollapsed(isTitleOnlyMode);
        collapsed = isTitleOnlyMode;
    }

    /**
     * Updates the state of visual components of the title bar.
     */
    void updateComponentsState()
    {
        ViewModePreferences prefs = config.getViewModePreferences();
        boolean cDate = isCompVisible(lbDate);
        boolean date = prefs.isDateVisible(mode);
        boolean cTime = isCompVisible(lbTime);
        boolean time = prefs.isTimeVisible(mode) && date;
        boolean cCategories = isCompVisible(lbCategories);
        boolean categories = prefs.isCategoriesVisible(mode);
        boolean cURL = isCompVisible(lbURL);
        boolean url = prefs.isUrlVisible(mode);
        boolean cPin = isCompVisible(lbPin);
        boolean pin = prefs.isPinVisible(mode);
        boolean cColorCode = isCompVisible(lbColorCode);
        boolean colorCode = isColorCodeVisible();

        updateTitle();
        if (lbDate != null) lbDate.setVisible(date);
        if (lbTime != null) lbTime.setVisible(time);
        if (lbCategories != null) lbCategories.setVisible(lbCategories.isEnabled() && categories);
        if (lbURL != null) lbURL.setVisible(url);
        if (lbPin != null) lbPin.setVisible(pin);
        if (lbColorCode != null) lbColorCode.setVisible(colorCode);

        // Rescale title if the number of visible components increased
        if ((!cDate && date) || (!cTime && time) || (!cCategories && categories) ||
            (!cPin && pin) || (!cURL && url) || (!cColorCode && colorCode))
        {
            rescaleTitle();
        }
    }

    /**
     * Returns TRUE if the color code component has to be visible. Takes the availability of the feature
     * into account.
     *
     * @return TRUE if visible.
     */
    private boolean isColorCodeVisible()
    {
        ViewModePreferences prefs = config.getViewModePreferences();
        return prefs.isColorCodeVisible(mode) && SentimentsFeature.isAvailable();
    }

    /**
     * Returns <code>TRUE</code> if component is visible.
     *
     * @param cmp component.
     *
     * @return <code>TRUE</code> if visible.
     */
    private static boolean isCompVisible(Component cmp)
    {
        return cmp != null && cmp.isVisible();
    }

    /**
     * Changes mode according to collapse state.
     *
     * @param aCollapsed collapsed.
     */
    public void setCollapsed(boolean aCollapsed)
    {
        setViewMode(aCollapsed ? IFeedDisplayConstants.MODE_MINIMAL
            : textMode == -1 ? IFeedDisplayConstants.MODE_FULL : textMode);
    }

    /**
     * Returns <code>TRUE</code> if the display is collapsed.
     *
     * @return <code>TRUE</code> if the display is collapsed.
     */
    public boolean isCollapsed()
    {
        return collapsed;
    }

    /**
     * Collects links from text of the pane.
     *
     * @param doc document to process.
     *
     * @return map of lower-cased string links to <code>TextRange</code> objects.
     */
    private static Map<String, List<TextRange>> collectLinks(HTMLDocument doc)
    {
        Map<String, List<TextRange>> links = new HashMap<String, List<TextRange>>();

        HTMLDocument.Iterator tagIterator = doc.getIterator(HTML.Tag.A);
        while (tagIterator.isValid())
        {
            SimpleAttributeSet attrSet = (SimpleAttributeSet)tagIterator.getAttributes();

            String link = (String)attrSet.getAttribute(HTML.Attribute.HREF);
            if (link != null)
            {
                int startOffset = tagIterator.getStartOffset();
                int endOffset = tagIterator.getEndOffset();
                TextRange textRange = new TextRange(startOffset, endOffset);

                addLinkToMap(links, link, textRange);
            }

            tagIterator.next();
        }

        return links;
    }

    /**
     * Adds another link to the map.
     *
     * @param aLinks        map of links.
     * @param aLink         link to add.
     * @param aTextRange    corresponding text range.
     */
    static void addLinkToMap(Map<String, List<TextRange>> aLinks, String aLink, TextRange aTextRange)
    {
        List<TextRange> ranges = aLinks.get(aLink);
        if (ranges == null)
        {
            ranges = new LinkedList<TextRange>();
            aLinks.put(aLink, ranges);
        }
        ranges.add(aTextRange);
    }

    /**
     * Hyperlink listener.
     *
     * @param l listener.
     */
    public void addHyperlinkListener(HyperlinkListener l)
    {
        tpText.addHyperlinkListener(l);
    }

    /**
     * Repaints article text if is currently in the given mode.
     *
     * @param briefMode <code>TRUE</code> for brief mode, otherwise -- full mode.
     */
    public void repaintIfInMode(boolean briefMode)
    {
        if (mode == (briefMode ? IFeedDisplayConstants.MODE_BRIEF : IFeedDisplayConstants.MODE_FULL))
        {
            setText(briefMode);
        }
    }

    /**
     * Sets the text corresponding to given view mode.
     *
     * @param briefMode <code>TRUE</code> if in brief mode.
     */
    private void setText(boolean briefMode)
    {
        String text = getArticleText(briefMode);

        try
        {
            setText(text);
            updateHighlights();
        } catch (Throwable e)
        {
            LOG.log(Level.SEVERE, MessageFormat.format(
                Strings.error("ui.failed.to.set.article.text"),
                article.getLink()), e);

            setText(Strings.message("articledisplay.cant.show.text"));
        }
    }

    /**
     * Sets the text.
     *
     * @param text text.
     */
    private void setText(String text)
    {
        // This is the special trick to outsmart Mac OS implementation of HTMLEditorKit.
        // Otherwise under some conditions the height will be equal to zero and
        // no text will be displayed.
        if (SystemUtils.IS_OS_MAC) text = text == null ? null : "<p id='start'>" + text;

        tpText.setText(text);
        UifUtilities.installTextStyle(tpText, TEXT_STYLE_NAME);
    }

    /**
     * Returns text of the article.
     *
     * @param briefMode TRUE if currently in brief mode.
     *
     * @return text of the article.
     */
    private String getArticleText(boolean briefMode)
    {
        String text = briefMode ? article.getBriefText() : article.getHtmlText();
        return text == null ? Strings.message("articledisplay.no.text") : text;
    }

    /**
     * Changes the selection state. Updates foreground, background and border.
     *
     * @param sel <code>TRUE</code> to display the article as selected.
     */
    public void setSelected(boolean sel)
    {
        if (selected != sel)
        {
            selected = sel;
            updateBackgrounds();
            updateForegrounds();
            updateBorder();
        }
    }

    /**
     * Sets or resets the focus for this view.
     *
     * @param foc <code>TRUE</code> to display the article focused.
     */
    public void setFocused(boolean foc)
    {
        if (focused != foc)
        {
            focused = foc;
            updateBorder();
        }
    }

    /**
     * Invoked when highlights should be repainted.
     */
    public void updateHighlights()
    {
        String text = getText(tpText);
        HTMLDocument doc = (HTMLDocument)tpText.getDocument();

        UpdateHighlights task = new UpdateHighlights(text, doc);
        executor.execute(task);
    }

    /**
     * Returns text from the given pane.
     *
     * @param aPane text pane.
     *
     * @return text.
     */
    private static String getText(JEditorPane aPane)
    {
        String text;

        Document document = aPane.getDocument();
        try
        {
            text = document.getText(0, document.getLength());
        } catch (BadLocationException e)
        {
            text = "";
        }

        return text;
    }

    /**
     * Updates the border.
     */
    private void updateBorder()
    {
        // TODO do we need any borders here?
//        setBorder(config.getBorder(selected, focused));
    }

    /**
     * Updates foreground color.
     */
    private void updateForegrounds()
    {
        Color titleColor = config.getTitleFGColor(selected);
        Color dateColor = config.getDateFGColor(selected);

        lbTitle.setForeground(titleColor);
        if (lbDate != null) lbDate.setForeground(dateColor);
        if (lbTime != null) lbTime.setForeground(dateColor);
    }

    /**
     * Updates background color.
     */
    private void updateBackgrounds()
    {
        Color globalColor = config.getGlobalBGColor(selected);
        Color titleColor = config.getTitleBGColor(selected);
        Color textColor = config.getTextBGColor(selected);

        this.setBackground(globalColor);
        pnlContent.setBackground(globalColor);
        lbTitle.setBackground(titleColor);
        if (pnlInfo != null) pnlInfo.setBackground(titleColor);
        if (pnlFromFeed != null) pnlFromFeed.setBackground(titleColor);
        tpText.setBackground(textColor);
    }

    /**
     * Updates fonts of components.
     */
    private void updateFonts()
    {
        updateTitleFont();

        if (lbDate != null)
        {
            Font dateFont = config.getDateFont();
            lbDate.setFont(dateFont);
            if (lbTime != null) lbTime.setFont(dateFont);

            UifUtilities.setPreferredWidth(lbDate, UifUtilities.estimateWidth(dateFont, MSG_SIZING_DATE));
            UifUtilities.setPreferredWidth(lbTime, UifUtilities.estimateWidth(dateFont, MSG_SIZING_TIME));
        }
        if (lbFrom != null)
        {
            lbFrom.setFont(config.getDateFont().deriveFont(Font.BOLD));
            lbFeedTitle.setFont(config.getDateFont());
        }
        if (lbCategories != null) lbCategories.setFont(config.getDateFont());
        if (lbURL != null) lbURL.setFont(config.getDateFont());

        HTMLDocument doc = (HTMLDocument)tpText.getDocument();
        UifUtilities.setFontAttributes(doc, TEXT_STYLE_NAME, config.getTextFont());
        UifUtilities.installTextStyle(tpText, TEXT_STYLE_NAME);

        rescaleTitle();
    }

    /**
     * Updates the font of the title label.
     */
    private void updateTitleFont()
    {
        lbTitle.setFont(config.getTitleFont(article.isRead()));
    }

    /**
     * Updates the title of the view.
     */
    private void updateTitle()
    {
        String title = cutTitle(article.getTitle());
        if (!StringUtils.isEmpty(article.getAuthor()) &&
            config.getViewModePreferences().isAuthorVisible(mode))
        {
            title += " (" + article.getAuthor() + ")";
        }

        URL link = article.getLink();

        lbTitle.setText(title);
        lbTitle.setLink(link);

        // We need to set any tooltip text just to enable tooltip showing
        if (link == null) lbTitle.setToolTipText("");
    }

    /**
     * Cuts the title text according to configuration.
     *
     * @param aTitle title.
     *
     * @return title to use in visual component.
     */
    private String cutTitle(String aTitle)
    {
        if (aTitle == null)
        {
            aTitle = Strings.message("untitled");
        } else if (config.isSingleLineTitles())
        {
            int maxLength = config.getMaxSingleLineTitleLength();
            if (aTitle.length() > maxLength)
            {
                aTitle = aTitle.substring(0, maxLength) + "\u2026";
            }
        }

        return aTitle.trim();
    }

    /**
     * Returns <code>TRUE</code> if content panel is currently visible.
     *
     * @return <code>TRUE</code> if content panel is currently visible.
     */
    boolean isContentPanelVisible()
    {
        return pnlContent.isVisible();
    }

    /**
     * Returns listener.
     *
     * @return listener.
     */
    public IArticleListener getArticleListener()
    {
        return this;
    }

    /**
     * Returns visual component.
     *
     * @return visual component.
     */
    public Component getComponent()
    {
        return this;
    }

    // ---------------------------------------------------------------------------------------------
    // Components factorying
    // ---------------------------------------------------------------------------------------------

    /**
     * Creates content panel.
     *
     * @param textPane  text pane.
     * @param l         mouse listener.
     *
     * @return panel.
     */
    private static JPanel createContentPanel(Component textPane, MouseListener l)
    {
        // WARNING: we need to have "pref" for text row (1st) height as JEditorPane reports
        //          incorrect minimum dimensions on MacOS X and, probably, under JRE 1.5.
        FormLayout layout = new FormLayout("min:grow", "2dlu, pref, 5dlu");
        JPanel panel = new JPanel(layout);

        panel.add(textPane, CELL_CONSTRAINTS.xy(1, 2));
        panel.addMouseListener(l);

        return panel;
    }

    /**
     * Creates title component.
     *
     * @param l mouse listener.
     *
     * @return title.
     */
    private LinkExtendedLabel createTitle(MouseListener l)
    {
        LinkExtendedLabel comp = new CustomTitleLabel();
        comp.addMouseListener(l);
        comp.setAlignmentX(0.0f);

        return comp;
    }

    /**
     * Creates text pane.
     *
     * @param l         mouse listener.
     * @param editorKit the editor kit to use.
     *
     * @return text pane.
     */
    private JEditorPane createTextPane(MouseListener l, EditorKit editorKit)
    {
        JEditorPane pane = new EditorPane();
        pane.addMouseListener(l);
        pane.setAlignmentX(0.0f);
        pane.setEditorKit(editorKit);
        pane.setEditable(false);

        return pane;
    }

    /**
     * Invoked on theme change.
     */
    public void onThemeChange()
    {
        updateFonts();
        updateBackgrounds();
        updateBorder();
        updateForegrounds();
    }

    /**
     * Invoked on view mode change.
     */
    public void onViewModeChange()
    {
        setViewMode(config.getViewMode());
    }

    /**
     * Invoked on font bias change.
     */
    public void onFontBiasChange()
    {
        updateFonts();
    }

    /**
     * Requests that this <code>Component</code> gets the input focus. Refer to {@link
     * java.awt.Component#requestFocusInWindow() Component.requestFocusInWindow()} for a complete
     * description of this method. <p> If you would like more information on focus, see <a
     * href="http://java.sun.com/docs/books/tutorial/uiswing/misc/focus.html"> How to Use the Focus
     * Subsystem</a>, a section in <em>The Java Tutorial</em>.
     *
     * @return <code>false</code> if the focus change request is guaranteed to fail;
     *         <code>true</code> if it is likely to succeed
     *
     * @see java.awt.Component#requestFocusInWindow()
     * @see java.awt.Component#requestFocusInWindow(boolean)
     * @since 1.4
     */
    public boolean focus()
    {
        boolean focusGiven = false;

        if (pnlContent.isVisible())
        {
            focusGiven = tpText.isFocusOwner() || tpText.requestFocusInWindow();
        }

        return focusGiven;
    }

    // --------------------------------------------------------------------------------------------
    // Events
    // --------------------------------------------------------------------------------------------

    /**
     * Editor pane which isn't processing any keyboard events, but delegating them
     * to the parent of this view.
     */
    private class EditorPane extends JEditorPane
    {
        /** Overrides <code>processKeyEvent</code> to process events. * */
        protected void processKeyEvent(KeyEvent e)
        {
            delegateToParent(e);
        }

        /**
         * Processes mouse events occurring on this component by dispatching them to any registered
         * <code>MouseListener</code> objects, refer to {@link java.awt.Component#processMouseEvent(
         *java.awt.event.MouseEvent)} for a complete description of this method.
         *
         * @param e the mouse event
         *
         * @see java.awt.Component#processMouseEvent
         * @since 1.5
         */
        protected void processMouseEvent(MouseEvent e)
        {
            if (e.getID() == MouseEvent.MOUSE_PRESSED)
            {
                checkIfClickOverTheImage(e);
            } else if (e.getID() == MouseEvent.MOUSE_RELEASED)
            {
                clickImageURL = null;
            }
            
            super.processMouseEvent(e);
        }

        /**
         * Checks if the mouse was clicked over the image view and saves the link.
         *
         * @param e mouse event.
         */
        private void checkIfClickOverTheImage(MouseEvent e)
        {
            Point point = e.getPoint();

            // Version 1
            View view = this.getUI().getRootView(this);

            float x = (float)point.getX();
            float y = (float)point.getY();
            Shape allocation = getRootViewAllocation();

            CustomImageView imageView = getImageView(view, x, y, allocation);
            clickImageURL = (imageView != null) ? imageView.getImageURL() : null;
        }

        /**
         * Finds an image view behind the cursor and returns it unless there's no one.
         *
         * @param view          view to start traversing children from.
         * @param x             x coordinate of a click.
         * @param y             y coordinate of a click.
         * @param allocation    allocation shape.
         *
         * @return view or NULL.
         */
        private CustomImageView getImageView(View view, float x, float y, Shape allocation)
        {
            if (view instanceof CustomImageView) return (CustomImageView)view;

            int viewIndex = view.getViewIndex(x, y, allocation);
            if (viewIndex >= 0)
            {
                allocation = view.getChildAllocation(viewIndex, allocation);
                Rectangle rect = (allocation instanceof Rectangle) ?
                                 (Rectangle)allocation : allocation.getBounds();

                if (rect.contains(x, y))
                {
                    return getImageView(view.getView(viewIndex), x, y, allocation);
                }
            }

            return null;
        }

        /**
         * Returns the allocation shape of the editor root view.
         *
         * @return allocation shape.
         */
        protected Rectangle getRootViewAllocation()
        {
            Rectangle alloc = this.getBounds();

            if ((alloc.width > 0) && (alloc.height > 0))
            {
                alloc.x = alloc.y = 0;
                Insets insets = this.getInsets();
                alloc.x += insets.left;
                alloc.y += insets.top;
                alloc.width -= insets.left + insets.right;
                alloc.height -= insets.top + insets.bottom;
                return alloc;
            }

            return null;
        }

        /**
         * Delegating the event to the parent.
         *
         * @param e event.
         */
        private void delegateToParent(KeyEvent e)
        {
            if (e.getKeyCode() == 'C' &&
                (SystemUtils.IS_OS_MAC ? e.isMetaDown() : e.isControlDown()))
            {
                super.processKeyEvent(e);
            } else
            {
                UifUtilities.delegateEventToParent(HTMLArticleDisplay.this, e);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Invoked when the property of the article has been changed.
     *
     * @param article  article.
     * @param property property of the article.
     * @param oldValue old property value.
     * @param newValue new property value.
     */
    public void propertyChanged(IArticle article, String property, Object oldValue, Object newValue)
    {
        if (IArticle.PROP_READ.equals(property))
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    onReadChange();
                }
            });
        } else if (lbPin != null && IArticle.PROP_PINNED.equals(property))
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    onPinnedChange();
                }
            });
        } else if (lbColorCode != null &&
            (IArticle.PROP_POSITIVE.equals(property) || IArticle.PROP_NEGATIVE.equals(property)))
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    onSentimentChange();
                }
            });
        }
    }

    /**
     * Sets the font of the read status change.
     */
    private void onReadChange()
    {
        updateTitleFont();
    }

    /**
     * Invoked when pin state changes.
     */
    private void onPinnedChange()
    {
        lbPin.updateState();
    }

    /**
     * Invoked when the color or article sentiment analysis results change.
     */
    private void onSentimentChange()
    {
        updateColorCode();
    }

    /**
     * Feed label.
     */
    private static class FeedLabel extends LinkLabel
    {
        /** Maximum length of feed title. */
        private static final int MAX_TITLE_LENGTH = 50;

        private final IFeed feed;
        private final IFeedJumpLinkClickCallback callback;

        /**
         * Creates feed label.
         *
         * @param aFeed     feed label.
         * @param aCallback callback.
         */
        public FeedLabel(IFeed aFeed, IFeedJumpLinkClickCallback aCallback)
        {
            super();

            addMouseListener(GlobalController.SINGLETON.getMainFrame().getFeedLinkPopupAdapter());
            
            feed = aFeed;
            if (feed != null)
            {
                String title = aFeed.getTitle();
                if (StringUtils.isNotEmpty(title) && title.length() > MAX_TITLE_LENGTH)
                {
                    title = title.substring(0, MAX_TITLE_LENGTH) + "\u2026";
                }

                setText("<html><u>" + title);
                setHighlightLink(true);
            }

            callback = aCallback;
        }

        /**
         * Handles the event.
         *
         * @param e event.
         */
        protected void processMouseEvent(MouseEvent e)
        {
            MainFrame.feedLinkFeed = feed;
            try
            {
                super.processMouseEvent(e);
            } finally
            {
                MainFrame.feedLinkFeed = null;
            }
        }

        /**
         * Returns status to be displayed.
         *
         * @return status.
         */
        protected String getStatus()
        {
            return feed == null ? null : MessageFormat.format(Strings.message("articledisplay.link.jump.to.feed"),
                feed.getTitle());
        }

        /**
         * Jumps to the feed.
         */
        protected void doAction()
        {
            if (callback != null) callback.onFeedJumpLinkClicked(feed);
        }
    }

    /**
     * Listens for clicks over collapse/expand icon.
     */
    private class CollapseExpandListener extends MouseAdapter
    {
        /**
         * Invoked when mouse clicked.
         *
         * @param e event.
         */
        public void mouseClicked(MouseEvent e)
        {
            setCollapsed(!collapsed);
        }
    }

    /**
     * Moves and resizes this component. The new location of the top-left corner is specified by
     * <code>x</code> and <code>y</code>, and the new size is specified by <code>width</code> and
     * <code>height</code>.
     *
     * @param x      the new <i>x</i>-coordinate of this component
     * @param y      the new <i>y</i>-coordinate of this component
     * @param width  the new <code>width</code> of this component
     * @param height the new <code>height</code> of this component
     */
    public void setBounds(int x, int y, int width, int height)
    {
        // If width of the article display decreases -- decrease the width
        // of title as well
        if (getSize().width > width) rescaleTitle();

        super.setBounds(x, y, width, height);
    }

    /**
     * Rescales the title to recalculate the desired width.
     */
    private void rescaleTitle()
    {
        lbTitle.setMinimumSize(new Dimension(0, 0));
    }

    /**
     * Simple compoent that paints the color code indicator.
     */
    private static class SentimentColorCode extends JComponent
    {
        private static final Dimension SIZE = new Dimension(18, 13);
        private static final Insets INSETS = new Insets(1, 6, 2, 2);
        private Color color;

        @Override
        public Dimension getPreferredSize()
        {
            return SIZE;
        }

        /**
         * Sets the color.
         *
         * @param color color.
         */
        public void setColor(Color color)
        {
            this.color = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            int x = INSETS.left;
            int y = INSETS.top;
            int w = SIZE.width - INSETS.left - INSETS.right;
            int h = SIZE.height - INSETS.top - INSETS.bottom;

            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (color == null)
            {
                // Neutral
                g.setColor(Color.LIGHT_GRAY);
                g.drawOval(x, y, w, h);
            } else
            {
                // Not neutral
                g.setColor(color);
                g.fillOval(x, y, w, h);
            }
        }
    }

    /**
     * Custom label with the tool-tip made of the article text excerpt.
     */
    private class CustomTitleLabel extends LinkExtendedLabel
    {
        /** Number of characters the tool-tip has max. */
        private static final int TITLE_TOOLTIP_LENGTH = 90;

        private String tooltipText;

        /**
         * Returns the string to be used as the tooltip for <code>event</code>.
         *
         * @param event the event in question.
         *
         * @return the string to be used as the tooltip for <code>event</code>
         */
        public String getToolTipText(MouseEvent event)
        {
            synchronized(this)
            {
                if (tooltipText == null)
                {
                    tooltipText = article.getPlainText();
                    if (tooltipText != null)
                    {
                        tooltipText = TextProcessor.toPlainText(tooltipText).trim();
                        tooltipText = StringUtils.left(tooltipText, TITLE_TOOLTIP_LENGTH) + "...";
                    }

                    if (StringUtils.isEmpty(tooltipText)) tooltipText = Strings.message("articledisplay.no.description");
                }
            }

            return tooltipText;
        }

        /**
         * Returns number of clicks triggering opening the link in bowser.
         *
         * @return number of clicks.
         */
        protected int getTriggerClickCount()
        {
            // We return 0 instead of 2 because double clicking anywhere over the
            // article body will produce the same effect, so we just need to skip
            // this event.
            return config.isBrowseOnTitleDoubleClick() ? 0 : 1;
        }

        /**
         * Performs an action when triggered.
         */
        protected void doAction()
        {
            IArticle article = getArticle();
            GlobalModel model = GlobalModel.SINGLETON;

            // Mark an article as read and update stats
            GlobalController.readArticles(true,
                model.getSelectedGuide(),
                model.getSelectedFeed(),
                article);

            super.doAction();

            IFeed feed = HTMLArticleDisplay.this.article.getFeed();
            if (feed != null) feed.setClickthroughs(feed.getClickthroughs() + 1);
        }
    }

    /** Updates highlights. */
    private class UpdateHighlights implements Runnable
    {
        private final String text;
        private final HTMLDocument doc;

        /**
         * Creates an updates task.
         *
         * @param text  text to process.
         * @param doc   document to process.
         */
        public UpdateHighlights(String text, HTMLDocument doc)
        {
            this.text = text;
            this.doc = doc;
        }

        /**
         * Runs the task.
         */
        public void run()
        {
            TextRange[] searchRanges = null;
            Map<IArticleDisplayConfig.LinkType, List<TextRange>> linkRanges = null;

            // Collect keywords & search ranges
            IHighlightsAdvisor ha = config.getHighlightsAdvisor();
            if (ha != null)
            {
                searchRanges = ha.getSearchwordsRanges(text);
            }

            // TODO Allow repainting of highlights when in temp-full mode
            // Collect links ranges
            if (mode == IFeedDisplayConstants.MODE_FULL)
            {
                synchronized (linksRangesLock)
                {
                    if (linksRanges == null) linksRanges = collectLinks(doc);
                }

                linkRanges = new HashMap<IArticleDisplayConfig.LinkType, List<TextRange>>();
                for (Map.Entry<String, List<TextRange>> entry : linksRanges.entrySet())
                {
                    String link = entry.getKey();
                    IArticleDisplayConfig.LinkType type = config.getLinkType(link);

                    // Add the range to the list
                    List<TextRange> ranges = linkRanges.get(type);
                    if (ranges == null)
                    {
                        ranges = new LinkedList<TextRange>();
                        linkRanges.put(type, ranges);
                    }
                    ranges.addAll(entry.getValue());
                }
            }

            // Perform actual ranges selection
            final TextRange[] fSeaRanges = searchRanges;
            final Map<IArticleDisplayConfig.LinkType, List<TextRange>> fLinRanges = linkRanges;

            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    Highlighter.removeHighlights(tpText);
                    if (fSeaRanges != null) Highlighter.highlight(tpText, fSeaRanges, config.getSearchwordBGColor());
                    if (fLinRanges != null)
                    {
                        for (Map.Entry<IArticleDisplayConfig.LinkType, List<TextRange>> entry : fLinRanges.entrySet())
                        {
                            Color color = config.getLinkBGColor(entry.getKey());
                            if (color != null)
                            {
                                List<TextRange> ranges = entry.getValue();
                                for (TextRange range : ranges) Highlighter.highlight(tpText, range, color);
                            }
                        }
                    }
                }
            });
        }
    }
}
