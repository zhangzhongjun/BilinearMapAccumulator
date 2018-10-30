package com.zhong.accumulator;

/**
 * 元素或子集的witness
 *
 * @author 张中俊
 */

import java.io.Serializable;

/**
 * 元素在集合中的证据
 *
 * @author 张中俊
 */
public class Witness implements Serializable {
    SerializableElement Uy;
    SerializableElement Wy;

    public Witness(SerializableElement uy, SerializableElement wy) {
        Uy = uy;
        Wy = wy;
    }

    public SerializableElement getUy() {
        return Uy;
    }

    public SerializableElement getWy() {
        return Wy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("====您正在查看witness中的内容====");
        sb.append("\t" + "Uy= " + this.Uy.toString() + System.lineSeparator());
        sb.append("\t" + "Wy= " + this.Wy.toString() + System.lineSeparator());
        return sb.toString();
    }
}

