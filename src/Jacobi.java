/* * This class provides a simple implementation of the Jacobi method for solving
 * systems of linear equations. */
 
/*
  How to use:
  The program reads an augmented matrix from standard input,
  for example:
 
   3
   5 -2  3 -1
  -3  9  1  2
   2 -1 -7  3
 
  The number in the first line is the number of equations
  and number of variables. You can put this values in a file
  and then execute the program as follows:
 
  $ java Jacobi < equations.txt
 
  If the matrix isn't diagonally dominant the program tries
  to convert it(if possible) by rearranging the rows.
  Source:http://rendon.x10.mx/jacobi-method/
*/
 
import java.util.Arrays;
import java.util.StringTokenizer;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
 
public class Jacobi 
{
 
    public static final int MAX_ITERATIONS = 100;
    private double[][] M;
    private Mat A;
    private Mat B;
 
    public Jacobi(Mat a_,Mat b_) 
    {     	
    	A=a_;
    	B=b_;
    	M=new double[][]{
    		 {5 ,-2 , 3, -1},
    		  {-3 , 9 , 1,  2},
    		   {2, -1, -7 , 3}    		
    	};
    }  
     /**
     * Applies Jacobi method to find the solution of the system
     * of linear equations represented in matrix M.
     * M is a matrix with the following form:
     * a_11 * x_1 + a_12 * x_2 + ... + a_1n * x_n = b_1
     * a_21 * x_1 + a_22 * x_2 + ... + a_2n * x_n = b_2
     * .                 .                  .        .
     * .                 .                  .        .
     * .                 .                  .        .
     * a_n1 * x_n + a_n2 * x_2 + ... + a_nn * x_n = b_n
     */
    public double[] partial(int n,int channel)
    {
    	int iterations = 0;
    	double epsilon = 1e-15;
    	double[] XX=new double[n];
    	double[] X=new double[n];
    	double[] P = new double[n]; // Prev
        Arrays.fill(X, 0);
        Arrays.fill(P, 0);
        //System.out.println("la valeur de n="+n+"/channel:"+channel);
        while (true) 
        {
            for (int i = 0; i < n; i++) 
            {
            	byte[] b_n=new byte[1];
            	byte[] a_n=new byte[1];
            	byte[] a_i=new byte[1];
            	B.get(i,0,b_n);
            	A.get(i,i, a_i);
            	//System.out.println(M[i][n]+"="+b_n[0]);
                double sum =b_n[0];//M[i][n]; // b_n    
                
                for (int j = 0; j < n; j++)
                {
                	A.get(i,j, a_n);
                	if (j != i)
                		sum -=(double)a_n[0] * P[j];
                }               
                X[i] =1/(double)a_i[0] * sum;                
            }           
            iterations++;
            if (iterations == 1) continue;
 
            boolean stop = true;
            for (int i = 0; i < n && stop; i++)
                if (Math.abs(X[i] - P[i]) > epsilon)
                    stop = false;
 
            if (stop || iterations == MAX_ITERATIONS) break;
            P = (double[])X.clone();
        }
        for(int i=0;i<n;i++)
        {
        	//XX[i]=X[i];//Math.abs(Math.round(X[i]));
        	XX[i]=Math.abs(X[i]);
        	System.out.print(XX[i]+" ("+(byte)XX[i]+") ");
        }
        System.out.println("");
    	return XX;
    }
    public double[] solve()
    {        
        int n = A.rows();           
        return partial(n, 0);  
    }
    public static void Test()
    {
    	Mat a_=new Mat(3,3,CvType.CV_64FC1);
		Mat b_=new Mat(1,3,CvType.CV_64FC1);
		double[] aData=new double[]{5,-2,3,-3,9,1,2,-1,-7};
		double[] bData=new double[]{-1,2,3};
		
		a_.put(0, 0, aData);
		b_.put(0, 0, bData);
		for(int i=0;i<a_.rows();i++)
		{
			for(int j=0;j<a_.cols();j++)
			{
				double[] d=new double[1];
				a_.get(i, j,d);
				System.out.print(d[0]+" ");
			}
			System.out.println("");
		}
		for(int i=0;i<a_.rows();i++)
		{
			for(int j=0;j<a_.cols();j++)
			{
				double[] d=new double[1];
				b_.get(i, j,d);
				System.out.print(d[0]+" ");
			}
			System.out.println("");
		}
		Jacobi jc=new Jacobi(a_, b_);
		jc.solve();
    }
}