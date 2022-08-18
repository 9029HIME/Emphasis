package com.mzsk.designpattern.adapter;

import org.springframework.data.domain.Page;

/**
 * @Desc 类适配器模式
 * 核心四要素：使用者、被使用者、适配器、被适配者
 *
 * 一般来说，【使用者】只能调用【被使用者】来达成目标，是无法调用【被适配者的】
 * 某些场景下，需要【使用者】调用【被适配者】，只能通过【适配器】同时具有【被使用者】和【被适配者】的特性
 * 然后【使用者】直接调用【适配器】，从而达到调用【被适配者】的效果
 *
 *
 * 以中转器来实现220V的插头插入110V排插为例，其中排插是使用者，110V插头是被使用者，中转器是适配器，220V插头是被适配者。
 */
public class ClassAdapterMode {
    public static void main(String[] args) {
        // 正常情况下，因为排查不兼容220V，会抛出异常
        try {
            Plug V220 = new SuperPlug();
            PowerStrip.inserted(V220);
        }catch (Exception e){
            e.printStackTrace();
        }


        // 于是我加入适配器
        Plug adapter = new PlugAdapter();
        PowerStrip.inserted(adapter);
    }

    // 插座接口
    public interface Plug{
        public void doPlug();
    }

    public interface Plug110V extends Plug{
    }

    public interface Plug220V extends Plug{
    }

    public static class SuperPlug implements Plug220V{
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


    public static class PlugAdapter extends SuperPlug implements Plug110V{
        @Override
        public void doPlug() {
            super.doPlug();
            System.out.println("作为110V的插头插进去了");
        }
    }


}
