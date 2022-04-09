package com.droidev.levantamento;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class CaixaDialogo {

    private ArrayList<String> historicoBens = new ArrayList<>();
    private ArrayList<String> historicoUL = new ArrayList<>();

    public void naoEncontrado(Context context, String patrimonio, Utils.onButtonPressed onButtonPressed) {

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

        androidx.appcompat.app.AlertDialog dialogo = new androidx.appcompat.app.AlertDialog.Builder(context)
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

        ArrayAdapter<String> adapterBens = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, historicoBens);
        nome.setAdapter(adapterBens);

        ArrayAdapter<String> adapterUL = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, historicoUL);
        local.setAdapter(adapterUL);

        Button positiveButton = dialogo.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
            }
        });
    }

    public void dialogoSimples(Context context, String title, String message, String positive, String negative, Utils.onButtonPressed onButtonPressed) {

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positive, null)
                .setNegativeButton(negative, null)
                .show();

        Button positiveButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onButtonPressed.buttonPressed("true");

                dialog.dismiss();
            }
        });
    }

    public void dialogoSimplesComView(Context context, String title, String message, String hint, String positive, String negative, int inputType, Boolean adapter, Utils.onButtonPressed onButtonPressed) {

        AutoCompleteTextView autoCompleteTextView = new AutoCompleteTextView(context);
        autoCompleteTextView.setHint(hint);
        autoCompleteTextView.setInputType(inputType);

        LinearLayout lay = new LinearLayout(context);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(autoCompleteTextView);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positive, null)
                .setNegativeButton(negative, null)
                .setView(lay)
                .show();

        TinyDB tinyDB = new TinyDB(context);

        historicoBens = tinyDB.getListString("historicoBens");

        ArrayAdapter<String> adapterBens = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, historicoBens);
        autoCompleteTextView.setAdapter(adapterBens);

        Button positiveButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String string = autoCompleteTextView.getText().toString();

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
