import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


public class ExternProcess 
{
	public static enum GradientType
	{
		R,G,B,RGB,Grey
	}
	public static void HistogrammeRGB(Mat m,int[] R,int[] G,int[] B)
	{
		byte[] col=new byte[3];
		for(int i=0;i<m.rows();i++)
		{
			for(int j=0;j<m.cols();j++)
			{					
				m.get(i, j,col);
				R[byteColorCVtoIntJava(col[2])]+=1;
				G[byteColorCVtoIntJava(col[1])]+=1;
				B[byteColorCVtoIntJava(col[0])]+=1;				
			}
		}
	}
	public static int byteColorCVtoIntJava(byte b)
	{		
		int i=(b+128)+128;		
		return b>=0?(int)b:i;
	}
	public static void HistogrammeCumuleRGB(Mat m,int[] R,int[] G,int[] B,int[] RC,int[] GC,int[] BC,int N)
	{
		int valueR=0;int valueG=0;int valueB=0;
		for(int i=0;i<256;i++)
		{
			valueR+=R[i];RC[i]=R[i]==0?0:valueR;
			valueG+=G[i];GC[i]=G[i]==0?0:valueG;
			valueB+=B[i];BC[i]=B[i]==0?0:valueB;
			
		}
	}
	public static void InverseHistogrammeRGB(int[] RC,int[] GC,int[] BC,Hashtable<Integer, Integer> InvHistoCumulR,Hashtable<Integer, Integer> InvHistoCumulG,Hashtable<Integer, Integer> InvHistoCumulB )
	{
		for(int i=0;i<256;i++)
		{				
			InvHistoCumulR.put(RC[i], i);
			InvHistoCumulG.put(GC[i], i);
			InvHistoCumulB.put(BC[i], i);			
		}
	}
	public static void MatchingHistogram(Mat imRef,Mat imTarget,Mat result)
	{
		int[] RRef=new int[256];int[] RCRef=new int[256];Hashtable<Integer, Integer> InvHistoCumulRRef=new Hashtable<>();
		int[] GRef=new int[256];int[] GCRef=new int[256];Hashtable<Integer, Integer> InvHistoCumulGRef=new Hashtable<>();
		int[] BRef=new int[256];int[] BCRef=new int[256];Hashtable<Integer, Integer> InvHistoCumulBRef=new Hashtable<>();
		int[] RTar=new int[256];int[] RCTar=new int[256];Hashtable<Integer, Integer> InvHistoCumulRTar=new Hashtable<>();
		int[] GTar=new int[256];int[] GCTar=new int[256];Hashtable<Integer, Integer> InvHistoCumulGTar=new Hashtable<>();
		int[] BTar=new int[256];int[] BCTar=new int[256];Hashtable<Integer, Integer> InvHistoCumulBTar=new Hashtable<>();
		int N=imRef.cols()*imRef.rows();
		HistogrammeRGB(imRef, RRef, GRef, BRef);
		HistogrammeCumuleRGB(imRef, RRef, GRef, BRef,RCRef,GCRef,BCRef,N);
		InverseHistogrammeRGB(RCRef, GCRef, BCRef, InvHistoCumulRRef, InvHistoCumulGRef, InvHistoCumulBRef);
		
		HistogrammeRGB(imTarget, RTar, GTar, BTar);
		HistogrammeCumuleRGB(imTarget, RTar, GTar, BTar,RCTar,GCTar,BCTar,N);
		InverseHistogrammeRGB(RCTar, GCTar, BCTar, InvHistoCumulRTar, InvHistoCumulGTar, InvHistoCumulBTar);		
		byte[] pixel=new byte[3];
		byte[] pixelTarget=new byte[3];		
		for(int i=0;i<imRef.rows();i++)
		{
			for(int j=0;j<imRef.cols();j++)
			{
				imRef.get(i, j,pixel);
				imTarget.get(i, j,pixelTarget);
				byte blue=pixelTarget[0];byte green=pixelTarget[1];byte red=pixelTarget[2];				
				int r=minimum(RCTar[byteColorCVtoIntJava(red)],RCRef,InvHistoCumulRRef);
				int g=minimum(GCTar[byteColorCVtoIntJava(green)],GCRef,InvHistoCumulGRef);
				int b=minimum(BCTar[byteColorCVtoIntJava(blue)],BCRef,InvHistoCumulBRef);		
				pixel[0]=b>256?(byte)255:(byte)b;
				pixel[1]=b>256?(byte)255:(byte)g;
				pixel[2]=b>256?(byte)255:(byte)r;				
				result.put(i, j, pixel);
			}
		}
	}
	public static void LaplacianPyramid(Mat src,Mat dest,List<Mat> gauss)
	{		
		Mat temp=src.clone();		
		Imgproc.pyrDown(temp, dest);
		Imgproc.pyrUp(dest, dest,temp.size());
		gauss.add(dest.clone());//we will use it to collapse-pyramid		
		//temp=dest.clone();		
		Core.subtract(src, dest, dest);
		//Core.add(dest, temp, dest);//Si on veut retrouver l'image originale Gi		
	}
	public static void createLaplacianPyramid(Mat dest,int n,List<Mat>pyramid,List<Mat>gauss)
	{
		int i=0;
		Mat temp=dest.clone();
		while(i<n)
		{			
			LaplacianPyramid(temp, dest,gauss);
			pyramid.add(dest.clone());			
			Imgproc.pyrDown(temp, temp);			
			i++;
		}
	}
	public static Mat TextureMatching(Mat imRef,Mat imTar,Mat result,int n)
	{
		List<Mat> pyramidRef=new ArrayList<>();
		List<Mat> pyramidTar=new ArrayList<>();
		List<Mat> gaussRef=new ArrayList<>();
		List<Mat> gaussTar=new ArrayList<>();
		Mat tempRef=imRef.clone();
		Mat tempTar=imTar.clone();
		createLaplacianPyramid(tempRef, n, pyramidRef,gaussRef);
		createLaplacianPyramid(tempTar, n, pyramidTar,gaussTar);	
		MatchingHistogram(imRef, imTar, imTar);
		int i=0;		
		while(i<n)
		{
			//result=new Mat();
			MatchingHistogram(pyramidRef.get(i),pyramidTar.get(i), pyramidTar.get(i));
			//result=pyramidTar.get(i).clone();
			i++;
		}		
		result=collapsePyramid(pyramidTar,gaussTar);
		MatchingHistogram(imRef, result, result);
		return result;
	}
	public static Mat collapsePyramid(List<Mat> pyramid,List<Mat>gauss)
	{
		int i=pyramid.size()-1;
		Mat temp=new Mat();		
		while(i>=0)
		{
			//Imgproc.pyrUp(pyramid.get(i), temp,pyramid.get(i-1).size());			
			Core.add(pyramid.get(i), gauss.get(i), temp);
			//
			i--;
		}		
		return temp;
	}
	public static int minimum(int value,int[] cumul,Hashtable<Integer, Integer>inv_cumul)
	{
		int[] temp=new int[256];
		Hashtable<Integer, Integer>tempHash=new Hashtable<>();
		for(int i=0;i<256;i++)
		{
			temp[i]=Math.abs(value-cumul[i]);
			tempHash.put(temp[i], cumul[i]);
		}		
		int a=0;
		for(int i=0;i<255;i++)
		{
			for(int j=i+1;j<256;j++)
			{
				if(temp[i]>temp[j])
				{
					a=temp[j];
					temp[j]=temp[i];
					temp[i]=a;
				}
			}
		}
		return inv_cumul.get(tempHash.get(temp[0]));
	}
	public static void gaussianTiles(Mat m,double size,double sigmaX)
	{
		//Mat newM=new Mat(m.rows(),m.cols(),CvType.CV_8UC3);
		Imgproc.GaussianBlur(m, m, new Size(size, size), sigmaX);
		//return newM;
	}
	public static void regularizeSVBRDF(Mat[][] f1,Mat[][]f2,int tileLenH,int tileLenW,int hauteur)
	{
		//f1 et f2 doivent être initialisé avant
		try
		{
			int[][] temp=new int[hauteur*hauteur][3];
			int index=0;
			byte[] pixel=new byte[3];
			for(int i=0;i<tileLenH;i++)
			{
				//parcours tous les tiles
				for(int j=0;j<tileLenW;j++)
				{					
					for(int row=0;row<hauteur;row++)
					{
						//parcours tous les pixels du tile
						for(int col=0;col<hauteur;col++)
						{
							f1[i][j].get(row, col,pixel);
							if(index==hauteur*hauteur)continue;
							temp[index][0]+=byteColorCVtoIntJava(pixel[0]);
							temp[index][1]+=byteColorCVtoIntJava(pixel[1]);
							temp[index][2]+=byteColorCVtoIntJava(pixel[2]);						
							index++;						
						}					
					}	
					index=0;
				}				
			}
			System.out.println("A");
			index=0;
			//on va faire l'inverse pour créer f2
			for(int i=0;i<tileLenH;i++)
			{
				//parcours tous les tiles
				for(int j=0;j<tileLenW;j++)
				{
					for(int row=0;row<hauteur;row++)
					{
						//parcours tous les pixels du tile
						for(int col=0;col<hauteur;col++)
						{		
							if(index==hauteur*hauteur)continue;
							double B=(temp[index][0]/(double)(tileLenH*tileLenW))*0.3*Math.cos(1);
							double G=(temp[index][1]/(double)(tileLenH*tileLenW))*0.3*Math.cos(1);
							double R=(temp[index][2]/(double)(tileLenH*tileLenW))*0.3*Math.cos(1);
							/*int B=(temp[index][0]/(tileLenH*tileLenW));
							int G=temp[index][1]/(tileLenH*tileLenW);
							int R=temp[index][2]/(tileLenH*tileLenW);*/
							pixel=new byte[]{(byte)B,(byte)G,(byte)R};
							//pixel=new byte[]{(byte)0,(byte)0,(byte)255};
							f2[i][j].put(row, col, pixel);
							index++;
						}
					}
					index=0;
				}				
			}
			gaussianTiles(f2[0][0], 3.0, 1);
			gaussianTiles(f2[0][1], 3.0, 1);
			gaussianTiles(f2[0][2], 3.0, 1);
		}
		catch(Exception e)
		{
			System.err.println("Regularize erreur:"+e.getMessage());
		}
		
	}
	public static void Gradient(Mat imageCV3,Mat imageCV4,GradientType type)
	{
		double[] color=new double[3];
		double[] greyCol=new double[3];
		byte[] pixelN=new byte[3];
		byte[] pixelS=new byte[3];
		byte[] pixelE=new byte[3];
		byte[] pixelW=new byte[3];
		
		int dx=0;
		int dy=0;
		int dB=0;
		for(int i=0;i<imageCV3.rows();i++)
		{
			for(int j=0;j<imageCV3.cols();j++)
			{					
				//
				imageCV3.get(i-1<0?i:(i-1), j,pixelN);
				imageCV3.get(i+1<imageCV3.rows()?i+1:i, j,pixelS);
				imageCV3.get(i, j+1<imageCV3.cols()?j+1:j,pixelE);
				imageCV3.get(i, j-1<0?j:j-1,pixelW);
				if(type==GradientType.Grey)
				{
					double[] N=new double[]{(pixelN[0]+pixelN[1]+pixelN[2])/3,(pixelN[0]+pixelN[1]+pixelN[2])/3,(pixelN[0]+pixelN[1]+pixelN[2])/3};
					double[] S=new double[]{(pixelS[0]+pixelS[1]+pixelS[2])/3,(pixelS[0]+pixelS[1]+pixelS[2])/3,(pixelS[0]+pixelS[1]+pixelS[2])/3};
					double[] E=new double[]{(pixelE[0]+pixelE[1]+pixelE[2])/3,(pixelE[0]+pixelE[1]+pixelE[2])/3,(pixelE[0]+pixelE[1]+pixelE[2])/3};
					double[] W=new double[]{(pixelW[0]+pixelW[1]+pixelW[2])/3,(pixelW[0]+pixelW[1]+pixelW[2])/3,(pixelW[0]+pixelW[1]+pixelW[2])/3};
					double ddB=Math.sqrt((E[0]-W[0])*(E[0]-W[0])+(N[0]-S[0])*(N[0]-S[0]));
					if(ddB>255)ddB=255;
					greyCol[0]=ddB;
					greyCol[1]=ddB;
					greyCol[2]=ddB;
					imageCV4.put(i, j, greyCol);					
				}
				else
				{
					dx=pixelE[0]-pixelW[0];
					dy=pixelN[0]-pixelS[0];
					dB=(int)Math.sqrt(dx*dx+dy*dy);				
					int dG=(int)Math.sqrt((pixelE[1]-pixelW[1])*(pixelE[1]-pixelW[1])+((pixelN[1]-pixelS[1])*(pixelN[1]-pixelS[1])));
					int dR=(int)Math.sqrt((pixelE[2]-pixelW[2])*(pixelE[2]-pixelW[2])+((pixelN[2]-pixelS[2])*(pixelN[2]-pixelS[2])));
					if(dB>255)dB=255;
					if(dG>255)dG=255;
					if(dR>255)dR=255;
					if(type==GradientType.B)
					{
						dG=0;dR=0;
					}
					else if(type==GradientType.R)
					{
						dB=0;dG=0;
					}
					else if(type==GradientType.G)
					{
						dB=0;
						dR=0;
					}					
					color[0]=dB;
					color[1]=dG;
					color[2]=dR;
					imageCV4.put(i, j, color);
				}
								
			}
		}
	}
	public static void Normal(Mat image,Mat resultat,GradientType type)
	{		
		int rows=image.rows();
		int cols=image.cols();
		double[] color=new double[3];
		double[] greyCol=new double[3];
		byte[] pixelNE=new byte[3];
		byte[] pixelSW=new byte[3];
		byte[] pixelNW=new byte[3];
		byte[] pixelSE=new byte[3];
		double dB,dR,dG;
		for(int i=0;i<rows;i++)
		{
			for(int j=0;j<cols;j++)
			{
				//
				if(i-1<0 && j+1>cols)image.get(i, j,pixelNE);
				else image.get(i-1, j+1,pixelNE);
				if(i-1<0 && j-1<0)image.get(i, j,pixelNW);
				else image.get(i-1, j-1,pixelNW);
				if(i+1>rows && j+1>cols)image.get(i, j,pixelSE);
				else image.get(i+1, j+1,pixelSE);
				if(i+1>rows && j-1<0)image.get(i, j,pixelSW);
				else image.get(i+1, j-1,pixelSW);				
				if(type==GradientType.Grey)
				{
					double[] NE=new double[]{(pixelNE[0]+pixelNE[1]+pixelNE[2])/3,(pixelNE[0]+pixelNE[1]+pixelNE[2])/3,(pixelNE[0]+pixelNE[1]+pixelNE[2])/3};
					double[] SE=new double[]{(pixelSE[0]+pixelSE[1]+pixelSE[2])/3,(pixelSE[0]+pixelSE[1]+pixelSE[2])/3,(pixelSE[0]+pixelSE[1]+pixelSE[2])/3};
					double[] NW=new double[]{(pixelNW[0]+pixelNW[1]+pixelNW[2])/3,(pixelNW[0]+pixelNW[1]+pixelNW[2])/3,(pixelNW[0]+pixelNW[1]+pixelNW[2])/3};
					double[] SW=new double[]{(pixelSW[0]+pixelSW[1]+pixelSW[2])/3,(pixelSW[0]+pixelSW[1]+pixelSW[2])/3,(pixelSW[0]+pixelSW[1]+pixelSW[2])/3};
					double ddB=Math.abs(Math.atan2(SE[0]-NW[0], SW[0]-NE[0])*255);
					if(ddB>255)ddB=255;
					greyCol[0]=ddB;
					greyCol[1]=ddB;
					greyCol[2]=ddB;
					resultat.put(i, j, greyCol);					
				}
				else if(type==GradientType.RGB)
				{					
					double ddB=Math.abs(Math.atan2(byteColorCVtoIntJava(pixelSE[0])-byteColorCVtoIntJava(pixelNE[0]),byteColorCVtoIntJava(pixelSW[0])-byteColorCVtoIntJava(pixelNW[0]))*255);
					double ddG=Math.abs(Math.atan2(byteColorCVtoIntJava(pixelSE[1])-byteColorCVtoIntJava(pixelNE[1]),byteColorCVtoIntJava(pixelSW[1])-byteColorCVtoIntJava(pixelNW[1]))*255);
					double ddR=Math.abs(Math.atan2(byteColorCVtoIntJava(pixelSE[2])-byteColorCVtoIntJava(pixelNE[2]),byteColorCVtoIntJava(pixelSW[2])-byteColorCVtoIntJava(pixelNW[2]))*255);
					if(ddB>255)ddB=255;
					if(ddR>255)ddR=255;
					if(ddG>255)ddG=255;
					greyCol[0]=ddB;
					greyCol[1]=ddG;
					greyCol[2]=ddR;
					resultat.put(i, j, greyCol);
				}
				else
				{									
					dB=Math.abs(Math.atan2(pixelSE[0]-pixelNW[0], pixelSW[0]-pixelNE[0])*255) ;
					dR=Math.abs(Math.atan2(pixelSE[2]-pixelNW[2], pixelSW[2]-pixelNE[2])*255);//
					dG=Math.abs(Math.atan2(pixelSE[1]-pixelNW[1], pixelSW[1]-pixelNE[1])*255);//
					if(type==GradientType.B)
					{
						dG=0;dR=0;
					}
					else if(type==GradientType.R)
					{
						dB=0;dG=0;
					}
					else if(type==GradientType.G)
					{
						dB=0;
						dR=0;
					}					
					color[0]=dB;
					color[1]=dG;
					color[2]=dR;
					resultat.put(i, j, color);
				}
			}
		}
		
	}
	public static BufferedImage cvToJava(Mat m)
	{
		int bufferSize=m.cols()*m.rows()*m.channels();
		BufferedImage image=new BufferedImage(m.cols(),m.rows(), BufferedImage.TYPE_3BYTE_BGR);
		byte[] data=new byte[bufferSize];
		byte[] dataDest=((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		m.get(0, 0, data);
		System.arraycopy(data, 0, dataDest, 0, bufferSize);
		return image;
	}
	public static Mat javaToCv(BufferedImage image)
	{
		Mat m=new Mat(image.getHeight(),image.getWidth(),CvType.CV_8UC3);
		int bufferSize=image.getHeight()*image.getWidth()*3;
		byte[] data=((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		byte[] dataDest=new byte[image.getHeight()*image.getWidth()*3];
		System.arraycopy(data, 0, dataDest, 0, bufferSize);
		m.put(0, 0, dataDest);
		return m;
	}
	public static double[] ChangeBase(double x,double y,double z,int[] centreP)
	{            
		double[] res = new double[3];      
		double[] u =new double[]{1,0,0};
		double[] v=new double[]{0,-1,0};//on met un signe - car les 2 répère n'ont pas les mêmes sens
		double[] w=new double[]{0,0,1};
	    res[0] = u[0] * x + centreP[0];
	    res[1] = v[1] * y + centreP[1];
	    res[2]=  w[2] * z+centreP[2];
	    return res;
	}
}
