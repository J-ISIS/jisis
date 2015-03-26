/**
 *  This code is from the book:
 *
 *    Winder, R and Roberts, G (1998) <em>Developing Java
 *    Software</em>, John Wiley & Sons.
 *
 *  It is copyright (c) 1997 Russel Winder and Graham Roberts.
 */

package org.unesco.jisis.sorting;


import java.util.List ;

/**
 *  Sort an array of <code>List</code>s using Quicksort.  This is an
 *  O(n.log(n)) sort except when the data is almost sorted in which
 *  case it O(n^2).
 *
 *  @version 1.0 19.5.97
 *  @author Russel Winder
 */
public class QuicksortList implements ListSort
{
    /**
     *  The per object sort operation.
     *
     *  @param list the <code>List</code> of <code>Object</code>s to be
     *  sorted.
     *
     *  @param c the <code>Comparator</code> used to compare the
     *  <code>Object</code> during the sort process.
     */ 
    public void sort(final List<Record> list, final Comparator c)
    {
        execute(list, c) ;
    }
    
    /**
     *  The statically accessible sort operation.
     *
     *  @param list the <code>List</code> of <code>Object</code>s to be
     *  sorted.
     *
     *  @param c the <code>Comparator</code> used to compare the
     *  <code>Object</code> during the sort process.
     */ 
    public static void execute(final List<Record> list, final Comparator c)
    {
        quicksort(list, 0, list.size()-1, c) ;
    }

    /**
     *  Given the array and two indices, swap the two items in the
     *  array.
     */
    private static void swap(final List<Record> list,
                             final int a,
                             final int b)
    {
        Record temp = list.get(a) ;
        list.set(a, list.get(b)) ;
        list.set(b, temp) ;
    }

    /**
     *  Partition an array in two using the pivot value that is at the
     *  centre of the array being partitioned.
     *
     *  <p>This partition implementation based on that in Winder, R
     *  (1993) "Developing C++ Software", Wiley, p.395.  NB. This
     *  implementation (unlike most others) does not guarantee that
     *  the split point contains the pivot value.  Unlike other
     *  implementations, it requires only < (or >) relation and not
     *  both < and <= (or > and >=).  Also, it seems easier to program
     *  and to comprehend.
     *
     *  @param list the array out of which to take a slice.
     *
     *  @param lower the lower bound of this slice.
     *
     *  @param upper the upper bound of this slice.
     *
     *  @param c the <code>Comparator</code> to be used to define the
     *  order.
     */
    private static int partition(final List<Record> list,
                                       int lower,
                                       int upper,
                                 final Comparator c)
    {
        Object pivotValue = list.get((upper+lower+1)/2) ;
        while (lower <= upper)
        {
            while (c.relation(list.get(lower), pivotValue))
            {
                lower++ ;
            }
            while (c.relation(pivotValue, list.get(upper)))
            {
                upper-- ;
            }
            if (lower <= upper)
            {
                if (lower < upper)
                {
                    swap(list, lower, upper) ;
                }
                lower++ ;
                upper-- ;
            }
        }
        return upper ;
    }

    /**
     *  The recursive Quicksort function.
     *
     *  @param v the array out of which to take a slice.
     *
     *  @param lower the lower bound of this slice.
     *
     *  @param upper the upper bound of this slice.
     *
     *  @param c the <code>Comparator</code> to be used to define the
     *  order.
     */
    private static void quicksort(final List<Record> list,
                                  final int lower,
                                  final int upper,
                                  final Comparator c)
    {
        int sliceLength = upper-lower+1 ;
        if (sliceLength > 1)
        {
            if (sliceLength == 2)
            {
                if (c.relation(list.get(upper),list.get(lower)))
                {
                    swap (list, lower, upper) ;
                }
            }
            else
            {
                //
                //  This pivot implementation does not guarantee that
                //  the split point contains the pivot value so we
                //  cannot assume that the pivot is between the two
                //  slices.
                //
                int pivotIndex = partition(list, lower, upper, c) ;
                quicksort(list, lower, pivotIndex, c) ;
                quicksort(list, pivotIndex+1, upper, c) ;
            }
        }
    }
}
