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
// $Id: FilterableList.java,v 1.5 2008/04/01 09:24:37 spyromus Exp $
//
package com.salas.bb.utils;

import com.salas.bb.domain.IFeed;
import com.salas.bb.utils.i18n.Strings;

import java.util.*;

/**
 * A simple implementation of filterable list.
 * 
 * It is a wrapper class for a source {@link java.util.List}
 * (which is passed as a constructor parameter) that allows to filter it
 * list by adding filters {@link com.salas.bb.utils.FilterableList.Filter}.
 */
public class FilterableList implements List<IFeed>
{
    private List<IFeed>     sourceList;
    private List<IFeed>     filteredSourceList;
    private MultiFilterAnd  filter;
    
    /**
     * Creates <code>FilterableList</code>.
     * 
     * @param aSourceList the list to be filtered
     */
    public FilterableList(List<IFeed> aSourceList)
    {
        sourceList = aSourceList;
        filteredSourceList = clone(aSourceList, new ArrayList<IFeed>(aSourceList.size()));
        filter = new MultiFilterAnd("milti filter");
    }
    
    /**
     * Adds a filter.
     * 
     * @param aFilter   the filter to be added.
     * @param priority  filter's priority (0 - highest).
     * 
     * Note: The objects are filtered starting with filter with
     * highest priority(i.e. 0). 
     */
    public void addFilter(Filter aFilter, final int priority)
    {
        final boolean becomeMoreRestrictive = filter.addFilter(aFilter, priority);
        
        if (becomeMoreRestrictive)
        {
            filteredSourceList = getFilteredList(aFilter, filteredSourceList);
        } else
        {
            filteredSourceList = getFilteredList(filter, sourceList);
        }
    }
    
    /**
     * Removes a filter.
     * 
     * @param aFilter   the filter to be removed.
     */
    public void removeFilter(Filter aFilter)
    {
        if (filter.removeFilter(aFilter))
        {
            filteredSourceList = getFilteredList(filter, sourceList);
        }
    }
    
    /**
     * Gets the filter index.
     * 
     * @param aFilter filter.
     * @return index of the filter (-1 if filter is not found).
     */
    public int getFilterIndex(Filter aFilter)
    {
        return filter.getFilterIndex(aFilter);
    }
    
    /**
     * Gets the list size.
     * 
     * @return list size.
     * @see java.util.List#size()
     */
    public int size()
    {
        return filteredSourceList.size();
    }

    /**
     * Clears the list.
     * 
     * @see java.util.List#clear()
     */
    public void clear()
    {
        sourceList.clear();
        filteredSourceList.clear();
    }

    /**
     * Is empty list.
     * 
     * @see java.util.List#isEmpty()
     */
    public boolean isEmpty()
    {
        return filteredSourceList.isEmpty();
    }

    /**
     * Converts list to array.
     * 
     * @see java.util.List#toArray()
     */
    public Object[] toArray()
    {
        return filteredSourceList.toArray();
    }

    /**
     * Gets the element from list at index position.
     * 
     * @see java.util.List#get(int)
     */
    public IFeed get(int index)
    {
        return filteredSourceList.get(index);
    }

    /**
     * Removes the element at index position.
     * 
     * @see java.util.List#remove(int)
     */
    public IFeed remove(int index)
    {
        final IFeed object = filteredSourceList.remove(index);
        sourceList.remove(object);
        
        return object;
    }

    /**
     * Adds the element to the list.
     * 
     * @see java.util.List#add(int, java.lang.Object)
     */
    public void add(int index, IFeed element)
    {
        if (filter.accept(element))
        {
            final IFeed objectAtIndex = filteredSourceList.get(index);
            
            filteredSourceList.add(index, element);
            sourceList.add(sourceList.indexOf(objectAtIndex), element);
        } else
        {
            sourceList.add(index, element);
        }
    }
    
    /**
     * Gets the index(in the filtered list) of the object.
     * 
     * @see java.util.List#indexOf(java.lang.Object)
     */
    public int indexOf(Object o)
    {
        return filteredSourceList.indexOf(o);
    }

    /**
     * Gets the last index(in the filtered list) of the object.
     * 
     * @see java.util.List#lastIndexOf(java.lang.Object)
     */
    public int lastIndexOf(Object o)
    {
        return filteredSourceList.lastIndexOf(o);
    }

