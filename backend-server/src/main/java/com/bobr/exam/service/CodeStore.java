package com.bobr.exam.service;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CodeStore {

    private static class Entry {
        String code;
        long expires;
        Entry(String c, long e) { code = c; expires = e; }
    }

    private final Map<String, Entry> map = new ConcurrentHashMap<>();
    private final SecureRandom rnd = new SecureRandom();

    public String issue(String key) {
        String code = String.format("%06d", rnd.nextInt(1000000));
        map.put(key.toLowerCase(), new Entry(code, Instant.now().getEpochSecond() + 600));
        return code;
    }

    public boolean verify(String key, String code) {
        Entry e = map.get(key.toLowerCase());
        if (e == null) return false;
        if (Instant.now().getEpochSecond() > e.expires) {
            map.remove(key.toLowerCase());
            return false;
        }
        boolean ok = e.code.equals(code);
        if (ok) map.remove(key.toLowerCase());
        return ok;
    }
}
