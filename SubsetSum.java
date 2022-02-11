import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubsetSum{

    /* This commneted code helped me test and debug this secific method it's not important in the grand scheme of the algorithm. 

    public static void main(String[] args){
        final long startTime = System.currentTimeMillis();
        int n = 7;
        int value = magicValue(n);
        ArrayList<Integer> ints = constructListOfInts(n);
        List<Set<Integer>> sols = new ArrayList<>();
        sols = subsetSum(ints, n, value);
        System.out.println(sols.toString() + "\n\nTotal number of unique vector solutions whose elements sum to the magic value: " +  sols.size());
        final long elapsedTimeMillis = System.currentTimeMillis() - startTime;
        
        // Inform the user of how long the total process took. 
        System.out.println("Total time to finish in milliseconds: " + elapsedTimeMillis); 
    }
    */

    public static void subsetSum(List<Integer> intList, int vectorSize, int idx, int targetSum, Set<Integer> current, List<Set<Integer>> solutions){
        if(current.size() == vectorSize && targetSum == 0){ // if we find a vector of the proper size and correct target sum then we store that value 
            solutions.add(new HashSet(current));
            return; 
        }

        if(current.size() == vectorSize || idx == intList.size() /*|| targetSum <= 0*/) return; // if we have gotten to the last iteration then stop 
        int x = intList.get(idx);
        current.add(x);
        subsetSum(intList, vectorSize, idx + 1, targetSum - x, current, solutions);
        current.remove(x);
        subsetSum(intList, vectorSize, idx + 1, targetSum, current, solutions);
    }

    public static List<Set<Integer>> subsetSum(List<Integer> intList, int vectorSize, int targetSum){
        List<Set<Integer>> solutions = new ArrayList<>();
        subsetSum(intList, vectorSize, 0, targetSum, new HashSet<Integer>(), solutions);
        return solutions; 
    } 

    public static ArrayList<Integer> constructListOfInts(int n){
        ArrayList<Integer> ints = new ArrayList<>();
        for(int i = 1; i <= n * n; i++){
            ints.add(i);
        }
        return ints;
    }

    public static int magicValue(int n){
        return n * (n * n + 1)/2;  
    }    
}