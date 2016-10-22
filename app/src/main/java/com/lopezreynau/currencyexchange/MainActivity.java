package com.lopezreynau.currencyexchange;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static final String DATA_API_URL = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.xchange%20where%20pair%20in%20%28%22USDEUR%22,%20%22USDJPY%22,%20%22USDBGN%22,%20%22USDCZK%22,%20%22USDDKK%22,%20%22USDGBP%22,%20%22USDHUF%22,%20%22USDLTL%22,%20%22USDLVL%22,%20%22USDPLN%22,%20%22USDRON%22,%20%22USDSEK%22,%20%22USDCHF%22,%20%22USDNOK%22,%20%22USDHRK%22,%20%22USDRUB%22,%20%22USDTRY%22,%20%22USDAUD%22,%20%22USDBRL%22,%20%22USDCAD%22,%20%22USDCNY%22,%20%22USDHKD%22,%20%22USDIDR%22,%20%22USDILS%22,%20%22USDINR%22,%20%22USDKRW%22,%20%22USDMXN%22,%20%22USDMYR%22,%20%22USDNZD%22,%20%22USDPHP%22,%20%22USDSGD%22,%20%22USDTHB%22,%20%22USDZAR%22,%20%22USDISK%22%29&format=json&&env=store://datatables.org/alltableswithkeys";
    static final String CHART_API_URL = "http://chart.finance.yahoo.com/z?s=";

    private ArrayList<CurrencyElement> currencyElements;

    private Context context;

    private boolean reloadingData;
    private boolean reloadingChart;
    private boolean modifyingTextEdit;

    private RelativeLayout reloadDataLayout;
    private RelativeLayout selectCurrencyLayout;
    private RelativeLayout loadingDataLayout;
    private Spinner firstSpinner;
    private Spinner secondSpinner;
    private EditText firstEditText;
    private EditText secondEditText;
    private ImageButton swapCurrencyButton;
    private Button reloadButton;
    private ImageView chartImageView;
    private TextView loadText;
    private TextView updateText;
    private int firstPosition;
    private int secondPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        reloadDataLayout = (RelativeLayout) findViewById(R.id.reloadDataLayout);
        loadingDataLayout = (RelativeLayout) findViewById(R.id.loadingDataLayout);
        selectCurrencyLayout = (RelativeLayout) findViewById(R.id.selectCurrencyLayout);
        firstSpinner = (Spinner) findViewById(R.id.firstCurrencySpinner);
        secondSpinner = (Spinner) findViewById(R.id.secondCurrencySpinner);
        firstEditText = (EditText) findViewById(R.id.valueFirstCurrency);
        secondEditText = (EditText) findViewById(R.id.valueSecondCurrency);
        swapCurrencyButton = (ImageButton) findViewById(R.id.swapCurrencyButton);
        reloadButton = (Button) findViewById(R.id.reloadButton);
        chartImageView = (ImageView) findViewById(R.id.chartImageView);
        loadText = (TextView) findViewById(R.id.loadText);
        updateText = (TextView) findViewById(R.id.updateText);

        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadData();
            }
        });

        swapCurrencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int aux = firstPosition;
                firstPosition = secondPosition;
                secondPosition = aux;
                firstSpinner.setSelection(firstPosition);
                secondSpinner.setSelection(secondPosition);
                refreshRightValue();
                new ObtenerGrafoAPI().execute();
            }
        });

        firstSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                firstPosition = position;
                refreshRightValue();
                new ObtenerGrafoAPI().execute();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        secondSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                secondPosition = position;
                refreshRightValue();
                new ObtenerGrafoAPI().execute();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        firstEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(modifyingTextEdit)
                    return;
                refreshRightValue();
            }

        });

        secondEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(modifyingTextEdit)
                    return;
                refreshLeftValue();
            }

        });

        currencyElements = new ArrayList<>();
        currencyElements.add(new CurrencyElement("USD", 1.0f, 1.0f, 1.0f));
        new ObtenerDatosAPI().execute();

        firstPosition = 0;
        secondPosition = 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.action_refresh:
                reloadData();
                break;
            case R.id.action_about:
                Intent about = new Intent(this, About.class);
                startActivity(about);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshRightValue() {
        try {
            float firstValue = Float.valueOf(firstEditText.getText().toString());
            float firstCurrencyRate = currencyElements.get(firstPosition).getRate();
            float secondCurrencyRate = currencyElements.get(secondPosition).getRate();
            float result = firstValue * (1 / firstCurrencyRate) * secondCurrencyRate;
            modifyingTextEdit = true;
            secondEditText.setText("" + result);
            modifyingTextEdit = false;
        }
        catch (NumberFormatException e) {
            firstEditText.setText("0");
            refreshRightValue();
        }
    }

    private void refreshLeftValue() {
        try {
            float secondValue = Float.valueOf(secondEditText.getText().toString());
            float secondCurrencyRate = currencyElements.get(secondPosition).getRate();
            float firstCurrencyRate = currencyElements.get(firstPosition).getRate();
            float result = secondValue * (1 / secondCurrencyRate) * firstCurrencyRate;
            modifyingTextEdit = true;
            firstEditText.setText("" + result);
            modifyingTextEdit = false;
        }
        catch (NumberFormatException e) {
            firstEditText.setText("0");
            refreshLeftValue();
        }
    }

    private void reloadData() {
        if(!reloadingData) {
            reloadingData = true;
            currencyElements.clear();
            currencyElements.add(new CurrencyElement("USD", 1.0f, 1.0f, 1.0f));
            new ObtenerDatosAPI().execute();
        }
    }

    private class ObtenerDatosAPI extends AsyncTask<Void, Void, String> {

        protected void onPreExecute() {
            reloadDataLayout.setVisibility(View.GONE);
            selectCurrencyLayout.setVisibility(View.GONE);
            chartImageView.setVisibility(View.GONE);
            loadText.setText(getResources().getString(R.string.loading_data_msg));
            loadingDataLayout.setVisibility(View.VISIBLE);
        }

        protected String doInBackground(Void... urls) {
            try {
                URL url = new URL(DATA_API_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                }
                finally{
                    urlConnection.disconnect();
                }
            } catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if(response == null) {
                loadText.setText(getResources().getString(R.string.loading_data_msg));
                reloadDataLayout.setVisibility(View.VISIBLE);
                loadingDataLayout.setVisibility(View.GONE);
            }
            else {
                Log.i("INFO", response);
                getDataFromJSON(response);
                updateUI();
                selectCurrencyLayout.setVisibility(View.VISIBLE);
                if(!reloadingChart)
                    new ObtenerGrafoAPI().execute();
            }
            reloadingData = false;
            Log.i("Elements loaded", currencyElements.size() + "");
        }

        private void getDataFromJSON(String response) {
            try {
                JSONObject object = (new JSONObject(response)).getJSONObject("query");
                object = object.getJSONObject("results");
                JSONArray elements = object.getJSONArray("rate");
                for(int i=0; i < elements.length(); i++){
                    JSONObject jsonObject = elements.getJSONObject(i);
                    String id = jsonObject.optString("id").toString();
                    float rate = Float.parseFloat(jsonObject.optString("Rate").toString());
                    float ask = Float.parseFloat(jsonObject.optString("Ask").toString());
                    float bid = Float.parseFloat(jsonObject.optString("Bid").toString());

                    currencyElements.add(new CurrencyElement(id.substring(3), rate, ask, bid));
                }
                Collections.sort(currencyElements, new Comparator<CurrencyElement>() {
                    @Override
                    public int compare(CurrencyElement c1, CurrencyElement c2) {
                        return c1.getId().compareToIgnoreCase(c2.getId());
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void updateUI() {
            List<String> list = new ArrayList<>();
            for(CurrencyElement ce : currencyElements)
                list.add(ce.getId());
            // android.R.layout.simple_spinner_item
            ArrayAdapter<String> spinnersDataAdapter = new ArrayAdapter<>(context,
                    android.R.layout.simple_spinner_item, list);
            spinnersDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            firstSpinner.setAdapter(spinnersDataAdapter);
            firstSpinner.setSelection(firstPosition);
            secondSpinner.setAdapter(spinnersDataAdapter);
            secondSpinner.setSelection(secondPosition);

            Time time = new Time(Time.getCurrentTimezone());
            time.setToNow();
            String dataInfo = "Last update on " + time.monthDay + "/" + time.month + 1 + "/" + time.year
                    + " at " + time.hour + ":" + time.minute + ":" +time.second;
            updateText.setText(dataInfo);
        }
    }

    private class ObtenerGrafoAPI extends AsyncTask<Void, Void, Bitmap> {

        protected void onPreExecute() {
            reloadingChart = true;
            loadText.setText(getResources().getString(R.string.loading_chart_msg));
            chartImageView.setVisibility(View.GONE);
            loadingDataLayout.setVisibility(View.VISIBLE);
        }

        protected Bitmap doInBackground(Void... urls) {
            try {
                String firstCurrencyId = currencyElements.get(firstPosition).getId();
                String secondCurrencyId = currencyElements.get(secondPosition).getId();

                URL url;
                if(firstCurrencyId == "USD") url = new URL(CHART_API_URL + secondCurrencyId + "=x");
                else if(secondCurrencyId == "USD") url = new URL(CHART_API_URL + firstCurrencyId + "=x");
                else url = new URL(CHART_API_URL + firstCurrencyId + secondCurrencyId + "=x");

                Log.i("CHART URL", url.toString());

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    urlConnection.setDoInput(true);
                    urlConnection.connect();
                    InputStream input = urlConnection.getInputStream();
                    Bitmap chart = BitmapFactory.decodeStream(input);
                    return chart;
                }
                finally{
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(Bitmap response) {
            if(response == null) {
                selectCurrencyLayout.setVisibility(View.GONE);
                loadingDataLayout.setVisibility(View.GONE);
                reloadDataLayout.setVisibility(View.VISIBLE);
            }
            else {
                loadingDataLayout.setVisibility(View.GONE);
                chartImageView.setVisibility(View.VISIBLE);
                Log.i("INFO", response.toString());

                Matrix matrix = new Matrix();
                matrix.postScale(2f, 2f);
                Bitmap chart = Bitmap.createBitmap(response, 0, 0, response.getWidth(), response.getHeight(), matrix, false);
                response.recycle();

                chartImageView.setImageBitmap(chart);
            }
            reloadingChart = false;
            Log.i("Elements loaded", currencyElements.size()+"");
        }

    }
}