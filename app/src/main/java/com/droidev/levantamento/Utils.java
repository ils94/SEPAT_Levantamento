package com.droidev.levantamento;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    public void autoScroll(ScrollView scrollView, TextView textView, String s) {

        textView.post(() -> {

            realcarTexto(textView, s);

            int index = textView.getText().toString().indexOf(s);

            int line = textView.getLayout().getLineForOffset(index);

            int y = textView.getLayout().getLineTop(line);

            scrollView.smoothScrollTo(0, y);

        });
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

    public String filtrarDigitos(String string) {

        StringBuilder sb = new StringBuilder();

        sb.append(string);

        sb.deleteCharAt(0);
        sb.deleteCharAt(0);

        return sb.toString();
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

    public void jsonDataStream(Activity activity, TextView textView, EditText editText, Uri data) {

        JSON json = new JSON();

        try {

            JSONObject jsonObject = new JSONObject(String.valueOf(json.lerJSON(activity, data)));

            String nomeArquivo = jsonObject.getString("nomeArquivo");

            editText.setText(jsonObject.getString("foraRelacao"));

            textView.setText(jsonObject.getString("relacao"));

            nomeArquivo = nomeArquivo.replace(".json", "");

            activity.setTitle(nomeArquivo);

            manterNaMemoria(activity, jsonObject.getString("anotacoes"), "anotacoes.txt");

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }

    }

    public void csvDataStream(Activity activity, TextView textView, EditText editText, Uri data) {

        try {
            InputStream inputStream = activity.getContentResolver().openInputStream(data);
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder relacao = new StringBuilder();

            textView.setText("");
            editText.setText("");

            String mLine;

            while ((mLine = r.readLine()) != null) {
                if (!mLine.equals("")) {
                    relacao.append(mLine.toUpperCase().replace(",", ": ").replace("  ", " ")).append("\n");
                }
            }

            textView.setText(relacao);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void juntarRelacoes(Activity activity, TextView textView, EditText editText, Uri data) {

        try {

            JSON json = new JSON();

            String anotacoes = recuperarDaMemoria(activity, "anotacoes.txt");

            StringBuilder novoForaDaRelacao = new StringBuilder();

            String[] novaRelacao = textView.getText().toString().split("\n");

            String[] foraDaRelacaoRecebida;

            String[] relacaoRecebida;

            JSONObject jsonObject = new JSONObject(String.valueOf(json.lerJSON(activity, data)));

            foraDaRelacaoRecebida = jsonObject.getString("foraRelacao").split("\n");

            relacaoRecebida = jsonObject.getString("relacao").split("\n");

            anotacoes = anotacoes + "\n\n" + jsonObject.getString("anotacoes");

            if (!foraDaRelacaoRecebida[0].equals("")) {

                for (String s : foraDaRelacaoRecebida) {

                    String[] patrimonio = s.split(": ");

                    if (!editText.getText().toString().contains(patrimonio[1])) {

                        novoForaDaRelacao.append(s).append("\n");
                    }
                }
            }

            novoForaDaRelacao.append(editText.getText().toString());

            for (int i = 0; i < relacaoRecebida.length; i++) {

                if (!novaRelacao[i].contains(relacaoRecebida[i])) {

                    novaRelacao[i] = novaRelacao[i].replace(novaRelacao[i], relacaoRecebida[i]);
                }
            }

            StringBuilder novaRelacaoSB = new StringBuilder();

            for (String s : novaRelacao) {

                novaRelacaoSB.append(s).append("\n");
            }

            editText.setText(novoForaDaRelacao);
            textView.setText(novaRelacaoSB);

            manterNaMemoria(activity, anotacoes, "anotacoes.txt");

        } catch (Exception e) {

            Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public String organizarDadosRelatorio(Activity activity, TextView textView, EditText editText) {

        String anotacoes = recuperarDaMemoria(activity, "anotacoes.txt");

        String[] separado = textView.getText().toString().split("\n");

        StringBuilder listaNaoAchados = new StringBuilder();
        int quantidadeForaRelacao;

        String[] listaForaRelacao = editText.getText().toString().split("\n");
        String[] listaRelacao = textView.getText().toString().split("\n");

        if (!editText.getText().toString().equals("")) {

            quantidadeForaRelacao = listaForaRelacao.length;
        } else {

            quantidadeForaRelacao = 0;
        }

        int j = 0;

        for (String s : separado) {
            if (!s.contains("[OK]")) {

                j = j + 1;

                listaNaoAchados.append(s).append("\n");
            }
        }

        String documento = dataHora()
                + "\n\nTOTAL DE BENS NA RELAÇÃO: "
                + listaRelacao.length
                + " ITENS\nTOTAL DE BENS LOCALIZADOS FISICAMENTE QUE NÃO CONSTA NA RELAÇÃO: "
                + quantidadeForaRelacao
                + " ITENS\nTOTAL DE BENS NÃO LOCALIZADOS FISICAMENTE: "
                + j + " ITENS\nSOMA TOTAL (RELAÇÃO + FISICAMENTE LOCALIZADOS QUE NÃO CONSTA NA RELAÇÃO): "
                + (listaRelacao.length + quantidadeForaRelacao)
                + " ITENS\n\nLOCALIZADOS FISICAMENTE QUE NÃO CONSTA NA RELAÇÃO: "
                + quantidadeForaRelacao + " ITENS\n\n"
                + editText.getText().toString() + "\n"
                + "BENS NÃO LOCALIZADOS FISICAMENTE: "
                + j + " ITENS\n\n" + listaNaoAchados
                + "\n" + "RELAÇÃO: "
                + listaRelacao.length
                + " ITENS\n\n" + textView.getText().toString()
                + "\nANOTAÇÕES\n\n" + anotacoes;

        return documento;
    }
}
