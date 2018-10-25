package com.balaganovrocks.yourmasterclean.ui;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AnimationUtils;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.balaganovrocks.yourmasterclean.base.BaseActivity;
import com.balaganovrocks.yourmasterclean.R;
import com.balaganovrocks.yourmasterclean.base.BaseActivity;
import com.balaganovrocks.yourmasterclean.bean.AppProcessInfo;
import com.balaganovrocks.yourmasterclean.service.CoreService;
import com.balaganovrocks.yourmasterclean.utils.StorageUtil;
import com.balaganovrocks.yourmasterclean.utils.SystemBarTintManager;
import com.balaganovrocks.yourmasterclean.utils.T;

import java.lang.reflect.Field;
import java.util.List;

import butterknife.InjectView;


public class ShortCutActivity extends BaseActivity implements CoreService.OnPeocessActionListener {

    @InjectView(R.id.layout_anim)
    RelativeLayout layoutAnim;

    @InjectView(R.id.mRelativeLayout)
    RelativeLayout mRelativeLayout;

    private Rect rect;
    @InjectView(R.id.clean_light_img)
    ImageView cleanLightImg;


    private CoreService mCoreService;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCoreService = ((CoreService.ProcessServiceBinder) service).getService();
            mCoreService.setOnActionListener(ShortCutActivity.this);
            mCoreService.cleanAllProcess();
            //  updateStorageUsage();


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCoreService.setOnActionListener(null);
            mCoreService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_short_cut);
        rect = getIntent().getSourceBounds();
        if (rect == null) {
            finish();
            return;
        }

        if (rect != null) {

            Class<?> c = null;
            Object obj = null;
            Field field = null;
            int x = 0, statusBarHeight = 0;
            try {
                c = Class.forName("com.android.internal.R$dimen");
                obj = c.newInstance();
                field = c.getField("status_bar_height");
                x = Integer.parseInt(field.get(obj).toString());
                statusBarHeight = getResources().getDimensionPixelSize(x);
            } catch (Exception e1) {

                e1.printStackTrace();
            }

            layoutAnim.measure(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            int height = layoutAnim.getMeasuredHeight();
            int width = layoutAnim.getMeasuredWidth();

            RelativeLayout.LayoutParams layoutparams = (RelativeLayout.LayoutParams) layoutAnim
                    .getLayoutParams();

            layoutparams.leftMargin = rect.left + rect.width() / 2 - width / 2;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                setTranslucentStatus(true);
                SystemBarTintManager tintManager = new SystemBarTintManager(
                        this);
                tintManager.setStatusBarTintEnabled(true);
                tintManager.setStatusBarTintResource(R.color.transparent);
                layoutparams.topMargin = rect.top + rect.height() / 2 - height
                        / 2;

            } else {
                layoutparams.topMargin = rect.top + rect.height() / 2 - height
                        / 2 - statusBarHeight;
            }

            mRelativeLayout.updateViewLayout(layoutAnim, layoutparams);
        }
        cleanLightImg.startAnimation(AnimationUtils.loadAnimation(this,
                R.anim.rotate_anim));
        bindService(new Intent(mContext, CoreService.class),
                mServiceConnection, Context.BIND_AUTO_CREATE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }



    @Override
    public void onScanStarted(Context context) {

    }

    @Override
    public void onScanProgressUpdated(Context context, int current, int max) {

    }

    @Override
    public void onScanCompleted(Context context, List<AppProcessInfo> apps) {

    }

    @Override
    public void onCleanStarted(Context context) {

    }

    @Override
    public void onCleanCompleted(Context context, long cacheSize) {
        if (cacheSize > 0) {
            T.showLong(mContext, "1" + StorageUtil.convertStorage(cacheSize) + "память");
        } else {
            T.showLong(mContext, "Вы уже очистили память, пожалуйста, вернитесь позже");
        }

        finish();
    }


    public void killProcess() {
        // TODO Auto-generated method stub

        ActivityManager am = (ActivityManager) getBaseContext()
                .getApplicationContext().getSystemService(
                        Context.ACTIVITY_SERVICE);
        // 获得正在运行的所有进程
        List<ActivityManager.RunningAppProcessInfo> processes = am
                .getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo info : processes) {
            if (info != null && info.processName != null
                    && info.processName.length() > 0) {
                String pkgName = info.processName;
                if (!("system".equals(pkgName) || "launcher".equals(pkgName)
                        || "android.process.media".equals(pkgName)
                        || "android.process.acore".equals(pkgName)
                        || "com.android.phone".equals(pkgName)
                        || "com.fb.FileBrower".equals(pkgName)// 浏览器
                        || "com.ott_pro.launcher".equals(pkgName)// 桌面
                        || "com.ott_pro.upgrade".equals(pkgName)// 升级
                        || "com.example.airplay".equals(pkgName)// 媒体分享
                        || "com.amlogic.mediacenter".equals(pkgName)// 媒体分享
                        || "com.android.dreams.phototable".equals(pkgName)// 屏保
                        || "com.amlogic.inputmethod.remote".equals(pkgName)// 输入法
                        || pkgName.startsWith("com.lefter"))) {
                    am.killBackgroundProcesses(pkgName);// 杀进程
                }
            }
        }


    }


    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        LayoutParams winParams = win.getAttributes();
        final int bits = LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }


    @Override
    public void onDestroy() {
        unbindService(mServiceConnection);
        super.onDestroy();
    }
}
