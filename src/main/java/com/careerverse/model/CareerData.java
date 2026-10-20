package com.careerverse.model;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * All careers known to CareerVerse.
 *
 * The data lives in src/main/resources/careers.json so it can be edited
 * without changing Java code. Each career says which branches it suits
 * (cs, ece, ee, me, civil), its skills, learning modules, projects,
 * certifications and internship roles.
 */
public class CareerData {

    public record Career(String id, String name, String icon, String description,
                         List<String> branches, List<String> core,
                         List<String> languages, List<String> skills,
                         List<Project> projects, List<Stage> stages,
                         Map<String, Integer> skillWeights,
                         List<Module> modules, List<String> certifications, List<String> roles) {}

    public record Project(String title, String level, String result, List<String> skills) {}

    public record Module(String title, List<String> topics) {}

    /** A stage of the career simulator. */
    public record Stage(String title, String duration, String goal, List<String> tasks, String checkpoint) {}

    /* JSON shape of careers.json (plain classes so Gson can fill them). */
    private static class FileJson { List<CareerJson> careers; }
    private static class CareerJson {
        String id, name, icon, description;
        List<String> branches, core, languages, skills, certifications, roles;
        Map<String, Integer> skillWeights;
        List<ProjectJson> projects;
        List<ModuleJson> modules;
    }
    private static class ProjectJson { String title, level, result; List<String> skills; }
    private static class ModuleJson { String title; List<String> topics; }

    public static final List<Career> ALL = load();

    private static List<Career> load() {
        try (Reader in = new InputStreamReader(
                Objects.requireNonNull(CareerData.class.getClassLoader().getResourceAsStream("careers.json"),
                        "careers.json is missing from src/main/resources"),
                StandardCharsets.UTF_8)) {
            FileJson file = new Gson().fromJson(in, FileJson.class);
            List<Career> out = new ArrayList<>();
            for (CareerJson c : file.careers) {
                List<Project> projects = new ArrayList<>();
                for (ProjectJson p : c.projects) projects.add(new Project(p.title, p.level, p.result, List.copyOf(p.skills)));
                List<Module> modules = new ArrayList<>();
                for (ModuleJson m : c.modules) modules.add(new Module(m.title, List.copyOf(m.topics)));
                out.add(new Career(c.id, c.name, c.icon, c.description,
                        List.copyOf(c.branches), c.core == null ? List.of() : List.copyOf(c.core),
                        List.copyOf(c.languages), List.copyOf(c.skills),
                        List.copyOf(projects), simulatorStages(c.name, modules, projects, c.roles),
                        Collections.unmodifiableMap(new LinkedHashMap<>(c.skillWeights)),
                        List.copyOf(modules), List.copyOf(c.certifications), List.copyOf(c.roles)));
            }
            return List.copyOf(out);
        } catch (Exception e) {
            throw new IllegalStateException("Could not load careers.json: " + e.getMessage(), e);
        }
    }

    /** Four simulator stages built from the career's own modules, projects and roles. */
    private static List<Stage> simulatorStages(String name, List<Module> modules, List<Project> projects, List<String> roles) {
        String firstProject = projects.get(0).title();
        String midProject = projects.get(Math.min(3, projects.size() - 1)).title();
        String capstone = projects.get(projects.size() - 1).title();
        List<String> foundation = new ArrayList<>(modules.get(0).topics());
        foundation.add("Build: " + firstProject);
        List<String> core = new ArrayList<>();
        for (int i = 1; i < Math.min(4, modules.size()); i++) core.add("Learn " + modules.get(i).title());
        core.add("Build: " + midProject);
        return List.of(
            new Stage("Foundation", "Months 1–2", "Build the fundamentals of " + name, foundation,
                    "You can explain the basics of " + modules.get(0).title() + " without notes."),
            new Stage("Core Skills", "Months 3–6", "Develop job-ready depth", core,
                    "You have finished " + midProject + " and can explain how it works."),
            new Stage("Portfolio", "Months 7–10", "Create proof of your ability",
                    List.of("Build: " + capstone, "Write a clear README / report", "Demo it to friends and mentors", "Share it on LinkedIn"),
                    "Your capstone project is demo-ready."),
            new Stage("Hiring Prep", "Months 11–12", "Become internship / job ready",
                    List.of("Update resume and LinkedIn", "Practise mock interviews", "Apply for " + String.join(", ", roles), "Track every application"),
                    "You are applying every week and getting interview calls."));
    }

    public static Career get(String id) {
        if (id == null) return null;
        for (Career c : ALL) if (c.id().equals(id)) return c;
        return null;
    }

    /**
     * Maps the branch text saved in the profile to a branch group:
     * cs, ece, ee, me, civil, or "all" when the branch is empty/other.
     */
    public static String branchGroup(String branch) {
        if (branch == null) return "all";
        String b = branch.toLowerCase(Locale.ROOT);
        if (b.isBlank()) return "all";
        if (b.contains("computer") || b.contains("information technology") || b.contains("artificial")
                || b.contains("data science") || b.contains("cyber") || b.contains("software")) return "cs";
        if (b.contains("electrical")) return "ee";
        if (b.contains("electronics") || b.contains("communication") || b.contains("instrumentation")) return "ece";
        if (b.contains("mechanical") || b.contains("automobile") || b.contains("production")
                || b.contains("mechatronics") || b.contains("aeronautical")) return "me";
        if (b.contains("civil")) return "civil";
        return "all";
    }

    /** Careers that suit a branch. Unknown branch -> every career. */
    public static List<Career> forBranch(String branch) {
        String group = branchGroup(branch);
        if ("all".equals(group)) return ALL;
        List<Career> out = new ArrayList<>();
        for (Career c : ALL) if (c.branches().contains(group)) out.add(c);
        return out;
    }

    /** True when the career is a core career of the student's branch (e.g. VLSI for ECE). */
    public static boolean isCore(Career c, String branch) {
        return c.core().contains(branchGroup(branch));
    }
}
