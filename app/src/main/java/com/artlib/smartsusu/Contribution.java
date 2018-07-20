package com.artlib.smartsusu;

public class Contribution {

    private String contributionDate;
    private String amountContributed;

    public Contribution(String contributionDate, String amountContributed) {
        this.contributionDate = contributionDate;
        this.amountContributed = amountContributed;
    }

    public String getContributionDate() {
        return contributionDate;
    }

    public String getAmountContributed() {
        return amountContributed;
    }
}
