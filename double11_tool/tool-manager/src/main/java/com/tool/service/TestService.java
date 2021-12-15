package com.tool.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestService implements ITestService {
    @Override
    public void test() {

    }

    @Transactional
    public void test2() {

    }
}
