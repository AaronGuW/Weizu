package com.demo.aaronapplication.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.demo.aaronapplication.weizu.R;
import com.demo.aaronapplication.weizu.User;
import com.demo.aaronapplication.weizu.myMessage;

import java.util.ArrayList;

/**
 * Created by Aaron on 2016/3/25.
 */
public class ChattingActivity extends Activity implements View.OnClickListener {

    private ArrayList<myMessage> messageArrayList;
    private ListView dialoglist;
    private BaseAdapter dialogAdapter;
    private LayoutInflater inflater;

    private TextView contact_name;
    private EditText input;
    private String contactname;
    private User contact, me;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chattingpage);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Intent intent = getIntent();
        contactname = intent.getStringExtra("contactname");
        contact = new User(contactname);
        me = new User("me");

        dialoglist = (ListView)findViewById(R.id.dialog);
        contact_name = (TextView)findViewById(R.id.contact_name);
        contact_name.setText(contactname);

        findViewById(R.id.back_btn).setOnClickListener(this);
        findViewById(R.id.root).setOnClickListener(this);

        messageArrayList = new ArrayList<>();
        inflater = LayoutInflater.from(this);

        for (int i = 0 ; i != 4 ; ++i) {
            if (i%2 == 0) {
                messageArrayList.add(new myMessage("你好",me));
            } else {
                messageArrayList.add(new myMessage("自动回复:多喝热水", contact));
            }
        }

        setInput();
        init_dialoglist();
    }

    @Override
    public void onClick(View v) {
        InputMethodManager imm =  (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),0);
        }
        if (v.getId() == R.id.back_btn) {
            finish();
        }
    }

    /**
     * When user click enter on the keyboard, put the sentence on the
     */
    private void setInput() {
        input = (EditText)findViewById(R.id.chatting_input);
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    if (input.getText().toString().compareTo("") != 0) {
                        messageArrayList.add(new myMessage(input.getText().toString(), me));
                        dialogAdapter.notifyDataSetChanged();
                        input.setText("");
                        autoreply();
                    }
                }
                return false;
            }
        });
    }

    private void autoreply() {
        messageArrayList.add(new myMessage("自动回复:多喝热水",contact));
        dialogAdapter.notifyDataSetChanged();
    }

    private void init_dialoglist() {
        dialogAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return messageArrayList.size();
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
                myMessage msg = messageArrayList.get(position);
                if (msg.getSender().getUsername().compareTo("me") == 0) {
                    v = inflater.inflate(R.layout.message_send, null);
                } else {
                    v = inflater.inflate(R.layout.message_recv, null);
                }
                ((TextView)v.findViewById(R.id.sentence)).setText(msg.getContent());
                return v;
            }
        };
        dialoglist.setAdapter(dialogAdapter);
        dialoglist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InputMethodManager imm =  (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),0);
                }
            }
        });
    }
}
