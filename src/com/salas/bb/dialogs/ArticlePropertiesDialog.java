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
// $Id: ArticlePropertiesDialog.java,v 1.27 2006/09/29 07:59:27 spyromus Exp $
//

package com.salas.bb.dialogs;

import com.jgoodies.uif.AbstractDialog;
import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.utils.GuidesUtils;
import com.salas.bb.utils.DateUtils;
import com.salas.bb.utils.i18n.Strings;
import com.salas.bb.utils.uif.BBFormBuilder;
import com.salas.bb.utils.uif.HeaderPanelExt;
import com.salas.bb.utils.uif.SelectableLabel;

import javax.swing.*;
import java.awt.*;

/**
 * Display the Article properties dialog.
 */
public class ArticlePropertiesDialog extends AbstractDialog
{
    private IArticle article;

    /**
     * Constructs dialog.
     *
     * @param owner   owning frame.
     * @param anArticle article of which we want to show properties.
     */
    public ArticlePropertiesDialog(Frame owner, IArticle anArticle)
    {
        super(owner, Strings.message("article.properties.dialog.title"));
        setResizable();
        this.article = anArticle;
    }

    /**
     * Build a pretty XP-style white header.
     *
     * @return header object.
     */
    protected JComponent buildHeader()
    {
        return new HeaderPanelExt(
            Strings.message("article.properties.dialog.title"),
            Strings.message("article.properties.dialog.header"));
    }

    /**
     * Builds content of dialog.
     *
     * @return content.
     */
    protected JComponent buildContent()
    {
        JPanel content = new JPanel(new BorderLayout());
        content.add(buildBody(), BorderLayout.CENTER);
        content.add(buildButtonBarWithOKCancel(), BorderLayout.SOUTH);
        return content;
    }

    /**
     * Returns the JPanel which will be the body of this little dialog.
     *
     * @return body of the dialog.
     */
    private JComponent buildBody()
    {
        String link = article.getLink() == null ? null : article.getLink().toString();
        SelectableLabel tfLink = new SelectableLabel(link);
        String title = article.getTitle();
        SelectableLabel tfTitle = new SelectableLabel(title == null
            ? Strings.message("untitled") : title);

        IFeed selectedFeed = article.getFeed();

        JLabel lbGuide = new JLabel(GuidesUtils.getGuidesNames(selectedFeed.getParentGuides()));
        JLabel lbFeed = new JLabel(selectedFeed.getTitle());
        JLabel lbCreator = new JLabel(article.getAuthor());
        JLabel lbDate = new JLabel(DateUtils.dateToString(article.getPublicationDate()));
        JLabel lbSubject = new JLabel(article.getSubject());

        BBFormBuilder builder = new BBFormBuilder("pref, 4dlu, 200dlu:grow");
        builder.append(Strings.message("article.properties.guide"), lbGuide);
        builder.append(Strings.message("article.properties.feed"), lbFeed);
        builder.append(Strings.message("article.properties.title"), tfTitle);
        builder.append(Strings.message("article.properties.link"), tfLink);
        builder.append(Strings.message("article.properties.creator"), lbCreator);
        builder.append(Strings.message("article.properties.date"), lbDate);
        builder.append(Strings.message("article.properties.subject"), lbSubject);

        return builder.getPanel();
    }
}
