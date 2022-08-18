package com.mzsk.designpattern.decorator;

/**
 * @Desc 装饰者模式
 * 一句话：是你（继承）还有你（属性），一切拜托你（使用属性），有点类似对象适配器模式
 * 核心二要素：装饰者，被装饰者，装饰行为
 *
 * 以演员（装饰者）扮演角色（被装饰者）讲台词（装饰行为）为例
 */
public class DecoratorMode {
    public static void main(String[] args) {
        NormalWarrior normalWarrior = new NormalWarrior();
        Stage.show(normalWarrior);

        Actor actor = new Actor(normalWarrior);
        Stage.show(actor);
    }

    public static class Stage{

        public static void show(Role role){
            role.show();
        }
    }

    public interface Role{
        void show();
    }

    public static class NormalWarrior implements Role{
        @Override
        public void show() {
            System.out.println("普通的战士演出");
        }
    }

    public static class Actor extends NormalWarrior{
        private NormalWarrior normalWarrior;

        public Actor(NormalWarrior normalWarrior) {
            this.normalWarrior = normalWarrior;
        }

        @Override
        public void show() {
            super.show();
            System.out.println("作为演员的出彩表演");
        }
    }



}
