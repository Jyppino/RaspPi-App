package com.boer.de.jaap.rasppiremote;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.client.Socket;


public class MainActivity extends AppCompatActivity {

    private SectionsStatePagerAdapter mSectionsStatePagerAdapter;
    private ViewPager mViewPager;
    private SharedPreferences prefs;
    public Communicator comms;
    public Socket mSocket = null;
    public ArrayList<Result> results = new ArrayList<Result>();
    public ResultHolder subObject = null;
    public ResultHolder torObject = null;
    private SlidingUpPanelLayout panel;
    public String selectedTitle = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create custom actionbar
        LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbarView = inflator.inflate(R.layout.actionbar, null);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled (false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(actionbarView);

        mSectionsStatePagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

        prefs = getPreferences(MODE_PRIVATE);
        String host = prefs.getString("host",getString(R.string.default_host));
        String port = prefs.getString("port",getString(R.string.default_port));

        comms = new Communicator(this.findViewById(android.R.id.content), actionbarView, host, port);
        panel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        actionbarView.findViewById(R.id.statusview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "DISCONNECTED";
                switch (comms.getState()) {
                    case -1:
                        message = "DISCONNECTED";
                        break;
                    case 0:
                        message = "IDLE";
                        break;
                    case 1:
                        message = "PAUSED";
                        break;
                    case 2:
                        message = "PLAYING";
                        break;
                }
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        panel.setOverlayed(true);
        panel.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    ((ImageButton)findViewById(R.id.togglePanel)).setImageResource(R.drawable.ic_slide_down);
                }
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    ((ImageButton)findViewById(R.id.togglePanel)).setImageResource(R.drawable.ic_slide_up);
                }
            }
        });

        //SET BUTTON ONCLICK LISTENERS
        findViewById(R.id.togglePanel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                panel.setPanelState((panel.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) ? SlidingUpPanelLayout.PanelState.EXPANDED : SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
        findViewById(R.id.togglePlay2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playButtonAction();
            }
        });
        findViewById(R.id.volDown).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeCheck()) {
                    mSocket.emit("volumedown");
                }
            }
        });
        findViewById(R.id.toggleSubs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeCheck()) {
                    if (comms.hasSubs()) {
                        mSocket.emit("togglesubtitle");
                    } else {
                        Toast.makeText(getApplicationContext(), "No subtitles", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeCheck()) {
                    mSocket.emit("stop");
                }
            }
        });
        findViewById(R.id.volUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeCheck()) {
                    mSocket.emit("volumeup");
                }
            }
        });
        findViewById(R.id.fastbackward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeCheck()) {
                    mSocket.emit("fastbackward");
                }
            }
        });
        findViewById(R.id.backward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeCheck()) {
                    mSocket.emit("backward");
                }
            }
        });
        findViewById(R.id.togglePlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playButtonAction();
            }
        });
        findViewById(R.id.forward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeCheck()) {
                    mSocket.emit("forward");
                }
            }
        });
        findViewById(R.id.fastforward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeCheck()) {
                    mSocket.emit("fastforward");
                }
            }
        });
    }

    private void playButtonAction() {
        if (disconnectCheck()) {
            if (comms.isIdle()) {
                if (torObject != null) {
                    comms.setLoading(true);
                    try {
                        JSONObject request = new JSONObject();
                        request.put("torrent", torObject.getRequest());

                        if (subObject != null) {
                            request.put("subtitle", subObject.getRequest());
                        }
                        mSocket.emit("play", request);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "No torrent specified", Toast.LENGTH_SHORT).show();
                }
            } else {
                mSocket.emit("pause");
            }
        }
    }

    private boolean activeCheck() {
        if (disconnectCheck()) {
            if (comms.isActive()) {
                comms.setLoading(true);
                return true;
            }
            Toast.makeText(getApplicationContext(), "Nothing playing", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean disconnectCheck() {
        if (comms.isConnected()) {
            return true;
        }
        Toast.makeText(getApplicationContext(), "No connection", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onBackPressed() {
        if (panel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            panel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else if (mViewPager.getCurrentItem() == 1) {
            setViewPager(0);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        mSocket = ((GlobalState) getApplication()).createSocket(comms.getAddress());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((GlobalState) getApplication()).destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    private void setupViewPager(ViewPager viewPager){
        mSectionsStatePagerAdapter.addFragment(new Fragment_Home(), "Home");
        mSectionsStatePagerAdapter.addFragment(new Fragment_Results(), "Results");
        viewPager.setAdapter(mSectionsStatePagerAdapter);
    }

    public void setViewPager(int fragmentNumber){
        mViewPager.setCurrentItem(fragmentNumber);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.hostChange:
                createEditDialog( "Edit the IP Address of your Raspberry Pi", 0);
                break;
            case R.id.portChange:
                createEditDialog("Edit the port number of your Raspberry Pi", 1);
                break;
            case R.id.clearRecents:
                Fragment_Home homeFragment = (Fragment_Home) mSectionsStatePagerAdapter.getItem(0);
                homeFragment.recentListAdapter.clearList();
                homeFragment.recentListAdapter.notifyDataSetChanged();
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("recents", homeFragment.recentListAdapter.getJSSON().toString());
                editor.apply();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createEditDialog(String message, final int toEdit) {
        final EditText input = new EditText(this);
        input.setText((toEdit == 0) ? comms.getHost() : comms.getPort());

        new AlertDialog.Builder(this)
                .setMessage(message)
                .setView(input)
                .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        in.hideSoftInputFromWindow(input.getWindowToken(),0);

                        String newValue = input.getText().toString();

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString((toEdit == 0) ? "host" : "port", newValue);
                        editor.commit();

                        if (toEdit == 0)
                            comms.setHost(newValue);
                        else
                            comms.setPort(newValue);
                        if (mSocket != null) {
                            ((GlobalState) getApplication()).destroy();
                        }
                        mSocket = ((GlobalState) getApplication()).createSocket(comms.getAddress());
                        Fragment_Home fragmentHome = (Fragment_Home) mSectionsStatePagerAdapter.getItem(0);
                        Fragment_Results fragmentResults = (Fragment_Results) mSectionsStatePagerAdapter.getItem(1);
                        fragmentHome.mSocket = ((GlobalState)getApplication()).getSocket();
                        fragmentResults.mSocket = ((GlobalState)getApplication()).getSocket();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        in.hideSoftInputFromWindow(input.getWindowToken(),0);
                    }
                })
                .show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void statusHandler(GlobalState.statusEvent status) {
        comms.update(status.status);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void messageHandler(GlobalState.messageEvent msgEvent) {
        if (!msgEvent.message.equals(""))
            Toast.makeText(getApplicationContext(), msgEvent.message, Toast.LENGTH_SHORT).show();
        if (msgEvent.last)
            comms.setLoading(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void fullStateHandler(GlobalState.fullStatusEvent stateEvent) {
        torObject = new ResultHolder(stateEvent.torTitle, stateEvent.torURL);
        subObject = new ResultHolder(stateEvent.subTitle, stateEvent.subURL);

        comms.updateTorrentTitle(torObject.title);

        if (subObject.title.equals("-")) {
            comms.updateSubtitleTitle("");
            subObject = null;
        } else {
            comms.updateSubtitleTitle(subObject.title);
        }
        comms.update(stateEvent.state);

        if (!comms.isExpandedOnStart()) {
            panel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        }
        comms.updateImage(stateEvent.title, stateEvent.image);

        Fragment_Home homeFragment = (Fragment_Home) mSectionsStatePagerAdapter.getItem(0);
        homeFragment.recentListAdapter.addItem(new RecentItem(stateEvent.title, stateEvent.image, stateEvent.season, stateEvent.episode));
        homeFragment.recentListAdapter.notifyDataSetChanged();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("recents", homeFragment.recentListAdapter.getJSSON().toString());
        editor.apply();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void searchResults(GlobalState.resultEvent result) {
        switch (result.type) {
            case "torrentresult":
                readTorrentData(result.results);
                setViewPager(1);
                break;
            case "subtitleresult":
                readSubtitleData(result.results);
                setViewPager(1);
                break;
            case "magneturl":
                torObject = new ResultHolder(selectedTitle, result.result);
                comms.updateTorrentTitle(selectedTitle);
                setViewPager(0);
                ((ImageView)findViewById(R.id.coverView)).setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                ((ImageView)findViewById(R.id.coverView)).setImageResource(R.mipmap.ic_launcher_pi_round);
                break;
        }
        comms.setLoading(false);
    }

    public void readTorrentData(JSONArray data) {
        results.clear();

        if (data.length() > 0) {
            try {
                for (int i = 0 ; i < data.length() ; i++) {
                    if (data.get(i).toString().equals("null")) {
                        continue;
                    }
                    String title, date, seeds, size, desc, provider;

                    JSONObject obj = (JSONObject) data.get(i);

                    title = obj.getString("title");
                    date = obj.getString("time");
                    seeds = obj.getString("seeds");
                    size = obj.getString("size");
                    desc = obj.getString("desc");
                    provider = obj.getString("provider");

                    ResultTorrent torrent = new ResultTorrent(title, date, seeds, size, desc, provider);
                    results.add(torrent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Fragment_Results fragment = (Fragment_Results) mSectionsStatePagerAdapter.getItem(1);
        fragment.adapter.notifyDataSetChanged();
    }

    public void readSubtitleData(JSONArray data) {
        results.clear();
        if (data.length() > 0) {
            try {
                for (int i = 0 ; i < data.length() ; i++) {
                    if (data.get(i).toString().equals("null")) {
                        continue;
                    }
                    String title, downloads, url;

                    JSONObject obj = (JSONObject) data.get(i);
                    title = obj.getString("filename");
                    downloads = obj.getString("downloads");
                    url = obj.getString("url");

                    ResultSubtitle subs = new ResultSubtitle(title, downloads, url);
                    results.add(subs);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Fragment_Results fragment = (Fragment_Results) mSectionsStatePagerAdapter.getItem(1);
        fragment.adapter.notifyDataSetChanged();
    }

    public static class ResultHolder {
        String title, url;
        ResultHolder (String title, String url) {
            this.title = title;
            this.url = url;
        }
        JSONObject getRequest() throws JSONException {
            JSONObject request = new JSONObject();
            request.put("title", this.title);
            request.put("url", this.url);
            return request;
        }
    }
}

