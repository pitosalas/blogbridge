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
// $Id$
//

package com.salas.bb.views.feeds.twitter;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.util.SystemUtils;
import com.jgoodies.uif.action.ActionManager;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.core.actions.ActionsTable;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IArticleListener;
import com.salas.bb.domain.NetworkFeed;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.DelegatingMouseListener;
import com.salas.bb.utils.uif.LinkLabel;
import com.salas.bb.utils.uif.UifUtilities;
import com.salas.bb.utils.uif.UpDownBorder;
import com.salas.bb.utils.uif.html.CustomHTMLEditorKit;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.views.feeds.ArticlePinControl;
import com.salas.bb.views.feeds.IArticleDisplay;
import com.salas.bb.views.feeds.IFeedDisplayConstants;
import com.salas.bb.views.feeds.html.HTMLArticleDisplay;
import com.salas.bb.views.feeds.html.IArticleDisplayConfig;
import com.salas.bb.twitter.ReplyAction;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Twitter article display.
 */
public class TwitterArticleDisplay extends JPanel implements IArticleListener, IArticleDisplay
{

    /** Name of the style we use to apply customized fonts. */
    private static final String TEXT_STYLE_NAME = "normal";
    private static final CellConstraints CELL_CONSTRAINTS = new CellConstraints();

    private final IArticleDisplayConfig config;
    private final IArticle              article;

    private int                         mode;
    private boolean                     selected;

    private JEditorPane                 tfText;
    private JLabel                      lbDate;
    private ArticlePinControl           lbPin;
    private LinkLabel                   lnReply;
    private JPanel                      pnlControls;

    /**
     * Creates the twitter article display component.
     *
     * @param article   article to display.
     * @param config    configuration.
     */
    public TwitterArticleDisplay(IArticle article, IArticleDisplayConfig config)
    {
        this.article = article;
        this.config = config;

        initComponents();

        setBorder(new UpDownBorder(HTMLArticleDisplay.COLOR_BORDER_LINE));

        setText();
        setViewMode(config.getViewMode());

        onThemeChange();
    }

    private void setText()
    {
        String name;
        String link = null;
        String text = article.getHtmlText();

        // Create new style for article and init it with default style settings
        HTMLDocument doc = (HTMLDocument)tfText.getDocument();
        doc.setBase(article.getLink());
        Style def = doc.getStyle("default");
        doc.addStyle(TEXT_STYLE_NAME, def);
        UifUtilities.setFontAttributes(doc, TEXT_STYLE_NAME, config.getTextFont());

        if (text != null)
        {
            if (text.matches("^[^:]+:.*$"))
            {
                // There's the user name at the beginning of the line, like "username: ...."
                int i = text.indexOf(':');
                name = StringUtils.substring(text, 0, i);
                text = StringUtils.substring(text, i + 2);
            } else
            {
                // Take the author
                name = article.getAuthor().split("\\s")[0];
            }
            link = "http://twitter.com/" + name;

            text = "<a href='" + link + "' rel='twitter'>" + name + "</a>: " + text;

            // Wrap "@name" and "#tag" with links
            text = text.replaceAll("@([\\w\\d]+)", "<a href=\"http://twitter.com/$1\">@$1</a>");
            text = text.replaceAll("#([\\w\\d]+)", "<a href=\"http://search.twitter.com/search?q=%23$1\">#$1</a>");

            // This is the special trick to outsmart Mac OS implementation of HTMLEditorKit.
            // Otherwise under some conditions the height will be equal to zero and
            // no text will be displayed.
            if (SystemUtils.IS_OS_MAC) text = "<p id='start'>" + text;
        }

        doc.putProperty(Document.StreamDescriptionProperty, ((NetworkFeed)article.getFeed()).getXmlURL());

        tfText.setText(text);
        UifUtilities.installTextStyle(tfText, TEXT_STYLE_NAME);

        if (link != null)
        {
            try
            {
                lnReply.setLink(new URL(link));
            } catch (MalformedURLException e)
            {
                lnReply.setVisible(false);
            }
        }
    }

