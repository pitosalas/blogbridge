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
// $Id: SyntaxChecker.java,v 1.2 2008/04/03 08:53:23 spyromus Exp $
//

package com.salas.bb.remixfeeds.templates;

import static com.salas.bb.remixfeeds.templates.SyntaxChecker.Block.Type.*;
import org.hsqldb.lib.StringUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks the syntax of templates.
 */
public class SyntaxChecker
{
    private static final Pattern PATTERN_OPERATION      = Pattern.compile("^\\s*#\\s+(.+)\\s*$");
    private static final Pattern PATTERN_IF             = Pattern.compile("^if\\s+single(\\s+article)?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_ELSE           = Pattern.compile("^else$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_ENDIF          = Pattern.compile("^endif$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_FOR_EACH       = Pattern.compile("^for\\s+each\\s+article", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_ENDFOR         = Pattern.compile("^endfor$", Pattern.CASE_INSENSITIVE);

    /**
     * Checks the text of the template and returns the list of errors.
     *
     * @param text text to check.
     *
     * @return list of errors.
     */
    public static List<SyntaxError> validate(String text)
    {
        LinkedList<SyntaxError> errors = new LinkedList<SyntaxError>();

        // Initialize context
        Block root = new Block(null, TEXT);
        Context context = new Context(root);

        String[] lines = text.split("\n");
        int cnt = 1;
        for (String line : lines)
        {
            List<SyntaxError> lineErrors = validate(line, cnt, context);
            if (lineErrors != null && !lineErrors.isEmpty()) errors.addAll(lineErrors);

            // If further validation makes no sense
            if (context.currentBlock == null) break;
        }

        // Verify that the context is closed
        if (context.currentBlock != null && context.currentBlock != root)
        {
            String t = "IF";
            if (context.currentBlock.type == FOR) t = "FOR";

            errors.add(new SyntaxError(cnt, t + " block isn't closed"));
        }
        
        return errors;
    }

    /**
     * Validates a line.
     *
     * @param line      line to check.
     * @param num       line number.
     * @param context   context.
     *
     * @return list of errors and updated context.
     */
    private static List<SyntaxError> validate(String line, int num, Context context)
    {
        List<SyntaxError> errors = null;

        // Skip empty lines
        if (StringUtil.isEmpty(line)) return errors;

        if (line.matches("^\\s*#.*"))
        {
            // Operation
            Matcher m = PATTERN_OPERATION.matcher(line);
            if (m.matches())
            {
                String  operation = m.group(1);
                String  error = null;
                boolean stopOnErorr = true;

                Block currentBlock = context.currentBlock;
                Block.Type currentType = currentBlock.type;
                if (PATTERN_IF.matcher(operation).matches())
                {
                    // if
                    if (currentType != TEXT)
                    {
                        error = "IF cannot be included in other blocks";
                    } else
                    {
                        context.currentBlock = new Block(currentBlock, IF);
                    }
                } else if (PATTERN_ELSE.matcher(operation).matches())
                {
                    // else
                    if (currentType != IF)
                    {
                        error = "ELSE can be only inside IF-ENDIF block";
                    }
                } else if (PATTERN_ENDIF.matcher(operation).matches())
                {
                    // endif
                    if (currentType != IF)
                    {
                        error = "ENDIF can be only inside IF block";
                    } else
                    {
                        context.currentBlock = currentBlock.previous;
                    }
                } else if (PATTERN_FOR_EACH.matcher(operation).matches())
                {
                    // for each article
                    if (currentType == FOR)
                    {
                        error = "FOR cannot be included";
                    } else
                    {
                        context.currentBlock = new Block(currentBlock, FOR);
                    }
                } else if (PATTERN_ENDFOR.matcher(operation).matches())
                {
                    // endfor
                    if (currentType != FOR)
                    {
                        error = "ENDFOR must be closing FOR";
                    } else
                    {
                        context.currentBlock = currentBlock.previous;
                    }
                } else
                {
                    // unknown operation
                    error = "Unknown operation: '" + operation + "'";
                    stopOnErorr = false;
                }

                if (error != null)
                {
                    errors = new LinkedList<SyntaxError>();
                    errors.add(new SyntaxError(num, error));

                    if (stopOnErorr) context.currentBlock = null;
                }
            }
        } else
        {
            // Normal line -- do nothing for now
        }

        return errors;
    }

    /**
     * Context.
     */
    private static class Context
    {
        private Block currentBlock;

        private Context(Block currentBlock)
        {
            this.currentBlock = currentBlock;
        }
    }

    /**
     * Helper class for the hierarchy checking.
     */
    static class Block
    {
        enum Type { TEXT, IF, FOR };

        private Block previous;
        private Type  type;

        private Block(Block previous, Type type)
        {
            this.previous = previous;
            this.type = type;
        }
    }
}
