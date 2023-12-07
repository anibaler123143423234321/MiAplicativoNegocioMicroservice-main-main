package com.dagnerchuman.miaplicativonegociomicroservice.entity;

public class EmailValuesDto {
    private String mailTo;

    public EmailValuesDto(String mailTo) {
        this.mailTo = mailTo;
    }

    public String getMailTo() {
        return mailTo;
    }

    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }
}
