package com.payrexx.taptopayecr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.payrexx.taptopayecr.dto.ActionDto;
import com.payrexx.taptopayecr.dto.Dto;
import com.payrexx.taptopayecr.dto.SaleDataDTO;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button submitButton = findViewById(R.id.Start);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText amountInput = findViewById(R.id.Amount);
                EditText amountTipInput = findViewById(R.id.TipAmount);
                EditText orderReferenceInput = findViewById(R.id.OrderReference);
                Gson gson = new Gson();

                SaleDataDTO dataDto = new SaleDataDTO();
                try {
                    // important: amount needs to be type of string
                    dataDto.amount = amountInput.getText().toString();
                } catch (NumberFormatException e) {
                    dataDto.amount = "0";
                }
                try {
                    // important: tip needs to be type of string
                    dataDto.tip = amountTipInput.getText().toString();
                } catch (NumberFormatException e) {
                    dataDto.tip = "0";
                }
                dataDto.order_reference = orderReferenceInput.getText().toString();

                ActionDto actionDto = new ActionDto();
                actionDto.operation = "sale";
                actionDto.data = dataDto;

                Dto dto = new Dto();
                // important: Payload needs to be a json object STRING
                dto.payload = gson.toJson(actionDto);


                String payload = gson.toJson(dto);
                // Payload will look like this:
                // {
                //     "payload": "{\"data\":{\"amount\":"9,5",\"order_reference\":\"Test\",\"show_result\":true,\"tip\":"0,5"},\"operation\":\"sale\"}",
                //     "signature": ""
                // }

                Intent intent = new Intent("com.payrexx.taptopay.SOFTPOS");
                intent.putExtra("com.payrexx.taptopay.CONFIGURATION", payload);
                startActivity(intent);
            }
        });
    }
}