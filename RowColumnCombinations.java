import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RowColumnCombinations{

    public static void rowColumnCombinations(List<List<Set<Integer>>> rowCombinations, int idx, List<List<Set<Integer>>> current, List<List<List<Set<Integer>>>> solution){
        if(current.size() == 2){
            solution.add(new ArrayList(current));
            return;
        }

        if(idx == rowCombinations.size()) return;
        // There are no other fail clauses since we check the condition and if true we add otherwise we skip (ie we reached a leaf)

        boolean condition = true; // innocent until proven guilty
        int count = 0;
        List<Set<Integer>> tempRowCombo = rowCombinations.get(idx);
        for(List<Set<Integer>> currentRowCombination: current){ // this will be empty over the first itteration
            for(Set<Integer> currentRowComboVector: currentRowCombination){ // for each row vector in the current row combination
                for(Set<Integer> tempRowComboVector: tempRowCombo){ // for each row vector in the tempRowCombo
                    for(int num: currentRowComboVector){ // for each number in each row vector in the current row combination 
                        if(tempRowComboVector.contains(num)){
                            count++;
                        }
                    }
                    // once we have check all 5 numbers from one row in the current to another row in the tempRowCombo
                    // if there is more than one count then we break otherwise keep going
                    if(count != 1){
                        condition = false; 
                        break;
                    } else{ // if the count is equal to one then reset the count and continue
                        count = 0; 
                    }

                }
                if(!condition){
                    break;
                }
            }
            if(!condition){
                break;
            }
        }

        if(condition){ // if it is a row and column pairing then add it to current and skip over it otherwise just skip over it. 
            current.add(rowCombinations.get(idx));
            rowColumnCombinations(rowCombinations, idx + 1, new ArrayList(current), solution);
            current.remove(rowCombinations.get(idx));
            rowColumnCombinations(rowCombinations, idx + 1, new ArrayList(current), solution);
        } else{
            rowColumnCombinations(rowCombinations, idx + 1, new ArrayList(current), solution);
        }
    }

    public static List<List<List<Set<Integer>>>> rowColumnCombinations(List<List<Set<Integer>>> rowCombinations){
        List<List<List<Set<Integer>>>> solution = new ArrayList<>();
        List<List<Set<Integer>>> current = new ArrayList<>();
        rowColumnCombinations(rowCombinations, 0, current, solution);

        return solution;
    }

}