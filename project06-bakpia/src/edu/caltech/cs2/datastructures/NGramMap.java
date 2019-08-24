package edu.caltech.cs2.datastructures;

import edu.caltech.cs2.interfaces.IDictionary;
import edu.caltech.cs2.interfaces.IPriorityQueue;
import edu.caltech.cs2.sorts.TopKSort;
import edu.caltech.cs2.types.NGram;

import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Supplier;


public class NGramMap {
    public static final Random RANDOM = new Random();
    private int N;
    private IDictionary<NGram, IDictionary<IterableString, Integer>> map;
    private Supplier<IDictionary<IterableString, Integer>> inner;

    public NGramMap(Scanner reader, int N,
                    IDictionary<NGram, IDictionary<IterableString, Integer>> map,
                    Supplier<IDictionary<IterableString, Integer>> inner) {
        this.N = N;
        this.map = map;
        this.inner = inner;

        // Stores the next NGram string
        NGram ngram;

        // Read in the first N words
        String[] words = new String[this.N];
        int i = 0;

        while (reader.hasNext() && i < this.N) {
            String next = NGram.normalize(reader.next());
            if (next.isEmpty()) {
                continue;
            }
            words[i] = next;
            i += 1;
        }
        ngram = new NGram(words);

        // Parse the rest of the text
        while (reader.hasNext()) {
            String next = NGram.normalize(reader.next());
            if (next.isEmpty()) {
                continue;
            }

            // Increment count of how many times "next" follows ngram
            updateCount(ngram, next);

            // Update the strings held in ngram
            ngram = ngram.next(next);
        }
    }

    public void updateCount(NGram ngram, String nexts) {
        IterableString next = new IterableString(nexts);
        // Check if the key already exists in map
        if (map.containsKey(ngram)) {
            IDictionary<IterableString, Integer> follows = map.get(ngram);

            // Check if the inner map already has next as a key
            if (follows.containsKey(next)) {
                follows.put(next, follows.get(next) + 1);
            }
            else {
                follows.put(next, 1);
            }
        }
        else {
            IDictionary<IterableString, Integer> follows = inner.get();
            follows.put(next, 1);
            map.put(ngram, follows);
        }
    }

    public String getRandomNext(NGram ngram) {
        if (!map.containsKey(ngram)) {
            return "No information regarding this prefix.";
        }

        IDictionary<IterableString, Integer> suffixes = map.get(ngram);

        int i = 0;
        int idx = RANDOM.nextInt(suffixes.size());

        for (IterableString suffix : suffixes) {
            if (i == idx) {
                return suffix.toString();
            }

            i++;

        }

        // Execution should never get here...
        return null;
    }

    @Override
    public String toString() {
        return map.toString();
    }

    /**
     * Returns an array of PQElements containing words and the number of
     * times each word was seen after ngram.
     * @param ngram - the ngram we want counts for
     * @return an array of hashmaps of words to counts
     */
    public IPriorityQueue.PQElement<String>[] getCountsAfter(NGram ngram) {
        IDictionary<IterableString, Integer> dict = map.get(ngram);
        if (dict == null) {
            return null;
        }
        IPriorityQueue.PQElement<String>[] counts = new IPriorityQueue.PQElement[dict.size()];
        int i = 0;
        for (IterableString key: dict.keySet()) {
            IPriorityQueue.PQElement<String> temp = new IPriorityQueue.PQElement<>(key.toString(), dict.get(key));
            counts[i] = temp;
            i++;
        }
        return counts;
    }

    /**
     * Gets k words that could potentially follow ngram in descending sorted order
     * @param ngram - the ngram we wish to suggest words for
     * @param k - the number of suggestions to return
     * @return - an array of suggestions
     */
    public String[] getWordsAfter(NGram ngram, int k) {
        IPriorityQueue.PQElement<String>[] counts = getCountsAfter(ngram);
        if (counts == null) {
            return new String[0];
        }
        TopKSort.sort(counts, k);
        String[] words = new String[counts.length];
        for (int i =0; i < counts.length; i++) {
            words[i] = counts[i].data;
        }
        return words;
    }
}