package com.droidev.levantamento;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class Pastebin {

    public String gerarChave(String login, String senha, String dev_key) {

        String result = "";

        try {

            URL url = new URL("https://pastebin.com/api/api_login.php");
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setDoInput(true);
            Map<String, String> arguments = new HashMap<>();

            arguments.put("api_dev_key", dev_key);
            arguments.put("api_user_name", login);
            arguments.put("api_user_password", senha);

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

            result = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));

        } catch (IOException urlException) {
            urlException.printStackTrace();
        }

        return result;
    }

    public void gerarQRCode(Context context, Activity activity, String content) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                TinyDB tinyDB = new TinyDB(context);

                String login = tinyDB.getString("login");
                String senha = tinyDB.getString("senha");
                String dev_key = tinyDB.getString("devKey");

                if (login.isEmpty() || senha.isEmpty() || dev_key.isEmpty()) {

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(context, "Erro, salve uma conta pastebin primeiro", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {

                    try {

                        String user_key = gerarChave(login, senha, dev_key);

                        URL url = new URL("https://pastebin.com/api/api_post.php");
                        URLConnection con = url.openConnection();
                        HttpURLConnection http = (HttpURLConnection) con;
                        http.setRequestMethod("POST");
                        http.setDoOutput(true);
                        http.setDoInput(true);
                        Map<String, String> arguments = new HashMap<>();

                        arguments.put("api_dev_key", dev_key);
                        arguments.put("api_user_key", user_key);
                        arguments.put("api_option", "paste");
                        arguments.put("api_paste_code", content);
                        arguments.put("api_paste_expire_date", "10M");

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

                        String result = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));

                        Intent myIntent = new Intent(context, QRCodeActivity.class);
                        myIntent.putExtra("content", result);
                        context.startActivity(myIntent);

                    } catch (IOException urlException) {
                        urlException.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void salvarPastebinLogin(Context context) {

        EditText login = new EditText(context);
        login.setHint("Seu login");
        login.setInputType(InputType.TYPE_CLASS_TEXT);

        EditText senha = new EditText(context);
        senha.setHint("Sua senha");
        senha.setInputType(InputType.TYPE_CLASS_TEXT);

        EditText devKey = new EditText(context);
        devKey.setHint("Sua chave dev api");
        devKey.setInputType(InputType.TYPE_CLASS_TEXT);

        LinearLayout lay = new LinearLayout(context);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(login);
        lay.addView(senha);
        lay.addView(devKey);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle("Salvar Conta")
                .setMessage("Insira seu login, senha e api_dev_key para salvar.")
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancelar", null)
                .setView(lay)
                .show();

        TinyDB tinyDB = new TinyDB(context);

        login.setText(tinyDB.getString("login"));
        senha.setText(tinyDB.getString("senha"));
        devKey.setText(tinyDB.getString("devKey"));

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String loginString = login.getText().toString();
                String senhaString = senha.getText().toString();
                String devKeyString = devKey.getText().toString();

                if (!loginString.equals("") && !senhaString.equals("") && !devKeyString.equals("")) {

                    tinyDB.remove("login");
                    tinyDB.remove("senha");
                    tinyDB.remove("devKey");

                    tinyDB.putString("login", loginString);
                    tinyDB.putString("senha", senhaString);
                    tinyDB.putString("devKey", devKeyString);

                    dialog.dismiss();

                } else {

                    Toast.makeText(context, "Erro, campo vazio", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
