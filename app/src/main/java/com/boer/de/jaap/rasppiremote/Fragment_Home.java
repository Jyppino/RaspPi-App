package com.boer.de.jaap.rasppiremote;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import io.socket.client.Socket;

import static android.content.Context.MODE_PRIVATE;

public class Fragment_Home extends Fragment {

    private Spinner spinnerCategory, spinnerSeason, spinnerEpisode;
    private EditText searchTerm, customURL;
    private ImageButton searchBtn, addCustomBtn, clearAll;
    public Socket mSocket = null;
    private Communicator comms;
    private SharedPreferences prefs;
    private GridView recentView;
    public RecentListAdapter recentListAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        spinnerCategory = (Spinner) view.findViewById(R.id.spinnercategory);
        spinnerSeason = (Spinner) view.findViewById(R.id.spinnerseason);
        spinnerEpisode = (Spinner) view.findViewById(R.id.spinnerepisode);
        searchTerm = (EditText) view.findViewById(R.id.searchinput);
        customURL = (EditText) view.findViewById(R.id.customURL);
        searchBtn = (ImageButton) view.findViewById(R.id.searchbutton);
        addCustomBtn = (ImageButton) view.findViewById(R.id.addCustomButton);
        comms = ((MainActivity)getActivity()).comms;
        mSocket = ((MainActivity)getActivity()).mSocket;
        clearAll = (ImageButton) view.findViewById(R.id.clearSearchInput);
        recentView = (GridView) view.findViewById(R.id.recentView);
        recentView.setEmptyView(view.findViewById(R.id.emptyElement));
        ArrayAdapter<CharSequence> adaptercategory = ArrayAdapter.createFromResource(getContext(), R.array.categories_array, R.layout.spinner_item);
        adaptercategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adaptercategory);

        ArrayAdapter<CharSequence> adapterseason = ArrayAdapter.createFromResource(getContext(), R.array.integer_array, R.layout.spinner_item);
        adapterseason.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSeason.setAdapter(adapterseason);

        ArrayAdapter<CharSequence> adapterepisode = ArrayAdapter.createFromResource(getContext(), R.array.integer_array, R.layout.spinner_item);
        adapterepisode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEpisode.setAdapter(adapterepisode);

        prefs = getActivity().getPreferences(MODE_PRIVATE);

        recentListAdapter = new RecentListAdapter(getContext());
        recentView.setAdapter(recentListAdapter);
        readRecentData(prefs.getString("recents", "[]"));
        recentListAdapter.notifyDataSetChanged();

        recentView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RecentItem currentItem = (RecentItem) recentListAdapter.getItem(position);
                searchTerm.setText(currentItem.getSearchTerm());
                spinnerSeason.setSelection(currentItem.getSeason());
                spinnerEpisode.setSelection(currentItem.getEpisode());
            }
        });

        recentView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                recentListAdapter.removeItem(position);
                recentListAdapter.notifyDataSetChanged();
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("recents", recentListAdapter.getJSSON().toString());
                editor.apply();
                return true;
            }
        });

        addCustomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (customURL.getText().length() < 50) {
                    Toast.makeText(getContext(), "Magnet link to short", Toast.LENGTH_SHORT).show();
                } else {
                    InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(searchTerm.getWindowToken(),0);

                    if (comms.isActive()) {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        ((MainActivity)getActivity()).findViewById(R.id.stop).performClick();
                                        ((MainActivity)getActivity()).torObject = new MainActivity.ResultHolder("Custom Torrent", customURL.getText().toString());
                                        comms.updateTorrentTitle("Custom Torrent");
                                        comms.updateImage("-","-");
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked -> do nothing
                                        break;
                                }
                            }
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("Stop and replace current playback?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();
                    } else {
                        ((MainActivity)getActivity()).torObject = new MainActivity.ResultHolder("Custom Torrent", customURL.getText().toString());
                        comms.updateTorrentTitle("Custom Torrent");
                        comms.updateImage("-","-");
                    }
                }
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (comms.isConnected()) {
                    if (searchTerm.getText().length() > 0) {
                        InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        in.hideSoftInputFromWindow(searchTerm.getWindowToken(),0);

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("search", searchTerm.getText().toString());
                        editor.putInt("season", spinnerSeason.getSelectedItemPosition());
                        editor.putInt("episode", spinnerEpisode.getSelectedItemPosition());
                        editor.apply();

                        comms.setLoading(true);
                        String searchText = searchTerm.getText().toString() + " ";
                        if (spinnerSeason.getSelectedItemPosition() != 0) {
                            searchText += "S" + spinnerSeason.getSelectedItem().toString();
                        }

                        if (spinnerEpisode.getSelectedItemPosition() != 0) {
                            searchText += "E" + spinnerEpisode.getSelectedItem().toString();
                        }

                        if (spinnerCategory.getSelectedItemPosition() == 0) {
                            mSocket.emit("searchTorrent", searchText);
                        } else {
                            mSocket.emit("searchSubtitle", searchText);
                        }
                    } else {
                        Toast.makeText(getContext(), "Empty search query", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "No connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTerm.setText("");
                searchTerm.requestFocus();
                spinnerSeason.setSelection(0);
                spinnerEpisode.setSelection(0);
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });

        searchTerm.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    searchBtn.performClick();
                    return true;
                }
                return false;
            }
        });

        view.findViewById(R.id.torrent_1337x).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://1337x.to/"));
                startActivity(browserIntent);
            }
        });
        view.findViewById(R.id.opensubtitles).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.opensubtitles.org/"));
                startActivity(browserIntent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSocket = ((MainActivity)getActivity()).mSocket;
    }

    private void readRecentData(String recentData) {
        try {
            JSONArray recentArray = new JSONArray(recentData);

            for (int i = 0 ; i < recentArray.length() ; i++) {
                int position = recentArray.length() - 1 - i;
                JSONObject obj = recentArray.getJSONObject(position);
                RecentItem ri = new RecentItem(obj.getString("searchTerm"), obj.getString("imageURL"), obj.getInt("season"), obj.getInt("episode"));
                recentListAdapter.addItem(ri);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
