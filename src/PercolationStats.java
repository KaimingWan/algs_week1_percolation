import edu.princeton.cs.algs4.StdRandom;
import edu.princeton.cs.algs4.StdStats;

/**
 * Created by Kami on 2016/4/14.
 */
public class PercolationStats {

    //可以定义成局部变量就尽量定义成局部变量，别浪费内存！！！
//    private Percolation perc;
    private double[] ratio;          //open的site个数占总共的site的比例
    private int T;

    public PercolationStats(int N, int T) {      // perform T independent experiments on an N-by-N grid


        //异常处理
        if (N <= 0 || T <= 0) throw new IllegalArgumentException("N，T不能小于等于0");


        this.T = T;       //记录需要实验的次数

        ratio = new double[T];      //初始化好需要统计的计数器


        for (int i = 0; i < T; i++) {       //总共进行T次独立试验
            Percolation perc = new Percolation(N);  //每次开始测验，生成一个新的Percolation对象,边长为N

            int j, k;        //j,k分别代表二位数组中的索引值
            int count = 0;      //记录每次试验open的site个数，用于计算试验样本的open比例，从而可以计算mean;总共T次，所以用个数组表示
            while (!perc.percolates()) { //当整个系统还没有完全渗透之前，就不断地开启site


                /**
                 * 算法分析：此处增加site认为可以最多增加n次，每次开启site后修改树中节点的根节点代价为nlogn，最后搜索匹配是否
                 * 连通的代价为logn;而总共又进行了T次试验，所以时间复杂读和T,N的关系为O(T(NlogN)^2)
                 */

                j = StdRandom.uniform(1, N + 1); //从[1,N+1)中随便选个值；
                k = StdRandom.uniform(1, N + 1); //从[1,N+1)中随便选个值;
                if (!perc.isOpen(j, k)) {
                    perc.open(j, k); //如果没打开的话则打开该site，已经打开则不必打开
                    count++;
                }


            }


            ratio[i] = (double) count / (N * N);


        }

    }

    public static void main(String[] args) {     // test client (described below)

        //以下代码在提交时可以注释掉，在本地测试时使用，因为下面有对args进行拆箱和装箱的过程，被认为是一个问题
//        Stopwatch watch = new Stopwatch();  //用作者提供的API可以进行计时
//
//        int N = Integer.valueOf(args[0]);         // 第一个参数是N的大小
//        int T = Integer.valueOf(args[1]);          //第二个数是实验次数T
//        PercolationStats percStats = new PercolationStats(N, T);
//
//        System.out.println("mean                    =" + percStats.mean());
//        System.out.println("stddev                  =" + percStats.stddev());
//        System.out.println("95% confidence interval =" + percStats.confidenceLo() + "," + percStats.confidenceHi());
//
//        System.out.println("Elapsed Time:" + watch.elapsedTime());


    }

    public double mean() {            // sample mean of percolation threshold
        //openCount记录每次试验需要开启多少site才能渗透，该函数计算该值平均值

        return StdStats.mean(ratio);        //这里也调用作者提供的StdStates里面的方法即可

    }

    public double stddev() {     // sample standard deviation of percolation threshold
        return StdStats.stddev(ratio);
    }

    public double confidenceLo() {    // low  endpoint of 95% confidence interval

        return mean() - (1.96 * stddev()) / Math.sqrt(T);

    }

    public double confidenceHi() {    // high endpoint of 95% confidence interval
        return mean() + (1.96 * stddev()) / Math.sqrt(T);
    }
}
