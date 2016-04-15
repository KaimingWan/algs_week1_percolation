import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.WeightedQuickUnionUF;

/**
 * Created by Kami on 4/13/2016.
 */
public class Percolation {

    private int[][] grid;   //二位数组里面的值即指的其关联的根节点
    private boolean[][] gridState;      //记录每个site的状态
    private int N;      //记录边长


    private WeightedQuickUnionUF wqu;

    //用于产生一个辅助的quickUnion对象，该对象底部不采用虚拟节点，用于在ifFull判断时避免backwash
    private WeightedQuickUnionUF wquAux;

    public Percolation(int N) {               // create N-by-N grid, with all sites blocked

        //抛出错误
        if (N <= 0) throw new IllegalArgumentException("N不能小于0");       //N小于0也要抛出异常

        //=========初始化阶段1：初始边长=============
        this.N = N;


        //=========初始化阶段2：初始平衡quickUnion对象=============
        wqu = new WeightedQuickUnionUF((N + 2) * (N + 2));    //初始化平衡QuickUnion对象
        wquAux = new WeightedQuickUnionUF((N + 1) * (N + 2));


        //=========初始化阶段3：初始grid=============


        //默认都为false,表示关闭状态；这里初始化为边长N+2的矩阵，不仅可以避免判断越界还可以用虚拟节点（即第一行肯定是全部白色的）
        grid = new int[N + 2][N + 2];

        //按照类似quickUnion当中一维数组的样子来初始化每个site的值
        for (int i = 0; i <= N + 1; i++) {
            for (int j = 0; j <= N + 1; j++) {
                grid[i][j] = getIndex(i, j);
            }
        }


        //=========初始化阶段4：初始gridState=============
        gridState = new boolean[N + 2][N + 2];     //初始化状态矩阵,默认都为false，即均不开启


        //初始化边框时需要注意第一行可以全部open，第0列和N+1列不能open，
        for (int i = 1; i <= N; i++) {     //4个角上的site可以不去open，不影响最后的结果
            openInline(0, i, wqu);
            openInline(0, i, wquAux);       //wquAux顶部一行不要忘记初始化
            openInline(N + 1, i, wqu);


        }


    }


    private void openInline(int i, int j, WeightedQuickUnionUF wquInline) {       //专门给第一行和最后一行使用的open函数，只需要关联他们在一行内的连通性即可
        if (!gridState[i][j]) gridState[i][j] = true;   //没打开，则打开site
        int centerSiteIndex = getIndex(i, j);    //记录被打开的这个中心site在quickUnion中的索引

        //此处避免使用isOpen()来判断，因为isOpen函数会对i,j做越界的判断，而由于我们使用了虚拟的site所以这里的某些值如果用isOpen方法会越界
        if (gridState[i][j-1]) {
            wquInline.union(centerSiteIndex, getIndex(i, j - 1));     //left
        }
        if (gridState[i][j+1]) {
            wquInline.union(centerSiteIndex, getIndex(i, j + 1));     //right
        }
    }


    private int getIndex(int i, int j) {      //将二维数组的索引值转化成quickUnion中一维数组的索引值
        return i * (N + 2) + j;       //N是真实边长，因为我们加了一个虚拟的边框，所以加2。这个转化大家自己画一下图就可以知道是这公式了
    }


    public void open(int i, int j) {          // open site (row i, column j) if it is not open already
        if (i < 0 || j < 0 || i > N + 2 || j > N + 2) throw new IndexOutOfBoundsException("越界错误");
        if (!gridState[i][j]) gridState[i][j] = true;   //没打开，则打开site

        int centerSiteIndex = getIndex(i, j);    //记录被打开的这个中心site在quickUnion中的索引

        //打开一个site之后则开始初始化QuickUnionUF中节点的连接关系,在这个打开点的四周判断是否可以确定连接关系,若可以则进行连通标记
        //此处避免使用isOpen()来判断，因为isOpen函数会对i,j做越界的判断，而由于我们使用了虚拟的site所以这里的某些值如果用isOpen方法会越界
        if (gridState[i - 1][j]) {
            wqu.union(centerSiteIndex, getIndex(i - 1, j));     //up
            wquAux.union(centerSiteIndex, getIndex(i - 1, j));     //up
        }
        if (gridState[i + 1][j]) {
            wqu.union(centerSiteIndex, getIndex(i + 1, j));     //down

            if (i + 1 < N + 1) {        //这里要注意下wquAux由于底层没有使用虚拟site，所以要做个越界判断
                wquAux.union(centerSiteIndex, getIndex(i + 1, j));     //down
            }

        }
        if (gridState[i][j - 1]) {
            wqu.union(centerSiteIndex, getIndex(i, j - 1));     //left
            wquAux.union(centerSiteIndex, getIndex(i, j - 1));     //left
        }
        if (gridState[i][j + 1]) {
            wqu.union(centerSiteIndex, getIndex(i, j + 1));     //right
            wquAux.union(centerSiteIndex, getIndex(i, j + 1));     //right
        }


    }


    public boolean isOpen(int i, int j) {     // is site (row i, column j) open?
        if (i <= 0 || j <= 0 || i > N || j > N) throw new IndexOutOfBoundsException("越界错误");
        return gridState[i][j];         //返回状态
    }


    public boolean isFull(int i, int j) {     // 判断某个site是否可渗透，这里用辅助的wquAux对象来判断，可以避免backwash
        if (i <= 0 || j <= 0 || i > N || j > N) throw new IndexOutOfBoundsException("越界错误");
        //full即指的是可以和最顶部的site连通，此处我们仅仅只要拿i=0,j=1这个第一个site来做连通测试即可
        int firstSiteIndex = 1;  //第一个site在quickUnion中的一维数组的索引页是1,我们只要判断是否和(0,1)坐标的site连通即可
        int newSiteIndex = getIndex(i, j);       //待判断的点
        return wquAux.connected(firstSiteIndex, newSiteIndex);    //如果测试能和第一块连通则认为是连通的


    }


    private boolean isPercolate(int i, int j) {      //这个是专门用来判断整体上是否可渗透的，使用wqu对象
        //full即指的是可以和最顶部的site连通，此处我们仅仅只要拿i=0,j=1这个第一个site来做连通测试即可
        int firstSiteIndex = 1;  //第一个site在quickUnion中的一维数组的索引页是1,我们只要判断是否和(0,1)坐标的site连通即可
        int newSiteIndex = getIndex(i, j);       //待判断的点
        return wqu.connected(firstSiteIndex, newSiteIndex);    //如果测试能和第一块连通则认为是连通的
    }

    public boolean percolates() {             // does the system percolate?
        //判断整个系统是否可渗透即判断最下面的虚拟site有没有办法和最上面的虚拟site连通，为了方便我们只取i=N.j=1的这个site
        return isPercolate(N + 1, 1);      //注意使用isPercolate方法来判断整体上是否是渗透的

    }

    public static void main(String[] args) {  // test client (optional)
        In in = new In(args[0]);      // input file
        int N = in.readInt();         // N-by-N percolation system


        //开始打开sites
        Percolation perc = new Percolation(N);
        while (!in.isEmpty()) {  //输入文件不为空的时候继续操作,每次读取一对i,j索引值
            int i = in.readInt();
            int j = in.readInt();
            perc.open(i, j);
            perc.isFull(i, j);
        }


        //判断系统是否可渗透
        if (perc.percolates()) {
            System.out.println("Yes");
        } else {
            System.out.println("No");
        }
    }
}
