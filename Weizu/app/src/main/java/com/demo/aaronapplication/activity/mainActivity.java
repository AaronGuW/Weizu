package com.demo.aaronapplication.activity;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.aaronapplication.application.App;
import com.demo.aaronapplication.fragments.categoryFragment;
import com.demo.aaronapplication.fragments.historyFragment;
import com.demo.aaronapplication.fragments.homepageFragment;
import com.demo.aaronapplication.fragments.messageFragment;
import com.demo.aaronapplication.fragments.searchFragment;
import com.demo.aaronapplication.fragments.trolleyFragment;
import com.demo.aaronapplication.fragments.userFragment;
import com.demo.aaronapplication.weizu.HttpUtil;
import com.demo.aaronapplication.weizu.R;
import com.demo.aaronapplication.weizu.User;
import com.demo.aaronapplication.weizu.fileUtil;

import org.json.JSONException;
import org.json.JSONObject;

import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;

public class mainActivity extends FragmentActivity implements View.OnClickListener, homepageFragment.onHeadlineClickListener,
                                                            searchFragment.onHeadlineClickListener, historyFragment.historylistener,
                                                            messageFragment.chattinglistener, userFragment.OperationListener,
                                                            categoryFragment.onTypeSearchListener, RongIM.UserInfoProvider,
                                                            homepageFragment.onHomeTypeSearchListener {

    //8 fragments on the mainpage
    private homepageFragment home_page;
    private categoryFragment category_page;
    private ConversationListFragment conversationList;
    private trolleyFragment trolley_page;
    private userFragment user_page;
    private searchFragment search_page;

    private TextView unReadCnt;

    //fixed views on the mainpage
    private ImageView homepage_btn, category_btn, message_btn, trolley_btn, user_btn;

    /** current fragment order
     *  0 homepage, 1 category, 2 message, 3 trolley, 4 user
     */
    private static final int HOMEPAGE = 0, CATEGORY = 1, MESSAGE = 2, TROLLEY = 3, USER = 4;
    private int cur_frag;

    private static final int TAKE = 0, PICK = 1;

    //request code, maybe there will be more
    public static final int PHOTOGET = 1995, CANCEL = 1974, INPUT = 7, PICTURE = 8,SHOWRESULT = 22, NEW_MODIFY_RELEASE = 10;

    //two continous back click to quit the app
    private long lastbacktimestamp = 0;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainpage);

        home_page = new homepageFragment();
        category_page = new categoryFragment();
        trolley_page = new trolleyFragment();
        user_page = new userFragment();
        search_page = new searchFragment();

        init_views();
        sethomepage();

        SharedPreferences account = getSharedPreferences("account",MODE_PRIVATE);
        if (account.getBoolean("login",false)) {
            connect(account.getString("token", "-"));
            Log.e("rong","attempt to connect");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (RongIM.getInstance() != null) {
            setUnReadCnt();
        }
    }

    /**
     * Reply to request from the homepage
     * @param requestCode
     */
    @Override
    public void onHomeHeadlineClicked(int requestCode) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        switch (requestCode) {
            case homepageFragment.TEXT:
                search_page = new searchFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("action",INPUT);
                search_page.setArguments(bundle);
                transaction.replace(R.id.maincontent, search_page);
                break;
            case homepageFragment.TAKE_PHOTO:
                Intent tintent = new Intent(mainActivity.this, UploadphotoActivity.class);
                tintent.putExtra("action",TAKE);
                startActivityForResult(tintent, PHOTOGET);
                break;
            case homepageFragment.PICK_PHOTO:
                Intent pintent = new Intent(mainActivity.this, UploadphotoActivity.class);
                pintent.putExtra("action", PICK);
                startActivityForResult(pintent, PHOTOGET);
                break;
        }

        transaction.commit();
    }

    @Override
    public void onSearchHeadlineClicked(int requestcode, String data) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        switch (requestcode) {
            case searchFragment.BACK:
                if (cur_frag == HOMEPAGE)
                    transaction.replace(R.id.maincontent, home_page);
                else
                    transaction.replace(R.id.maincontent, category_page);
                break;
            case searchFragment.SEARCH:
                //transaction.replace(R.id.childcontent, result_page);
                break;
        }

        transaction.commit();
    }


    @Override
    public void onTypeSearch(String res) {
        Bundle bundle = new Bundle();
        bundle.putInt("action", SHOWRESULT);
        bundle.putString("res", res);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        search_page = new searchFragment();
        search_page.setArguments(bundle);
        Log.i("show", "result");
        transaction.replace(R.id.maincontent, search_page);
        transaction.commit();
    }

    @Override
    public void onHomeTypeSearch(String res) {
        Bundle bundle = new Bundle();
        bundle.putInt("action", SHOWRESULT);
        bundle.putString("res", res);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        search_page = new searchFragment();
        search_page.setArguments(bundle);
        Log.i("show", "result");
        transaction.replace(R.id.maincontent, search_page);
        transaction.commit();
    }

    @Override
    public void onTendtoChat(User contact) {
        Intent intent = new Intent(mainActivity.this, ChattingActivity.class);
        intent.putExtra("contactname",contact.getUsername());
        startActivity(intent);
    }

    @Override
    public void onLogin() {
        connect(getSharedPreferences("account",MODE_PRIVATE).getString("token","-"));
        Log.e("rong","attempt to connect");
    }

    /**
     * interface of historyFragment, called to fill the edittext in the searchFragment
     * @param key
     */
    @Override
    public void fillinput(String key) {
        ((EditText)findViewById(R.id.search_input)).setText(key);
    }

    @Override
    public void onClick(View v) {
        FragmentManager fm = getSupportFragmentManager();
        // 开启Fragment事务
        FragmentTransaction transaction = fm.beginTransaction();

        switch (v.getId()) {
            case R.id.homepage_btn:
                if (cur_frag != HOMEPAGE ) {
                    transaction.replace(R.id.maincontent,home_page);
                    update_bg(cur_frag, HOMEPAGE);
                    cur_frag = HOMEPAGE;
                }
                break;
            case R.id.category_btn:
                if (cur_frag != CATEGORY ) {
                    transaction.replace(R.id.maincontent,category_page);
                    update_bg(cur_frag, CATEGORY);
                    cur_frag = CATEGORY;
                }
                break;
            case R.id.message_btn:
                if (getSharedPreferences("account",MODE_PRIVATE).getBoolean("login", false)) {
                    if (cur_frag != MESSAGE) {
                        conversationList = new ConversationListFragment();
                        Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                                .appendPath("conversationlist")
                                .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") //设置私聊会话非聚合显示
                                .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "true")//设置群组会话聚合显示
                                .appendQueryParameter(Conversation.ConversationType.DISCUSSION.getName(), "false")//设置讨论组会话非聚合显示
                                .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "false")//设置系统会话非聚合显示
                                .build();
                        conversationList.setUri(uri);
                        transaction.replace(R.id.maincontent, conversationList);
                        update_bg(cur_frag, MESSAGE);
                        cur_frag = MESSAGE;
                        //RongIM.getInstance().startPrivateChat(mainActivity.this, "12", "老爸");
                    }
                } else {
                    Toast.makeText(this, getString(R.string.loginFirst), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.trolley_btn:
                if (getSharedPreferences("account",MODE_PRIVATE).getBoolean("login", false)) {
                    if (cur_frag != TROLLEY) {
                        transaction.replace(R.id.maincontent, trolley_page);
                        update_bg(cur_frag, TROLLEY);
                        cur_frag = TROLLEY;
                    }
                } else {
                    Toast.makeText(this, getString(R.string.loginFirst), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.user_btn:
                if (cur_frag != USER ) {
                    transaction.replace(R.id.maincontent,user_page);
                    update_bg(cur_frag, USER);
                    cur_frag = USER;
                }
                break;
        }
        transaction.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            long curtime = System.currentTimeMillis();
            if (curtime - lastbacktimestamp > 2000) {
                lastbacktimestamp = curtime;
                Toast.makeText(this,"再次点击退出微租",Toast.LENGTH_SHORT).show();
            }
            else {
                finish();
            }
            return false;
        } else {
            return super.onKeyDown(keyCode,event);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (resultCode != RESULT_OK)
            return;
        switch (requestCode) {
            case PHOTOGET:
                if (data.getIntExtra("action",0) == PHOTOGET) {
                    String raw = data.getStringExtra("photo");
                    //Bitmap target = data.getParcelableExtra("photo"); //TODO the bitmap is returned and to be handled
                    Bundle bundle = new Bundle();
                    bundle.putInt("action", PICTURE);
                    bundle.putString("photo", raw);
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    search_page = new searchFragment();
                    search_page.setArguments(bundle);
                    Log.i("show", "result");
                    transaction.replace(R.id.maincontent, search_page);
                    transaction.commit();
                } else if (data.getIntExtra("action",0) == CANCEL) {
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.replace(R.id.maincontent, home_page);
                    transaction.commit();
                }
                break;
        }
    }

    /**  Assign all the views to the variables on creation
     *   The corresponding onclick listeners are assigned too
     */
    private void init_views() {
        homepage_btn = (ImageView)findViewById(R.id.homepage_btn);
        homepage_btn.setOnClickListener(this);
        category_btn = (ImageView)findViewById(R.id.category_btn);
        category_btn.setOnClickListener(this);
        message_btn = (ImageView)findViewById(R.id.message_btn);
        message_btn.setOnClickListener(this);
        trolley_btn = (ImageView)findViewById(R.id.trolley_btn);
        trolley_btn.setOnClickListener(this);
        user_btn = (ImageView)findViewById(R.id.user_btn);
        user_btn.setOnClickListener(this);
        unReadCnt = (TextView)findViewById(R.id.unread);
    }

    /**
     * The default page of the app is the homepage, set the fragment to homepage on creation
     */
    private void sethomepage() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.maincontent, home_page); //replace = remove + add
        transaction.commit();
        ((ImageView)findViewById(R.id.homepage_btn)).setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.homepage_pressed));
        cur_frag = HOMEPAGE;
    }

    /**
     * update the button background when switching fragments
     * @param originstatus
     * @param newstatus
     */
    private void update_bg(int originstatus, int newstatus) {
        switch (originstatus) {
            case HOMEPAGE:
                homepage_btn.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.homepage));
                break;
            case CATEGORY:
                category_btn.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.category));
                break;
            case MESSAGE:
                message_btn.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.message));
                break;
            case TROLLEY:
                trolley_btn.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.trolley));
                break;
            case USER:
                user_btn.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.user));
                break;
        }

        switch (newstatus) {
            case HOMEPAGE:
                homepage_btn.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.homepage_pressed));
                break;
            case CATEGORY:
                category_btn.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.category_pressed));
                break;
            case MESSAGE:
                message_btn.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.message_pressed));
                break;
            case TROLLEY:
                trolley_btn.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.trolley_pressed));
                break;
            case USER:
                user_btn.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.user_pressed));
                break;
        }
    }

    /**
     * 建立与融云服务器的连接
     *
     * @param token
     */
    public void connect(String token) {

        if (getApplicationInfo().packageName.equals(App.getCurProcessName(getApplicationContext()))) {
            Log.e("log","start");
            /**
             * IMKit SDK调用第二步,建立与服务器的连接
             */
            RongIM.connect(token, new RongIMClient.ConnectCallback() {

                /**
                 * Token 错误，在线上环境下主要是因为 Token 已经过期，您需要向 App Server 重新请求一个新的 Token
                 */
                @Override
                public void onTokenIncorrect() {
                    Log.e("MainActivity", "--onTokenIncorrect");
                }

                /**
                 * 连接融云成功
                 * @param userid 当前 token
                 */
                @Override
                public void onSuccess(String userid) {
                    Log.e("MainActivity", "--onSuccess" + userid);
                    RongIM.setUserInfoProvider(mainActivity.this,true);
                    RongIM.setOnReceiveMessageListener(new RongIMClient.OnReceiveMessageListener() {
                        @Override
                        public boolean onReceived(Message message, int i) {
                            setUnReadCnt();
                            return false;
                        }
                    });
                    setUnReadCnt();
                }

                /**
                 * 连接融云失败
                 * @param errorCode 错误码，可到官网 查看错误码对应的注释
                 */
                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {
                    Log.e("MainActivity", "--onError" + errorCode);
                }
            });
        }
    }

    @Override
    public UserInfo getUserInfo(String s) {
        Log.e("getUserInfo",s);
        SharedPreferences account = getSharedPreferences("account", MODE_PRIVATE);
        String uid = account.getString("uid","0");
        if (s.equals(uid)) {
            String username = account.getString("username","unknown");
            Boolean hasphoto = account.getBoolean("hasphoto",false);
            String path = new String();
            if (hasphoto) {
                path = "file:/"+Environment.getExternalStorageDirectory().getPath()+"/weizu/img/portrait/"+uid+".jpeg";
            }
            return new UserInfo(s, username, hasphoto?Uri.parse(path):null);
        } else {
            Uri uri = fileUtil.getPortraitUri(s);
            if (uri != null) {
                //头像已缓存，获取用户名
                String un = HttpUtil.SynHttpClientGet(HttpUtil.host+"p?action=2&uid="+s);
                Log.e("userInfo","success local");
                return new UserInfo(s, un, uri);
            } else {
                String userInfo = HttpUtil.SynHttpClientGet(HttpUtil.host+"p?action=1&uid="+s);
                try {
                    JSONObject res = new JSONObject(userInfo);
                    if (res.getBoolean("success")) {
                        String username = res.getString("username");
                        boolean hasPortrait = res.getBoolean("hasPortrait");
                        if (hasPortrait) {
                            byte[] data = Base64.decode(res.getString("portrait"), Base64.DEFAULT);
                            Bitmap portrait = BitmapFactory.decodeByteArray(data, 0, data.length);
                            fileUtil.savePortrait(portrait, s);
                            portrait.recycle();
                            Log.e("userInfo","success full");
                            return new UserInfo(s, username, Uri.parse("file:/"+Environment.getExternalStorageDirectory().getPath()+"/weizu/img/portrait/"+s+".jpeg"));
                        } else {
                            Log.e("userInfo","success no portrait");
                            return new UserInfo(s, username, null);
                        }
                    } else {
                        Log.e("userInfo","fail to get");
                        return null;
                    }
                } catch (JSONException JE) {
                    JE.printStackTrace();
                    Log.e("userInfo","error");
                    return null;
                }
            }
        }
    }

    public void setUnReadCnt() {
        RongIM.getInstance().getTotalUnreadCount(new RongIMClient.ResultCallback<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
                unReadCnt.setVisibility(integer == 0 ? View.GONE: View.VISIBLE);
                unReadCnt.setText(integer < 99 ? String.valueOf(integer):"99+");
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                unReadCnt.setVisibility(View.GONE);
            }
        });
    }

}
