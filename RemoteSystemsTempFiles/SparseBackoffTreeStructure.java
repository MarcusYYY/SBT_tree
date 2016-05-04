
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.*;

public class SparseBackoffTreeStructure {
	int [] _numLeaves; //the number of leaf nodes descendant from each child.  All 1s if children are leaves
	int [] _numLeavesHereAndLeft; //convenience.  _numLeavesToLeft[j] = sum of _numLeaves[i] for i<=j

	SparseBackoffTreeStructure [] _children; // if null, children are leaves (= random variable values)
	int _minGlobalIndex; //minimum global index of descendent leaf
	double _delta; //discount subtracted from every leaf and added to this node's smoother
	
	//assumes all entries of branches are > 0
	public SparseBackoffTreeStructure(ArrayList<ArrayList<Integer>> branches, int idx1, int idx2) {
		this(branches, new double[branches.size()], 0, 0, 0);
	}
	
	public SparseBackoffTreeStructure(ArrayList<ArrayList<Integer>> branches, int idx1, int idx2,int startingGlobalIndex) {
		this(branches, new double[branches.size()], idx1, idx2, startingGlobalIndex);
	}
	
	public SparseBackoffTreeStructure(ArrayList<ArrayList<Integer>> branches, double [] discounts, int idx1, int idx2,int startingGlobalIndex) {
		_minGlobalIndex = startingGlobalIndex;
		_numLeaves = new int[branches.get(idx1).get(idx2)];
		_numLeavesHereAndLeft = new int[branches.get(idx1).get(idx2)];
		_delta = discounts[idx1];
		int sum = 0;
		int summ = 0;
		for (int i = 0; i < idx2 ; i++) {
			summ += branches.get(idx1).get(i);
		}
		if(idx1 < branches.size() - 1) {
			_children = new SparseBackoffTreeStructure[branches.get(idx1).get(idx2)];
			for(int i=0; i<_children.length; i++) {
				_children[i] = new SparseBackoffTreeStructure(branches, discounts, idx1+1, summ + i, sum + _minGlobalIndex);
				_numLeaves[i] = _children[i].sumLeaves();
				sum += _numLeaves[i];
				_numLeavesHereAndLeft[i] = sum;
			}
		}
		else {
			Arrays.fill(_numLeaves, 1);
			for(int i=0; i<_numLeavesHereAndLeft.length; i++) {
				_numLeavesHereAndLeft[i] = i+1;
			}
		}
	}
	public static  ArrayList<ArrayList<Integer>> extract_list(SparseBackoffTreeStructure root){
		ArrayList<ArrayList<Integer>> result = new ArrayList<>();
		if(root == null) return result;
		ArrayList<SparseBackoffTreeStructure> list_of_node = new ArrayList<>();
		list_of_node.add(root);
		
		while(!list_of_node.isEmpty()){
			int size = list_of_node.size();
			ArrayList<Integer> sublist = new ArrayList<Integer>();
			while(size > 0){
				SparseBackoffTreeStructure node = list_of_node.remove(0);
				if(node._children != null){
					sublist.add(node._children.length);
					for(int i = 0; i < node._children.length; i++){
						list_of_node.add(node._children[i]);
					}
				}
				else{
					sublist.add(node._numLeaves.length);
				}
				size--;
			}
			result.add(sublist);
		}
		return result;	
	}

	public static  SparseBackoffTreeStructure grow(SparseBackoffTreeStructure root, ArrayList<Integer> method, , ArrayList<Integer> discounts){
		SparseBackoffTreeStructure result = root;
		if(method.length == 0) return result;
		ArrayList<SparseBackoffTreeStructure> queue = new ArrayList<>();
		queue.add(root);
		childIdx = 0
		ArrayList<ArrayList<Integer>> branch = new ArrayList<ArrayList<Integer>>();
		double [] discount;
		while(!queue.isEmpty()){
			int size = queue.size();
			while(size > 0){
				SparseBackoffTreeStructure node = queue.remove(0);
				if(node._children != null){
					for(int i = 0; i < node._children.length; i++){
						queue.add(node._children[i]);
					}
				}
				else{
					node._children = new SparseBackoffTreeStructure[node._numLeaves.length];
					for(int i=0; i<_children.length; i++) {
						temp = new ArrayList<Integer>();
						temp.add(method(childIdx));
						branch.add(temp);
						discount[0] = discounts[childIdx];
						node._children[i] = new SparseBackoffTreeStructure(branch, discount, 0, childIdx, sum + _minGlobalIndex);
						node._numLeaves[i] = _children[i].sumLeaves();
						sum += _numLeaves[i];
						node._numLeavesHereAndLeft[i] = sum;
						childIdx += 1;
					}

				}
				size--;
			}
		}
		return result;	
	}
	
	//assumes all entries of branches are > 0
	public SparseBackoffTreeStructure(ArrayList<ArrayList<Integer>> branches) {
		this(branches, 0, 0);
	}
	
	public SparseBackoffTreeStructure(ArrayList<ArrayList<Integer>> branches, double [] discounts) {
		this(branches, discounts, 0,0,0);
	}
	
	public int sumLeaves() {
		int sum = 0;
		for(int i=0; i<_numLeaves.length; i++)
			sum += _numLeaves[i];
		return sum;
	}
	
	//returns {childIndex, leafIndexInChild}
	public int [] getLocalIndex(int leafIndex) {
		int [] out = new int[2];
		System.out.println(_numLeavesHereAndLeft[0]);
		if(_children == null) {
			out[0] = leafIndex;
			out[1] = 0;
		}
		else {
			for(int i=0; i<_numLeavesHereAndLeft.length; i++) {
				if(_numLeavesHereAndLeft[i] > leafIndex) {
					out[0] = i;
					if(i>0)
						out[1] = leafIndex - _numLeavesHereAndLeft[i-1];
					else 
						out[1] = leafIndex;
					break;
				}
			}
		}
		return out;
	}
	
