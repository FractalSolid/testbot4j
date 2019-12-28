package org.splatnik.dumbot.dice;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DieRegex {

    private int amount;
    private int die;
    private int mult = 1;
    private int add = 0;

    public String matchCheck(String matcher) {
        Pattern pattern = Pattern.compile("([1-9]\\d*)?d([1-9]\\d*)([/x][1-9]\\d*)?([+-]\\d+)?");
        Matcher matches = pattern.matcher(matcher);

        if (matches.matches()) {
            amount = (matches.group(1) != null) ? Integer.parseInt(matches.group(1)) : 1;
            die = Integer.parseInt(matches.group(2));
            if (matches.group(3) != null) {
                boolean positive = matches.group(3).startsWith("x");
                int val = Integer.parseInt(matches.group(3).substring(1));
                mult = positive ? val : -val;
            }
            if (matches.group(4) != null) {
                boolean positive = matches.group(4).startsWith("+");
                int val = Integer.parseInt(matches.group(4).substring(1));
                add = positive ? val : -val;
            }
            return rollDice(amount, die, mult, add);
//            return "this is a wip. Args are: " + amount + " --- " + die;
        } else {
            return "That's not how dice rolls work";
        }
    }

    public String rollDice(int amount, int die, int mult, int add) {

        List<Integer> integerList = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();

        for(int i = 1; i <= amount; i++) {
            Random random = new Random();
            int result = random.nextInt((die - 1) + 1) + 1;
            stringBuilder.append("You rolled a: " + result + ". \n");
            integerList.add(result);
        }
        return stringBuilder.toString();

    }
}