    /**
     * Creates and initializes view components.
     */
    private void initComponents()
    {
        Date date = article.getPublicationDate();
        GlobalModel model = GlobalModel.SINGLETON;

        tfText  = createTextArea();
        lbDate  = new JLabel(SimpleDateFormat.getDateInstance().format(date), SwingConstants.LEFT);
        lbPin   = new ArticlePinControl(model.getSelectedGuide(), model.getSelectedFeed(), article);
        lnReply = new LinkLabel(Strings.message("twitter.article.reply"))
        {
            protected void doAction()
            {
                ReplyAction action = (ReplyAction)ActionManager.get(ActionsTable.CMD_TWITTER_REPLY);
                action.setUserURL(lnReply.getLink());
                action.actionPerformed(null);
            }
        };
        lnReply.setForeground(LinkLabel.HIGHLIGHT_COLOR);

        setLayout(new FormLayout("5dlu, min:grow, 5dlu", "5dlu, pref, pref, 5dlu"));

        pnlControls = new JPanel(new FormLayout("pref, 5dlu, pref, 5dlu, pref, 0:grow", "3dlu, pref"));
        pnlControls.add(lbPin, CELL_CONSTRAINTS.xy(1, 2));
        pnlControls.add(lbDate, CELL_CONSTRAINTS.xy(3, 2));
        pnlControls.add(lnReply, CELL_CONSTRAINTS.xy(5, 2));

        add(tfText, CELL_CONSTRAINTS.xy(2, 2));
        add(pnlControls, CELL_CONSTRAINTS.xy(2, 3));

        // Register delegating mouse listener
        DelegatingMouseListener ml = new DelegatingMouseListener(this, true);
        this.addMouseListener(ml);
        lbDate.addMouseListener(ml);
        tfText.addMouseListener(ml);
    }

    /**
     * Creates the pane.
     *
     * @return pane.
     */
    private JEditorPane createTextArea()
    {
        final JEditorPane pane = new JEditorPane();

        pane.setEditorKit(new CustomHTMLEditorKit());
        pane.setAlignmentX(0.0f);
        pane.setEditable(false);

        return pane;
    }

    /**
     * Updates the view according to read state.
     */
    private void updateReadState()
    {
        updateFonts();
    }


    /**
     * Updates foreground colors.
     */
    private void updateForegrounds()
    {
        lbDate.setForeground(config.getDateFGColor(selected));
    }

    /**
     * Updates background colors.
     */
    private void updateBackgrounds()
    {
        // Image / Item colors
        Color color = config.getGlobalBGColor(selected);
        setBackground(color);
        pnlControls.setBackground(color);
        tfText.setBackground(color);
    }

    /**
     * Updates fonts.
     */
    private void updateFonts()
    {
        Font dateFont = config.getDateFont();
        lbDate.setFont(dateFont);
        lnReply.setFont(config.getTextFont());

        doLayout();
    }

    // --------------------------------------------------------------------------------------------
    // IArticleDisplay
    // --------------------------------------------------------------------------------------------

    /**
     * Returns assigned article.
     *
     * @return article.
     */
    public IArticle getArticle()
    {
        return article;
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

    /**
     * Registers hyperlink listener.
     *
     * @param aListener listener.
     */
    public void addHyperlinkListener(HyperlinkListener aListener)
    {
        tfText.addHyperlinkListener(aListener);
    }

    /**
     * Invoked on view mode change.
     */
    public void onViewModeChange()
    {
        setViewMode(config.getViewMode());
        doLayout();
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
     * Sets the mode of this display.
     *
     * @param aMode mode of display.
     */
    public void setViewMode(int aMode)
    {
        mode = aMode;

        pnlControls.setVisible(aMode != IFeedDisplayConstants.MODE_MINIMAL);
    }

    /**
     * Sets <code>TRUE</code> if the display is currently selected.
     *
     * @param sel <code>TRUE</code> if the display is currently selected.
     */
    public void setSelected(boolean sel)
    {
        if (selected != sel)
        {
            selected = sel;
            updateBackgrounds();
        }
    }

    /** Invoked when font bias changes. */
    public void onFontBiasChange()
    {
        updateFonts();
    }

    /** Invoked on theme change. */
    public void onThemeChange()
    {
        updateFonts();
        updateBackgrounds();
        updateForegrounds();
    }

    /**
     * Sets <code>TRUE</code> if the display should become collapsed.
     *
     * @param col <code>TRUE</code> if the display is currently selected.
     */
    public void setCollapsed(boolean col)
    {
    }

    /**
     * Requests focus and returns the state.
     *
     * @return <code>FALSE</code> if focus isn't likely to be changed.
     */
    public boolean focus()
    {
        return this.requestFocusInWindow();
    }

    /** Invoked when article should update highlights. */
    public void updateHighlights()
    {
    }

    /** Updates a color code. */
    public void updateColorCode()
    {
    }

    // --------------------------------------------------------------------------------------------
    // IArticleListener
    // --------------------------------------------------------------------------------------------

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
            updateReadState();
        }
    }
}
