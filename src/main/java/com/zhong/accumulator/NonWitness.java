package com.zhong.accumulator;

import java.io.Serializable;

/**
 * 元素不在集合中的witness
 *
 * @author 张中俊
 */

public class NonWitness implements Serializable {
    SerializableElement Uy;
    SerializableElement Wy;

    public NonWitness(SerializableElement uy, SerializableElement wy) {
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
