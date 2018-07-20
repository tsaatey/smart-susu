package com.artlib.smartsusu;

public class ProcessPayment {

    private String paymentUrl;
    private String primaryCallbackUrl;
    private String merchantAccount;

    public ProcessPayment(PaymentParams paymentParams) {
        primaryCallbackUrl = "https://smartsusu-32864.firebaseio.com/";
        merchantAccount = "HM2409170003";
        paymentUrl = "https://api.hubtel.com/merchants/" + merchantAccount + "/receive/mobilemoney";
    }


}
