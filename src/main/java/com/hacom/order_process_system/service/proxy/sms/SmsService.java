package com.hacom.order_process_system.service.proxy.sms;

public interface SmsService {
     void sendSms(String phoneNumber, String message);
}
