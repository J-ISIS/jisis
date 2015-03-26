///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
//
//
//package org.unesco.jisis.jisisutils;
//
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import org.openide.util.Exceptions;
//
//import static org.junit.Assert.*;
//
//import java.io.IOException;
//
///**
// *
// * @author jc_dauphin
// */
//public class VArrayTest {
//    public VArrayTest() {}
//
//    @BeforeClass
//    public static void setUpClass() throws Exception {}
//
//    @AfterClass
//    public static void tearDownClass() throws Exception {}
//
//    @Before
//    public void setUp() {}
//
//    @After
//    public void tearDown() {}
//
//    /**
//     * Test of makeVArray method, of class VArray.
//     */
//    @Test
//    public void testMakeVArray() throws Exception {
//        System.out.println("makeVArray jcd ");
//        try {
//            VArray va =
//                VArray.makeVArray(10, 10,
//                                  "c:\\NetBeansProjects\\jisis NetBeans Project\\jisis\\vatest");
//            for (int i = 0; i < 200; i++) {
//                va.storeValue(i, i);
//            }
//            va.storeValue(251, 251);
//            va.storeValue(271, 271);
//            for (int i = 0; i < 200; i++) {
//                long k = va.fetchValue(i);
//                System.out.println("fetchValue i=" + i + " value=" + k);
//            }
//            long val = va.fetchValue(251);
//            System.out.println("fetchValue i=" + 251 + " value=" + val);
//            val = va.fetchValue(271);
//             System.out.println("fetchValue i=" + 271 + " value=" + val);
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//    }
//
//    /**
//     * Test of fetchValue method, of class VArray.
//     */
//    @Test
//    public void testFetchValue() throws Exception {
////        System.out.println("fetchValue");
////        long   index     = 0L;
////        VArray instance  = null;
////        long   expResult = 0L;
////        long   result    = instance.fetchValue(index);
////        assertEquals(expResult, result);
////        // TODO review the generated test code and remove the default call to fail.
////        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of storeValue method, of class VArray.
//     */
//    @Test
//    public void testStoreValue() {
////        System.out.println("storeValue");
////        long   index    = 0L;
////        long   value    = 0L;
////        VArray instance = null;
////        instance.storeValue(index, value);
////        // TODO review the generated test code and remove the default call to fail.
////        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of main method, of class VArray.
//     */
//    @Test
//    public void testMain() {
////        System.out.println("main");
////        String[] args = null;
////        VArray.main(args);
////        // TODO review the generated test code and remove the default call to fail.
////        fail("The test case is a prototype.");
//    }
//}
