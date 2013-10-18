/*
# DARPA XDATA licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with 
# the License.  You may obtain a copy of the License at 
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software 
# distributed under the License is distributed on an "AS IS" BASIS, 
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and  
# limitations under the License.
#
# Copyright 2013 Raytheon BBN Technologies Corp.  All Rights Reserved. 
#
*/

package fileformats;

import java.io.*;
import org.apache.hadoop.io.*;
import org.jblas.NativeBlas;

//probably want to add a sparse conversion and a sparse storage format
//may want to have this be a different class as well
public class MatrixBlock implements Writable, Serializable {

	//name of the matrix
	public String name;
	
	//size of the local block - this is the full block - doesn't take into account any trailing zeros or anything
	public int sR, sC;
	//location of local block within the global matrix
	public int nR, nC;
	
	public double[] vals;
	
	//maintain the array dynamically
	public int capacity;
	public int length;
	
	//for hadoop/spark may need to tell which operand it is - 0 is neutral, 1 is left, 2 is right

	
	public MatrixBlock () {sR = sC = nR = nC = length = capacity = 0; vals = new double[capacity]; name = "";} 
	public MatrixBlock (int r,int c) {sR = r; sC = c; nR = nC = 0; length = capacity = r*c; vals = new double[capacity]; name = "";}
	public MatrixBlock (String n, int r,int c) {sR = r; sC = c; nR = nC = 0; length = capacity = r*c; vals = new double[capacity]; name = n;}
	public MatrixBlock (int r, int c, int nr, int nc) {sR = r; sC = c; nR = nr; nC = nc; length = capacity = r*c; vals = new double[capacity]; name = "";}
	public MatrixBlock (String n, int r, int c, int nr, int nc) {sR = r; sC = c; nR = nr; nC = nc; length = capacity = r*c; vals = new double[capacity]; name = n;}
	
	//not sure if we use this - doesn't copy 
	public MatrixBlock (String n, double[] v, int r, int c, int nr, int nc) {sR = r; sC = c; nR = nr; nC = nc; length = capacity = r*c; vals = v; name = n;}
	
	public MatrixBlock (MatrixBlock b) {
		this.name = b.name;
		this.sR = b.sR;
		this.sC = b.sC;
		this.nR = b.nR;
		this.nC = b.nC;
		this.length = b.length;
		this.capacity = b.capacity;
		
		this.vals = new double[this.capacity];
		
		System.arraycopy(b.vals,0,this.vals,0,this.length);
	}
	
	public void set (MatrixBlock b) {
		this.name = b.name;
		this.sR = b.sR + 0;
		this.sC = b.sC + 0;
		this.nR = b.nR + 0;
		this.nC = b.nC + 0;
		
		//only re-allocate if we have to
		if (b.length >= this.length) {
			this.vals = new double[b.length];
		}
		
		this.length = b.length;
		this.capacity = b.length;
		
		System.arraycopy(b.vals,0,this.vals,0,this.length);
	}
		

	//add to the array - reallocate if necessary - scheme now is to set the capacity to twice the new index but this should be pre-allocated
	//Note that the extra capacity does not get serialized
	public void put(int r, int c, double v) {
		int idx = r * sC + c;
		
		if (idx >= capacity) {
			capacity = idx*2;
			double newVals[] = new double[capacity];
			
			//copy
			System.arraycopy(vals,0,newVals,0,length);

			//reassign
			vals = newVals;
		}
		
		if (idx >= length) {
			length = idx+1;
		}
		
		vals[idx] = v;
	}

	public double[] get() {return vals;}
	public double get(int r, int c) {return vals[r*sC+c];}
	public double get(int i) {return vals[i];}
	
	//math operations
	public MatrixBlock dGemm (MatrixBlock b, double alpha, boolean lTrans, boolean rTrans) {
		//create output MatrixBlock
		MatrixBlock c = new MatrixBlock(this.sR,b.sC,this.nR,b.nC);
				
		//char transa, char transb, int m, int n, int k, double alpha, double[] a, int aIdx, int lda, double[] b, int bIdx, int ldb, double beta, double[] c, int cIdx, int ldc
		char ltChar = lTrans? 'T' : 'N';
		char rtChar = rTrans? 'T' : 'N';
		
		//for some reason this worked backwards
		NativeBlas.dgemm(rtChar, ltChar, c.sR, c.sC, this.sC, alpha, b.vals, 0, b.sR, this.vals, 0, this.sR, 0, c.vals, 0, c.sR);
		
		System.out.printf("\tC: %d %d %d %d\n", c.nR, c.nC, c.sR, c.sC);
		
		
	
		return c;
	}
	
