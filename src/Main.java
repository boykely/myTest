import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.swing.*;
import javax.tools.JavaFileObject;
import javax.xml.crypto.Data;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.ejml.data.DenseMatrix64F;
import org.ejml.simple.SimpleMatrix;
import org.opencv.core.*;
import org.opencv.imgproc.*;


public class Main 
{
	public static enum GradientType
	{
		R,G,B,RGB,Grey,RG
	}
	public static void Normal(Mat image,Mat resultat,GradientType type)
	{		
		try
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
					if(i-1>=0 && j+1<cols)
					{
						image.get(i-1, j+1,pixelNE);
					}
					else
					{
						image.get(i, j,pixelNE);
					}
					if(i-1>=0 && j-1>=0)image.get(i-1, j-1,pixelNW);
					else image.get(i, j,pixelNW);
					if(i+1<rows && j+1<cols)image.get(i+1, j+1,pixelSE);
					else image.get(i, j,pixelSE);
					if(i+1<rows && j-1<=0)image.get(i+1, j-1,pixelSW);
					else image.get(i, j,pixelSW);				
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
						double ddB=Math.abs(Math.atan2(pixelSE[0]-pixelNW[0],pixelSW[0]-pixelNE[0])*255);//(byteColorCVtoIntJava(pixelSE[0])-byteColorCVtoIntJava(pixelNW[0]),byteColorCVtoIntJava(pixelSW[0])-byteColorCVtoIntJava(pixelNE[0]))*255);
						double ddG=Math.abs(Math.atan2(pixelSE[1]-pixelNW[1],pixelSW[1]-pixelNE[1])*255);//(Math.atan2(byteColorCVtoIntJava(pixelSE[1])-byteColorCVtoIntJava(pixelNW[1]),byteColorCVtoIntJava(pixelSW[1])-byteColorCVtoIntJava(pixelNE[1]))*255);
						double ddR=Math.abs(Math.atan2(pixelSE[2]-pixelNW[2],pixelSW[2]-pixelNE[2])*255);//(Math.atan2(byteColorCVtoIntJava(pixelSE[2])-byteColorCVtoIntJava(pixelNW[2]),byteColorCVtoIntJava(pixelSW[2])-byteColorCVtoIntJava(pixelNE[2]))*255);
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
						else if(type==GradientType.RG)
						{
							dB=0;
						}
						color[0]=dB;
						color[1]=dG;
						color[2]=dR;
						resultat.put(i, j, color);
					}
				}
			}	
		}
		catch(Exception e)
		{
			System.out.println("Pb"+e.getMessage());
		}		
	}
	public static int gl=0;
	public static void main(String[] args)
	{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		try
		{
			
			String dir="C:\\Users\\ralambomahay1\\Downloads\\Java_workspace\\newGit\\Data\\SampleTransportReflectance\\Final\\";//"C:\\Users\\ralambomahay1\\Downloads\\Java_workspace\\newGit\\Data\\";
			String path="source_tile_";
			Color av=AverageColor(dir+"flash.jpg");
			//BufferedImage[][] tiles=new BufferedImage[12][16];
			int tileWNumber=17;
			int tileHNumber=12;
			int rows=tileHNumber*192;
			int cols=tileWNumber*192;
			int brdfEstimation=tileWNumber*tileHNumber;
			
			
			int originS=0;
			int originT=0;
			double[] cam=(ChangeBase(new double[]{0,0,1}));
			double[] lum=(ChangeBase(new double[]{0,0,1}));
			double[] E=new double[3];
			double[] L=new double[3];
			double[] H=new double[3];
			double D2;
			Mat image=new Mat(rows,cols,CvType.CV_8UC3);
			System.out.println("Start!!");
			Mat normalR=Imgcodecs.imread(dir+"normal.jpg");
			System.out.println("ok");
			System.out.println(normalR);
			//Mat diffuse=Imgcodecs.imread("C:\\Users\\ralambomahay1\\Downloads\\Java_workspace\\newGit\\Data\\SampleTransportReflectance\\Final\\render ros=0.jpg");
			//Mat spec=Imgcodecs.imread("C:\\Users\\ralambomahay1\\Downloads\\Java_workspace\\newGit\\Data\\SampleTransportReflectance\\Final\\render rod=0.jpg");
			for(int i=0;i<tileHNumber;i++)
			{
				for(int j=0;j<tileWNumber;j++)
				{
					FileReader fileB=new FileReader(dir+"testB.txt");
					FileReader fileG=new FileReader(dir+"testG.txt");
					FileReader fileR=new FileReader(dir+"testR.txt");
					byte[] normal=new byte[3];
					byte[] diff=new byte[3];
					byte[] sp=new byte[3];
					BufferedReader readerB=new BufferedReader(fileB);
					BufferedReader readerG=new BufferedReader(fileG);
					BufferedReader readerR=new BufferedReader(fileR);
					String lb=readerB.readLine();
					String lg=readerG.readLine();
					String lr=readerR.readLine();
					for(int s=0;s<192;s++)
					{
						for(int t=0;t<192;t++)
						{
							String[] brdfParamB=lb.split("//");
							String[] brdfParamG=lg.split("//");
							String[] brdfParamR=lr.split("//");
							originS=s+192*i;
							originT=t+192*j;
							double[] pos=(new double[]{originS,originT,0});
							E=normalize(XY(pos,cam));
							L=calculeL(pos, lum);
							double[] le=addXY(L,E);
							H=normalize(le);
							D2=dot(E,E);
							normalR.get(originS, originT,normal);
							//diffuse.get(originS, originT, diff);
							//spec.get(originS, originT, sp);
							/*double blue=color(byteColorCVtoIntJava(diff[0]),byteColorCVtoIntJava(sp[0]),pos,E,L,H,D2,byteColorCVtoIntJava(normal[2]),byteColorCVtoIntJava(normal[1]),byteColorCVtoIntJava(normal[0]),av,Math.abs(Double.parseDouble(brdfParamB[0])),Math.abs(Double.parseDouble(brdfParamB[1])),Math.abs(Double.parseDouble(brdfParamB[8])));
							double green=color(byteColorCVtoIntJava(diff[1]),byteColorCVtoIntJava(sp[1]),pos,E,L,H,D2,byteColorCVtoIntJava(normal[2]),byteColorCVtoIntJava(normal[1]),byteColorCVtoIntJava(normal[0]),av,Math.abs(Double.parseDouble(brdfParamG[0])),Math.abs(Double.parseDouble(brdfParamG[1])),Math.abs(Double.parseDouble(brdfParamG[8])));
							double red=color(byteColorCVtoIntJava(diff[2]),byteColorCVtoIntJava(sp[2]),pos,E,L,H,D2,byteColorCVtoIntJava(normal[2]),byteColorCVtoIntJava(normal[1]),byteColorCVtoIntJava(normal[0]),av,Math.abs(Double.parseDouble(brdfParamR[0])),Math.abs(Double.parseDouble(brdfParamR[1])),Math.abs(Double.parseDouble(brdfParamR[8])));*/
							/*double blue=color(av.getBlue(),av.getBlue(),pos,E,L,H,D2,byteColorCVtoIntJava(normal[2]),byteColorCVtoIntJava(normal[1]),byteColorCVtoIntJava(normal[0]),av,Math.abs(Double.parseDouble(brdfParamB[0])),Math.abs(Double.parseDouble(brdfParamB[1])),Math.abs(Double.parseDouble(brdfParamB[8])));
							double green=color(av.getGreen(),av.getGreen(),pos,E,L,H,D2,byteColorCVtoIntJava(normal[2]),byteColorCVtoIntJava(normal[1]),byteColorCVtoIntJava(normal[0]),av,Math.abs(Double.parseDouble(brdfParamG[0])),Math.abs(Double.parseDouble(brdfParamG[1])),Math.abs(Double.parseDouble(brdfParamG[8])));
							double red=color(av.getRed(),av.getRed(),pos,E,L,H,D2,byteColorCVtoIntJava(normal[2]),byteColorCVtoIntJava(normal[1]),byteColorCVtoIntJava(normal[0]),av,Math.abs(Double.parseDouble(brdfParamR[0])),Math.abs(Double.parseDouble(brdfParamR[1])),Math.abs(Double.parseDouble(brdfParamR[8])));*/
							double blue=color(av.getBlue(),av.getBlue(),pos,E,L,H,D2,Double.parseDouble(brdfParamB[5]),Double.parseDouble(brdfParamB[6]),2,av,Math.abs(Double.parseDouble(brdfParamB[0])),Math.abs(Double.parseDouble(brdfParamB[1])),Math.abs(Double.parseDouble(brdfParamB[8])),Double.parseDouble(brdfParamB[7]));
							double green=color(av.getGreen(),av.getGreen(),pos,E,L,H,D2,Double.parseDouble(brdfParamG[5]),Double.parseDouble(brdfParamG[6]),2,av,Math.abs(Double.parseDouble(brdfParamG[0])),Math.abs(Double.parseDouble(brdfParamG[1])),Math.abs(Double.parseDouble(brdfParamG[8])),Double.parseDouble(brdfParamG[7]));
							double red=color(av.getRed(),av.getRed(),pos,E,L,H,D2,Double.parseDouble(brdfParamR[5]),Double.parseDouble(brdfParamR[6]),2,av,Math.abs(Double.parseDouble(brdfParamR[0])),Math.abs(Double.parseDouble(brdfParamR[1])),Math.abs(Double.parseDouble(brdfParamR[8])),Double.parseDouble(brdfParamR[7]));
							image.put(originS, originT, new byte[]{(byte)(blue),(byte)(green),(byte)(red)});
							lb=readerB.readLine();
							lg=readerG.readLine();
							lr=readerR.readLine();
						}
					}
				}
			}
			System.out.println("save");
			saveTile(image, dir+"render.jpg");
			System.out.println("fin");
		}
		catch(Exception e)
		{
			System.err.println("Erreur general:"+e.getMessage());
		}		
	}
	public static double color(double d,double s,double[] pos,double[] E,double[] L,double[] H,double D2,double nx,double ny,double nz,Color lightColor,double rod,double ros,double alpha,double tof)
	{
		double value=0;
		double m=0.3;
		double[] N=normalize(XY(pos,ChangeBase(new double[]{nx,ny,nz})));
		double angle=Math.acos(dot(N,H))/m;
		double spec=1*Math.exp(-(angle*angle));
		double cosine=Math.max(0, dot(N,E));
		
		double v=((spec*ros)+rod);
		v=Math.sqrt(v);
		value=v>256?255:v<0?d:v;
		return value;
	}
	/*
	 * Fonction pour la construction (Re-render)
	 */
	public static int[] ChangeBase(int[] xyz)
	{
	        int[] P = new int[3];
	        int[] u = new int[] { 1, 0, 0 };
	        int[] v = new int[] { 0, 1, 0 };
	        int[] w = new int[] { 0, 0, 1 };
	        /*
	        P[0] = u[0] * xyz[0]+(3264) ;
	        P[1] = v[1] * xyz[1] +(0);
	        P[2] = w[2]*xyz[2];*/
	        P[0] = u[0] * xyz[0]+(2304/2) ;//optimize7
	        P[1] = v[1] * xyz[1] +(3264/2);
	        P[2] = w[2]*xyz[2];
	        return P;
	 }
	public static double[] ChangeBase(double[] xyz)
	{
		double[] P = new double[3];
		double[] u = new double[] { 1, 0, 0 };
		double[] v = new double[] { 0, 1, 0 };
		double[] w = new double[] { 0, 0, 1 };
        /*P[0] = u[0] * xyz[0]+(3263) ;
        P[1] = v[1] * xyz[1] +(0);
        P[2] = w[2]*xyz[2];*/
		P[0] = u[0] * xyz[0]+(2304/2) ;//optimize7
        P[1] = v[1] * xyz[1] +(3264/2);
	        P[2] = w[2]*xyz[2];
        return P;
	}
	public static int[] XY(int[] x,int[] y)
	{
		return new int[]{y[0]-x[0],y[1]-x[1],y[2]-x[2]};
	}
	public static double[] XY(double[] x,double[] y)
	{
		return new double[]{y[0]-x[0],y[1]-x[1],y[2]-x[2]};
	}
	public static double[] normalize(int[] x)
	{
		double norm=Math.sqrt(x[0]*x[0]+x[1]*x[1]+x[2]*x[2]);
		if(norm==0)return new double[3];
		return new double[]{x[0]/norm,x[1]/norm,x[2]/norm};
	}
	public static double[] normalize(double[] x)
	{
		double norm=Math.sqrt(x[0]*x[0]+x[1]*x[1]+x[2]*x[2]);
		return new double[]{x[0]/norm,x[1]/norm,x[2]/norm};
	}
	public static double[] calculeL(int[]x,int[]y)
	{
		int[] xy=XY(x,y);
		return normalize(xy);
	}
	public static double[] calculeL(double[]x,double[]y)
	{
		double[] xy=XY(x,y);
		return normalize(xy);
	}
	public static double[] addXY(double[]x,double[] y)
	{
		return new double[]{x[0]+y[0],x[1]+y[1],x[2]+y[2]};
	}
	public static double dot(double[] x,double[] y)
	{
		if(x.length==2)return x[0]*y[0]+x[1]*y[1];
		return x[0]*y[0]+x[1]*y[1]+x[2]*y[2];
	}
	public static double[] calculeHn(double[] h,double[] n,SimpleMatrix r)
	{		
		SimpleMatrix ha=new SimpleMatrix(new double[][]{{h[0]},{h[1]},{h[2]}});
		SimpleMatrix first=ha.plus(r.mult(ha)).plus(1);
		SimpleMatrix second=r.mult(r.mult(ha)).mult(new SimpleMatrix(new double[][]{{n[2]+1}}));
		return first.elementDiv(second).getMatrix().data;		
	}
	public static double[] div(double[] xy,double e)
	{
		return new double[]{xy[0]/e,xy[1]/e,xy[2]/e};
	}
	public static double[] calculeHnpW(SimpleMatrix m,double x,double y)
	{
		SimpleMatrix xy=new SimpleMatrix(new double[][]{
			{x},
			{y}
		});
		SimpleMatrix temp=m.mult(xy);
		double[] res=temp.getMatrix().data;
		return new double[]{res[0],res[1],1};
	}
	public static Color AverageColor(String path)
	{
		Color c=null;
		int temp;
		int tempR=0;
		int tempG=0;
		int tempB=0;
		try 
		{
			BufferedImage image=ImageIO.read(new File(path));
			//BufferedImage image=ImageIO.read(new File("C:\\Users\\ralambomahay1\\Downloads\\Java_workspace\\newGit\\Data\\book_black_input_flash.jpg"));
			for(int i=0;i<image.getHeight();i++)
			{
				for(int j=0;j<image.getWidth();j++)
				{
					temp=image.getRGB(j, i);
					c=new Color(temp);
					tempR+=c.getRed();
					tempG+=c.getGreen();
					tempB+=c.getBlue();
				}
			}
			tempR/=image.getHeight()*image.getWidth();
			tempG/=image.getHeight()*image.getWidth();
			tempB/=image.getHeight()*image.getWidth();
			c=new Color(tempR,tempG,tempB);
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			System.out.println("AverageColor erreur:"+e.getMessage());
		}
		return c;
	}
	
	public static void saveTile(Mat m,String path)
	{
		try
		{				
			int type=BufferedImage.TYPE_3BYTE_BGR;
			int bufferSize=m.channels()*m.cols()*m.rows();
			byte[] data=new byte[bufferSize];
			m.get(0, 0, data);
			BufferedImage image=new BufferedImage(m.cols(), m.rows(), type);
			final byte[] containerPixels=((DataBufferByte)image.getRaster().getDataBuffer()).getData();
			System.arraycopy(data, 0, containerPixels, 0, bufferSize);		
			//save image		
			ImageIO.write(image, "jpg", new File(path));			
		}
		catch(IOException e)
		{
			System.err.println("Error saving file");
		}		
	}
	public static void gaussianTiles(Mat m,double size,double sigmaX)
	{
		//Mat newM=new Mat(m.rows(),m.cols(),CvType.CV_8UC3);
		Imgproc.GaussianBlur(m, m, new Size(size, size), sigmaX);
		//return newM;
	}
	public static Mat convertTileToCV(BufferedImage im)
	{
		Mat m=new Mat(im.getHeight(), im.getWidth(), CvType.CV_8UC3);
		byte[] data=((DataBufferByte)im.getRaster().getDataBuffer()).getData();
		m.put(0, 0, data);
		return m;
	}	
	public static int byteColorCVtoIntJava(byte b)
	{		
		int i=(b+128)+128;		
		return b>=0?(int)b:i;
	}
	public static void Histogramme(Mat m,int[] hist)
	{		
		//for Grey level images
		byte[] pixel=new byte[3];
		for(int i=0;i<m.rows();i++)
		{
			for(int j=0;j<m.cols();j++)
			{					
				m.get(i, j,pixel);				
				hist[byteColorCVtoIntJava(pixel[0])]+=1;				
			}
		}
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
	public static void HistogrammeHSV(BufferedImage image,float[] H,float[]S)
	{
		float[] hsb;
		int c;
		Color col;
		for(int i=0;i<image.getHeight();i++)
		{
			for(int j=0;j<image.getWidth();j++)
			{
				c=image.getRGB(j, i);
				col=new Color(c);
				hsb=Color.RGBtoHSB(col.getRed(), col.getGreen(), col.getBlue(), null);
				//System.out.println("R:"+col.getRed()+"G:"+col.getGreen()+"B:"+col.getBlue()+"<>H:"+hsb[0]+"S:"+hsb[1]+"V(B):"+hsb[2]);
				
			}
		}
	}
	public static void HistogrammeCumuleRGB(Mat m,int[] R,int[] G,int[] B,int[] RC,int[] GC,int[] BC,int N)
	{
		//System.out.println("Le nombre total des pixel:"+N);
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
	public static void HistogrammeCumule(Mat m,int[] hist,int[] histoCumul)
	{
		int value=0;		
		for (int i=0;i<256;i++)
		{							    
		    value+=hist[i];
		    histoCumul[i]=value;		    
		}
	}
	public static void InverseHistogrammeCumule(int[] histoCumul,Hashtable<Integer, Integer> InvHistoCumul)
	{
		for(int i=0;i<histoCumul.length;i++)
		{			
			InvHistoCumul.put(histoCumul[i], i);
		}
	}
	public static void MatchingHistogram(Mat imRef,Mat imTarget,Mat result)
	{
		//imRef => image de référence
		//imTarget => image à changer d'histogramme comme l'imRef
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
		//on va essayer de calculer le gradient du ref 
		//Imgproc.Sobel(imTarget, imTarget, imTarget.depth(), 1, 1);
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
		System.out.println("CollapsePyramid:"+gaussTar.size()+"/"+pyramidTar.size());
		result=collapsePyramid(pyramidTar,gaussTar);
		MatchingHistogram(imRef, result, result);
		return result;
	}
	public static void EqualHistogram(Mat im,Mat result)
	{
		int[] RRef=new int[256];int[] RCRef=new int[256];
		int[] GRef=new int[256];int[] GCRef=new int[256];
		int[] BRef=new int[256];int[] BCRef=new int[256];
		int N=im.cols()*im.rows();
		HistogrammeRGB(im, RRef, GRef, BRef);
		HistogrammeCumuleRGB(im, RRef, GRef, BRef,RCRef,GCRef,BCRef,N);
		byte[] pixel=new byte[3];
		//System.out.println(im);System.out.println(result);
		for(int i=0;i<im.rows();i++)
		{
			for(int j=0;j<im.cols();j++)
			{
				im.get(i, j,pixel);
				double r=RCRef[byteColorCVtoIntJava(pixel[2])]*255/N;
				double g=GCRef[byteColorCVtoIntJava(pixel[1])]*255/N;
				double b=BCRef[byteColorCVtoIntJava(pixel[0])]*255/N;
				result.put(i, j, new double[]{b,g,r});
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
	public static Mat collapsePyramid(List<Mat> pyramid,List<Mat>gauss)
	{
		/*Si Core.add dans LaplacianPyramid
		 * alors i>0
		 * on ne fait que imgproc.pyrUp
		 * Sinon i>=0 et Core.add
		 * NB:Si les 2 images (Ref et Tar sont très différents => il vaut mieux utiliser Imgproc.pyrUp sinon Core.add
		 */
		int i=pyramid.size()-1;
		Mat temp=new Mat();		
		while(i>=0)
		{
			//Imgproc.pyrUp(pyramid.get(i), temp,pyramid.get(i-1).size());			
			Core.add(pyramid.get(i), gauss.get(i), temp);
			//
			i--;
		}
		System.out.println("valeur de i="+i);
		System.out.println(temp);
		return temp;
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
	public static double convertDouble(double x)
	{		
		return ((int)(x*100000000))/100000000.;
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
		//trions temp pour avoir la minimal
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
		//
		return inv_cumul.get(tempHash.get(temp[0]));
	}
	public static void gradientX(Mat im1,Mat im2)
	{
		int width=im1.cols();
		int height=im1.rows();
		byte[] pixelD=new byte[3];
		byte[] pixelG=new byte[3];
		byte[] pixelH=new byte[3];
		byte[] pixelB=new byte[3];
		byte[] pixelC=new byte[3];
		byte[] pixel=new byte[3];
		for(int i=0;i<height;i++)
		{
			for(int j=0;j<width;j++)
			{
				im1.get(i, j,pixelC);
				if(j+1>width)pixelD=new byte[]{0,0,0};					
				else im1.get(i, j+1,pixelD);
				if(j-1<0)pixelG=new byte[]{0,0,0};
				else im1.get(i, j-1,pixelG);
				if(i-1<0)pixelH=new byte[]{0,0,0};
				else im1.get(i-1, j,pixelH);
				if(i+1>im1.height())pixelB=new byte[]{0,0,0};
				else im1.get(i+1, j,pixelB);
				int bx=(byteColorCVtoIntJava(pixelC[0])-byteColorCVtoIntJava(pixelG[0]));//byteColorCVtoIntJava(pixelG[0]));
				int gx=(byteColorCVtoIntJava(pixelC[1])-byteColorCVtoIntJava(pixelG[1]));//byteColorCVtoIntJava(pixelG[1]));
				int rx=(byteColorCVtoIntJava(pixelC[2])-byteColorCVtoIntJava(pixelG[2]));//byteColorCVtoIntJava(pixelG[2]));
				
				pixel[0]=(byte)bx;
				pixel[1]=(byte)gx;
				pixel[2]=(byte)rx;
			
				im2.put(i, j, pixel);
			}
		}
	}
	public static void gradientY(Mat im1,Mat im2)
	{
		int width=im1.cols();
		int height=im1.rows();
		byte[] pixelD=new byte[3];
		byte[] pixelG=new byte[3];
		byte[] pixelH=new byte[3];
		byte[] pixelB=new byte[3];
		byte[] pixel=new byte[3];
		for(int i=0;i<height;i++)
		{
			for(int j=0;j<width;j++)
			{
				if(j+1>width)pixelD=new byte[]{0,0,0};					
				else im1.get(i, j+1,pixelD);
				if(j-1<0)pixelG=new byte[]{0,0,0};
				else im1.get(i, j-1,pixelG);
				if(i-1<0)pixelH=new byte[]{0,0,0};
				else im1.get(i-1, j,pixelH);
				if(i+1>im1.height())pixelB=new byte[]{0,0,0};
				else im1.get(i+1, j,pixelB);				
				int by=(byteColorCVtoIntJava(pixelH[0])-byteColorCVtoIntJava(pixelB[0]));
				int gy=(byteColorCVtoIntJava(pixelH[1])-byteColorCVtoIntJava(pixelB[1]));
				int ry=(byteColorCVtoIntJava(pixelH[2])-byteColorCVtoIntJava(pixelB[2]));
				pixel[0]=(byte)by;
				pixel[1]=(byte)gy;
				pixel[2]=(byte)ry;			
				im2.put(i, j, pixel);
			}
		}
	}
	public static void gradientMagnitude(Mat im1,Mat im2)
	{
		int width=im1.cols();
		int height=im1.rows();
		byte[] pixelD=new byte[3];
		byte[] pixelG=new byte[3];
		byte[] pixelH=new byte[3];
		byte[] pixelB=new byte[3];
		byte[] pixel=new byte[3];
		for(int i=0;i<height;i++)
		{
			for(int j=0;j<width;j++)
			{
				if(j+1>width)pixelD=new byte[]{0,0,0};					
				else im1.get(i, j+1,pixelD);
				if(j-1<0)pixelG=new byte[]{0,0,0};
				else im1.get(i, j-1,pixelG);
				if(i-1<0)pixelH=new byte[]{0,0,0};
				else im1.get(i-1, j,pixelH);
				if(i+1>im1.height())pixelB=new byte[]{0,0,0};
				else im1.get(i+1, j,pixelB);
				int bx=(byteColorCVtoIntJava(pixelD[0])-byteColorCVtoIntJava(pixelG[0]));
				int gx=(byteColorCVtoIntJava(pixelD[1])-byteColorCVtoIntJava(pixelG[1]));
				int rx=(byteColorCVtoIntJava(pixelD[2])-byteColorCVtoIntJava(pixelG[2]));
				int by=(byteColorCVtoIntJava(pixelH[0])-byteColorCVtoIntJava(pixelB[0]));
				int gy=(byteColorCVtoIntJava(pixelH[1])-byteColorCVtoIntJava(pixelB[1]));
				int ry=(byteColorCVtoIntJava(pixelH[2])-byteColorCVtoIntJava(pixelB[2]));
				
				pixel[0]=(byte)(Math.sqrt(bx*bx+by*by));
				pixel[1]=(byte)(Math.sqrt(gx*gx+gy*gy));
				pixel[2]=(byte)(Math.sqrt(rx*rx+ry*ry));
				im2.put(i, j, pixel);
			}
		}
	}
	public static void gradientXtoOrigin(Mat gradient,Mat origin)
	{
		int width=gradient.cols();
		int height=gradient.rows();
		byte[] pixelD=new byte[3];
		byte[] pixelG=new byte[3];
		byte[] pixelH=new byte[3];
		byte[] pixelB=new byte[3];
		byte[] pixel=new byte[]{0,0,0};
		for(int i=0;i<height;i++)
		{
			for(int j=0;j<width;j++)
			{
				gradient.get(i, j,pixelD);
				
				pixel[0]=(byte)(byteColorCVtoIntJava(pixel[0])+byteColorCVtoIntJava(pixelD[0]));
				pixel[1]=(byte)(byteColorCVtoIntJava(pixel[1])+byteColorCVtoIntJava(pixelD[1]));
				pixel[2]=(byte)(byteColorCVtoIntJava(pixel[2])+byteColorCVtoIntJava(pixelD[2]));
				origin.put(i, j, pixel);
				
			}
			
		}
	}
	public static void talohaMain(int a)
	{
		JFrame frame=new JFrame("Titre");
		frame.setSize(new Dimension(1400, 450));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);		
		FlowLayout layout=new FlowLayout();
		frame.setLayout(layout);
		try
		{
			BufferedImage guide=ImageIO.read(new File("C:\\Users\\ralambomahay1\\Downloads\\stage\\ImageTest\\test-3-tiles-Gaussian\\gauss.jpg"));
			//BufferedImage guide=ImageIO.read(new File("C:\\Users\\ralambomahay1\\Downloads\\tsi.jpg"));
			BufferedImage source=ImageIO.read(new File("C:\\Users\\ralambomahay1\\Downloads\\stage\\ImageTest\\test-3-tiles-Gaussian\\messi.jpg"));
			
			
			
			JLabel labelGuide=new JLabel(new ImageIcon(guide));	
			JLabel labelSource=new JLabel(new ImageIcon(source));
			JLabel labelResult=new JLabel();
			
			JScrollPane scroll=new JScrollPane(labelGuide);
			JScrollPane scroll_=new JScrollPane(labelSource);
			JScrollPane scroll__=new JScrollPane(labelResult);
			
			scroll.setPreferredSize(new Dimension(300, 450));
			scroll_.setPreferredSize(new Dimension(300, 450));
			scroll__.setPreferredSize(new Dimension(500, 450));
			
			frame.add(scroll);
			frame.add(scroll_);
			frame.add(scroll__);
			
			//traitement
			Mat guideMat=new Mat(guide.getHeight(),guide.getWidth(),CvType.CV_8UC3);
			Mat sourceMat=new Mat(source.getHeight(),source.getWidth(),CvType.CV_8UC3);
			Mat resultMat=new Mat(guide.getHeight(),guide.getWidth(),CvType.CV_8UC3);
			Mat gradientGuide=new Mat(guide.getHeight(),guide.getWidth(),CvType.CV_8UC3);
			Mat gradientSource=new Mat(source.getHeight(),source.getWidth(),CvType.CV_8UC3);
			byte[] guideData=((DataBufferByte)guide.getRaster().getDataBuffer()).getData();
			byte[] sourceData=((DataBufferByte)source.getRaster().getDataBuffer()).getData();
			guideMat.put(0, 0, guideData);
			sourceMat.put(0, 0, sourceData);
			System.err.println(guideMat);
			//System.err.println(sourceMat);
			Mat dx=new Mat();
			Mat dy=new Mat();
			Mat carre=new Mat();
			//Imgproc.Sobel(guideMat, dx, guideMat.depth(), 1, 0);		
			//Imgproc.Sobel(guideMat, dy, guideMat.depth(),0, 1);
			//Core.pow(dx, 2,dx);
			//Core.pow(dy, 2, dy);			
			//Core.add(dx, dy, carre);
			//gradient guide
			byte[] pixel=new byte[3];			
			byte[] pixelD=new byte[3];
			byte[] pixelG=new byte[3];
			byte[] pixelH=new byte[3];
			byte[] pixelB=new byte[3];
			for(int i=0;i<guideMat.rows();i++)
			{
				for(int j=0;j<guideMat.cols();j++)
				{					
					if(i<sourceMat.width() && j<sourceMat.height())
					{
						if(j+1>sourceMat.width())pixelD=new byte[]{0,0,0};					
						else sourceMat.get(i, j+1,pixelD);
						if(j-1<0)pixelG=new byte[]{0,0,0};
						else sourceMat.get(i, j-1,pixelG);
						if(i-1<0)pixelH=new byte[]{0,0,0};
						else sourceMat.get(i-1, j,pixelH);
						if(i+1>sourceMat.height())pixelB=new byte[]{0,0,0};
						else sourceMat.get(i+1, j,pixelB);
						sourceMat.get(i, j,pixel);
						int bx=(byteColorCVtoIntJava(pixelD[0])-byteColorCVtoIntJava(pixel[0]));
						int gx=(byteColorCVtoIntJava(pixelD[1])-byteColorCVtoIntJava(pixel[1]));
						int rx=(byteColorCVtoIntJava(pixelD[2])-byteColorCVtoIntJava(pixel[2]));
						int by=(byteColorCVtoIntJava(pixelH[0])-byteColorCVtoIntJava(pixel[0]));
						int gy=(byteColorCVtoIntJava(pixelH[1])-byteColorCVtoIntJava(pixel[1]));
						int ry=(byteColorCVtoIntJava(pixelH[2])-byteColorCVtoIntJava(pixel[2]));
						pixel[0]=(byte)(Math.sqrt(bx*bx+by*by));
						pixel[1]=(byte)(Math.sqrt(gx*gx+gy*gy));
						pixel[2]=(byte)(Math.sqrt(rx*rx+ry*ry));
						//sourceMat.get(i, j,pixel);
						/*pixel[0]=(byte)(byteColorCVtoIntJava(pixel[0])*4-bx-by);
						pixel[1]=(byte)(byteColorCVtoIntJava(pixel[1])*4-gx-gy);
						pixel[2]=(byte)(byteColorCVtoIntJava(pixel[2])*4-rx-ry);*/
						gradientSource.put(i, j, pixel);
					}
					if(j+1>guideMat.width())pixelD=new byte[]{0,0,0};					
					else guideMat.get(i, j+1,pixelD);
					if(j-1<0)pixelG=new byte[]{0,0,0};
					else guideMat.get(i, j-1,pixelG);
					if(i-1<0)pixelH=new byte[]{0,0,0};
					else guideMat.get(i-1, j,pixelH);
					if(i+1>guideMat.height())pixelB=new byte[]{0,0,0};
					else guideMat.get(i+1, j,pixelB);
					guideMat.get(i, j,pixel);
					int bx=(byteColorCVtoIntJava(pixelD[0])-byteColorCVtoIntJava(pixel[0]));
					int gx=(byteColorCVtoIntJava(pixelD[1])-byteColorCVtoIntJava(pixel[1]));
					int rx=(byteColorCVtoIntJava(pixelD[2])-byteColorCVtoIntJava(pixel[2]));
					int by=(byteColorCVtoIntJava(pixelH[0])-byteColorCVtoIntJava(pixel[0]));
					int gy=(byteColorCVtoIntJava(pixelH[1])-byteColorCVtoIntJava(pixel[1]));
					int ry=(byteColorCVtoIntJava(pixelH[2])-byteColorCVtoIntJava(pixel[2]));
					pixel[0]=(byte)(Math.sqrt(bx*bx+by*by));
					pixel[1]=(byte)(Math.sqrt(gx*gx+gy*gy));
					pixel[2]=(byte)(Math.sqrt(rx*rx+ry*ry));
					/*pixel[0]=(byte)(byteColorCVtoIntJava(pixel[0])*4-bx-by);
					pixel[1]=(byte)(byteColorCVtoIntJava(pixel[1])*4-gx-gy);
					pixel[2]=(byte)(byteColorCVtoIntJava(pixel[2])*4-rx-ry);*/
					
					gradientGuide.put(i, j, pixel);
				}
			}			
			//Calcule matrice A=NxN
			Mat A=new Mat(sourceMat.rows(),sourceMat.cols(),CvType.CV_8UC1);
			//Mat A=new Mat(3,3,CvType.CV_8UC1);
			//byte[] a_data=new byte[]{5 ,-2 , 3,-3 , 9 , 1 ,2 ,-1, -7 };
			//A.put(0, 0, a_data);
			for(int i=0;i<A.rows();i++)
			{
				for(int j=0;j<A.cols();j++)
				{					
					if(i==j)
					{
						A.put(i, j, new byte[]{-4});						
						if(i-1>=0)A.put(i-1, j, new byte[]{1});
						if(i+1<A.rows())A.put(i+1, j, new byte[]{1});
						if(j-1>=0)A.put(i, j-1, new byte[]{1});
						if(j+1<A.cols())A.put(i, j+1, new byte[]{1});						
					}
					else
					{						
						A.put(i, j, new byte[]{0});
					}
				}
			}
			
			//Calcule B=vecteur contenant le gradient
			double[][] X=new double[3][];
			Mat B=new Mat(A.rows(),1,CvType.CV_8UC1);//pour les 3 channels (RGB)
			System.out.println(A);
			System.out.println(B);
			double alpha=0.5;
			/*byte[] b_data=new byte[]{-1,2,3,-1,2,3,-1,2,3};
			B.put(0, 0, b_data);
			Jacobi jc1=new Jacobi(A,B);
			X[0]=jc1.solve();
			System.out.println(X[0][0]);*/
			//à copier ici

			//composante bleu
			byte[] blue=new byte[1];
			for(int i=0;i<A.rows();i++)
			{
				for(int j=0;j<A.cols();j++)
				{
					gradientGuide.get(i, j,pixelG);
					
					gradientSource.get(i, j,pixelD);
					byte[] h=new byte[3];
					byte[] b=new byte[3];
					byte[] d=new byte[3];
					byte[] g=new byte[3];
					if(i-1>=0)sourceMat.get(i-1, j,h);
					if(i+1<sourceMat.rows())sourceMat.get(i, j,b);
					if(j-1>=0)sourceMat.get(i, j-1,g);
					if(j+1<sourceMat.cols())sourceMat.get(i, j+1,d);
					
					int bx=(byteColorCVtoIntJava(pixelD[0])+byteColorCVtoIntJava(pixelG[0]));
					int gx=(byteColorCVtoIntJava(pixelD[1])+byteColorCVtoIntJava(pixelG[1]));
					int rx=(byteColorCVtoIntJava(pixelD[2])+byteColorCVtoIntJava(pixelG[2]));
					int by=(byteColorCVtoIntJava(pixelH[0])+byteColorCVtoIntJava(pixelB[0]));
					int gy=(byteColorCVtoIntJava(pixelH[1])+byteColorCVtoIntJava(pixelB[1]));
					int ry=(byteColorCVtoIntJava(pixelH[2])+byteColorCVtoIntJava(pixelB[2]));int Bguide=byteColorCVtoIntJava(pixelG[0]);int Bsource=byteColorCVtoIntJava(pixelD[0]);
					int Gguide=byteColorCVtoIntJava(pixelG[1]);int Gsource=byteColorCVtoIntJava(pixelD[1]);
					int Rguide=byteColorCVtoIntJava(pixelG[2]);int Rsource=byteColorCVtoIntJava(pixelD[2]);
					/*pixelD[0]=(byte)((alpha*Bguide+(1-alpha)*Bsource)*(alpha*Bguide+(1-alpha)*Bsource));
					pixelD[1]=(byte)((alpha*Gguide+(1-alpha)*Gsource)*(alpha*Gguide+(1-alpha)*Gsource));
					pixelD[2]=(byte)((alpha*Rguide+(1-alpha)*Rsource)*(alpha*Rguide+(1-alpha)*Rsource));*/
					//System.out.println(pixelD[0]+":"+pixelD[1]+":"+pixelD[2]);
					//blue[0]=(byte)(pixelG[0]+h[0]+b[0]+d[0]+g[0]);
					blue[0]=(byte)((byteColorCVtoIntJava(pixelD[0])+byteColorCVtoIntJava(pixelG[0]))/2);
					B.put(i, 0, blue);
				}
			}	
			
			Jacobi jcB=new Jacobi(A, B);
			X[0]=jcB.solve();
			//composante verte
			byte[] green=new byte[1];
			for(int i=0;i<A.rows();i++)
			{
				for(int j=0;j<A.cols();j++)
				{
					gradientGuide.get(i, j,pixelG);
					
					gradientSource.get(i, j,pixelD);
					byte[] h=new byte[3];
					byte[] b=new byte[3];
					byte[] d=new byte[3];
					byte[] g=new byte[3];
					if(i-1>=0)sourceMat.get(i-1, j,h);
					if(i+1<sourceMat.rows())sourceMat.get(i, j,b);
					if(j-1>=0)sourceMat.get(i, j-1,g);
					if(j+1<sourceMat.cols())sourceMat.get(i, j+1,d);
					int Bguide=byteColorCVtoIntJava(pixelG[0]);int Bsource=byteColorCVtoIntJava(pixelD[0]);
					int Gguide=byteColorCVtoIntJava(pixelG[1]);int Gsource=byteColorCVtoIntJava(pixelD[1]);
					int Rguide=byteColorCVtoIntJava(pixelG[2]);int Rsource=byteColorCVtoIntJava(pixelD[2]);
					/*pixelD[0]=(byte)((alpha*Bguide+(1-alpha)*Bsource)*(alpha*Bguide+(1-alpha)*Bsource));
					pixelD[1]=(byte)((alpha*Gguide+(1-alpha)*Gsource)*(alpha*Gguide+(1-alpha)*Gsource));
					pixelD[2]=(byte)((alpha*Rguide+(1-alpha)*Rsource)*(alpha*Rguide+(1-alpha)*Rsource));*/
					//System.out.println(pixelD[0]+":"+pixelD[1]+":"+pixelD[2]);
					//green[0]=(byte)(pixelG[1]+h[1]+b[1]+d[1]+g[1]);
					green[0]=(byte)((byteColorCVtoIntJava(pixelD[1])+byteColorCVtoIntJava(pixelG[1]))/2);
					B.put(i, 0, green);
				}
			}			
			Jacobi jcG=new Jacobi(A, B);
			X[1]=jcG.solve();
			//Composante rouge
			byte[] red=new byte[1];
			for(int i=0;i<A.rows();i++)
			{
				for(int j=0;j<A.cols();j++)
				{
					gradientGuide.get(i, j,pixelG);
					
					gradientSource.get(i, j,pixelD);
					byte[] h=new byte[3];
					byte[] b=new byte[3];
					byte[] d=new byte[3];
					byte[] g=new byte[3];
					if(i-1>=0)sourceMat.get(i-1, j,h);
					if(i+1<sourceMat.rows())sourceMat.get(i, j,b);
					if(j-1>=0)sourceMat.get(i, j-1,g);
					if(j+1<sourceMat.cols())sourceMat.get(i, j+1,d);
					int Bguide=byteColorCVtoIntJava(pixelG[0]);int Bsource=byteColorCVtoIntJava(pixelD[0]);
					int Gguide=byteColorCVtoIntJava(pixelG[1]);int Gsource=byteColorCVtoIntJava(pixelD[1]);
					int Rguide=byteColorCVtoIntJava(pixelG[2]);int Rsource=byteColorCVtoIntJava(pixelD[2]);
					/*pixelD[0]=(byte)((alpha*Bguide+(1-alpha)*Bsource)*(alpha*Bguide+(1-alpha)*Bsource));
					pixelD[1]=(byte)((alpha*Gguide+(1-alpha)*Gsource)*(alpha*Gguide+(1-alpha)*Gsource));
					pixelD[2]=(byte)((alpha*Rguide+(1-alpha)*Rsource)*(alpha*Rguide+(1-alpha)*Rsource));*/
					//System.out.println(pixelD[0]+":"+pixelD[1]+":"+pixelD[2]);
					//red[0]=(byte)(pixelG[2]+h[2]+b[2]+d[2]+g[2]);
					red[0]=(byte)((byteColorCVtoIntJava(pixelD[2])+byteColorCVtoIntJava(pixelG[2]))/2);//(pixelG[2]*pixelD[2])<255?(byte)(pixelG[2]*pixelD[2]):(byte)255;
					B.put(i, 0, red);
				}
			}			
			Jacobi jcR=new Jacobi(A, B);
			X[2]=jcR.solve();
			byte[] kaka=new byte[3];			
			gradientGuide.get(0, 0,kaka);
			double[] border=new double[]{byteColorCVtoIntJava(kaka[0]),byteColorCVtoIntJava(kaka[1]),byteColorCVtoIntJava(kaka[2])};//{X[0][0],X[1][0],X[2][0]};
			byte[] finalP=new byte[]{0,0,0};
			System.out.println(X[0].length+" / "+sourceMat.cols());
			for(int i=0;i<guideMat.rows();i++)
			{
				for(int j=0;j<guideMat.cols();j++)
				{
					guideMat.get(i, j,pixelG);
					if(i<sourceMat.height() && j<sourceMat.width())
					{
						sourceMat.get(i, j,pixelD);
						/*
						//alpha blending
						int Bguide=byteColorCVtoIntJava(pixelG[0]);int Bsource=byteColorCVtoIntJava(pixelD[0]);
						int Gguide=byteColorCVtoIntJava(pixelG[1]);int Gsource=byteColorCVtoIntJava(pixelD[1]);
						int Rguide=byteColorCVtoIntJava(pixelG[2]);int Rsource=byteColorCVtoIntJava(pixelD[2]);
						pixelD[0]=(byte)(0.25*Bguide+(1-0.25)*Bsource);
						pixelD[1]=(byte)(0.25*Gguide+(1-0.25)*Gsource);
						pixelD[2]=(byte)(0.25*Rguide+(1-0.25)*Rsource);
						//
						//Mixing gradients*/
						pixelD[0]=(byte)(X[0][j]+byteColorCVtoIntJava(pixelG[0]));
						pixelD[1]=(byte)(X[1][j]+byteColorCVtoIntJava(pixelG[1]));
						pixelD[2]=(byte)(X[2][j]+byteColorCVtoIntJava(pixelG[2]));
										
						resultMat.put(i, j, pixelD);
						
					}
					else
					{						
						resultMat.put(i, j, pixelG);
					}
				}
			}
			BufferedImage imageResult=new BufferedImage(resultMat.cols(), resultMat.rows(), BufferedImage.TYPE_3BYTE_BGR);
			byte[] data=((DataBufferByte)imageResult.getRaster().getDataBuffer()).getData();
			byte[] resultData=new byte[resultMat.cols()*resultMat.rows()*resultMat.channels()];
			resultMat.get(0,0,resultData);
			System.arraycopy(resultData, 0, data, 0, resultMat.cols()*resultMat.rows()*resultMat.channels());
			labelResult.setIcon(new ImageIcon(imageResult));
		}
		catch(Exception e)
		{
			System.out.println("Erreur:"+e.getMessage());
		}
		JTextArea text=new JTextArea();
		frame.add(text);
		frame.setVisible(true);
		System.out.println((byte)254);
	}
	public static void talohaMain()
	{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);		
		JFrame frame=new JFrame("Titre");
		frame.setSize(new Dimension(1400, 450));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);		
		FlowLayout layout=new FlowLayout();
		frame.setLayout(layout);
		try
		{
			BufferedImage guide=ImageIO.read(new File("C:\\Users\\ralambomahay1\\Downloads\\stage\\ImageTest\\test-3-tiles-Gaussian\\gauss.jpg"));
			//BufferedImage guide=ImageIO.read(new File("C:\\Users\\ralambomahay1\\Downloads\\tsi.jpg"));
			BufferedImage gradient=ImageIO.read(new File("C:\\Users\\ralambomahay1\\Downloads\\stage\\ImageTest\\test-3-tiles-Gaussian\\gauss.jpg"));
			BufferedImage finalImage=new BufferedImage(guide.getWidth(),guide.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
			Mat guideMat=new Mat(guide.getHeight(),guide.getWidth(),CvType.CV_8UC3);int nombrePixel=guideMat.rows()*guideMat.cols()*guideMat.channels();byte[] guideData=new byte[nombrePixel];
			Mat gradientMat=new Mat(guide.getHeight(),guide.getWidth(),CvType.CV_8UC3);byte[] gradientData=new byte[nombrePixel];
			Mat finalMat=new Mat(guide.getHeight(),guide.getWidth(),CvType.CV_8UC3);byte[] finalData=new byte[nombrePixel];
			guideMat.put(0, 0, ((DataBufferByte)guide.getRaster().getDataBuffer()).getData());
			gradientX(guideMat, gradientMat);
			gradientMat.get(0,0,gradientData);
			byte[] gd= ((DataBufferByte)guide.getRaster().getDataBuffer()).getData();
			System.arraycopy(gradientData,0, gd, 0, nombrePixel);
			//construire finalMat à partir de gradientX
			gradientXtoOrigin(gradientMat, finalMat);
			//
			finalMat.get(0, 0,finalData);
			byte[] fd= ((DataBufferByte)finalImage.getRaster().getDataBuffer()).getData();
			System.arraycopy(finalData,0, fd, 0, nombrePixel);
			JLabel labelGuide=new JLabel(new ImageIcon(guide));	
			JLabel labelSource=new JLabel(new ImageIcon(gradient));
			JLabel labelResult=new JLabel(new ImageIcon(finalImage));
			
			JScrollPane scroll=new JScrollPane(labelGuide);
			JScrollPane scroll_=new JScrollPane(labelSource);
			JScrollPane scroll__=new JScrollPane(labelResult);
			
			scroll.setPreferredSize(new Dimension(300, 450));
			scroll_.setPreferredSize(new Dimension(300, 450));
			scroll__.setPreferredSize(new Dimension(500, 450));
			
			frame.add(scroll);
			frame.add(scroll_);
			frame.add(scroll__);
			
		}
		catch(Exception e)
		{
			System.out.println("Erreur:"+e.getMessage());
		}
		frame.setVisible(true);
		
	}
}
