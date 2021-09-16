package com.example.imirror.other;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.imirror.R;
import com.github.ybq.android.spinkit.SpinKitView;
import com.github.ybq.android.spinkit.SpriteFactory;
import com.github.ybq.android.spinkit.Style;
import com.github.ybq.android.spinkit.sprite.Sprite;

public class LoadingUtils {

    private static Dialog mLoadingDialog;
    private static SpinKitView dialogSpinKit;

    /**
     * 显示加载对话框
     *
     * @param context    上下文
     * @param msg        对话框显示内容
     */
    public static void showDialogForLoading(Context context, String msg) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_progress_bar, null);
        TextView loadingText = (TextView) view.findViewById(R.id.id_tv_loading_dialog_text);
        dialogSpinKit = (SpinKitView) view.findViewById(R.id.dialog_spin_kit);
        loadingText.setText(msg);

        Style style = Style.values()[8];//设置加载动画效果
        Sprite drawable = SpriteFactory.create(style);
        dialogSpinKit.setIndeterminateDrawable(drawable);

        mLoadingDialog = new Dialog(context, R.style.dialogStyle);
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.setCanceledOnTouchOutside(false);
        mLoadingDialog.setContentView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

//        mLoadingDialog.show();

    }
    public static void showLoading(){
        if(mLoadingDialog!=null){
            mLoadingDialog.show();
        }

    }
    public static void closeLoading() {
        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
            mLoadingDialog.cancel();
        }
    }
}
