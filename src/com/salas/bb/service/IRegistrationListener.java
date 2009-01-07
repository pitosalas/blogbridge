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
// $Id: IRegistrationListener.java,v 1.6 2006/02/17 09:23:21 spyromus Exp $
//

package com.salas.bb.service;

/**
 * Registration listener receives notifications on successful registrations.
 * It's necessary to decouple two panels (RegistrationPanel and SyncSettingsPanel).
 * Settings panel requires to know when registration succeed to change
 * E-mail and password in fields if they are not set.
 */
interface IRegistrationListener
{
    /**
     * Invoked when registration finished successfully.
     *
     * @param email    email used for registration.
     * @param password password.
     */
    void registeredSuccessfully(String email, String password);
}
