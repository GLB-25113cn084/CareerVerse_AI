package com.careerverse.service;

import com.careerverse.model.CareerData;

import java.time.LocalDate;
import java.util.*;

/**
 * The scoring and planning logic of CareerVerse.
 *
 * recommend() - career fit scores for the careers of the student's branch
 * gap()       - skill-gap analysis for one career
 * roadmap()   - a semester-by-semester plan from now until the end of 4th year
 */
public class CareerEngine {

    /** Extra points for careers that are "core" for the student's branch (e.g. VLSI for ECE). */
    static final int CORE_BONUS = 4;

    /* ===================== RECOMMENDATION ===================== */

    public static List<Map<String, Object>> recommend(Map<String, Integer> aptitude,
                                                      Map<String, Integer> userSkills,
                                                      String branch) {
        List<Map<String, Object>> out = new ArrayList<>();
        double aptitudeScore = aptitude.values().stream().mapToInt(i -> i).average().orElse(50) * .40;

        for (CareerData.Career c : CareerData.forBranch(branch)) {
            double weighted = 0;
            double max = 0;
            for (var e : c.skillWeights().entrySet()) {
                max += e.getValue();
                int level = userSkills.getOrDefault(e.getKey().toLowerCase(), 0);
                weighted += e.getValue() * (level / 5.0);
            }
            double skillScore = max == 0 ? 0 : (weighted / max) * 50;
            boolean core = CareerData.isCore(c, branch);
            double fit = Math.min(99, skillScore + aptitudeScore + 10 + (core ? CORE_BONUS : 0));

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.id());
            m.put("name", c.name());
            m.put("icon", c.icon());
            m.put("description", c.description());
            m.put("match", (int) Math.round(fit));
            m.put("core", core);
            m.put("skills", c.skills());
            out.add(m);
        }
        out.sort((a, b) -> Integer.compare((Integer) b.get("match"), (Integer) a.get("match")));
        return out;
    }

    /* ===================== SKILL GAP ===================== */

    public static List<Map<String, Object>> gap(CareerData.Career c, Map<String, Integer> userSkills) {
        List<Map<String, Object>> out = new ArrayList<>();
        int max = c.skillWeights().values().stream().mapToInt(x -> x).sum();
        for (var e : c.skillWeights().entrySet()) {
            int level = userSkills.getOrDefault(e.getKey().toLowerCase(), 0);
            int current = level == 0 ? 0 : level * 20;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("skill", e.getKey());
            m.put("importance", Math.round(e.getValue() * 100.0 / max));
            m.put("current", current);
            m.put("gap", 100 - current);
            m.put("status", level >= 4 ? "Strong" : level > 0 ? "Develop further" : "Priority gap");
            out.add(m);
        }
        out.sort((a, b) -> Integer.compare((Integer) b.get("gap"), (Integer) a.get("gap")));
        return out;
    }

    /* ===================== ROADMAP ===================== */

    private static final Map<String, Integer> LEVEL_ORDER =
            Map.of("Beginner", 0, "Intermediate", 1, "Advanced", 2, "Capstone", 3);

    /**
     * Semester the student is in now. Odd semesters run Jul–Dec, even semesters Jan–Jun.
     * 2nd year in October -> semester 3; 2nd year in March -> semester 4.
     */
    public static int currentSemester(int year, LocalDate today) {
        int y = Math.max(1, Math.min(4, year));
        return 2 * y - 1 + (today.getMonthValue() <= 6 ? 1 : 0);
    }

    private static String phaseName(int sem) {
        return switch (sem) {
            case 1, 2 -> "Foundations";
            case 3, 4 -> "Core Skills";
            case 5 -> "Specialisation";
            case 6 -> "Advanced Skills & Internship";
            case 7 -> "Major Project & Placement Prep";
            default -> "Placements & Launch";
        };
    }

    private static List<String> milestones(int sem, CareerData.Career c) {
        String role = c.roles().isEmpty() ? "an intern" : c.roles().get(0);
        String roles = String.join(", ", c.roles());
        if ("gate".equals(c.id())) {
            return switch (sem) {
                case 1, 2 -> List.of("Score well in Engineering Mathematics – it is 13% of GATE", "Make neat notes for every subject");
                case 3, 4 -> List.of("Start topic-wise previous year questions alongside college exams", "Join a technical club in your branch");
                case 5 -> List.of("Finish the first round of all core subjects", "Do a summer internship at a PSU, IIT or core company");
                case 6 -> List.of("Begin weekly topic tests", "Pick a final-year project in a core area");
                case 7 -> List.of("Fill the GATE application form (Aug–Sep)", "Take full-length mock tests every week", "Revise using your short notes");
                default -> List.of("Write GATE (February)", "Apply to PSUs through GATE score and to IITs/NITs via COAP/CCMT", "Prepare for PSU interviews and group discussions");
            };
        }
        return switch (sem) {
            case 1 -> List.of("Join a technical club and set up GitHub and LinkedIn profiles", "Explore careers in CareerVerse and shortlist two");
            case 2 -> List.of("Take part in your first hackathon or tech fest", "Finish one online course and share the certificate on LinkedIn");
            case 3 -> List.of("Start a daily practice habit (coding, aptitude or subject problems)", "Attend industry talks and workshops");
            case 4 -> List.of("Apply for a summer training or internship as " + role, "Work on a team project or an open-source contribution");
            case 5 -> List.of("Earn one recognised certification", "Apply for internships: " + roles);
            case 6 -> List.of("Secure your summer internship (" + role + ")", "Start aptitude and interview preparation", "Choose your final-year project topic");
            case 7 -> List.of("Prepare for campus placements: resume, aptitude tests, mock interviews",
                              "Build your major project", "Apply off-campus on LinkedIn, Naukri and Internshala");
            default -> List.of("Convert your internship into a PPO or crack placement drives",
                               "Finish and present your major project", "Get ready for your first job: documentation, teamwork, communication");
        };
    }

    /** Builds a semester-by-semester roadmap from the current semester until semester 8. */
    public static List<Map<String, Object>> roadmap(CareerData.Career c, int year, LocalDate today) {
        int start = currentSemester(year, today);
        List<Integer> sems = new ArrayList<>();
        for (int s = start; s <= 8; s++) sems.add(s);
        int n = sems.size();
        boolean fastTrack = start >= 5;

        // Semesters used for learning: all except the last one (placements), unless there are only 1–2 left.
        List<Integer> learn = n <= 2 ? new ArrayList<>(sems) : new ArrayList<>(sems.subList(0, n - 1));
        // A fresher's first semester is for general foundations, so career modules start from semester 2.
        boolean genericFirst = start == 1;
        List<Integer> moduleSems = genericFirst ? learn.subList(1, learn.size()) : learn;

        Map<Integer, List<CareerData.Module>> modulesBySem = new HashMap<>();
        List<CareerData.Module> modules = c.modules();
        for (int i = 0; i < modules.size(); i++) {
            int idx = (int) ((long) i * moduleSems.size() / modules.size());
            modulesBySem.computeIfAbsent(moduleSems.get(idx), k -> new ArrayList<>()).add(modules.get(i));
        }

        // Projects: skip beginner ones for students who start late.
        List<CareerData.Project> projects = new ArrayList<>(c.projects());
        projects.sort(Comparator.comparingInt(p -> LEVEL_ORDER.getOrDefault(p.level(), 1)));
        if (fastTrack) projects.removeIf(p -> "Beginner".equals(p.level()));
        Map<Integer, List<CareerData.Project>> projectsBySem = new HashMap<>();
        for (int j = 0; j < projects.size(); j++) {
            int idx = (int) ((long) j * learn.size() / projects.size());
            projectsBySem.computeIfAbsent(learn.get(idx), k -> new ArrayList<>()).add(projects.get(j));
        }

        Map<Integer, List<String>> certsBySem = new HashMap<>();
        List<String> certs = c.certifications();
        // Certifications start from the second learning semester when possible.
        List<Integer> certSems = learn.size() > 2 ? learn.subList(1, learn.size()) : learn;
        for (int k = 0; k < certs.size(); k++) {
            int idx = (int) ((long) k * certSems.size() / certs.size());
            certsBySem.computeIfAbsent(certSems.get(idx), x -> new ArrayList<>()).add(certs.get(k));
        }

        List<Map<String, Object>> out = new ArrayList<>();
        int calendarYear = today.getYear();
        boolean odd = start % 2 == 1;
        for (int i = 0; i < n; i++) {
            int sem = sems.get(i);
            int yearOfStudy = (sem + 1) / 2;
            String months = (sem % 2 == 1 ? "Jul–Dec " : "Jan–Jun ") + calendarYear;

            List<Map<String, Object>> moduleList = new ArrayList<>();
            if (genericFirst && sem == 1) {
                moduleList.add(module("First-year Foundations",
                        List.of("Programming in C or Python", "Engineering mathematics", "Communication and presentation skills",
                                "Explore your branch through clubs and labs")));
            }
            for (CareerData.Module m : modulesBySem.getOrDefault(sem, List.of())) {
                moduleList.add(module(m.title(), fastTrack && n <= 2 ? m.topics().subList(0, Math.min(2, m.topics().size())) : m.topics()));
            }

            List<CareerData.Project> semProjects = projectsBySem.getOrDefault(sem, List.of());
            List<String> semCerts = certsBySem.getOrDefault(sem, List.of());

            String goal;
            if (sem == 8 && n > 1) {
                goal = "Turn your skills into an offer: placements, internship conversion and final project";
            } else if (fastTrack) {
                goal = "Fast track: cover " + (moduleList.isEmpty() ? "advanced practice" : titles(moduleList)) + " while preparing for placements";
            } else {
                goal = phaseName(sem) + ": " + (moduleList.isEmpty() ? "deepen and apply what you learnt" : titles(moduleList));
            }

            String checkpoint;
            if (sem == 8) {
                checkpoint = "A job offer, PPO or higher-studies admission – and a portfolio that shows " + c.name() + " skills.";
            } else if (!semProjects.isEmpty()) {
                checkpoint = "By the end of Semester " + sem + " you can explain " + (moduleList.isEmpty() ? "your work" : titles(moduleList))
                        + " and show \"" + semProjects.get(semProjects.size() - 1).title() + "\" on GitHub or in your portfolio.";
            } else {
                checkpoint = "By the end of Semester " + sem + " you can explain " + titles(moduleList) + " confidently.";
            }

            List<String> tasks = new ArrayList<>(milestones(sem, c));
            if (sem == 8 && n > 1) tasks.add(0, "Polish your best project and record a 2-minute demo video");

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("semester", sem);
            m.put("yearOfStudy", yearOfStudy);
            m.put("title", "Year " + yearOfStudy + " · Semester " + sem);
            m.put("phase", phaseName(sem));
            m.put("duration", months);
            m.put("current", i == 0);
            m.put("goal", goal);
            m.put("modules", moduleList);
            m.put("projects", semProjects);
            m.put("certifications", semCerts);
            m.put("tasks", tasks);
            m.put("checkpoint", checkpoint);
            m.put("languages", c.languages());
            out.add(m);

            if (odd) calendarYear++;
            odd = !odd;
        }
        return out;
    }

    /** Old 4-stage roadmap kept for compatibility: uses 1st year in July. */
    public static List<Map<String, Object>> roadmap(CareerData.Career c) {
        return roadmap(c, 1, LocalDate.of(LocalDate.now().getYear(), 7, 1));
    }

    private static Map<String, Object> module(String title, List<String> topics) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("title", title);
        m.put("topics", topics);
        return m;
    }

    private static String titles(List<Map<String, Object>> modules) {
        List<String> t = new ArrayList<>();
        for (Map<String, Object> m : modules) t.add(String.valueOf(m.get("title")));
        if (t.size() <= 1) return t.isEmpty() ? "" : t.get(0);
        return String.join(", ", t.subList(0, t.size() - 1)) + " and " + t.get(t.size() - 1);
    }
}
