package com.careerverse.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Hand-picked LeetCode problems, study plans and roadmap targets for every career.
 *
 * Three "tracks" exist:
 *   dsa   - software careers (data structures and algorithms)
 *   sql   - data careers (SQL / database problems)
 *   light - design and writing careers (short logic warm-ups)
 */
public class PracticeCatalog {

    public record Problem(String title, String slug, String difficulty) {}
    public record Section(String name, List<Problem> problems) {}
    public record Plan(String name, String slug) {}
    /** metric is easy, medium, hard or sql; goal is counted from the day the student linked LeetCode. */
    public record Target(String label, String metric, int goal) {}
    public record StageTarget(String stage, String duration, int weeks, List<Target> targets, int weeklyDays) {}

    private static Problem e(String t, String s) { return new Problem(t, s, "Easy"); }
    private static Problem m(String t, String s) { return new Problem(t, s, "Medium"); }
    private static Problem h(String t, String s) { return new Problem(t, s, "Hard"); }

    /* ---------------- problem sets ---------------- */

    private static final List<Section> DSA_CORE = List.of(
        new Section("Arrays & Hashing", List.of(
            e("Two Sum", "two-sum"), e("Contains Duplicate", "contains-duplicate"),
            e("Valid Anagram", "valid-anagram"), m("Group Anagrams", "group-anagrams"),
            m("Top K Frequent Elements", "top-k-frequent-elements"),
            m("Product of Array Except Self", "product-of-array-except-self"))),
        new Section("Two Pointers & Sliding Window", List.of(
            e("Valid Palindrome", "valid-palindrome"),
            e("Best Time to Buy and Sell Stock", "best-time-to-buy-and-sell-stock"),
            m("3Sum", "3sum"), m("Container With Most Water", "container-with-most-water"),
            m("Longest Substring Without Repeating Characters", "longest-substring-without-repeating-characters"))),
        new Section("Stack, Binary Search & Linked List", List.of(
            e("Valid Parentheses", "valid-parentheses"), e("Binary Search", "binary-search"),
            e("Reverse Linked List", "reverse-linked-list"), e("Merge Two Sorted Lists", "merge-two-sorted-lists"),
            e("Linked List Cycle", "linked-list-cycle"), m("Min Stack", "min-stack"),
            m("Search in Rotated Sorted Array", "search-in-rotated-sorted-array"))),
        new Section("Trees & Graphs", List.of(
            e("Maximum Depth of Binary Tree", "maximum-depth-of-binary-tree"),
            e("Invert Binary Tree", "invert-binary-tree"),
            m("Binary Tree Level Order Traversal", "binary-tree-level-order-traversal"),
            m("Validate Binary Search Tree", "validate-binary-search-tree"),
            m("Number of Islands", "number-of-islands"), m("Course Schedule", "course-schedule"))),
        new Section("Dynamic Programming", List.of(
            e("Climbing Stairs", "climbing-stairs"), m("House Robber", "house-robber"),
            m("Coin Change", "coin-change"), m("Longest Increasing Subsequence", "longest-increasing-subsequence"),
            h("Trapping Rain Water", "trapping-rain-water")))
    );

    private static final List<Section> SQL_CORE = List.of(
        new Section("SQL Basics (SELECT & WHERE)", List.of(
            e("Big Countries", "big-countries"),
            e("Recyclable and Low Fat Products", "recyclable-and-low-fat-products"),
            e("Find Customer Referee", "find-customer-referee"), e("Article Views I", "article-views-i"),
            e("Invalid Tweets", "invalid-tweets"), e("Not Boring Movies", "not-boring-movies"))),
        new Section("Joins & Grouping", List.of(
            e("Combine Two Tables", "combine-two-tables"),
            e("Employees Earning More Than Their Managers", "employees-earning-more-than-their-managers"),
            e("Customers Who Never Order", "customers-who-never-order"), e("Duplicate Emails", "duplicate-emails"),
            e("Game Play Analysis I", "game-play-analysis-i"))),
        new Section("Advanced SQL (ranking & window functions)", List.of(
            m("Second Highest Salary", "second-highest-salary"), m("Rank Scores", "rank-scores"),
            m("Consecutive Numbers", "consecutive-numbers"),
            m("Department Highest Salary", "department-highest-salary"),
            h("Department Top Three Salaries", "department-top-three-salaries"),
            h("Trips and Users", "trips-and-users")))
    );

