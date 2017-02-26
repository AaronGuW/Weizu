package com.demo.aaronapplication.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.aaronapplication.weizu.R;
import com.demo.aaronapplication.weizu.User;

import java.util.ArrayList;

/**
 * Created by Aaron on 2016/3/22.
 */
public class messageFragment extends Fragment {

    private ListView contactslist;
    private BaseAdapter contactsAdapter;
    private ArrayList<User> contactsArraylist;
    private LayoutInflater layoutInflater;

    private static final String secondname[] = {"一","二","三","四","五","六","七","八","九","十"};

    public interface chattinglistener {
        public void onTendtoChat(User contact);
    }

    private chattinglistener chattinglistener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            chattinglistener = (chattinglistener) activity;
        } catch (ClassCastException e) {
            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.messagepage, container, false);
        contactslist = (ListView)v.findViewById(R.id.contactslist);


        contactsArraylist = new ArrayList<>();
        for (int i = 0 ; i != 8 ; ++i) {
            contactsArraylist.add(new User("联系人" + secondname[i]));
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
                ((TextView)v.findViewById(R.id.contact_name)).setText(contactsArraylist.get(position).getUsername());
                return v;
            }
        };
        contactslist.setAdapter(contactsAdapter);
        contactslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chattinglistener.onTendtoChat(contactsArraylist.get(position));
            }
        });
    }

}
