package com.zhong.accumulator;

import com.zhong.utils.MyUtils;
import com.zhong.utils.SerializationDemonstrator;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.io.Serializable;

/**
 * 基于双线性对的聚合器的公钥
 * <br>包括一个生成元和一个私钥s
 *
 * @author 张中俊
 */

public class BilinearMapAccumulator_SiYao implements Serializable {
    /**
     * 群的生成元
     */
    private SerializableElement g = null;
    /**
     * 私钥
     */
    private SerializableElement s = null;

    public BilinearMapAccumulator_SiYao(SerializableElement g, SerializableElement s) {
        this.g = g;
        this.s = s;
    }

    /**
     * 生成Key并序列化到文本文件中
     */
    public static void generateKey() {
        /**
         * 双线性对
         */
        Pairing pairing = PairingFactory.getPairing("params/curves/a.properties");
        // 从G1群中随机选一个元素 生成元
        SerializableElement g = new SerializableElement(pairing.getG1().newRandomElement());
        // 从Zr群中随机选择一个元素 私钥
        SerializableElement s = new SerializableElement(pairing.getZr().newRandomElement());

        BilinearMapAccumulator_SiYao bk = new BilinearMapAccumulator_SiYao(g, s);

        String bilinearMapAccumulator_KeyFile = MyUtils.getFile("keys", "BilinearMapAccumulator_SiYao.key").getAbsolutePath();
        SerializationDemonstrator.serialize(bk, bilinearMapAccumulator_KeyFile);
    }

    /**
     * 从文件中反序列化出私钥并返回
     *
     * @return 私钥
     */
    public static BilinearMapAccumulator_SiYao getKey() {
        String bilinearMapAccumulator_KeyFile = MyUtils.getFile("keys", "BilinearMapAccumulator_SiYao.key").getAbsolutePath();
        return SerializationDemonstrator.deserialize(bilinearMapAccumulator_KeyFile);
    }

    public SerializableElement getG() {
        return g;
    }

    public SerializableElement getS() {
        return s;
    }
}


