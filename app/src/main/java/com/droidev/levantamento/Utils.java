package com.droidev.levantamento;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class Utils {

    private Boolean boo = false;

    @SuppressLint("SetTextI18n")
    public void procurarTexto(TextView tv, String textToHighlight, TextView tv2) {

        tv2.setText("Procurando...");

        final int[] count = {0};

        new Thread(() -> {

            tv.post(() -> {

                SpannableString spannableString = new SpannableString(tv.getText().toString());
                BackgroundColorSpan backgroundSpan = new BackgroundColorSpan(Color.WHITE);
                spannableString.setSpan(backgroundSpan, 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                tv.setText(spannableString);

                String tvt = tv.getText().toString();

                int ofe = tvt.indexOf(textToHighlight);
                Spannable wordToSpan = new SpannableString(tv.getText());
                for (int ofs = 0; ofs < tvt.length() && ofe != -1; ofs = ofe + 1) {
                    ofe = tvt.indexOf(textToHighlight, ofs);

                    count[0] = count[0] + 1;

                    if (ofe == -1) {
                        break;
                    } else {

                        wordToSpan.setSpan(new BackgroundColorSpan(Color.YELLOW), ofe, ofe + textToHighlight.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        tv.setText(wordToSpan, TextView.BufferType.SPANNABLE);
                    }
                }

                count[0] = count[0] - 1;
            });

            tv2.post(() -> {

                if (count[0] < 0) {

                    tv2.setText("ACHADOS: " + 0);
                } else {

                    tv2.setText("ACHADOS: " + count[0]);
                }
            });
        }).start();
    }

    public void realcarTexto(TextView tv, String textToHighlight) {

        SpannableString spannableString = new SpannableString(tv.getText().toString());
        BackgroundColorSpan backgroundSpan = new BackgroundColorSpan(Color.WHITE);
        spannableString.setSpan(backgroundSpan, 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        tv.setText(spannableString);

        String tvt = tv.getText().toString();

        int ofe = tvt.indexOf(textToHighlight);
        Spannable wordToSpan = new SpannableString(tv.getText());
        for (int ofs = 0; ofs < tvt.length() && ofe != -1; ofs = ofe + 1) {
            ofe = tvt.indexOf(textToHighlight, ofs);

            if (ofe == -1) {
                break;
            } else {

                wordToSpan.setSpan(new BackgroundColorSpan(Color.YELLOW), ofe, ofe + textToHighlight.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv.setText(wordToSpan, TextView.BufferType.SPANNABLE);
            }
        }
    }

    public void autoScroll(TextView tv, String s) {

        Layout layout = tv.getLayout();

        tv.scrollTo(0, layout.getLineTop(layout.getLineForOffset(tv.getText().toString().indexOf(s))) - (tv.getHeight() / 2));

        realcarTexto(tv, s);
    }

    public void copiarTexto(Context context, String string) {

        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copiado", string);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(context, "Copiado", Toast.LENGTH_SHORT).show();
    }

    public void campoEditavel(Context context, EditText editText) {

        if (!boo) {

            boo = true;

            editText.setFocusableInTouchMode(true);
            editText.setCursorVisible(true);

            Toast.makeText(context, "Campo ''Fora da Relação'' editável", Toast.LENGTH_SHORT).show();
        } else {

            boo = false;

            editText.setFocusableInTouchMode(false);
            editText.clearFocus();
            editText.setCursorVisible(false);

            Toast.makeText(context, "Campo ''Fora da Relação'' não editável", Toast.LENGTH_SHORT).show();

            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    @SuppressLint("SetTextI18n")
    public void contadorLinhas(EditText editText, TextView textView1, TextView textView2, TextView textView3) {

        int contadorForaDaRelacao, contadorRelacao;

        contadorForaDaRelacao = editText.getLineCount() - 1;
        contadorRelacao = textView1.getLineCount() - 1;

        textView2.setText("FORA DA RELAÇÃO - " + contadorForaDaRelacao + " ITENS");
        textView3.setText("RELAÇÃO - " + contadorRelacao + " ITENS");
    }

    public void scanner(Activity context) {

        IntentIntegrator intentIntegrator = new IntentIntegrator(context);
        intentIntegrator.setPrompt("Aponte a câmera para o código de barras ou QR code");
        intentIntegrator.setCaptureActivity(ScannerActivity.class);
        intentIntegrator.setCameraId(0);
        intentIntegrator.initiateScan();
    }

    public void salvarConfigScanner(Context context, String flashKey, String flash, String rotationKey, String rotation) {

        TinyDB tinydb = new TinyDB(context);

        tinydb.remove(flashKey);
        tinydb.remove(rotationKey);

        tinydb.putString(flashKey, flash);
        tinydb.putString(rotationKey, rotation);

    }

    public String[] carregarConfigScanner(Context context, String flashKey, String rotationKey) {

        TinyDB tinydb = new TinyDB(context);

        String tinyFlashKey = tinydb.getString(flashKey);
        String tinyRotationKey = tinydb.getString(rotationKey);

        if (tinyFlashKey.isEmpty()) {

            tinyFlashKey = "Off";
        }
        if (tinyRotationKey.isEmpty()) {

            tinyRotationKey = "Portrait";
        }

        return new String[]{tinyFlashKey, tinyRotationKey};
    }

    public void manterNaMemoria(Context context, String content, String file) {

        File path = context.getFilesDir();
        try {
            FileOutputStream writer = new FileOutputStream(new File(path, file));
            writer.write(content.getBytes());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public String recuperarDaMemoria(Context context, String filename) {
        File path = context.getFilesDir();
        File readFrom = new File(path, filename);
        byte[] content = new byte[(int) readFrom.length()];

        try {

            FileInputStream stream = new FileInputStream(readFrom);
            stream.read(content);
            return new String(content);

        } catch (Exception e) {
            return "";
        }
    }

    public String dataHora() {

        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        return "RELATÓRIO GERADO EM " + currentDate + " ÀS " + currentTime;
    }

    public void separarNaoAchados(Context context, TextView textView) {

        new Thread(() -> {

            try {

                String[] separado = textView.getText().toString().split("\n");
                ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(separado));

                for (String s : separado) {
                    if (s.contains("[OK]")) {

                        arrayList.remove(s);
                    }
                }

                Intent myIntent = new Intent(context, NaoLocalizadosActivity.class);
                myIntent.putExtra("arraylist", arrayList);
                context.startActivity(myIntent);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }).start();
    }
}
