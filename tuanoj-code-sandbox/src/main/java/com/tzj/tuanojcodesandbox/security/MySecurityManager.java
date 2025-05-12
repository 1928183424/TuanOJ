package com.tzj.tuanojcodesandbox.security;

import java.security.Permission;

public class MySecurityManager extends SecurityManager{

    // 检查所有的权限
    @Override
    public void checkPermission(Permission perm) {
//        super.checkPermission(perm);
    }

    // 检查执行程序的权限
    @Override
    public void checkExec(String cmd) {
        super.checkExec(cmd);
        // throw new SecurityException("checkExec 权限异常：" + cmd);
    }

    // 检查读文件的权限
    @Override
    public void checkRead(String file) {
        super.checkRead(file);
        // throw new SecurityException("checkRead 权限异常：" + file);
    }

    // 检查写文件的权限
    @Override
    public void checkWrite(String file) {
        super.checkWrite(file);
        // throw new SecurityException("checkWrite 权限异常：" + file);
    }

    //  检查删除文件的权限
    @Override
    public void checkDelete(String file) {
        super.checkDelete(file);
        // throw new SecurityException("checkDelete 权限异常：" + file);
    }

    // 检查连接网络的权限
    @Override
    public void checkConnect(String host, int port) {
        super.checkConnect(host, port);
        // throw new SecurityException("checkConnect 权限异常：" + host + ":" + port);
    }
}