    private static final Section LOGIC = new Section("Logic Warm-ups", List.of(
        e("Fizz Buzz", "fizz-buzz"), e("Reverse String", "reverse-string"),
        e("Merge Strings Alternately", "merge-strings-alternately"), e("Roman to Integer", "roman-to-integer"),
        e("Two Sum", "two-sum"), e("Valid Palindrome", "valid-palindrome")));

    private static final Map<String, Section> EXTRA = Map.of(
        "software", new Section("Hard Interview Classics", List.of(
            h("Merge k Sorted Lists", "merge-k-sorted-lists"),
            h("Median of Two Sorted Arrays", "median-of-two-sorted-arrays"),
            h("Word Ladder", "word-ladder"), m("LRU Cache", "lru-cache"))),
        "aiml", new Section("Math & Matrices (for ML)", List.of(
            e("Sqrt(x)", "sqrtx"), m("Pow(x, n)", "powx-n"), m("Rotate Image", "rotate-image"),
            m("Spiral Matrix", "spiral-matrix"), m("Set Matrix Zeroes", "set-matrix-zeroes"))),
        "web", new Section("Design Problems", List.of(
            e("Design HashMap", "design-hashmap"), m("LRU Cache", "lru-cache"),
            m("Time Based Key-Value Store", "time-based-key-value-store"))),
        "mobile", new Section("Design Problems", List.of(
            e("Design HashMap", "design-hashmap"), m("LRU Cache", "lru-cache"),
            m("Time Based Key-Value Store", "time-based-key-value-store"))),
        "cyber", new Section("Bits & Encoding", List.of(
            e("Number of 1 Bits", "number-of-1-bits"), e("Reverse Bits", "reverse-bits"),
            e("Single Number", "single-number"), e("Counting Bits", "counting-bits"),
            m("Sum of Two Integers", "sum-of-two-integers"))),
        "devops", new Section("Shell (Bash) Problems", List.of(
            e("Tenth Line", "tenth-line"), e("Valid Phone Numbers", "valid-phone-numbers"),
            m("Word Frequency", "word-frequency"), m("Transpose File", "transpose-file"),
            m("Task Scheduler", "task-scheduler"))),
        "cloud", new Section("Shell (Bash) Problems", List.of(
            e("Tenth Line", "tenth-line"), e("Valid Phone Numbers", "valid-phone-numbers"),
            m("Word Frequency", "word-frequency"), m("Task Scheduler", "task-scheduler"))),
        "qa", new Section("Edge-case Thinking", List.of(
            e("Plus One", "plus-one"), e("Roman to Integer", "roman-to-integer"),
            m("String to Integer (atoi)", "string-to-integer-atoi"), h("Valid Number", "valid-number"))),
        "product", new Section("Logic Warm-ups", LOGIC.problems())
    );

    /* ---------------- targets ---------------- */

    private static final List<StageTarget> DSA_TARGETS = List.of(
        new StageTarget("Foundation", "Month 1", 4, List.of(new Target("Easy problems", "easy", 15)), 4),
        new StageTarget("Core Skills", "Months 2–3", 8, List.of(
            new Target("Easy problems", "easy", 35), new Target("Medium problems", "medium", 15)), 5),
        new StageTarget("Portfolio", "Months 4–5", 8, List.of(
            new Target("Easy problems", "easy", 50), new Target("Medium problems", "medium", 40),
            new Target("Hard problems", "hard", 3)), 5),
        new StageTarget("Hiring Prep", "Month 6", 4, List.of(
            new Target("Medium problems", "medium", 60), new Target("Hard problems", "hard", 8)), 6)
    );

    private static final List<StageTarget> SQL_TARGETS = List.of(
        new StageTarget("Foundation", "Month 1", 4, List.of(new Target("SQL problems", "sql", 10)), 3),
        new StageTarget("Core Skills", "Months 2–3", 8, List.of(
            new Target("SQL problems", "sql", 25), new Target("Easy problems", "easy", 10)), 4),
        new StageTarget("Portfolio", "Months 4–5", 8, List.of(
            new Target("SQL problems", "sql", 40), new Target("Easy problems", "easy", 20)), 4),
        new StageTarget("Hiring Prep", "Month 6", 4, List.of(
            new Target("SQL problems", "sql", 50), new Target("Medium problems", "medium", 5)), 5)
    );

