package me.arnoldwho.hongdou;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    private OutputStream outputStream;
    public Socket socket;
    MySocket mySocket = new MySocket();
    String response;
    int flags = 1;

    @BindView(R.id.input_name) EditText _nameText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.btn_signup) Button _signupButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        new Thread(connect).start();

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("value");
            if (val.equals("Yes")){
                onSignupSuccess();
            }
            else if (val.equals("No")){
                //final int flags = 0;
                onSignupFailed();
            }
        }
    };

    Runnable connect = new Runnable() {
        @Override
        public void run() {
            try{
                socket = new Socket("45.63.91.170", 20566);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    Runnable loginsocket = new Runnable() {
        @Override
        public void run() {

            if (!socket.isConnected()){
                new Thread(connect).start();
            }
            final String name = _nameText.getText().toString();
            final String password = _passwordText.getText().toString();

            response = mySocket.getResponse("/login", socket);
            if (response.equals("/sure")){
                String str = name + " " + password;
                response = mySocket.getResponse(str, socket);
                if (response.equals("/Successed")){
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putString("value", "Yes");
                    msg.setData(data);
                    handler.sendMessage(msg);
                }
                else {
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putString("value", "No");
                    msg.setData(data);
                    handler.sendMessage(msg);
                }
            }
        }
    };


    public void login() {
        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loging...");
        progressDialog.show();
        new Thread(loginsocket).start();
        if (flags == 0){
            progressDialog.dismiss();}
    }


    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed() {
        flags = 0;
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String password = _passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
}