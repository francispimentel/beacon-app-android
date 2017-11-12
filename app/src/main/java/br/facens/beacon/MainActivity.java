package br.facens.beacon;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private MeuBeaconConsumer beaconConsumer;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, 2);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, 3);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (beaconConsumer != null) {
            beaconConsumer.pararExecucao();
        }
        mHandler.removeCallbacks(atualizacaoTela);
    }

    public void iniciarExecucao(View view) {
        View v1 = findViewById(R.id.view_configuracao);
        View v2 = findViewById(R.id.view_execucao);
        v1.setVisibility(View.GONE);
        v2.setVisibility(View.VISIBLE);
        beaconConsumer = new MeuBeaconConsumer(this);
        beaconConsumer.iniciarExecucao();
        mHandler = new Handler();
        atualizacaoTela.run();
//        consultarServidor.run();
    }

    public void atualizarListaBeacons() {
        final StringBuffer sb = new StringBuffer();

        long tempo = System.currentTimeMillis();
        List<String> beaconsRecentes = new ArrayList<>();
        Map<String, Date> beaconMap = beaconConsumer.getBeaconMap();
        for (String s : beaconMap.keySet()) {
            Date d = beaconMap.get(s);
            long diff = tempo - d.getTime();
            diff = diff / 1000;
            if (diff < 20d) {
                beaconsRecentes.add(s);
                if (diff < 1) {
                    sb.append(s).append("\nVisto por último há menos de 1 segundo.\n\n");
                } else if (diff == 1) {
                    sb.append(s).append("\nVisto por ultimo há 1 segundo.\n\n");
                } else {
                    sb.append(s).append("\nVisto por ultimo há ").append(diff).append(" segundos.\n\n");

                }
            }
        }

        runOnUiThread(new Runnable() {
            public void run() {
                TextView tv = findViewById(R.id.tv_beacons_detectados);
                if (sb.length() == 0) {
                    tv.setText("Nenhum beacon recente.");
                } else {
                    tv.setText(sb.toString());

                }
            }
        });
    }

    public void consultarInformacoesServidor(View v) {
        long tempo = System.currentTimeMillis();
        List<String> beaconsRecentes = new ArrayList<>();
        Map<String, Date> beaconMap = beaconConsumer.getBeaconMap();
        for (String s : beaconMap.keySet()) {
            Date d = beaconMap.get(s);
            long diff = tempo - d.getTime();
            diff = diff / 1000;
            if (diff < 20d) {
                beaconsRecentes.add(s);
            }
        }

        EditText et = findViewById(R.id.et_host_servidor);
        String host = et.getText().toString();
        et = findViewById(R.id.et_id_usuario);
        Long idUsuario = Long.parseLong(et.getText().toString());

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://" + host + "/api/beacon/registrarBeaconsVistos";

        JSONArray array = new JSONArray();
        try {
            for (String s : beaconsRecentes) {
                JSONObject obj = new JSONObject();
                obj.put("usuario", idUsuario);
                obj.put("beacon", s);
                array.put(obj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.POST, url, array,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        TextView mTextView = findViewById(R.id.tv_resposta_servidor);
                        mTextView.setText(response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                TextView mTextView = findViewById(R.id.tv_resposta_servidor);
                mTextView.setText("Erro na comunicação com servidor!");
            }
        });
        queue.add(request);
    }

    private Runnable atualizacaoTela = new Runnable() {
        @Override
        public void run() {
            try {
                atualizarListaBeacons();
            } finally {
                mHandler.postDelayed(atualizacaoTela, 300);
            }
        }
    };

    private Runnable consultarServidor = new Runnable() {
        @Override
        public void run() {
            try {
//                consultarInformacoesServidor();
            } finally {
                mHandler.postDelayed(consultarServidor, 10000);
            }
        }
    };
}
