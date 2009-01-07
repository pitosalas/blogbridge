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
// $Id: TestFilterableList.java,v 1.2 2006/01/08 05:28:19 kyank Exp $
//

package com.salas.bb.utils;

import java.util.ArrayList;
import java.util.List;

import com.salas.bb.utils.FilterableList.Filter;

import junit.framework.TestCase;

/**
 * @see FilterableList
 */
public class TestFilterableList extends TestCase
{
    /**
     * Tests filter addition by priority.
     */
    public void testFilterPriority()
    {
        ArrayList list = new ArrayList();
        
        Filter f1 = new FilterStringLengthLessEq(5); 
        Filter f2 = new FilterStringLetter('4');
        
        FilterableList flist = new FilterableList(list);
        
        flist.addFilter(f1, 10);
        flist.addFilter(f2, 20);
        
        assertTrue("Filter f1 is not 0 position", flist.getFilterIndex(f1) == 0);
        assertTrue("Filter f2 is not 1 position", flist.getFilterIndex(f2) == 1);
        
        flist.removeFilter(f1);
        assertTrue("Filter f1 was removed but remain in filter list",
                flist.getFilterIndex(f1) == -1);
        assertTrue("Filter f2 is not 0 position after f1 remove",
                flist.getFilterIndex(f2) == 0);
        
        flist.addFilter(f1, 10);
        assertTrue("Filter f1 is not 0 position after f1 addition with higher priority",
                flist.getFilterIndex(f1) == 0);
        assertTrue("Filter f2 is not 1 position", flist.getFilterIndex(f2) == 1);
    }
    
    /**
     * Tests correct filter work.
     */
    public void testFilter1()
    {
        ArrayList list = new ArrayList();
        list.add("Welcome to Moldova");
        list.add("Hilly landscape");
        list.add("Fresh fruits");
        list.add("like");
        list.add("Apple");
        list.add("Peach");
        list.add("Tomato");
        list.add("...........");
        list.add("Good bye");
        
        for (int length = 0; length < 14; length++)
        {
            ArrayList resultList = new ArrayList();
            for (int i = 0, n = list.size(); i < n; i++)
            {
                String o = (String) list.get(i);
                
                if (o.length() <= length)
                {
                    resultList.add(o);
                }
            }
            
            FilterableList flist = new FilterableList(list);
            flist.addFilter(new FilterStringLengthLessEq(length), 0);
            
            assertTrue("Doesn't filter properly for length = " + length,
                equals(flist, resultList));
        }
    }
    
    /**
     * Tests correct filter work.
     */
    public void testFilter2()
    {
        ArrayList list = new ArrayList();
        list.add("The Moldova Foundation is a nonp");
        list.add("it organization establish");
        list.add(" DC area. Its main g");
        list.add("like");
        list.add("Apple");
        list.add("Peach");
        list.add("Tomato");
        list.add("...........");
        list.add("Good bye");
        
        char letter = 'b';
        
        ArrayList resultList = new ArrayList();
        for (int i = 0, n = list.size(); i < n; i++)
        {
            String o = (String)list.get(i);
            
            if (o.indexOf(letter) >= 0)
            {
                resultList.add(o);
            }
        }
        
        FilterableList flist = new FilterableList(list);
        flist.addFilter(new FilterStringLetter(letter), 0);
            
        assertTrue("Doesn't filter properly for letter = " + letter,
                equals(flist, resultList));
    }
    
    /**
     * Compares two lists on equality (lists are equal if they are with the
     * same size and with equal elements).
     * 
     * @param list1 list1.
     * @param list2 list2.
     * @return true if lists are equal.
     */
    private boolean equals(List list1, List list2)
    {
        if (list1.size() != list2.size()) return false;
        
        for (int i = 0, n = list1.size(); i < n; i++)
        {
            if (!list2.contains(list1.get(i)))
                return false;
        }
        
        return true;
    }
    
    /**
     * Filter for the string with length less and equal than specified.
     */
    private class FilterStringLengthLessEq extends FilterableList.Filter
    {
        private int length;
        
        /**
         * Creates <code>FilterStringLength</code>.
         * 
         * @param stringLength length of string.
         */
        public FilterStringLengthLessEq(final int stringLength)
        {
            super("string length");
            length = stringLength;
        }

        /**
         * @see com.salas.bb.utils.FilterableList.Filter#isMoreRestrictive(com.salas.bb.utils.FilterableList.Filter)
         */
        public boolean isMoreRestrictive(Filter filter)
        {
            return length < ((FilterStringLengthLessEq)filter).length;
        }

        /**
         * @see com.salas.bb.utils.FilterableList.Filter#accept(java.lang.Object)
         */
        public boolean accept(Object object)
        {
            return ((String)object).length() <= length;
        }
    }
    
    /**
     * Filter for the string with length less and equal than specified.
     */
    private class FilterStringLetter extends FilterableList.Filter
    {
        private char letter;
        
        /**
         * Creates <code>FilterStringLetter</code>.
         * 
         * @param aLetter letter.
         */
        public FilterStringLetter(final char aLetter)
        {
            super("string letter");
            letter = aLetter;
        }

        /**
         * @see com.salas.bb.utils.FilterableList.Filter#isMoreRestrictive(com.salas.bb.utils.FilterableList.Filter)
         */
        public boolean isMoreRestrictive(Filter filter)
        {
            return false; 
        }

        /**
         * @see com.salas.bb.utils.FilterableList.Filter#accept(java.lang.Object)
         */
        public boolean accept(Object object)
        {
            return ((String)object).indexOf(letter) >= 0;
        }
    }
}