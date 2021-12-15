package com.tool.bean;

import net.galasports.support.pub.bean.proto.BaseProtos;

import java.util.List;

public class AccountInfo {
    private List<TestAccount> testAccounts;
    private List<ChooseServerInfo> chooseServerInfos;

    public List<TestAccount> getTestAccounts() {
        return testAccounts;
    }

    public void setTestAccounts(List<TestAccount> testAccounts) {
        this.testAccounts = testAccounts;
    }

    public List<ChooseServerInfo> getChooseServerInfos() {
        return chooseServerInfos;
    }

    public void setChooseServerInfos(List<ChooseServerInfo> chooseServerInfos) {
        this.chooseServerInfos = chooseServerInfos;
    }
}
