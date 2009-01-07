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
// $Id: HtmlParser.java,v 1.3 2006/01/08 05:10:10 kyank Exp $
//

package com.salas.bb.utils.htmlparser;

import java.io.Reader;
import java.io.IOException;

/**
 * Simplpified and fast parser of HTML that detects text, tags and entities separately.
 */
public class HtmlParser
{
    private static final int TEXT               = 0;
    private static final int TAG                = 1;
    private static final int ENTITY             = 2;

    private int mode;

    private StringBuffer full;
    private StringBuffer name;
    private StringBuffer temp;

    private boolean nameRead;

    private int     tagCharNum;
    private boolean closeTag;

    private IHtmlParserListener listener;

    private boolean swingMode;

    /**
     * Creates parser in non-Swing mode.
     */
    public HtmlParser()
    {
        this(false);
    }

    /**
     * Creates parser in specified mode.
     *
     * @param swingMode TRUE for Swing mode.
     */
    public HtmlParser(boolean swingMode)
    {
        this.swingMode = swingMode;

        full = new StringBuffer();
        name = new StringBuffer(10);
        temp = new StringBuffer(2);
    }

    /**
     * Parses HTML read from given reader and sending events to the specified listener.
     *
     * @param reader    reader to use for reading.
     * @param l         listener to notify.
     *
     * @throws IOException in case of any errors.
     */
    public void parse(Reader reader, IHtmlParserListener l)
        throws IOException
    {
        listener = l;

        init();

        // start reading
        l.onStart();

        // main loop
        int ch;
        while ((ch = reader.read()) != -1)
        {
            parse((char)ch);
        }

        if (mode == TEXT)
        {
            fireOnText(full.toString());
        } else if (mode == ENTITY)
        {
            fireOnEntity(name.toString(), full.toString());
        }

        // finish reading
        l.onFinish();
    }

    /**
     * First start initialization.
     */
    private void init()
    {
        mode = TEXT;
        clearBuffers();
    }

    /**
     * Parse single char.
     *
     * @param ch char.
     */
    private void parse(char ch)
    {
        switch (mode)
        {
            case TAG:
                parseTag(ch);
                break;
            case ENTITY:
                parseEntity(ch);
                break;
            default:
                parseText(ch);
                break;
        }
    }

    /**
     * Parse char when in TEXT mode.
     *
     * @param ch char.
     */
    private void parseText(char ch)
    {
        if (ch == '<')
        {
            fireOnText(full.toString());
            startTag(ch);
        } else if (ch == '&')
        {
            fireOnText(full.toString());
            startEntity(ch);
        } else
        {
            full.append(ch);
        }
    }

    /**
     * Parse char when in TAG mode.
     *
     * @param ch char.
     */
    private void parseTag(char ch)
    {
        if (swingMode)
        {
            if (ch == '/')
            {
                temp.append(ch);
            } else
            {
                if (ch != '>') full.append(temp);
                full.append(ch);
                clearBuffer(temp);
            }
        } else
        {
            full.append(ch);
        }

        tagCharNum++;

        if (ch == '>')
        {
            fireOnTag(name.toString(), full.toString(), closeTag);
            clearBuffers();

            mode = TEXT;
        } else if (!nameRead)
        {
            if (tagCharNum == 1 && ch == '/')
            {
                closeTag = true;
            } else if (ch == '/' || Character.isWhitespace(ch))
            {
                nameRead = true;
            } else
            {
                name.append(Character.toLowerCase(ch));
            }
        }
    }

    /**
     * Parse char when in ENTITY mode.
     *
     * @param ch char.
     */
    private void parseEntity(char ch)
    {
        boolean entityTerminator = ch == ';' || Character.isWhitespace(ch) || ch == '<' ||
            ch == '&';

        if (entityTerminator)
        {
            if (ch == ';') full.append(ch);

            fireOnEntity(name.toString(), full.toString());
            clearBuffers();

            if (ch == '<')
            {
                startTag(ch);
            } else if (ch == '&')
            {
                startEntity(ch);
            } else
            {
                if (ch != ';') full.append(ch);
                mode = TEXT;
            }
        } else
        {
            full.append(ch);
            name.append(ch);
        }
    }

    /**
     * Initialize before start of TAG-mode parsing.
     *
     * @param ch char.
     */
    private void startTag(char ch)
    {
        startBlock(TAG, ch);

        tagCharNum = 0;
        closeTag = false;
    }

    /**
     * Initialize before start of ENTITY-mode parsing.
     *
     * @param ch char.
     */
    private void startEntity(char ch)
    {
        startBlock(ENTITY, ch);
    }

    /**
     * Initialize before start of parsing in specified mode.
     *
     * @param aMode  mode to set.
     * @param ch    char.
     */
    private void startBlock(int aMode, char ch)
    {
        clearBuffers();

        this.mode = aMode;
        nameRead = false;
        full.append(ch);
    }

    /**
     * Fire text block parsing completion event.
     *
     * @param text text.
     */
    private void fireOnText(String text)
    {
        if (text == null || text.length() == 0) return;

        listener.onText(text);
    }

    /**
     * Fire tag parsing completion event.
     *
     * @param aName     lower-case name of tag.
     * @param aFull     full version of text.
     * @param aCloseTag TRUE if closing tag detected.
     */
    private void fireOnTag(String aName, String aFull, boolean aCloseTag)
    {
        listener.onTag(aName, aFull, aCloseTag);
    }

    /**
     * Fires entity parsing completion event.
     *
     * @param aName     name of entity in original case.
     * @param aFull     full entity text.
     */
    private void fireOnEntity(String aName, String aFull)
    {
        listener.onEntity(aName, aFull);
    }

    /**
     * Clears internal buffers.
     */
    private void clearBuffers()
    {
        clearBuffer(full);
        clearBuffer(name);
        clearBuffer(temp);
    }

    /**
     * Clears given buffer.
     *
     * @param buf buffer to clear.
     */
    private void clearBuffer(StringBuffer buf)
    {
        buf.delete(0, buf.length());
    }
}