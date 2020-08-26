package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private EditText textInput;
    private TextView textResult;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Input view
        textInput = findViewById(R.id.plainText);

        // extra
        textInput.requestFocus();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            textInput.setShowSoftInputOnFocus(false);
        }else {
            textInput.setInputType(InputType.TYPE_NULL);
        }
        textInput.setSelection(textInput.getText().length());

        // Output view
        textResult = findViewById(R.id.textResult);
        textResult.setText("=");

        // index do cursor
        index = 0;
    }

    public void write(View view) {

        String text = textInput.getText().toString();
        String start = text.substring(0, index);
        String end = text.substring(index);

        text = start + ((Button) view).getText() + end;

        textInput.setText(text);
        textInput.setSelection(++index);
    }

    public void clear(View view) {
        textInput.setText("");
        textResult.setText("=");
        index = 0;
    }

    public void incrementIndex(View view) {
        if (index < textInput.getText().length()) {
            textInput.setSelection(++index);
        }
    }

    public void decrementIndex(View view) {
        if (index > 0) {
            textInput.setSelection(--index);
        }
    }

    public void calculate(View view) {
        String expression = textInput.getText().toString().replace('x', '*');

        String result = "";

        try {
            result = "=" + (int) eval(expression);
        } catch (Exception e) {
            result = "=expressão inválida";
        } finally {
            textResult.setText(result);
        }
    }

    // O método abaixo foi retirado de: https://stackoverflow.com/questions/3422673/how-to-evaluate-a-math-expression-given-in-string-form
    public double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    switch (func) {
                        case "sqrt":
                            x = Math.sqrt(x);
                            break;
                        case "sin":
                            x = Math.sin(Math.toRadians(x));
                            break;
                        case "cos":
                            x = Math.cos(Math.toRadians(x));
                            break;
                        case "tan":
                            x = Math.tan(Math.toRadians(x));
                            break;
                        default:
                            throw new RuntimeException("Unknown function: " + func);
                    }
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }
}