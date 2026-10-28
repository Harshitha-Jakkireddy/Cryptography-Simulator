/**
 * Caesar cipher: shifts every letter by a fixed amount. Non-letters are left unchanged.
 */
public class CaesarCipher {

    public static String encrypt(String text, int shift) {
        return shiftText(text, shift);
    }

    public static String decrypt(String text, int shift) {
        return shiftText(text, -shift);
    }

    private static String shiftText(String text, int shift) {
        int s = Math.floorMod(shift, 26);
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                sb.append((char) ('A' + (c - 'A' + s) % 26));
            } else if (c >= 'a' && c <= 'z') {
                sb.append((char) ('a' + (c - 'a' + s) % 26));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
