package io.github.payrexx.taptopayecr;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import io.github.payrexx.taptopay.sdk.TapToPay;
import io.github.payrexx.taptopay.sdk.dto.SaleDto;
import io.github.payrexx.taptopay.sdk.dto.TransactionHistoryDto;
import io.github.payrexx.taptopay.sdk.lib.operation.Sale;
import io.github.payrexx.taptopay.sdk.lib.operation.TransactionHistory;
import io.github.payrexx.taptopay.sdk.model.EmptyResponse;
import io.github.payrexx.taptopay.sdk.model.ListResponse;
import io.github.payrexx.taptopay.sdk.model.SingleResponse;
import io.github.payrexx.taptopay.sdk.model.transaction.Operation;
import io.github.payrexx.taptopay.sdk.model.transaction.Status;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TapToPay tapToPay = new TapToPay(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main),
            (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            }
        );

        Button submitButton = findViewById(R.id.Start);
        submitButton.setOnClickListener(v -> {
            EditText amountInput = findViewById(R.id.Amount);
            EditText amountTipInput = findViewById(R.id.TipAmount);
            EditText orderReferenceInput = findViewById(R.id.OrderReference);

            float amount = 0;
            try {
                amount = Float.parseFloat(amountInput.getText().toString());
            } catch (NumberFormatException e) {
               Log.d("TAPTOPAY", "Amount Invalid");
            }

            float tip = 0;
            try {
                tip = Float.parseFloat(amountTipInput.getText().toString());
            } catch (NumberFormatException e) {
                Log.d("TAPTOPAY", "Tip Invalid");
            }

            String orderReference = orderReferenceInput.getText().toString();

            SaleDto saleDto = new SaleDto(amount, tip, orderReference);

            tapToPay.doOperation(
                new Sale(saleDto),
                response -> {
                    if (
                        response instanceof EmptyResponse
                        || ((SingleResponse) response).transaction == null
                    ) {
                        Toast toast = new Toast(this);
                        toast.setText(R.string.payment_cancelled);
                        toast.show();
                    } else {
                        Intent intent = new Intent(getApplicationContext(), TransactionActivity.class);
                        intent.putExtra("response", response);
                        startActivity(intent);
                    }
                }
            );
        });

        Button historyButton = findViewById(R.id.History);
        historyButton.setOnClickListener(view -> {
            TransactionHistoryDto transactionHistoryDto = new TransactionHistoryDto(1, 20);
            tapToPay.doOperation(
                new TransactionHistory(transactionHistoryDto),
                result -> {
                    if (result instanceof ListResponse && !((ListResponse) result).items.isEmpty()) {
                        Intent intent = new Intent(getApplicationContext(), TransactionListActivity.class);
                        intent.putExtra("response", result);
                        startActivity(intent);
                    }
                }
            );
        });
    }
}