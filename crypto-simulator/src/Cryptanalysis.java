import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Cryptanalysis helpers:
 *  - Caesar: brute force all 26 shifts, rank by chi-squared distance from English letter frequencies.
 *  - Vigenère: estimate key length with the Index of Coincidence, then recover each key letter
 *    by solving one Caesar cipher per key position (chi-squared again).
 */
public class Cryptanalysis {

    /** Standard English letter frequencies (percent), A..Z. */
    public static final double[] ENGLISH_FREQ = {
        8.167, 1.492, 2.782, 4.253, 12.702, 2.228, 2.015, 6.094, 6.966, 0.153,
        0.772, 4.025, 2.406, 6.749, 7.507, 1.929, 0.095, 5.987, 6.327, 9.056,
        2.758, 0.978, 2.360, 0.150, 1.974, 0.074
    };

    // ---------- basic statistics ----------

    public static String lettersOnly(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c >= 'A' && c <= 'Z') sb.append(c);
            else if (c >= 'a' && c <= 'z') sb.append((char) (c - 32));
        }
        return sb.toString();
    }

    public static int[] letterCounts(String text) {
        int[] counts = new int[26];
        for (char c : lettersOnly(text).toCharArray()) counts[c - 'A']++;
        return counts;
    }

    public static double[] letterPercent(String text) {
        int[] counts = letterCounts(text);
        int total = 0;
        for (int c : counts) total += c;
        double[] pct = new double[26];
        if (total == 0) return pct;
        for (int i = 0; i < 26; i++) pct[i] = 100.0 * counts[i] / total;
        return pct;
    }

    /** Lower = closer to English. */
    public static double chiSquared(int[] counts) {
        int n = 0;
        for (int c : counts) n += c;
        if (n == 0) return Double.MAX_VALUE;
        double chi = 0;
        for (int i = 0; i < 26; i++) {
            double expected = n * ENGLISH_FREQ[i] / 100.0;
            double diff = counts[i] - expected;
            chi += diff * diff / expected;
        }
        return chi;
    }

    /** English ≈ 0.066, uniformly random text ≈ 0.038. */
    public static double indexOfCoincidence(String letters) {
        int[] counts = new int[26];
        for (char c : letters.toCharArray()) counts[c - 'A']++;
        long n = letters.length();
        if (n < 2) return 0;
        long sum = 0;
        for (int c : counts) sum += (long) c * (c - 1);
        return (double) sum / (n * (n - 1));
    }

    // ---------- Caesar ----------

    public static class CaesarResult {
        public final int shift;
        public final String plaintext;
        public final double score;

        CaesarResult(int shift, String plaintext, double score) {
            this.shift = shift;
            this.plaintext = plaintext;
            this.score = score;
        }
    }

    /** Tries all 26 shifts; the result list is sorted best-first. */
    public static List<CaesarResult> caesarBruteForce(String cipherText) {
        List<CaesarResult> results = new ArrayList<>();
        for (int shift = 0; shift < 26; shift++) {
            String plain = CaesarCipher.decrypt(cipherText, shift);
            results.add(new CaesarResult(shift, plain, chiSquared(letterCounts(plain))));
        }
        results.sort(Comparator.comparingDouble(r -> r.score));
        return results;
    }

    // ---------- Vigenère ----------

    public static class VigenereResult {
        public int keyLength;
        public String key;
        public String plaintext;
        public double[] icByLength; // index 1..maxLen
        public int maxLen;
    }

    private static String column(String letters, int keyLen, int col) {
        StringBuilder sb = new StringBuilder();
        for (int i = col; i < letters.length(); i += keyLen) sb.append(letters.charAt(i));
        return sb.toString();
    }

    private static double averageIC(String letters, int keyLen) {
        double total = 0;
        for (int c = 0; c < keyLen; c++) total += indexOfCoincidence(column(letters, keyLen, c));
        return total / keyLen;
    }

    /** Best Caesar shift for one column = the key letter at that position. */
    private static char bestKeyLetter(String columnText) {
        int[] counts = letterCounts(columnText);
        int bestShift = 0;
        double bestScore = Double.MAX_VALUE;
        for (int shift = 0; shift < 26; shift++) {
            // Plaintext count of letter i, assuming key shift 'shift', is counts[(i + shift) % 26]
            int[] shifted = new int[26];
            for (int i = 0; i < 26; i++) shifted[i] = counts[(i + shift) % 26];
            double score = chiSquared(shifted);
            if (score < bestScore) {
                bestScore = score;
                bestShift = shift;
            }
        }
        return (char) ('A' + bestShift);
    }

    public static VigenereResult crackVigenere(String cipherText, int maxLen) {
        String letters = lettersOnly(cipherText);
        VigenereResult r = new VigenereResult();
        r.maxLen = Math.max(1, Math.min(maxLen, letters.length() / 4));
        r.icByLength = new double[r.maxLen + 1];

        double best = 0;
        for (int len = 1; len <= r.maxLen; len++) {
            r.icByLength[len] = averageIC(letters, len);
            best = Math.max(best, r.icByLength[len]);
        }
        // Prefer the smallest length that is close to the best (multiples of the true length also score high).
        r.keyLength = 1;
        for (int len = 1; len <= r.maxLen; len++) {
            if (r.icByLength[len] >= best - 0.008) {
                r.keyLength = len;
                break;
            }
        }

        StringBuilder key = new StringBuilder();
        for (int c = 0; c < r.keyLength; c++) key.append(bestKeyLetter(column(letters, r.keyLength, c)));
        r.key = key.toString();
        r.plaintext = VigenereCipher.decrypt(cipherText, r.key);
        return r;
    }
}