    private static final List<StageTarget> LIGHT_TARGETS = List.of(
        new StageTarget("Foundation", "Month 1", 4, List.of(new Target("Easy problems", "easy", 5)), 2),
        new StageTarget("Core Skills", "Months 2–3", 8, List.of(new Target("Easy problems", "easy", 15)), 3),
        new StageTarget("Portfolio", "Months 4–5", 8, List.of(new Target("Easy problems", "easy", 25)), 3),
        new StageTarget("Hiring Prep", "Month 6", 4, List.of(
            new Target("Easy problems", "easy", 30), new Target("Medium problems", "medium", 3)), 3)
    );

    /* ---------------- public API ---------------- */

    public static String track(String careerId) {
        if (careerId == null) return "dsa";
        return switch (careerId) {
            case "data", "product" -> "sql";
            case "uiux", "technical-writer" -> "light";
            default -> "dsa";
        };
    }

    public static List<StageTarget> targets(String careerId) {
        return switch (track(careerId)) {
            case "sql" -> SQL_TARGETS;
            case "light" -> LIGHT_TARGETS;
            default -> DSA_TARGETS;
        };
    }

    public static List<Section> sections(String careerId) {
        List<Section> out = new ArrayList<>();
        switch (track(careerId)) {
            case "sql" -> out.addAll(SQL_CORE);
            case "light" -> out.add(LOGIC);
            default -> out.addAll(DSA_CORE);
        }
        Section extra = EXTRA.get(careerId);
        if (extra != null && !"light".equals(track(careerId))) out.add(extra);
        return out;
    }

    public static List<Plan> plans(String careerId) {
        return switch (careerId == null ? "" : careerId) {
            case "data" -> List.of(new Plan("Top SQL 50", "top-sql-50"),
                    new Plan("Introduction to Pandas", "introduction-to-pandas"),
                    new Plan("30 Days of Pandas", "30-days-of-pandas"));
            case "product" -> List.of(new Plan("Top SQL 50", "top-sql-50"), new Plan("LeetCode 75", "leetcode-75"));
            case "web", "mobile" -> List.of(new Plan("LeetCode 75", "leetcode-75"),
                    new Plan("30 Days of JavaScript", "30-days-of-javascript"));
            case "uiux", "technical-writer" -> List.of(new Plan("LeetCode 75", "leetcode-75"));
            default -> List.of(new Plan("LeetCode 75", "leetcode-75"),
                    new Plan("Top Interview 150", "top-interview-150"));
        };
    }

    /** Builds the "practice" part of the API answer, marking problems the student has solved. */
    public static JsonObject practiceJson(String careerId, Set<String> solvedSlugs) {
        JsonObject out = new JsonObject();
        out.addProperty("track", track(careerId));
        JsonArray sections = new JsonArray();
        for (Section s : sections(careerId)) {
            JsonObject so = new JsonObject();
            so.addProperty("name", s.name());
            JsonArray ps = new JsonArray();
            for (Problem p : s.problems()) {
                JsonObject po = new JsonObject();
                po.addProperty("title", p.title());
                po.addProperty("slug", p.slug());
                po.addProperty("difficulty", p.difficulty());
                po.addProperty("url", "https://leetcode.com/problems/" + p.slug() + "/");
                po.addProperty("solved", solvedSlugs.contains(p.slug()));
                ps.add(po);
            }
            so.add("problems", ps);
            sections.add(so);
        }
        out.add("sections", sections);
        JsonArray plans = new JsonArray();
        for (Plan p : plans(careerId)) {
            JsonObject po = new JsonObject();
            po.addProperty("name", p.name());
            po.addProperty("url", "https://leetcode.com/studyplan/" + p.slug() + "/");
            plans.add(po);
        }
        out.add("plans", plans);
        return out;
    }

