package com.droidev.levantamento;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class CaixaDialogo {

    private ArrayList<String> historicoBens = new ArrayList<>();
    private ArrayList<String> historicoUL = new ArrayList<>();

    interface onButtonPressed {

        void buttonPressed(String i);
    }

    public void naoEncontrado(Context context, String patrimonio, onButtonPressed onButtonPressed) {

        AutoCompleteTextView nome = new AutoCompleteTextView(context);
        nome.setHint("Nome (exemplo: mesa em L)");
        AutoCompleteTextView local = new AutoCompleteTextView(context);
        local.setHint("Código da U.L (exemplo: 000123)");

        nome.setInputType(InputType.TYPE_CLASS_TEXT);
        local.setInputType(InputType.TYPE_CLASS_NUMBER);

        LinearLayout lay = new LinearLayout(context);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(nome);
        lay.addView(local);

        AlertDialog dialogo = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle("Não encontrado")
                .setMessage(patrimonio + " não foi encontrado na relação. Digite o nome do bem, e o código da U.L onde ele foi encontrado:")
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancelar", null)
                .setView(lay)
                .show();

        TinyDB tinyDB = new TinyDB(context);

        historicoBens = tinyDB.getListString("historicoBens");
        historicoUL = tinyDB.getListString("historicoUL");

        ArrayAdapter<String> adapterBens = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, historicoBens);
        nome.setAdapter(adapterBens);

        ArrayAdapter<String> adapterUL = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, historicoUL);
        local.setAdapter(adapterUL);

        Button positiveButton = dialogo.getButton(AlertDialog.BUTTON_POSITIVE);

        positiveButton.setOnClickListener(v -> {

            if (!nome.getText().toString().equals("") && !local.getText().toString().equals("")) {

                onButtonPressed.buttonPressed(nome.getText().toString().toUpperCase() + " - " + local.getText().toString().toUpperCase() + ": " + patrimonio + "\n");

                if (!historicoBens.contains(nome.getText().toString())) {

                    tinyDB.remove("historicoBens");
                    historicoBens.add(nome.getText().toString());
                    tinyDB.putListString("historicoBens", historicoBens);
                }

                if (!historicoUL.contains(local.getText().toString())) {

                    tinyDB.remove("historicoUL");
                    historicoUL.add(local.getText().toString());
                    tinyDB.putListString("historicoUL", historicoUL);
                }

                dialogo.dismiss();
            } else {

                Toast.makeText(context, "Erro, campo vazio", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void inserirManualmente(Context context, onButtonPressed onButtonPressed) {

        EditText editText = new AutoCompleteTextView(context);
        editText.setHint("Exemplo: 012345");
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);

        LinearLayout lay = new LinearLayout(context);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(editText);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle("Modo manual")
                .setMessage("Insira o número patrimonial abaixo:")
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancelar", null)
                .setNeutralButton("N° de Série", null)
                .setView(lay)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

        positiveButton.setOnClickListener(v -> {

            String string = editText.getText().toString();

            if (string.length() < 6 && editText.getInputType() == InputType.TYPE_CLASS_NUMBER) {

                Toast.makeText(context, "Erro, o campo deve conter pelo menos 6 números", Toast.LENGTH_SHORT).show();
            } else {

                onButtonPressed.buttonPressed(string.toUpperCase());

                dialog.dismiss();

            }
        });

        neutralButton.setOnClickListener(v -> {

            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

            if (editText.getInputType() == InputType.TYPE_CLASS_NUMBER) {

                editText.setInputType(InputType.TYPE_CLASS_TEXT);

                editText.setHint("Exemplo: ABC123");

                neutralButton.setText("N° Patrimonial");

                dialog.setMessage("Insira o número de série abaixo:");

            } else {

                editText.setInputType(InputType.TYPE_CLASS_NUMBER);

                editText.setHint("Exemplo: 012345");

                neutralButton.setText("N° de Série");

                dialog.setMessage("Insira o número patrimonial abaixo:");
            }

            imm.showSoftInput(lay, InputMethodManager.SHOW_IMPLICIT);
        });
    }

    public void simples(Context context, String title, String message, String positive, String negative, onButtonPressed onButtonPressed) {

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positive, null)
                .setNegativeButton(negative, null)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton((AlertDialog.BUTTON_NEGATIVE));

        positiveButton.setOnClickListener(v -> {

            onButtonPressed.buttonPressed("true");

            dialog.dismiss();
        });

        negativeButton.setOnClickListener(v -> {

            onButtonPressed.buttonPressed("false");

            dialog.dismiss();
        });
    }

    public void simplesTresBotoes(Context context, String title, String message, String positive, String negative, String neutral, onButtonPressed onButtonPressed) {

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positive, null)
                .setNegativeButton(negative, null)
                .setNeutralButton(neutral, null)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton((AlertDialog.BUTTON_NEGATIVE));
        Button neutralButton = dialog.getButton((AlertDialog.BUTTON_NEUTRAL));

        positiveButton.setOnClickListener(v -> {

            onButtonPressed.buttonPressed("true");

            dialog.dismiss();
        });

        negativeButton.setOnClickListener(v -> {

            onButtonPressed.buttonPressed("false");

            dialog.dismiss();
        });

        neutralButton.setOnClickListener(v -> {

            onButtonPressed.buttonPressed("neutral");

            dialog.dismiss();
        });
    }

    public void simplesComView(Context context, String title, String message, String hint, String positive, String negative, int inputType, Boolean adapter, Boolean length, onButtonPressed onButtonPressed) {

        AutoCompleteTextView autoCompleteTextView = new AutoCompleteTextView(context);
        autoCompleteTextView.setHint(hint);
        autoCompleteTextView.setInputType(inputType);

        LinearLayout lay = new LinearLayout(context);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(autoCompleteTextView);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positive, null)
                .setNegativeButton(negative, null)
                .setView(lay)
                .show();

        TinyDB tinyDB = new TinyDB(context);

        historicoBens = tinyDB.getListString("historicoBens");

        ArrayAdapter<String> adapterBens = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, historicoBens);
        autoCompleteTextView.setAdapter(adapterBens);

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        positiveButton.setOnClickListener(v -> {

            String string = autoCompleteTextView.getText().toString();

            if (length && string.length() < 6) {

                Toast.makeText(context, "Erro, o campo deve conter pelo menos 6 números", Toast.LENGTH_SHORT).show();
            } else {

                if (!string.equals("")) {

                    onButtonPressed.buttonPressed(string);

                    if (adapter) {

                        if (!historicoBens.contains(autoCompleteTextView.getText().toString())) {

                            tinyDB.remove("historicoBens");
                            historicoBens.add(autoCompleteTextView.getText().toString());
                            tinyDB.putListString("historicoBens", historicoBens);
                        }
                    }

                    dialog.dismiss();

                } else {

                    Toast.makeText(context, "Erro, campo vazio", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