	public MatrixBlock dGemm (MatrixBlock b, double alpha, boolean lTrans, boolean rTrans, String resultName) {
		MatrixBlock c = dGemm(b,alpha,lTrans,rTrans);
		c.name = resultName;
		return c;
	}
		
	
	public MatrixBlock simpleMult (MatrixBlock b, double alpha, boolean lTrans, boolean rTrans) {
		MatrixBlock c = new MatrixBlock(this.name+"x"+b.name, this.sR,b.sC,this.nR,b.nC);
		
		if (alpha != 0) {
		
			if (lTrans && rTrans) {
				if (alpha == 1) {
					for (int j = 0; j < b.sR; j++) {
						for (int i = 0; i < sC; i++) {
							for (int k = 0; k < sR; k++) {
								c.put(i,j,c.get(i,j) + get(k,i) * b.get(j,k));
							}
						}
					}
				}
				
				else {
					for (int j = 0; j < b.sR; j++) {
						for (int i = 0; i < sC; i++) {
							for (int k = 0; k < sR; k++) {
								c.put(i,j,c.get(i,j) + alpha * get(k,i) * b.get(j,k));
							}
						}
					}
				}
			}
					
			else if (lTrans) {
				if (alpha == 1) {
					for (int k = 0; k < sR; k++) {
						for (int i = 0; i < sC; i++) {
							for (int j = 0; j < b.sC; j++) {
								c.put(i,j,c.get(i,j) + get(k,i) * b.get(k,j));
							}
						}
					}
				}
				else {
					for (int k = 0; k < sR; k++) {
						for (int i = 0; i < sC; i++) {
							for (int j = 0; j < b.sC; j++) {
								c.put(i,j,c.get(i,j) + alpha * get(k,i) * b.get(k,j));
							}
						}
					}
				}
			}
		
			else if (rTrans) {
				if (alpha == 1) {
					for (int i = 0; i < sR; i++) {
						for (int j = 0; j < sC; j++) {
							for (int k = 0; k < b.sC; k++) {
								c.put(i,j,c.get(i,j) + get(i,k) * b.get(j,k));
							}
						}
					}
				}
				else {
					for (int i = 0; i < sR; i++) {
						for (int j = 0; j < sC; j++) {
							for (int k = 0; k < b.sC; k++) {
								c.put(i,j,c.get(i,j) + alpha * get(i,k) * b.get(j,k));
							}
						}
					}
				}
			}
			
			else {
				if (alpha !=0 && alpha != 1) {
					for (int i = 0; i < sR; i++) {
						for (int k = 0; k < sC; k++) {
							for (int j = 0; j < b.sC; j++) {
								c.put(i,j,c.get(i,j) + this.get(i,k) * b.get(k,j) * alpha);
							}
						}
					}
				}
				else if (alpha == 1) {
					for (int i = 0; i < sR; i++) {
						for (int k = 0; k < sC; k++) {
							for (int j = 0; j < b.sC; j++) {
								c.put(i,j, c.get(i,j) + this.get(i,k) * b.get(k,j));
							}
						}
					}
				}
			}
		}
		else {
			int iMax = lTrans? sR : sC;
			int jMax = rTrans? b.sR : b.sC; 
		
			for (int i = 0; i < iMax; i++) {
				for (int j = 0; j < jMax; j++) {
					c.put(i,j,0);
				}
			}
		}
	
		return c;
	}
	
	public MatrixBlock simpleMult (MatrixBlock b, double alpha, boolean lTrans, boolean rTrans, String resultName) {
		MatrixBlock c = simpleMult(b,alpha,lTrans,rTrans);
		c.name = resultName;
		return c;
	}
		
	public double SquareTraceMult (MatrixBlock b, double alpha, boolean lTrans, boolean rTrans) {
		double contrib = 0;
		
		double right = 0, left = 0;
		for (int n = 0; n < sR; n++) {
			for (int k = 0; k < sR; k++) {
				left = lTrans? this.get(k,n) : this.get(n,k);
				right = rTrans? b.get(n,k) : b.get(k,n);
				
				contrib += left * right * alpha;
			}
		}
	
		return contrib;
	} 
					
					
			

