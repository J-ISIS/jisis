/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.sorting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 *
 * @author jc_dauphin
 */
public class ExternalSort {
//public static void externalSort(String relation, String attribute)
//{
//     try
//     {
//         FileReader intialRelationInput = new FileReader(relation + ".csv"); 
//         BufferedReader initRelationReader = new BufferedReader(intialRelationInput);
//         String [] header = initRelationReader.readLine().split(",");
//         String [] row = header;
//         int indexToCompare = getIndexForColumn(header,attribute);
//         ArrayList<Integer[]> tenKRows = new ArrayList<Integer[]>();
//                     
//         int numFiles = 0;
//         while (row!=null)
//         {
//             // get 10k rows
//             for(int i=0; i<10000; i++)
//             {
//                 String line = initRelationReader.readLine();
//                 if (line==null) 
//                 {
//                     row = null;
//                     break;
//                 }
//                 row = line.split(",");
//                 tenKRows.add(getIntsFromStringArray(row));
//             }
//             // sort the rows
//             tenKRows = mergeSort(tenKRows, indexToCompare);
//             
//             // write to disk
//             FileWriter fw = new FileWriter(relation + "_chunk" + numFiles + ".csv");
//             BufferedWriter bw = new BufferedWriter(fw);
//             bw.write(flattenArray(header,",")+"\n");
//             for(int i=0; i<tenKRows.size(); i++)
//             {
//                 bw.append(flattenArray(tenKRows.get(i),",")+"\n");
//             }
//             bw.close();
//             numFiles++;
//             tenKRows.clear();
//         }
//     
//         mergeFiles(relation, numFiles, indexToCompare);
//         
//         initRelationReader.close();
//         intialRelationInput.close();
//         
//     }
//     catch (Exception ex)
//     {
//         ex.printStackTrace();
//         System.exit(-1);
//     }
//     
//     
//}
//    // sort an arrayList of arrays based on the ith column
//    private static ArrayList<Integer[]> mergeSort(ArrayList<Integer[]> arr, int index)
//    {
//         ArrayList<Integer[]> left = new ArrayList<Integer[]>();
//         ArrayList<Integer[]> right = new ArrayList<Integer[]>();
//         if(arr.size()<=1)
//             return arr;
//         else
//         {
//             int middle = arr.size()/2;
//             for (int i = 0; i<middle; i++)
//                 left.add(arr.get(i));
//             for (int j = middle; j<arr.size(); j++)
//                 right.add(arr.get(j));
//             left = mergeSort(left, index);
//             right = mergeSort(right, index);
//             return merge(left, right, index);
//             
//         }
//         
//    }
//    
//    // merge the the results for mergeSort back together 
//    private static ArrayList<Integer[]> merge(ArrayList<Integer[]> left, ArrayList<Integer[]> right, int index)
//    {
//         ArrayList<Integer[]> result = new ArrayList<Integer[]>();
//         while (left.size() > 0 && right.size() > 0)
//         {
//             if(left.get(0)[index] <= right.get(0)[index])
//             {
//                 result.add(left.get(0));
//                 left.remove(0);
//             }
//             else
//             {
//                 result.add(right.get(0));
//                 right.remove(0);
//             }
//         }
//         if (left.size()>0) 
//         {
//             for(int i=0; i<left.size(); i++)
//                 result.add(left.get(i));
//         }
//         if (right.size()>0) 
//         {
//             for(int i=0; i<right.size(); i++)
//                 result.add(right.get(i));
//         }
//         return result;
//}
//
//private static void mergeFiles(String relation, int numFiles, int compareIndex)
//{
//     try
//     {
//         ArrayList<FileReader> mergefr = new ArrayList<FileReader>();
//         ArrayList<BufferedReader> mergefbr = new ArrayList<BufferedReader>();
//         ArrayList<Integer[]> filerows = new ArrayList<Integer[]>(); 
//         FileWriter fw = new FileWriter(relation + "_sorted.csv");
//         BufferedWriter bw = new BufferedWriter(fw);
//         String [] header;
//             
//         boolean someFileStillHasRows = false;
//         
//         for (int i=0; i<numFiles; i++)
//         {
//             mergefr.add(new FileReader(relation+"_chunk"+i+".csv"));
//             mergefbr.add(new BufferedReader(mergefr.get(i)));
//             // get each one past the header
//             header = mergefbr.get(i).readLine().split(",");
//                             
//             if (i==0) bw.write(flattenArray(header,",")+"\n");
//             
//             // get the first row
//             String line = mergefbr.get(i).readLine();
//             if (line != null)
//             {
//                 filerows.add(getIntsFromStringArray(line.split(",")));
//                 someFileStillHasRows = true;
//             }
//             else 
//             {
//                 filerows.add(null);
//             }
//                 
//         }
//         
//         Integer[] row;
//         int cnt = 0;
//         while (someFileStillHasRows)
//         {
//             Integer min;
//             int minIndex = 0;
//             
//             row = filerows.get(0);
//             if (row!=null) {
//                 min = row[compareIndex];
//                 minIndex = 0;
//             }
//             else {
//                 min = null;
//                 minIndex = -1;
//             }
//             
//             // check which one is min
//             for(int i=1; i<filerows.size(); i++)
//             {
//                 row = filerows.get(i);
//                 if (min!=null) {
//                     
//                     if(row!=null && row[compareIndex] < min)
//                     {
//                         minIndex = i;
//                         min = filerows.get(i)[compareIndex];
//                     }
//                 }
//                 else
//                 {
//                     if(row!=null)
//                     {
//                         min = row[compareIndex];
//                         minIndex = i;
//                     }
//                 }
//             }
//             
//             if (minIndex < 0) {
//                 someFileStillHasRows=false;
//             }
//             else
//             {
//                 // write to the sorted file
//                 bw.append(flattenArray(filerows.get(minIndex),",")+"\n");
//                 
//                 // get another row from the file that had the min
//                 String line = mergefbr.get(minIndex).readLine();
//                 if (line != null)
//                 {
//                     filerows.set(minIndex,getIntsFromStringArray(line.split(",")));
//                 }
//                 else 
//                 {
//                     filerows.set(minIndex,null);
//                 }
//             }                                 
//             // check if one still has rows
//             for(int i=0; i<filerows.size(); i++)
//             {
//                 
//                 someFileStillHasRows = false;
//                 if(filerows.get(i)!=null) 
//                 {
//                     if (minIndex < 0) 
//                     {
//                         System.out.println("mindex lt 0 and found row not null" 
//                                 + flattenArray(filerows.get(i)," "));
//                         System.exit(-1);
//                     }
//                     someFileStillHasRows = true;
//                     break;
//                 }
//             }
//             
//             // check the actual files one more time
//             if (!someFileStillHasRows)
//             {
//                 
//                 //write the last one not covered above
//                 for(int i=0; i<filerows.size(); i++)
//                 {
//                     if (filerows.get(i) == null)
//                     {
//                         String line = mergefbr.get(i).readLine();
//                         if (line!=null) 
//                         {
//                             
//                             someFileStillHasRows=true;
//                             filerows.set(i,getIntsFromStringArray(line.split(",")));
//                         }
//                     }
//                             
//                 }
//             }
//                 
//         }
//         
//         
//         
//         // close all the files
//         bw.close();
//         fw.close();
//         for(int i=0; i<mergefbr.size(); i++)
//             mergefbr.get(i).close();
//         for(int i=0; i<mergefr.size(); i++)
//             mergefr.get(i).close();
//         
//         
//         
//     }
//     catch (Exception ex)
//     {
//         ex.printStackTrace();
//         System.exit(-1);
//     }
//}
//
//private static int getIndexForColumn(String [] arr, String val)
//{
//    int result = -1;
//    for(int i=0; i<arr.length; i++) 
//    {
//       if (val==arr[i]) { result=i; break; }
//    }
//    return result;
//}
////Basically, it does this (not tested, so you'll need to make it work if it doesn't):
//
//private static Integer[] getIntsFromStringArray(String[] arr)
//{
//   Integer[] result = new Integer[arr.length];
//   for (int i=0; i<arr.length; i++) {
//     result[i]=Integer.parseInt(arr[i]); 
//   } 
//   return result;
//}
//// Flatten array just takes an array and joins each element, 
////separated by the 2nd parameter, into a string. Something like:
//
//
//private static String flattenArray(String[] a, String separator)
//{
//
//String result = "";
//for(int i=0; i<a.length;  i++)
//result+=a[i] + separator;
//return result;
//}
//
///*Bugfix: getIndexForColumn()
//
//Instead of testing for pointer equality with "val == arr[i]" it should test for string equality with "val.equals(arr[i])".
//
//
//Optimization: mergeFiles()
//
//For each line, it performs a linear search to determine which file
//has the minimum line. And we're facing files with trillions of lines
//and probably many files to merge. Instead, it'd be good to use a heap.
//Lotta code to write in the world.
// * */
}