import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

/**
 * Dark-themed Swing front end for the Classical Cryptography Simulator.
 * All cipher logic lives in CaesarCipher, VigenereCipher and Cryptanalysis.
 */
public class CipherSimulator extends JFrame {

    static final Color BG = new Color(0x0D0D0F);
    static final Color PANEL = new Color(0x16161A);
    static final Color FIELD = new Color(0x1E1E24);
    static final Color FG = new Color(0xE6E6E6);
    static final Color MUTED = new Color(0x8A8A94);
    static final Color ACCENT = new Color(0x00E5A8);
    static final Color WARN = new Color(0xFFB454);

    private static final String SAMPLE =
        "Classical ciphers were the first tools people used to hide messages. A Caesar cipher "
      + "simply slides every letter along the alphabet by a fixed amount, which means there are "
      + "only twenty five possible keys to try. The Vigenere cipher improves on this by using a "
      + "keyword, so the same plaintext letter can turn into many different ciphertext letters. "
      + "Even so, patterns in the language leak through, and an analyst who counts letters "
      + "carefully can recover the secret keyword without ever having been told it.";

    private final JComboBox<String> cipherBox =
        new JComboBox<>(new String[] {"Caesar", "Vigen\u00e8re"});
    private final JLabel keyLabel = new JLabel("Shift (0-25)");
    private final JTextField keyField = new JTextField("3", 12);
    private final JTextArea inputArea = makeArea(true);
    private final JTextArea outputArea = makeArea(false);
    private final JTextArea reportArea = makeArea(false);
    private final FrequencyPanel freqPanel = new FrequencyPanel();

    public CipherSimulator() {
        super("Classical Cryptography Simulator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        inputArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { freqPanel.setText(inputArea.getText()); }
            public void removeUpdate(DocumentEvent e) { freqPanel.setText(inputArea.getText()); }
            public void changedUpdate(DocumentEvent e) { freqPanel.setText(inputArea.getText()); }
        });

        cipherBox.addActionListener(e -> {
            boolean caesar = cipherBox.getSelectedIndex() == 0;
            keyLabel.setText(caesar ? "Shift (0-25)" : "Keyword");
            keyField.setText(caesar ? "3" : "KEY");
        });

        reportArea.setText("Pick a cipher, enter text and a key, then Encrypt / Decrypt.\n"
            + "Use Cryptanalyze to attack the input text without knowing the key.\n"
            + "Tip: press 'Load sample' for a text long enough to break Vigen\u00e8re.");

