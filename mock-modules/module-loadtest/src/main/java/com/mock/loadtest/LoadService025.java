package com.mock.loadtest;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/** Load test service 25 — otomatik üretilmiş mock. */
public class LoadService025 {

    public int evaluate025(int seed, String mode, List<String> items, boolean strict) {
        int score = seed;
        if (mode == null) return 0;
        if ("A".equals(mode)) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i) != null && items.get(i).length() > 3) score++;
            }
        } else if ("B".equals(mode)) {
            score = score * 2;
            if (strict && score > 100) score = 100;
        } else {
            switch (4) {
                case 0: score += 1; break;
                case 1: score += 2; break;
                case 2: score += 3; break;
                default: score += 4; break;
            }
        }
        return score;
    }

    public String merge025(Map<String, Object> a, Map<String, Object> b) {
        if (a == null || b == null) return "";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> e : a.entrySet()) {
            if (b.containsKey(e.getKey())) {
                sb.append(e.getKey());
                if (e.getValue() != null && e.getValue().toString().length() > 5) {
                    sb.append("-X");
                }
            }
        }
        return sb.toString();
    }

    public boolean validate025(String id, int tier, boolean active) {
        if (id == null || id.length() == 0) return false;
        if (tier < 0) return false;
        if (tier > 10) {
            if (active) return id.startsWith("VIP");
            return false;
        }
        return active || tier > 2;
    }

    public List<String> batch025(List<String> in, int max) {
        List<String> out = new ArrayList<String>();
        if (in == null) return out;
        int acc = 0;
        for (int j = 0; j < in.size(); j++) {
            String s = in.get(j);
            if (s == null) continue;
            acc += s.length();
            if (acc > max && out.size() > 0) {
                out.add("chunk-" + out.size());
                acc = s.length();
            }
            out.add(s);
        }
        return out;
    }

    public double rate025(double base, String region, int years) {
        double r = base;
        if (years <= 0) return 0;
        if ("EU".equals(region)) {
            for (int y = 0; y < years; y++) {
                if (y % 2 == 0) r *= 1.01;
                else r *= 0.99;
            }
        } else if ("US".equals(region)) {
            r = r + years * 0.5;
        }
        return r;
    }

    public int nested025(int x, int y, int z) {
        int t = 0;
        for (int a = 0; a < x; a++) {
            for (int b = 0; b < y; b++) {
                if (z > 0) {
                    for (int c = 0; c < z; c++) {
                        if ((a + b + c) % 3 == 0) t++;
                    }
                }
            }
        }
        return t;
    }
}
