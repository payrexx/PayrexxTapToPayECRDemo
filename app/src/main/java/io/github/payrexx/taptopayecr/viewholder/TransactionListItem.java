package io.github.payrexx.taptopayecr.viewholder;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.github.payrexx.taptopay.sdk.model.SingleResponse;
import io.github.payrexx.taptopay.sdk.model.Transaction;
import io.github.payrexx.taptopayecr.R;
import io.github.payrexx.taptopayecr.TransactionActivity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class TransactionListItem extends RecyclerView.ViewHolder {

    private final View itemView;

    public TransactionListItem(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
    }

    public void setData(Transaction transaction) {
        Context context = this.itemView.getContext();
        ((TextView) this.itemView.findViewById(R.id.reference)).setText(transaction.orderReference);
        ((TextView) this.itemView.findViewById(R.id.amount)).setText(
            String.format("%s %s", transaction.currency, transaction.amount.toString())
        );
        LocalDateTime dateTime = LocalDateTime.parse(transaction.date, Transaction.dateTimeFormatter);
        ((TextView) this.itemView.findViewById(R.id.date)).setText(
            dateTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
        );
        String methodText = transaction.paymentMethod.toString();
        if (transaction.cardType != null && !transaction.cardType.isEmpty()) {
            methodText += " - " + transaction.cardType;
        }
        ((TextView) this.itemView.findViewById(R.id.method)).setText(methodText);
        String operationText;
        switch (transaction.operation) {
            case VOID:
            case REFUND:
                operationText = context.getString(R.string.payment_refund);
                break;
            case SALE:
            default:
                operationText = context.getString(R.string.payment_sale);
                break;
        }

        String statusText;
        switch (transaction.status) {
            case FAILED:
                statusText = context.getString(R.string.failed);
                break;
            case PENDING:
                statusText = context.getString(R.string.pending);
                break;
            case SUCCESSFUL:
            default:
                statusText = context.getString(R.string.successful);
                break;
        }

        ((TextView) this.itemView.findViewById(R.id.status)).setText(
            String.format("%s - %s", operationText, statusText)
        );
        LinearLayout item = this.itemView.findViewById(R.id.TransactionItem);
        item.setOnClickListener(view -> {
            Intent intent = new Intent(itemView.getContext(), TransactionActivity.class);
            SingleResponse response = SingleResponse.getInstance(transaction);
            intent.putExtra("response", response);
            itemView.getContext().startActivity(intent);
        });
    }
}
