package com.blazemeter.taurus.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Utils {
    private static final String CSV_QUOTE_STR = String.valueOf('"');
    private static final char[] CSV_SEARCH_CHARS = new char[]{',', '"', '\r', '\n'};

    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    public static String escapeCSV(String input) {
        if (containsNone(input, CSV_SEARCH_CHARS)) {
            return input;
        } else {
            StringWriter out = new StringWriter(input.length() * 2);
            out.write(34);
            out.write(input.replace(CSV_QUOTE_STR, CSV_QUOTE_STR + CSV_QUOTE_STR));
            out.write(34);
            return out.toString();
        }
    }

    public static boolean containsNone(CharSequence cs, char... searchChars) {
        if (cs != null && searchChars != null) {
            int csLen = cs.length();
            int csLast = csLen - 1;
            int searchLen = searchChars.length;
            int searchLast = searchLen - 1;

            for (int i = 0; i < csLen; ++i) {
                char ch = cs.charAt(i);

                for (int j = 0; j < searchLen; ++j) {
                    if (searchChars[j] == ch) {
                        if (!Character.isHighSurrogate(ch)) {
                            return false;
                        }

                        if (j == searchLast) {
                            return false;
                        }

                        if (i < csLast && searchChars[j + 1] == cs.charAt(i + 1)) {
                            return false;
                        }
                    }
                }
            }

            return true;
        } else {
            return true;
        }
    }
}
