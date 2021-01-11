package com.boer.de.jaap.rasppiremote;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import io.socket.client.Socket;

/**
 * Created by jaap on 25-3-18.
 */

public class Fragment_Results extends Fragment {

    ResultListAdapter adapter;
    private ListView mListView;
    public Socket mSocket = null;
    private Communicator comms;
    MainActivity mainActivity;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_results, container, false);

        mListView = (ListView) view.findViewById(R.id.results);
        mListView.setEmptyView(view.findViewById(R.id.emptyElement));
        adapter = new ResultListAdapter((MainActivity) getActivity());
        mListView.setAdapter(adapter);
        comms = ((MainActivity)getActivity()).comms;
        mSocket = ((MainActivity)getActivity()).mSocket;
        mainActivity = (MainActivity)getActivity();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                if (comms.isActive()) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    mainActivity.findViewById(R.id.stop).performClick();
                                    if (adapter.getItemViewType(position) == adapter.TYPE_TORRENT) {
                                        ResultTorrent selectedItem = (ResultTorrent) (((MainActivity)getActivity()).results.get(position));
                                        mainActivity.selectedTitle = selectedItem.getTitle();
                                        mSocket.emit("getTorrentURL", selectedItem.getRequest());
                                        comms.setLoading(true);
                                    } else {
                                        ResultSubtitle selectedItem = (ResultSubtitle) (((MainActivity)getActivity()).results.get(position));
                                        mainActivity.subObject = new MainActivity.ResultHolder(selectedItem.getTitle(), selectedItem.getUrl());
                                        comms.updateSubtitleTitle(selectedItem.getTitle());
                                        mainActivity.setViewPager(0);
                                    }
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
                    if (adapter.getItemViewType(position) == adapter.TYPE_TORRENT) {
                        ResultTorrent selectedItem = (ResultTorrent) (((MainActivity)getActivity()).results.get(position));
                        mainActivity.selectedTitle = selectedItem.getTitle();
                        mSocket.emit("getTorrentURL", selectedItem.getRequest());
                        comms.setLoading(true);
                    } else {
                        ResultSubtitle selectedItem = (ResultSubtitle) (((MainActivity)getActivity()).results.get(position));
                        mainActivity.subObject = new MainActivity.ResultHolder(selectedItem.getTitle(), selectedItem.getUrl());
                        comms.updateSubtitleTitle(selectedItem.getTitle());
                        mainActivity.setViewPager(0);
                    }
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSocket = ((MainActivity)getActivity()).mSocket;
    }
}
