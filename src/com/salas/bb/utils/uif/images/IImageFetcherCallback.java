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
// $Id: IImageFetcherCallback.java,v 1.2 2006/01/08 05:05:22 kyank Exp $
//

package com.salas.bb.utils.uif.images;

/**
 * Callback for <code>URLImageProducer</code> to report the moment when processing should start.
 * When the <code>ImageFetcher</code> will get this message it will put the producer in
 * the threads' tasks queue.
 */
interface IImageFetcherCallback
{
    /**
     * Invoked by producers to let the <code>ImageFetcher</code> know when to start
     * processing in background.
     *
     * @param producer producer, requesting the processing.
     */
    void startLoading(URLImageProducer producer);
}
