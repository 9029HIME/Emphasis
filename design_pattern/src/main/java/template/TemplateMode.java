package com.mzsk.designpattern.template;

/**
 * @Desc 模板模式，就是面向接口编程，方法通过调用接口的方式确定【整体流程】，通过接口实现类来达成【自定义效果】
 */
public class TemplateMode {
    public static void main(String[] args) {
        loanProcess(new LoanBehavior() {
            @Override
            public void creditApply() {
                System.out.println("授信申请");
            }

            @Override
            public void borrowApply() {
                System.out.println("借款申请");
            }

            @Override
            public void repayApply() {
                System.out.println("还款申请");
            }
        });
    }

    public static void loanProcess(LoanBehavior loanBehavior){
        loanBehavior.creditApply();
        loanBehavior.borrowApply();
        loanBehavior.repayApply();
    }

    public interface LoanBehavior{
        public void creditApply();
        public void borrowApply();
        public void repayApply();
    }

}
