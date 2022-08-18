package com.mzsk.designpattern.builder;

/**
 * @Desc 生成器模式
 * 核心的四要素：生成器、具体生成器、指导器、产品
 *
 * 生成器规范了生产步骤（接口）
 * 【具体生成器】实现生成器的接口，从而实现具体的生成逻辑
 * 【指导器】规范了【生成器】的步骤顺序，通过【具体生成器】来完成这个步骤，最终返回【产品】
 *
 * 以电脑为【产品】的产品，编写生成器模式的代码
 */
public class BuilderMode {
    public static void main(String[] args) {
        ComputerBuilder computerBuilder = new SuperComputerBuilder();
        ComputerDirector director = new ComputerDirector(computerBuilder);
        Computer computer = director.constructComputer();
        System.out.println(computer);

    }


    public interface ComputerBuilder{
        String buildCPU();
        String buildScreen();
        String buildKeyboard();
        String buildMouse();
    }

    public static class SuperComputerBuilder implements ComputerBuilder{
        @Override
        public String buildCPU() {
            return "SuperCPU";
        }

        @Override
        public String buildScreen() {
            return "SuperScreen";
        }

        @Override
        public String buildKeyboard() {
            return "SuperKeyboard";
        }

        @Override
        public String buildMouse() {
            return "SuperMouse";
        }
    }


    public static class ComputerDirector{
        private ComputerBuilder builder;
        public ComputerDirector(ComputerBuilder builder){
            this.builder = builder;
        }

        public Computer constructComputer(){
            Computer computer = new Computer();
            computer.setCpu(builder.buildCPU());
            computer.setScreen(builder.buildScreen());
            computer.setKeyboard(builder.buildKeyboard());
            computer.setMouse(builder.buildMouse());
            return computer;
        }
    }

    public static class Computer{
        private String cpu;
        private String screen;
        private String keyboard;
        private String mouse;

        public String getCpu() {
            return cpu;
        }

        public void setCpu(String cpu) {
            this.cpu = cpu;
        }

        public String getScreen() {
            return screen;
        }

        public void setScreen(String screen) {
            this.screen = screen;
        }

        public String getKeyboard() {
            return keyboard;
        }

        public void setKeyboard(String keyboard) {
            this.keyboard = keyboard;
        }

        public String getMouse() {
            return mouse;
        }

        public void setMouse(String mouse) {
            this.mouse = mouse;
        }

        @Override
        public String toString() {
            return "Computer{" +
                    "cpu='" + cpu + '\'' +
                    ", screen='" + screen + '\'' +
                    ", keyboard='" + keyboard + '\'' +
                    ", mouse='" + mouse + '\'' +
                    '}';
        }
    }
}




