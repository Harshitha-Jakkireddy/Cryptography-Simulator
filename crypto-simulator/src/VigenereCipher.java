/**
 * Vigenère cipher: a repeating keyword selects a different Caesar shift for each letter.
 * The key only advances on letters, so spaces and punctuation don't consume key characters.
 */
public class VigenereCipher {

    /** Keeps only letters of the key, uppercased. */
    public static String cleanKey(String key) {
        StringBuilder sb = new StringBuilder();
        for (char c : key.toCharArray()) {
            if (Character.isLetter(c) && c < 128) sb.append(Character.toUpperCase(c));
        }
        return sb.toString();
    }

    public static String encrypt(String text, String key) {
        return process(text, key, 1);
    }

    public static String decrypt(String text, String key) {
        return process(text, key, -1);
    }

    private static String process(String text, String key, int direction) {
        String k = cleanKey(key);
        if (k.isEmpty()) {
            throw new IllegalArgumentException("Key must contain at least one letter (A-Z).");
        }
        StringBuilder sb = new StringBuilder(text.length());
        int ki = 0;
        for (char c : text.toCharArray()) {
            char base;
            if (c >= 'A' && c <= 'Z') base = 'A';
            else if (c >= 'a' && c <= 'z') base = 'a';
            else {
                sb.append(c);
                continue;
            }
            int shift = (k.charAt(ki % k.length()) - 'A') * direction;
            sb.append((char) (base + Math.floorMod(c - base + shift, 26)));
            ki++;
        }
        return sb.toString();
    }
}
