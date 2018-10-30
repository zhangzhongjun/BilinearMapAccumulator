package com.zhong.accumulator;

import com.zhong.utils.MyUtils;
import com.zhong.utils.SerializationDemonstrator;
import it.unisa.dia.gas.jpbc.Element;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;

/**
 * 基于双线性对的聚合器的公钥
 * <br>有n个，g^s g^s^2  g^s^3 ....
 *
 * @author 张中俊
 **/
public class BilinearMapAccumulator_GongYao implements Serializable {
    /**
     * 公钥
     */
    private ArrayList<SerializableElement> hs = null;


    public BilinearMapAccumulator_GongYao(ArrayList<SerializableElement> hs) {
        this.hs = hs;
    }

    public ArrayList<SerializableElement> getHs() {
        return hs;
    }

    /**
     * 根据用户提供的私钥生成对应的公钥并序列化到文本文件中
     *
     * @param siYao
     *         私钥
     */
    public static void generateKey(BilinearMapAccumulator_SiYao siYao,int q) {
        Element g = siYao.getG().getElement().duplicate();
        Element s = siYao.getS().getElement().duplicate();

        SerializableElement h = new SerializableElement(g.duplicate());

        ArrayList<SerializableElement> hs = new ArrayList<>();
        hs.add(new SerializableElement(g));
        for(int i=1;i<=q;i++){
            Element t = s.duplicate().pow(new BigInteger(i+""));
            Element tt = g.duplicate().powZn(t.duplicate());
            hs.add(new SerializableElement(tt));
        }

        BilinearMapAccumulator_GongYao bk = new BilinearMapAccumulator_GongYao(hs);

        String bilinearMapAccumulator_KeyFile = MyUtils.getFile("keys", "BilinearMapAccumulator_GongYao.key").getAbsolutePath();
        SerializationDemonstrator.serialize(bk, bilinearMapAccumulator_KeyFile);
    }

    /**
     * 从文本文件中反序列化出公钥
     *
     * @return 公钥
     */
    public static BilinearMapAccumulator_GongYao getKey() {
        String bilinearMapAccumulator_KeyFile = MyUtils.getFile("keys", "BilinearMapAccumulator_GongYao.key").getAbsolutePath();
        return SerializationDemonstrator.deserialize(bilinearMapAccumulator_KeyFile);
    }
}
