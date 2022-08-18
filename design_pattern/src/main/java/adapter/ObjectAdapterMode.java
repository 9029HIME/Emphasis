package com.mzsk.designpattern.adapter;

/**
 * @Desc 对象适配器
 *  和类适配器差不多，区别是220V是作为对象的传入适配器的
 *  有点类似装饰者模式
 */
public class ObjectAdapterMode {
    public static void main(String[] args) {
        Plug220V V220 = new SuperPlug();

        // 正常情况下，因为排查不兼容220V，会抛出异常
        try {
            PowerStrip.inserted(V220);
        }catch (Exception e){
            e.printStackTrace();
        }


        // 于是我加入适配器
        Plug adapter = new PlugAdapter(V220);
        PowerStrip.inserted(adapter);
    }

    // 插座接口
    public interface Plug{
        public void doPlug();
    }

    public interface Plug110V extends Plug {
    }

    public interface Plug220V extends Plug {
    }

    public static class SuperPlug implements Plug220V {
        @Override
        public void doPlug() {
            System.out.println("220V的插头被插入了");
        }
    }



    public static class PowerStrip{
        public static void inserted(Plug plug){
            if(plug instanceof Plug110V){
                plug.doPlug();
            }else{
                throw new RuntimeException("不能插入非110V的插头");
            }
        }
    }


    public static class PlugAdapter implements Plug110V {
        private Plug220V plug;

        public PlugAdapter(Plug220V plug){
            this.plug = plug;
        }

        @Override
        public void doPlug() {
            plug.doPlug();
            System.out.println("作为110V的插头插进去了");
        }
    }

}
