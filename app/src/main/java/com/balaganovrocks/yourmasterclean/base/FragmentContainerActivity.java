package com.balaganovrocks.yourmasterclean.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;

import com.balaganovrocks.yourmasterclean.R;
import com.balaganovrocks.yourmasterclean.utils.FragmentArgs;

import java.lang.reflect.Method;

public class FragmentContainerActivity extends BaseSwipeBackActivity {

    /**
     * 启动一个界面
     *
     * @param activity
     * @param clazz
     */
    public static void launch(Activity activity, Class<? extends Fragment> clazz, FragmentArgs args) {
        Intent intent = new Intent(activity, FragmentContainerActivity.class);
        intent.putExtra("className", clazz.getName());
        if (args != null)
            intent.putExtra("args", args);
        activity.startActivity(intent);
    }

    public FragmentContainerActivity() {
        super();
    }

    public static void launchForResult(Fragment fragment, Class<? extends Fragment> clazz, FragmentArgs args,
                                       int requestCode) {
        if (fragment.getActivity() == null)
            return;
        Activity activity = fragment.getActivity();

        Intent intent = new Intent(activity, FragmentContainerActivity.class);
        intent.putExtra("className", clazz.getName());
        if (args != null)
            intent.putExtra("args", args);
        fragment.startActivityForResult(intent, requestCode);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String className = getIntent().getStringExtra("className");
        if (TextUtils.isEmpty(className)) {
            finish();
            return;
        }

        FragmentArgs values = (FragmentArgs) getIntent().getSerializableExtra("args");

        Fragment fragment = null;
        if (savedInstanceState == null) {
            try {
                Class clazz = Class.forName(className);
                fragment = (Fragment) clazz.newInstance();
                if (values != null) {
                    try {
                        Method method = clazz.getMethod("setArguments", new Class[]{Bundle.class});
                        method.invoke(fragment, FragmentArgs.transToBundle(values));
                    } catch (Exception e) {
//						e.printStackTrace();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                finish();
                return;
            }
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_fragment_container);


        if (fragment != null) {
            getFragmentManager().beginTransaction().add(R.id.fragmentContainer, fragment, className).commit();
        }

        if (getActionBar() != null)
            getActionBar().setDisplayShowHomeEnabled(false);
    }

    @Override
    public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String message = "SSL Certificate error.";
        switch (error.getPrimaryError()) {
            case SslError.SSL_UNTRUSTED:
                message = "The certificate authority is not trusted.";
                break;
            case SslError.SSL_EXPIRED:
                message = "The certificate has expired.";
                break;
            case SslError.SSL_IDMISMATCH:
                message = "The certificate Hostname mismatch.";
                break;
            case SslError.SSL_NOTYETVALID:
                message = "The certificate is not yet valid.";
                break;
        }
        message += " Do you want to continue anyway?";

        builder.setTitle("SSL Certificate Error");
        builder.setMessage(message);
        builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.proceed();
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.cancel();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:

                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
