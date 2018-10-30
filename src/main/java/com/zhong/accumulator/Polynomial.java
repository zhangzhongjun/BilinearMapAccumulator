package com.zhong.accumulator;


import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.EnumMap;

/**
 * 多项式运算
 */
public class Polynomial {
    // 从文件a1.properties中读取参数初始化双线性群
    private static Pairing pairing = null;

    static {
        pairing = PairingFactory.getPairing("params/curves/a.properties");
    }

    /**
     * 计算多项式的值
     * 内部逻辑是一个简单递归
     *
     * @param para 多项数参数组成的数组 para[0]是常数项，para[1]是一次项，以此类推
     * @param x    变量的数值
     * @return 变量值对应的多项式值
     */
    public static double polynomial1D(double[] para, double x) {
        double result = 0;
        for (int i = para.length - 1; i >= 0; i--) {
            result = result * x + para[i];
        }
        return result;
    }

    /**
     * 多项式乘法
     *
     * @param a 第一个多项式的系数， para[0]是常数项，para[1]是一次项
     * @param b 第二个多项式的系数， para[0]是常数项，para[1]是一次项
     * @return 相乘后的多项式系数数组
     */
    public static double[] polynomial_mul(double[] a, double[] b) {
        int na = a.length;
        int nb = b.length;
        double[] r = new double[na + nb - 1];//新建一个乘积多项式参数数组，数组元素的个数是na+nb-1
        for (int i = 0; i < na + nb - 1; i++) {
            r[i] = 0.0;//逐个初始化
        }
        for (int j = 0; j < na; j++) {
            for (int k = 0; k < nb; k++) {
                r[j + k] += a[j] * b[k];//逐个相乘，累加
            }
        }
        return r;
    }

    /**
     * 多项式乘法
     *
     * @param a 第一个多项式的系数， para[0]是常数项，para[1]是一次项
     * @param b 第二个多项式的系数， para[0]是常数项，para[1]是一次项
     * @return 计算结果多项式系数数组
     */
    public static Element[] polynomial_mul(Element[] a, Element[] b) {
        int na = a.length;
        int nb = b.length;
        Element[] r = new Element[na + nb - 1];//新建一个乘积多项式参数数组，数组元素的个数是na+nb-1
        for (int i = 0; i < na + nb - 1; i++) {
            r[i] = pairing.getZr().newZeroElement();//逐个初始化
        }
        for (int j = 0; j < na; j++) {
            for (int k = 0; k < nb; k++) {
                r[j + k] = r[j + k].duplicate().add(a[j].duplicate().mul(b[k].duplicate()));//逐个相乘，累加
            }
        }
        return r;
    }

    /**
     * 多项式除法另一种实现，返回由商和余数多项式系数数组组成的EnumMap
     * 这样做是为了解决同时输出两个对象的需求，好处是不需要在调用方法时认为定义并计算商和余数多项数参数数组
     * EnumMap比较少见，实质上实现和定义一个处罚结果类相似
     * 代码依然不算优雅
     *
     * @param a 第一个多项式的系数， para[0]是常数项，para[1]是一次项
     * @param b 第二个多项式的系数， para[0]是常数项，para[1]是一次项
     * @return 计算结果多项式系数数组
     */
    public static EnumMap<DIV, double[]> polynomial_div(double[] a, double[] b) {
        int na = a.length;
        int nb = b.length;
        int nr = na - nb + 1;
        int nl = nb - 1;

        double[] r = new double[a.length - b.length + 1];
        double[] l = new double[b.length - 1];
        int ta, tb;
        ta = na - 1;
        for (int i = 0; i < nr; i++) {
            r[i] = 0.0;
        }
        for (int i = nr; i > 0; i--) {
            r[i - 1] = a[ta] / b[nb - 1];
            tb = ta;
            for (int j = 1; j <= nb - 1; j++) {
                a[tb - 1] -= r[i - 1] * b[nb - j - 1];
                tb -= 1;
            }
            ta -= 1;
        }
        for (int i = 0; i < nl; i++) {
            l[i] = a[i];
        }

        EnumMap<DIV, double[]> enumMap = new EnumMap<DIV, double[]>(DIV.class);
        enumMap.put(DIV.SHANG, r);
        enumMap.put(DIV.YUSHU, l);
        return enumMap;
    }

