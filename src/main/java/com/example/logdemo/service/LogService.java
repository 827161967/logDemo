package com.example.logdemo.service;

import com.example.logdemo.bean.LogDTO;

public interface LogService {

    boolean createLog(LogDTO logDTO) throws Exception;

}
