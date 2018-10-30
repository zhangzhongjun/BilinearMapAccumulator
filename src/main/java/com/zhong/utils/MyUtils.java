package com.zhong.utils;


import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.*;

/**
 * Util
 *
 * @author 张中俊
 */
public class MyUtils {
    /**
     * 获得项目某文件夹下的某文件
     *
     * @param dirname
     *         文件夹名
     * @param fileName
     *         文件名
     *
     * @return 该文件File对象
     */
    public static File getFile(String dirname, String fileName) {
        String path = System.getProperty("user.dir");
        path = path + File.separator + dirname;
        File file = new File(path, fileName);
        return file;
    }
}