	//c = aX+bY
	public MatrixBlock simpleAdd (MatrixBlock b, double alpha, double beta) {
		//create output MatrixBlock
		MatrixBlock c = new MatrixBlock(this.name+"+"+b.name, this.sR,this.sC, this.nR,this.nC);
		
		//just do this internally as it shouldn't take that long?
		if (alpha ==1 && beta ==1) {
			for (int i = 0; i < this.length; i++) {
				c.vals[i] = this.vals[i] + b.get(i);
			}
		}
		else if (alpha != 1) {
			for (int i = 0; i < this.length; i++) {
				c.vals[i] = alpha*this.vals[i] + b.get(i);
			}
		}
		else {
			for (int i = 0; i < this.length; i++) {
				c.vals[i] = this.vals[i] + beta*b.get(i);
			}
		}
		
		return c;
	}
	
	public MatrixBlock simpleAdd (MatrixBlock b, double alpha, double beta, String name) {
		MatrixBlock c = simpleAdd(b,alpha,beta);
		c.name = name;
		return c;
	}
	
	//a = aX + bY
	public void simpleAddIP (MatrixBlock b, double alpha, double beta) {
		//just do this internally as it shouldn't take that long?
		if (alpha ==1 && beta ==1) {
			for (int i = 0; i < this.length; i++) {
				this.vals[i] = this.vals[i] + b.get(i);
			}
		}
		else if (alpha != 1 && beta == 1) {
			for (int i = 0; i < this.length; i++) {
				this.vals[i] = alpha*this.vals[i] + b.get(i);
			}
		}
		else if (alpha ==1 && beta != 1) {
			for (int i = 0; i < this.length; i++) {
				this.vals[i] = this.vals[i] + beta*b.get(i);
			}
		}
		else {
			for (int i = 0; i < this.length; i++) {
				this.vals[i] = alpha*this.vals[i] + beta*b.get(i);
			}
		}
	}
	
	//y = aX + Y
	public MatrixBlock dAxpy (MatrixBlock b, double alpha) {
		//create output MatrixBlock
		MatrixBlock c = new MatrixBlock(this.name+"+"+b.name,this.sR,this.sC,this.nR,this.nC);
		
		NativeBlas.daxpy(this.length, alpha, this.vals, 0, 1, b.vals, 0, 1);
		
		return c;
	}
	
	//should add a multiply by scalar function and fake the aX + bY behavior
	public MatrixBlock simpleAdd (MatrixBlock b, double alpha, String name) {
		MatrixBlock c = dAxpy(b,alpha);
		c.name = name;
		return c;
	}
	
				
	@Override
	public void readFields(DataInput in) throws IOException {
		name = in.readUTF();
		sR = in.readInt();
		sC = in.readInt();
		nR = in.readInt();
		nC = in.readInt();
		length = capacity = in.readInt();
		
		vals = new double[length];
		for (int i = 0; i < length; i++) {
			vals[i] = in.readDouble();
		}
	}

	@Override
	public void write (DataOutput out) throws IOException {
		out.writeUTF(name);
		out.writeInt(sR);
		out.writeInt(sC);
		out.writeInt(nR);
		out.writeInt(nC);
	
		out.writeInt(length);
		
		for (int i = 0; i < length; i++) {
			out.writeDouble(vals[i]);
		}
	}
	
	public String toString() {
		String out = "";
		
		for (int i = 0; i < sR; i++) {
			for (int j = 0; j < sC; j++) {
				out+=vals[i*sR+j]+" ";
			}
		}
		
		return out;
	}
	
	//come back to the serialization issue
	//@Override
	// private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		// //out.defaultWriteObject();
		// out.writeInt(sR);
		// out.writeInt(sC);
		// out.writeInt(nR);
		// out.writeInt(nC);
		// out.writeInt(length);
		// for (int i = 0; i < length; i++) {
			// out.writeDouble(vals[i]);
		// }
	// }
	
	// //@Override
	// private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		// sR = in.readInt();
		// sC = in.readInt();
		// nR = in.readInt();
		// nC = in.readInt();
		// length = capacity = in.readInt();
		
		// vals = new double[length];
		// for (int i = 0; i < length; i++) {
			// vals[i] = in.readDouble();
		// }
	// }
}