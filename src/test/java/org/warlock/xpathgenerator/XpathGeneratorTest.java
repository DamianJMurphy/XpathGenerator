/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.warlock.xpathgenerator;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author simonfarrow
 */
public class XpathGeneratorTest {

    public XpathGeneratorTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of main method, of class XpathGenerator.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = new String[]{"-i", "-p", "src/test/resources/problems_resp.xml", "src/test/resources/context.txt"};
        PrintStream prev = System.out;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));
        XpathGenerator.main(args);
        System.setOut(prev);
        System.out.println(bos.toString());
        assertTrue(bos.toString().startsWith("/fhir:Bundle[1]"));
    }

}
