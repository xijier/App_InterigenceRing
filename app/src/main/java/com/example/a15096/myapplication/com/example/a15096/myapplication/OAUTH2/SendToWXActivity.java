package com.example.a15096.myapplication.com.example.a15096.myapplication.OAUTH2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.a15096.myapplication.ListItemAdapter;
import com.example.a15096.myapplication.R;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.ShowMessageFromWX;
import com.tencent.mm.opensdk.modelmsg.WXAppExtendObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class SendToWXActivity extends AppCompatActivity implements IWXAPIEventHandler {
    private IWXAPI api;
    private Context mContext = null;
    private int mTargetScene = SendMessageToWX.Req.WXSceneSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);
        setContentView(R.layout.send_to_wx);
        mContext = SendToWXActivity.this;
        Button regBtn = (Button) findViewById(R.id.reg_btn);
        regBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 将该app注册到微信
                api.registerApp(Constants.APP_ID);
            }
        });
        initView();
    }

    private void initView() {
        findViewById(R.id.send_text).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final EditText editor = new EditText(SendToWXActivity.this);
                editor.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                editor.setText(R.string.send_text_default);

                new AlertDialog.Builder(mContext)
                        .setTitle("确定删除")
                        .setMessage("")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 清除sharedpreferences的数据
                                String text = editor.getText().toString();
                                if (text == null || text.length() == 0) {
                                    return;
                                }

                                WXTextObject textObj = new WXTextObject();
                                textObj.text = text;

                                WXMediaMessage msg = new WXMediaMessage();
                                msg.mediaObject = textObj;
                                // msg.title = "Will be ignored";
                                msg.description = text;

                                SendMessageToWX.Req req = new SendMessageToWX.Req();
                                req.transaction = buildTransaction("text");
                                req.message = msg;
                                req.scene = mTargetScene;
                                api.sendReq(req);
                            }
                        }).create().show();
            }
        });

        findViewById(R.id.get_token).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // send oauth request
                final SendAuth.Req req = new SendAuth.Req();
                req.scope = "snsapi_userinfo";
                req.state = "none";
                api.sendReq(req);
                finish();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        api.handleIntent(intent, this);
    }

    // 微信发送请求到第三方应用时，会回调到该方法
    @Override
    public void onReq(BaseReq req) {
        switch (req.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
                goToGetMsg();
                break;
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
                goToShowMsg((ShowMessageFromWX.Req) req);
                break;
            default:
                break;
        }
    }

    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    @Override
    public void onResp(BaseResp resp) {
        int result = 0;

        Toast.makeText(this, "baseresp.getType = " + resp.getType(), Toast.LENGTH_SHORT).show();

        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                result = R.string.errcode_success;
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = R.string.errcode_cancel;
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = R.string.errcode_deny;
                break;
            case BaseResp.ErrCode.ERR_UNSUPPORT:
                result = R.string.errcode_unsupported;
                break;
            default:
                result = R.string.errcode_unknown;
                break;
        }

        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    }

    private void goToGetMsg() {
      // Intent intent = new Intent(this, GetFromWXActivity.class);
    //   intent.putExtras(getIntent());
    //   startActivity(intent);
        finish();
    }

    private void goToShowMsg(ShowMessageFromWX.Req showReq) {
        WXMediaMessage wxMsg = showReq.message;
        WXAppExtendObject obj = (WXAppExtendObject) wxMsg.mediaObject;

        StringBuffer msg = new StringBuffer(); // 组织一个待显示的消息内容
        msg.append("description: ");
        msg.append(wxMsg.description);
        msg.append("\n");
        msg.append("extInfo: ");
        msg.append(obj.extInfo);
        msg.append("\n");
        msg.append("filePath: ");
        msg.append(obj.filePath);

        Intent intent = new Intent(this, ShowFromWXActivity.class);
        intent.putExtra(Constants.ShowMsgActivity.STitle, wxMsg.title);
        intent.putExtra(Constants.ShowMsgActivity.SMessage, msg.toString());
        intent.putExtra(Constants.ShowMsgActivity.BAThumbData, wxMsg.thumbData);
        startActivity(intent);
        finish();
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }
}
