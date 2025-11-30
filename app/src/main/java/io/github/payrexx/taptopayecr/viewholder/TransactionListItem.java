package io.github.payrexx.taptopayecr.viewholder;

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

    View itemView;

    public TransactionListItem(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
    }

    public void setData(Transaction transaction) {
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
        ((TextView) this.itemView.findViewById(R.id.status)).setText(
            String.format("%s - %s", transaction.operation.toString(), transaction.status.toString())
        );
        LinearLayout item = this.itemView.findViewById(R.id.TransactionItem);
        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(itemView.getContext(), TransactionActivity.class);
                SingleResponse response = SingleResponse.getInstance(transaction);
                intent.putExtra("response", response);
                itemView.getContext().startActivity(intent);
            }
        });
    }
}
