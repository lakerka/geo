package views.other;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class JTextFieldNumbersSupported extends JTextField {

    /**
     * 
     */
    private static final long serialVersionUID = -238520648168954418L;
    
    int integerValue;
    double doubleValue;

    public JTextFieldNumbersSupported() {
        this(null);
    }

    public JTextFieldNumbersSupported(int i) {
        this(null, i);
    }

    public JTextFieldNumbersSupported(String s) {
        this(s, 7);
    }

    public JTextFieldNumbersSupported(String s, int i) {
        super(i);
        
        setDocument(new PlainDocument() {
            /**
             * 
             */
            private static final long serialVersionUID = 501801624612252237L;

            @Override
            public void insertString(int offs, String str, AttributeSet a)
                    throws BadLocationException {
                super.insertString(offs, str.toUpperCase(), a);
            }
        });

        setText(s);
    }

    public void setText(double v) {
        if (v == Double.MAX_VALUE || v == 0) {
            doubleValue = v;
            setText(null);
        } else {
            super.setText("" + v);
        }
    }

    public void setText(int v) {
        if (v == Integer.MAX_VALUE || v == 0) {
            integerValue = v;
            setText(null);
        } else {
            super.setText("" + v);
        }
    }

    @Override
    public void setText(String t) {
        super.setText(t);
    }

    public double getDouble() {
        try {
            String str = super.getText();
            return str == null || str.length() == 0 ? doubleValue : Double
                    .parseDouble(super.getText().trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public int getInt() {
        try {
            String str = super.getText();
            return str == null || str.length() == 0 ? integerValue : Integer
                    .parseInt(super.getText().trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