    /** Progress of each roadmap stage. done = what was solved since linking, per metric. */
    public static JsonArray targetsJson(String careerId, Map<String, Integer> done, int activeDaysThisWeek) {
        JsonArray arr = new JsonArray();
        boolean currentFound = false;
        Map<String, Integer> previousGoal = new java.util.HashMap<>();
        for (StageTarget st : targets(careerId)) {
            JsonObject so = new JsonObject();
            so.addProperty("stage", st.stage());
            so.addProperty("duration", st.duration());
            so.addProperty("weeklyDays", st.weeklyDays());
            JsonArray ts = new JsonArray();
            double sum = 0;
            int perWeek = 0;
            for (Target t : st.targets()) {
                int d = done.getOrDefault(t.metric(), 0);
                JsonObject to = new JsonObject();
                to.addProperty("label", t.label());
                to.addProperty("metric", t.metric());
                to.addProperty("goal", t.goal());
                to.addProperty("done", Math.min(d, t.goal()));
                to.addProperty("percent", (int) Math.round(Math.min(1.0, d / (double) t.goal()) * 100));
                ts.add(to);
                sum += Math.min(1.0, d / (double) t.goal());
                int newInStage = Math.max(0, t.goal() - previousGoal.getOrDefault(t.metric(), 0));
                perWeek += (int) Math.ceil(newInStage / (double) st.weeks());
            }
            for (Target t : st.targets()) previousGoal.put(t.metric(), t.goal());
            int percent = (int) Math.round(sum / st.targets().size() * 100);
            so.add("targets", ts);
            so.addProperty("percent", percent);
            so.addProperty("problemsPerWeek", perWeek);
            boolean current = !currentFound && percent < 100;
            if (current) {
                currentFound = true;
                so.addProperty("activeDaysThisWeek", activeDaysThisWeek);
            }
            so.addProperty("current", current);
            so.addProperty("complete", percent >= 100);
            arr.add(so);
        }
        return arr;
    }

    /* ---------------- skill-gap link ---------------- */

    static int level(int value, int... thresholds) {
        int lvl = 0;
        for (int i = 0; i < thresholds.length; i++) if (value >= thresholds[i]) lvl = i + 1;
        return lvl;
    }

    /** Suggested CareerVerse skill levels (1-5) worked out from LeetCode numbers. 0 = no evidence. */
    public static JsonArray suggestedSkills(JsonObject stats, Map<String, Integer> currentSkills) {
        JsonArray out = new JsonArray();
        JsonObject solved = stats.getAsJsonObject("solved");
        int easy = solved.get("easy").getAsInt();
        int medium = solved.get("medium").getAsInt();
        int hard = solved.get("hard").getAsInt();
        int dsaScore = easy + 2 * medium + 4 * hard;
        add(out, currentSkills, "DSA", level(dsaScore, 1, 40, 120, 300, 600),
                easy + " easy, " + medium + " medium, " + hard + " hard solved");

        int sql = stats.has("sqlSolved") ? stats.get("sqlSolved").getAsInt() : 0;
        add(out, currentSkills, "SQL", level(sql, 1, 10, 25, 45, 80), sql + " SQL problems solved");

        JsonObject langs = stats.has("languages") ? stats.getAsJsonObject("languages") : new JsonObject();
        int java = n(langs, "Java");
        int python = n(langs, "Python3") + n(langs, "Python");
        int js = n(langs, "JavaScript") + n(langs, "TypeScript");
        int kotlin = n(langs, "Kotlin");
        int dart = n(langs, "Dart");
        int bash = n(langs, "Bash");
        add(out, currentSkills, "Java", level(java, 1, 15, 50, 120, 250), java + " problems solved in Java");
        add(out, currentSkills, "Python", level(python, 1, 15, 50, 120, 250), python + " problems solved in Python");
        add(out, currentSkills, "JavaScript", level(js, 1, 15, 50, 120, 250), js + " problems solved in JavaScript/TypeScript");
        add(out, currentSkills, "Kotlin", level(kotlin, 1, 15, 50, 120, 250), kotlin + " problems solved in Kotlin");
        add(out, currentSkills, "Dart", level(dart, 1, 15, 50, 120, 250), dart + " problems solved in Dart");
        add(out, currentSkills, "Bash", level(bash, 1, 3, 4, 5, 6), bash + " shell problems solved");
        return out;
    }

    private static int n(JsonObject o, String k) {
        return o.has(k) ? o.get(k).getAsInt() : 0;
    }

    private static void add(JsonArray out, Map<String, Integer> current, String skill, int suggested, String reason) {
        if (suggested <= 0) return;
        int now = current.getOrDefault(skill.toLowerCase(), 0);
        JsonObject o = new JsonObject();
        o.addProperty("skill", skill);
        o.addProperty("current", now);
        o.addProperty("suggested", suggested);
        o.addProperty("canRaise", suggested > now);
        o.addProperty("reason", reason);
        out.add(o);
    }
}
