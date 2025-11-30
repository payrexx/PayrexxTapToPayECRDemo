package io.github.payrexx.taptopayecr;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.github.payrexx.taptopay.sdk.TapToPay;
import io.github.payrexx.taptopay.sdk.dto.ReceiptDto;
import io.github.payrexx.taptopay.sdk.dto.RefundDto;
import io.github.payrexx.taptopay.sdk.dto.VoidDto;
import io.github.payrexx.taptopay.sdk.lib.operation.Receipt;
import io.github.payrexx.taptopay.sdk.lib.operation.Refund;
import io.github.payrexx.taptopay.sdk.lib.operation.Void;
import io.github.payrexx.taptopay.sdk.model.EmptyResponse;
import io.github.payrexx.taptopay.sdk.model.SingleResponse;
import io.github.payrexx.taptopay.sdk.model.transaction.Operation;
import io.github.payrexx.taptopay.sdk.model.transaction.Status;

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
        String paymentType;
        if (response.transaction.operation == Operation.SALE) {
            paymentType = this.getString(R.string.payment_sale);
        } else {
            paymentType = this.getString(R.string.payment_refund);
        }

        if (response.transaction.status != Status.SUCCESSFUL) {
            statusView.setText(
                String.format("%s %s", paymentType, this.getString(R.string.failed))
            );
            statusImage.setImageResource(R.drawable.declined);

            String errorMessage = response.message;

            errorMessageView.setVisibility(VISIBLE);
            errorMessageView.setText(errorMessage);
        } else {
            statusView.setText(
                String.format("%s %s", paymentType, this.getString(R.string.approved))
            );
            statusImage.setImageResource(R.drawable.approved);
        }

        TextView amount = findViewById(R.id.amount);
        String amountText = response.transaction.currency + " " + response.transaction.amount;
        amount.setText(amountText);

        TextView reference = findViewById(R.id.reference);
        String orderRef = "Ref: " + response.extras.orderReference;
        reference.setText(orderRef);

        Button refund = findViewById(R.id.Refund);
        if (response.transaction.refundableAmount <= 0) {
            refund.setVisibility(GONE);
        } else {
            refund.setVisibility(VISIBLE);
        }
        refund.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(TransactionActivity.this);
            builder.setView(R.layout.refund_dialog);
            builder.setTitle(R.string.refund);
            builder.setNegativeButton(R.string.cancel,
                (dialogInterface, i) -> dialogInterface.cancel()
            );
            builder.setPositiveButton(R.string.refund, (dialogInterface, i) -> {
                AlertDialog dialog = (AlertDialog) dialogInterface;
                EditText editText = dialog.findViewById(R.id.Amount);
                Float amount1 = Float.parseFloat(editText.getText().toString());
                if (response.transaction.refundableAmount < amount1) {
                    Toast toast = new Toast(TransactionActivity.this);
                    toast.setText(R.string.refund_amount_too_high);
                    toast.show();
                    return;
                }
                if (response.transaction.voidable && amount1.equals(response.transaction.amount)) {
                    VoidDto voidDto = new VoidDto(response.transaction.id);
                    tapToPay.doOperation(
                        new Void(voidDto),
                        result -> {
                            if (!(result instanceof EmptyResponse)) {
                                SingleResponse response1 = (SingleResponse) result;
                                if (response1.transaction != null) {
                                    Intent intent = new Intent(
                                        TransactionActivity.this,
                                        TransactionActivity.class
                                    );
                                    intent.putExtra("response", response1);
                                    startActivity(intent);
                                    finish();
                                    return;
                                }
                            }
                            Toast toast = new Toast(TransactionActivity.this);
                            toast.setText(R.string.refund_failed);
                            toast.show();
                        }
                    );
                } else {
                    RefundDto refundDto = new RefundDto(response.transaction.id, amount1);
                    tapToPay.doOperation(
                        new Refund(refundDto),
                        result -> {
                            if (!(result instanceof EmptyResponse)) {
                                SingleResponse response1 = (SingleResponse) result;
                                if (response1.transaction != null) {
                                    Intent intent = new Intent(TransactionActivity.this, TransactionActivity.class);
                                    intent.putExtra("response", response1);
                                    startActivity(intent);
                                    finish();
                                    return;
                                }
                            }
                            Toast toast = new Toast(TransactionActivity.this);
                            toast.setText(R.string.refund_failed);
                            toast.show();
                        }
                    );
                }
                dialogInterface.cancel();
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        Button sendReceipt = findViewById(R.id.SendReceipt);
        sendReceipt.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(TransactionActivity.this);
            builder.setView(R.layout.receipt_dialog);
            builder.setTitle(R.string.send_receipt);
            builder.setNegativeButton(R.string.send_sms, (dialogInterface, i) -> {
                AlertDialog dialog = (AlertDialog) dialogInterface;
                EditText editText = dialog.findViewById(R.id.EmailSms);
                ReceiptDto receiptDto = new ReceiptDto(response.transaction.id, "sms", editText.getText().toString());
                tapToPay.doOperation(
                    new Receipt(receiptDto),
                    result -> {
                        String message = getString(R.string.receipt_sent);
                        if (result instanceof EmptyResponse || ((SingleResponse)result).message.equals("Failure")) {
                            message = getString(R.string.receipt_send_failed);
                        }
                        Toast toast = new Toast(TransactionActivity.this);
                        toast.setText(message);
                        toast.show();
                    }
                );
                dialogInterface.cancel();
            });
            builder.setPositiveButton(R.string.send_email, (dialogInterface, i) -> {
                AlertDialog dialog = (AlertDialog) dialogInterface;
                EditText editText = dialog.findViewById(R.id.EmailSms);
                ReceiptDto receiptDto = new ReceiptDto(response.transaction.id, "email", editText.getText().toString());
                tapToPay.doOperation(
                    new Receipt(receiptDto),
                    result -> {
                        String message = getString(R.string.receipt_sent);
                        if (result instanceof EmptyResponse || ((SingleResponse)result).message.equals("Failure")) {
                            message = getString(R.string.receipt_send_failed);
                        }
                        Toast toast = new Toast(TransactionActivity.this);
                        toast.setText(message);
                        toast.show();
                    }
                );
                dialogInterface.cancel();
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        Button back = findViewById(R.id.Back);
        back.setOnClickListener(v -> finish());
    }
}
