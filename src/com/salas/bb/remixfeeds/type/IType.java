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
// $Id: IType.java,v 1.3 2008/03/31 15:29:14 spyromus Exp $
//

package com.salas.bb.remixfeeds.type;

import com.salas.bb.remixfeeds.templates.Template;

/**
 * Post-to-blog type.
 */
public interface IType
{
    /**
     * Returns <code>TRUE</code> if action of this type is available.
     *
     * @return <code>TRUE</code> if action of this type is available.
     */
    boolean isAvailable();
    
    /**
     * Returns the post data.
     *
     * @param template template.
     *
     * @return data.
     */
    PostData getPostData(Template template);

    /**
     * Says if dynamic template change is supported by this type (with SHIFT-click over the PTB command).
     *
     * @return TRUE if it is.
     */
    boolean isTemplateChangeSupported();
}