    /**
     * Adds the object to list.
     * 
     * @see java.util.List#add(java.lang.Object)
     */
    public boolean add(IFeed o)
    {
        if (filter.accept(o)) filteredSourceList.add(o);
        sourceList.add(o);
        
        return true;
    }

    /**
     * Does list contain the object.
     * 
     * @see java.util.List#contains(java.lang.Object)
     */
    public boolean contains(Object o)
    {
        return filteredSourceList.contains(o);
    }

    /**
     * Removes the object from list.
     * 
     * @see java.util.List#remove(java.lang.Object)
     */
    public boolean remove(Object o)
    {
        boolean containElement = filteredSourceList.remove(o);
        
        if (containElement)
        {
            sourceList.remove(o);
        }
        
        return containElement;
    }

    public boolean addAll(Collection<? extends IFeed> c)
    {
        return false;
    }

    public boolean addAll(int index, Collection<? extends IFeed> c)
    {
        return false;
    }

    /**
     * @see java.util.List#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection<?> c)
    {
        return false;
    }

    /**
     * @see java.util.List#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection<?> c)
    {
        return false;
    }

    /**
     * @see java.util.List#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection<?> c)
    {
        return false;
    }

    /**
     * @see java.util.List#iterator()
     */
    public Iterator<IFeed> iterator()
    {
        return filteredSourceList.iterator();
    }

    /** 
     * @see java.util.List#subList(int, int)
     */
    public List<IFeed> subList(int fromIndex, int toIndex)
    {
        return null;
    }

    /** 
     * @see java.util.List#listIterator()
     */
    public ListIterator<IFeed> listIterator()
    {
        return filteredSourceList.listIterator();
    }

    /** 
     * @see java.util.List#listIterator(int)
     */
    public ListIterator<IFeed> listIterator(int index)
    {
        return filteredSourceList.listIterator(index);
    }

    /**
     * Is not supported.
     * 
     * @see java.util.List#set(int, java.lang.Object)
     */
    public IFeed set(int index, IFeed element)
    {
        throw new IllegalArgumentException(Strings.error("operation.is.not.supported"));
    }

    /**
     * Copies list of filtered elements into array.
     *
     * @see java.util.List#toArray(java.lang.Object[])
     */
    public <T> T[] toArray(T[] a)
    {
        return filteredSourceList.toArray(a);
    }

    // inner use methods
    /**
     * Gets the filtered list (objects that passed the filter).
     * 
     * @param aFilter       filter
     * @param aSourceList    list to filter
     * @return filtered list
     */
    private List<IFeed> getFilteredList(Filter aFilter, List<IFeed> aSourceList)
    {
        ArrayList<IFeed> filteredList = new ArrayList<IFeed>(aSourceList.size());

        for (IFeed feed : aSourceList)
        {
            if (aFilter.accept(feed)) filteredList.add(feed);
        }

        return filteredList;
    }
    
    /**
     * Clones the prototype list.
     * 
     * @param aListPrototype    prototype clone.
     * @param aListClone        clone list.
     *
     * @return clone list
     */
    private List<IFeed> clone(List<IFeed> aListPrototype, List<IFeed> aListClone)
    {
        aListClone.clear();
        aListClone.addAll(aListPrototype);

        return aListClone;
    }
    
    /**
     * Basic class for filters.
     */
    public abstract static class Filter
    {
        private String name;
        
        /**
         * Creates <code>Filter</code> with name.
         * 
         * @param filterName unique filter's name.
         */
        public Filter(final String filterName)
        {
            name = filterName;
        }
        
        /**
         * Indicates whether some other object is "equal to" this one.
         * 
         * @param object object.
         * @return true if filters are equal (i.e. with the same name).
         */
        public boolean equals(Object object)
        {
            return name.equalsIgnoreCase(((Filter) object).name);
        }
        
        /**
         * Returns a hash code value for the object.
         * 
         * @return hash code.
         */
        public int hashCode()
        {
            return super.hashCode();
        }
        
        /**
         * Returns a string representation of the object.
         * 
         * @return string representation.
         */
        public String toString()
        {
            return "Filter: " + name;
        }
        
