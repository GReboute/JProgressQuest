package com.JProgressQuest.util;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

/**
 * Pseudo-random number generator based on Alea algorithm.
 * This is a Java conversion of the Alea PRNG from the original JavaScript code.
 */
public class RandomGenerator {
    private double s0;
    private double s1;
    private double s2;
    private int c;
    private Object[] args;
    
    /**
     * Create a new RandomGenerator with the current timestamp as seed
     */
    public RandomGenerator() {
        this(Instant.now().toEpochMilli());
    }
    
    /**
     * Create a new RandomGenerator with the specified seed
     * @param seeds Initial seeds for the generator
     */
    public RandomGenerator(Object... seeds) {
        args = seeds.length > 0 ? seeds : new Object[] { Instant.now().toEpochMilli() };
        
        s0 = mash(" ");
        s1 = mash(" ");
        s2 = mash(" ");
        
        for (Object seed : args) {
            s0 -= mash(seed);
            if (s0 < 0) {
                s0 += 1;
            }
            s1 -= mash(seed);
            if (s1 < 0) {
                s1 += 1;
            }
            s2 -= mash(seed);
            if (s2 < 0) {
                s2 += 1;
            }
        }
    }
    
    /**
     * The Mash function for mixing seeds
     * @param data The data to mash
     * @return A double value between 0 and 1
     */
    private double mash(Object data) {
        String str = Objects.toString(data);
        double n = 0xefc8249d;
        
        for (int i = 0; i < str.length(); i++) {
            n += str.charAt(i);
            double h = 0.02519603282416938 * n;
            n = (int) h;
            h -= n;
            h *= n;
            n = (int) h;
            h -= n;
            n += h * 0x100000000L; // 2^32
        }
        
        return ((int) n) * 2.3283064365386963e-10; // 2^-32
    }
    
    /**
     * Generate a random double between 0 and 1
     * @return Random value between 0 and 1
     */
    public double random() {
        double t = 2091639 * s0 + c * 2.3283064365386963e-10; // 2^-32
        s0 = s1;
        s1 = s2;
        return s2 = t - (c = (int) t);
    }
    
    /**
     * Generate a random unsigned 32-bit integer
     * @return Random value between 0 and 2^32-1
     */
    public long uint32() {
        return (long) (random() * 0x100000000L); // 2^32
    }
    
    /**
     * Generate a random integer in the range [0, n-1]
     * @param n Upper bound (exclusive)
     * @return Random integer between 0 and n-1
     */
    public int nextInt(int n) {
        return (int) (uint32() % n);
    }
    
    /**
     * Pick a random element from a list
     * @param <T> Type of elements in the list
     * @param list List to pick from
     * @return Random element from the list
     */
    public <T> T pick(T[] list) {
        return list[nextInt(list.length)];
    }
    
    /**
     * Pick a random element from a list
     * @param <T> Type of elements in the list
     * @param list List to pick from
     * @return Random element from the list
     */
    public <T> T pick(java.util.List<T> list) {
        return list.get(nextInt(list.size()));
    }
    
    /**
     * Generate a random character name
     * @return A randomly generated name
     */
    public String generateName() {
        // Syllable parts for name generation
        String[][] nameParts = {
            "br,cr,dr,fr,gr,j,kr,l,m,n,pr,,,,r,sh,tr,v,wh,x,y,z".split(","),
            "a,a,e,e,i,i,o,o,u,u,ae,ie,oo,ou".split(","),
            "b,ck,d,g,k,m,n,p,t,v,x,z".split(",")
        };
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i <= 5; i++) {
            result.append(pick(nameParts[i % 3]));
        }
        
        // Capitalize first letter
        if (result.length() > 0) {
            result.setCharAt(0, Character.toUpperCase(result.charAt(0)));
        }
        
        return result.toString();
    }
    
    /**
     * Get the current state of the generator
     * @return Array containing the current state [s0, s1, s2, c]
     */
    public double[] getState() {
        return new double[] { s0, s1, s2, c };
    }
    
    /**
     * Set the state of the generator
     * @param state Array containing the new state [s0, s1, s2, c]
     */
    public void setState(double[] state) {
        if (state.length >= 4) {
            s0 = state[0];
            s1 = state[1];
            s2 = state[2];
            c = (int) state[3];
        }
    }
}
