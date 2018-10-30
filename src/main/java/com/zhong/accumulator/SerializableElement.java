package com.zhong.accumulator;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Zr中的元素 长度为 20byte
 * <br>G1中的元素 长度为 128byte
 * <br>G2中的元素 长度为 128byte
 * <br>GT中的元素 长度为 128byte
 * <br>我们只处理G1 Zr中的元素，所以我是使用字节长度区分的
 * <br>但是如果涉及到了GT中的元素，则序列化会出错
 *
 * @author 张中俊
 */
public class SerializableElement implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Element element;

    public SerializableElement(final Element element) {
        super();
        this.element = element;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    @Override
    public String toString() {
        return this.element.toString();
    }

    /**
     * Serialize this instance.
     *
     * @param out
     *         Target to which this instance is written.
     *
     * @throws IOException
     *         Thrown if exception occurs during serialization.
     */
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.write(this.element.toBytes());
    }

    /**
     * Deserialize this instance from input stream.
     *
     * @param in
     *         Input Stream from which this instance is to be deserialized.
     *
     * @throws IOException
     *         Thrown if error occurs in deserialization.
     * @throws ClassNotFoundException
     *         Thrown if expected class is not found.
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        /**
         * 双线性对
         */
        Pairing pairing = PairingFactory.getPairing("params/curves/a.properties");
        byte[] arr = IOUtils.toByteArray(in);
        if (arr.length == 20) {
            this.element = pairing.getZr().newRandomElement();
        } else if (arr.length == 128) {
            this.element = pairing.getG1().newRandomElement();
        } else {
            throw new IOException("序列化出错，请检查你的元素属于那个域");
        }
        this.element.setFromBytes(arr);
    }
}
