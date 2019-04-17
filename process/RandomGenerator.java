package com.etf.os2.js150411.process;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomGenerator {
    private final Random random;
    private int freqs[];
    private long max;
    private long min;
    private int number = 0;
    private List<Integer> numbers = Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});

    public RandomGenerator(Random random, int[] freqs, long max, long min) {
        this.random = random;
        this.freqs = freqs;
        this.max = max;
        this.min = min;
    }

    public long getNext() {
        if (number == 0) {
            Collections.shuffle(numbers, random);
        }

        long begin = min;
        long end = max;

        long step = (max - min) / freqs.length;
        long current = min;

        boolean left = true;
        for (int x : freqs) {
            if (x >= numbers.get(number)) {
                left = false;
                end = current + step;
            } else {
                if (left) {
                    begin = current + step;
                }
            }
            current += step;
        }

        if (max - end < step) {
            end = max;
        }

        number = (number + 1) % numbers.size();

        double rand = random.nextDouble();
        long ret = (long) (rand * (end - begin + 1)) + begin;
        return ret;
    }
}
