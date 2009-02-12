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
// $Id: ImageArticleDisplay.java,v 1.19 2008/02/28 12:36:17 spyromus Exp $
//

package com.salas.bb.views.feeds.image;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.util.ResourceUtils;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IArticleListener;
import com.salas.bb.utils.StringUtils;
import com.salas.bb.utils.uif.DelegatingMouseListener;
import com.salas.bb.utils.uif.ShadowBorder;
import com.salas.bb.utils.uif.UifUtilities;
import com.salas.bb.utils.uif.html.CustomHTMLEditorKit;
import com.salas.bb.views.feeds.IArticleDisplay;
import com.salas.bb.views.feeds.html.IArticleDisplayConfig;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Style;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.net.URL;

/**
 * The view showing the article as an image.
 */
class ImageArticleDisplay extends JPanel implements IArticleListener, IArticleDisplay
{
    private static final int EXCERPT_MIN_CHARS = 100;
    private static final int EXCERPT_MAX_CHARS = 200;
    private static final int EXCERPT_SENTENCES = 1;

    private static final String TEXT_STYLE_NAME = "normal";
    private static final Color COLOR_SHADOW_BORDER = Color.decode("#b0b0b0");
    private static final CellConstraints CELL_CONSTRAINTS = new CellConstraints();

    private final IImageFeedDisplayConfig config;
    private final IArticle                article;

    private AsyncImagePanel lbImage;
    private JLabel          lbTitle;
    private JEditorPane     tfText;

    private boolean         selected;
    private int             mode;
    private FormLayout      layout;

    /**
     * Creates view for an article.
     *
     * @param aArticle  the article.
     * @param aConfig   configuration.
     */
    public ImageArticleDisplay(IArticle aArticle, IImageFeedDisplayConfig aConfig)
    {
        article = aArticle;
        config = aConfig;

        URL imageURL = ImageArticleInterpreter.getImageURL(article);

        selected = false;

        initGUI(imageURL, article.getTitle(), article.getBriefText());
    }

    /**
     * Initializes GUI.
     *
     * @param aImageURL URL of the image to load.
     * @param aTitle    article title.
     * @param aText     article text.
     */
    private void initGUI(URL aImageURL, String aTitle, String aText)
    {
        IArticleDisplayConfig articleConfig = config.getArticleViewConfig();

        lbTitle = new JLabel(aTitle);
        lbTitle.setToolTipText(aTitle);

        tfText = new JEditorPane();
        tfText.setEditorKit(new CustomHTMLEditorKit());
        tfText.setText(StringUtils.excerpt(aText, EXCERPT_SENTENCES, EXCERPT_MIN_CHARS, EXCERPT_MAX_CHARS));
        tfText.setEditable(false);
        tfText.setToolTipText("<html>" + aText);

        HTMLDocument doc = (HTMLDocument)tfText.getDocument();
        doc.setBase(article.getLink());
        Style def = doc.getStyle("default");
        doc.addStyle(TEXT_STYLE_NAME, def);
        UifUtilities.setFontAttributes(doc, TEXT_STYLE_NAME, articleConfig.getTextFont());

        setViewMode(config.getViewMode());

        layout = new FormLayout("5px, center:pref, 5px", "5px, pref, 5px, pref, 5px, pref, 5px");
        setLayout(layout);

        aImageURL = aImageURL == null ? config.getNoImageURL() : aImageURL;
        Dimension dim = modeToDimension(config.getViewMode());
        lbImage = new AsyncImagePanel(aImageURL, dim.width, dim.height,
            new ShadowBorder(COLOR_SHADOW_BORDER), article.isRead());
        setTextComponentsWidth();

        add(lbImage, CELL_CONSTRAINTS.xy(2, 2));
        add(lbTitle, CELL_CONSTRAINTS.xy(2, 4));
        add(tfText, CELL_CONSTRAINTS.xy(2, 6));

        setToolTipText(aTitle == null ? null : "<html>" + aTitle + "</html>");

        // Register delegating mouse listener
        DelegatingMouseListener ml = new DelegatingMouseListener(this, true);
        this.addMouseListener(ml);
        lbImage.addMouseListener(ml);
        lbTitle.addMouseListener(ml);
        tfText.addMouseListener(ml);

        onThemeChange();
        updatePinnedState();
    }

