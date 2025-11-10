package com.payrexx.taptopayecr;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.payrexx.taptopay.sdk.TapToPay;
import com.payrexx.taptopay.sdk.dto.ReceiptDto;
import com.payrexx.taptopay.sdk.dto.RefundDto;
import com.payrexx.taptopay.sdk.dto.VoidDto;
import com.payrexx.taptopay.sdk.lib.operation.Receipt;
import com.payrexx.taptopay.sdk.lib.operation.Refund;
import com.payrexx.taptopay.sdk.lib.operation.Void;
import com.payrexx.taptopay.sdk.model.EmptyResponse;
import com.payrexx.taptopay.sdk.model.SingleResponse;
import com.payrexx.taptopay.sdk.model.Transaction;
import com.payrexx.taptopay.sdk.model.transaction.Status;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TransactionActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        Bundle extras = getIntent().getExtras();
        TextView statusView = findViewById(R.id.status);
        TextView errorMessageView = findViewById(R.id.error_message);
        errorMessageView.setVisibility(GONE);

        ImageView statusImage = findViewById(R.id.status_image);

        if (extras == null ) {
            finish();
            return;
        }

        SingleResponse response = (SingleResponse) extras.getSerializable("response");

        if (response == null) {
            finish();
            return;
        }

        TapToPay tapToPay = new TapToPay(this);

        if (response.transaction.status != Status.SUCCESSFUL) {
            statusView.setText(R.string.declined);
            statusImage.setImageResource(R.drawable.declined);

            String errorMessage = response.message;

            errorMessageView.setVisibility(VISIBLE);
            errorMessageView.setText(errorMessage);
        } else {
            statusView.setText(R.string.approved);
            statusImage.setImageResource(R.drawable.approved);
        }

        TextView amount = findViewById(R.id.amount);
        String amountText = response.transaction.currency + " " + response.transaction.amount;
        amount.setText(amountText);

        TextView reference = findViewById(R.id.reference);
        String orderRef = "Ref: " + response.extras.orderReference;
        reference.setText(orderRef);

        Button refund = findViewById(R.id.Refund);
        if (response.transaction.refundableAmount.floatValue() <= 0) {
            refund.setVisibility(GONE);
        } else {
            refund.setVisibility(VISIBLE);
        }
        refund.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TransactionActivity.this);
                builder.setView(R.layout.refund_dialog);
                builder.setTitle("Refund");
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.setPositiveButton("Refund", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AlertDialog dialog = (AlertDialog) dialogInterface;
                        EditText editText = dialog.findViewById(R.id.Amount);
                        Float amount = Float.parseFloat(editText.getText().toString());
                        if (response.transaction.refundableAmount.floatValue() < amount.floatValue()) {
                            Toast toast = new Toast(TransactionActivity.this);
                            toast.setText("Amount higher than refundable amount");
                            toast.show();
                            return;
                        }
                        if (response.transaction.voidable && amount.equals(response.transaction.amount)) {
                            VoidDto voidDto = new VoidDto(response.transaction.id);
                            tapToPay.doOperation(
                                new Void(voidDto),
                                result -> {
                                    String message = "Refunded Successfully";
                                    if (result instanceof EmptyResponse || ((SingleResponse)result).message.equals("Failure")) {
                                        message = "Failed to refund";
                                    }
                                    Toast toast = new Toast(TransactionActivity.this);
                                    toast.setText(message);
                                    toast.show();
                                }
                            );
                        } else {
                            RefundDto refundDto = new RefundDto(response.transaction.id, amount);
                            tapToPay.doOperation(
                                new Refund(refundDto),
                                result -> {
                                    String message = "Refunded Successfully";
                                    if (result instanceof EmptyResponse || ((SingleResponse)result).message.equals("Failure")) {
                                        message = "Failed to refund";
                                    }
                                    Toast toast = new Toast(TransactionActivity.this);
                                    toast.setText(message);
                                    toast.show();
                                }
                            );
                        }
                        dialogInterface.cancel();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        Button sendReceipt = findViewById(R.id.SendReceipt);
        sendReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TransactionActivity.this);
                builder.setView(R.layout.receipt_dialog);
                builder.setTitle("Send Receipt to");
                builder.setNegativeButton("Send SMS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AlertDialog dialog = (AlertDialog) dialogInterface;
                        EditText editText = dialog.findViewById(R.id.EmailSms);
                        ReceiptDto receiptDto = new ReceiptDto(response.transaction.id, "sms", editText.getText().toString());
                        tapToPay.doOperation(
                                new Receipt(receiptDto),
                                result -> {
                                    String message = "Receipt Sent";
                                    if (result instanceof EmptyResponse || ((SingleResponse)result).message.equals("Failure")) {
                                        message = "Failed to send Receipt";
                                    }
                                    Toast toast = new Toast(TransactionActivity.this);
                                    toast.setText(message);
                                    toast.show();
                                }
                        );
                        dialogInterface.cancel();
                    }
                });
                builder.setPositiveButton("Send Email", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AlertDialog dialog = (AlertDialog) dialogInterface;
                        EditText editText = dialog.findViewById(R.id.EmailSms);
                        ReceiptDto receiptDto = new ReceiptDto(response.transaction.id, "email", editText.getText().toString());
                        tapToPay.doOperation(
                                new Receipt(receiptDto),
                                result -> {
                                    String message = "Receipt Sent";
                                    if (result instanceof EmptyResponse || ((SingleResponse)result).message.equals("Failure")) {
                                        message = "Failed to send Receipt";
                                    }
                                    Toast toast = new Toast(TransactionActivity.this);
                                    toast.setText(message);
                                    toast.show();
                                }
                        );
                        dialogInterface.cancel();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        Button back = findViewById(R.id.Back);
        back.setOnClickListener(v -> finish());
    }
}
