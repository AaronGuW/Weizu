package com.demo.aaronapplication.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.demo.aaronapplication.weizu.R;
import com.demo.aaronapplication.weizu.User;

import java.util.ArrayList;

/**
 * Created by Aaron on 2016/3/24.
 */
public class contactsFragment extends Fragment {

    private ListView contactslist;
    private BaseAdapter contactsAdapter;
    private ArrayList<User> contactsArraylist;
    private LayoutInflater layoutInflater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.contactspage, container, false);
        contactslist = (ListView)v.findViewById(R.id.contactslist);

        contactsArraylist = new ArrayList<>();
        for (int i = 0 ; i != 8 ; ++i) {
            contactsArraylist.add(new User());
        }

        layoutInflater = LayoutInflater.from(getActivity());
        init_contactslist();
        return v;
    }

    private void init_contactslist() {
        contactsAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return contactsArraylist.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v;
                if (convertView != null) {
                    v = convertView;
                } else {
                    v = layoutInflater.inflate(R.layout.contactslistitem, null);
                }
                return v;
            }
        };
        contactslist.setAdapter(contactsAdapter);
        contactslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }
}
