/** Headless sanity checks for the cipher logic. Run: java CryptoTest */
public class CryptoTest {
    static int failures = 0;

    static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
        if (!ok) failures++;
    }

    public static void main(String[] args) {
        // Known vectors
        check("Caesar encrypt", CaesarCipher.encrypt("Hello, World!", 3).equals("Khoor, Zruog!"));
        check("Caesar decrypt", CaesarCipher.decrypt("Khoor, Zruog!", 3).equals("Hello, World!"));
        check("Caesar negative shift", CaesarCipher.encrypt("abc", -1).equals("zab"));
        check("Vigenere encrypt", VigenereCipher.encrypt("ATTACKATDAWN", "LEMON").equals("LXFOPVEFRNHR"));
        check("Vigenere decrypt", VigenereCipher.decrypt("LXFOPVEFRNHR", "LEMON").equals("ATTACKATDAWN"));
        check("Vigenere keeps punctuation",
            VigenereCipher.decrypt(VigenereCipher.encrypt("Attack at dawn!", "Lemon"), "Lemon").equals("Attack at dawn!"));

        String plain = "Classical ciphers were the first tools people used to hide messages. A Caesar cipher "
            + "simply slides every letter along the alphabet by a fixed amount, which means there are "
            + "only twenty five possible keys to try. The Vigenere cipher improves on this by using a "
            + "keyword, so the same plaintext letter can turn into many different ciphertext letters. "
            + "Even so, patterns in the language leak through, and an analyst who counts letters "
            + "carefully can recover the secret keyword without ever having been told it.";

        // Caesar attack
        String c = CaesarCipher.encrypt(plain, 11);
        Cryptanalysis.CaesarResult best = Cryptanalysis.caesarBruteForce(c).get(0);
        check("Caesar attack finds shift 11", best.shift == 11 && best.plaintext.equals(plain));

        // Vigenere attack with several key lengths
        for (String key : new String[] {"KEY", "CIPHER", "SECRETS"}) {
            String vc = VigenereCipher.encrypt(plain, key);
            Cryptanalysis.VigenereResult r = Cryptanalysis.crackVigenere(vc, 12);
            System.out.println("      key " + key + " -> recovered " + r.key);
            check("Vigenere attack recovers " + key, r.key.equals(key) && r.plaintext.equals(plain));
        }

        System.out.println(failures == 0 ? "\nAll checks passed." : "\n" + failures + " check(s) FAILED.");
        System.exit(failures == 0 ? 0 : 1);
    }
}
