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
// $Id: TestAdvancedPreferencesPanel.java,v 1.2 2007/03/07 16:42:06 spyromus Exp $
//

package com.salas.bb.dialogs;

import junit.framework.TestCase;

/**
 * Tests adv. preferences panel methods.
 */
public class TestAdvancedPreferencesPanel extends TestCase
{
    /** Tests empty text. */
    public void testIsValidProxyExclusions_empty()
    {
        assertTrue(AdvancedPreferencesPanel.isValidProxyExclusions(null));
        assertTrue(AdvancedPreferencesPanel.isValidProxyExclusions(""));
        assertTrue(AdvancedPreferencesPanel.isValidProxyExclusions(" "));
    }

    /** Tests valid cases. */
    public void testIsValidProxyExclusions_valid()
    {
        // Names
        assertTrue(AdvancedPreferencesPanel.isValidProxyExclusions("abc"));
        assertTrue(AdvancedPreferencesPanel.isValidProxyExclusions("abc,bbc"));
        assertTrue(AdvancedPreferencesPanel.isValidProxyExclusions(" abc , bbc "));
        assertTrue(AdvancedPreferencesPanel.isValidProxyExclusions("abc.com"));
        assertTrue(AdvancedPreferencesPanel.isValidProxyExclusions("abc.com,bbc.com"));
        assertTrue(AdvancedPreferencesPanel.isValidProxyExclusions(" abc.com , bbc.com "));

        // Addresses
        assertTrue(AdvancedPreferencesPanel.isValidProxyExclusions("10.10.10.10"));
        assertTrue(AdvancedPreferencesPanel.isValidProxyExclusions("10.10.10.10,20.20.20.20"));
        assertTrue(AdvancedPreferencesPanel.isValidProxyExclusions(" 10.10.10.10 , 20.20.20.20 "));
    }
    
    /** Tests invalid cases. */
    public void testIsValidProxyExclusions_invalid()
    {
        // Names
        assertFalse(AdvancedPreferencesPanel.isValidProxyExclusions("abc com"));
        assertFalse(AdvancedPreferencesPanel.isValidProxyExclusions("abc.com."));
        assertFalse(AdvancedPreferencesPanel.isValidProxyExclusions("abc..com"));

        // Addresses
        assertFalse(AdvancedPreferencesPanel.isValidProxyExclusions("10"));
        assertFalse(AdvancedPreferencesPanel.isValidProxyExclusions("10.10"));
        assertFalse(AdvancedPreferencesPanel.isValidProxyExclusions("10.10.10"));
        assertFalse(AdvancedPreferencesPanel.isValidProxyExclusions("10.10.10."));
        assertFalse(AdvancedPreferencesPanel.isValidProxyExclusions("10.10.10.10."));
        assertFalse(AdvancedPreferencesPanel.isValidProxyExclusions("10.10 10.10"));
        assertFalse(AdvancedPreferencesPanel.isValidProxyExclusions("10.10, 10.10"));
        assertFalse(AdvancedPreferencesPanel.isValidProxyExclusions("10.10. 10.10"));
        assertFalse(AdvancedPreferencesPanel.isValidProxyExclusions("10.10.10.10.10"));
        assertFalse(AdvancedPreferencesPanel.isValidProxyExclusions("10.10.10.0"));
        assertFalse(AdvancedPreferencesPanel.isValidProxyExclusions("10.10.10.00"));
        assertFalse(AdvancedPreferencesPanel.isValidProxyExclusions("10.10.10.000"));
        assertFalse(AdvancedPreferencesPanel.isValidProxyExclusions("10.10.10.1000"));
        assertFalse(AdvancedPreferencesPanel.isValidProxyExclusions("10.10.10.255"));
        assertFalse(AdvancedPreferencesPanel.isValidProxyExclusions("10.10.10.260"));
    }
}
