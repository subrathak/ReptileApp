package com.reptile.nomad.changedReptile;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by sankarmanoj on 25/05/16.
 */
public class SearchEditText extends EditText {
    public ActionBar actionBar;
    Context mContext;

    public SearchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
    }

    public SearchEditText(Context context, AttributeSet attrs) {

        super(context, attrs);
        this.mContext = context;
    }

    public SearchEditText(Context context)
    {
        super(context);
        this.mContext = context;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            InputMethodManager mgr = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(this.getWindowToken(), 0);
            actionBar.setDisplayShowCustomEnabled(false); //disable a customRadioButton view inside the actionbar
            actionBar.setDisplayShowTitleEnabled(true); //show the title in the action bar
        }
        return super.onKeyPreIme(keyCode, event);
    }
}
