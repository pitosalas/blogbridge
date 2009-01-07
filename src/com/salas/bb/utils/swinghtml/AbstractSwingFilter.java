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
// $Id: AbstractSwingFilter.java,v 1.4 2006/12/14 09:31:30 spyromus Exp $
//

package com.salas.bb.utils.swinghtml;

import com.salas.bb.utils.htmlparser.IHtmlParserListener;
import com.salas.bb.utils.htmlparser.utils.DefaultFilter;

import java.util.HashMap;
import java.util.List;

/**
 * Abstract filter of Swing related HTML.
 */
public abstract class AbstractSwingFilter extends DefaultFilter
{
    private HashMap     entitiesMap = new HashMap();
    private String      lastTagP;
    private String      whiteSpace;
    protected boolean   tagPArmed;
    private int         scriptTagCount;

    private static final String TAG_STRONG = "strong";
    private static final String TAG_B = "b";
    private static final String TAG_P = "p";
    private static final String TAG_B_CLOSE_FULL = "</b>";
    private static final String TAG_B_FULL = "<b>";

    /**
     * Creates a filter for a given listener.
     *
     * @param listener listener.
     */
    public AbstractSwingFilter(IHtmlParserListener listener)
    {
        super(listener);

        initEntitiesMap();

        tagPArmed = false;
        scriptTagCount = 0;
    }

    /**
     * Loads list of entities to be decoded in Unicode chars.
     */
    protected void initEntitiesMap()
    {
        String[] entities = { "hellip", "ndash", "ldquo", "rdquo", "lsquo", "rsquo" };
        String[] textuals = { "\u2026", "\u2013", "\u201c", "\u201d", "\u2018", "\u2019" };
        for (int i = 0; i < entities.length; i++)
        {
            String entity = entities[i];
            String textual = textuals[i];

            entitiesMap.put(entity, textual);
        }
    }

    /**
     * Returns the list of allowed tags.
     *
     * @return allowed tags.
     */
    protected abstract List getAllowedTags();

    /**
     * Invoked when tag detected.
     *
     * @param name     name of tag.
     * @param full     full tag text.
     * @param closeTag TRUE if tag is closing tag.
     */
    public void onTag(String name, String full, boolean closeTag)
    {
        if ("script".equals(name))
        {
            scriptTagCount = scriptTagCount + (closeTag ? -1 : 1);
            return;
        } else if (scriptTagCount > 0) return;

        if (!isAllowedTag(name)) return;

        if (name.equals(TAG_P))
        {
            if (!closeTag)
            {
                lastTagP = full;
                tagPArmed = true;
                whiteSpace = null;
            }

            return;
        } else
        {
            outputTagPIfNecessary(false);

            if (name.equals(TAG_STRONG))
            {
                name = TAG_B;
                if (closeTag) full = TAG_B_CLOSE_FULL; else full = TAG_B_FULL;
            }
        }

        super.onTag(name, full, closeTag);
    }

    /**
     * Invoked when entity detected.
     *
     * @param entity entity name.
     * @param full   full entity text.
     */
    public void onEntity(String entity, String full)
    {
        if (scriptTagCount > 0) return;

        outputTagPIfNecessary(false);

        String text = null;

        int length = entity.length();
        if (length > 1 && entity.charAt(0) == '#')
        {
            int radix;
            String valueString;

            if (length > 2 && entity.charAt(1) == 'x')
            {
                radix = 16;
                valueString = entity.substring(2);
            } else
            {
                radix = 10;
                valueString = entity.substring(1);
            }

            try
            {
                int value = Integer.parseInt(valueString, radix);
                text = Character.toString((char)value);
            } catch (Exception e)
            {
                // Failed to convert entity -- will be outputted as is
            }
        } else
        {
            text = (String)entitiesMap.get(entity);
        }

        if (text != null)
        {
            super.onText(text);
        } else
        {
            super.onEntity(entity, full);
        }
    }

    /**
     * Invoked when text block detected.
     *
     * @param text text.
     */
    public void onText(String text)
    {
        if (scriptTagCount > 0) return;

        if (tagPArmed && text.trim().length() == 0)
        {
            whiteSpace = text;
            return;
        }

        outputTagPIfNecessary(false);

        super.onText(text);
    }

    /**
     * Invoked on document parsing finish.
     */
    public void onFinish()
    {
        outputTagPIfNecessary(true);

        super.onFinish();
    }

    /**
     * Outputs P-tag if they are currently groupped.
     *
     * @param noWhiteSpace TRUE when no whitespace requied.
     */
    private void outputTagPIfNecessary(boolean noWhiteSpace)
    {
        if (tagPArmed)
        {
            super.onTag(TAG_P, lastTagP, false);
            if (whiteSpace != null)
            {
                if (!noWhiteSpace) super.onText(whiteSpace);
                whiteSpace = null;
            }

            lastTagP = null;
            tagPArmed = false;
        }
    }

    /**
     * Returns TRUE if tag in the list of allowed tags.
     *
     * @param tag tag to check.
     *
     * @return TRUE if tag in the list of allowed tags.
     */
    private boolean isAllowedTag(String tag)
    {
        return getAllowedTags().contains(tag);
    }
}
