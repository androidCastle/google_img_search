package com.imagegrid.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.anupcowkur.reservoir.Reservoir;
import com.anupcowkur.reservoir.ReservoirGetCallback;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.imagegrid.R;
import com.imagegrid.adapters.GridImageAdapter;
import com.imagegrid.network.SearchAsyncHttp;
import com.imagegrid.pojos.SearchResultPOJO;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private GridImageAdapter mGridImageAdapter;
    private EditText edTxtSearchable;
    private GridView mGridViewImages;
    private int numColumns = 2;
    private boolean isLoading = false;
    private int currentPage = 1;
    private SharedPreferences mSharedPreferences;
    private static final boolean cacheMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (cacheMode) {
            initReservoirCache();
        } else {
            mSharedPreferences = getSharedPreferences("shared_preferences", MODE_PRIVATE);
        }
        initViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_two:
                MainActivity.this.numColumns = 2;
                updateGridViewColumns();
                return true;
            case R.id.action_three:
                MainActivity.this.numColumns = 3;
                updateGridViewColumns();
                return true;
            case R.id.action_four:
                MainActivity.this.numColumns = 4;
                updateGridViewColumns();
                return true;
            case R.id.action_five:
                MainActivity.this.numColumns = 5;
                updateGridViewColumns();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        edTxtSearchable = findViewById(R.id.edTxtSearchable);
        ImageView imgVwSearch = findViewById(R.id.imgVwSearch);
        mGridViewImages = findViewById(R.id.grid_view);
        imgVwSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String keywords = edTxtSearchable.getText().toString().trim();
                if (keywords.length() > 0) {
                    hideDeviceKeyboard(view);
                    searchImage(currentPage);
                }
            }
        });
        mGridViewImages.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                    if (!isLoading) {
                        isLoading = true;
                        currentPage++;
                        searchImage((currentPage * 10) - 9);
                    }
                }
            }
        });

        mGridViewImages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                SearchResultPOJO searchResultPOJO = (SearchResultPOJO) adapterView.getItemAtPosition(position);
                if (searchResultPOJO != null) {
                    Intent intent = new Intent(MainActivity.this, DisplayImageActivity.class);
                    intent.putExtra("IMAGE_URL", searchResultPOJO.getFullImageLink());
                    startActivity(intent);
                }
            }
        });

        ArrayList<SearchResultPOJO> imageSearchResults = new ArrayList<>();
        mGridImageAdapter = new GridImageAdapter(this, imageSearchResults, numColumns);
        mGridViewImages.setAdapter(mGridImageAdapter);
        updateGridViewColumns();
    }

    private void updateGridViewColumns() {
        mGridViewImages.setNumColumns(MainActivity.this.numColumns);
        mGridImageAdapter.updateColumns(MainActivity.this.numColumns);
        mGridImageAdapter.notifyDataSetChanged();
    }

    public void searchImage(int startIndex) {
        final String inputTxt = edTxtSearchable.getText().toString();
        if (cacheMode) {
            boolean exists = checkIfDataExistsInReservoir(inputTxt);
            if (exists) {
                getDataFromReservoir(inputTxt, startIndex);
            } else {
                searchUserInput(startIndex);
            }
        } else {
            if (mSharedPreferences.contains(inputTxt)) {
                retrieveDataFromPrefs(inputTxt, startIndex);
            } else {
                searchUserInput(startIndex);
            }
        }
    }

    private void searchUserInput(final int startIndex) {
        final String inputTxt = edTxtSearchable.getText().toString();
        if (checkIsNetworkAvailable()) {
            SearchAsyncHttp client = new SearchAsyncHttp();
            if (startIndex == 1) mGridImageAdapter.clear();
            if (!inputTxt.equals("")) {
                client.startSearchAsync(inputTxt, startIndex, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    if (response != null) {
                                        Gson gson = new Gson();
                                        Type listType = new TypeToken<List<SearchResultPOJO>>() {
                                        }.getType();
                                        ArrayList<SearchResultPOJO> searchResultPOJOS = gson.fromJson(response.getJSONArray("items").toString(), listType);
                                        if (searchResultPOJOS != null) {
                                            if (cacheMode) {
                                                boolean result = checkIfDataExistsInReservoir(inputTxt);
                                                if (!result) clearGridAdapter();
                                                updateSearchResultViews(searchResultPOJOS);
                                                checkAndSaveDataInReservoir(inputTxt, searchResultPOJOS);
                                                isLoading = false;
                                            } else {
                                                if (!checkIfDataExistsInPrefs(inputTxt)) clearGridAdapter();
                                                updateSearchResultViews(searchResultPOJOS);
                                                checkAndSaveDataInPrefs(inputTxt, searchResultPOJOS);
                                                isLoading = false;
                                            }
                                        }
                                    }
                                } catch (JSONException ex) {
                                    caughtException(ex);
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                super.onFailure(statusCode, headers, responseString, throwable);
                                Toast.makeText(getApplicationContext(), R.string.str_txt_failed_to_retrieve_data, Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            } else {
                Toast.makeText(MainActivity.this, R.string.str_txt_no_input, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            clearGridAdapter();
        }
    }

    private void caughtException(Exception ex) {
        ex.printStackTrace();
    }

    private void clearGridAdapter() {
        mGridImageAdapter.clear();
        mGridImageAdapter.notifyDataSetChanged();
    }

    private void updateSearchResultViews(final ArrayList<SearchResultPOJO> searchResultPOJOS) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGridImageAdapter.addAll(searchResultPOJOS);
            }
        });
    }

    private void refreshGridViews(final ArrayList<SearchResultPOJO> searchResultPOJOS) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGridImageAdapter = new GridImageAdapter(MainActivity.this, searchResultPOJOS, numColumns);
                mGridViewImages.setAdapter(mGridImageAdapter);
            }
        });
    }

    public Boolean checkIsNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static void hideDeviceKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void printLogs(String msg) {
        Log.d("_street", msg);
    }


    private void checkAndSaveDataInPrefs(String key, ArrayList<SearchResultPOJO> searchResultPOJOS) {
        if (checkIfDataExistsInPrefs(key)) {
            ArrayList<SearchResultPOJO> savedList = getSearchedListFromPrefs(key);
            if (savedList != null) {
                savedList.addAll(searchResultPOJOS);
                saveDataIntoPrefs(key, savedList);
            } else {
                saveDataIntoPrefs(key, searchResultPOJOS);
            }
        } else {
            saveDataIntoPrefs(key, searchResultPOJOS);
        }
    }

    private void saveDataIntoPrefs(String key, ArrayList<SearchResultPOJO> searchResultPOJOS) {
        try {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            Gson gson = new Gson();
            String jsonText = gson.toJson(searchResultPOJOS);
            editor.putString(key, jsonText);
            editor.apply();
        } catch (Exception ex) {
            printLogs(ex.getMessage());
        }
    }

    private void retrieveDataFromPrefs(String key, int startIndex) {
        try {
            Gson gson = new Gson();
            String jsonText = mSharedPreferences.getString(key, null);
            ArrayList<SearchResultPOJO> searchResultPOJOList = gson.fromJson(jsonText, new TypeToken<List<SearchResultPOJO>>() {
            }.getType());
            if (searchResultPOJOList != null) {
                if (!checkIsNetworkAvailable()) {
                    refreshGridViews(searchResultPOJOList);
                } else {
                    searchUserInput(startIndex);
                }
            }
        } catch (Exception ex) {
            printLogs(ex.getMessage());
        }
    }

    private boolean checkIfDataExistsInPrefs(String key) {
        return mSharedPreferences.contains(key);
    }

    private ArrayList<SearchResultPOJO> getSearchedListFromPrefs(String key) {
        try {
            Gson gson = new Gson();
            String prefStr = mSharedPreferences.getString(key, null);
            ArrayList<SearchResultPOJO> searchResultPOJOList = gson.fromJson(prefStr, new TypeToken<List<SearchResultPOJO>>() {
            }.getType());
            if (searchResultPOJOList != null) {
                return searchResultPOJOList;
            }
        } catch (Exception ex) {
            printLogs(ex.getMessage());
        }
        return null;
    }

    private void initReservoirCache() {
        try {
            Reservoir.init(MainActivity.this, (20 * 1024));
        } catch (IOException ex) {
            printLogs("Exception: initReservoirCache " + ex.getMessage());
        }
    }

    private void checkAndSaveDataInReservoir(String key, ArrayList<SearchResultPOJO> searchResultPOJOS) {
        boolean exist = checkIfDataExistsInReservoir(key);
        if (exist) {
            Type resultType = new TypeToken<ArrayList<SearchResultPOJO>>() {
            }.getType();
            try {
                ArrayList<SearchResultPOJO> savedArrayList = Reservoir.get(key, resultType);
                if (savedArrayList != null) {
                    savedArrayList.addAll(searchResultPOJOS);
                    putDataIntoReservoir(key, savedArrayList);
                } else putDataIntoReservoir(key, searchResultPOJOS);
            } catch (IOException e) {
                printLogs("Exception: checkAndSaveDataInReservoir " + e.getMessage());
            }
        } else putDataIntoReservoir(key, searchResultPOJOS);

    }

    private void putDataIntoReservoir(String key, ArrayList<SearchResultPOJO> arrayList) {
        try {
            Reservoir.put(key, arrayList);
        } catch (IOException e) {
            printLogs("Exception: putDataIntoReservoir " + e.getMessage());
        }
    }

    private void getDataFromReservoir(String key, int startIndex) {
        Type resultType = new TypeToken<ArrayList<SearchResultPOJO>>() {
        }.getType();
        try {
            ArrayList<SearchResultPOJO> savedArrayList = Reservoir.get(key, resultType);
            if (savedArrayList != null) {
                if (!checkIsNetworkAvailable()) {
                    refreshGridViews(savedArrayList);
                } else {
                    searchUserInput(startIndex);
                }
            }
        } catch (IOException e) {
            printLogs("Exception: getDataFromReservoir " + e.getMessage());
        }
    }

    private boolean checkIfDataExistsInReservoir(String key) {
        try {
            return Reservoir.contains(key);
        } catch (IOException e) {
            printLogs("Exception: checkIfDataExistsInReservoir " + e.getMessage());
        }
        return false;
    }
}