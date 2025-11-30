package io.github.payrexx.taptopayecr;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.github.payrexx.taptopay.sdk.TapToPay;
import io.github.payrexx.taptopay.sdk.model.ListResponse;
import io.github.payrexx.taptopayecr.R;

import io.github.payrexx.taptopayecr.adapter.TransactionListAdapter;
import io.github.payrexx.taptopayecr.viewholder.TransactionListItem;

public class TransactionListActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);
        Bundle extras = getIntent().getExtras();
        if (extras == null ) {
            finish();
            return;
        }

        ListResponse response = (ListResponse) extras.getSerializable("response");

        if (response == null) {
            finish();
            return;
        }

        RecyclerView recyclerView = findViewById(R.id.TransactionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.Adapter<TransactionListItem> adapter = new TransactionListAdapter(response.items);
        recyclerView.setAdapter(adapter);

        Button back = findViewById(R.id.Back);
        back.setOnClickListener(view -> finish());
    }
}
