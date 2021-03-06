package com.sizer.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.sizer.R;
import com.sizer.mvp.presenter.RegisterPresenter;
import com.sizer.mvp.view.RegisterView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;

public class RegisterActivity extends MvpAppCompatActivity implements RegisterView {

    @BindView(R.id.layout_register)
    View layoutRegister;

    @BindView(R.id.layout_done)
    View layoutDone;

    @BindView(R.id.layout_uploading)
    View layoutUpload;

    @BindView(R.id.til_email)
    TextInputLayout tilEmail;

    @BindView(R.id.edit_email)
    TextInputEditText editEmail;

    @BindView(R.id.til_password)
    TextInputLayout tilPassword;

    @BindView(R.id.edit_password)
    TextInputEditText editPassword;

    @BindView(R.id.til_name)
    TextInputLayout tilName;

    @BindView(R.id.edit_name)
    TextInputEditText editName;

    @BindView(R.id.til_height)
    TextInputLayout tilHeight;

    @BindView(R.id.edit_height)
    TextInputEditText editHeight;

    @BindView(R.id.radio_male)
    RadioButton radioMale;

    @BindView(R.id.radio_female)
    RadioButton radioFemale;

    @BindView(R.id.btn_create)
    Button btnCreate;


    Snackbar loadingSnackbar;

    private boolean isLoading;

    @InjectPresenter(tag = RegisterPresenter.TAG)
    RegisterPresenter presenter;
    private int animTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        loadingSnackbar = Snackbar.make(findViewById(android.R.id.content), R.string.loading, Snackbar.LENGTH_INDEFINITE);
        Snackbar.SnackbarLayout snack_view = (Snackbar.SnackbarLayout) loadingSnackbar.getView();
        snack_view.addView(new ProgressBar(this));

        animTime = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        editHeight.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onFocusChange();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        presenter.callUpload();
    }

    @OnFocusChange({R.id.edit_email, R.id.edit_password, R.id.edit_name, R.id.edit_height})
    void onFocusChange() {
        if (editEmail.length() > 0) {
            tilEmail.setErrorEnabled(false);
        } else {
            setFieldError(tilEmail, R.string.error_empty_field);
        }

        if (editPassword.length() > 0) {
            if (editPassword.length() < 6) {
                setFieldError(tilPassword, R.string.error_weak_password);
            } else {
                tilPassword.setErrorEnabled(false);
            }
        } else {
            setFieldError(tilPassword, R.string.error_empty_field);
        }

        if (editName.length() > 0) tilName.setErrorEnabled(false);
        else {
            setFieldError(tilName, R.string.error_empty_field);
        }

        if (editHeight.length() > 0) tilHeight.setErrorEnabled(false);
        else {
            setFieldError(tilHeight, R.string.error_empty_field);
        }

        btnCreate.setEnabled(!checkFields() && !isLoading);
    }

    @OnClick(R.id.btn_create)
    void onBtnCreate() {
        presenter.callRegister(editEmail.getText().toString(), editPassword.getText().toString(),
                editName.getText().toString(), editHeight.getText().toString(), getGender());
    }

    private void setFieldError(TextInputLayout til, int resId) {
        til.setErrorEnabled(true);
        til.setError(getString(resId));
    }

    private boolean checkFields() {
        return tilEmail.isErrorEnabled() || tilPassword.isErrorEnabled() || tilName.isErrorEnabled() || tilHeight.isErrorEnabled();
    }

    private String getGender() {
        return (radioMale.isChecked()) ? "male" : "female";
    }


    @Override
    public void onSuccess() {
        layoutRegister.setVisibility(View.GONE);
        layoutDone.setVisibility(View.VISIBLE);
    }

    @Override
    public void onUploaded() {
        layoutUpload.setVisibility(View.GONE);
        btnCreate.setEnabled(true);
    }

    @Override
    public void showMessage(String msg) {
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showLoading(boolean state) {
        isLoading = state;
        if (state) {
            loadingSnackbar.show();
        } else {
            loadingSnackbar.dismiss();
        }
    }

    @OnClick(R.id.btn_done)
    void onClickDone() {
        onBackPressed();
    }

    @Override
    protected void onDestroy() {
        presenter.callScanFinish();
        super.onDestroy();
    }
}
