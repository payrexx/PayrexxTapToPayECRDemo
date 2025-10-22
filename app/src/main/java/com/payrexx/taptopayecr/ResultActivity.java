package com.payrexx.taptopayecr;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ResultActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Bundle extras = getIntent().getExtras();
        TextView statusView = findViewById(R.id.status);
        TextView errorMessageView = (TextView) findViewById(R.id.error_message);
        errorMessageView.setVisibility(TextView.GONE);

        ImageView statusImage = findViewById(R.id.status_image);

        if (extras == null ) {
            finish();
            return;
        }

        String responseStr = extras.getString("responseStr");

        if (responseStr == null) {
            finish();
            return;
        }

        JsonObject response = JsonParser.parseString(responseStr).getAsJsonObject();
        JsonObject transaction = response.getAsJsonObject("transaction");

        if (transaction == null) {
            finish();
            return;
        }
        int status = transaction.get("status").getAsInt();
        if (status != 1) {
            statusView.setText(R.string.declined);
            statusImage.setImageResource(R.drawable.declined);

            String errorMessage = response.get("errorMsg").getAsString();

            errorMessageView.setVisibility(TextView.VISIBLE);
            errorMessageView.setText(errorMessage);
        } else {
            statusView.setText(R.string.approved);
            statusImage.setImageResource(R.drawable.approved);
        }

        TextView amount = (TextView) findViewById(R.id.amount);
        String amountText = transaction.get("currency").getAsString() + " " + transaction.get("amount").getAsString();
        amount.setText(amountText);

        TextView reference = (TextView) findViewById(R.id.reference);
        String orderRef = "Ref: " + transaction.get("order_reference").getAsString();
        reference.setText(orderRef);

        Button back = (Button) findViewById(R.id.Back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
