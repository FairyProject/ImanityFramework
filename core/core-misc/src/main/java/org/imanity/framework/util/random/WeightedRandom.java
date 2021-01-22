/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.imanity.framework.util.random;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

@UtilityClass
public class WeightedRandom {

    private static final Random DEFAULT_RANDOM = new Random();

    public static int getTotalWeight(Collection<? extends WeightedItem> collection) {
        return collection.stream()
                .map(WeightedItem::getWeight)
                .reduce(0, Integer::sum);
    }

    public static <T extends WeightedItem> T getRandomItem(List<? extends T> collection, Random random) {
        int total = WeightedRandom.getTotalWeight(collection);

        int index = random.nextInt(total);
        int sum = 0;
        int i = 0;
        while (sum < index) {
            sum += collection.get(i++).getWeight();
        }
        return collection.get(Math.max(0, i - 1));
    }

    public static <T extends WeightedItem> T getRandomItem(Collection<? extends T> collection, Random random) {
        return getRandomItem(new ArrayList<>(collection), random);
    }

    public static <T extends WeightedItem> T getRandomItem(List<? extends T> collection) {
        return getRandomItem(collection, DEFAULT_RANDOM);
    }

    public static <T extends WeightedItem> T getRandomItem(Collection<? extends T> collection) {
        return getRandomItem(collection, DEFAULT_RANDOM);
    }

}
