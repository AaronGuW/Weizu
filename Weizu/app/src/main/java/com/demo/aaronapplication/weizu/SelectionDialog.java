package com.demo.aaronapplication.weizu;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;

/**
 * Created by Aaron on 2017/1/20.
 */
public class SelectionDialog extends Dialog implements View.OnClickListener {

    private ArrayList<String> options;
    private RadioGroup group;
    private View.OnClickListener confirmListener;
    private String selectedOption;

    private Context context;

    public SelectionDialog(Context context, ArrayList<String> options, View.OnClickListener confirmListener) {
        super(context);
        this.context = context;
        this.options = options;
        this.confirmListener = confirmListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selectdialog);
        init();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                SelectionDialog.this.dismiss();
                break;
        }
    }

    private void init() {
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.confirm).setOnClickListener(confirmListener);

        group = (RadioGroup)findViewById(R.id.options);

        for (String option: options) {
            RadioButton btn = new RadioButton(context);
            btn.setText(option);
            group.addView(btn, RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT);
        }

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton selected = (RadioButton)findViewById(checkedId);
                selectedOption = selected.getText().toString();
            }
        });
    }

    public String getSelected() {
        return selectedOption;
    }
}