        setSize(1050, 700);
        setMinimumSize(new Dimension(800, 560));
        setLocationRelativeTo(null);
    }

    // ---------- layout ----------

    private JComponent buildHeader() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 12));
        p.setBackground(PANEL);

        JLabel title = new JLabel("\u25B8 CRYPTO SIMULATOR");
        title.setForeground(ACCENT);
        title.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));

        JLabel cipherLabel = new JLabel("Cipher");
        cipherLabel.setForeground(MUTED);
        keyLabel.setForeground(MUTED);

        styleInput(cipherBox);
        styleInput(keyField);

        p.add(title);
        p.add(Box.createHorizontalStrut(20));
        p.add(cipherLabel);
        p.add(cipherBox);
        p.add(keyLabel);
        p.add(keyField);
        return p;
    }

    private JComponent buildCenter() {
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 10));
        left.setOpaque(false);
        left.add(wrap(inputArea, "INPUT"));
        left.add(wrap(outputArea, "OUTPUT"));

        JPanel right = new JPanel(new GridLayout(2, 1, 0, 10));
        right.setOpaque(false);
        right.add(wrapPanel(freqPanel, "LETTER FREQUENCY (INPUT)"));
        right.add(wrap(reportArea, "ANALYSIS REPORT"));

        JPanel center = new JPanel(new GridLayout(1, 2, 12, 0));
        center.setBackground(BG);
        center.setBorder(BorderFactory.createEmptyBorder(12, 12, 6, 12));
        center.add(left);
        center.add(right);
        return center;
    }

    private JComponent buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(0, 6, 4, 6));

        JButton enc = makeButton("Encrypt", ACCENT);
        JButton dec = makeButton("Decrypt", ACCENT);
        JButton ana = makeButton("Cryptanalyze", WARN);
        JButton swap = makeButton("Output \u2192 Input", MUTED);
        JButton sample = makeButton("Load sample", MUTED);
        JButton clear = makeButton("Clear", MUTED);

        enc.addActionListener(e -> run(true));
        dec.addActionListener(e -> run(false));
        ana.addActionListener(e -> analyze());
        swap.addActionListener(e -> {
            inputArea.setText(outputArea.getText());
            outputArea.setText("");
        });
        sample.addActionListener(e -> {
            inputArea.setText(SAMPLE);
            inputArea.setCaretPosition(0);
        });
        clear.addActionListener(e -> {
            inputArea.setText("");
            outputArea.setText("");
            reportArea.setText("");
        });

        for (JButton b : new JButton[] {enc, dec, ana, swap, sample, clear}) p.add(b);
        return p;
    }

    // ---------- actions ----------

    private void run(boolean encrypt) {
        String text = inputArea.getText();
        try {
            String result;
            String desc;
            if (cipherBox.getSelectedIndex() == 0) {
                int shift = Integer.parseInt(keyField.getText().trim());
                result = encrypt ? CaesarCipher.encrypt(text, shift) : CaesarCipher.decrypt(text, shift);
                desc = "Caesar, shift = " + Math.floorMod(shift, 26);
            } else {
                String key = keyField.getText();
                result = encrypt ? VigenereCipher.encrypt(text, key) : VigenereCipher.decrypt(text, key);
                desc = "Vigen\u00e8re, key = " + VigenereCipher.cleanKey(key);
            }
            outputArea.setText(result);
            outputArea.setCaretPosition(0);
            reportArea.setText((encrypt ? "Encrypted" : "Decrypted") + " with " + desc + "\n"
                + "Letters processed: " + Cryptanalysis.lettersOnly(text).length());
        } catch (NumberFormatException ex) {
            showError("Caesar shift must be a whole number.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void analyze() {
        String text = inputArea.getText();
        int letterCount = Cryptanalysis.lettersOnly(text).length();
        if (letterCount < 10) {
            showError("Enter at least 10 letters of ciphertext in the input box.");
            return;
        }
        StringBuilder rpt = new StringBuilder();

        if (cipherBox.getSelectedIndex() == 0) {
            List<Cryptanalysis.CaesarResult> results = Cryptanalysis.caesarBruteForce(text);
            rpt.append("CAESAR BRUTE FORCE (best match first)\n");
            rpt.append(String.format("%-5s %-9s %s%n", "Rank", "Shift", "Chi-sq / preview"));
            int rank = 1;
            for (Cryptanalysis.CaesarResult r : results) {
                rpt.append(String.format("%-5d %-9d %8.1f  %s%n", rank++, r.shift, r.score, preview(r.plaintext)));
            }
            Cryptanalysis.CaesarResult best = results.get(0);
            rpt.append("\nMost likely shift: ").append(best.shift);
            outputArea.setText(best.plaintext);
            keyField.setText(String.valueOf(best.shift));
        } else {
            Cryptanalysis.VigenereResult r = Cryptanalysis.crackVigenere(text, 12);
            rpt.append("VIGEN\u00c8RE ATTACK\n");
            rpt.append("1) Key length via average Index of Coincidence (English ~ 0.066):\n");
            for (int len = 1; len <= r.maxLen; len++) {
                rpt.append(String.format("   len %2d  IC = %.4f %s%n", len, r.icByLength[len],
                    len == r.keyLength ? " <-- chosen" : ""));
            }
            rpt.append("\n2) Each key position solved as a Caesar cipher (chi-squared).\n");
            rpt.append("\nRecovered key: ").append(r.key).append("  (length ").append(r.keyLength).append(")");
            if (letterCount < 100) {
                rpt.append("\n\nNote: under 100 letters, the statistics are unreliable. Try a longer ciphertext.");
            }
            outputArea.setText(r.plaintext);
            keyField.setText(r.key);
        }
        outputArea.setCaretPosition(0);
        reportArea.setText(rpt.toString());
        reportArea.setCaretPosition(0);
    }

    private static String preview(String s) {
        String flat = s.replaceAll("\\s+", " ");
        return flat.length() > 34 ? flat.substring(0, 34) + "..." : flat;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Input problem", JOptionPane.WARNING_MESSAGE);
    }

    // ---------- styling helpers ----------

    private static JTextArea makeArea(boolean editable) {
        JTextArea a = new JTextArea();
        a.setEditable(editable);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBackground(FIELD);
        a.setForeground(FG);
        a.setCaretColor(ACCENT);
        a.setSelectionColor(new Color(0x1F5F52));
        a.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        a.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return a;
    }

    private static void styleInput(JComponent c) {
        c.setBackground(FIELD);
        c.setForeground(FG);
        c.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        if (c instanceof JTextField) ((JTextField) c).setCaretColor(ACCENT);
    }

    private static JButton makeButton(String text, Color color) {
        JButton b = new JButton(text);
        b.setBackground(FIELD);
        b.setForeground(color);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 1),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static JComponent wrap(JTextArea area, String title) {
        JScrollPane sp = new JScrollPane(area);
        sp.getViewport().setBackground(FIELD);
        sp.setBorder(titled(title));
        sp.setBackground(BG);
        return sp;
    }

    private static JComponent wrapPanel(JComponent c, String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(FIELD);
        p.setBorder(titled(title));
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private static javax.swing.border.Border titled(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0x2A2A32)), " " + title + " ",
            javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
            new Font(Font.MONOSPACED, Font.BOLD, 11), MUTED);
    }

    // ---------- frequency chart ----------

    static class FrequencyPanel extends JPanel {
        private double[] pct = new double[26];

        FrequencyPanel() {
            setBackground(FIELD);
        }

        void setText(String text) {
            pct = Cryptanalysis.letterPercent(text);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int left = 10, right = 10, top = 24, bottom = 20;
            int chartH = h - top - bottom;
            double slot = (w - left - right) / 26.0;

            double max = 13;
            for (double v : pct) max = Math.max(max, v);

            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
            g.setColor(ACCENT);
            g.fillRect(left, 8, 8, 8);
            g.setColor(MUTED);
            g.drawString("input", left + 12, 16);
            g.setColor(WARN);
            g.fillRect(left + 60, 11, 10, 2);
            g.setColor(MUTED);
            g.drawString("English", left + 74, 16);

            for (int i = 0; i < 26; i++) {
                int x = (int) (left + i * slot);
                int bw = (int) Math.max(3, slot - 3);
                int bh = (int) (pct[i] / max * chartH);
                g.setColor(ACCENT);
                g.fillRect(x, top + chartH - bh, bw, bh);

                int ey = top + chartH - (int) (Cryptanalysis.ENGLISH_FREQ[i] / max * chartH);
                g.setColor(WARN);
                g.fillRect(x - 1, ey, bw + 2, 2);

                g.setColor(MUTED);
                g.drawString(String.valueOf((char) ('A' + i)), x + Math.max(0, bw / 2 - 3), h - 6);
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new CipherSimulator().setVisible(true));
    }
}
