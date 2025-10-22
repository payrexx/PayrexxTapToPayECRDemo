package com.payrexx.taptopayecr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.payrexx.taptopayecr.dto.ActionDto;
import com.payrexx.taptopayecr.dto.Dto;
import com.payrexx.taptopayecr.dto.SaleDataDTO;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityResultLauncher<Intent> tapToPayLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // handle activity results
                if (result.getResultCode() != Activity.RESULT_OK) {
                    return;
                }

                Intent data = result.getData();
                if (data == null) {
                    return;
                }

                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }

                String resultStr = bundle.getString("com.payrexx.taptopay.RESULT");
                if (resultStr == null) {
                    return;
                }

                JsonObject resultObject = JsonParser.parseString(resultStr).getAsJsonObject();
                String payloadStr = resultObject.get("payload").getAsString();

                JsonObject payload = JsonParser.parseString(payloadStr).getAsJsonObject();
                JsonObject responseBody = payload.get("responseBody").getAsJsonObject();
                /*
                  responseBody:
                  {
                    "transaction" : {
                      "isTransaction3D" : false,
                      "status" : <1 | -1>,
                      "operation" : "sale",
                      "add_date" : "2025-10-22T18:10:32UTC",
                      "amount" : 1,
                      "tip_amount" : 0,
                      "currency" : "CHF",
                      "voidable" : false,
                      "refundable_amount" : 0,
                      "sca_type" : 1,
                      "retrieval_reference_number" : "<internal reference>",
                      "surcharge_amount" : 0,
                      "payment_method" : 0,
                      "instalments_number" : 0,
                      "order_reference" : "<your custom order_reference>",
                      "transaction_key" : "<internal reference>",
                      "stan" : "000038",
                      "auth_code" : "000000",
                      "application_id" : "<internal id>",
                      "card" : "<anonymised card number>",
                      "card_type" : "VISA",
                      "action_code" : 96,
                      "message" : ""
                    },
                    "mid" : "<mid>",
                    "tid" : "<tid>",
                    "extras" : {
                      "order_reference" : "<your custom order_reference>"
                    },
                    "errorMsg" : "<detailed error message>"
                  }
                 */

                Intent intent;
                intent = new Intent(this, ResultActivity.class);
                intent.putExtra("responseStr", responseBody.toString());
                startActivity(intent);
            }
        );

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
                tapToPayLauncher.launch(intent);
            }
        });
    }
}