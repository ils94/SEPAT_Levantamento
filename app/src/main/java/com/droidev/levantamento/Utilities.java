package com.droidev.levantamento;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.google.zxing.integration.android.IntentIntegrator;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class Utilities {

    private static final int LER_ARQUIVO = 1;
    private static final int CRIAR_JSON = 2;

    private Boolean boo = false;
    private ArrayList<String> historicoBens = new ArrayList<>();
    private ArrayList<String> historicoUL = new ArrayList<>();

    private void exportarDados(Context context, String dados, String extensao) {

        dialogoSimplesComView(context, "Enviar relatório", "Nome do arquivo:", "Exemplo: Deposito 2 / SAMS UL 580", "Enviar", "Cancelar", InputType.TYPE_CLASS_TEXT, false, new Utilities.onButtonPressed() {
            @Override
            public void buttonPressed(String i) {

                String[] informacao;

                informacao = dados.split("\n");

                StringBuilder data = new StringBuilder();

                for (String dado : informacao) {

                    data.append(dado + "\n");
                }

                enviarArquivo(context, i, data.toString(), extensao);
            }
        });
    }

    public void enviarArquivo(Context context, String file, String content, String extension) {

        try {

            FileOutputStream out = context.openFileOutput(file + extension, Context.MODE_PRIVATE);
            out.write((content.getBytes()));
            out.close();

            File fileLocation = new File(context.getFilesDir(), file + extension);
            Uri path = FileProvider.getUriForFile(context, "com.droidev.levantamento.fileprovider", fileLocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv");
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            context.startActivity(Intent.createChooser(fileIntent, "Enviar"));

        } catch (Exception e) {

            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void abrirArquivo(Activity activity) {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("text/comma-separated-values|application/json");
            String[] mimetypes = {"text/comma-separated-values", "application/json"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
            activity.startActivityForResult(Intent.createChooser(intent, "Abrir relação"), LER_ARQUIVO);
        } catch (Exception e) {

            Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public JSONObject criarJson(Context context, String nomeArquivo, String foraRelacao, String relacao) {

        JSONObject jsonObject = new JSONObject();

        try {

            jsonObject.put("nomeArquivo", nomeArquivo);
            jsonObject.put("foraRelacao", foraRelacao);
            jsonObject.put("relacao", relacao);

        } catch (Exception e) {

            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }

        return jsonObject;
    }

    public void criarESalvarJson(Activity activity, String nomeArquivo) {

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, nomeArquivo + ".json");

        activity.startActivityForResult(intent, CRIAR_JSON);
    }

    public void realcarTexto(TextView tv, String textToHighlight, TextView tv2) {

        tv2.setText("Procurando...");

        final int[] count = {0};

        new Thread(new Runnable() {
            @Override
            public void run() {

                tv.post(new Runnable() {
                    @Override
                    public void run() {

                        SpannableString spannableString = new SpannableString(tv.getText().toString());
                        BackgroundColorSpan backgroundSpan = new BackgroundColorSpan(Color.WHITE);
                        spannableString.setSpan(backgroundSpan, 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        tv.setText(spannableString);

                        String tvt = tv.getText().toString();

                        int ofe = tvt.indexOf(textToHighlight, 0);
                        Spannable wordToSpan = new SpannableString(tv.getText());
                        for (int ofs = 0; ofs < tvt.length() && ofe != -1; ofs = ofe + 1) {
                            ofe = tvt.indexOf(textToHighlight, ofs);

                            count[0] = count[0] + 1;

                            if (ofe == -1)
                                break;
                            else {

                                wordToSpan.setSpan(new BackgroundColorSpan(Color.YELLOW), ofe, ofe + textToHighlight.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                tv.setText(wordToSpan, TextView.BufferType.SPANNABLE);
                            }
                        }

                        count[0] = count[0] - 1;
                    }
                });

                tv2.post(new Runnable() {
                    @Override
                    public void run() {

                        if (count[0] < 0) {

                            tv2.setText("Achados: " + 0);
                        } else {

                            tv2.setText("Achados: " + count[0]);
                        }
                    }
                });
            }
        }).start();
    }

    public void scanner(Activity context) {

        IntentIntegrator intentIntegrator = new IntentIntegrator(context);
        intentIntegrator.setPrompt("Aponte a câmera para o código de barras ou QR code");
        intentIntegrator.setCaptureActivity(Scanner.class);
        intentIntegrator.setCameraId(0);
        intentIntegrator.initiateScan();
    }

    interface onButtonPressed {

        void buttonPressed(String i);
    }

    public void dialogoSimples(Context context, String title, String message, String positive, String negative, onButtonPressed onButtonPressed) {

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positive, null)
                .setNegativeButton(negative, null)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onButtonPressed.buttonPressed("true");

                dialog.dismiss();
            }
        });
    }

    public void dialogoSimplesComView(Context context, String title, String message, String hint, String positive, String negative, int inputType, Boolean adapter, onButtonPressed onButtonPressed) {

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

        ArrayAdapter<String> adapterBens = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, historicoBens);
        autoCompleteTextView.setAdapter(adapterBens);

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

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

        ArrayAdapter<String> adapterBens = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, historicoBens);
        nome.setAdapter(adapterBens);

        ArrayAdapter<String> adapterUL = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, historicoUL);
        local.setAdapter(adapterUL);

        Button positiveButton = dialogo.getButton(AlertDialog.BUTTON_POSITIVE);

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

    public void manterNaMemoria(Context context, String content, String file) {

        File path = context.getFilesDir();
        try {
            FileOutputStream writer = new FileOutputStream(new File(path, file));
            writer.write(content.getBytes());
            writer.close();
        } catch (Exception e) {
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

    public String pegarData() {

        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        return "RELATÓRIO GERADO EM " + currentDate + " ÀS " + currentTime;
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

    public void separarNaoAchados(Context context, TextView textView) {

        try {

            String[] separado = textView.getText().toString().split("\n");
            ArrayList arrayList = new ArrayList<>(Arrays.asList(separado));

            new Thread(new Runnable() {
                @Override
                public void run() {

                    for (String s : separado) {
                        if (s.contains("[OK]")) {

                            arrayList.remove(s);
                        }
                    }
                }
            }).start();

            Intent myIntent = new Intent(context, NaoAchadosActivity.class);
            myIntent.putExtra("key", arrayList);
            context.startActivity(myIntent);

        } catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void relatorioCompleto(Context context, TextView textView, EditText editText) {

        String[] separado = textView.getText().toString().split("\n");
        ArrayList arrayList = new ArrayList<>(Arrays.asList(separado));

        String listaNaoAchados = "";
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

                listaNaoAchados += s + "\n";
            }
        }

        String documento = pegarData() + "\n\nTOTAL DE BENS NA RELAÇÃO: " + listaRelacao.length + " ITENS\nTOTAL DE BENS LOCALIZADOS FISICAMENTE QUE NÃO CONSTA NA RELAÇÃO: " + quantidadeForaRelacao + " ITENS\nTOTAL DE BENS NÃO LOCALIZADOS FISICAMENTE: " + j + " ITENS\nSOMA TOTAL (RELAÇÃO + FISICAMENTE LOCALIZADOS QUE NÃO CONSTA NA RELAÇÃO): " + (listaRelacao.length + quantidadeForaRelacao) + " ITENS\n\nLOCALIZADOS FISICAMENTE QUE NÃO CONSTA NA RELAÇÃO: " + quantidadeForaRelacao + " ITENS\n\n" + editText.getText().toString() + "\n" + "BENS NÃO LOCALIZADOS FISICAMENTE: " + j + " ITENS\n\n" + listaNaoAchados + "\n\n" + "RELAÇÃO: " + listaRelacao.length + " ITENS\n\n" + textView.getText().toString();

        exportarDados(context, documento, ".txt");

    }

    public void relatorioForaDaRelacaoCSV(Context context, EditText editText) {

        String foraDaRelacaoReplace = editText.getText().toString().replace("-", ",").replace(":", ",");

        exportarDados(context, foraDaRelacaoReplace, ".csv");
    }

    public void connectPastebin() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                String key = "chave";
                String paste = "teste";

                try {

                    URL url = new URL("https://pastebin.com/api/api_post.php");
                    URLConnection con = url.openConnection();
                    HttpURLConnection http = (HttpURLConnection) con;
                    http.setRequestMethod("POST");
                    http.setDoOutput(true);
                    http.setDoInput(true);
                    Map<String, String> arguments = new HashMap<>();

                    arguments.put("api_dev_key", key);
                    arguments.put("api_option", "paste");
                    arguments.put("api_paste_code", paste);

                    StringJoiner sj = new StringJoiner("&");

                    for (Map.Entry<String, String> entry : arguments.entrySet())
                        sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
                                + URLEncoder.encode(entry.getValue(), "UTF-8"));

                    byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
                    int length = out.length;

                    http.setFixedLengthStreamingMode(length);
                    http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                    http.connect();

                    OutputStream os = http.getOutputStream();
                    os.write(out);

                    InputStream is = http.getInputStream();
                    String text = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));

                    System.out.println(text);

                } catch (IOException urlException) {
                    urlException.printStackTrace();
                }

            }
        }).start();

    }

}