    private void setTextComponentsWidth()
    {
        Dimension imgSize = lbImage.getPreferredSize();
        layout.setColumnSpec(2, new ColumnSpec("center:" + imgSize.width + "px"));
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
        if (lbImage != null)
        {
            lbImage.setImageSize(modeToDimension(aMode));
            setTextComponentsWidth();
        }

        lbTitle.setVisible(aMode > 0);
        tfText.setVisible(aMode > EXCERPT_SENTENCES);
    }

    /**
     * Returns dimension of image panel for the mode.
     *
     * @param aMode mode.
     *
     * @return dimension.
     */
    private static Dimension modeToDimension(int aMode)
    {
        Dimension imageDim;

        switch(aMode)
        {
            case 0:
                imageDim = new Dimension(50, 50);
                break;
            case EXCERPT_SENTENCES:
                imageDim = new Dimension(100, 100);
                break;
            default:
                imageDim = new Dimension(200, 200);
                break;
        }

        return imageDim;
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
     * Updates the view according to read state.
     */
    private void updateReadState()
    {
        lbImage.setSecondMode(article.isRead());
        updateFonts();
    }

    /**
     * Updates the view according to pinned state.
     */
    private void updatePinnedState()
    {
        lbImage.setBadge(article.isPinned()
            ? ResourceUtils.getIcon("pin.sel.icon").getImage()
            : null);
    }

    /**
     * Sets tooltip text to for the whole item cell.
     *
     * @param text the string to display; if the text is <code>null</code>, the tool tip is turned
     *             off for this component
     */
    public void setToolTipText(String text)
    {
        super.setToolTipText(text);
        lbImage.setToolTipText(text);
    }

    /**
     * Sets / resets the selection.
     *
     * @param sel <code>TRUE</code> to select item.
     */
    public void setSelected(boolean sel)
    {
        selected = sel;

        updateBackgrounds();
    }

    /**
     * Registers hyperlink listener.
     *
     * @param aListener listener.
     */
    public void addHyperlinkListener(HyperlinkListener aListener)
    {
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
     * Updates foreground colors.
     */
    private void updateForegrounds()
    {
        IArticleDisplayConfig cnf = config.getArticleViewConfig();
        lbTitle.setForeground(cnf.getTitleFGColor(selected));
    }

    /**
     * Updates background colors.
     */
    private void updateBackgrounds()
    {
        // Image / Item colors
        Color color = config.getItemBGColor(selected);
        setBackground(color);
        lbImage.setBackground(color);

        // Font colors
        IArticleDisplayConfig cnf = config.getArticleViewConfig();
        lbTitle.setBackground(cnf.getTitleBGColor(selected));
        tfText.setBackground(cnf.getTextBGColor(selected));
    }

    /**
     * Updates fonts.
     */
    private void updateFonts()
    {
        IArticleDisplayConfig cnf = config.getArticleViewConfig();

        // Title font
        Font font = cnf.getTitleFont(article.isRead());
        lbTitle.setFont(font);

        // Text area
        font = cnf.getTextFont();
        HTMLDocument doc = (HTMLDocument)tfText.getDocument();
        UifUtilities.setFontAttributes(doc, TEXT_STYLE_NAME, font);
        UifUtilities.installTextStyle(tfText, TEXT_STYLE_NAME);

        doLayout();
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

    // ---------------------------------------------------------------------------------------------
    // Article listener
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
                    updateReadState();
                }
            });
        } else if (IArticle.PROP_PINNED.equals(property))
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    updatePinnedState();
                }
            });
        }
    }
}
