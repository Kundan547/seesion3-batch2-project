package com.nameutility;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NameScorer {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java NameScorer /home/kundan-vyas/Desktop/workspace/NameScoringUtility/names.txt");
            return;
        }
        String filePath = args[0];
        List<String> names = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine();
            if (line != null) {
                names = parseNames(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return;
        }
        Collections.sort(names);
        long totalScore = 0;
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            int nameValue = alphabeticalValue(name);
            int position = i + 1;
            totalScore += (long) nameValue * position;
        }
        System.out.println("Total score: " + totalScore);
    }

    private static List<String> parseNames(String line) {
        // Remove all whitespace, then split by comma, then strip quotes
        String[] rawNames = line.trim().split(",");
        List<String> names = new ArrayList<>();
        for (String raw : rawNames) {
            String name = raw.replaceAll("^\"|\"$", ""); // Remove leading/trailing quotes
            names.add(name);
        }
        return names;
    }

    private static int alphabeticalValue(String name) {
        int sum = 0;
        for (char c : name.toUpperCase().toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                sum += (c - 'A' + 1);
            }
        }
        return sum;
    }
} 