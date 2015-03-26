/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.sorting;

import org.unesco.jisis.jisisutils.IRowAdapter;

/**
 * Comparator for objects that implements the IRowAdater interface
 * 
 * @author jc_dauphin
 */
public class RowComparator implements java.util.Comparator {

    private int[] iFields_; // Indexes of fields to sort on
    private boolean ascending_ = true;

    public RowComparator(int[] iFields) {
        iFields_ = iFields;
        ascending_ = true;
    }

    public RowComparator(int[] iFields, boolean reverse) {
        iFields_ = iFields;
        ascending_ = !reverse;
        
    }
    /**
     * Compares its 2 arguments for order
     * @param o1
     * @param o2
     * @return <0 if 01<02 0 if O1=02 and >0 if o1>o2 
     */
    public int compare(Object o1, Object o2) {
        IRowAdapter r1 = (IRowAdapter) o1;
        IRowAdapter r2 = (IRowAdapter) o2;
        int result = 0;
        for (int i = 0; i < iFields_.length; i++) {
            Object o = r1.getValueAt(iFields_[i]);
            if (o instanceof String) {
                String s1 = (String) r1.getValueAt(iFields_[i]);
                String s2 = (String) r2.getValueAt(iFields_[i]);
                if (s1.equals(s2)) {
                    continue;
                }
                result = s1.compareTo(s2);
                return (ascending_ == true) ? result : result*(-1);
            } else if (o instanceof Integer) {
                Integer i1 = (Integer) r1.getValueAt(iFields_[i]);
                Integer i2 = (Integer) r2.getValueAt(iFields_[i]);
                if (i1.equals(i2)) {
                    continue;
                }
                result = i1.compareTo(i2);
                return (ascending_ == true) ? result : result*(-1);
            }
            else if (o instanceof Long) {
                Long l1 = (Long) r1.getValueAt(iFields_[i]);
                Long l2 = (Long) r2.getValueAt(iFields_[i]);
                if (l1.equals(l2)) {
                    continue;
                }
                result = l1.compareTo(l2);
                return (ascending_ == true) ? result : result*(-1);
            }
        }
        return 0;
    }
}
