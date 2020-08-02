package com.keuin.kbackupfabric;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Random {
    public int random(int A, int B) {
        return (int) (A + (B - A + 1) * Math.random());
    }

    public double[] randomArray(int MaxN, int lowA, int highA) {
        double[] array = new double[random(1,MaxN)];
        for (int i = 0; i < array.length; i++) {
            array[i] = lowA + (highA - lowA) * Math.random();
        }
        return array;
    }

    public double[] generate(int MaxN, int lowA, int highA) {
        double[] testData = randomArray(MaxN,lowA,highA);
        double x = testData[random(0,testData.length-1)];
    }

    public int R(double[] a, double x) {
        List<Integer> list = new LinkedList<>();
        int k;
        for (int i = 0; i < a.length; i++) {
            if(Math.abs(a[i] - x) < 1e-6)
                list.add(i);
        }
        assert !list.isEmpty();
        k = list.get(list.size() / 2);
        return k;
    }


    public boolean domR(double[] a, double x) {
        for (double v : a) {
            if(Math.abs(v - x) < 1e-6)
                return true;
        }
        return false;
    }

    public boolean oracle(double[] a, double x, int k) {
        if(!(Math.abs(a[k] - x) < 1e-6))
            return false;
        int c1=0,c2=0;
        for (int i = 0; i < k; i++) {
            if(Math.abs(a[i] - x) < 1e-6)
                ++c1;
        }
        for (int i = k; i < a.length; i++) {
            if(Math.abs(a[i] - x) < 1e-6)
                ++c2;
        }
        return c1 - c2 >= -1 && c1 - c2 <= 1;
    }

    public boolean driver() {
        int MaxN = 80, lowA = 20, highA = 50;
        double[] a = randomArray(MaxN,lowA,highA);
        double x = a[random(0,a.length-1)];
        return oracle(a,x,search(a,x));
    }


    public int search(double[] a, double x) {
        int counter = 0;
        for (double i : a) {
            if(Math.abs(i - x) < 1e-6)
                ++counter;
        }
        counter /= 2;
        for (int i = 0; i < a.length; i++) {
            if(Math.abs(a[i] - x) < 1e-6) {
                --counter;
                if(counter == 0)
                    return i;
            }
        }
        return -1;
    }

    public static void Main() {
        int pass = 0, fail = 0;
        for (int i = 0; i < 10000; i++) {
            if(driver())
                pass++;
            else
                fail++;
        }
        System.out.println(String.format("pass: %d, fail: %d.",pass,fail));
    }

}
