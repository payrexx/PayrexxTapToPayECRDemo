package io.github.payrexx.taptopayecr.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.github.payrexx.taptopay.sdk.model.Transaction;
import io.github.payrexx.taptopayecr.R;
import io.github.payrexx.taptopayecr.viewholder.TransactionListItem;

import java.util.List;

public class TransactionListAdapter extends RecyclerView.Adapter<TransactionListItem> {
    private final List<Transaction> transactionList;

    public TransactionListAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionListItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transaction_list_item, parent, false);
        return new TransactionListItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionListItem holder, int position) {
        holder.setData(this.transactionList.get(position));
    }

    @Override
    public int getItemCount() {
        return this.transactionList.size();
    }
}