    /**
     * 多项式除法另一种实现，返回由商和余数多项式系数数组组成的EnumMap
     * 这样做是为了解决同时输出两个对象的需求，好处是不需要在调用方法时认为定义并计算商和余数多项数参数数组
     * EnumMap比较少见，实质上实现和定义一个处罚结果类相似
     * 代码依然不算优雅
     *
     * @param a 第一个多项式的系数， para[0]是常数项，para[1]是一次项
     * @param b 第二个多项式的系数， para[0]是常数项，para[1]是一次项
     * @return 计算结果多项式系数数组
     */
    public static EnumMap<DIV, Element[]> polynomial_div(Element[] a, Element[] b) {
        int na = a.length;
        int nb = b.length;
        int nr = na - nb + 1;
        int nl = nb - 1;

        Element[] r = new Element[a.length - b.length + 1];
        Element[] l = new Element[b.length - 1];
        int ta, tb;
        ta = na - 1;
        for (int i = 0; i < nr; i++) {
            r[i] = pairing.getG1().newZeroElement();
        }
        for (int i = nr; i > 0; i--) {
            r[i - 1] = a[ta].duplicate().div(b[nb - 1].duplicate());
            tb = ta;
            for (int j = 1; j <= nb - 1; j++) {
                a[tb - 1] = a[tb - 1].duplicate().sub(r[i - 1].duplicate().mul(b[nb - j - 1].duplicate()));
                tb -= 1;
            }
            ta -= 1;
        }
        for (int i = 0; i < nl; i++) {
            l[i] = a[i];
        }

        EnumMap<DIV, Element[]> enumMap = new EnumMap<DIV, Element[]>(DIV.class);
        enumMap.put(DIV.SHANG, r);
        enumMap.put(DIV.YUSHU, l);
        return enumMap;
    }

    /**
     * 多项式运算 多项式为 [（s+p1[1]）（s+p1[2]）（s+p1[3]）+ p2]/(s+p3)
     *
     * @param p1 第一个参数
     * @param p2 第二个参数
     * @param p3 第三个参数
     */
    public static void poly(double[] p1, double p2, double p3) {
        // 构造出 s+p1[0] 的多项式
        double[] r = {p1[0], 1};
        for (int i = 1; i < p1.length; i++) {
            // 构造出 s+p1[i] 的多项式
            double a[] = {p1[i], 1};
            r = polynomial_mul(r, a);
        }
        r[0] = r[0] + p2;

        // 构造出 s+p3 的多项式
        double[] n = {p3, 1};

        EnumMap<DIV, double[]> divResultMap = polynomial_div(r, n);
        double[] rr = divResultMap.get(DIV.SHANG);
        double[] ll = divResultMap.get(DIV.YUSHU);
        for (double d : rr) {
            System.out.print(d + " ");
        }
        System.out.println();
    }

    /**
     * 多项式运算 多项式为 [（s+p1[1]）（s+p1[2]）（s+p1[3]）+ p2]/(s+p3)
     *
     * @param p1 第一个参数
     * @param p2 第二个参数
     * @param p3 第三个参数
     */
    public static Element[] poly(Element[] p1, Element p2, Element p3) {
        Element[] r = {p1[0], pairing.getZr().newOneElement()};
        for (int i = 1; i < p1.length; i++) {
            Element a[] = {p1[i], pairing.getZr().newOneElement()};
            r = polynomial_mul(r, a);
        }
        r[0] = r[0].duplicate().add(p2.duplicate());

        Element[] n = {p3, pairing.getZr().newOneElement()};

        EnumMap<DIV, Element[]> divResultMap = polynomial_div(r, n);
        Element[] rr = divResultMap.get(DIV.SHANG);
        //Element[] ll=divResultMap.get(DIV.YUSHU);
        //for(Element d:rr){
        //    System.out.print(d+" ");
        //}
        //System.out.println();
        return rr;
    }

    /**
     * 多项式运算 计算（s+p1[1]）（s+p1[2]）（s+p1[3]）
     *
     * @param p1 多项式
     * @return 计算结果
     */
    public static Element[] poly(Element[] p1) {
        // 构造 （s+p1[0]）
        Element[] r = {p1[0], pairing.getZr().newOneElement()};
        for (int i = 1; i < p1.length; i++) {
            // 构造（s+p1[i]）
            Element a[] = {p1[i], pairing.getZr().newOneElement()};
            r = polynomial_mul(r, a);
        }
        return r;
    }

    /**
     * 测试多项式
     */
    public static void testPoly() {
        double p1[] = {1, 2, 3};
        double p2 = 6;
        double p3 = 4;
        poly(p1, p2, p3);
    }

    /**
     * 测试一元多项式计算
     */
    public static void test1() {
        double a[] = {1, 2, 3};
        System.out.println(polynomial1D(a, 2));
    }

    /**
     * 测试多项式乘法
     */
    public static void test3() {
        double a[] = {1, 2};
        double b[] = {1, 3};
        double[] r = polynomial_mul(a, b);
        for (double d : r) {
            System.out.print(d + " ");
        }
        System.out.println();
    }


    /**
     * 测试多项式除法
     */
    public static void test5() {
        double[] m = {-3.5, 6.4, -3, 4, 2};
        double[] n = {-1, 1.2, 1};

        EnumMap<DIV, double[]> divResultMap = polynomial_div(m, n);
        double[] rr = (double[]) divResultMap.get(DIV.SHANG);
        double[] ll = (double[]) divResultMap.get(DIV.YUSHU);
        for (double d : rr) {
            System.out.print(d + " ");
        }
        System.out.println();
        for (double d : ll) {
            System.out.print(d + " ");
        }
        System.out.println();
    }


    public static void main(String[] args) {
        testPoly();
    }

    public enum DIV {
        SHANG, YUSHU;
    }
}