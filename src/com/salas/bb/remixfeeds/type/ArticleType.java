// BlogBridge -- RSS feed reader, manager, and web based service
// Copyright (C) 2002-2007 by R. Pito Salas
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
// $Id: ArticleType.java,v 1.4 2008/04/02 14:58:44 spyromus Exp $
//

package com.salas.bb.remixfeeds.type;

import com.salas.bb.core.FeatureManager;
import com.salas.bb.core.GlobalController;
import com.salas.bb.core.GlobalModel;
import com.salas.bb.domain.IArticle;
import com.salas.bb.remixfeeds.templates.Template;
import com.salas.bb.views.feeds.IFeedDisplay;
import com.salas.bb.views.mainframe.MainFrame;

import java.util.Arrays;
import java.util.Collection;

/**
 * Single article post-to-blog.
 */
public class ArticleType extends MultipleArticlesType
{
    /**
     * Returns <code>TRUE</code> if action of this type is available.
     *
     * @return <code>TRUE</code> if action of this type is available.
     */
    public boolean isAvailable()
    {
        GlobalController controller = GlobalController.SINGLETON;
        GlobalModel model = controller.getModel();
        FeatureManager fm = controller.getFeatureManager();

        boolean ptbEnabled = fm.isPtbEnabled();
        boolean articleSelected = model.getSelectedArticle() != null;
        boolean manyArticlesSelected = articleSelected && model.getSelectedArticles().length > 1;
        boolean blogRecordsPresent = model.getUserPreferences().getBloggingPreferences().getBlogsCount() > 0;

        return ptbEnabled && blogRecordsPresent && articleSelected &&
            (!manyArticlesSelected || fm.isPtbAdvanced());
    }

    /**
     * Returns the post data.
     *
     * @param template template.
     *
     * @return data.
     */
    public PostData getPostData(Template template)
    {
        String title = null;
        String text = null;
        IArticle source = null;

        IArticle[] articles = GlobalModel.SINGLETON.getSelectedArticles();
        if (articles != null && articles.length > 0)
        {
            // Prepare data for the first article
            IArticle article = articles[0];
            title            = article.getTitle();
            source           = article;

            String selectedText = getSelectedText();
            if (articles.length > 1)
            {
                // If there are more than one article, ask the user to choose
                // and then render
                title  = "";
                source = null;

                Collection<IArticle> articleList = Arrays.asList(articles);
                articleList = chooseArticles(articleList);

                text = template.render(articleList);
            } else
            {
                // Render a single article
                text = template.render(article, selectedText);
            }
        }

        return new PostData(title, text, source);
    }

    /**
     * Returns the selected text.
     *
     * @return selected text or empty.
     */
    private static String getSelectedText()
    {
        MainFrame frame = GlobalController.SINGLETON.getMainFrame();
        IFeedDisplay feedDisplay = frame.getArticlesListPanel().getFeedView();
        return feedDisplay.getSelectedText();
    }
}