	public int numLeaves() {
		return _numLeavesHereAndLeft[_numLeavesHereAndLeft.length - 1];
	}
	
	//only meaningful for leaf children
	public int getGlobalIndex(int childIndex) {
		return _minGlobalIndex + childIndex;
	}
	
	public int randomLeaf(Random r) {
		int ch = r.nextInt(numLeaves());
		return ch + _minGlobalIndex;
	}
	
	public int [] getLocalIdxTrace(int leafIndex) {
		ArrayList<Integer> localIdx = new ArrayList<Integer>();
		SparseBackoffTreeStructure struct = this;
		while(struct._children != null) {
			int [] idxs = struct.getLocalIndex(leafIndex);
			int childIdx = idxs[0];
			localIdx.add(childIdx);
			leafIndex = idxs[1];
			struct = struct._children[childIdx];
		}
		int [] idxs = struct.getLocalIndex(leafIndex);
		int childIdx = idxs[0];
		localIdx.add(childIdx);
		
		int [] out = new int[localIdx.size()];
		Iterator<Integer> it = localIdx.iterator();
		for(int i=0; i<out.length; i++) {
			out[i] = it.next();
		}
		return out;
	}
	
	public double [] getDiscountTrace(int [] localIdxTrace) {
		double [] out = new double[localIdxTrace.length];
		SparseBackoffTreeStructure struct = this;
		for(int i=0; i<localIdxTrace.length; i++) {
			out[i] = struct._delta;
			if(i<localIdxTrace.length-1)
				struct = struct._children[localIdxTrace[i]];
		}
		return out;
	}
	
	public static boolean testRandomLeaf() {
		ArrayList<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> sublist_1 = new ArrayList<Integer>();
		ArrayList<Integer> sublist_2 = new ArrayList<Integer>();
		ArrayList<Integer> sublist_3 = new ArrayList<Integer>();
		sublist_1.add(2);
		sublist_2.add(1);
		sublist_2.add(2);
		sublist_3.add(1);
		sublist_3.add(2);
		sublist_3.add(3);
		list.add(sublist_1);
		list.add(sublist_2);
		list.add(sublist_3);
		
		SparseBackoffTreeStructure struct = new SparseBackoffTreeStructure(list);
//		Random r = new Random();
//		TIntIntHashMap cts = new TIntIntHashMap();
//		for(int i=0; i<10000; i++) {
//			int d = struct._children[1].randomLeaf(r);
//			cts.adjustOrPutValue(d,  1,  1);
//		}
		System.out.println("The branching factor is [[2],[1,2],[1,2,3]]");
				
		System.out.println("The first node's leaves should be 1 and the second's should be 5 at the second layer");
		if(struct._numLeaves[0] == 1 && struct._numLeaves[1] == 5){
			System.out.println("Test 1 passed");
		}
		System.out.println("The first node's leaves should be 1 , the second's should be 2 and the last's should be 3 at the third layer");
		if(struct._children[0]._numLeaves[0] == 1 && struct._children[1]._numLeaves[0] == 2 && struct._children[1]._numLeaves[1] == 3 )
			System.out.println("Test 2 passed");
		//System.out.println(struct._numLeavesHereAndLeft[1]);
		//System.out.println("should be roughly uniform from 6 to 11:");
		//System.out.println(cts.toString());
		return false;
	}
	
	public static boolean testGetLocalIndex() {
		ArrayList<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> sublist_1 = new ArrayList<Integer>();
		ArrayList<Integer> sublist_2 = new ArrayList<Integer>();
		ArrayList<Integer> sublist_3 = new ArrayList<Integer>();
		sublist_1.add(2);
		sublist_2.add(1);
		sublist_2.add(2);
		sublist_3.add(1);
		sublist_3.add(2);
		sublist_3.add(3);
		list.add(sublist_1);
		list.add(sublist_2);
		list.add(sublist_3);
		SparseBackoffTreeStructure struct = new SparseBackoffTreeStructure(list);
		int [] lidx = struct.getLocalIndex(3);
		boolean passed = true;
		if(lidx[0]==0) {
			System.out.println("test 1 passed.");
		}
		else {
			System.err.println("test 1 failed: " + lidx[0] + " should be " + 0);
			passed = false;
		}
		if(lidx[1]==3) {
			System.out.println("test 2 passed.");
		}
		else {
			System.err.println("test 2 failed: " + lidx[1] + " should be " + 3);
			passed = false;
		}
		return passed;
		
	}
	
	public static boolean testExtract(){
		ArrayList<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> sublist_1 = new ArrayList<Integer>();
		ArrayList<Integer> sublist_2 = new ArrayList<Integer>();
		ArrayList<Integer> sublist_3 = new ArrayList<Integer>();
		sublist_1.add(2);
		sublist_2.add(1);
		sublist_2.add(2);
		sublist_3.add(1);
		sublist_3.add(2);
		sublist_3.add(3);
		list.add(sublist_1);
		list.add(sublist_2);
		list.add(sublist_3);
		SparseBackoffTreeStructure struct = new SparseBackoffTreeStructure(list);
		ArrayList<ArrayList<Integer>> list_of_tree = new ArrayList<>();
		list_of_tree = extract_list(struct);
        
		boolean pass_ = list_of_tree.equals(list);

		if (pass_)
			System.out.print("Test passed");
		else
			System.out.print(String.valueOf(list_of_tree) + "should be " + String.valueOf(list));
		return false;
	}
	
	public static void main(String args[]) {
		testRandomLeaf();
		//testGetLocalIndex();
		testExtract();
	}
	
}
