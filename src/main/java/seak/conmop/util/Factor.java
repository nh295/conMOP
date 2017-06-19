/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * class that uses a pre computed table to find the divisors of a given number.
 * The divisors are from sequence A027750 available at https://oeis.org/A027750
 * from the On-line encyclopedia of integer sequences (OEIS)
 *
 * @author nhitomi
 */
public class Factor {

    private static final HashMap<Integer, List<Integer>> map = new HashMap<>();

    private Factor() {
        System.out.print("Loading divisors...");
        String fileName = "1-1000_divisors.txt";
        String resourcePath = System.getProperty("user.dir") + File.separator + "resources";
        try (BufferedReader br = new BufferedReader(new FileReader(new File(resourcePath, fileName)))) {
            String line = br.readLine();
            while (line != null) {
                String[] args = line.split("\\s");
                String[] divisors = args[1].split(",");
                List<Integer> divisorList = new ArrayList(divisors.length);
                for (String div : divisors) {
                    divisorList.add(Integer.parseInt(div));
                }
                map.put(Integer.parseInt(args[0]), Collections.unmodifiableList(divisorList));
                line = br.readLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(Factor.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("done");
    }

    /**
     * Returns all the divisors of n in ascending order.
     *
     * @param n
     * @return
     */
    public static List<Integer> divisors(int n) {
        if (map.isEmpty()) {
            new Factor();
        }
        return map.get(n);
    }
}