        /**
         * Is more restrictive than specified filter.
         * 
         * @param aFilter   filter to compare.
         * @return <code>true</code> if this is more restrictive than parameter filter.
         */
        public abstract boolean isMoreRestrictive(final Filter aFilter);

        /**
         * Filters the object.
         * 
         * @param object    the object to be accepted.
         * @return <code>true</code> if object was accepted by filter.
         */
        public abstract boolean accept(Object object);
    }
    
    /**
     * A basic implementation of 'AND' multi filter (composite filter).
     */
    public static final class MultiFilterAnd extends Filter
    {
        private List<Filter>    filters;
        private List<Integer>   filterPriorities;
        
        /**
         * Creates <code>MultiFilter</code>.
         * 
         * @param name unique filter's name.
         */
        public MultiFilterAnd(String name)
        {
            super(name);
            this.filters = new ArrayList<Filter>();
            this.filterPriorities = new ArrayList<Integer>();
        }

        /**
         * Gets the filter index.
         * 
         * @param aFilter filter.
         * @return index of the filter (<code>-1</code> if filter is not found).
         */
        public int getFilterIndex(Filter aFilter)
        {
            return filters.indexOf(aFilter);
        }
        
        /**
         * Adds a new filter to multi filter.
         * 
         * @param aFilter   the filter to be added.
         * @param priority  filter's priority(0 - is the highest priority).
         * @return <code>true</code> if the filter becomes more restrictive.
         */
        public boolean addFilter(Filter aFilter, final int priority)
        {
            boolean becomeMoreRestrictive = true;
            
            final int filterIndex = filters.indexOf(aFilter);
            if (filterIndex == -1)
            {
                int fi = -1;
                for (int i = 0, n = filterPriorities.size(); (i < n) && (fi == -1); i++)
                {
                    if (priority <= filterPriorities.get(i))
                    {
                        fi = i;
                    }
                }
                
                if (fi == -1)
                {
                    filters.add(aFilter);
                    filterPriorities.add(priority);
                } else
                {
                    filters.add(fi, aFilter);
                    filterPriorities.add(fi, priority);
                }
            } else
            {
                final Filter filterPrev = filters.get(filterIndex);
                
                if (!aFilter.isMoreRestrictive(filterPrev))
                {
                    becomeMoreRestrictive = false;
                }
                
                filters.set(filterIndex, aFilter);
            }
            
            return becomeMoreRestrictive;
        }

        /**
         * Removes the filter.
         * 
         * @param aFilter   the filter to be removed.
         * @return <code>true</code> if filter becomes less restrictive(i.e. is removed).
         */
        public boolean removeFilter(Filter aFilter)
        {
            boolean becomeLessRestrictive = false;
            for (int i = 0, n = filters.size(); i < n && !becomeLessRestrictive; i++)
            {
                if (aFilter.equals(filters.get(i)))
                {
                    filters.remove(i);
                    filterPriorities.remove(i);
                    becomeLessRestrictive = true;
                }
            }
            
            return becomeLessRestrictive;
        }
        
        /**
         * Removes filter by name.
         * 
         * @param filterName    name of the filter to remove
         * @return true         if filter becomes less restrictive(i.e. is removed)
         */
        public boolean removeFilter(final String filterName)
        {
            boolean becomeLessRestrictive = false;
            for (int i = 0, n = filters.size(); i < n && !becomeLessRestrictive; i++)
            {
                final Filter aFilter = this.filters.get(i);
                
                if (filterName.equalsIgnoreCase(aFilter.name))
                {
                    filters.remove(i);
                    filterPriorities.remove(i);
                    becomeLessRestrictive = true;
                }
            }
            
            return becomeLessRestrictive;
        }
        
        /**
         * Is more restrictive than specified filter.
         * 
         * @param aFilter   filter to compare
         * @return true     if this is more restrictive than parameter filter
         */
        public boolean isMoreRestrictive(Filter aFilter)
        {
            return true;
        }

        /**
         * Filters the object.
         * 
         * @param object    object to filter
         * @return true     if object was filtered (i.e. didn't pass the filter)
         */
        public boolean accept(Object object)
        {
            boolean isAccepted = true;
            for (int i = 0, n = filters.size(); i < n && isAccepted; i++)
            {
                final Filter aFilter = filters.get(i);
                
                isAccepted = aFilter.accept(object);
            }
            
            return isAccepted;
        }
    }
}