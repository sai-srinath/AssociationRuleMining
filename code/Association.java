import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
public class Association{
	public static void main(String[] args) throws IOException{
		
		
		//declaring matrix to input dataset
		String[][] db = new String[100][103];
		//loading the input file 
		try {
			BufferedReader br = new BufferedReader(new FileReader(args[0]));			
			String line;
			int i=0;
			while((line=br.readLine()) != null){
				
				String[] row = line.split("\\s+");
				
				
				int j=0;
				for(String s:row){
					db[i][j]=s;					
					
					j++;
					}
				
				i++;				
			}
					
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		Scanner in = new Scanner(System.in);
		System.out.println("Enter the support and confidence(in %)");
		int support = in.nextInt();
		double confidence=in.nextDouble();
		confidence=confidence/100;
		int sum=0;
		
		
		
		ConcurrentHashMap<String,Integer> c1 = new ConcurrentHashMap<String,Integer>();
		//for storing possible subset values
		ConcurrentHashMap<String,Integer> store = new ConcurrentHashMap<String,Integer>();
		for(int i=1;i<101;i++){
			int upcount=0;
			int downcount=0;
			for(int j=0;j<100;j++){
				if(db[j][i].equals("UP"))
					upcount++;
				else
					downcount++;
			}
			String k1 = String.valueOf(i)+"_UP";
			String k2= String.valueOf(i)+"_Down";
			c1.put(k1, upcount);
			c1.put(k2, downcount);
		}
		int ct1=0,ct2=0,ct3=0,ct4=0;
		for(int j=0;j<100;j++){
				
			if(db[j][101].equals("ALL"))
				ct1++;				
			else if(db[j][101].equals("AML"))
				ct2++;
			else if(db[j][101].equals("Colon"))
				ct3++;
			else
				ct4++;
		c1.put("101"+"_"+"ALL", ct1);
		c1.put("101"+"_"+"AML", ct2);
		c1.put("101"+"_"+"Colon", ct3);
		c1.put("101"+"_"+"Breast", ct4);
	    
		}
			
		prune(c1,support);
				
		System.out.println("Size of frequent itemset = 1: "+c1.size());
		sum=sum+c1.size();
		
		String[] c2 = secondpass(c1);
				
		ConcurrentHashMap<String,Integer> c_next = new ConcurrentHashMap<String,Integer>();
		c_next=find_freq(c2,db);		
		//System.out.println("before pruning No is: "+c_next.size());
		prune(c_next,support);	
		copy(c_next,store);
		System.out.println("Size of frequent itemset = 2: "+c_next.size());
		sum=sum+c_next.size();
		
		
		int n=3;
		while(!c_next.isEmpty()){
		String[] test=genericpass(c_next,n);
		
		c_next.clear();	
		c_next=find_freq(test,db);
		
		prune(c_next,support);
		System.out.println("Size of frequent itemset = " +n +" : "+c_next.size());
		copy(c_next,store);
		sum=sum+c_next.size();
		n++;
		}
		
		
		System.out.println("Total is: "+sum);
		
		
		//going to add to ruleset
		ArrayList<ruleset> rules = new ArrayList<ruleset>();
		String[] arr=new String[store.size()];
		for(String s:store.keySet()){
			arr=store.keySet().toArray(new String[store.size()]);	
		}
		
		for(int i=0;i<arr.length;i++)
			for(int j=i+1;j<arr.length;j++){
				
				
				if(store.get(arr[i])<store.get(arr[j])){
					
					double conf = (double)store.get(arr[i])/(double)store.get(arr[j]);
					
					int supp = store.get(arr[i]);
					String left=arr[j];
					String right=arr[i].replace(arr[j],"");
					ruleset rule = new ruleset();
					if(conf>=confidence){
						rule.setparams(left,right);
						rule.set_conf(conf);
						rule.set_support(supp);
						rules.add(rule);
					}
						
					
					
				}
				else{
					double conf = (double)store.get(arr[j])/(double)store.get(arr[i]);
					
					int supp = store.get(arr[j]);
					String left=arr[i];
					String right=arr[j].replace(arr[i],"");
					ruleset rule = new ruleset();
					if(conf>=confidence){
						rule.setparams(left,right);
						rule.set_conf(conf);
						rule.set_support(supp);
						rules.add(rule);
					}
					
				}
				
			}
			
		
		for(ruleset r:rules){
			System.out.println("LHS: "+r.LHS.replace("-",",")+" RHS: "+r.RHS.replace("-",",")+" confidence:"+r.confidence+" support: "+r.support);
			
		}
		System.out.println("Number of rules generated: "+rules.size());
		
		
		
	}
	static void prune(ConcurrentHashMap<String,Integer> map,int support){
		for(String s:map.keySet()){
			if(map.get(s)<support)
				map.remove(s);
				
		}			
	}
	
	static String[] secondpass(ConcurrentHashMap<String,Integer> map){
		int no = map.size();
		
		String[] tmp = new String[no];
		no=no*(no-1)/2;
		
		String[] result = new String[no];
		int i=0;
		int key_length=map.keySet().toString().length();
		tmp=map.keySet().toString().substring(1,key_length-1).split(", ");
		
		
		
		int k=0;
		for(i=0;i<tmp.length;i++)
			for(int j=i+1;j<tmp.length;j++){
				result[k]=tmp[i]+"-"+tmp[j];
				k++;
			}
		
		return result;
	}
	
	static ConcurrentHashMap<String,Integer> find_freq(String[] s1,String[][] m){
		ConcurrentHashMap<String,Integer> res = new ConcurrentHashMap<String,Integer>();
		for(int i=0;i<s1.length;i++){
			String line=s1[i];
			String[] tok = line.split("-");
			int tok_no=tok.length;
			int k=0;
			int ct=0;
			
			
			for(int j=0;j<100;j++){
				while(k<tok_no){
					if(m[j][Integer.parseInt(tok[k].split("_")[0])].equals(tok[k].split("_")[1]))						
						k++;						
					else 
						break;
				}
				if(k==tok_no)
					ct++;
				k=0;				
			}
			res.put(line, ct);
				
		}	
		return res;
		
	}
	
	static String[] genericpass(ConcurrentHashMap<String,Integer> map,int n){
		
		int size = map.size();
		
				
		int key_length=map.keySet().toString().length();
		String[] tmp = new String[key_length];
		
		tmp=map.keySet().toString().substring(1,key_length-1).split(", ");
		
		
		//to remove last element
		String[][] orig = new String[tmp.length][n-1];
		for(int i=0;i<tmp.length;i++) {
			orig[i]=tmp[i].split("-");
			
		}
		String[][] pattern = new String[tmp.length][n-1];
		for(int i=0;i<tmp.length;i++){
			pattern[i]=Arrays.copyOf(orig[i], n-2);
			
			
		}
				
		ArrayList<String> result = new ArrayList<String>();
		
		
		//now to match within pattern
		for(int i=0;i<pattern.length;i++){
			for(int j=i+1;j<pattern.length;j++){
				
				if(Arrays.toString(pattern[i]).equals(Arrays.toString(pattern[j]))){
					String var = Arrays.toString(pattern[i]);				
					String var2=var.replace(", ", "-");				
					var2=var2.replace("[","");
					var2=var2.replace("]","");					
							
					result.add(var2+"-"+orig[i][n-2]+"-"+orig[j][n-2]);					
				}					
			}
		}
									
		
	    String[] final_result=result.toArray(new String[result.size()]);
	   
	    
		
		
		return final_result;
	}
	
	static  void copy(ConcurrentHashMap<String,Integer> original,ConcurrentHashMap<String,Integer> added){
		for(String s:original.keySet())
			added.put(s, original.get(s));
		
	}
	static double find_conf(int lhs,int lhsrhs){
		return (double)lhsrhs/(double)lhs;
		
	}
		
	
	
}

class ruleset{
 
	public String LHS;
	public String RHS;
	public int LHS_count;
	public int RHS_count;
	public double confidence;
	public int support;
	
	public void setparams(String l,String r){
		LHS=l;
		RHS=r;
		
		
	}
	public void set_conf(double a){
		confidence=a;
	}
	public void set_support(int a){
		support=a;
	}
	
	
	
	
}