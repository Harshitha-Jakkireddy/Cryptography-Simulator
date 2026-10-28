# Classical Cryptography Simulator

Caesar and Vigenère ciphers in Java with encryption, decryption and cryptanalysis, plus a dark Swing UI.

## Structure
```
src/
  CaesarCipher.java     Caesar encrypt/decrypt
  VigenereCipher.java   Vigenère encrypt/decrypt
  Cryptanalysis.java    Brute force, chi-squared, Index of Coincidence key recovery
  CipherSimulator.java  Swing UI (main class)
  CryptoTest.java       Console tests
```

## Run in VS Code
1. Install JDK 11+ and the "Extension Pack for Java" extension.
2. File > Open Folder... and choose this `crypto-simulator` folder.
3. Open `src/CipherSimulator.java` and click **Run** above `main`
   (or press F5 and pick "Run Simulator (UI)").

## Run from a terminal
```
cd src
javac -encoding UTF-8 *.java
java CipherSimulator      # UI
java CryptoTest           # tests
```

## Using the app
- Caesar: key is a number (shift). Vigenère: key is a keyword.
- Encrypt / Decrypt use the key you enter.
- Cryptanalyze ignores the key and attacks the input text; the recovered key fills the key field.
- Vigenère cryptanalysis needs about 100+ letters. "Load sample" gives a long enough text.
