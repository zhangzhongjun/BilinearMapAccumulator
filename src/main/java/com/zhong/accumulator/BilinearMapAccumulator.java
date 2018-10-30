package com.zhong.accumulator;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于双线性对的聚合器
 * <br>不管是什么类型的数据，都可以转化为string类型，所以我只需要处理String类型的数据即可
 * <br>对于群中的元素 首先使用toString转化为字符串
 *
 * @author 张中俊
 */
public class BilinearMapAccumulator implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static BilinearMapAccumulator_SiYao BM_siYao;
    private static BilinearMapAccumulator_GongYao BM_gongYao;
    private static Pairing pairing;

    static {
        BM_siYao = BilinearMapAccumulator_SiYao.getKey();
        BM_gongYao = BilinearMapAccumulator_GongYao.getKey();
        // 从文件a1.properties中读取参数初始化双线性群
        pairing = PairingFactory.getPairing("params/curves/a.properties");
    }

    /**
     * 要处理的集合
     */
    private List<SerializableElement> X = null;
    /**
     * 要处理的集合
     */
    private List<String> Y = null;
    /**
     * 计算得到的accumulatorValue
     */
    private SerializableElement accumulatorValue = null;
    /**
     * 该集合的 聚合值
     */
    private SerializableElement AkX = null;
    /**
     * 该集合的 累加值 是计算聚合值的一个中间结果
     */
    private SerializableElement FxK;

    /**
     *
     * 构造方法
     * <br>既然任何类型的数据都能转化为字符串类型，那么我们只需要处理字符串类型即可
     * <br>传入一个字符串类型的集合，构造出他的聚合器
     * @param Y
     *         要处理的集合
     *
     * @throws Exception
     *         异常
     */
    public BilinearMapAccumulator(List<String> Y) throws Exception {
        this.Y = Y;
        this.X = new ArrayList<SerializableElement>();

        // 将FxK初始化为Zr中的单位元
        Element temp = pairing.getZr().newOneElement();
        // 计算FxK
        for (String y : Y) {
            Element e = pairing.getZr().newElementFromHash(y.getBytes("utf-8"), 0, y.getBytes("utf-8").length);
            this.X.add(new SerializableElement(e));
            temp = temp.duplicate().mul(e.duplicate().add(BM_siYao.getS().getElement()));
        }
        FxK = new SerializableElement(temp);
        AkX = new SerializableElement(BM_siYao.getG().getElement().duplicate().powZn(FxK.getElement().duplicate()));
    }

    /**
     * 获得原始的集合
     *
     * @return 原始的集合，是一个String 类型的数组
     */
    public List<String> getY() {
        return Y;
    }


    /**
     * 获得y的witness
     * @param y 要测试的元素
     * @return y的witness
     * @throws Exception 异常
     */
    public Witness getElementWitness(String y) throws Exception{
        Element e = pairing.getZr().newElementFromHash(y.getBytes("utf-8"), 0, y.getBytes("utf-8").length);
        return getWitness(e);
    }


    /**
     * 获得元素y的witness
     * <br>使用该方法之前，要将需要检测的字符串映射到Zr域中的一个元素上
     *
     * @param y
     *         Zr域中的一个元素
     *
     * @return 该元素的witness
     */
    private Witness getWitness(Element y) {
        Element temp = pairing.getZr().newOneElement();
        for (SerializableElement x : X) {
            temp = temp.duplicate().mul(x.getElement().duplicate().sub(y.duplicate()));
        }
        Element zeroElem = pairing.getZr().newZeroElement();
        Element Uy = zeroElem.duplicate().sub(temp);
        Element Wy = BM_siYao.getG().getElement().duplicate().powZn(FxK.getElement().duplicate().add(Uy).duplicate().div(y.duplicate().add(BM_siYao.getS().getElement())));
        return new Witness(new SerializableElement(Uy), new SerializableElement(Wy));
    }

    /**
     * 计算一个元素的证据（版本2 不使用私钥的情况）
     *
     * @param y 要计算的元素
     * @return 该元素的witness
     * @throws Exception 异常
     */
    public Witness getElementWitness2(String y) throws Exception{
        Element e = pairing.getZr().newElementFromHash(y.getBytes("utf-8"), 0, y.getBytes("utf-8").length);
        return getWitness2(e);
    }

    /**
     * 获得元素y的witness（版本2：不使用私钥的情况）
     * <br>使用该方法之前，要将需要检测的字符串映射到Zr域中的一个元素上
     *
     * @param y
     *         Zr域中的一个元素
     *
     * @return 该元素的witness
     */
    private Witness getWitness2(Element y) {
        Element temp = pairing.getZr().newOneElement();
        for (SerializableElement x : X) {
            temp = temp.duplicate().mul(x.getElement().duplicate().sub(y.duplicate()));
        }
        Element zeroElem = pairing.getZr().newZeroElement();
        Element Uy = zeroElem.duplicate().sub(temp);

        Element p1[] = new Element[this.X.size()];
        for(int i=0;i<this.X.size();i++){
            p1[i] = this.X.get(i).getElement();
        }
        Element[] res = Polynomial.poly(p1,Uy,y);
        Element Wy = pairing.getG1().newOneElement().duplicate();
        for(int i=0;i<res.length;i++){
            Element xiShu = res[i].duplicate();
            Element g_pow_s_pow_i = BM_gongYao.getHs().get(i).getElement().duplicate();
            Element g_pow_s_pow_i_pow_xishu = g_pow_s_pow_i.duplicate().powZn(xiShu.duplicate());
            Wy = Wy.duplicate().mul(g_pow_s_pow_i_pow_xishu.duplicate());
        }
        return new Witness(new SerializableElement(Uy), new SerializableElement(Wy));
    }


    /**
     * 测试y的witness是否成立
     *
     * @param y
     *         要测试的字符串
     * @param witness
     *         y对应的证据
     *
     * @return 该集合是否成立
     * @throws Exception 异常
     */
    public boolean testElementWitness(String y, Witness witness) throws Exception{
        Element e = pairing.getZr().newElementFromHash(y.getBytes("utf-8"), 0, y.getBytes("utf-8").length);
        return testWitness(e,witness);
    }

    /**
     * 测试元素y是否在集合中
     *
     * @param y
     *         要测试的元素，在Zr上
     * @param witness
     *         y对应的证据
     *
     * @return 是否在集合中
     */
    private boolean testWitness(Element y, Witness witness) {
        Element g = BM_gongYao.getHs().get(0).getElement().duplicate();
        Element h = BM_gongYao.getHs().get(1).getElement().duplicate();

        Element Wy = witness.getWy().getElement().duplicate();
        Element Uy = witness.getUy().getElement().duplicate();

        Element left = Wy.duplicate();
        Element right = g.duplicate().powZn(y.duplicate()).duplicate().mul(h.duplicate());

        Element e1 = pairing.pairing(left.duplicate(), right.duplicate());
        Element e2 = pairing.pairing(AkX.getElement().duplicate(), g);

        return e1.isEqual(e2) && Uy.isEqual(pairing.getZr().newZeroElement());
    }


    /**
     * 计算一个元素的nonwitness
     *
     * @param y
     *         要计算的元素
     *
     * @return 该元素的nonwitness
     * @throws Exception 异常
     */
    public NonWitness getElementNonWitness(String y) throws Exception{
        Element e = pairing.getZr().newElementFromHash(y.getBytes("utf-8"), 0, y.getBytes("utf-8").length);
        return getNonWitness(e);
    }


    /**
     * 计算一个元素的nonwitness
     *
     * @param y
     *         要计算的元素，在Zr域中
     *
     * @return 该元素的nonwitness
     */
    private NonWitness getNonWitness(Element y) {
        Element temp = pairing.getZr().newOneElement();
        for (SerializableElement x : X) {
            temp = temp.duplicate().mul(x.getElement().duplicate().sub(y.duplicate()));
        }
        Element zeroElem = pairing.getZr().newZeroElement();
        Element Uy = zeroElem.duplicate().sub(temp);
        Element Wy = BM_siYao.getG().getElement().duplicate().powZn(FxK.getElement().duplicate().add(Uy).duplicate().div(y.duplicate().add(BM_siYao.getS().getElement())));
        return new NonWitness(new SerializableElement(Uy), new SerializableElement(Wy));
    }




    /**
     * 计算一个元素的nonwitness（版本2，不使用私钥）
     *
     * @param y
     *         要计算的元素
     *
     * @return 该元素的nonwitness
     * @throws Exception 异常
     */
    public NonWitness getElementNonWitness2(String y) throws Exception{
        Element e = pairing.getZr().newElementFromHash(y.getBytes("utf-8"), 0, y.getBytes("utf-8").length);
        return getNonWitness(e);
    }


    /**
     * 计算一个元素的nonwitness（版本2，不使用私钥）
     *
     * @param y
     *         要计算的元素，在Zr域中
     *
     * @return 该元素的nonwitness
     */
    private NonWitness getNonWitness2(Element y) {
        Element temp = pairing.getZr().newOneElement();
        for (SerializableElement x : X) {
            temp = temp.duplicate().mul(x.getElement().duplicate().sub(y.duplicate()));
        }
        Element zeroElem = pairing.getZr().newZeroElement();
        Element Uy = zeroElem.duplicate().sub(temp);


        Element p1[] = new Element[this.X.size()];
        for(int i=0;i<this.X.size();i++){
            p1[i] = this.X.get(i).getElement();
        }
        Element[] res = Polynomial.poly(p1,Uy,y);
        Element Wy = pairing.getG1().newOneElement().duplicate();
        for(int i=0;i<res.length;i++){
            Element xiShu = res[i].duplicate();
            Element g_pow_s_pow_i = BM_gongYao.getHs().get(i).getElement().duplicate();
            Element g_pow_s_pow_i_pow_xishu = g_pow_s_pow_i.duplicate().powZn(xiShu.duplicate());
            Wy = Wy.duplicate().mul(g_pow_s_pow_i_pow_xishu.duplicate());
        }
        return new NonWitness(new SerializableElement(Uy), new SerializableElement(Wy));
    }



    /**
     * 检测一个nonWitness是否成立
     *
     * @param y
     *         要检测的元素
     * @param witness
     *         证明
     *
     * @return 证据是否成立
     *
     * @throws Exception 异常
     */
    public boolean testElementNonWitness(String y,NonWitness witness) throws Exception{
        Element e = pairing.getZr().newElementFromHash(y.getBytes("utf-8"), 0, y.getBytes("utf-8").length);
        return testNonWitness(e,witness);
    }

    /**
     * 检测一个nonWitness是否成立
     *
     * @param y
     *         要检测的元素
     * @param witness
     *         证明
     *
     * @return 证据是否成立
     */
    private boolean testNonWitness(Element y, NonWitness witness) {
        Element g = BM_gongYao.getHs().get(0).getElement().duplicate();
        Element h = BM_gongYao.getHs().get(1).getElement().duplicate();

        Element Wy = witness.getWy().getElement().duplicate();
        Element Uy = witness.getUy().getElement().duplicate();

        Element left = Wy;
        Element right = g.duplicate().powZn(y.duplicate()).duplicate().mul(h.duplicate());

        Element e1 = pairing.pairing(left.duplicate(), right.duplicate());
        Element e2 = pairing.pairing(AkX.getElement().duplicate(), g.duplicate());

        return !(e1.isEqual(e2) && Uy.isEqual(pairing.getZr().newZeroElement()));
    }


    /**
     * 获得一个子集的evidence
     *
     * @param y
     *         要计算的集合
     *
     * @return 集合的evidence
     *
     * @throws Exception
     *         异常
     */
    public Witness getSubsetWitness(List<String> y) throws Exception {
        List<String> x_sub_y = new ArrayList<String>();
        // 求出y的差集
        x_sub_y.addAll(this.Y);
        x_sub_y.removeAll(y);

        Element temp = pairing.getZr().newOneElement();
        for (String str : x_sub_y) {
            Element e = pairing.getZr().newElementFromHash(str.getBytes("utf-8"), 0, str.getBytes("utf-8").length);
            temp = temp.duplicate().mul(e.duplicate().add(BM_siYao.getS().getElement()));
        }
        Element Wy = BM_siYao.getG().getElement().duplicate().powZn(temp.duplicate());
        return new Witness(null, new SerializableElement(Wy));
    }


    /**
     * 获得一个子集的evidence
     *
     * @param y
     *         要计算的集合
     *
     * @return 集合的evidence
     *
     * @throws Exception
     *         异常
     */
    public Witness getSubsetWitness2(List<String> y) throws Exception {
        //TODO 当y=Y的时候，应该判断为正确还是错误
        List<String> x_sub_y = new ArrayList<String>();
        // 求出y的差集
        x_sub_y.addAll(this.Y);
        x_sub_y.removeAll(y);

        if(x_sub_y.size()==0){
            Element Wy = BM_gongYao.getHs().get(0).getElement().duplicate();
            return new Witness(null,new SerializableElement(Wy));
        }else {
            Element[] ys = new Element[x_sub_y.size()];
            for (int i = 0; i < x_sub_y.size(); i++) {
                String str = x_sub_y.get(i);
                Element e = pairing.getZr().newElementFromHash(str.getBytes("utf-8"), 0, str.getBytes("utf-8").length);
                ys[i] = e;
            }
            Element Wy = pairing.getG1().newOneElement().duplicate();
            Element[] res = Polynomial.poly(ys);
            for (int i = 0; i < res.length; i++) {
                Element xiShu = res[i].duplicate();
                Element g_pow_s_pow_i = BM_gongYao.getHs().get(i).getElement().duplicate();
                Element g_pow_s_pow_i_pow_xishu = g_pow_s_pow_i.duplicate().powZn(xiShu.duplicate());
                Wy = Wy.duplicate().mul(g_pow_s_pow_i_pow_xishu.duplicate());
            }
            return new Witness(null, new SerializableElement(Wy));
        }

    }


    /**
     * 检测一个集合的nonWitness是否成立
     *
     * @param strs
     *         要判断的集合
     * @param witness
     *         要判断的集合的witness
     *
     * @return strs是否是子集
     *
     * @throws Exception
     *         异常
     */
    public boolean testSubsetWitness(List<String> strs, Witness witness) throws Exception {
        Element temp = pairing.getZr().newOneElement();
        for (String str : strs) {
            Element e = pairing.getZr().newElementFromHash(str.getBytes("utf-8"), 0, str.getBytes("utf-8").length);
            temp = temp.duplicate().mul(e.duplicate().add(BM_siYao.getS().getElement()));
        }
        Element left_left = BM_siYao.getG().getElement().duplicate().powZn(temp.duplicate());
        Element left = pairing.pairing(witness.getWy().getElement(), left_left);
        Element right = pairing.pairing(AkX.getElement(), BM_siYao.getG().getElement());

        return left.isEqual(right);
    }

}
