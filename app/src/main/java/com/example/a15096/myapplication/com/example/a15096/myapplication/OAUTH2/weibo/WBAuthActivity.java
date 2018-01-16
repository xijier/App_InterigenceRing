package com.example.a15096.myapplication.com.example.a15096.myapplication.OAUTH2.weibo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a15096.myapplication.R;
import com.example.a15096.myapplication.com.example.a15096.myapplication.OAUTH2.Constants;
import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.auth.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
import com.sina.weibo.sdk.auth.sso.SsoHandler;

import java.text.SimpleDateFormat;

public class WBAuthActivity extends AppCompatActivity {

    private static final String TAG = "weibosdk";
    /** 显示认证后的信息，如 AccessToken */
    private TextView mTokenText;
    /** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能  */
    private Oauth2AccessToken mAccessToken;
    /** 注意：SsoHandler 仅当 SDK 支持 SSO 时有效 */
    private SsoHandler mSsoHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wbauth);
        WbSdk.install(this,new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE));
        // 获取 Token View，并让提示 View 的内容可滚动（小屏幕可能显示不全）
        mTokenText = (TextView) findViewById(R.id.token_text_view);
        TextView hintView = (TextView) findViewById(R.id.obtain_token_hint);
        hintView.setMovementMethod(new ScrollingMovementMethod());
        // 创建微博实例

        mSsoHandler = new SsoHandler(WBAuthActivity.this);

        // SSO 授权, 仅客户端
        findViewById(R.id.obtain_token_via_sso).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSsoHandler.authorizeClientSso(new SelfWbAuthListener());
            }
        });


    }
    private class SelfWbAuthListener implements com.sina.weibo.sdk.auth.WbAuthListener{
        @Override
        public void onSuccess(final Oauth2AccessToken token) {
            WBAuthActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAccessToken = token;
                    if (mAccessToken.isSessionValid()) {
                        // 显示 Token
                        updateTokenView(false);
                        // 保存 Token 到 SharedPreferences
                        AccessTokenKeeper.writeAccessToken(WBAuthActivity.this, mAccessToken);
                        Toast.makeText(WBAuthActivity.this,
                                R.string.weibosdk_demo_toast_auth_success, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public void cancel() {
            Toast.makeText(WBAuthActivity.this,
                    R.string.weibosdk_demo_toast_auth_canceled, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onFailure(WbConnectErrorMessage errorMessage) {
            Toast.makeText(WBAuthActivity.this, errorMessage.getErrorMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 显示当前 Token 信息。
     *
     * @param hasExisted 配置文件中是否已存在 token 信息并且合法
     */
    private void updateTokenView(boolean hasExisted) {
        String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                new java.util.Date(mAccessToken.getExpiresTime()));
        String format = getString(R.string.weibosdk_demo_token_to_string_format_1);
        mTokenText.setText(String.format(format, mAccessToken.getToken(), date));

        String message = String.format(format, mAccessToken.getToken(), date);
        if (hasExisted) {
            message = getString(R.string.weibosdk_demo_token_has_existed) + "\n" + message;
        }
        mTokenText.setText(message);
    }
}
