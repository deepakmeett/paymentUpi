package com.example.paymentgateway;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
public class MainActivity extends AppCompatActivity {

    EditText amountEt, upiEt, nameEt, noteEt;
    Button sendButton;
    final int UPI_PAYMENT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        amountEt = findViewById( R.id.amount );
        upiEt = findViewById( R.id.upi );
        nameEt = findViewById( R.id.name );
        noteEt = findViewById( R.id.note );
        sendButton = findViewById( R.id.sendUpi );
        sendButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amount = amountEt.getText().toString();
                String upi = upiEt.getText().toString();
                String name = nameEt.getText().toString();
                String note = noteEt.getText().toString();
                payUsingUpi( amount, upi, name, note );
            }
        } );
    }

    private void payUsingUpi(String amount, String upi, String name, String note) {
        Uri uri = Uri.parse( "upi://pay" ).buildUpon()
                .appendQueryParameter( "pa", upi )
                .appendQueryParameter( "pn", name )
                .appendQueryParameter( "tn", note )
                .appendQueryParameter( "am", amount )
                .appendQueryParameter( "cu", "INR" )
                .build();
        Intent intent = new Intent( Intent.ACTION_VIEW );
        intent.setData( uri );
        Intent chooser = Intent.createChooser( intent, "Pay with" );
        if (null != chooser.resolveActivity( getPackageManager() )) {
            startActivityForResult( chooser, UPI_PAYMENT );
        } else {
            Toast.makeText( this, "No UPI app found, Please install one to continue", Toast.LENGTH_SHORT ).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        ArrayList<String> dataList = new ArrayList<>();
        if (requestCode == UPI_PAYMENT && RESULT_OK == resultCode) {
            if (data != null) {
                String trxt = data.getStringExtra( "response" );
                dataList.add( trxt );
                upiPaymentDataOperation( dataList );
            } else {
                dataList.add( "Nothing" );
                upiPaymentDataOperation( dataList );
            }

        }
    }

    private void upiPaymentDataOperation(ArrayList<String> data) {
        String str = data.get( 0 );
        String paymentCancel = "";
        if (str == null) str = "discard";
        String status = "";
        String approvalno = "";
        String res[] = str.split( "&" );
        for (String re : res) {
            String equalsStr[] = re.split( "=" );
            if (equalsStr.length >= 2) {
                if (equalsStr[0].toLowerCase().equals( "Status".toLowerCase() )) {
                    status = equalsStr[1].toLowerCase();
                } else if (equalsStr[0].toLowerCase().equals( "ApprovalRefNo".toLowerCase() ) ||
                           equalsStr[0].toLowerCase().equals( "txnRef".toLowerCase() )) {
                    approvalno = equalsStr[1];
                } else {
                    paymentCancel = "payment cancelled by User";
                }
            }
            if (status.equals( "success" )) {
                Toast.makeText( this, "Transaction Successful", Toast.LENGTH_SHORT ).show();
            } else if (paymentCancel.equals( "payment cancelled by User" )) {
                Toast.makeText( this, "Payment cancelled by user", Toast.LENGTH_SHORT ).show();
            } else {
                Toast.makeText( this, "Transaction failed. Please try again", Toast.LENGTH_SHORT ).show();
            }
        }

    }

}
