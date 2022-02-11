import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RowVectorCombinations {


    // Given a List<Set<Integer>> magic vector solutions, 
    // perform a brute force combination of all of them 
    // the conditions to break should be constructed via a hashmap


    public static void rowVectorCombination(List<Set<Integer>> vectors, int n, int idx, HashMap<List<Set<Integer>>, ArrayList<Integer>> map, List<Set<Integer>> current, List<List<Set<Integer>>> solution){
        if(current.size() == n && map.get(current).size() == 0){ // If we have a solution of size n then we add that solution
            solution.add(new ArrayList(current));
            return;
        }
        
        if(current.size() > n || idx == vectors.size()) return; // failed condition statement.

        boolean condition = true; 
        for(int num: vectors.get(idx)){
            if(!map.get(current).contains(num)){
                condition = false;
                break;
            }
        }
        if(condition){
            ArrayList<Integer> tempList = new ArrayList(map.get(current));
            for(int num: vectors.get(idx)){
                tempList.remove(new Integer(num));
            }
            current.add(vectors.get(idx));
            HashMap<List<Set<Integer>>, ArrayList<Integer>> tempMap = new HashMap<>();
            tempMap.put(new ArrayList(current), new ArrayList(tempList));
            rowVectorCombination(vectors, n, idx + 1, tempMap, current, solution);
            current.remove(vectors.get(idx));
            rowVectorCombination(vectors, n, idx + 1, map, current, solution);
        } else{
            rowVectorCombination(vectors, n, idx + 1, map, current, solution);
        }
    }

    public static List<List<Set<Integer>>> rowVectorCombination(List<Set<Integer>> vectors, int n, ArrayList<Integer> listOfNums){
        List<List<Set<Integer>>> solution = new ArrayList<>();
        HashMap<List<Set<Integer>>, ArrayList<Integer>> map = new HashMap<>();
        List<Set<Integer>> initalCurrent = new ArrayList<>();
        map.put(initalCurrent, listOfNums); // empty inital list, full arrayList

        rowVectorCombination(vectors, n, 0, map, initalCurrent, solution);

        return solution;
    }
    
}
