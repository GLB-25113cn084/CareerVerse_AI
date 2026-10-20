package com.careerverse.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

/**
 * Works out streaks from a "day -> number of submissions" map.
 * Pure Java with no database code, so it is easy to test.
 */
public class StreakCalculator {

    public record Result(int current, int longest, int activeDays, int activeDaysThisWeek,
                         int submissionsThisWeek, boolean activeToday, boolean atRisk) {}

    public static Result calculate(Map<LocalDate, Integer> days, LocalDate today) {
        TreeMap<LocalDate, Integer> active = new TreeMap<>();
        days.forEach((d, n) -> { if (n != null && n > 0) active.put(d, n); });

        boolean activeToday = active.containsKey(today);

        // Current streak: counts back from today, or from yesterday if today is still empty.
        int current = 0;
        LocalDate cursor = activeToday ? today : today.minusDays(1);
        while (active.containsKey(cursor)) {
            current++;
            cursor = cursor.minusDays(1);
        }

        // Longest streak ever recorded.
        int longest = 0;
        int run = 0;
        LocalDate previous = null;
        for (LocalDate d : active.keySet()) {
            run = (previous != null && previous.plusDays(1).equals(d)) ? run + 1 : 1;
            longest = Math.max(longest, run);
            previous = d;
        }

        // This week = Monday to today.
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        int weekDays = 0;
        int weekSubs = 0;
        for (var e : active.subMap(monday, true, today, true).entrySet()) {
            weekDays++;
            weekSubs += e.getValue();
        }

        boolean atRisk = !activeToday && current > 0;
        return new Result(current, longest, active.size(), weekDays, weekSubs, activeToday, atRisk);
    }
}
